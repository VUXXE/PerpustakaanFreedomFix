package com.kelompok1.ui.panel.member;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.model.Transaction;
import com.kelompok1.service.TransactionService;
import com.kelompok1.ui.panel.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BorrowedBooksPanel extends JPanel {
    private final TransactionService transactionService;
    private final int userId;
    private DefaultTableModel borrowedTableModel;
    private JTable borrowedTable;
    private JLabel borrowedTitleLabel;
    private int currentPage = 1;
    private final int pageSize = 15;
    private JButton btnPrevPage;
    private JButton btnNextPage;
    private JLabel lblPage;

    public BorrowedBooksPanel(int userId) {
        this.transactionService = new TransactionService();
        this.userId = userId;
        
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        initPanel();
    }

    private void initPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIManager.getColor("Panel.background"));
        
        borrowedTitleLabel = new JLabel("Pinjaman Saya (Memuat...)");
        borrowedTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +6");
        headerPanel.add(borrowedTitleLabel, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        String[] cols = {"ID Transaksi", "Judul Buku", "Tgl Pinjam", "Tenggat Waktu", "Tgl Kembali", "Status"};
        borrowedTableModel = new DefaultTableModel(new Object[][]{}, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        borrowedTable = UIUtils.createStyledTable(new Object[][]{}, cols);
        borrowedTable.setModel(borrowedTableModel);
        
        JScrollPane scroll = new JScrollPane(borrowedTable);
        add(scroll, BorderLayout.CENTER);
        
        btnPrevPage = new JButton();
        btnNextPage = new JButton();
        lblPage = new JLabel("Halaman 1");
        
        btnPrevPage.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadBorrowedData();
            }
        });
        
        btnNextPage.addActionListener(e -> {
            currentPage++;
            loadBorrowedData();
        });
        
        add(UIUtils.createPaginationPanel(btnPrevPage, btnNextPage, lblPage), BorderLayout.SOUTH);
        
        loadBorrowedData();
    }

    private void loadBorrowedData() {
        borrowedTableModel.setRowCount(0);
        if (btnPrevPage != null) btnPrevPage.setEnabled(false);
        if (btnNextPage != null) btnNextPage.setEnabled(false);
        
        SwingWorker<List<Transaction>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Transaction> doInBackground() {
                return transactionService.getTransactionsByUser(userId, currentPage, pageSize);
            }
            @Override
            protected void done() {
                try {
                    List<Transaction> txs = get();
                    for (Transaction tx : txs) {
                        String statusIndo = tx.getStatus();
                        if ("Issued".equalsIgnoreCase(statusIndo)) statusIndo = "Dipinjam";
                        else if ("Returned".equalsIgnoreCase(statusIndo)) statusIndo = "Dikembalikan";
                        else if ("Lost".equalsIgnoreCase(statusIndo)) statusIndo = "Hilang";
                        
                        borrowedTableModel.addRow(new Object[]{
                            tx.getTransactionId(), tx.getBookTitle(),
                            tx.getIssueDate(), tx.getDueDate(),
                            tx.getReturnDate() == null ? "-" : tx.getReturnDate(),
                            statusIndo
                        });
                    }
                    if (lblPage != null) {
                        lblPage.setText("Halaman " + currentPage);
                        btnPrevPage.setEnabled(currentPage > 1);
                        btnNextPage.setEnabled(txs.size() == pageSize);
                    }
                    borrowedTitleLabel.setText("Pinjaman Saya (" + borrowedTableModel.getRowCount() + " data ditampilkan)");
                } catch (Exception e) {
                    e.printStackTrace();
                    borrowedTitleLabel.setText("Pinjaman Saya (Gagal memuat data)");
                }
            }
        };
        worker.execute();
    }

    public void refreshTable() {
        loadBorrowedData();
    }
}
