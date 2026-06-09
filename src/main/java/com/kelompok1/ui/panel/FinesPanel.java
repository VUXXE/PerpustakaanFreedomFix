package com.kelompok1.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.model.Fine;
import com.kelompok1.service.FineService;
import com.kelompok1.util.DesignSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FinesPanel extends JPanel {
    private final FineService fineService;
    private DefaultTableModel finesTableModel;
    private JTable finesTable;
    private JLabel finesTitleLabel;
    private int currentPage = 1;
    private final int pageSize = 15;
    private JButton btnPrevPage;
    private JButton btnNextPage;
    private JLabel lblPage;
    private String currentSearchQuery = null;

    // Form fields (top panel)
    private JLabel lblFormTitle;
    private JLabel lblFormSubtitle;
    private JTextField txtMemberCode;
    private JTextField txtMemberName;
    private JTextField txtBookTitle;
    private JTextField txtAmount;
    private JTextField txtStatus;
    private JTextField txtUpdatedAt;
    private JButton btnPayFine;
    private int selectedFineId = -1;

    public FinesPanel() {
        this.fineService = new FineService();
        
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        initPanel();
    }

    private void initPanel() {
        // ─── 1. NORTH WRAPPER (FORM AT TOP) ────────────────────────
        JPanel northWrapper = new JPanel();
        northWrapper.setLayout(new BoxLayout(northWrapper, BoxLayout.Y_AXIS));
        northWrapper.setOpaque(false);

        // Instantiate form components
        txtMemberCode = UIUtils.createFormTextField("Pilih denda di tabel");
        txtMemberCode.setEditable(false);
        
        txtMemberName = UIUtils.createFormTextField("Pilih denda di tabel");
        txtMemberName.setEditable(false);
        
        txtBookTitle = UIUtils.createFormTextField("Pilih denda di tabel");
        txtBookTitle.setEditable(false);
        
        txtAmount = UIUtils.createFormTextField("Pilih denda di tabel");
        txtAmount.setEditable(false);
        
        txtStatus = UIUtils.createFormTextField("Pilih denda di tabel");
        txtStatus.setEditable(false);
        
        txtUpdatedAt = UIUtils.createFormTextField("Pilih denda di tabel");
        txtUpdatedAt.setEditable(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, DesignSystem.OUTLINE_VARIANT),
            BorderFactory.createEmptyBorder(10, 0, 15, 0)
        ));

        GridBagConstraints fGbc = new GridBagConstraints();
        fGbc.fill = GridBagConstraints.HORIZONTAL;
        fGbc.insets = new Insets(4, 6, 4, 6);
        fGbc.weightx = 0.33;

        int r = 0;

        // Row 0: Form Header Title & Subtitle
        fGbc.gridy = r;
        fGbc.gridx = 0;
        fGbc.gridwidth = 3;
        fGbc.weightx = 1.0;
        fGbc.insets = new Insets(4, 0, 4, 0); // Align with table title

        lblFormTitle = new JLabel("Detail & Pembayaran Denda");
        lblFormTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +4");
        lblFormTitle.setForeground(DesignSystem.ON_SURFACE);

        lblFormSubtitle = new JLabel("(Pilih denda pada tabel di bawah untuk melihat rincian.)");
        lblFormSubtitle.setFont(DesignSystem.bodyFont(10f, Font.PLAIN));
        lblFormSubtitle.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(lblFormTitle);
        titlePanel.add(Box.createHorizontalStrut(8));
        titlePanel.add(lblFormSubtitle);
        formPanel.add(titlePanel, fGbc);

        // Row 1: Labels 1-3
        r++;
        fGbc.gridy = r;
        fGbc.gridwidth = 1;
        fGbc.weightx = 0.33;
        fGbc.insets = new Insets(10, 6, 0, 6);

        fGbc.gridx = 0;
        JLabel lblMem = new JLabel("Kode Anggota");
        lblMem.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblMem.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblMem, fGbc);

        fGbc.gridx = 1;
        JLabel lblName = new JLabel("Nama Anggota");
        lblName.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblName.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblName, fGbc);

        fGbc.gridx = 2;
        JLabel lblBook = new JLabel("Judul Buku");
        lblBook.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblBook.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblBook, fGbc);

        // Row 2: Fields 1-3
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(2, 6, 6, 6);

        fGbc.gridx = 0; formPanel.add(txtMemberCode, fGbc);
        fGbc.gridx = 1; formPanel.add(txtMemberName, fGbc);
        fGbc.gridx = 2; formPanel.add(txtBookTitle, fGbc);

        // Row 3: Labels 4-6
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(6, 6, 0, 6);

        fGbc.gridx = 0;
        JLabel lblAmt = new JLabel("Jumlah Denda");
        lblAmt.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblAmt.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblAmt, fGbc);

        fGbc.gridx = 1;
        JLabel lblSts = new JLabel("Status Denda");
        lblSts.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblSts.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblSts, fGbc);

        fGbc.gridx = 2;
        JLabel lblUpd = new JLabel("Diperbarui Pada");
        lblUpd.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblUpd.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblUpd, fGbc);

        // Row 4: Fields 4-6
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(2, 6, 6, 6);

        fGbc.gridx = 0; formPanel.add(txtAmount, fGbc);
        fGbc.gridx = 1; formPanel.add(txtStatus, fGbc);
        fGbc.gridx = 2; formPanel.add(txtUpdatedAt, fGbc);

        // Row 5: Action Buttons Panel (Batal, Proses Pembayaran)
        r++;
        fGbc.gridy = r;
        fGbc.gridx = 0;
        fGbc.gridwidth = 3;
        fGbc.weightx = 1.0;
        fGbc.insets = new Insets(12, 6, 6, 6);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnCancel = new JButton("Batal");
        DesignSystem.applySecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> clearForm());
        buttonPanel.add(btnCancel);

        btnPayFine = new JButton("Proses Pembayaran");
        DesignSystem.applyPrimaryButton(btnPayFine);
        btnPayFine.setEnabled(false);
        buttonPanel.add(btnPayFine);

        formPanel.add(buttonPanel, fGbc);

        northWrapper.add(formPanel);
        add(northWrapper, BorderLayout.NORTH);

        // ─── 2. TABLE PANEL (CENTER - BELOW FORM) ───────────────────────────
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Title directly above table
        finesTitleLabel = new JLabel("Manajemen Denda (Memuat...)");
        finesTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +4");
        finesTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        centerWrapper.add(finesTitleLabel, BorderLayout.NORTH);

        String[] cols = {"ID Denda", "ID Transaksi", "Kode Anggota", "Nama Anggota", "Judul Buku", "Jumlah Denda", "Status", "Diperbarui Pada"};
        finesTableModel = new DefaultTableModel(new Object[][]{}, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        finesTable = UIUtils.createStyledTable(new Object[][]{}, cols);
        finesTable.setModel(finesTableModel);

        // Resize ID columns
        finesTable.getColumnModel().getColumn(0).setMinWidth(0);
        finesTable.getColumnModel().getColumn(0).setMaxWidth(0);
        finesTable.getColumnModel().getColumn(0).setWidth(0);

        finesTable.getColumnModel().getColumn(1).setMinWidth(0);
        finesTable.getColumnModel().getColumn(1).setMaxWidth(0);
        finesTable.getColumnModel().getColumn(1).setWidth(0);

        // Selection Listener
        finesTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = finesTable.getSelectedRow();
            boolean hasSelection = selectedRow != -1;
            if (hasSelection) {
                selectedFineId = (Integer) finesTableModel.getValueAt(selectedRow, 0);
                String code = (String) finesTableModel.getValueAt(selectedRow, 2);
                String name = (String) finesTableModel.getValueAt(selectedRow, 3);
                String book = (String) finesTableModel.getValueAt(selectedRow, 4);
                String amount = (String) finesTableModel.getValueAt(selectedRow, 5);
                String status = (String) finesTableModel.getValueAt(selectedRow, 6);
                String updated = (String) finesTableModel.getValueAt(selectedRow, 7);

                txtMemberCode.setText(code);
                txtMemberName.setText(name);
                txtBookTitle.setText(book);
                txtAmount.setText(amount);
                txtStatus.setText(status);
                txtUpdatedAt.setText(updated);

                btnPayFine.setEnabled("Lunas".equalsIgnoreCase(status) ? false : true);
            } else {
                btnPayFine.setEnabled(false);
            }
        });

        // Double-click row logic
        finesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = finesTable.getSelectedRow();
                    if (selectedRow != -1) {
                        String status = (String) finesTableModel.getValueAt(selectedRow, 6);
                        btnPayFine.setEnabled("Lunas".equalsIgnoreCase(status) ? false : true);
                    }
                }
            }
        });

        // Action Listener
        btnPayFine.addActionListener(e -> {
            int selectedRow = finesTable.getSelectedRow();
            if (selectedRow == -1 && selectedFineId == -1) return;

            final int fineId = selectedFineId != -1 ? selectedFineId : (Integer) finesTableModel.getValueAt(selectedRow, 0);
            String memberName = selectedRow != -1 ? (String) finesTableModel.getValueAt(selectedRow, 3) : txtMemberName.getText();
            String amountStr = selectedRow != -1 ? (String) finesTableModel.getValueAt(selectedRow, 5) : txtAmount.getText();

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Konfirmasi pembayaran sebesar " + amountStr + " dari anggota \"" + memberName + "\"?",
                "Konfirmasi Pembayaran Denda",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                btnPayFine.setEnabled(false);
                SwingWorker<Boolean, Void> payWorker = new SwingWorker<>() {
                    @Override
                    protected Boolean doInBackground() {
                        return fineService.payFine(fineId);
                    }
                    @Override
                    protected void done() {
                        try {
                            boolean success = get();
                            if (success) {
                                JOptionPane.showMessageDialog(FinesPanel.this, "Pembayaran berhasil diproses.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                                loadFinesData(currentSearchQuery);
                                clearForm();
                            } else {
                                JOptionPane.showMessageDialog(FinesPanel.this, "Gagal memproses pembayaran.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(FinesPanel.this, "Kesalahan saat memproses pembayaran: " + ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                payWorker.execute();
            }
        });

        JScrollPane tableScroll = new JScrollPane(finesTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        centerWrapper.add(tableScroll, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);

        // ─── 3. PAGINATION PANEL (SOUTH) ────────────────────────────────────
        btnPrevPage = new JButton();
        btnNextPage = new JButton();
        lblPage = new JLabel("Halaman 1");

        btnPrevPage.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadFinesData(currentSearchQuery);
            }
        });

        btnNextPage.addActionListener(e -> {
            currentPage++;
            loadFinesData(currentSearchQuery);
        });

        add(UIUtils.createPaginationPanel(btnPrevPage, btnNextPage, lblPage), BorderLayout.SOUTH);

        loadFinesData(null);
    }

    private void clearForm() {
        selectedFineId = -1;
        txtMemberCode.setText("");
        txtMemberName.setText("");
        txtBookTitle.setText("");
        txtAmount.setText("");
        txtStatus.setText("");
        txtUpdatedAt.setText("");
        btnPayFine.setEnabled(false);
        finesTable.clearSelection();
    }
    
    public void setSearchQuery(String query) {
        this.currentSearchQuery = query;
        currentPage = 1;
        loadFinesData(query);
    }

    private void loadFinesData(String searchQuery) {
        finesTitleLabel.setText("Manajemen Denda (Memuat...)");
        finesTableModel.setRowCount(0);
        if (btnPrevPage != null) btnPrevPage.setEnabled(false);
        if (btnNextPage != null) btnNextPage.setEnabled(false);
        
        SwingWorker<List<Fine>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Fine> doInBackground() {
                if (searchQuery == null || searchQuery.trim().isEmpty()) {
                    return fineService.getAllFines(currentPage, pageSize);
                } else {
                    return fineService.searchFines(searchQuery.trim(), currentPage, pageSize);
                }
            }
            @Override
            protected void done() {
                try {
                    List<Fine> fines = get();
                    for (Fine f : fines) {
                        String statusIndo = "Paid".equalsIgnoreCase(f.getStatus()) ? "Lunas" : "Belum Lunas";
                        finesTableModel.addRow(new Object[]{
                            f.getFineId(), f.getTransactionId(),
                            f.getMemberCode(), f.getMemberName(), f.getBookTitle(),
                            String.format("Rp %,.2f", f.getAmount()), statusIndo,
                            f.getUpdatedAt() == null ? "-" : f.getUpdatedAt()
                        });
                    }
                    if (lblPage != null) {
                        lblPage.setText("Halaman " + currentPage);
                        btnPrevPage.setEnabled(currentPage > 1);
                        btnNextPage.setEnabled(fines.size() == pageSize);
                    }
                    finesTitleLabel.setText("Manajemen Denda (" + finesTableModel.getRowCount() + " data ditampilkan)");
                } catch (Exception e) {
                    e.printStackTrace();
                    finesTitleLabel.setText("Manajemen Denda (Gagal memuat data)");
                }
            }
        };
        worker.execute();
    }
}
