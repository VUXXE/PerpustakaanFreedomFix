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

    // Form fields (top panel)
    private JLabel lblFormTitle;
    private JLabel lblFormSubtitle;
    private JTextField txtTitle;
    private JTextField txtSeries;
    private JTextField txtAuthor;
    private JTextField txtPublisher;
    private JTextField txtCallNum;
    private JTextField txtCollation;
    private JTextField txtLanguage;
    private JTextField txtIsbn;
    private JTextField txtClass;
    private JTextField txtEdition;
    private JSpinner spinTotal;
    private JSpinner spinAvail;
    private JButton btnDeleteBook;
    private JButton btnEditBook;
    private JButton btnSaveBook;
    private JLabel lblError;
    private Book editingBook = null;

    public BooksPanel() {
        this.bookService = new BookService();
        
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

        // Instantiate all form fields
        txtTitle = UIUtils.createFormTextField("misal: Clean Code");
        txtSeries = UIUtils.createFormTextField("misal: Pearson Tech");
        txtAuthor = UIUtils.createFormTextField("misal: Robert C. Martin");
        txtPublisher = UIUtils.createFormTextField("misal: Prentice Hall");
        txtCallNum = UIUtils.createFormTextField("misal: QA76.76.C65 M37");
        txtCollation = UIUtils.createFormTextField("misal: 431 p. : ill. ; 23 cm.");
        txtLanguage = UIUtils.createFormTextField("misal: English");
        txtIsbn = UIUtils.createFormTextField("misal: 9780132350884");
        txtClass = UIUtils.createFormTextField("misal: 005.1");
        txtEdition = UIUtils.createFormTextField("misal: Edisi ke-1");

        spinTotal = new JSpinner(new SpinnerNumberModel(1, 0, 10000, 1));
        spinAvail = new JSpinner(new SpinnerNumberModel(1, 0, 10000, 1));

        // Styling spinners to match input fields height and width
        int tfHeight = txtTitle.getPreferredSize().height;
        int tfWidth = txtTitle.getPreferredSize().width;
        spinTotal.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        spinTotal.setPreferredSize(new Dimension(tfWidth, tfHeight));
        spinAvail.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        spinAvail.setPreferredSize(new Dimension(tfWidth, tfHeight));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        // Matte bottom border acting as divider line between form and table
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, DesignSystem.OUTLINE_VARIANT),
            BorderFactory.createEmptyBorder(10, 0, 15, 0)
        ));

        GridBagConstraints fGbc = new GridBagConstraints();
        fGbc.fill = GridBagConstraints.HORIZONTAL;
        fGbc.insets = new Insets(4, 6, 4, 6);
        fGbc.weightx = 0.25;

        int r = 0;

        // Row 0: Form Header Title & Status Subtitle
        fGbc.gridy = r;
        fGbc.gridx = 0;
        fGbc.gridwidth = 4;
        fGbc.weightx = 1.0;
        fGbc.insets = new Insets(4, 0, 4, 0); // No left inset to align with table title

        lblFormTitle = new JLabel("Form Registrasi Buku");
        lblFormTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +4");
        lblFormTitle.setForeground(DesignSystem.ON_SURFACE);

        lblFormSubtitle = new JLabel("(ID Buku akan dibuat otomatis.)");
        lblFormSubtitle.setFont(DesignSystem.bodyFont(10f, Font.PLAIN));
        lblFormSubtitle.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(lblFormTitle);
        titlePanel.add(Box.createHorizontalStrut(8));
        titlePanel.add(lblFormSubtitle);
        formPanel.add(titlePanel, fGbc);

        // Row 1: Labels for inputs 1-4
        r++;
        fGbc.gridy = r;
        fGbc.gridwidth = 1;
        fGbc.weightx = 0.25;
        fGbc.insets = new Insets(10, 6, 0, 6); // gap above input labels

        fGbc.gridx = 0;
        JLabel lblName = new JLabel("Judul Buku*");
        lblName.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblName.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblName, fGbc);

        fGbc.gridx = 1;
        JLabel lblSer = new JLabel("Judul Seri");
        lblSer.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblSer.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblSer, fGbc);

        fGbc.gridx = 2;
        JLabel lblAuth = new JLabel("Penulis");
        lblAuth.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblAuth.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblAuth, fGbc);

        fGbc.gridx = 3;
        JLabel lblPub = new JLabel("Penerbit");
        lblPub.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblPub.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblPub, fGbc);

        // Row 2: Fields 1-4
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(2, 6, 6, 6);

        fGbc.gridx = 0; formPanel.add(txtTitle, fGbc);
        fGbc.gridx = 1; formPanel.add(txtSeries, fGbc);
        fGbc.gridx = 2; formPanel.add(txtAuthor, fGbc);
        fGbc.gridx = 3; formPanel.add(txtPublisher, fGbc);

        // Row 3: Labels for inputs 5-8
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(6, 6, 0, 6);

        fGbc.gridx = 0;
        JLabel lblCall = new JLabel("Nomor Panggil");
        lblCall.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblCall.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblCall, fGbc);

        fGbc.gridx = 1;
        JLabel lblColl = new JLabel("Kolasi");
        lblColl.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblColl.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblColl, fGbc);

        fGbc.gridx = 2;
        JLabel lblLang = new JLabel("Bahasa");
        lblLang.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblLang.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblLang, fGbc);

        fGbc.gridx = 3;
        JLabel lblIsbnField = new JLabel("ISBN");
        lblIsbnField.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblIsbnField.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblIsbnField, fGbc);

        // Row 4: Fields 5-8
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(2, 6, 6, 6);

        fGbc.gridx = 0; formPanel.add(txtCallNum, fGbc);
        fGbc.gridx = 1; formPanel.add(txtCollation, fGbc);
        fGbc.gridx = 2; formPanel.add(txtLanguage, fGbc);
        fGbc.gridx = 3; formPanel.add(txtIsbn, fGbc);

        // Row 5: Labels for inputs 9-12
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(6, 6, 0, 6);

        fGbc.gridx = 0;
        JLabel lblCls = new JLabel("Klasifikasi");
        lblCls.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblCls.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblCls, fGbc);

        fGbc.gridx = 1;
        JLabel lblEd = new JLabel("Edisi");
        lblEd.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblEd.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblEd, fGbc);

        fGbc.gridx = 2;
        JLabel lblTot = new JLabel("Jumlah Salinan");
        lblTot.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblTot.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblTot, fGbc);

        fGbc.gridx = 3;
        JLabel lblAv = new JLabel("Salinan Tersedia");
        lblAv.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblAv.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblAv, fGbc);

        // Row 6: Fields 9-12
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(2, 6, 6, 6);

        fGbc.gridx = 0; formPanel.add(txtClass, fGbc);
        fGbc.gridx = 1; formPanel.add(txtEdition, fGbc);
        fGbc.gridx = 2; formPanel.add(spinTotal, fGbc);
        fGbc.gridx = 3; formPanel.add(spinAvail, fGbc);

        // Row 7: Action Buttons Panel (Hapus, Edit, Simpan, Batal)
        r++;
        fGbc.gridy = r;
        fGbc.gridx = 0;
        fGbc.gridwidth = 4;
        fGbc.weightx = 1.0;
        fGbc.insets = new Insets(12, 6, 6, 6);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        btnDeleteBook = new JButton("Hapus");
        DesignSystem.applyDangerButton(btnDeleteBook);
        btnDeleteBook.setEnabled(false);
        buttonPanel.add(btnDeleteBook);

        btnEditBook = new JButton("Edit");
        DesignSystem.applySecondaryButton(btnEditBook);
        btnEditBook.setEnabled(false);
        buttonPanel.add(btnEditBook);

        JButton btnCancel = new JButton("Batal");
        DesignSystem.applySecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> clearForm());
        buttonPanel.add(btnCancel);

        btnSaveBook = new JButton("Simpan");
        DesignSystem.applyPrimaryButton(btnSaveBook);
        btnSaveBook.addActionListener(e -> saveBook());
        buttonPanel.add(btnSaveBook);

        formPanel.add(buttonPanel, fGbc);

        // Row 8: Error Label
        r++;
        fGbc.gridy = r;
        fGbc.gridx = 0;
        fGbc.gridwidth = 4;
        fGbc.weightx = 1.0;
        fGbc.insets = new Insets(4, 6, 0, 6);

        lblError = new JLabel(" ");
        lblError.putClientProperty(FlatClientProperties.STYLE, "foreground: $Component.error.focusedBorderColor; font: bold -1");
        formPanel.add(lblError, fGbc);

        northWrapper.add(formPanel);
        add(northWrapper, BorderLayout.NORTH);

        // ─── 2. TABLE PANEL (CENTER - BELOW FORM) ───────────────────────────
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Title directly above table
        booksTitleLabel = new JLabel("Manajemen Buku (Memuat...)");
        booksTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +4");
        booksTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        centerWrapper.add(booksTitleLabel, BorderLayout.NORTH);

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

        // Row Selection Listener
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            boolean hasSelection = selectedRow != -1;
            btnEditBook.setEnabled(hasSelection);
            btnDeleteBook.setEnabled(hasSelection);
        });

        // Double-click row to edit
        booksTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = booksTable.getSelectedRow();
                    if (selectedRow != -1) {
                        int bookId = (Integer) booksTableModel.getValueAt(selectedRow, 0);
                        loadBookIntoForm(bookId);
                    }
                }
            }
        });

        // Edit button listener
        btnEditBook.addActionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow != -1) {
                int bookId = (Integer) booksTableModel.getValueAt(selectedRow, 0);
                loadBookIntoForm(bookId);
            }
        });

        // Delete button listener
        btnDeleteBook.addActionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow == -1 && editingBook == null) return;

            final int bookId = (editingBook != null) ? editingBook.getBookId() : (Integer) booksTableModel.getValueAt(selectedRow, 0);
            final String titleStr = (editingBook != null) ? editingBook.getTitle() : (String) booksTableModel.getValueAt(selectedRow, 2);

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
                                clearForm();
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

        JScrollPane tableScroll = new JScrollPane(booksTable);
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
                            b.getBookId(), b.getIsbn() != null ? b.getIsbn() : "", b.getTitle(),
                            b.getCallNumber() != null ? b.getCallNumber() : "", b.getCollation() != null ? b.getCollation() : "",
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

    private void loadBookIntoForm(int bookId) {
        SwingWorker<Book, Void> fetchWorker = new SwingWorker<>() {
            @Override
            protected Book doInBackground() {
                return bookService.getBookById(bookId);
            }
            @Override
            protected void done() {
                try {
                    Book b = get();
                    if (b != null) {
                        editingBook = b;
                        lblFormTitle.setText("Ubah Detail Buku");
                        lblFormSubtitle.setText("(ID Buku: " + b.getBookId() + ")");
                        lblFormSubtitle.setForeground(DesignSystem.PRIMARY);

                        txtTitle.setText(b.getTitle());
                        txtSeries.setText(b.getSeriesTitle() != null ? b.getSeriesTitle() : "");
                        txtAuthor.setText(b.getAuthor() != null ? b.getAuthor() : "");
                        txtPublisher.setText(b.getPublisher() != null ? b.getPublisher() : "");
                        txtCallNum.setText(b.getCallNumber() != null ? b.getCallNumber() : "");
                        txtCollation.setText(b.getCollation() != null ? b.getCollation() : "");
                        txtLanguage.setText(b.getLanguage() != null ? b.getLanguage() : "");
                        txtIsbn.setText(b.getIsbn() != null ? b.getIsbn() : "");
                        txtClass.setText(b.getClassification() != null ? b.getClassification() : "");
                        txtEdition.setText(b.getEdition() != null ? b.getEdition() : "");
                        spinTotal.setValue(b.getTotalCopies());
                        spinAvail.setValue(b.getAvailableCopies());

                        btnDeleteBook.setEnabled(true);
                        btnSaveBook.setText("Simpan Perubahan");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        fetchWorker.execute();
    }

    private void clearForm() {
        editingBook = null;
        lblFormTitle.setText("Form Registrasi Buku");
        lblFormSubtitle.setText("(ID Buku akan dibuat otomatis.)");
        lblFormSubtitle.setForeground(UIManager.getColor("Label.disabledForeground"));

        txtTitle.setText("");
        txtTitle.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtSeries.setText("");
        txtAuthor.setText("");
        txtPublisher.setText("");
        txtCallNum.setText("");
        txtCollation.setText("");
        txtLanguage.setText("");
        txtIsbn.setText("");
        txtClass.setText("");
        txtEdition.setText("");
        spinTotal.setValue(1);
        spinAvail.setValue(1);

        btnDeleteBook.setEnabled(false);
        btnEditBook.setEnabled(false);
        btnSaveBook.setText("Simpan");
        btnSaveBook.setEnabled(true);
        lblError.setText(" ");
        booksTable.clearSelection();
    }

    private void saveBook() {
        String titleStr = txtTitle.getText().trim();
        if (titleStr.isEmpty()) {
            txtTitle.putClientProperty(FlatClientProperties.OUTLINE, "error");
            lblError.setText("Judul wajib diisi.");
            return;
        } else {
            txtTitle.putClientProperty(FlatClientProperties.OUTLINE, null);
        }

        int totalVal = (Integer) spinTotal.getValue();
        int availVal = (Integer) spinAvail.getValue();

        if (availVal > totalVal) {
            lblError.setText("Salinan tersedia tidak boleh melebihi jumlah salinan.");
            return;
        }

        lblError.setText(" ");
        btnSaveBook.setEnabled(false);

        Book book = editingBook == null ? new Book() : editingBook;
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
                if (editingBook == null) {
                    return bookService.addBook(book);
                } else {
                    return bookService.updateBook(book);
                }
            }

            @Override
            protected void done() {
                btnSaveBook.setEnabled(true);
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(BooksPanel.this, "Buku berhasil disimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        loadBooksData(currentSearchQuery);
                        clearForm();
                    } else {
                        lblError.setText("Gagal menyimpan buku ke database.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    lblError.setText("Kesalahan: " + ex.getMessage());
                }
            }
        };
        saveWorker.execute();
    }
}
