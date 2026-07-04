package com.kelompok1.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.model.Models.Book;
import com.kelompok1.service.Services.BookService;
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
    private JTextField txtAuthor;
    private JTextField txtPublisher;
    private JTextField txtIsbn;
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

        // Instantiate form fields
        txtTitle = UIUtils.createFormTextField("misal: Clean Code");
        txtAuthor = UIUtils.createFormTextField("misal: Robert C. Martin");
        txtPublisher = UIUtils.createFormTextField("misal: Prentice Hall");
        txtIsbn = UIUtils.createFormTextField("misal: 9780132350884");

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
        fGbc.weightx = 0.33;

        int r = 0;

        // Row 0: Form Header Title & Status Subtitle
        fGbc.gridy = r;
        fGbc.gridx = 0;
        fGbc.gridwidth = 3;
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

        // Row 1: Labels for inputs 1-3
        r++;
        fGbc.gridy = r;
        fGbc.gridwidth = 1;
        fGbc.weightx = 0.33;
        fGbc.insets = new Insets(10, 6, 0, 6); // gap above input labels

        fGbc.gridx = 0;
        JLabel lblName = new JLabel("Judul Buku*");
        lblName.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblName.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblName, fGbc);

        fGbc.gridx = 1;
        JLabel lblAuth = new JLabel("Penulis");
        lblAuth.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblAuth.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblAuth, fGbc);

        fGbc.gridx = 2;
        JLabel lblPub = new JLabel("Penerbit");
        lblPub.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblPub.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblPub, fGbc);

        // Row 2: Fields 1-3
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(2, 6, 6, 6);

        fGbc.gridx = 0; formPanel.add(txtTitle, fGbc);
        fGbc.gridx = 1; formPanel.add(txtAuthor, fGbc);
        fGbc.gridx = 2; formPanel.add(txtPublisher, fGbc);

        // Row 3: Labels for inputs 4-6
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(6, 6, 0, 6);

        fGbc.gridx = 0;
        JLabel lblIsbnField = new JLabel("ISBN");
        lblIsbnField.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblIsbnField.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblIsbnField, fGbc);

        fGbc.gridx = 1;
        JLabel lblTot = new JLabel("Jumlah Salinan");
        lblTot.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblTot.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblTot, fGbc);

        fGbc.gridx = 2;
        JLabel lblAv = new JLabel("Salinan Tersedia");
        lblAv.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblAv.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblAv, fGbc);

        // Row 4: Fields 4-6
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(2, 6, 6, 6);

        fGbc.gridx = 0; formPanel.add(txtIsbn, fGbc);
        fGbc.gridx = 1; formPanel.add(spinTotal, fGbc);
        fGbc.gridx = 2; formPanel.add(spinAvail, fGbc);

        // Row 5: Action Buttons Panel (Hapus, Edit, Simpan, Batal)
        r++;
        fGbc.gridy = r;
        fGbc.gridx = 0;
        fGbc.gridwidth = 3;
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

        // Row 6: Error Label
        r++;
        fGbc.gridy = r;
        fGbc.gridx = 0;
        fGbc.gridwidth = 3;
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

        String[] cols = {"ID", "ISBN", "Judul", "Penulis", "Penerbit", "Total", "Tersedia"};
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
        // Resize Penulis column
        booksTable.getColumnModel().getColumn(3).setMinWidth(120);
        booksTable.getColumnModel().getColumn(3).setMaxWidth(150);
        // Resize Penerbit column
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
                try {
                    boolean success = bookService.deleteBook(bookId);
                    if (success) {
                        JOptionPane.showMessageDialog(BooksPanel.this, "Buku berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        loadBooksData(currentSearchQuery);
                        clearForm();
                    } else {
                        JOptionPane.showMessageDialog(BooksPanel.this, "Gagal menghapus buku.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (java.sql.SQLException ex) {
                    String msg;
                    if (ex.getSQLState() != null && ex.getSQLState().startsWith("23")) {
                        msg = "Tidak dapat menghapus buku ini karena dirujuk dalam transaksi peminjaman.";
                    } else {
                        msg = ex.getMessage();
                    }
                    JOptionPane.showMessageDialog(BooksPanel.this, msg, "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BooksPanel.this, "Terjadi kesalahan: " + ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                }
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

        try {
            List<Book> books;
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                books = bookService.getAllBooks(currentPage, pageSize);
            } else {
                books = bookService.searchBooks(searchQuery.trim(), currentPage, pageSize);
            }
            
            for (Book b : books) {
                booksTableModel.addRow(new Object[]{
                    b.getBookId(), b.getIsbn() != null ? b.getIsbn() : "", b.getTitle(),
                    b.getAuthor() != null ? b.getAuthor() : "", b.getPublisher() != null ? b.getPublisher() : "",
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

    private void loadBookIntoForm(int bookId) {
        try {
            Book b = bookService.getBookById(bookId);
            if (b != null) {
                editingBook = b;
                lblFormTitle.setText("Ubah Detail Buku");
                lblFormSubtitle.setText("(ID Buku: " + b.getBookId() + ")");
                lblFormSubtitle.setForeground(DesignSystem.PRIMARY);

                txtTitle.setText(b.getTitle());
                txtAuthor.setText(b.getAuthor() != null ? b.getAuthor() : "");
                txtPublisher.setText(b.getPublisher() != null ? b.getPublisher() : "");
                txtIsbn.setText(b.getIsbn() != null ? b.getIsbn() : "");
                spinTotal.setValue(b.getTotalCopies());
                spinAvail.setValue(b.getAvailableCopies());

                btnDeleteBook.setEnabled(true);
                btnSaveBook.setText("Simpan Perubahan");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearForm() {
        editingBook = null;
        lblFormTitle.setText("Form Registrasi Buku");
        lblFormSubtitle.setText("(ID Buku akan dibuat otomatis.)");
        lblFormSubtitle.setForeground(UIManager.getColor("Label.disabledForeground"));

        txtTitle.setText("");
        txtTitle.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtAuthor.setText("");
        txtPublisher.setText("");
        txtIsbn.setText("");
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
        book.setAuthor(txtAuthor.getText().trim());
        book.setPublisher(txtPublisher.getText().trim());
        book.setIsbn(txtIsbn.getText().trim());
        book.setTotalCopies(totalVal);
        book.setAvailableCopies(availVal);

        try {
            boolean success;
            if (editingBook == null) {
                success = bookService.addBook(book);
            } else {
                success = bookService.updateBook(book);
            }

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
        } finally {
            btnSaveBook.setEnabled(true);
        }
    }
}
