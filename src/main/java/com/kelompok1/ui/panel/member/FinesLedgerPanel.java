package com.kelompok1.ui.panel.member;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.model.Fine;
import com.kelompok1.service.FineService;
import com.kelompok1.ui.panel.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FinesLedgerPanel extends JPanel {
    private final FineService fineService;
    private final int userId;
    private DefaultTableModel finesTableModel;
    private JTable finesTable;
    private JLabel finesTitleLabel;
    private int currentPage = 1;
    private final int pageSize = 15;
    private JButton btnPrevPage;
    private JButton btnNextPage;
    private JLabel lblPage;

    public FinesLedgerPanel(int userId) {
        this.fineService = new FineService();
        this.userId = userId;
        
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        initPanel();
    }

    private void initPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIManager.getColor("Panel.background"));
        
        finesTitleLabel = new JLabel("Tagihan Denda Saya (Memuat...)");
        finesTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +6");
        headerPanel.add(finesTitleLabel, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        String[] cols = {"ID Denda", "ID Transaksi", "Judul Buku", "Jumlah Denda", "Status", "Diperbarui Pada"};
        finesTableModel = new DefaultTableModel(new Object[][]{}, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        finesTable = UIUtils.createStyledTable(new Object[][]{}, cols);
        finesTable.setModel(finesTableModel);
        
        JScrollPane scroll = new JScrollPane(finesTable);
        add(scroll, BorderLayout.CENTER);
        
        btnPrevPage = new JButton();
        btnNextPage = new JButton();
        lblPage = new JLabel("Halaman 1");
        
        btnPrevPage.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadFinesData();
            }
        });
        
        btnNextPage.addActionListener(e -> {
            currentPage++;
            loadFinesData();
        });
        
        add(UIUtils.createPaginationPanel(btnPrevPage, btnNextPage, lblPage), BorderLayout.SOUTH);
        
        loadFinesData();
    }

    private void loadFinesData() {
        finesTableModel.setRowCount(0);
        if (btnPrevPage != null) btnPrevPage.setEnabled(false);
        if (btnNextPage != null) btnNextPage.setEnabled(false);
        
        SwingWorker<List<Fine>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Fine> doInBackground() {
                return fineService.getFinesByUser(userId, currentPage, pageSize);
            }
            @Override
            protected void done() {
                try {
                    List<Fine> fines = get();
                    for (Fine f : fines) {
                        String statusIndo = "Paid".equalsIgnoreCase(f.getStatus()) ? "Lunas" : "Belum Lunas";
                        finesTableModel.addRow(new Object[]{
                            f.getFineId(), f.getTransactionId(), f.getBookTitle(),
                            String.format("Rp %,.2f", f.getAmount()), statusIndo, f.getUpdatedAt()
                        });
                    }
                    if (lblPage != null) {
                        lblPage.setText("Halaman " + currentPage);
                        btnPrevPage.setEnabled(currentPage > 1);
                        btnNextPage.setEnabled(fines.size() == pageSize);
                    }
                    finesTitleLabel.setText("Tagihan Denda Saya (" + finesTableModel.getRowCount() + " data ditampilkan)");
                } catch (Exception e) {
                    e.printStackTrace();
                    finesTitleLabel.setText("Tagihan Denda Saya (Gagal memuat data)");
                }
            }
        };
        worker.execute();
    }
}
