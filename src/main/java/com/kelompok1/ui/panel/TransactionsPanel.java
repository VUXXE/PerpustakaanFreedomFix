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
    private JButton btnMarkLost;
    private JButton btnIssueBook;
    private JButton btnPrintReceipt;
    private JLabel lblError;
    private final int[] validatedUserId = new int[]{-1};
    private int selectedTransactionId = -1;

    // Beautiful Split Preview Labels
    private JLabel lblPreviewMemberName;
    private JLabel lblPreviewMemberCode;
    private JLabel lblPreviewMemberContact;
    private JLabel lblPreviewMemberAddress;
    private JLabel lblPreviewBookTitle;
    private JLabel lblPreviewBookAuthor;
    private JLabel lblPreviewBookMeta;
    private JLabel lblPreviewBookStock;

    // Debounce Timers for autocomplete search
    private javax.swing.Timer memberDebounceTimer;
    private javax.swing.Timer bookDebounceTimer;

    public TransactionsPanel() {
        this.transactionService = new TransactionService();
        this.userService = new UserService();
        this.bookService = new BookService();
        this.fineService = new FineService();
        this.settingsService = new SettingsService();
        
        this.memberDebounceTimer = new javax.swing.Timer(150, null);
        this.memberDebounceTimer.setRepeats(false);
        this.bookDebounceTimer = new javax.swing.Timer(150, null);
        this.bookDebounceTimer.setRepeats(false);
        
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
        txtMemberCode = UIUtils.createFormTextField("Masukkan Kode/Nama Anggota...");
        txtBookId = UIUtils.createFormTextField("Masukkan ID/Judul Buku...");

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

        // Load default borrow duration setting directly
        try {
            spinDuration.setValue(Integer.parseInt(settingsService.getSetting("borrow_duration", "7")));
        } catch (Exception e) {
            spinDuration.setValue(7);
        }

        // Suggestions Popup for Member Name Search
        JPopupMenu memberSuggestPopup = new JPopupMenu();
        JList<User> memberList = new JList<>();
        memberList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberList.setFocusable(true);
        
        memberList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) {
                    User u = (User) value;
                    lbl.setText(u.getFullName() + " (" + u.getMemberCode() + ")");
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                return lbl;
            }
        });
        
        JScrollPane memberScroll = new JScrollPane(memberList) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(txtMemberCode.getWidth(), Math.min(180, memberList.getPreferredSize().height + 4));
            }
        };
        memberScroll.setBorder(null);
        memberSuggestPopup.add(memberScroll);

        memberList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                User selected = memberList.getSelectedValue();
                if (selected != null) {
                    txtMemberCode.setText(selected.getMemberCode());
                    memberSuggestPopup.setVisible(false);
                    txtBookId.requestFocusInWindow();
                }
            }
        });

        memberList.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    User selected = memberList.getSelectedValue();
                    if (selected != null) {
                        txtMemberCode.setText(selected.getMemberCode());
                        memberSuggestPopup.setVisible(false);
                        txtBookId.requestFocusInWindow();
                    }
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    memberSuggestPopup.setVisible(false);
                    txtMemberCode.requestFocusInWindow();
                }
            }
        });

        txtMemberCode.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE || keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                    return;
                }
                if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
                    if (memberSuggestPopup.isVisible() && memberList.getModel().getSize() > 0) {
                        memberList.setSelectedIndex(0);
                        memberList.requestFocusInWindow();
                    }
                    return;
                }

                String text = txtMemberCode.getText().trim();
                if (text.length() >= 2) {
                    for (java.awt.event.ActionListener al : memberDebounceTimer.getActionListeners()) {
                        memberDebounceTimer.removeActionListener(al);
                    }
                    memberDebounceTimer.addActionListener(ae -> {
                        new SwingWorker<List<User>, Void>() {
                            @Override
                            protected List<User> doInBackground() throws Exception {
                                return userService.searchUsers(text, 1, 8);
                            }

                            @Override
                            protected void done() {
                                try {
                                    List<User> matches = get();
                                    if (matches.size() == 1 && matches.get(0).getMemberCode().equalsIgnoreCase(text)) {
                                        memberSuggestPopup.setVisible(false);
                                        return;
                                    }

                                    if (!matches.isEmpty()) {
                                        DefaultListModel<User> model = new DefaultListModel<>();
                                        for (User u : matches) {
                                            model.addElement(u);
                                        }
                                        memberList.setModel(model);
                                        if (txtMemberCode.isShowing()) {
                                            memberSuggestPopup.show(txtMemberCode, 0, txtMemberCode.getHeight());
                                        }
                                    } else {
                                        memberSuggestPopup.setVisible(false);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }.execute();
                    });
                    memberDebounceTimer.restart();
                } else {
                    memberSuggestPopup.setVisible(false);
                }
            }
        });

        // Suggestions Popup for Book Search (by Title/Author/ID)
        JPopupMenu bookSuggestPopup = new JPopupMenu();
        JList<Book> bookList = new JList<>();
        bookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookList.setFocusable(true);
        
        bookList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Book) {
                    Book b = (Book) value;
                    lbl.setText(b.getTitle() + " (ID: " + b.getBookId() + ")");
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                return lbl;
            }
        });
        
        JScrollPane bookScroll = new JScrollPane(bookList) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(txtBookId.getWidth(), Math.min(180, bookList.getPreferredSize().height + 4));
            }
        };
        bookScroll.setBorder(null);
        bookSuggestPopup.add(bookScroll);

        bookList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                Book selected = bookList.getSelectedValue();
                if (selected != null) {
                    txtBookId.setText(String.valueOf(selected.getBookId()));
                    bookSuggestPopup.setVisible(false);
                    spinDuration.requestFocusInWindow();
                }
            }
        });

        bookList.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    Book selected = bookList.getSelectedValue();
                    if (selected != null) {
                        txtBookId.setText(String.valueOf(selected.getBookId()));
                        bookSuggestPopup.setVisible(false);
                        spinDuration.requestFocusInWindow();
                    }
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    bookSuggestPopup.setVisible(false);
                    txtBookId.requestFocusInWindow();
                }
            }
        });

        txtBookId.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE || keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                    return;
                }
                if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
                    if (bookSuggestPopup.isVisible() && bookList.getModel().getSize() > 0) {
                        bookList.setSelectedIndex(0);
                        bookList.requestFocusInWindow();
                    }
                    return;
                }

                String text = txtBookId.getText().trim();
                if (text.length() >= 2) {
                    for (java.awt.event.ActionListener al : bookDebounceTimer.getActionListeners()) {
                        bookDebounceTimer.removeActionListener(al);
                    }
                    bookDebounceTimer.addActionListener(ae -> {
                        new SwingWorker<List<Book>, Void>() {
                            @Override
                            protected List<Book> doInBackground() throws Exception {
                                return bookService.searchBooks(text, 1, 8);
                            }

                            @Override
                            protected void done() {
                                try {
                                    List<Book> matches = get();
                                    if (matches.size() == 1 && String.valueOf(matches.get(0).getBookId()).equalsIgnoreCase(text)) {
                                        bookSuggestPopup.setVisible(false);
                                        return;
                                    }

                                    if (!matches.isEmpty()) {
                                        DefaultListModel<Book> model = new DefaultListModel<>();
                                        for (Book b : matches) {
                                            model.addElement(b);
                                        }
                                        bookList.setModel(model);
                                        if (txtBookId.isShowing()) {
                                            bookSuggestPopup.show(txtBookId, 0, txtBookId.getHeight());
                                        }
                                    } else {
                                        bookSuggestPopup.setVisible(false);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }.execute();
                    });
                    bookDebounceTimer.restart();
                } else {
                    bookSuggestPopup.setVisible(false);
                }
            }
        });

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

        // Action Listeners for Keyboard Navigation UX
        txtMemberCode.addActionListener(e -> txtBookId.requestFocusInWindow());
        txtBookId.addActionListener(e -> spinDuration.requestFocusInWindow());
        
        JSpinner.DefaultEditor spinEditor = (JSpinner.DefaultEditor) spinDuration.getEditor();
        spinEditor.getTextField().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    if (btnIssueBook.isEnabled()) {
                        btnIssueBook.doClick();
                    }
                }
            }
        });

        // Header Title Panel
        lblFormTitle = new JLabel("Form Transaksi Peminjaman");
        lblFormTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +4");
        lblFormTitle.setForeground(DesignSystem.ON_SURFACE);

        lblFormSubtitle = new JLabel("(Lengkapi data anggota dan buku di bawah.)");
        lblFormSubtitle.setFont(DesignSystem.bodyFont(10f, Font.PLAIN));
        lblFormSubtitle.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(lblFormTitle);
        titlePanel.add(Box.createHorizontalStrut(8));
        titlePanel.add(lblFormSubtitle);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        northWrapper.add(titlePanel);

        // Split Layout Panel (Left Card and Right Preview Cards)
        JPanel splitFormPanel = new JPanel(new GridBagLayout());
        splitFormPanel.setOpaque(false);
        splitFormPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, DesignSystem.OUTLINE_VARIANT),
            BorderFactory.createEmptyBorder(0, 0, 15, 0)
        ));

        GridBagConstraints splitGbc = new GridBagConstraints();
        splitGbc.fill = GridBagConstraints.BOTH;
        splitGbc.weighty = 1.0;

        // ─── LEFT COLUMN: FORM INPUT CARD ───
        JPanel leftCardPanel = new JPanel(new GridBagLayout());
        leftCardPanel.setBackground(UIManager.getColor("Component.background"));
        leftCardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DesignSystem.OUTLINE_VARIANT, 1, true),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));
        leftCardPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        GridBagConstraints lGbc = new GridBagConstraints();
        lGbc.fill = GridBagConstraints.HORIZONTAL;
        lGbc.gridx = 0;
        lGbc.weightx = 1.0;
        lGbc.insets = new Insets(4, 0, 4, 0);

        int leftRow = 0;

        // Label Anggota
        lGbc.gridy = leftRow++;
        JLabel lblMem = new JLabel("Kode/Nama Anggota*");
        lblMem.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblMem.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        leftCardPanel.add(lblMem, lGbc);

        // Input Anggota
        lGbc.gridy = leftRow++;
        lGbc.insets = new Insets(0, 0, 10, 0);
        leftCardPanel.add(txtMemberCode, lGbc);

        // Label Buku
        lGbc.gridy = leftRow++;
        lGbc.insets = new Insets(4, 0, 4, 0);
        JLabel lblBkB = new JLabel("ID/Judul Buku*");
        lblBkB.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblBkB.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        leftCardPanel.add(lblBkB, lGbc);

        // Input Buku
        lGbc.gridy = leftRow++;
        lGbc.insets = new Insets(0, 0, 10, 0);
        leftCardPanel.add(txtBookId, lGbc);

        // Label Durasi
        lGbc.gridy = leftRow++;
        lGbc.insets = new Insets(4, 0, 4, 0);
        JLabel lblDur = new JLabel("Durasi Peminjaman (Hari)*");
        lblDur.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblDur.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        leftCardPanel.add(lblDur, lGbc);

        // Input Durasi
        lGbc.gridy = leftRow++;
        lGbc.insets = new Insets(0, 0, 16, 0);
        leftCardPanel.add(spinDuration, lGbc);

        // Buttons
        lGbc.gridy = leftRow++;
        lGbc.insets = new Insets(8, 0, 4, 0);
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

        btnMarkLost = new JButton("Buku Hilang");
        DesignSystem.applySecondaryButton(btnMarkLost);
        btnMarkLost.setForeground(DesignSystem.ERROR); // Red text to indicate danger
        btnMarkLost.setEnabled(false);
        buttonPanel.add(btnMarkLost);

        JButton btnCancel = new JButton("Batal");
        DesignSystem.applySecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> clearForm());
        buttonPanel.add(btnCancel);

        btnIssueBook = new JButton("Pinjamkan");
        DesignSystem.applyPrimaryButton(btnIssueBook);
        btnIssueBook.addActionListener(e -> saveTransaction());
        buttonPanel.add(btnIssueBook);

        leftCardPanel.add(buttonPanel, lGbc);

        // Error Label
        lGbc.gridy = leftRow++;
        lGbc.insets = new Insets(6, 0, 0, 0);
        lblError = new JLabel(" ");
        lblError.putClientProperty(FlatClientProperties.STYLE, "foreground: $Component.error.focusedBorderColor; font: bold -1");
        leftCardPanel.add(lblError, lGbc);

        // Add leftCardPanel to splitFormPanel
        splitGbc.gridx = 0;
        splitGbc.weightx = 0.55;
        splitGbc.insets = new Insets(0, 0, 0, 15);
        splitFormPanel.add(leftCardPanel, splitGbc);

        // ─── RIGHT COLUMN: PREVIEW CARDS STACK ───
        JPanel rightPreviewPanel = new JPanel(new GridBagLayout());
        rightPreviewPanel.setOpaque(false);

        lblPreviewMemberName = new JLabel("Belum Memilih Anggota");
        lblPreviewMemberCode = new JLabel("-Masukkan nama/kode anggota-");
        lblPreviewMemberContact = new JLabel(" ");
        lblPreviewMemberAddress = new JLabel(" ");

        lblPreviewBookTitle = new JLabel("Belum Memilih Buku");
        lblPreviewBookAuthor = new JLabel("-Masukkan judul/ID buku-");
        lblPreviewBookMeta = new JLabel(" ");
        lblPreviewBookStock = new JLabel(" ");

        JPanel cardMemberPreview = createPreviewCard("DETAIL ANGGOTA", lblPreviewMemberName, lblPreviewMemberCode, 
            new JLabel[]{ lblPreviewMemberContact, lblPreviewMemberAddress }, lblMemberName);
        JPanel cardBookPreview = createPreviewCard("DETAIL BUKU", lblPreviewBookTitle, lblPreviewBookAuthor, 
            new JLabel[]{ lblPreviewBookMeta, lblPreviewBookStock }, lblBookTitle);

        GridBagConstraints rGbc = new GridBagConstraints();
        rGbc.fill = GridBagConstraints.BOTH;
        rGbc.gridx = 0;
        rGbc.weightx = 1.0;
        rGbc.weighty = 0.5;

        rGbc.gridy = 0;
        rGbc.insets = new Insets(0, 0, 12, 0);
        rightPreviewPanel.add(cardMemberPreview, rGbc);

        rGbc.gridy = 1;
        rGbc.insets = new Insets(0, 0, 0, 0);
        rightPreviewPanel.add(cardBookPreview, rGbc);

        // Add rightPreviewPanel to splitFormPanel
        splitGbc.gridx = 1;
        splitGbc.weightx = 0.45;
        splitGbc.insets = new Insets(0, 0, 0, 0);
        splitFormPanel.add(rightPreviewPanel, splitGbc);

        northWrapper.add(splitFormPanel);
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
                btnReturnBook.setEnabled("Dipinjam".equals(status));
                btnMarkLost.setEnabled("Dipinjam".equals(status));
                selectedTransactionId = (Integer) transactionsTableModel.getValueAt(selectedRow, 0);
                btnPrintReceipt.setEnabled(true);

                // Populate inline fields
                txtMemberCode.setText((String) transactionsTableModel.getValueAt(selectedRow, 3));
                txtBookId.setText(String.valueOf(transactionsTableModel.getValueAt(selectedRow, 2)));
            } else {
                btnReturnBook.setEnabled(false);
                btnMarkLost.setEnabled(false);
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
                try {
                    double fineAmount = 0.0;
                    boolean isOverdue = false;
                    java.time.LocalDate today = java.time.LocalDate.now();
                    java.time.LocalDate dueDate = java.time.LocalDate.parse(dueDateStr);
                    if (today.isAfter(dueDate)) {
                        long days = java.time.temporal.ChronoUnit.DAYS.between(dueDate, today);
                        double fineRate = 5000.0;
                        try {
                            fineRate = Double.parseDouble(settingsService.getSetting("fine_rate", "5000"));
                        } catch (Exception ex) {
                            // ignore fallback
                        }
                        fineAmount = days * fineRate;
                        isOverdue = true;
                    }

                    if (isOverdue) {
                        fineService.assessFines();
                    }

                    boolean success = transactionService.returnBook(txId, bookId);
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
                } finally {
                    btnReturnBook.setEnabled(true);
                    btnMarkLost.setEnabled(true);
                }
            }
        });

        btnMarkLost.addActionListener(e -> {
            int selectedRow = transactionsTable.getSelectedRow();
            if (selectedRow == -1) return;

            int txId = (Integer) transactionsTableModel.getValueAt(selectedRow, 0);
            int bookId = (Integer) transactionsTableModel.getValueAt(selectedRow, 2);

            String input = JOptionPane.showInputDialog(
                this,
                "Masukkan jumlah denda untuk Buku Hilang (Rp):",
                "Konfirmasi Buku Hilang",
                JOptionPane.WARNING_MESSAGE
            );

            if (input != null && !input.trim().isEmpty()) {
                try {
                    double fineAmount = Double.parseDouble(input.trim().replace(".", "").replace(",", ""));
                    if (fineAmount < 0) {
                        JOptionPane.showMessageDialog(this, "Denda tidak boleh negatif.", "Input Tidak Valid", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    btnMarkLost.setEnabled(false);
                    boolean success = transactionService.markBookAsLost(txId, bookId, fineAmount);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Buku berhasil ditandai sebagai Hilang.\nDenda sebesar Rp " + String.format("%,.0f", fineAmount) + " telah ditambahkan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        loadTransactionsData(currentSearchQuery);
                        clearForm();
                    } else {
                        JOptionPane.showMessageDialog(this, "Gagal memproses buku hilang.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Masukkan angka yang valid.", "Input Tidak Valid", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnMarkLost.setEnabled(true);
                }
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

    private JPanel createPreviewCard(String headerText, JLabel titleLabel, JLabel subtitleLabel, JLabel[] extraLabels, JLabel statusLabel) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIManager.getColor("Component.background"));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DesignSystem.OUTLINE_VARIANT, 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        JLabel header = new JLabel(headerText);
        header.setFont(DesignSystem.bodyFont(9f, Font.BOLD));
        header.setForeground(UIManager.getColor("Label.disabledForeground"));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(header, gbc);

        titleLabel.setFont(DesignSystem.bodyFont(13f, Font.BOLD));
        titleLabel.setForeground(DesignSystem.ON_SURFACE);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(titleLabel, gbc);

        subtitleLabel.setFont(DesignSystem.bodyFont(11f, Font.PLAIN));
        subtitleLabel.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(subtitleLabel, gbc);

        int gridy = 3;
        if (extraLabels != null) {
            for (JLabel lbl : extraLabels) {
                lbl.setFont(DesignSystem.bodyFont(10.5f, Font.PLAIN));
                lbl.setForeground(DesignSystem.ON_SURFACE_VARIANT);
                gbc.gridy = gridy++;
                gbc.insets = new Insets(0, 0, 4, 0);
                card.add(lbl, gbc);
            }
        }

        statusLabel.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        gbc.gridy = gridy;
        gbc.insets = new Insets(6, 0, 0, 0);
        card.add(statusLabel, gbc);

        return card;
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

        try {
            List<Transaction> txs;
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                txs = transactionService.getAllTransactions(currentPage, pageSize);
            } else {
                txs = transactionService.searchTransactions(searchQuery.trim(), currentPage, pageSize);
            }
            for (Transaction tx : txs) {
                String statusIndo = tx.getStatus();
                if ("Issued".equalsIgnoreCase(statusIndo)) statusIndo = "Dipinjam";
                else if ("Returned".equalsIgnoreCase(statusIndo)) statusIndo = "Dikembalikan";
                else if ("Lost".equalsIgnoreCase(statusIndo)) statusIndo = "Hilang";

                transactionsTableModel.addRow(new Object[]{
                    tx.getTransactionId(), tx.getUserId(), tx.getBookId(),
                    tx.getMemberCode(), tx.getBookTitle(),
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
            transactionsTitleLabel.setText("Riwayat Transaksi (" + transactionsTableModel.getRowCount() + " data ditampilkan)");
        } catch (Exception e) {
            e.printStackTrace();
            transactionsTitleLabel.setText("Riwayat Transaksi (Gagal memuat data)");
        }
    }

    private void clearForm() {
        txtMemberCode.setText("");
        txtMemberCode.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtBookId.setText("");
        txtBookId.putClientProperty(FlatClientProperties.OUTLINE, null);

        lblMemberName.setText(" ");
        lblBookTitle.setText(" ");
        lblError.setText(" ");

        if (lblPreviewMemberName != null) {
            lblPreviewMemberName.setText("Belum Memilih Anggota");
            lblPreviewMemberCode.setText("-Masukkan nama/kode anggota-");
            lblPreviewMemberContact.setText(" ");
            lblPreviewMemberAddress.setText(" ");
        }
        if (lblPreviewBookTitle != null) {
            lblPreviewBookTitle.setText("Belum Memilih Buku");
            lblPreviewBookAuthor.setText("-Masukkan judul/ID buku-");
            lblPreviewBookMeta.setText(" ");
            lblPreviewBookStock.setText(" ");
        }

        validatedUserId[0] = -1;
        selectedTransactionId = -1;

        btnReturnBook.setEnabled(false);
        btnMarkLost.setEnabled(false);
        btnPrintReceipt.setEnabled(false);
        btnIssueBook.setEnabled(true);
        transactionsTable.clearSelection();

        try {
            spinDuration.setValue(Integer.parseInt(settingsService.getSetting("borrow_duration", "7")));
        } catch (Exception e) {
            spinDuration.setValue(7);
        }
        
        txtMemberCode.requestFocusInWindow();
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

        try {
            int newTxId = transactionService.issueBook(userId, bookId, duration);
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
        } finally {
            btnIssueBook.setEnabled(true);
        }
    }

    private void verifyMember(String codeText, JLabel outputLabel, int[] validatedUserId) {
        validatedUserId[0] = -1;
        if (codeText.isEmpty()) {
            outputLabel.setText(" ");
            outputLabel.setForeground(UIManager.getColor("Label.foreground"));
            if (lblPreviewMemberName != null) {
                lblPreviewMemberName.setText("Belum Memilih Anggota");
                lblPreviewMemberCode.setText("-Masukkan nama/kode anggota-");
                lblPreviewMemberContact.setText(" ");
                lblPreviewMemberAddress.setText(" ");
            }
            return;
        }
        
        new SwingWorker<User, Void>() {
            private int activeLoans = 0;
            private int maxLimit = 3;

            @Override
            protected User doInBackground() throws Exception {
                User u = userService.getUserByMemberCode(codeText);
                if (u != null) {
                    activeLoans = transactionService.getActiveLoansCount(u.getUserId());
                    maxLimit = Integer.parseInt(settingsService.getSetting("max_borrow_limit", "3"));
                }
                return u;
            }

            @Override
            protected void done() {
                try {
                    User u = get();
                    if (!txtMemberCode.getText().trim().equals(codeText)) {
                        return;
                    }
                    if (u != null) {
                        String roleIndo = "Admin".equalsIgnoreCase(u.getRole()) ? "Admin" : "Anggota";
                        
                        if (lblPreviewMemberName != null) {
                            lblPreviewMemberName.setText(u.getFullName());
                            lblPreviewMemberCode.setText(u.getMemberCode() + " • " + roleIndo);
                            
                            String emailStr = (u.getEmail() == null || u.getEmail().isEmpty()) ? "-" : u.getEmail();
                            String phoneStr = (u.getPhone() == null || u.getPhone().isEmpty()) ? "-" : u.getPhone();
                            lblPreviewMemberContact.setText("✉ " + emailStr + "  •  📞 " + phoneStr);
                            
                            String addrStr = (u.getAddress() == null || u.getAddress().isEmpty()) ? "-" : u.getAddress();
                            lblPreviewMemberAddress.setText("🏠 Alamat: " + addrStr);
                        }
                        
                        if ("Suspended".equals(u.getStatus())) {
                            outputLabel.setText("❌ DITANGGUHKAN - TIDAK BISA MEMINJAM");
                            outputLabel.setForeground(new Color(230, 80, 80));
                        } else if (activeLoans >= maxLimit) {
                            outputLabel.setText("⚠ LIMIT TERCAPAI (" + activeLoans + "/" + maxLimit + " buku)");
                            outputLabel.setForeground(new Color(230, 80, 80));
                        } else {
                            outputLabel.setText("✔ AKTIF - Pinjam: " + activeLoans + "/" + maxLimit);
                            outputLabel.setForeground(new Color(101, 183, 108));
                            validatedUserId[0] = u.getUserId();
                        }
                    } else {
                        outputLabel.setText("❌ Anggota tidak ditemukan");
                        outputLabel.setForeground(new Color(230, 80, 80));
                        if (lblPreviewMemberName != null) {
                            lblPreviewMemberName.setText("Anggota Tidak Ditemukan");
                            lblPreviewMemberCode.setText("-");
                            lblPreviewMemberContact.setText(" ");
                            lblPreviewMemberAddress.setText(" ");
                        }
                    }
                } catch (Exception ex) {
                    outputLabel.setText("❌ Kesalahan memeriksa anggota");
                    outputLabel.setForeground(new Color(230, 80, 80));
                }
            }
        }.execute();
    }

    private void verifyBook(String idText, JLabel outputLabel) {
        if (idText.isEmpty()) {
            outputLabel.setText(" ");
            outputLabel.setForeground(UIManager.getColor("Label.foreground"));
            if (lblPreviewBookTitle != null) {
                lblPreviewBookTitle.setText("Belum Memilih Buku");
                lblPreviewBookAuthor.setText("-Masukkan judul/ID buku-");
                lblPreviewBookMeta.setText(" ");
                lblPreviewBookStock.setText(" ");
            }
            return;
        }
        
        int bookId;
        try {
            bookId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            outputLabel.setText("❌ Format ID tidak valid");
            outputLabel.setForeground(new Color(230, 80, 80));
            if (lblPreviewBookTitle != null) {
                lblPreviewBookTitle.setText("Format ID Tidak Valid");
                lblPreviewBookAuthor.setText("Harus berupa angka");
                lblPreviewBookMeta.setText(" ");
                lblPreviewBookStock.setText(" ");
            }
            return;
        }

        new SwingWorker<Book, Void>() {
            @Override
            protected Book doInBackground() throws Exception {
                return bookService.getBookById(bookId);
            }

            @Override
            protected void done() {
                try {
                    Book b = get();
                    if (!txtBookId.getText().trim().equals(idText)) {
                        return;
                    }
                    if (b != null) {
                        if (lblPreviewBookTitle != null) {
                            lblPreviewBookTitle.setText(b.getTitle());
                            lblPreviewBookAuthor.setText("Penulis: " + (b.getAuthor().isEmpty() ? "-" : b.getAuthor()) + "  •  Penerbit: " + (b.getPublisher().isEmpty() ? "-" : b.getPublisher()));
                            
                            String callNum = (b.getCallNumber().isEmpty()) ? "-" : b.getCallNumber();
                            String classif = (b.getClassification().isEmpty()) ? "-" : b.getClassification();
                            lblPreviewBookMeta.setText("Call No: " + callNum + "  •  Klasifikasi: " + classif);
                            
                            String isbnStr = (b.getIsbn().isEmpty()) ? "-" : b.getIsbn();
                            String editStr = (b.getEdition().isEmpty()) ? "-" : b.getEdition();
                            lblPreviewBookStock.setText("ISBN: " + isbnStr + "  •  Edisi: " + editStr);
                        }
                        
                        if (b.getAvailableCopies() > 0) {
                            outputLabel.setText("✔ TERSEDIA (" + b.getAvailableCopies() + " salinan)");
                            outputLabel.setForeground(new Color(101, 183, 108));
                        } else {
                            outputLabel.setText("❌ KOSONG - TIDAK BISA DIPINJAM");
                            outputLabel.setForeground(new Color(230, 80, 80));
                        }
                    } else {
                        outputLabel.setText("❌ Buku tidak ditemukan");
                        outputLabel.setForeground(new Color(230, 80, 80));
                        if (lblPreviewBookTitle != null) {
                            lblPreviewBookTitle.setText("Buku Tidak Ditemukan");
                            lblPreviewBookAuthor.setText("-");
                            lblPreviewBookMeta.setText(" ");
                            lblPreviewBookStock.setText(" ");
                        }
                    }
                } catch (Exception ex) {
                    outputLabel.setText("❌ Kesalahan memeriksa buku");
                    outputLabel.setForeground(new Color(230, 80, 80));
                }
            }
        }.execute();
    }

    private void printReceipt(int transactionId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("transaction_id", transactionId);
            ReportGenerator.showReportViewer("/reports/transaction_receipt.jrxml", params);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mencetak nota: " + ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }
}
