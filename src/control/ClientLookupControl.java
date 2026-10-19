package control;

import entity.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientLookupControl {

    public ClientLookupResult lookupByPhone(String phoneRaw) {
        try {
            String phone = normalizePhone(phoneRaw);
            if (phone == null) return ClientLookupResult.fail("Please enter a valid phone number.");

            ClientLookupResult out = ClientLookupResult.ok("OK");
            out.phone = phone;

            try (Connection c = DB.getConnection()) {

                // 1) Find client by phone (support phoneNumber OR mobilePhone)
                ClientRow client = findClientByPhone(c, phone);
                if (client == null) return ClientLookupResult.fail("No client found with this phone.");

                out.clientId = client.clientId;
                out.clientFullName = client.firstName + " " + client.lastName;

                // 2) Get all vehicles for this client
                List<String> vehicles = getVehiclesForClient(c, client.clientId);

                // 3) For each vehicle, check active session
                for (String vehicleNumber : vehicles) {
                    ActiveSessionView s = getActiveSessionForVehicle(c, vehicleNumber);
                    if (s != null) {
                        s.vehicleNumber = vehicleNumber;
                        // lot name/city
                        fillLotInfo(c, s);
                        // location text
                        s.locationText = buildLocationText(c, s.finalSpaceId, s.currentConveyorId);
                        out.activeSessions.add(s);
                    }
                }

                if (out.activeSessions.isEmpty()) {
                    out.message = "Client found, but no ACTIVE parking sessions right now.";
                } else {
                    out.message = "Found " + out.activeSessions.size() + " active session(s).";
                }

                return out;
            }

        } catch (Exception e) {
            return ClientLookupResult.fail("Lookup failed: " + e.getMessage());
        }
    }

    private String normalizePhone(String raw) {
        if (raw == null) return null;
        String p = raw.trim().replaceAll("[^0-9+]", "");
        if (p.isEmpty()) return null;
        // allow +972... or 0xxxxxxxxx
        if (p.startsWith("+")) {
            if (p.length() < 8) return null;
            return p;
        }
        if (p.length() < 7) return null;
        return p;
    }

    // ---------------- DB helpers ----------------

    private static class ClientRow {
        int clientId;
        String firstName;
        String lastName;
    }

    private ClientRow findClientByPhone(Connection c, String phone) throws SQLException {
        // try phoneNumber
        try {
            return findClientByPhoneColumn(c, phone, "phoneNumber");
        } catch (SQLException ex) {
            // fallback mobilePhone
            return findClientByPhoneColumn(c, phone, "mobilePhone");
        }
    }

    private ClientRow findClientByPhoneColumn(Connection c, String phone, String col) throws SQLException {
        String sql = "SELECT clientID, firstName, lastName FROM Client WHERE " + col + "=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                ClientRow r = new ClientRow();
                r.clientId = rs.getInt("clientID");
                r.firstName = rs.getString("firstName");
                r.lastName = rs.getString("lastName");
                return r;
            }
        }
    }

    private List<String> getVehiclesForClient(Connection c, int clientId) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT vehicleNumber FROM Vehicle WHERE clientID=? ORDER BY vehicleNumber";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString("vehicleNumber"));
            }
        }
        return list;
    }

    private ActiveSessionView getActiveSessionForVehicle(Connection c, String vehicleNumber) throws SQLException {
        String sql = "SELECT sessionID, lotNumber, startTime, finalParkingSpaceID, currentConveyorID " +
                "FROM ParkingSession WHERE vehicleNumber=? AND endTime IS NULL";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, vehicleNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                ActiveSessionView s = new ActiveSessionView();
                s.sessionId = rs.getInt("sessionID");
                s.lotNumber = rs.getInt("lotNumber");
                s.startTime = rs.getTimestamp("startTime");

                int sp = rs.getInt("finalParkingSpaceID");
                s.finalSpaceId = rs.wasNull() ? null : sp;

                int cv = rs.getInt("currentConveyorID");
                s.currentConveyorId = rs.wasNull() ? null : cv;

                return s;
            }
        }
    }

    private void fillLotInfo(Connection c, ActiveSessionView s) throws SQLException {
        String sql = "SELECT lotName, city FROM ParkingLot WHERE lotNumber=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.lotNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    s.lotName = rs.getString("lotName");
                    s.city = rs.getString("city");
                }
            }
        }
    }

    private String buildLocationText(Connection c, Integer spaceId, Integer conveyorId) throws SQLException {
        if (spaceId != null) {
            String sql = "SELECT floorNumber, positionX, positionY FROM ParkingSpace WHERE spaceID=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, spaceId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int f = rs.getInt("floorNumber");
                        int x = rs.getInt("positionX");
                        int y = rs.getInt("positionY");
                        return "In Space #" + spaceId + " (floor " + f + ", x=" + x + ", y=" + y + ")";
                    }
                }
            }
            return "In Space #" + spaceId;
        }

        if (conveyorId != null) {
            String sql = "SELECT floorNumber, positionX, positionY FROM Conveyor WHERE conveyorID=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, conveyorId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int f = rs.getInt("floorNumber");
                        int x = rs.getInt("positionX");
                        int y = rs.getInt("positionY");
                        return "On Conveyor #" + conveyorId + " (floor " + f + ", x=" + x + ", y=" + y + ")";
                    }
                }
            }
            return "On Conveyor #" + conveyorId;
        }

        return "At gate / waiting for assignment";
    }
}
