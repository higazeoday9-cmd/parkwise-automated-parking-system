package control;

import entity.DB;
import net.sf.jasperreports.engine.*;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ReportControl {

    private static final String JRXML_PATH = "reports/AnnualSummary.jrxml";
    private static final String OUT_DIR = "output";

    public File generateAnnualSummaryPdf(int year) throws Exception {

        File jrxml = new File(JRXML_PATH);
        if (!jrxml.exists()) {
            throw new IllegalStateException("JRXML not found at: " + jrxml.getAbsolutePath());
        }

        // compile
        JasperReport report = JasperCompileManager.compileReport(jrxml.getAbsolutePath());

        // params (MUST match $P{YEAR} in the JRXML)
        Map<String, Object> params = new HashMap<>();
        params.put("YEAR", year);

        // fill + export
        try (Connection conn = DB.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, params, conn);

            File outDir = new File(OUT_DIR);
            if (!outDir.exists() && !outDir.mkdirs()) {
                throw new IllegalStateException("Failed to create output directory: " + outDir.getAbsolutePath());
            }

            File pdf = new File(outDir, "AnnualSummary_" + year + ".pdf");
            JasperExportManager.exportReportToPdfFile(print, pdf.getAbsolutePath());

            return pdf;
        }
    }
}
