package com.kelompok1.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.kelompok1.model.Fine;
import com.kelompok1.service.FineService;

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

    public FinesPanel() {
        this.fineService = new FineService();
        
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        initPanel();
    }

    private void initPanel() {
        // --- HEADER PANEL (NORTH) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIManager.getColor("Panel.background"));
        
        finesTitleLabel = new JLabel("Manajemen Denda (Memuat...)");
        finesTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +6");
        headerPanel.add(finesTitleLabel, BorderLayout.WEST);
        
        // Controls (Search + Pay Fine)
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setBackground(UIManager.getColor("Panel.background"));
        
        // Local search bar removed, using header search bar
        
        JButton btnPayFine = new JButton("Proses Pembayaran");
        btnPayFine.putClientProperty(FlatClientProperties.STYLE, "background: $Component.accentColor; foreground: #ffffff; arc: 10");
        btnPayFine.setEnabled(false);
        
        controlsPanel.add(btnPayFine);
        headerPanel.add(controlsPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // --- TABLE (CENTER) ---
        String[] cols = {"ID Denda", "ID Transaksi", "Kode Anggota", "Nama Anggota", "Judul Buku", "Jumlah Denda", "Status", "Diperbarui Pada"};
        finesTableModel = new DefaultTableModel(new Object[][]{}, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        finesTable = UIUtils.createStyledTable(new Object[][]{}, cols);
        finesTable.setModel(finesTableModel);
        
        finesTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = finesTable.getSelectedRow();
            if (selectedRow != -1) {
                String status = (String) finesTableModel.getValueAt(selectedRow, 6);
                btnPayFine.setEnabled("Belum Lunas".equals(status));
            } else {
                btnPayFine.setEnabled(false);
            }
        });
        
        // Action Listener
        btnPayFine.addActionListener(e -> {
            int selectedRow = finesTable.getSelectedRow();
            if (selectedRow == -1) return;
            
            int fineId = (Integer) finesTableModel.getValueAt(selectedRow, 0);
            String memberName = (String) finesTableModel.getValueAt(selectedRow, 3);
            String amountStr = (String) finesTableModel.getValueAt(selectedRow, 5);
            
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
        
        JScrollPane scroll = new JScrollPane(finesTable);
        add(scroll, BorderLayout.CENTER);
        
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
