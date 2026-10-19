package entity;

import java.sql.*;
import java.util.LinkedHashSet;
import java.util.Set;

public class PreferredLotSelectionDAO {

    /** Save a NEW batch (audit). We do not delete old rows. */
    public void insertBatch(int memberId, Set<Integer> lotNumbers) throws SQLException {
        Timestamp batchTime = new Timestamp(System.currentTimeMillis());

        String sql = "INSERT INTO PreferredLotSelection (memberID, lotNumber, selectionDat) VALUES (?, ?, ?)";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            for (int lot : lotNumbers) {
                ps.setInt(1, memberId);
                ps.setInt(2, lot);
                ps.setTimestamp(3, batchTime);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Latest selection time (<= atTime if provided, else latest overall). */
    public Timestamp getLatestSelectionTime(int memberId, Timestamp atTimeOrNull) throws SQLException {
        String sql =
                (atTimeOrNull == null)
                        ? "SELECT MAX(selectionDat) AS mx FROM PreferredLotSelection WHERE memberID=?"
                        : "SELECT MAX(selectionDat) AS mx FROM PreferredLotSelection WHERE memberID=? AND selectionDat<=?";

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, memberId);
            if (atTimeOrNull != null) ps.setTimestamp(2, atTimeOrNull);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getTimestamp("mx"); // can be null if never selected
            }
        }
    }

    /** Lots for a specific batch timestamp. */
    public Set<Integer> getLotsForBatch(int memberId, Timestamp batchTime) throws SQLException {
        Set<Integer> set = new LinkedHashSet<>();
        if (batchTime == null) return set;

        String sql =
                "SELECT lotNumber FROM PreferredLotSelection " +
                        "WHERE memberID=? AND selectionDat=? " +
                        "ORDER BY lotNumber ASC";

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setTimestamp(2, batchTime);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) set.add(rs.getInt("lotNumber"));
            }
        }
        return set;
    }

    /** Current preferred lots at time (or now if atTimeOrNull is null). */
    public Set<Integer> getCurrentPreferredLots(int memberId, Timestamp atTimeOrNull) throws SQLException {
        Timestamp t = getLatestSelectionTime(memberId, atTimeOrNull);
        return getLotsForBatch(memberId, t);
    }
}
