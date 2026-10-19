package entity;

import java.sql.*;

public class VehicleDAO {

    public Vehicle getByNumber(String vehicleNumber) throws SQLException {
        String sql = "SELECT vehicleNumber, type, color, size, weightKg, clientID FROM Vehicle WHERE vehicleNumber=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, vehicleNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public boolean exists(String vehicleNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Vehicle WHERE vehicleNumber=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, vehicleNumber);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** Insert a vehicle (photos are not stored in DB for now). */
    public void insert(Vehicle v) throws SQLException {
        String sql = "INSERT INTO Vehicle (vehicleNumber, type, color, size, weightKg, clientID) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, v.getVehicleNumber());
            ps.setString(2, v.getType());
            ps.setString(3, v.getColor());
            ps.setString(4, v.getSize());
            ps.setInt(5, v.getWeightKg());
            ps.setInt(6, v.getClientID());
            ps.executeUpdate();
        }
    }

    public void update(Vehicle v) throws SQLException {
        String sql = "UPDATE Vehicle SET type=?, color=?, size=?, weightKg=?, clientID=? WHERE vehicleNumber=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, v.getType());
            ps.setString(2, v.getColor());
            ps.setString(3, v.getSize());
            ps.setInt(4, v.getWeightKg());
            ps.setInt(5, v.getClientID());
            ps.setString(6, v.getVehicleNumber());
            ps.executeUpdate();
        }
    }

    public void upsert(Vehicle v) throws SQLException {
        if (exists(v.getVehicleNumber())) update(v);
        else insert(v);
    }

    /** Requirement: each vehicle is associated with exactly one client. */
    public void setClientForVehicle(String vehicleNumber, int clientID) throws SQLException {
        String sql = "UPDATE Vehicle SET clientID=? WHERE vehicleNumber=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, clientID);
            ps.setString(2, vehicleNumber);
            ps.executeUpdate();
        }
    }

    private Vehicle map(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setVehicleNumber(rs.getString("vehicleNumber"));
        v.setType(rs.getString("type"));
        v.setColor(rs.getString("color"));
        v.setSize(rs.getString("size"));
        v.setWeightKg(rs.getInt("weightKg"));
        v.setClientID(rs.getInt("clientID"));
        return v;
    }
}
