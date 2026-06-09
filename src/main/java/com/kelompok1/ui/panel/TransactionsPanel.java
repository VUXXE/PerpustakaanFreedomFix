package com.kelompok1.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.kelompok1.model.Book;
import com.kelompok1.model.Transaction;
import com.kelompok1.model.User;
import com.kelompok1.service.BookService;
import com.kelompok1.service.FineService;
import com.kelompok1.service.SettingsService;
import com.kelompok1.service.TransactionService;
import com.kelompok1.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

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
        // --- HEADER PANEL (NORTH) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIManager.getColor("Panel.background"));
        
        transactionsTitleLabel = new JLabel("Riwayat Transaksi (Memuat...)");
        transactionsTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +6");
        headerPanel.add(transactionsTitleLabel, BorderLayout.WEST);
        
        // Controls (Search + Issue/Return)
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setBackground(UIManager.getColor("Panel.background"));
        
        // Local search bar removed, using header search bar
        
        JButton btnIssueBook = new JButton("Pinjamkan Buku");
        btnIssueBook.putClientProperty(FlatClientProperties.STYLE, "background: $Component.accentColor; foreground: #ffffff; arc: 10");
        
        JButton btnReturnBook = new JButton("Kembalikan Buku");
        btnReturnBook.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        btnReturnBook.setEnabled(false);
        
        controlsPanel.add(btnIssueBook);
        controlsPanel.add(btnReturnBook);
        headerPanel.add(controlsPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // --- TABLE (CENTER) ---
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
        
        transactionsTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = transactionsTable.getSelectedRow();
            if (selectedRow != -1) {
                String status = (String) transactionsTableModel.getValueAt(selectedRow, 8);
                btnReturnBook.setEnabled("Issued".equals(status));
            } else {
                btnReturnBook.setEnabled(false);
            }
        });
        
        // Action Listeners
        btnIssueBook.addActionListener(e -> showIssueBookDialog());
        
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
        
        JScrollPane scroll = new JScrollPane(transactionsTable);
        add(scroll, BorderLayout.CENTER);
        
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

    private void showIssueBookDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Pinjamkan Buku (Peminjaman)", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1.0;

        JTextField txtMemberCode = UIUtils.createFormTextField("Masukkan Kode Anggota (misal: MEM-0001)");
        JLabel lblMemberName = new JLabel(" ");
        lblMemberName.putClientProperty(FlatClientProperties.STYLE, "font: -1");
        // We will store the validated userId here
        final int[] validatedUserId = new int[]{-1};

        JTextField txtBookId = UIUtils.createFormTextField("Masukkan ID Buku (angka)");
        JLabel lblBookTitle = new JLabel(" ");
        lblBookTitle.putClientProperty(FlatClientProperties.STYLE, "font: -1");

        JSpinner spinDuration = new JSpinner(new SpinnerNumberModel(7, 1, 90, 1));
        spinDuration.putClientProperty(FlatClientProperties.STYLE, "margin: 2, 5, 2, 5; arc: 8");

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

        int row = 0;
        UIUtils.addFormRow(formPanel, gbc, "Kode Anggota*", txtMemberCode, row++);
        UIUtils.addFormRow(formPanel, gbc, "", lblMemberName, row++);
        UIUtils.addFormRow(formPanel, gbc, "ID Buku*", txtBookId, row++);
        UIUtils.addFormRow(formPanel, gbc, "", lblBookTitle, row++);
        UIUtils.addFormRow(formPanel, gbc, "Durasi Peminjaman (Hari)*", spinDuration, row++);

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setBackground(UIManager.getColor("Panel.background"));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));

        JButton btnCancel = new JButton("Batal");
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = new JButton("Pinjamkan Buku");
        btnSave.putClientProperty(FlatClientProperties.STYLE, "background: $Component.accentColor; foreground: #ffffff; font: bold");
        btnSave.addActionListener(e -> {
            String memberText = txtMemberCode.getText().trim();
            String bookText = txtBookId.getText().trim();

            if (memberText.isEmpty() || bookText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Silakan isi semua kolom.", "Kesalahan Validasi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!lblMemberName.getText().startsWith("✔") || validatedUserId[0] == -1) {
                JOptionPane.showMessageDialog(dialog, "Silakan masukkan anggota yang valid dan aktif.", "Kesalahan Validasi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!lblBookTitle.getText().startsWith("✔")) {
                JOptionPane.showMessageDialog(dialog, "Silakan masukkan buku yang valid dan tersedia.", "Kesalahan Validasi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int userId = validatedUserId[0];
            int bookId = Integer.parseInt(bookText);
            int duration = (Integer) spinDuration.getValue();

            SwingWorker<Boolean, Void> issueWorker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return transactionService.issueBook(userId, bookId, duration);
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            dialog.dispose();
                            loadTransactionsData(currentSearchQuery);
                        } else {
                            JOptionPane.showMessageDialog(dialog, "Gagal meminjamkan buku. Buku mungkin sedang kosong.", "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(dialog, "Terjadi kesalahan: " + ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            issueWorker.execute();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
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
}
