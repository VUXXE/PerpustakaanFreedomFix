package com.kelompok1.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.model.Book;
import com.kelompok1.model.Transaction;
import com.kelompok1.model.User;
import com.kelompok1.report.ReportGenerator;
import com.kelompok1.service.BookService;
import com.kelompok1.service.FineService;
import com.kelompok1.service.SettingsService;
import com.kelompok1.service.TransactionService;
import com.kelompok1.service.UserService;
import com.kelompok1.util.DesignSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionsPanel extends JPanel {
    private final TransactionService transactionService;
    private final UserService userService;
    private final BookService bookService;
    private final FineService fineService;
    private final SettingsService settingsService;
    private DefaultTableModel transactionsTableModel;
    private JTable transactionsTable;
    private JLabel transactionsTitleLabel;
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
    private JLabel lblMemberName;
    private JTextField txtBookId;
    private JLabel lblBookTitle;
    private JSpinner spinDuration;
    private JButton btnReturnBook;
    private JButton btnIssueBook;
    private JButton btnPrintReceipt;
    private JLabel lblError;
    private final int[] validatedUserId = new int[]{-1};
    private int selectedTransactionId = -1;

    public TransactionsPanel() {
        this.transactionService = new TransactionService();
        this.userService = new UserService();
        this.bookService = new BookService();
        this.fineService = new FineService();
        this.settingsService = new SettingsService();
        
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
        txtMemberCode = UIUtils.createFormTextField("Masukkan Kode Anggota (misal: MEM-0001)");
        txtBookId = UIUtils.createFormTextField("Masukkan ID Buku (angka)");

        lblMemberName = new JLabel(" ");
        lblMemberName.putClientProperty(FlatClientProperties.STYLE, "font: -1");

        lblBookTitle = new JLabel(" ");
        lblBookTitle.putClientProperty(FlatClientProperties.STYLE, "font: -1");

        spinDuration = new JSpinner(new SpinnerNumberModel(7, 1, 90, 1));

        // Match heights and widths
        int tfHeight = txtMemberCode.getPreferredSize().height;
        int tfWidth = txtMemberCode.getPreferredSize().width;
        spinDuration.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        spinDuration.setPreferredSize(new Dimension(tfWidth, tfHeight));

        // Background SwingWorker to load default borrow duration setting
        SwingWorker<Integer, Void> durationLoader = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                try {
                    return Integer.parseInt(settingsService.getSetting("borrow_duration", "7"));
                } catch (Exception e) {
                    return 7;
                }
            }
            @Override
            protected void done() {
                try {
                    spinDuration.setValue(get());
                } catch (Exception e) {
                    // Ignore, fallback to default model
                }
            }
        };
        durationLoader.execute();

        // Listeners for verification as typing
        txtMemberCode.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { check(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { check(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { check(); }
            private void check() {
                SwingUtilities.invokeLater(() -> verifyMember(txtMemberCode.getText().trim(), lblMemberName, validatedUserId));
            }
        });

        txtBookId.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { check(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { check(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { check(); }
            private void check() {
                SwingUtilities.invokeLater(() -> verifyBook(txtBookId.getText().trim(), lblBookTitle));
            }
        });

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
        fGbc.insets = new Insets(4, 0, 4, 0); // No left inset to align with table title

        lblFormTitle = new JLabel("Form Transaksi Peminjaman");
        lblFormTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +4");
        lblFormTitle.setForeground(DesignSystem.ON_SURFACE);

        lblFormSubtitle = new JLabel("(Silakan lengkapi data anggota dan buku.)");
        lblFormSubtitle.setFont(DesignSystem.bodyFont(10f, Font.PLAIN));
        lblFormSubtitle.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(lblFormTitle);
        titlePanel.add(Box.createHorizontalStrut(8));
        titlePanel.add(lblFormSubtitle);
        formPanel.add(titlePanel, fGbc);

        // Row 1: Labels
        r++;
        fGbc.gridy = r;
        fGbc.gridwidth = 1;
        fGbc.weightx = 0.33;
        fGbc.insets = new Insets(10, 6, 0, 6);

        fGbc.gridx = 0;
        JLabel lblMem = new JLabel("Kode Anggota*");
        lblMem.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblMem.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblMem, fGbc);

        fGbc.gridx = 1;
        JLabel lblBkB = new JLabel("ID Buku*");
        lblBkB.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblBkB.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblBkB, fGbc);

        fGbc.gridx = 2;
        JLabel lblDur = new JLabel("Durasi Peminjaman (Hari)*");
        lblDur.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblDur.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblDur, fGbc);

        // Row 2: Fields
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(2, 6, 4, 6);

        fGbc.gridx = 0; formPanel.add(txtMemberCode, fGbc);
        fGbc.gridx = 1; formPanel.add(txtBookId, fGbc);
        fGbc.gridx = 2; formPanel.add(spinDuration, fGbc);

        // Row 3: Verification Feedback Labels
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(0, 6, 6, 6);

        fGbc.gridx = 0; formPanel.add(lblMemberName, fGbc);
        fGbc.gridx = 1; formPanel.add(lblBookTitle, fGbc);

        // Row 4: Buttons
        r++;
        fGbc.gridy = r;
        fGbc.gridx = 0;
        fGbc.gridwidth = 3;
        fGbc.weightx = 1.0;
        fGbc.insets = new Insets(12, 6, 6, 6);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        btnPrintReceipt = new JButton("Cetak Nota");
        DesignSystem.applySecondaryButton(btnPrintReceipt);
        btnPrintReceipt.setEnabled(false);
        buttonPanel.add(btnPrintReceipt);

        btnReturnBook = new JButton("Kembalikan");
        DesignSystem.applyDangerButton(btnReturnBook);
        btnReturnBook.setEnabled(false);
        buttonPanel.add(btnReturnBook);

        JButton btnCancel = new JButton("Batal");
        DesignSystem.applySecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> clearForm());
        buttonPanel.add(btnCancel);

        btnIssueBook = new JButton("Pinjamkan");
        DesignSystem.applyPrimaryButton(btnIssueBook);
        btnIssueBook.addActionListener(e -> saveTransaction());
        buttonPanel.add(btnIssueBook);

        formPanel.add(buttonPanel, fGbc);

        // Row 5: Error Label
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
        transactionsTitleLabel = new JLabel("Riwayat Transaksi (Memuat...)");
        transactionsTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +4");
        transactionsTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        centerWrapper.add(transactionsTitleLabel, BorderLayout.NORTH);

        String[] cols = {"ID Transaksi", "ID Pengguna", "ID Buku", "Kode Anggota", "Judul Buku", "Tgl Pinjam", "Tenggat Waktu", "Tgl Kembali", "Status"};
        transactionsTableModel = new DefaultTableModel(new Object[][]{}, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        transactionsTable = UIUtils.createStyledTable(new Object[][]{}, cols);
        transactionsTable.setModel(transactionsTableModel);

        transactionsTable.getColumnModel().getColumn(1).setMinWidth(0);
        transactionsTable.getColumnModel().getColumn(1).setMaxWidth(0);
        transactionsTable.getColumnModel().getColumn(1).setWidth(0);

        transactionsTable.getColumnModel().getColumn(2).setMinWidth(0);
        transactionsTable.getColumnModel().getColumn(2).setMaxWidth(0);
        transactionsTable.getColumnModel().getColumn(2).setWidth(0);

        // Selection Listener
        transactionsTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = transactionsTable.getSelectedRow();
            boolean hasSelection = selectedRow != -1;
            if (hasSelection) {
                String status = (String) transactionsTableModel.getValueAt(selectedRow, 8);
                btnReturnBook.setEnabled("Issued".equals(status));
                selectedTransactionId = (Integer) transactionsTableModel.getValueAt(selectedRow, 0);
                btnPrintReceipt.setEnabled(true);

                // Populate inline fields
                txtMemberCode.setText((String) transactionsTableModel.getValueAt(selectedRow, 3));
                txtBookId.setText(String.valueOf(transactionsTableModel.getValueAt(selectedRow, 2)));
            } else {
                btnReturnBook.setEnabled(false);
                btnPrintReceipt.setEnabled(false);
                selectedTransactionId = -1;
            }
        });

        // Print Receipt button listener
        btnPrintReceipt.addActionListener(e -> {
            if (selectedTransactionId == -1) return;
            printReceipt(selectedTransactionId);
        });

        // Double-click row logic
        transactionsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = transactionsTable.getSelectedRow();
                    if (selectedRow != -1) {
                        txtMemberCode.setText((String) transactionsTableModel.getValueAt(selectedRow, 3));
                        txtBookId.setText(String.valueOf(transactionsTableModel.getValueAt(selectedRow, 2)));
                    }
                }
            }
        });

        btnReturnBook.addActionListener(e -> {
            int selectedRow = transactionsTable.getSelectedRow();
            if (selectedRow == -1) return;

            int txId = (Integer) transactionsTableModel.getValueAt(selectedRow, 0);
            int userId = (Integer) transactionsTableModel.getValueAt(selectedRow, 1);
            int bookId = (Integer) transactionsTableModel.getValueAt(selectedRow, 2);
            String dueDateStr = (String) transactionsTableModel.getValueAt(selectedRow, 6);

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Proses pengembalian untuk transaksi #" + txId + "?",
                "Konfirmasi Pengembalian",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                btnReturnBook.setEnabled(false);
                SwingWorker<Boolean, Void> returnWorker = new SwingWorker<>() {
                    private double fineAmount = 0.0;
                    private boolean isOverdue = false;

                    @Override
                    protected Boolean doInBackground() {
                        try {
                            java.time.LocalDate today = java.time.LocalDate.now();
                            java.time.LocalDate dueDate = java.time.LocalDate.parse(dueDateStr);
                            if (today.isAfter(dueDate)) {
                                long days = java.time.temporal.ChronoUnit.DAYS.between(dueDate, today);
                                double fineRate = 5000.0;
                                try {
                                    fineRate = Double.parseDouble(settingsService.getSetting("fine_rate", "5000"));
                                } catch (Exception e) {
                                    // fallback
                                }
                                fineAmount = days * fineRate;
                                isOverdue = true;
                            }

                            if (isOverdue) {
                                fineService.assessFines();
                            }

                            return transactionService.returnBook(txId, bookId);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return false;
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            boolean success = get();
                            if (success) {
                                if (fineAmount > 0) {
                                    JOptionPane.showMessageDialog(TransactionsPanel.this, "Buku berhasil dikembalikan.\nDenda keterlambatan sebesar Rp " + String.format("%,.2f", fineAmount) + " telah dikenakan.", "Pengembalian Diproses", JOptionPane.WARNING_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(TransactionsPanel.this, "Buku berhasil dikembalikan. Tidak ada denda.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                                }
                                loadTransactionsData(currentSearchQuery);
                                clearForm();
                            } else {
                                JOptionPane.showMessageDialog(TransactionsPanel.this, "Gagal memproses pengembalian buku.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(TransactionsPanel.this, "Kesalahan saat mengembalikan buku: " + ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                returnWorker.execute();
            }
        });

        JScrollPane tableScroll = new JScrollPane(transactionsTable);
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
                loadTransactionsData(currentSearchQuery);
            }
        });

        btnNextPage.addActionListener(e -> {
            currentPage++;
            loadTransactionsData(currentSearchQuery);
        });

        add(UIUtils.createPaginationPanel(btnPrevPage, btnNextPage, lblPage), BorderLayout.SOUTH);

        loadTransactionsData(null);
    }

    public void setSearchQuery(String query) {
        this.currentSearchQuery = query;
        currentPage = 1;
        loadTransactionsData(query);
    }

    private void loadTransactionsData(String searchQuery) {
        transactionsTitleLabel.setText("Riwayat Transaksi (Memuat...)");
        transactionsTableModel.setRowCount(0);
        if (btnPrevPage != null) btnPrevPage.setEnabled(false);
        if (btnNextPage != null) btnNextPage.setEnabled(false);

        SwingWorker<List<Transaction>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Transaction> doInBackground() {
                if (searchQuery == null || searchQuery.trim().isEmpty()) {
                    return transactionService.getAllTransactions(currentPage, pageSize);
                } else {
                    return transactionService.searchTransactions(searchQuery.trim(), currentPage, pageSize);
                }
            }
            @Override
            protected void done() {
                try {
                    List<Transaction> txs = get();
                    for (Transaction tx : txs) {
                        transactionsTableModel.addRow(new Object[]{
                            tx.getTransactionId(), tx.getUserId(), tx.getBookId(),
                            tx.getMemberCode(), tx.getBookTitle(),
                            tx.getIssueDate(), tx.getDueDate(),
                            tx.getReturnDate() == null ? "-" : tx.getReturnDate(),
                            tx.getStatus()
                        });
                    }
                    if (lblPage != null) {
                        lblPage.setText("Halaman " + currentPage);
                        btnPrevPage.setEnabled(currentPage > 1);
                        btnNextPage.setEnabled(txs.size() == pageSize);
                    }
                    transactionsTitleLabel.setText("Riwayat Transaksi (" + transactionsTableModel.getRowCount() + " data ditampilkan)");
                } catch (Exception e) {
                    e.printStackTrace();
                    transactionsTitleLabel.setText("Riwayat Transaksi (Gagal memuat data)");
                }
            }
        };
        worker.execute();
    }

    private void clearForm() {
        txtMemberCode.setText("");
        txtMemberCode.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtBookId.setText("");
        txtBookId.putClientProperty(FlatClientProperties.OUTLINE, null);

        lblMemberName.setText(" ");
        lblBookTitle.setText(" ");
        lblError.setText(" ");

        validatedUserId[0] = -1;
        selectedTransactionId = -1;

        btnReturnBook.setEnabled(false);
        btnPrintReceipt.setEnabled(false);
        btnIssueBook.setEnabled(true);
        transactionsTable.clearSelection();

        // Reset default borrow duration setting
        SwingWorker<Integer, Void> durationLoader = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                try {
                    return Integer.parseInt(settingsService.getSetting("borrow_duration", "7"));
                } catch (Exception e) {
                    return 7;
                }
            }
            @Override
            protected void done() {
                try {
                    spinDuration.setValue(get());
                } catch (Exception e) {
                    // Ignore fallback
                }
            }
        };
        durationLoader.execute();
    }

    private void saveTransaction() {
        String memberText = txtMemberCode.getText().trim();
        String bookText = txtBookId.getText().trim();

        boolean isValid = true;
        if (memberText.isEmpty()) {
            txtMemberCode.putClientProperty(FlatClientProperties.OUTLINE, "error");
            isValid = false;
        } else {
            txtMemberCode.putClientProperty(FlatClientProperties.OUTLINE, null);
        }

        if (bookText.isEmpty()) {
            txtBookId.putClientProperty(FlatClientProperties.OUTLINE, "error");
            isValid = false;
        } else {
            txtBookId.putClientProperty(FlatClientProperties.OUTLINE, null);
        }

        if (!isValid) {
            lblError.setText("Silakan isi semua kolom.");
            return;
        }

        if (!lblMemberName.getText().startsWith("✔") || validatedUserId[0] == -1) {
            lblError.setText("Silakan masukkan anggota yang valid dan aktif.");
            return;
        }
        if (!lblBookTitle.getText().startsWith("✔")) {
            lblError.setText("Silakan masukkan buku yang valid dan tersedia.");
            return;
        }

        lblError.setText(" ");
        btnIssueBook.setEnabled(false);

        int userId = validatedUserId[0];
        int bookId = Integer.parseInt(bookText);
        int duration = (Integer) spinDuration.getValue();

        SwingWorker<Integer, Void> issueWorker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                return transactionService.issueBook(userId, bookId, duration);
            }

            @Override
            protected void done() {
                btnIssueBook.setEnabled(true);
                try {
                    int newTxId = get();
                    if (newTxId > 0) {
                        loadTransactionsData(currentSearchQuery);
                        clearForm();
                        // Ask user if they want to print receipt
                        int choice = JOptionPane.showConfirmDialog(
                            TransactionsPanel.this,
                            "Buku berhasil dipinjamkan (Transaksi #" + newTxId + ").\nApakah Anda ingin mencetak nota transaksi?",
                            "Pinjamkan Sukses",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        if (choice == JOptionPane.YES_OPTION) {
                            printReceipt(newTxId);
                        }
                    } else {
                        lblError.setText("Gagal meminjamkan buku. Buku mungkin sedang kosong.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    lblError.setText("Terjadi kesalahan: " + ex.getMessage());
                }
            }
        };
        issueWorker.execute();
    }

    private static class MemberVerificationResult {
        final User user;
        final int activeLoans;
        final int maxLimit;
        
        MemberVerificationResult(User user, int activeLoans, int maxLimit) {
            this.user = user;
            this.activeLoans = activeLoans;
            this.maxLimit = maxLimit;
        }
    }

    private void verifyMember(String codeText, JLabel outputLabel, int[] validatedUserId) {
        validatedUserId[0] = -1;
        if (codeText.isEmpty()) {
            outputLabel.setText(" ");
            outputLabel.setForeground(UIManager.getColor("Label.foreground"));
            return;
        }
        
        SwingWorker<MemberVerificationResult, Void> worker = new SwingWorker<>() {
            @Override
            protected MemberVerificationResult doInBackground() {
                User u = userService.getUserByMemberCode(codeText);
                if (u != null) {
                    int activeLoans = transactionService.getActiveLoansCount(u.getUserId());
                    int maxLimit = Integer.parseInt(settingsService.getSetting("max_borrow_limit", "3"));
                    return new MemberVerificationResult(u, activeLoans, maxLimit);
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    MemberVerificationResult result = get();
                    if (result != null && result.user != null) {
                        User u = result.user;
                        String roleIndo = "Admin".equalsIgnoreCase(u.getRole()) ? "Admin" : "Anggota";
                        String statusIndo = "Active".equalsIgnoreCase(u.getStatus()) ? "Aktif" : "Ditangguhkan";
                        if ("Suspended".equals(u.getStatus())) {
                            outputLabel.setText("⚠ " + u.getFullName() + " (Ditangguhkan - TIDAK BISA MEMINJAM)");
                            outputLabel.setForeground(new Color(230, 80, 80));
                        } else if (result.activeLoans >= result.maxLimit) {
                            outputLabel.setText("⚠ " + u.getFullName() + " (Batas pinjam terpenuhi: " + result.activeLoans + "/" + result.maxLimit + " buku)");
                            outputLabel.setForeground(new Color(230, 80, 80));
                        } else {
                            outputLabel.setText("✔ " + u.getFullName() + " (" + roleIndo + " - " + statusIndo + " - Pinjam: " + result.activeLoans + "/" + result.maxLimit + ")");
                            outputLabel.setForeground(new Color(101, 183, 108));
                            validatedUserId[0] = u.getUserId();
                        }
                    } else {
                        outputLabel.setText("❌ Anggota tidak ditemukan");
                        outputLabel.setForeground(new Color(230, 80, 80));
                    }
                } catch (Exception ex) {
                    outputLabel.setText("❌ Kesalahan memeriksa anggota");
                    outputLabel.setForeground(new Color(230, 80, 80));
                }
            }
        };
        worker.execute();
    }

    private void verifyBook(String idText, JLabel outputLabel) {
        if (idText.isEmpty()) {
            outputLabel.setText(" ");
            outputLabel.setForeground(UIManager.getColor("Label.foreground"));
            return;
        }
        try {
            int bookId = Integer.parseInt(idText);
            SwingWorker<Book, Void> worker = new SwingWorker<>() {
                @Override
                protected Book doInBackground() {
                    return bookService.getBookById(bookId);
                }
                @Override
                protected void done() {
                    try {
                        Book b = get();
                        if (b != null) {
                            if (b.getAvailableCopies() > 0) {
                                outputLabel.setText("✔ " + b.getTitle() + " (" + b.getAvailableCopies() + " salinan tersedia)");
                                outputLabel.setForeground(new Color(101, 183, 108));
                            } else {
                                outputLabel.setText("⚠ " + b.getTitle() + " (Kosong - TIDAK BISA MEMINJAM)");
                                outputLabel.setForeground(new Color(230, 80, 80));
                            }
                        } else {
                            outputLabel.setText("❌ Buku tidak ditemukan");
                            outputLabel.setForeground(new Color(230, 80, 80));
                        }
                    } catch (Exception ex) {
                        outputLabel.setText("❌ Kesalahan memeriksa buku");
                        outputLabel.setForeground(new Color(230, 80, 80));
                    }
                }
            };
            worker.execute();
        } catch (NumberFormatException e) {
            outputLabel.setText("❌ Format ID tidak valid");
            outputLabel.setForeground(new Color(230, 80, 80));
        }
    }

    /**
     * Opens the JasperViewer with the transaction receipt for the given transaction ID.
     * Runs the report compilation and fill in a background SwingWorker thread to keep the UI responsive.
     */
    private void printReceipt(int transactionId) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                Map<String, Object> params = new HashMap<>();
                params.put("transaction_id", transactionId);
                ReportGenerator.showReportViewer("/reports/transaction_receipt.jrxml", params);
                return null;
            }
        };
        worker.execute();
    }
}
