package entity;

import java.sql.*;
import java.time.LocalDateTime;

public class ParkingSessionDAO {

    /** Creates a new session safely without FK crashing. Returns new sessionID. */
    public int createSession(String vehicleNumber, int lotNumber) throws SQLException {

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int nextId = getNextSessionId(conn);

                // TEMP valid IDs (must exist) to satisfy FK constraints when Access defaults FK to 0.
                int tempSpaceId = getAnyParkingSpaceId(conn, lotNumber);
                int tempConveyorId = getAnyConveyorId(conn, lotNumber);

                String insertSql =
                        "INSERT INTO ParkingSession " +
                                "(sessionID, vehicleNumber, lotNumber, startTime, endTime, calculatedAmount, finalParkingSpaceID, isFreeSession, currentConveyorID, amountPaid) " +
                                "VALUES (?,?,?,?,?,?,?,?,?,?)";

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, nextId);
                    ps.setString(2, vehicleNumber);
                    ps.setInt(3, lotNumber);
                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setNull(5, Types.TIMESTAMP);
                    ps.setInt(6, 0);

                    // TEMP values to pass FK validation
                    ps.setInt(7, tempSpaceId);
                    ps.setBoolean(8, false);
                    ps.setInt(9, tempConveyorId);
                    ps.setInt(10, 0);

                    ps.executeUpdate();
                }

                // Now set them to NULL (unassigned until CC runs)
                String nullFixSql =
                        "UPDATE ParkingSession SET finalParkingSpaceID=NULL, currentConveyorID=NULL WHERE sessionID=?";

                try (PreparedStatement ps2 = conn.prepareStatement(nullFixSql)) {
                    ps2.setInt(1, nextId);
                    ps2.executeUpdate();
                }

                conn.commit();
                return nextId;

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /** Returns the ACTIVE session of the vehicle (endTime is NULL). */
    public ParkingSession getActiveByVehicle(String vehicleNumber) throws SQLException {
        String sql = "SELECT * FROM ParkingSession WHERE vehicleNumber=? AND endTime IS NULL";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, vehicleNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapSession(rs);
            }
        }
    }

    /** Update current conveyor (NULL allowed). */
    public void setCurrentConveyor(int sessionId, Integer conveyorId) throws SQLException {
        String sql = "UPDATE ParkingSession SET currentConveyorID=? WHERE sessionID=?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (conveyorId == null) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, conveyorId);

            ps.setInt(2, sessionId);
            ps.executeUpdate();
        }
    }

    /** Update final parking space (NULL allowed). */
    public void setFinalParkingSpace(int sessionId, Integer spaceId) throws SQLException {
        String sql = "UPDATE ParkingSession SET finalParkingSpaceID=? WHERE sessionID=?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (spaceId == null) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, spaceId);

            ps.setInt(2, sessionId);
            ps.executeUpdate();
        }
    }

    // ----------------- helpers -----------------

    private int getNextSessionId(Connection conn) throws SQLException {
        String sql = "SELECT MAX(sessionID) FROM ParkingSession";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int max = 0;
            if (rs.next()) max = rs.getInt(1);
            return max + 1;
        }
    }

    private int getAnyParkingSpaceId(Connection conn, int lotNumber) throws SQLException {
        String sql = "SELECT TOP 1 spaceID FROM ParkingSpace WHERE lotNumber=? ORDER BY spaceID";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lotNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No ParkingSpaces exist for lotNumber=" + lotNumber);
                return rs.getInt(1);
            }
        }
    }

    private int getAnyConveyorId(Connection conn, int lotNumber) throws SQLException {
        String sql = "SELECT TOP 1 conveyorID FROM Conveyor WHERE lotNumber=? ORDER BY conveyorID";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lotNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No Conveyors exist for lotNumber=" + lotNumber);
                return rs.getInt(1);
            }
        }
    }

    private ParkingSession mapSession(ResultSet rs) throws SQLException {
        ParkingSession s = new ParkingSession();

        s.setSessionID(rs.getInt("sessionID"));
        s.setVehicleNumber(rs.getString("vehicleNumber"));
        s.setLotNumber(rs.getInt("lotNumber"));

        Timestamp start = rs.getTimestamp("startTime");
        if (start != null) s.setStartTime(start.toLocalDateTime());

        Timestamp end = rs.getTimestamp("endTime");
        if (end != null) s.setEndTime(end.toLocalDateTime());

        // optional fields exist in your schema (safe reads)
        try { s.setCalculatedAmount(rs.getInt("calculatedAmount")); } catch (Exception ignored) {}
        try { s.setFreeSession(rs.getBoolean("isFreeSession")); } catch (Exception ignored) {}
        try { s.setAmountPaid(rs.getInt("amountPaid")); } catch (Exception ignored) {}

        // FK nullable: need to detect NULL
        Integer finalSpace = null;
        try {
            int val = rs.getInt("finalParkingSpaceID");
            if (!rs.wasNull()) finalSpace = val;
        } catch (Exception ignored) {}
        s.setFinalParkingSpaceID(finalSpace);

        Integer conveyor = null;
        try {
            int val = rs.getInt("currentConveyorID");
            if (!rs.wasNull()) conveyor = val;
        } catch (Exception ignored) {}
        s.setCurrentConveyorID(conveyor);

        return s;
    }
}
