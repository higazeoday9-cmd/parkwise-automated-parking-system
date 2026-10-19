package control;

import entity.ClubMembership;
import entity.ClubMembershipDAO;
import entity.PreferredLotSelectionDAO;
import entity.DB;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

public class CustomerClubControl {

    // default if you want a fixed discount when registering
    public static final int DEFAULT_DISCOUNT_PERCENT = 10;

    private final ClubMembershipDAO membershipDAO = new ClubMembershipDAO();
    private final PreferredLotSelectionDAO prefDAO = new PreferredLotSelectionDAO();

    public ClubMembership getMembership(int clientId) throws SQLException {
        return membershipDAO.find(clientId);
    }

    public boolean isMember(int clientId) throws SQLException {
        return membershipDAO.find(clientId) != null;
    }

    public void registerMember(int clientId) throws SQLException {
        membershipDAO.registerIfMissing(clientId, DEFAULT_DISCOUNT_PERCENT);
    }

    public Set<Integer> getCurrentPreferredLots(int clientId) throws SQLException {
        return prefDAO.getCurrentPreferredLots(clientId, null);
    }

    public void savePreferredLots(int clientId, Set<Integer> lots) throws SQLException {
        prefDAO.insertBatch(clientId, lots); // audit-friendly
    }

    /** pricing decision object */
    public static class PricingDecision {
        public int baseAmount;
        public int discountPercent;
        public int finalAmount;
        public boolean free;
        public String reason;
    }

    /**
     * Club rules:
     * - Members get discountAmo (%) always
     * - First session in registration month, in a selected lot, is FREE
     */
    public PricingDecision applyClubRules(int clientId, int lotNumber, Timestamp sessionStartTime, int baseAmount) throws SQLException {

        PricingDecision d = new PricingDecision();
        d.baseAmount = baseAmount;
        d.discountPercent = 0;
        d.finalAmount = baseAmount;
        d.free = false;
        d.reason = "not a member";

        ClubMembership m = membershipDAO.find(clientId);
        if (m == null) return d;

        // member discount from DB
        d.discountPercent = m.getDiscountAmo();
        d.reason = "club discount";

        // preferred lots at the time of session start (more correct than using latest-now)
        Set<Integer> preferredAtStart = prefDAO.getCurrentPreferredLots(clientId, sessionStartTime);
        boolean lotSelected = preferredAtStart.contains(lotNumber);

        LocalDate join = m.getRegistrationD().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate start = sessionStartTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (lotSelected && join.getYear() == start.getYear() && join.getMonthValue() == start.getMonthValue()) {
            boolean alreadyHadSessionThisMonth = hasAnySessionForClientInMonth(clientId, join.getYear(), join.getMonthValue());
            if (!alreadyHadSessionThisMonth) {
                d.free = true;
                d.discountPercent = 100;
                d.finalAmount = 0;
                d.reason = "FREE (first session in registration month, selected lot)";
                return d;
            }
        }

        int discounted = (int) Math.round(baseAmount * (100.0 - d.discountPercent) / 100.0);
        d.finalAmount = Math.max(0, discounted);
        return d;
    }

    private boolean hasAnySessionForClientInMonth(int clientId, int year, int month) throws SQLException {
        String sql =
                "SELECT COUNT(*) " +
                        "FROM (ParkingSession ps INNER JOIN Vehicle v ON ps.vehicleNumber=v.vehicleNumber) " +
                        "WHERE v.clientID=? AND YEAR(ps.startTime)=? AND MONTH(ps.startTime)=?";

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
