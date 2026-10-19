package entity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingSpaceDAO {

    public List<ParkingSpace> getAllByLot(int lotNumber) throws SQLException {
        String sql = "SELECT * FROM ParkingSpace WHERE lotNumber=? ORDER BY floorNumber, positionX, positionY";
        List<ParkingSpace> list = new ArrayList<>();
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, lotNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Returns spaces that are not occupied by an active session AND size is compatible.
     * We define "occupied" as a space that appears in an active ParkingSession (endTime IS NULL).
     */
    public List<ParkingSpace> getVacantCompatibleSpaces(int lotNumber, String vehicleSize) throws SQLException {
        String sql =
                "SELECT s.* " +
                        "FROM ParkingSpace s " +
                        "WHERE s.lotNumber=? " +
                        "AND " + sizeCompatibilityWhereClause() + " " +
                        "AND s.spaceID NOT IN (" +
                        "   SELECT finalParkingSpaceID FROM ParkingSession " +
                        "   WHERE endTime IS NULL AND finalParkingSpaceID IS NOT NULL" +
                        ") " +
                        "ORDER BY s.floorNumber, s.positionX, s.positionY";

        List<ParkingSpace> list = new ArrayList<>();
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, lotNumber);
            ps.setString(2, vehicleSize);
            ps.setString(3, vehicleSize);
            ps.setString(4, vehicleSize);


            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Compatibility rule (simple + safe):
     * - small fits in small/medium/large
     * - medium fits in medium/large
     * - large fits in large
     */
    private String sizeCompatibilityWhereClause() {
        // uses a single parameter (?) = vehicleSize
        return "(" +
                " (?='small' AND (s.size='small' OR s.size='medium' OR s.size='large')) OR " +
                " (?='medium' AND (s.size='medium' OR s.size='large')) OR " +
                " (?='large' AND (s.size='large'))" +
                ")";
    }

    private ParkingSpace map(ResultSet rs) throws SQLException {
        ParkingSpace s = new ParkingSpace();
        s.setSpaceID(rs.getInt("spaceID"));
        s.setLotNumber(rs.getInt("lotNumber"));
        s.setFloorNumber(rs.getInt("floorNumber"));
        s.setPositionX(rs.getInt("positionX"));
        s.setPositionY(rs.getInt("positionY"));
        s.setSize(rs.getString("size"));
        return s;
    }
}
