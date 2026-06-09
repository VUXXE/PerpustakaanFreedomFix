package com.kelompok1.ui.panel.member;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.kelompok1.model.Book;
import com.kelompok1.service.BookService;
import com.kelompok1.ui.panel.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SearchCatalogPanel extends JPanel {
    private final BookService bookService;
    private DefaultTableModel catalogTableModel;
    private JTable catalogTable;
    private JLabel catalogTitleLabel;
    private int currentPage = 1;
    private final int pageSize = 15;
    private JButton btnPrevPage;
    private JButton btnNextPage;
    private JLabel lblPage;

    public SearchCatalogPanel() {
        this.bookService = new BookService();
        
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        initPanel();
    }

    private void initPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIManager.getColor("Panel.background"));
        
        catalogTitleLabel = new JLabel("Katalog Perpustakaan (Memuat...)");
        catalogTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +6");
        headerPanel.add(catalogTitleLabel, BorderLayout.WEST);
        
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setBackground(UIManager.getColor("Panel.background"));
        
        JTextField txtSearch = new JTextField(20);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari judul, penulis, kata kunci...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 999; margin: 4, 10, 4, 10");
        txtSearch.addActionListener(e -> {
            currentPage = 1;
            loadCatalogData(txtSearch.getText());
        });
        
        controlsPanel.add(txtSearch);
        headerPanel.add(controlsPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        String[] cols = {"Judul", "Penulis", "Penerbit", "Nomor Panggil", "Tersedia"};
        catalogTableModel = new DefaultTableModel(new Object[][]{}, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        catalogTable = UIUtils.createStyledTable(new Object[][]{}, cols);
        catalogTable.setModel(catalogTableModel);
        
        catalogTable.setRowHeight(40);
        
        JScrollPane scroll = new JScrollPane(catalogTable);
        add(scroll, BorderLayout.CENTER);
        
        btnPrevPage = new JButton();
        btnNextPage = new JButton();
        lblPage = new JLabel("Halaman 1");
        
        btnPrevPage.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadCatalogData(txtSearch.getText());
            }
        });
        
        btnNextPage.addActionListener(e -> {
            currentPage++;
            loadCatalogData(txtSearch.getText());
        });
        
        add(UIUtils.createPaginationPanel(btnPrevPage, btnNextPage, lblPage), BorderLayout.SOUTH);
        
        loadCatalogData(null);
    }

    private void loadCatalogData(String searchQuery) {
        catalogTitleLabel.setText("Katalog Perpustakaan (Memuat...)");
        catalogTableModel.setRowCount(0);
        if (btnPrevPage != null) btnPrevPage.setEnabled(false);
        if (btnNextPage != null) btnNextPage.setEnabled(false);
        
        SwingWorker<List<Book>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Book> doInBackground() {
                if (searchQuery == null || searchQuery.trim().isEmpty()) {
                    return bookService.getAllBooks(currentPage, pageSize);
                } else {
                    return bookService.searchBooks(searchQuery.trim(), currentPage, pageSize);
                }
            }
            @Override
            protected void done() {
                try {
                    List<Book> books = get();
                    for (Book b : books) {
                        catalogTableModel.addRow(new Object[]{
                            b.getTitle(), b.getAuthor(), b.getPublisher(),
                            b.getCallNumber(), b.getAvailableCopies() + " / " + b.getTotalCopies()
                        });
                    }
                    if (lblPage != null) {
                        lblPage.setText("Halaman " + currentPage);
                        btnPrevPage.setEnabled(currentPage > 1);
                        btnNextPage.setEnabled(books.size() == pageSize);
                    }
                    catalogTitleLabel.setText("Katalog Perpustakaan (" + catalogTableModel.getRowCount() + " data ditampilkan)");
                } catch (Exception e) {
                    e.printStackTrace();
                    catalogTitleLabel.setText("Katalog Perpustakaan (Gagal memuat data)");
                }
            }
        };
        worker.execute();
    }
}
