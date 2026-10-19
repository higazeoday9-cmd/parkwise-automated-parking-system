package entity;

import java.sql.*;

public class ClubMembershipDAO {

    public ClubMembership find(int memberId) throws SQLException {
        String sql = "SELECT memberID, registrationD, discountAmo FROM ClubMembership WHERE memberID=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new ClubMembership(
                        rs.getInt("memberID"),
                        rs.getTimestamp("registrationD"),
                        rs.getInt("discountAmo")
                );
            }
        }
    }

    public void registerIfMissing(int memberId, int discountPercent) throws SQLException {
        if (find(memberId) != null) return;

        String sql = "INSERT INTO ClubMembership (memberID, registrationD, discountAmo) VALUES (?, ?, ?)";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, discountPercent);
            ps.executeUpdate();
        }
    }
}

