package control;

import entity.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingSessionViewControl {

    public List<ParkingSessionViewRow> search(String vehiclePrefix,
                                              String phone,
                                              Integer lotNumber,
                                              boolean activeOnly) throws SQLException {

        // phone column name differs sometimes, so try phoneNumber then mobilePhone
        try {
            return searchWithPhoneColumn(vehiclePrefix, phone, lotNumber, activeOnly, "phoneNumber");
        } catch (SQLException ex) {
            return searchWithPhoneColumn(vehiclePrefix, phone, lotNumber, activeOnly, "mobilePhone");
        }
    }

    private List<ParkingSessionViewRow> searchWithPhoneColumn(String vehiclePrefix,
                                                              String phone,
                                                              Integer lotNumber,
                                                              boolean activeOnly,
                                                              String phoneCol) throws SQLException {

        String sql =
                "SELECT " +
                        "ps.sessionID, ps.vehicleNumber, ps.lotNumber, ps.startTime, ps.endTime, " +
                        "ps.finalParkingSpaceID, ps.currentConveyorID, " +
                        "pl.lotName, pl.city, " +
                        "cl.clientID, cl.firstName, cl.lastName, cl." + phoneCol + " AS phone " +
                        "FROM " +
                        "(((ParkingSession ps " +
                        "INNER JOIN Vehicle v ON ps.vehicleNumber = v.vehicleNumber) " +
                        "INNER JOIN Client cl ON v.clientID = cl.clientID) " +
                        "INNER JOIN ParkingLot pl ON ps.lotNumber = pl.lotNumber) " +
                        "WHERE 1=1 ";

        List<Object> params = new ArrayList<>();

        if (vehiclePrefix != null && !vehiclePrefix.trim().isEmpty()) {
            sql += " AND ps.vehicleNumber LIKE ? ";
            params.add(vehiclePrefix.trim() + "%");
        }

        if (phone != null && !phone.trim().isEmpty()) {
            sql += " AND cl." + phoneCol + " = ? ";
            params.add(phone.trim());
        }

        if (lotNumber != null) {
            sql += " AND ps.lotNumber = ? ";
            params.add(lotNumber);
        }

        if (activeOnly) {
            sql += " AND ps.endTime IS NULL ";
        }

        sql += " ORDER BY ps.startTime DESC";

        List<ParkingSessionViewRow> out = new ArrayList<>();

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                Object v = params.get(i);
                if (v instanceof Integer) ps.setInt(i + 1, (Integer) v);
                else ps.setString(i + 1, String.valueOf(v));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ParkingSessionViewRow r = new ParkingSessionViewRow();

                    r.sessionId = rs.getInt("sessionID");
                    r.vehicleNumber = rs.getString("vehicleNumber");

                    r.lotNumber = rs.getInt("lotNumber");
                    r.lotName = rs.getString("lotName");
                    r.city = rs.getString("city");

                    r.startTime = rs.getTimestamp("startTime");
                    r.endTime = rs.getTimestamp("endTime");
                    r.active = (r.endTime == null);

                    int sp = rs.getInt("finalParkingSpaceID");
                    r.finalSpaceId = rs.wasNull() ? null : sp;

                    int cv = rs.getInt("currentConveyorID");
                    r.currentConveyorId = rs.wasNull() ? null : cv;

                    r.clientId = rs.getInt("clientID");
                    String fn = rs.getString("firstName");
                    String ln = rs.getString("lastName");
                    r.clientName = ((fn == null ? "" : fn) + " " + (ln == null ? "" : ln)).trim();
                    r.phone = rs.getString("phone");

                    // optional fields (if exist in your schema)
                    r.calculatedAmount = tryGetInt(rs, "calculatedAmount");
                    r.amountPaid = tryGetInt(rs, "amountPaid");

                    out.add(r);
                }
            }
        } catch (SQLException ex) {
            // if calculatedAmount/amountPaid columns don’t exist, fallback without them
            if (looksLikeMissingColumn(ex)) {
                return searchWithoutAmounts(vehiclePrefix, phone, lotNumber, activeOnly, phoneCol);
            }
            throw ex;
        }

        return out;
    }

    private List<ParkingSessionViewRow> searchWithoutAmounts(String vehiclePrefix,
                                                             String phone,
                                                             Integer lotNumber,
                                                             boolean activeOnly,
                                                             String phoneCol) throws SQLException {
        String sql =
                "SELECT " +
                        "ps.sessionID, ps.vehicleNumber, ps.lotNumber, ps.startTime, ps.endTime, " +
                        "ps.finalParkingSpaceID, ps.currentConveyorID, " +
                        "pl.lotName, pl.city, " +
                        "cl.clientID, cl.firstName, cl.lastName, cl." + phoneCol + " AS phone " +
                        "FROM " +
                        "(((ParkingSession ps " +
                        "INNER JOIN Vehicle v ON ps.vehicleNumber = v.vehicleNumber) " +
                        "INNER JOIN Client cl ON v.clientID = cl.clientID) " +
                        "INNER JOIN ParkingLot pl ON ps.lotNumber = pl.lotNumber) " +
                        "WHERE 1=1 ";

        List<Object> params = new ArrayList<>();

        if (vehiclePrefix != null && !vehiclePrefix.trim().isEmpty()) {
            sql += " AND ps.vehicleNumber LIKE ? ";
            params.add(vehiclePrefix.trim() + "%");
        }

        if (phone != null && !phone.trim().isEmpty()) {
            sql += " AND cl." + phoneCol + " = ? ";
            params.add(phone.trim());
        }

        if (lotNumber != null) {
            sql += " AND ps.lotNumber = ? ";
            params.add(lotNumber);
        }

        if (activeOnly) {
            sql += " AND ps.endTime IS NULL ";
        }

        sql += " ORDER BY ps.startTime DESC";

        List<ParkingSessionViewRow> out = new ArrayList<>();

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                Object v = params.get(i);
                if (v instanceof Integer) ps.setInt(i + 1, (Integer) v);
                else ps.setString(i + 1, String.valueOf(v));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ParkingSessionViewRow r = new ParkingSessionViewRow();
                    r.sessionId = rs.getInt("sessionID");
                    r.vehicleNumber = rs.getString("vehicleNumber");
                    r.lotNumber = rs.getInt("lotNumber");
                    r.lotName = rs.getString("lotName");
                    r.city = rs.getString("city");
                    r.startTime = rs.getTimestamp("startTime");
                    r.endTime = rs.getTimestamp("endTime");
                    r.active = (r.endTime == null);

                    int sp = rs.getInt("finalParkingSpaceID");
                    r.finalSpaceId = rs.wasNull() ? null : sp;

                    int cv = rs.getInt("currentConveyorID");
                    r.currentConveyorId = rs.wasNull() ? null : cv;

                    r.clientId = rs.getInt("clientID");
                    String fn = rs.getString("firstName");
                    String ln = rs.getString("lastName");
                    r.clientName = ((fn == null ? "" : fn) + " " + (ln == null ? "" : ln)).trim();
                    r.phone = rs.getString("phone");

                    r.calculatedAmount = null;
                    r.amountPaid = null;

                    out.add(r);
                }
            }
        }
        return out;
    }

    private Integer tryGetInt(ResultSet rs, String col) {
        try {
            int v = rs.getInt(col);
            return rs.wasNull() ? null : v;
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean looksLikeMissingColumn(SQLException ex) {
        String m = (ex.getMessage() == null) ? "" : ex.getMessage().toLowerCase();
        return m.contains("column") || m.contains("field") || m.contains("not found");
    }
}

