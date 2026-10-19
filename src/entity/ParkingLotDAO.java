package entity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingLotDAO {

    public List<ParkingLot> getAll() throws SQLException {
        String sql = "SELECT * FROM ParkingLot ORDER BY city, lotNumber";
        List<ParkingLot> list = new ArrayList<>();

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public ParkingLot getById(int lotNumber) throws SQLException {
        String sql = "SELECT * FROM ParkingLot WHERE lotNumber=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, lotNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public void updateLotDetails(int lotNumber, String lotName, String adress, String city) throws SQLException {
        String sql = "UPDATE ParkingLot SET lotName=?, adress=?, city=? WHERE lotNumber=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, lotName);
            ps.setString(2, adress);
            ps.setString(3, city);
            ps.setInt(4, lotNumber);
            ps.executeUpdate();
        }
    }

    public void setAvailableSpaces(int lotNumber, int availableSpaces) throws SQLException {
        String sql = "UPDATE ParkingLot SET availableSpaces=? WHERE lotNumber=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, availableSpaces);
            ps.setInt(2, lotNumber);
            ps.executeUpdate();
        }
    }

    public void decrementAvailableSpaces(int lotNumber) throws SQLException {
        String sql = "UPDATE ParkingLot SET availableSpaces = IIF(availableSpaces>0, availableSpaces-1, 0) WHERE lotNumber=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, lotNumber);
            ps.executeUpdate();
        }
    }

    public void incrementAvailableSpaces(int lotNumber) throws SQLException {
        String sql = "UPDATE ParkingLot SET availableSpaces = availableSpaces+1 WHERE lotNumber=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, lotNumber);
            ps.executeUpdate();
        }
    }

    public void setIsAvilaible(int lotNumber, boolean isAvilaible) throws SQLException {
        String sql = "UPDATE ParkingLot SET isAvilaible=? WHERE lotNumber=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, isAvilaible);
            ps.setInt(2, lotNumber);
            ps.executeUpdate();
        }
    }

    private ParkingLot map(ResultSet rs) throws SQLException {
        ParkingLot pl = new ParkingLot();
        pl.setLotNumber(rs.getInt("lotNumber"));
        pl.setLotName(rs.getString("lotName"));
        pl.setAdress(rs.getString("adress"));
        pl.setCity(rs.getString("city"));
        pl.setAvailableSpaces(rs.getInt("availableSpaces"));
        pl.setAvilaible(rs.getBoolean("isAvilaible"));
        return pl;
    }
}
