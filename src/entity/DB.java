package entity;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    private static final String DB_FILENAME = "ParkWise_Ex2.accdb";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "UCanAccess driver not found.\n" +
                            "Fix: make sure ucanaccess + deps jars are included in your project.", e);
        }

        Path dbPath = resolveDbPath();

        if (!Files.exists(dbPath)) {
            throw new SQLException("Database file not found: " + dbPath.toAbsolutePath());
        }

        String connStr = "jdbc:ucanaccess://" + dbPath.toAbsolutePath().normalize();
        return DriverManager.getConnection(connStr);
    }

    private static Path resolveDbPath() throws SQLException {
        // 1) override via system property
        String override = System.getProperty("parkwise.db");
        if (override != null && !override.isBlank()) {
            return Paths.get(override).toAbsolutePath().normalize();
        }

        // 2) try next to the JAR (best for double-click)
        Path jarDir = getJarDirectory();
        if (jarDir != null) {
            Path fromJar = jarDir.resolve("db").resolve(DB_FILENAME).normalize();
            if (Files.exists(fromJar)) return fromJar;
        }

        // 3) fallback: working directory (best for IntelliJ)
        return Paths.get("db", DB_FILENAME).toAbsolutePath().normalize();
    }

    private static Path getJarDirectory() throws SQLException {
        try {
            File jarFile = new File(DB.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());

            File dir = jarFile.isFile() ? jarFile.getParentFile() : jarFile;
            return dir.toPath().toAbsolutePath().normalize();
        } catch (URISyntaxException e) {
            throw new SQLException("Failed to resolve JAR directory: " + e.getMessage(), e);
        }
    }
}


