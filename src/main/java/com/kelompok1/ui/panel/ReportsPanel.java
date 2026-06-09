package com.kelompok1.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.report.ReportGenerator;
import com.kelompok1.util.DesignSystem;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Modern dashboard panel for generating and exporting system reports.
 */
public class ReportsPanel extends JPanel {

    public ReportsPanel() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        initPanel();
    }

    private void initPanel() {
        // ─── 1. HEADER PANEL (NORTH) ─────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel title = new JLabel("Laporan Sistem");
        title.putClientProperty(FlatClientProperties.STYLE, "font: bold +6");
        headerPanel.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Cetak laporan dan unduh dokumen PDF secara dinamis.");
        subtitle.setFont(DesignSystem.bodyFont(13f, Font.ITALIC));
        subtitle.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // ─── 2. CONTENT PANEL (CENTER) ─────────────────────────────────────────
        JPanel mainGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        mainGrid.setOpaque(false);
        mainGrid.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Create Report Cards
        mainGrid.add(createBooksCard());
        mainGrid.add(createMembersCard());
        mainGrid.add(createTransactionsCard());
        mainGrid.add(createFinesCard());

        JScrollPane scrollPane = new JScrollPane(mainGrid);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  BOOKS REPORT CARD
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel createBooksCard() {
        JPanel card = UIUtils.createCardPanel(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Laporan Koleksi Buku");
        lblTitle.setFont(DesignSystem.displayFont(15f, Font.BOLD));
        lblTitle.setForeground(DesignSystem.PRIMARY);
        JLabel lblDesc = new JLabel("Daftar buku dengan filter status stok ketersediaan.");
        lblDesc.setFont(DesignSystem.bodyFont(11f, Font.PLAIN));
        lblDesc.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        cardHeader.add(lblTitle, BorderLayout.NORTH);
        cardHeader.add(lblDesc, BorderLayout.SOUTH);
        card.add(cardHeader, BorderLayout.NORTH);

        // Form/Filter
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;

        JComboBox<String> comboStock = new JComboBox<>(new String[]{"Semua Buku", "Tersedia saja (> 0)", "Habis/Kosong saja (= 0)"});
        comboStock.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        UIUtils.addFormRow(filterPanel, gbc, "Status Stok:", comboStock, 0);
        card.add(filterPanel, BorderLayout.CENTER);

        // Actions Footer
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        JButton btnPdf = new JButton("Unduh PDF");
        DesignSystem.applySecondaryButton(btnPdf);
        JButton btnPrint = new JButton("Cetak Laporan");
        DesignSystem.applyPrimaryButton(btnPrint);
        actionPanel.add(btnPdf);
        actionPanel.add(btnPrint);
        card.add(actionPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnPrint.addActionListener(e -> {
            String filter = getBooksFilterValue(comboStock.getSelectedIndex());
            Map<String, Object> params = new HashMap<>();
            params.put("status_filter", filter);
            triggerReportViewer("/reports/books_report.jrxml", params);
        });

        btnPdf.addActionListener(e -> {
            String filter = getBooksFilterValue(comboStock.getSelectedIndex());
            Map<String, Object> params = new HashMap<>();
            params.put("status_filter", filter);
            triggerPdfDownload("/reports/books_report.jrxml", "laporan_buku.pdf", params);
        });

        return card;
    }

    private String getBooksFilterValue(int selectedIdx) {
        return switch (selectedIdx) {
            case 1 -> "Available";
            case 2 -> "OutOfStock";
            default -> "All";
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MEMBERS REPORT CARD
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel createMembersCard() {
        JPanel card = UIUtils.createCardPanel(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Laporan Anggota");
        lblTitle.setFont(DesignSystem.displayFont(15f, Font.BOLD));
        lblTitle.setForeground(DesignSystem.PRIMARY);
        JLabel lblDesc = new JLabel("Direktori seluruh anggota perpustakaan yang terdaftar.");
        lblDesc.setFont(DesignSystem.bodyFont(11f, Font.PLAIN));
        lblDesc.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        cardHeader.add(lblTitle, BorderLayout.NORTH);
        cardHeader.add(lblDesc, BorderLayout.SOUTH);
        card.add(cardHeader, BorderLayout.NORTH);

        // Form/Filter
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.weightx = 1.0;

        JComboBox<String> comboRole = new JComboBox<>(new String[]{"Semua Peran", "Anggota (Member)", "Admin"});
        comboRole.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

        JComboBox<String> comboStatus = new JComboBox<>(new String[]{"Semua Status", "Aktif", "Ditangguhkan"});
        comboStatus.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

        UIUtils.addFormRow(filterPanel, gbc, "Peran Akun:", comboRole, 0);
        UIUtils.addFormRow(filterPanel, gbc, "Status Akun:", comboStatus, 1);
        card.add(filterPanel, BorderLayout.CENTER);

        // Actions Footer
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        JButton btnPdf = new JButton("Unduh PDF");
        DesignSystem.applySecondaryButton(btnPdf);
        JButton btnPrint = new JButton("Cetak Laporan");
        DesignSystem.applyPrimaryButton(btnPrint);
        actionPanel.add(btnPdf);
        actionPanel.add(btnPrint);
        card.add(actionPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnPrint.addActionListener(e -> {
            String roleVal = getRoleFilterValue(comboRole.getSelectedIndex());
            String statusVal = getStatusFilterValue(comboStatus.getSelectedIndex());
            Map<String, Object> params = new HashMap<>();
            params.put("role_filter", roleVal);
            params.put("status_filter", statusVal);
            triggerReportViewer("/reports/members_report.jrxml", params);
        });

        btnPdf.addActionListener(e -> {
            String roleVal = getRoleFilterValue(comboRole.getSelectedIndex());
            String statusVal = getStatusFilterValue(comboStatus.getSelectedIndex());
            Map<String, Object> params = new HashMap<>();
            params.put("role_filter", roleVal);
            params.put("status_filter", statusVal);
            triggerPdfDownload("/reports/members_report.jrxml", "laporan_anggota.pdf", params);
        });

        return card;
    }

    private String getRoleFilterValue(int idx) {
        return switch (idx) {
            case 1 -> "Member";
            case 2 -> "Admin";
            default -> "All";
        };
    }

    private String getStatusFilterValue(int idx) {
        return switch (idx) {
            case 1 -> "Active";
            case 2 -> "Suspended";
            default -> "All";
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TRANSACTIONS REPORT CARD
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel createTransactionsCard() {
        JPanel card = UIUtils.createCardPanel(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Laporan Transaksi Sirkulasi");
        lblTitle.setFont(DesignSystem.displayFont(15f, Font.BOLD));
        lblTitle.setForeground(DesignSystem.PRIMARY);
        JLabel lblDesc = new JLabel("Catatan sirkulasi peminjaman, pengembalian, dan kehilangan.");
        lblDesc.setFont(DesignSystem.bodyFont(11f, Font.PLAIN));
        lblDesc.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        cardHeader.add(lblTitle, BorderLayout.NORTH);
        cardHeader.add(lblDesc, BorderLayout.SOUTH);
        card.add(cardHeader, BorderLayout.NORTH);

        // Form/Filter
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;

        JComboBox<String> comboTx = new JComboBox<>(new String[]{"Semua Transaksi", "Aktif (Dipinjam)", "Selesai (Dikembalikan)", "Buku Hilang"});
        comboTx.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        UIUtils.addFormRow(filterPanel, gbc, "Status Transaksi:", comboTx, 0);
        card.add(filterPanel, BorderLayout.CENTER);

        // Actions Footer
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        JButton btnPdf = new JButton("Unduh PDF");
        DesignSystem.applySecondaryButton(btnPdf);
        JButton btnPrint = new JButton("Cetak Laporan");
        DesignSystem.applyPrimaryButton(btnPrint);
        actionPanel.add(btnPdf);
        actionPanel.add(btnPrint);
        card.add(actionPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnPrint.addActionListener(e -> {
            String filter = getTxFilterValue(comboTx.getSelectedIndex());
            Map<String, Object> params = new HashMap<>();
            params.put("status_filter", filter);
            triggerReportViewer("/reports/transactions_report.jrxml", params);
        });

        btnPdf.addActionListener(e -> {
            String filter = getTxFilterValue(comboTx.getSelectedIndex());
            Map<String, Object> params = new HashMap<>();
            params.put("status_filter", filter);
            triggerPdfDownload("/reports/transactions_report.jrxml", "laporan_sirkulasi.pdf", params);
        });

        return card;
    }

    private String getTxFilterValue(int idx) {
        return switch (idx) {
            case 1 -> "Issued";
            case 2 -> "Returned";
            case 3 -> "Lost";
            default -> "All";
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FINES REPORT CARD
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel createFinesCard() {
        JPanel card = UIUtils.createCardPanel(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Laporan Keuangan Denda");
        lblTitle.setFont(DesignSystem.displayFont(15f, Font.BOLD));
        lblTitle.setForeground(DesignSystem.PRIMARY);
        JLabel lblDesc = new JLabel("Rekapitulasi denda keterlambatan pengembalian buku.");
        lblDesc.setFont(DesignSystem.bodyFont(11f, Font.PLAIN));
        lblDesc.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        cardHeader.add(lblTitle, BorderLayout.NORTH);
        cardHeader.add(lblDesc, BorderLayout.SOUTH);
        card.add(cardHeader, BorderLayout.NORTH);

        // Form/Filter
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;

        JComboBox<String> comboFines = new JComboBox<>(new String[]{"Semua Denda", "Lunas", "Belum Lunas (Unpaid)"});
        comboFines.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        UIUtils.addFormRow(filterPanel, gbc, "Status Bayar:", comboFines, 0);
        card.add(filterPanel, BorderLayout.CENTER);

        // Actions Footer
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        JButton btnPdf = new JButton("Unduh PDF");
        DesignSystem.applySecondaryButton(btnPdf);
        JButton btnPrint = new JButton("Cetak Laporan");
        DesignSystem.applyPrimaryButton(btnPrint);
        actionPanel.add(btnPdf);
        actionPanel.add(btnPrint);
        card.add(actionPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnPrint.addActionListener(e -> {
            String filter = getFinesFilterValue(comboFines.getSelectedIndex());
            Map<String, Object> params = new HashMap<>();
            params.put("status_filter", filter);
            triggerReportViewer("/reports/fines_report.jrxml", params);
        });

        btnPdf.addActionListener(e -> {
            String filter = getFinesFilterValue(comboFines.getSelectedIndex());
            Map<String, Object> params = new HashMap<>();
            params.put("status_filter", filter);
            triggerPdfDownload("/reports/fines_report.jrxml", "laporan_denda.pdf", params);
        });

        return card;
    }

    private String getFinesFilterValue(int idx) {
        return switch (idx) {
            case 1 -> "Paid";
            case 2 -> "Unpaid";
            default -> "All";
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SwingWorker Thread Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void triggerReportViewer(String jrxmlPath, Map<String, Object> params) {
        // Show status dialog or glasspane if loading is slow
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog loadingDialog = new JDialog(parentFrame, "Menyiapkan Laporan", true);
        loadingDialog.setLayout(new BorderLayout());
        loadingDialog.setSize(250, 100);
        loadingDialog.setLocationRelativeTo(parentFrame);
        JLabel lbl = new JLabel("Mengompilasi & mengisi data...", SwingConstants.CENTER);
        lbl.setFont(DesignSystem.bodyFont(12f, Font.BOLD));
        loadingDialog.add(lbl, BorderLayout.CENTER);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                ReportGenerator.showReportViewer(jrxmlPath, params);
                return null;
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
            }
        };

        worker.execute();
        loadingDialog.setVisible(true);
    }

    private void triggerPdfDownload(String jrxmlPath, String defaultFileName, Map<String, Object> params) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan PDF");
        fileChooser.setSelectedFile(new File(defaultFileName));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Berkas PDF (*.pdf)", "pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String destPath = fileToSave.getAbsolutePath();
            if (!destPath.toLowerCase().endsWith(".pdf")) {
                destPath += ".pdf";
            }

            final String finalDestPath = destPath;

            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            JDialog loadingDialog = new JDialog(parentFrame, "Mengekspor PDF", true);
            loadingDialog.setLayout(new BorderLayout());
            loadingDialog.setSize(250, 100);
            loadingDialog.setLocationRelativeTo(parentFrame);
            JLabel lbl = new JLabel("Mengekspor berkas laporan...", SwingConstants.CENTER);
            lbl.setFont(DesignSystem.bodyFont(12f, Font.BOLD));
            loadingDialog.add(lbl, BorderLayout.CENTER);

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return ReportGenerator.generateReport(jrxmlPath, finalDestPath, params);
                }

                @Override
                protected void done() {
                    loadingDialog.dispose();
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(ReportsPanel.this, 
                                    "Laporan berhasil disimpan ke:\n" + finalDestPath, 
                                    "Ekspor Berhasil", 
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ReportsPanel.this, 
                                "Gagal mengekspor laporan: " + ex.getMessage(), 
                                "Kesalahan", 
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
            loadingDialog.setVisible(true);
        }
    }
}
