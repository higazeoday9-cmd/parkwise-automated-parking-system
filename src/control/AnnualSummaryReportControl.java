package control;

import entity.DB;
import net.sf.jasperreports.engine.*;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class AnnualSummaryReportControl {

    // keep these relative to the project/run folder (same style as DB.java)
    private static final String JRXML_PATH = "reports/AnnualSummary.jrxml";

    public String generateAnnualSummary(int year) throws Exception {

        // (optional) friendly error if JRXML missing
        File jrxml = new File(JRXML_PATH);
        if (!jrxml.exists()) {
            throw new IllegalStateException("JRXML not found at: " + jrxml.getAbsolutePath());
        }

        try (Connection conn = DB.getConnection()) {

            JasperReport report = JasperCompileManager.compileReport(jrxml.getAbsolutePath());

            Map<String, Object> params = new HashMap<>();
            params.put("YEAR", year); // must match $P{YEAR} in JRXML

            JasperPrint print = JasperFillManager.fillReport(report, params, conn);

            String out = "AnnualSummary_" + year + ".pdf";
            JasperExportManager.exportReportToPdfFile(print, out);

            return new File(out).getAbsolutePath();
        }
    }
}

