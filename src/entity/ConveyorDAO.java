package entity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConveyorDAO {

    public int countConveyors() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Conveyor";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public List<Integer> getAllConveyorIds() throws SQLException {
        String sql = "SELECT conveyorID FROM Conveyor ORDER BY conveyorID";
        List<Integer> ids = new ArrayList<>();
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ids.add(rs.getInt(1));
        }
        return ids;
    }

    public Conveyor getById(int id) throws SQLException {
        String sql = "SELECT * FROM Conveyor WHERE conveyorID=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Conveyor conv = new Conveyor();
                conv.setConveyorID(rs.getInt("conveyorID"));
                conv.setLotNumber(rs.getInt("lotNumber"));
                conv.setMaxWeightKg(rs.getInt("maxWeightKg"));
                conv.setFloorNumber(rs.getInt("floorNumber"));
                conv.setPositionX(rs.getInt("positionX"));
                conv.setPositionY(rs.getInt("positionY"));

                conv.setState(rs.getString("state"));
                conv.setAttemptCount(rs.getInt("attemptCount"));
                conv.setBusy(rs.getBoolean("isBusy"));

                conv.setPausedFromState(rs.getString("pausedFromState"));

                Integer pending = (Integer) rs.getObject("pendingMaxWeightKg");
                conv.setPendingMaxWeightKg(pending);

                conv.setWeightConfirmed(rs.getBoolean("weightConfirmed"));
                conv.setPendingWeight(rs.getBoolean("pendingWeight"));
                conv.setLastFailureReason(rs.getString("lastFailureReason"));

                return conv;
            }
        }
    }

    /** Main method used by GUI (kept name so your ConveyorControl compiles). */
    public List<Conveyor> getAllConveyors() throws SQLException {
        return getAll();
    }

    /** Canonical list method (single source of truth). */
    public List<Conveyor> getAll() throws SQLException {
        String sql = "SELECT * FROM Conveyor ORDER BY conveyorID";
        List<Conveyor> list = new ArrayList<>();

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Conveyor conv = new Conveyor();
                conv.setConveyorID(rs.getInt("conveyorID"));
                conv.setLotNumber(rs.getInt("lotNumber"));
                conv.setMaxWeightKg(rs.getInt("maxWeightKg"));
                conv.setFloorNumber(rs.getInt("floorNumber"));
                conv.setPositionX(rs.getInt("positionX"));
                conv.setPositionY(rs.getInt("positionY"));

                conv.setState(rs.getString("state"));
                conv.setAttemptCount(rs.getInt("attemptCount"));
                conv.setBusy(rs.getBoolean("isBusy"));

                conv.setPausedFromState(rs.getString("pausedFromState"));
                conv.setPendingMaxWeightKg((Integer) rs.getObject("pendingMaxWeightKg"));
                conv.setWeightConfirmed(rs.getBoolean("weightConfirmed"));
                conv.setPendingWeight(rs.getBoolean("pendingWeight"));
                conv.setLastFailureReason(rs.getString("lastFailureReason"));

                list.add(conv);
            }
        }
        return list;
    }

    public void updateState(int id, String newState) throws SQLException {
        String sql = "UPDATE Conveyor SET state=? WHERE conveyorID=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newState);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void updateBusy(int id, boolean busy) throws SQLException {
        String sql = "UPDATE Conveyor SET isBusy=? WHERE conveyorID=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, busy);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void updateAttemptCount(int id, int attemptCount) throws SQLException {
        String sql = "UPDATE Conveyor SET attemptCount=? WHERE conveyorID=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, attemptCount);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void setPausedFromState(int id, String pausedFromState) throws SQLException {
        String sql = "UPDATE Conveyor SET pausedFromState=? WHERE conveyorID=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pausedFromState);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void setPendingMaxWeight(int id, Integer pendingMaxWeightKg) throws SQLException {
        String sql = "UPDATE Conveyor SET pendingMaxWeightKg=? WHERE conveyorID=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (pendingMaxWeightKg == null) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, pendingMaxWeightKg);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void setWeightConfirmed(int id, boolean confirmed) throws SQLException {
        String sql = "UPDATE Conveyor SET weightConfirmed=? WHERE conveyorID=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, confirmed);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void setPendingWeight(int id, boolean pending) throws SQLException {
        String sql = "UPDATE Conveyor SET pendingWeight=? WHERE conveyorID=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, pending);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void setLastFailureReason(int id, String reason) throws SQLException {
        String sql = "UPDATE Conveyor SET lastFailureReason=? WHERE conveyorID=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void updateMaxWeight(int id, int newMaxWeightKg) throws SQLException {
        String sql = "UPDATE Conveyor SET maxWeightKg=? WHERE conveyorID=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, newMaxWeightKg);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
    public void updateLocation(int conveyorID, int floorNumber, int positionX, int positionY) throws SQLException {
        String sql = "UPDATE Conveyor SET floorNumber=?, positionX=?, positionY=? WHERE conveyorID=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, floorNumber);
            ps.setInt(2, positionX);
            ps.setInt(3, positionY);
            ps.setInt(4, conveyorID);
            ps.executeUpdate();
        }
    }

}
