package control;



import entity.DB;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class EndParkingControl {

    private final PaymentGatewayStub pg = new PaymentGatewayStub();
    private final BillingCalculator calc = new BillingCalculator();
    private final CustomerClubPricingControl clubPricing = new CustomerClubPricingControl();

    public EndParkingResult endParking(String vehicleNumberRaw, String phoneRaw, boolean wantReceipt) {
        try {
            String vehicleNumber = (vehicleNumberRaw == null) ? "" : vehicleNumberRaw.trim();
            String phone = (phoneRaw == null) ? "" : phoneRaw.trim();

            if (vehicleNumber.isEmpty()) return EndParkingResult.fail("Vehicle number is required.");
            if (phone.isEmpty()) return EndParkingResult.fail("Phone number is required.");

            try (Connection c = DB.getConnection()) {
                c.setAutoCommit(false);

                try {
                    // 1) Validate phone matches vehicle's client AND fetch clientId
                    Integer clientId = validateAndGetClientId(c, vehicleNumber, phone);
                    if (clientId == null) {
                        c.rollback();
                        return EndParkingResult.fail("Validation failed: phone does not match this vehicle.");
                    }

                    // 2) Find active session
                    SessionRow s = getActiveSession(c, vehicleNumber);
                    if (s == null) {
                        c.rollback();
                        return EndParkingResult.fail("No ACTIVE session for this vehicle.");
                    }

                    // 3) Compute duration
                    LocalDateTime now = LocalDateTime.now();
                    long minutes = Duration.between(s.startTime.toLocalDateTime(), now).toMinutes();
                    if (minutes < 1) minutes = 1;

                    // 4) Load rate for lot + current year
                    int year = now.getYear();
                    RateRow rate = getRateForLotYear(c, s.lotNumber, year);
                    if (rate == null) {
                        c.rollback();
                        return EndParkingResult.fail("No pricing rate found for lot " + s.lotNumber + " (year " + year + ").");
                    }

                    BillingCalculator.Bill bill = calc.calculate(minutes, rate.firstHour, rate.addHour, rate.fullDay);

                    int beforeDiscount = bill.amount;

                    // 5) Apply Customer Club rules (discount + possible free session)
                    CustomerClubPricingControl.PricingDecision decision =
                            clubPricing.applyClubRules(c, clientId, s.lotNumber, s.startTime, beforeDiscount);

                    int finalAmount = decision.finalAmount;
                    int discountPercent = decision.discountPercent;
                    String reasonLine = decision.reasonLine;

                    // 6) Payment (skip PG if free)
                    boolean approved;
                    String transactionId;
                    if (finalAmount <= 0) {
                        approved = true;
                        transactionId = CustomerClubPricingControl.freeTransactionId();
                    } else {
                        PaymentGatewayStub.PaymentResponse pay = pg.charge(phone, finalAmount);
                        approved = pay.approved;
                        transactionId = pay.transactionId;

                        if (!approved) {
                            c.rollback();
                            return EndParkingResult.fail("Payment declined: " + pay.message);
                        }
                    }

                    // 7) End session + store amounts + clear location fields
                    endSession(c, s.sessionId, now, beforeDiscount, finalAmount);

                    // (Optional) If you track availableSpaces in ParkingLot, you can increment it.
                    // Only do it if there *was* a parked space.
                    if (s.finalSpaceId != null) {
                        incrementLotSpaces(c, s.lotNumber, 1);
                    }

                    c.commit();

                    EndParkingResult out = EndParkingResult.ok("Payment approved. Gate opened.");
                    out.sessionId = s.sessionId;
                    out.amountBeforeDiscount = beforeDiscount;
                    out.amountFinal = finalAmount;
                    out.totalHours = bill.totalHours;
                    out.transactionId = transactionId;


                    if (wantReceipt) {
                        out.receiptText = buildReceiptText(
                                vehicleNumber, phone, s.lotNumber, s.startTime, now,
                                bill.totalHours, bill.rateUsed,
                                beforeDiscount, discountPercent, finalAmount, reasonLine,
                                transactionId
                        );
                    }

                    return out;

                } catch (Exception ex) {
                    c.rollback();
                    return EndParkingResult.fail("End parking failed: " + ex.getMessage());
                } finally {
                    c.setAutoCommit(true);
                }
            }

        } catch (Exception e) {
            return EndParkingResult.fail("End parking failed: " + e.getMessage());
        }
    }

    // -------------------- DB rows --------------------

    private static class SessionRow {
        int sessionId;
        int lotNumber;
        Timestamp startTime;
        Integer finalSpaceId;
    }

    private static class RateRow {
        int firstHour;
        int addHour;
        int fullDay;
    }

    // -------------------- DB ops --------------------

    private Integer validateAndGetClientId(Connection c, String vehicleNumber, String phone) throws SQLException {
        // try phoneNumber column first; fallback mobilePhone (your DB seems to use mobilePhone)
        try {
            return validateAndGetClientIdCol(c, vehicleNumber, phone, "phoneNumber");
        } catch (SQLException ex) {
            return validateAndGetClientIdCol(c, vehicleNumber, phone, "mobilePhone");
        }
    }

    private Integer validateAndGetClientIdCol(Connection c, String vehicleNumber, String phone, String phoneCol) throws SQLException {
        String sql =
                "SELECT cl.clientID " +
                        "FROM Vehicle v INNER JOIN Client cl ON v.clientID = cl.clientID " +
                        "WHERE v.vehicleNumber=? AND cl." + phoneCol + "=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, vehicleNumber);
            ps.setString(2, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt(1);
            }
        }
    }

    private SessionRow getActiveSession(Connection c, String vehicleNumber) throws SQLException {
        String sql = "SELECT sessionID, lotNumber, startTime, finalParkingSpaceID " +
                "FROM ParkingSession WHERE vehicleNumber=? AND endTime IS NULL";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, vehicleNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                SessionRow s = new SessionRow();
                s.sessionId = rs.getInt("sessionID");
                s.lotNumber = rs.getInt("lotNumber");
                s.startTime = rs.getTimestamp("startTime");

                int sp = rs.getInt("finalParkingSpaceID");
                s.finalSpaceId = rs.wasNull() ? null : sp;

                return s;
            }
        }
    }

    private RateRow getRateForLotYear(Connection c, int lotNumber, int year) throws SQLException {
        // Prefer latest effectiveYear <= year
        String sql = "SELECT TOP 1 priceFirstHour, priceAdditionalHour, priceFullDay " +
                "FROM PriceRate WHERE lotNumber=? AND effectiveYear<=? ORDER BY effectiveYear DESC";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, lotNumber);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RateRow r = new RateRow();
                    r.firstHour = rs.getInt("priceFirstHour");
                    r.addHour = rs.getInt("priceAdditionalHour");
                    r.fullDay = rs.getInt("priceFullDay");
                    return r;
                }
            }
        }

        // fallback: latest regardless of year
        String sql2 = "SELECT TOP 1 priceFirstHour, priceAdditionalHour, priceFullDay " +
                "FROM PriceRate WHERE lotNumber=? ORDER BY effectiveYear DESC";
        try (PreparedStatement ps = c.prepareStatement(sql2)) {
            ps.setInt(1, lotNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                RateRow r = new RateRow();
                r.firstHour = rs.getInt("priceFirstHour");
                r.addHour = rs.getInt("priceAdditionalHour");
                r.fullDay = rs.getInt("priceFullDay");
                return r;
            }
        }
    }

    private void incrementLotSpaces(Connection c, int lotNumber, int delta) throws SQLException {
        String sql = "UPDATE ParkingLot SET availableSpaces = availableSpaces + ? WHERE lotNumber=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, lotNumber);
            ps.executeUpdate();
        }
    }

    private void endSession(Connection c, int sessionId, LocalDateTime endTime, int calculatedAmount, int amountPaid) throws SQLException {
        String sql = "UPDATE ParkingSession SET endTime=?, calculatedAmount=?, amountPaid=?, " +
                "finalParkingSpaceID=NULL, currentConveyorID=NULL WHERE sessionID=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(endTime));
            ps.setInt(2, calculatedAmount);
            ps.setInt(3, amountPaid);
            ps.setInt(4, sessionId);
            ps.executeUpdate();
        }
    }

    private String buildReceiptText(String vehicleNumber, String phone, int lotNumber,
                                    Timestamp start, LocalDateTime end,
                                    int hours, String rateUsed,
                                    int beforeDiscount, int discountPercent,
                                    int finalAmount, String reasonLine,
                                    String tx) {

        return """
                ===== ParkWise Receipt =====
                Vehicle: %s
                Phone: %s
                Lot: %d

                Start: %s
                End: %s
                Total hours: %d
                Rate: %s

                Amount before discount: %d
                Discount: %d%%
                Final amount: %d
                Note: %s

                Transaction: %s
                ===========================
                """.formatted(
                vehicleNumber, phone, lotNumber,
                start, end,
                hours, rateUsed,
                beforeDiscount, discountPercent, finalAmount,
                reasonLine,
                tx
        );
    }
}
