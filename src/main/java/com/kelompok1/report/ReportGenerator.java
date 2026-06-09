package com.kelompok1.report;

import com.kelompok1.config.DatabaseHelper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ReportGenerator {

    /**
     * Helper to generate a report from a JRXML file and export to PDF.
     * In a real app, you'd load the .jrxml from resources. 
     * Since we might not have the files yet, this is a placeholder structure.
     */
    public static void generateReport(String jrxmlPath, String outputPdfPath, Map<String, Object> parameters) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            
            // 1. Load the design
            InputStream is = ReportGenerator.class.getResourceAsStream(jrxmlPath);
            if (is == null) {
                System.err.println("Report template not found: " + jrxmlPath);
                return;
            }
            JasperDesign design = JRXmlLoader.load(is);

            // 2. Compile report
            JasperReport report = JasperCompileManager.compileReport(design);

            // 3. Fill report
            JasperPrint print = JasperFillManager.fillReport(report, parameters, conn);

            // 4. Export to PDF
            JasperExportManager.exportReportToPdfFile(print, outputPdfPath);
            System.out.println("Report generated successfully at: " + outputPdfPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // specific method for defaulters list
    public static void generateDefaultersReport(String outputPdfPath) {
        Map<String, Object> params = new HashMap<>();
        params.put("ReportTitle", "Defaulters List");
        // generateReport("/reports/defaulters.jrxml", outputPdfPath, params);
        System.out.println("Defaulters report generation logic placeholder executed.");
    }
}
