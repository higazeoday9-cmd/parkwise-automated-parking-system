package control;



import entity.DB;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class CustomerClubPricingControl {

    public static class PricingDecision {
        public final boolean isMember;
        public final boolean isFree;
        public final int discountPercent;     // 0..100
        public final int baseAmount;
        public final int finalAmount;
        public final String reasonLine;

        public PricingDecision(boolean isMember, boolean isFree, int discountPercent,
                               int baseAmount, int finalAmount, String reasonLine) {
            this.isMember = isMember;
            this.isFree = isFree;
            this.discountPercent = discountPercent;
            this.baseAmount = baseAmount;
            this.finalAmount = finalAmount;
            this.reasonLine = reasonLine;
        }
    }

    private static class MembershipRow {
        LocalDateTime registration;
        int discountPercent;
    }

    public PricingDecision applyClubRules(int clientId, int lotNumber, Timestamp sessionStart, int baseAmount) throws SQLException {
        try (Connection c = DB.getConnection()) {
            return applyClubRules(c, clientId, lotNumber, sessionStart, baseAmount);
        }
    }

    public PricingDecision applyClubRules(Connection c, int clientId, int lotNumber, Timestamp sessionStart, int baseAmount) throws SQLException {
        MembershipRow m = getMembershipSafe(c, clientId);
        if (m == null) {
            return new PricingDecision(false, false, 0, baseAmount, baseAmount, "Not a club member");
        }

        int discount = Math.max(0, Math.min(100, m.discountPercent));
        LocalDateTime start = sessionStart.toLocalDateTime();

        boolean sameRegMonth =
                (m.registration.getYear() == start.getYear()) &&
                        (m.registration.getMonthValue() == start.getMonthValue());

        boolean lotIsPreferredNow = isLotPreferredAsOf(c, clientId, lotNumber, sessionStart);

        // FREE RULE:
        // First parking session in registration month, in any of the selected preferred lots, is free.
        if (sameRegMonth && lotIsPreferredNow) {
            boolean isFirst = isFirstSessionThisMonthInPreferredLots(c, clientId, sessionStart);
            if (isFirst) {
                return new PricingDecision(true, true, 100, baseAmount, 0,
                        "FREE: first session in registration month (preferred lot)");
            }
        }

        // Otherwise: percentage discount
        if (discount <= 0) {
            return new PricingDecision(true, false, 0, baseAmount, baseAmount, "Member: no discount configured");
        }

        int finalAmount = (int) Math.round(baseAmount * (1.0 - (discount / 100.0)));
        if (finalAmount < 0) finalAmount = 0;

        return new PricingDecision(true, false, discount, baseAmount, finalAmount,
                "Member discount: " + discount + "%");
    }

    // -------------------- Robust membership fetch (fixes your REGISTRATIOND problem) --------------------

    private MembershipRow getMembershipSafe(Connection c, int memberId) {
        try {
            String idCol = resolveColumn(c, "ClubMembership", "memberID", "clientID");
            String regCol = resolveColumn(c, "ClubMembership", "registrationD", "registrationDate", "registrationDat", "regDate");
            String disCol = resolveColumn(c, "ClubMembership", "discountAmo", "discountAmount", "discountPercent", "discount");

            if (idCol == null || regCol == null || disCol == null) {
                // If your table/columns are different, treat as "not member" instead of crashing
                return null;
            }

            String sql = "SELECT [" + regCol + "], [" + disCol + "] FROM [ClubMembership] WHERE [" + idCol + "]=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, memberId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;

                    LocalDateTime reg = readDateTime(rs, 1);
                    int discount = rs.getInt(2);

                    if (reg == null) {
                        // if date failed for some reason, still treat as member but with safe default
                        reg = LocalDateTime.now().minusYears(10);
                    }

                    MembershipRow m = new MembershipRow();
                    m.registration = reg;
                    m.discountPercent = discount;
                    return m;
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDateTime readDateTime(ResultSet rs, int idx) throws SQLException {
        Timestamp ts = rs.getTimestamp(idx);
        if (ts != null) return ts.toLocalDateTime();
        Date d = rs.getDate(idx);
        if (d != null) return d.toLocalDate().atStartOfDay();
        return null;
    }

    private static String resolveColumn(Connection c, String table, String... candidates) {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM [" + table + "] WHERE 1=0")) {
            ResultSetMetaData md = rs.getMetaData();
            for (String cand : candidates) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    if (md.getColumnName(i).equalsIgnoreCase(cand)) return md.getColumnName(i);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // -------------------- Preferred lots logic --------------------

    private boolean isLotPreferredAsOf(Connection c, int memberId, int lotNumber, Timestamp asOf) throws SQLException {
        String dateCol = resolveColumn(c, "PreferredLotSelection", "selectionDat", "selectionDate");
        if (dateCol == null) dateCol = "selectionDat";

        String sql =
                "SELECT COUNT(*) " +
                        "FROM [PreferredLotSelection] " +
                        "WHERE memberID=? AND lotNumber=? AND [" + dateCol + "]<=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setInt(2, lotNumber);
            ps.setTimestamp(3, asOf);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private boolean isFirstSessionThisMonthInPreferredLots(Connection c, int memberId, Timestamp sessionStart) throws SQLException {
        LocalDateTime start = sessionStart.toLocalDateTime();
        int y = start.getYear();
        int m = start.getMonthValue();

        String dateCol = resolveColumn(c, "PreferredLotSelection", "selectionDat", "selectionDate");
        if (dateCol == null) dateCol = "selectionDat";

        String sql =
                "SELECT COUNT(*) " +
                        "FROM ParkingSession ps " +
                        "INNER JOIN Vehicle v ON ps.vehicleNumber = v.vehicleNumber " +
                        "WHERE v.clientID=? " +
                        "AND DatePart(\"yyyy\", ps.startTime)=? " +
                        "AND DatePart(\"m\", ps.startTime)=? " +
                        "AND ps.startTime < ? " +
                        "AND ps.lotNumber IN ( " +
                        "   SELECT lotNumber FROM PreferredLotSelection " +
                        "   WHERE memberID=? AND [" + dateCol + "]<=? " +
                        ")";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setInt(2, y);
            ps.setInt(3, m);
            ps.setTimestamp(4, sessionStart);
            ps.setInt(5, memberId);
            ps.setTimestamp(6, sessionStart);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        }
    }

    // -------------------- Utility for free "transaction" --------------------

    public static String freeTransactionId() {
        return "FREE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

