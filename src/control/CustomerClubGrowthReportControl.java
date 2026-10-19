package control;

import entity.DB;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class CustomerClubGrowthReportControl {

    public static class Row {
        public final int month;
        public final int joinedThisMonth;
        public final int totalByEndOfMonth;
        public final double growthPercentVsPrev;

        public Row(int month, int joinedThisMonth, int totalByEndOfMonth, double growthPercentVsPrev) {
            this.month = month;
            this.joinedThisMonth = joinedThisMonth;
            this.totalByEndOfMonth = totalByEndOfMonth;
            this.growthPercentVsPrev = growthPercentVsPrev;
        }
    }

    public List<Row> build(int year) throws SQLException {
        int[] joined = new int[12];

        String sql = "SELECT registrationD FROM ClubMembership WHERE YEAR(registrationD)=? ORDER BY registrationD";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp t = rs.getTimestamp("registrationD");
                    LocalDate d = t.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    joined[d.getMonthValue() - 1]++;
                }
            }
        }

        List<Row> rows = new ArrayList<>();
        int cumulative = 0;
        for (int i = 0; i < 12; i++) {
            int prev = cumulative;
            cumulative += joined[i];

            double growth;
            if (i == 0) growth = 0.0;
            else growth = (prev == 0) ? (cumulative > 0 ? 100.0 : 0.0) : ((cumulative - prev) * 100.0 / prev);

            rows.add(new Row(i + 1, joined[i], cumulative, growth));
        }
        return rows;
    }
}
