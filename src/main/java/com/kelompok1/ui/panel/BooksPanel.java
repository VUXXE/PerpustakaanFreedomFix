package com.kelompok1.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.model.Book;
import com.kelompok1.service.BookService;
import com.kelompok1.util.DesignSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BooksPanel extends JPanel {
    private final BookService bookService;
    private DefaultTableModel booksTableModel;
    private JTable booksTable;
    private JLabel booksTitleLabel;
    private int currentPage = 1;
    private final int pageSize = 15;
    private JButton btnPrevPage;
    private JButton btnNextPage;
    private JLabel lblPage;
    private String currentSearchQuery = null;

    public BooksPanel() {
        this.bookService = new BookService();
        
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        initPanel();
    }

    private void initPanel() {
        // --- HEADER PANEL (NORTH) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIManager.getColor("Panel.background"));
        
        booksTitleLabel = new JLabel("Manajemen Buku (Memuat...)");
        booksTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +6");
        headerPanel.add(booksTitleLabel, BorderLayout.WEST);
        
        // Controls (Search + Add/Edit/Delete)
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setBackground(UIManager.getColor("Panel.background"));
        
        // Local search bar removed, using header search bar
        
        JButton btnAddBook = new JButton("+ Tambah Buku");
        DesignSystem.applyPrimaryButton(btnAddBook);

        JButton btnEditBook = new JButton("Ubah");
        DesignSystem.applySecondaryButton(btnEditBook);
        btnEditBook.setEnabled(false);

        JButton btnDeleteBook = new JButton("Hapus");
        DesignSystem.applyDangerButton(btnDeleteBook);
        btnDeleteBook.setEnabled(false);

        controlsPanel.add(btnAddBook);
        controlsPanel.add(btnEditBook);
        controlsPanel.add(btnDeleteBook);
        headerPanel.add(controlsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        
        // --- TABLE (CENTER) ---
        String[] cols = {"ID", "ISBN/ISSN", "Judul", "Nomor Panggil", "Kolasi", "Total", "Tersedia"};
        booksTableModel = new DefaultTableModel(new Object[][]{}, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        booksTable = UIUtils.createStyledTable(new Object[][]{}, cols);
        booksTable.setModel(booksTableModel);
        
        // Resize ID column
        booksTable.getColumnModel().getColumn(0).setMinWidth(50);
        booksTable.getColumnModel().getColumn(0).setMaxWidth(50);
        // Resize ISBN column
        booksTable.getColumnModel().getColumn(1).setMinWidth(120);
        booksTable.getColumnModel().getColumn(1).setMaxWidth(150);
        // Resize Call Number column
        booksTable.getColumnModel().getColumn(3).setMinWidth(120);
        booksTable.getColumnModel().getColumn(3).setMaxWidth(150);
        // Resize Collation column
        booksTable.getColumnModel().getColumn(4).setMinWidth(120);
        booksTable.getColumnModel().getColumn(4).setMaxWidth(150);
        // Resize Total column
        booksTable.getColumnModel().getColumn(5).setMinWidth(50);
        booksTable.getColumnModel().getColumn(5).setMaxWidth(50);
        // Resize Avail column
        booksTable.getColumnModel().getColumn(6).setMinWidth(50);
        booksTable.getColumnModel().getColumn(6).setMaxWidth(50);
        
        // Make the Title column wrap text to 2 lines
        booksTable.setRowHeight(40);
        booksTable.getColumnModel().getColumn(2).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            private final javax.swing.JTextArea textArea = new javax.swing.JTextArea();
            {
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setOpaque(true);
                textArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 5, 2, 5));
            }
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (isSelected) {
                    textArea.setBackground(t.getSelectionBackground());
                    textArea.setForeground(t.getSelectionForeground());
                } else {
                    textArea.setBackground(t.getBackground());
                    textArea.setForeground(t.getForeground());
                }
                textArea.setFont(t.getFont());
                textArea.setText(value != null ? value.toString() : "");
                return textArea;
            }
        });
        
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = booksTable.getSelectedRow() != -1;
            btnEditBook.setEnabled(hasSelection);
            btnDeleteBook.setEnabled(hasSelection);
        });
        
        // Action Listeners
        btnAddBook.addActionListener(e -> showBookFormDialog(null));
        
        btnEditBook.addActionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow == -1) return;
            int bookId = (Integer) booksTableModel.getValueAt(selectedRow, 0);
            
            btnEditBook.setEnabled(false);
            SwingWorker<Book, Void> fetchWorker = new SwingWorker<>() {
                @Override
                protected Book doInBackground() {
                    return bookService.getBookById(bookId);
                }
                @Override
                protected void done() {
                    btnEditBook.setEnabled(true);
                    try {
                        Book b = get();
                        if (b != null) {
                            showBookFormDialog(b);
                        } else {
                            JOptionPane.showMessageDialog(BooksPanel.this, "Gagal mengambil detail buku.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(BooksPanel.this, "Kesalahan saat mengambil data: " + ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            fetchWorker.execute();
        });
        
        btnDeleteBook.addActionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow == -1) return;
            int bookId = (Integer) booksTableModel.getValueAt(selectedRow, 0);
            String titleStr = (String) booksTableModel.getValueAt(selectedRow, 2);
            
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin menghapus \"" + titleStr + "\"?\nTindakan ini tidak dapat dibatalkan.",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                SwingWorker<Boolean, Void> deleteWorker = new SwingWorker<>() {
                    private String errorMessage = null;
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        try {
                            return bookService.deleteBook(bookId);
                        } catch (java.sql.SQLException ex) {
                            if (ex.getSQLState() != null && ex.getSQLState().startsWith("23")) {
                                errorMessage = "Tidak dapat menghapus buku ini karena dirujuk dalam transaksi peminjaman.";
                            } else {
                                errorMessage = ex.getMessage();
                            }
                            throw ex;
                        }
                    }
                    @Override
                    protected void done() {
                        try {
                            boolean success = get();
                            if (success) {
                                JOptionPane.showMessageDialog(BooksPanel.this, "Buku berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                                loadBooksData(currentSearchQuery);
                            } else {
                                JOptionPane.showMessageDialog(BooksPanel.this, "Gagal menghapus buku.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            String msg = errorMessage != null ? errorMessage : "Terjadi kesalahan: " + ex.getMessage();
                            JOptionPane.showMessageDialog(BooksPanel.this, msg, "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                deleteWorker.execute();
            }
        });
        
        JScrollPane scroll = new JScrollPane(booksTable);
        add(scroll, BorderLayout.CENTER);
        
        btnPrevPage = new JButton();
        btnNextPage = new JButton();
        lblPage = new JLabel("Halaman 1");
        
        btnPrevPage.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadBooksData(currentSearchQuery);
            }
        });
        
        btnNextPage.addActionListener(e -> {
            currentPage++;
            loadBooksData(currentSearchQuery);
        });
        
        add(UIUtils.createPaginationPanel(btnPrevPage, btnNextPage, lblPage), BorderLayout.SOUTH);
        
        loadBooksData(null);
    }
    
    public void setSearchQuery(String query) {
        this.currentSearchQuery = query;
        currentPage = 1;
        loadBooksData(query);
    }

    private void loadBooksData(String searchQuery) {
        booksTitleLabel.setText("Manajemen Buku (Memuat...)");
        booksTableModel.setRowCount(0);
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
                        booksTableModel.addRow(new Object[]{
                            b.getBookId(), b.getIsbn(), b.getTitle(),
                            b.getCallNumber(), b.getCollation(),
                            b.getTotalCopies(), b.getAvailableCopies()
                        });
                    }
                    if (lblPage != null) {
                        lblPage.setText("Halaman " + currentPage);
                        btnPrevPage.setEnabled(currentPage > 1);
                        btnNextPage.setEnabled(books.size() == pageSize);
                    }
                    booksTitleLabel.setText("Manajemen Buku (" + booksTableModel.getRowCount() + " data ditampilkan)");
                } catch (Exception e) {
                    e.printStackTrace();
                    booksTitleLabel.setText("Manajemen Buku (Gagal memuat data)");
                }
            }
        };
        worker.execute();
    }

    private void showBookFormDialog(Book bookToEdit) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, bookToEdit == null ? "Tambah Buku Baru" : "Ubah Detail Buku", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(550, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.weightx = 1.0;

        JTextField txtTitle = UIUtils.createFormTextField("misal: Clean Code");
        JTextField txtSeries = UIUtils.createFormTextField("misal: Pearson Tech");
        JTextField txtAuthor = UIUtils.createFormTextField("misal: Robert C. Martin");
        JTextField txtPublisher = UIUtils.createFormTextField("misal: Prentice Hall");
        JTextField txtCallNum = UIUtils.createFormTextField("misal: QA76.76.C65 M37");
        JTextField txtCollation = UIUtils.createFormTextField("misal: 431 p. : ill. ; 23 cm.");
        JTextField txtLanguage = UIUtils.createFormTextField("misal: English");
        JTextField txtIsbn = UIUtils.createFormTextField("misal: 9780132350884");
        JTextField txtClass = UIUtils.createFormTextField("misal: 005.1");
        JTextField txtEdition = UIUtils.createFormTextField("misal: Edisi ke-1");

        JSpinner spinTotal = new JSpinner(new SpinnerNumberModel(1, 0, 10000, 1));
        JSpinner spinAvail = new JSpinner(new SpinnerNumberModel(1, 0, 10000, 1));

        if (bookToEdit != null) {
            txtTitle.setText(bookToEdit.getTitle());
            txtSeries.setText(bookToEdit.getSeriesTitle());
            txtAuthor.setText(bookToEdit.getAuthor());
            txtPublisher.setText(bookToEdit.getPublisher());
            txtCallNum.setText(bookToEdit.getCallNumber());
            txtCollation.setText(bookToEdit.getCollation());
            txtLanguage.setText(bookToEdit.getLanguage());
            txtIsbn.setText(bookToEdit.getIsbn());
            txtClass.setText(bookToEdit.getClassification());
            txtEdition.setText(bookToEdit.getEdition());
            spinTotal.setValue(bookToEdit.getTotalCopies());
            spinAvail.setValue(bookToEdit.getAvailableCopies());
        }

        int row = 0;
        UIUtils.addFormRow(formPanel, gbc, "Judul*", txtTitle, row++);
        UIUtils.addFormRow(formPanel, gbc, "Judul Seri", txtSeries, row++);
        UIUtils.addFormRow(formPanel, gbc, "Penulis", txtAuthor, row++);
        UIUtils.addFormRow(formPanel, gbc, "Penerbit", txtPublisher, row++);
        UIUtils.addFormRow(formPanel, gbc, "Nomor Panggil", txtCallNum, row++);
        UIUtils.addFormRow(formPanel, gbc, "Kolasi", txtCollation, row++);
        UIUtils.addFormRow(formPanel, gbc, "Bahasa", txtLanguage, row++);
        UIUtils.addFormRow(formPanel, gbc, "ISBN", txtIsbn, row++);
        UIUtils.addFormRow(formPanel, gbc, "Klasifikasi", txtClass, row++);
        UIUtils.addFormRow(formPanel, gbc, "Edisi", txtEdition, row++);
        UIUtils.addFormRow(formPanel, gbc, "Jumlah Salinan", spinTotal, row++);
        UIUtils.addFormRow(formPanel, gbc, "Salinan Tersedia", spinAvail, row++);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setBackground(UIManager.getColor("Panel.background"));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));

        JButton btnCancel = new JButton("Batal");
        DesignSystem.applySecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = new JButton("Simpan");
        DesignSystem.applyPrimaryButton(btnSave);
        btnSave.addActionListener(e -> {
            String titleStr = txtTitle.getText().trim();
            if (titleStr.isEmpty()) {
                txtTitle.putClientProperty(FlatClientProperties.OUTLINE, "error");
                JOptionPane.showMessageDialog(dialog, "Judul wajib diisi.", "Kesalahan Validasi", JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                txtTitle.putClientProperty(FlatClientProperties.OUTLINE, null);
            }

            int totalVal = (Integer) spinTotal.getValue();
            int availVal = (Integer) spinAvail.getValue();

            if (availVal > totalVal) {
                JOptionPane.showMessageDialog(dialog, "Salinan tersedia tidak boleh melebihi jumlah salinan.", "Kesalahan Validasi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Book book = bookToEdit == null ? new Book() : bookToEdit;
            book.setTitle(titleStr);
            book.setSeriesTitle(txtSeries.getText().trim());
            book.setAuthor(txtAuthor.getText().trim());
            book.setPublisher(txtPublisher.getText().trim());
            book.setCallNumber(txtCallNum.getText().trim());
            book.setCollation(txtCollation.getText().trim());
            book.setLanguage(txtLanguage.getText().trim());
            book.setIsbn(txtIsbn.getText().trim());
            book.setClassification(txtClass.getText().trim());
            book.setEdition(txtEdition.getText().trim());
            book.setTotalCopies(totalVal);
            book.setAvailableCopies(availVal);

            SwingWorker<Boolean, Void> saveWorker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    if (bookToEdit == null) {
                        return bookService.addBook(book);
                    } else {
                        return bookService.updateBook(book);
                    }
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            dialog.dispose();
                            loadBooksData(null);
                        } else {
                            JOptionPane.showMessageDialog(dialog, "Gagal menyimpan buku ke database.", "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(dialog, "Terjadi kesalahan: " + ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            saveWorker.execute();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
