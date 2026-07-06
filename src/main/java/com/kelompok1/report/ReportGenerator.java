package com.kelompok1.report;

import com.kelompok1.config.DatabaseHelper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import javax.swing.JOptionPane;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ReportGenerator {

    /**
     * Helper to generate a report from a JRXML file and export to PDF.
     * Returns true if successful, false otherwise.
     */
    public static boolean generateReport(String jrxmlPath, String outputPdfPath, Map<String, Object> parameters) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            
            // Add logo path dynamically
            if (parameters == null) {
                parameters = new java.util.HashMap<>();
            }
            parameters.put("logoPath", new java.io.File("LOGO2.png").getAbsolutePath());

            // 1. Load the design
            InputStream is = ReportGenerator.class.getResourceAsStream(jrxmlPath);
            if (is == null) {
                JOptionPane.showMessageDialog(null, "Template laporan tidak ditemukan: " + jrxmlPath, "Kesalahan", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            JasperDesign design = JRXmlLoader.load(is);

            // 2. Compile report
            JasperReport report = JasperCompileManager.compileReport(design);

            // 3. Fill report
            JasperPrint print = JasperFillManager.fillReport(report, parameters, conn);

            // 4. Export to PDF
            JasperExportManager.exportReportToPdfFile(print, outputPdfPath);
            System.out.println("Report generated successfully at: " + outputPdfPath);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal mengunduh PDF laporan: " + e.getMessage(), "Kesalahan Sistem", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Loads, compiles, and opens a report template in the JasperViewer Swing window.
     */
    public static void showReportViewer(String jrxmlPath, Map<String, Object> parameters) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            // Add logo path dynamically
            if (parameters == null) {
                parameters = new java.util.HashMap<>();
            }
            parameters.put("logoPath", new java.io.File("LOGO2.png").getAbsolutePath());

            // 1. Load the design
            InputStream is = ReportGenerator.class.getResourceAsStream(jrxmlPath);
            if (is == null) {
                JOptionPane.showMessageDialog(null, "Template laporan tidak ditemukan: " + jrxmlPath, "Kesalahan", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JasperDesign design = JRXmlLoader.load(is);

            // 2. Compile report
            JasperReport report = JasperCompileManager.compileReport(design);

            // 3. Fill report
            JasperPrint print = JasperFillManager.fillReport(report, parameters, conn);

            // 4. Show Report Viewer (Swing Dialog Frame)
            net.sf.jasperreports.view.JasperViewer viewer = new net.sf.jasperreports.view.JasperViewer(print, false);
            viewer.setTitle("Pratinjau Laporan — Perpustakaan Freedom");
            viewer.setLocationRelativeTo(null);
            viewer.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal memproses pratinjau laporan: " + e.getMessage(), "Kesalahan Sistem", JOptionPane.ERROR_MESSAGE);
        }
    }

    // specific method for defaulters list
    public static void generateDefaultersReport(String outputPdfPath) {
        Map<String, Object> params = new HashMap<>();
        params.put("status_filter", "Unpaid");
        generateReport("/reports/fines_report.jrxml", outputPdfPath, params);
    }
}
