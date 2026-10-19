package control;

import entity.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * UI-facing control for Customer Club page:
 * - Load client by phone
 * - Register membership
 * - Load/save preferred lots
 * - Build growth report (no YEAR() to avoid UCanAccess issues)
 */
public class CustomerClubUIControl {

    // -------------------- DTOs --------------------

    public static class ClientInfo {
        public int clientId;
        public String firstName;
        public String lastName;
        public String phone;
    }

    public static class LotInfo {
        public int lotNumber;
        public String lotName;
        public String city;

        @Override
        public String toString() {
            String name = (lotName == null || lotName.isBlank()) ? ("Lot " + lotNumber) : lotName;
            String c = (city == null) ? "" : city;
            return "#" + lotNumber + " - " + name + (c.isBlank() ? "" : (" (" + c + ")"));
        }
    }

    public static class GrowthRow {
        public int month;           // 1..12
        public int joined;          // joined this month
        public int total;           // cumulative total by end of month
        public String growthPct;    // % compared to previous month total

        public GrowthRow(int month, int joined, int total, String growthPct) {
            this.month = month;
            this.joined = joined;
            this.total = total;
            this.growthPct = growthPct;
        }
    }

    // -------------------- Public API used by the page --------------------

    /** Find client by phone. Returns null if not found. */
    public ClientInfo findClientByPhone(String phoneRaw) throws SQLException {
        String phone = safe(phoneRaw);
        if (phone.isEmpty()) return null;

        try (Connection c = DB.getConnection()) {
            String phoneCol = resolveColumn(c, "Client", "phoneNumber", "mobilePhone", "phone");
            if (phoneCol == null) return null;

            String firstCol = resolveColumn(c, "Client", "firstName", "fname", "FirstName");
            String lastCol  = resolveColumn(c, "Client", "lastName", "lname", "LastName");

            String sql = "SELECT clientID, " +
                    (firstCol != null ? "[" + firstCol + "]" : "''") + " AS fn, " +
                    (lastCol  != null ? "[" + lastCol + "]"  : "''") + " AS ln, " +
                    "[" + phoneCol + "] AS ph " +
                    "FROM Client WHERE [" + phoneCol + "]=?";

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, phone);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    ClientInfo info = new ClientInfo();
                    info.clientId = rs.getInt("clientID");
                    info.firstName = rs.getString("fn");
                    info.lastName = rs.getString("ln");
                    info.phone = rs.getString("ph");
                    return info;
                }
            }
        }
    }

    /** Is this client already a club member? (memberID = clientID in your DB) */
    public boolean isMember(int clientId) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM ClubMembership WHERE memberID=?")) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** Register membership if missing. Discount defaults to 10 if your table has discountAmo. */
    public void registerMemberIfMissing(int clientId) throws SQLException {
        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (existsMember(c, clientId)) {
                    c.commit();
                    return;
                }

                String regCol = resolveColumn(c, "ClubMembership", "registrationD", "registrationDate");
                String discCol = resolveColumn(c, "ClubMembership", "discountAmo", "discountAmount", "discountPercent");

                if (regCol == null) regCol = "registrationD";

                if (discCol != null) {
                    String sql = "INSERT INTO ClubMembership (memberID, [" + regCol + "], [" + discCol + "]) VALUES (?,?,?)";
                    try (PreparedStatement ps = c.prepareStatement(sql)) {
                        ps.setInt(1, clientId);
                        ps.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                        ps.setInt(3, 10);
                        ps.executeUpdate();
                    }
                } else {
                    String sql = "INSERT INTO ClubMembership (memberID, [" + regCol + "]) VALUES (?,?)";
                    try (PreparedStatement ps = c.prepareStatement(sql)) {
                        ps.setInt(1, clientId);
                        ps.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                        ps.executeUpdate();
                    }
                }

                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /** Load preferred lots for this member. */
    public Set<Integer> getPreferredLots(int memberId) throws SQLException {
        Set<Integer> out = new HashSet<>();
        try (Connection c = DB.getConnection()) {
            String lotCol = resolveColumn(c, "PreferredLotSelection", "lotNumber", "lotID");
            if (lotCol == null) lotCol = "lotNumber";

            String sql = "SELECT [" + lotCol + "] FROM PreferredLotSelection WHERE memberID=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, memberId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) out.add(rs.getInt(1));
                }
            }
        }
        return out;
    }

    /** Save preferred lots: delete current rows, insert new rows with selection date. */
    public void savePreferredLots(int memberId, Set<Integer> chosenLots) throws SQLException {
        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                // 1) Delete old rows for that member
                try (PreparedStatement del = c.prepareStatement(
                        "DELETE FROM PreferredLotSelection WHERE memberID=?")) {
                    del.setInt(1, memberId);
                    del.executeUpdate();
                }

                // 2) Detect whether selectionID exists and is required (not autonumber)
                boolean hasSelectionId = hasColumn(c, "PreferredLotSelection", "selectionID");
                String dateCol = resolveColumn(c, "PreferredLotSelection", "selectionDat", "selectionDate");
                if (dateCol == null) dateCol = "selectionDat";

                if (!hasSelectionId) {
                    // No selectionID column → simple insert
                    String ins = "INSERT INTO PreferredLotSelection (memberID, lotNumber, [" + dateCol + "]) VALUES (?,?,?)";
                    try (PreparedStatement ps = c.prepareStatement(ins)) {
                        for (int lot : chosenLots) {
                            ps.setInt(1, memberId);
                            ps.setInt(2, lot);
                            ps.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                } else {
                    // selectionID exists → we generate it manually (max + 1)
                    int nextId = getNextSelectionId(c);

                    String ins = "INSERT INTO PreferredLotSelection (selectionID, memberID, lotNumber, [" + dateCol + "]) VALUES (?,?,?,?)";
                    try (PreparedStatement ps = c.prepareStatement(ins)) {
                        for (int lot : chosenLots) {
                            ps.setInt(1, nextId++);
                            ps.setInt(2, memberId);
                            ps.setInt(3, lot);
                            ps.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }


    /** Load all lots from ParkingLot table for the checkbox list. */
    public List<LotInfo> getAllLots() throws SQLException {
        List<LotInfo> lots = new ArrayList<>();
        try (Connection c = DB.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM ParkingLot")) {

            ResultSetMetaData md = rs.getMetaData();
            Map<String, Integer> cols = cols(md);

            String lotNumCol = pick(cols, "lotNumber", "lotNum", "number");
            String lotNameCol = pick(cols, "lotName", "name");
            String cityCol = pick(cols, "city", "City");

            while (rs.next()) {
                LotInfo li = new LotInfo();
                li.lotNumber = rs.getInt(lotNumCol != null ? lotNumCol : md.getColumnName(1));
                li.lotName = lotNameCol != null ? rs.getString(lotNameCol) : null;
                li.city = cityCol != null ? rs.getString(cityCol) : null;
                lots.add(li);
            }
        }

        lots.sort(Comparator.comparingInt(a -> a.lotNumber));
        return lots;
    }

    /** Growth report without YEAR(). */
    public List<GrowthRow> buildGrowthReport(int year) throws SQLException {
        int[] joined = new int[12];

        try (Connection c = DB.getConnection()) {
            String regCol = resolveColumn(c, "ClubMembership", "registrationD", "registrationDate");
            if (regCol == null) regCol = "registrationD";

            LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(year + 1, 1, 1, 0, 0);

            String sql = "SELECT [" + regCol + "] FROM ClubMembership " +
                    "WHERE [" + regCol + "] >= ? AND [" + regCol + "] < ? " +
                    "ORDER BY [" + regCol + "]";

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(start));
                ps.setTimestamp(2, Timestamp.valueOf(end));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Timestamp ts = rs.getTimestamp(1);
                        if (ts == null) continue;
                        int m = ts.toLocalDateTime().getMonthValue();
                        joined[m - 1]++;
                    }
                }
            }
        }

        List<GrowthRow> rows = new ArrayList<>();
        int cumulative = 0;
        int prevTotal = 0;

        for (int m = 1; m <= 12; m++) {
            cumulative += joined[m - 1];

            String growth;
            if (m == 1 || prevTotal == 0) {
                growth = "N/A";
            } else {
                double pct = ((double) (cumulative - prevTotal) / (double) prevTotal) * 100.0;
                growth = String.format(Locale.US, "%.2f%%", pct);
            }

            rows.add(new GrowthRow(m, joined[m - 1], cumulative, growth));
            prevTotal = cumulative;
        }

        return rows;
    }

    // -------------------- Helpers --------------------

    private boolean existsMember(Connection c, int clientId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM ClubMembership WHERE memberID=?")) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private static String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    private static String resolveColumn(Connection c, String table, String... candidates) {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM [" + table + "] WHERE 1=0")) {
            ResultSetMetaData md = rs.getMetaData();
            Map<String, Integer> cols = cols(md);
            for (String cand : candidates) {
                for (String k : cols.keySet()) {
                    if (k.equalsIgnoreCase(cand)) return k;
                }
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Map<String, Integer> cols(ResultSetMetaData md) throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            map.put(md.getColumnName(i), i);
        }
        return map;
    }

    private static String pick(Map<String, Integer> cols, String... candidates) {
        for (String cand : candidates) {
            for (String k : cols.keySet()) {
                if (k.equalsIgnoreCase(cand)) return k;
            }
        }
        return null;
    }
    private static boolean hasColumn(Connection c, String table, String column) {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM [" + table + "] WHERE 1=0")) {
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                if (md.getColumnName(i).equalsIgnoreCase(column)) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static int getNextSelectionId(Connection c) throws SQLException {
        String sql = "SELECT MAX(selectionID) FROM PreferredLotSelection";
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            int max = 0;
            if (rs.next()) {
                max = rs.getInt(1);
                if (rs.wasNull()) max = 0;
            }
            return max + 1;
        }
    }

}
