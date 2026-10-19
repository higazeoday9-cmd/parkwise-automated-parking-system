package entity;

import java.sql.*;

public class DbMigrations {

    private static boolean ran = false;

    public static synchronized void apply(Connection conn) throws SQLException {
        if (ran) return;
        ran = true;

        ensureParkingSpaceIsVacant(conn);
    }

    private static void ensureParkingSpaceIsVacant(Connection conn) throws SQLException {
        if (columnExists(conn, "ParkingSpace", "isVacant")) {
            return; // already there
        }

        // 1) Add column
        try (Statement st = conn.createStatement()) {
            // YESNO is the Access boolean type; UCanAccess supports it.
            st.executeUpdate("ALTER TABLE ParkingSpace ADD COLUMN isVacant YESNO");
        }

        // 2) Default: all spaces vacant
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("UPDATE ParkingSpace SET isVacant = TRUE");
        }

        // 3) Mark occupied spaces as NOT vacant (active sessions only)
        // Access UPDATE JOIN syntax:
        String sql =
                "UPDATE ParkingSpace " +
                        "INNER JOIN ParkingSession " +
                        "ON ParkingSpace.spaceID = ParkingSession.finalParkingSpaceID " +
                        "SET ParkingSpace.isVacant = FALSE " +
                        "WHERE ParkingSession.endTime IS NULL";

        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        DatabaseMetaData md = conn.getMetaData();

        // Access/UCANACCESS can be weird with case, so we check case-insensitively.
        try (ResultSet rs = md.getColumns(null, null, table, null)) {
            while (rs.next()) {
                String col = rs.getString("COLUMN_NAME");
                if (col != null && col.equalsIgnoreCase(column)) return true;
            }
        }
        return false;
    }
}
