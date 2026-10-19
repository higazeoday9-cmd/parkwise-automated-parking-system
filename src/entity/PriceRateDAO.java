package entity;

import java.sql.*;

public class PriceRateDAO {

    public boolean exists(int priceListID, int lotNumber, int effectiveYear) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PriceRate WHERE priceListID=? AND lotNumber=? AND effectiveYear=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, priceListID);
            ps.setInt(2, lotNumber);
            ps.setInt(3, effectiveYear);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void insert(int priceListID, int lotNumber, int effectiveYear,
                       int firstHour, int additionalHour, int fullDay) throws SQLException {
        String sql = "INSERT INTO PriceRate (priceListID, lotNumber, effectiveYear, priceFirstHour, priceAdditionalHour, priceFullDay) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, priceListID);
            ps.setInt(2, lotNumber);
            ps.setInt(3, effectiveYear);
            ps.setInt(4, firstHour);
            ps.setInt(5, additionalHour);
            ps.setInt(6, fullDay);
            ps.executeUpdate();
        }
    }
    public java.util.List<PriceRate> getAll() throws SQLException {
        String sql = "SELECT * FROM PriceRate ORDER BY lotNumber, effectiveYear";
        java.util.List<PriceRate> list = new java.util.ArrayList<>();

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PriceRate pr = new PriceRate();
                pr.setPriceListID(rs.getInt("priceListID"));
                pr.setLotNumber(rs.getInt("lotNumber"));
                pr.setEffectiveYear(rs.getInt("effectiveYear"));
                pr.setPriceFirstHour(rs.getInt("priceFirstHour"));
                pr.setPriceAdditionalHour(rs.getInt("priceAdditionalHour"));
                pr.setPriceFullDay(rs.getInt("priceFullDay"));
                list.add(pr);
            }
        }
        return list;
    }

}
