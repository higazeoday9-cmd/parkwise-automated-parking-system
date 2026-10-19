package control;

import entity.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VehicleClientInfoControl {

    public static class ClientInfo {
        public int clientId;
        public String firstName;
        public String lastName;
        public String phone;

        public String fullName() {
            return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
        }
    }

    public ClientInfo findClientByVehicle(String vehicleNumber) throws Exception {
        // Try phoneNumber column first
        try {
            return findClientByVehiclePhoneCol(vehicleNumber, "phoneNumber");
        } catch (Exception e) {
            // Fallback to mobilePhone (in case your DB uses a different column name)
            return findClientByVehiclePhoneCol(vehicleNumber, "mobilePhone");
        }
    }

    private ClientInfo findClientByVehiclePhoneCol(String vehicleNumber, String phoneCol) throws Exception {
        String sql = """
                SELECT cl.clientID, cl.firstName, cl.lastName, cl.%s
                FROM Vehicle v
                INNER JOIN Client cl ON v.clientID = cl.clientID
                WHERE v.vehicleNumber = ?
                """.formatted(phoneCol);

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, vehicleNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                ClientInfo info = new ClientInfo();
                info.clientId = rs.getInt(1);
                info.firstName = rs.getString(2);
                info.lastName = rs.getString(3);
                info.phone = rs.getString(4);
                return info;
            }
        }
    }
}

