package com.kelompok1.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.model.Models.User;
import com.kelompok1.model.Models.Book;
import com.kelompok1.model.Models.Transaction;
import com.kelompok1.model.Models.Fine;
import com.kelompok1.service.Services.BookService;
import com.kelompok1.service.Services.TransactionService;
import com.kelompok1.service.Services.FineService;
import com.kelompok1.ui.panel.UIUtils;
import com.kelompok1.util.DesignSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MemberDashboard extends JFrame {

    private User loggedInUser;
    
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private MemberSidebarPanel sidebar;

    private MemberHomePanel homePanel;
    private SearchCatalogPanel catalogPanel;
    private BorrowedBooksPanel borrowedPanel;
    private FinesLedgerPanel finesPanel;

    public MemberDashboard(User user) {
        this.loggedInUser = user;
        setTitle("Perpustakaan Freedom — Anggota");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(DesignSystem.SURFACE);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. Sidebar
        sidebar = new MemberSidebarPanel(loggedInUser, new MemberSidebarPanel.SidebarListener() {
            @Override
            public void onTabSelected(String tabName) {
                MemberDashboard.this.onTabSelected(tabName);
            }

            @Override
            public void onLogout() {
                MemberDashboard.this.onLogout();
            }
        });
        add(sidebar, BorderLayout.WEST);

        // 2. Main Area Container
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(DesignSystem.SURFACE);

        // Top Bar (Theme Toggle)
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        topBar.setBackground(DesignSystem.SURFACE);
        JButton btnTheme = new JButton(DesignSystem.getToggleLabel());
        btnTheme.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btnTheme.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 4, 12, 4, 12");
        btnTheme.addActionListener(e -> {
            DesignSystem.toggleTheme(this);
            btnTheme.setText(DesignSystem.getToggleLabel());
        });
        topBar.add(btnTheme);
        mainArea.add(topBar, BorderLayout.NORTH);

        // Card Content
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(DesignSystem.SURFACE);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 16, 16));

        // Initialize Panels
        homePanel = new MemberHomePanel(loggedInUser);
        catalogPanel = new SearchCatalogPanel();
        borrowedPanel = new BorrowedBooksPanel(loggedInUser.getUserId());
        finesPanel = new FinesLedgerPanel(loggedInUser.getUserId());

        mainContentPanel.add(homePanel, "Dashboard");
        mainContentPanel.add(catalogPanel, "Catalog");
        mainContentPanel.add(borrowedPanel, "Borrowed");
        mainContentPanel.add(finesPanel, "Fines");

        mainArea.add(mainContentPanel, BorderLayout.CENTER);
        add(mainArea, BorderLayout.CENTER);

        // Show Dashboard by default
        onTabSelected("Dashboard");
    }

    public void onTabSelected(String tabName) {
        cardLayout.show(mainContentPanel, tabName);
        
        // Refresh data when tabs are opened
        if (tabName.equals("Dashboard")) {
            homePanel.refreshData();
        } else if (tabName.equals("Borrowed")) {
            borrowedPanel.refreshTable();
        } else if (tabName.equals("Fines")) {
            finesPanel.refreshTable();
        }
    }

    public void onLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin keluar?", "Konfirmasi Keluar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginView().setVisible(true);
            this.dispose();
        }
    }

    // ─── Nested Sidebar Panel Class ──────────────────────────────────────────
    
    private static class MemberSidebarPanel extends JPanel {
        
        public interface SidebarListener {
            void onTabSelected(String tabName);
            void onLogout();
        }

        private final SidebarListener listener;
        private final SidebarNavButton btnDashboard;
        private final SidebarNavButton btnCatalog;
        private final SidebarNavButton btnBorrowed;
        private final SidebarNavButton btnFines;

        public MemberSidebarPanel(User loggedInUser, SidebarListener listener) {
            this.listener = listener;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setPreferredSize(new Dimension(240, 800));
            setBackground(DesignSystem.SURFACE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, DesignSystem.OUTLINE_VARIANT),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));

            // Brand logo text
            JLabel lblHeader = new JLabel("Perpustakaan Freedom");
            lblHeader.setFont(DesignSystem.displayFont(15f, Font.BOLD));
            lblHeader.setForeground(DesignSystem.PRIMARY);
            lblHeader.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));
            lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(lblHeader);

            // Section divider
            JSeparator sep = new JSeparator();
            sep.setForeground(DesignSystem.OUTLINE_VARIANT);
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            sep.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(sep);
            add(Box.createVerticalStrut(16));

            // Nav buttons
            ButtonGroup group = new ButtonGroup();
            btnDashboard = createNavButton("Beranda",       "Dashboard");
            btnCatalog   = createNavButton("Katalog Buku",  "Catalog");
            btnBorrowed  = createNavButton("Pinjaman Saya", "Borrowed");
            btnFines     = createNavButton("Tagihan Denda", "Fines");

            group.add(btnDashboard);
            group.add(btnCatalog);
            group.add(btnBorrowed);
            group.add(btnFines);
            btnDashboard.setSelected(true);

            add(btnDashboard);
            add(Box.createVerticalStrut(8));
            add(btnCatalog);
            add(Box.createVerticalStrut(8));
            add(btnBorrowed);
            add(Box.createVerticalStrut(8));
            add(btnFines);
            add(Box.createVerticalGlue());

            // Logout button
            JButton btnLogout = new JButton("Keluar");
            btnLogout.setFont(DesignSystem.bodyFont(14f, Font.BOLD));
            btnLogout.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
            btnLogout.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; " +
                "margin: 12, 20, 12, 20; " +
                "focusWidth: 0; innerFocusWidth: 0; " +
                "background: null; borderWidth: 0; " +
                "foreground: #ba1a1a; " +
                "hoverBackground: #fdebee; " +
                "font: bold");
            btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            btnLogout.setHorizontalAlignment(SwingConstants.LEFT);
            btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnLogout.addActionListener(e -> {
                if (listener != null) {
                    listener.onLogout();
                }
            });
            add(btnLogout);
            add(Box.createVerticalStrut(10));
        }

        private SidebarNavButton createNavButton(String label, String tabName) {
            SidebarNavButton btn = new SidebarNavButton(label);
            btn.addActionListener(e -> notifyTabSelected(tabName));
            return btn;
        }

        private void notifyTabSelected(String tabName) {
            if (listener != null) {
                listener.onTabSelected(tabName);
            }
        }

        public void setSelectedTab(String tabName) {
            switch (tabName) {
                case "Dashboard" -> btnDashboard.setSelected(true);
                case "Catalog"   -> btnCatalog.setSelected(true);
                case "Borrowed"  -> btnBorrowed.setSelected(true);
                case "Fines"     -> btnFines.setSelected(true);
            }
        }

        private static class SidebarNavButton extends JToggleButton {
            SidebarNavButton(String label) {
                super(label);
                setFont(DesignSystem.bodyFont(14f, Font.PLAIN));
                setForeground(new Color(0x4a4a4a));
                setHorizontalAlignment(SwingConstants.LEFT);
                setAlignmentX(Component.LEFT_ALIGNMENT);
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setOpaque(true);
                setBorderPainted(false);
                setFocusPainted(false);
                setContentAreaFilled(true);
                putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
                putClientProperty(FlatClientProperties.STYLE,
                    "arc: 12; " +
                    "margin: 12, 20, 12, 20; " +
                    "selectedBackground: #86000d; " +
                    "selectedForeground: #ffffff; " +
                    "hoverBackground: #eaecf0; " +
                    "font: +0"
                );
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (isSelected()) {
                    if (getFont().getStyle() != Font.BOLD) {
                        setFont(DesignSystem.bodyFont(14f, Font.BOLD));
                    }
                } else {
                    if (getFont().getStyle() != Font.PLAIN) {
                        setFont(DesignSystem.bodyFont(14f, Font.PLAIN));
                    }
                }
            }
        }
    }

    // ─── Nested MemberHomePanel Class ────────────────────────────────────────

    public static class MemberHomePanel extends JPanel {
        private final User user;
        private final TransactionService transactionService = new TransactionService();
        private final FineService fineService = new FineService();

        private JLabel lblActiveLoans;
        private JLabel lblPendingFines;

        public MemberHomePanel(User user) {
            this.user = user;
            setLayout(new BorderLayout(0, 24));
            setBackground(DesignSystem.SURFACE);
            setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

            initComponents();
            refreshData();
        }

        private void initComponents() {
            // Welcome Banner
            JPanel welcomePanel = new JPanel();
            welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
            welcomePanel.setOpaque(true);
            welcomePanel.setBackground(DesignSystem.PRIMARY);
            welcomePanel.putClientProperty(FlatClientProperties.STYLE, "arc: 16");
            welcomePanel.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

            JLabel lblGreeting = new JLabel("Halo, " + user.getFullName() + "!");
            lblGreeting.setFont(DesignSystem.displayFont(28f, Font.BOLD));
            lblGreeting.setForeground(Color.WHITE);
            lblGreeting.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblSubGreeting = new JLabel("Selamat datang kembali di Perpustakaan Freedom. Selamat membaca!");
            lblSubGreeting.setFont(DesignSystem.bodyFont(14f, Font.PLAIN));
            lblSubGreeting.setForeground(new Color(255, 255, 255, 200));
            lblSubGreeting.setAlignmentX(Component.LEFT_ALIGNMENT);

            welcomePanel.add(lblGreeting);
            welcomePanel.add(Box.createVerticalStrut(8));
            welcomePanel.add(lblSubGreeting);

            add(welcomePanel, BorderLayout.NORTH);

            // Stats Container
            JPanel statsContainer = new JPanel(new GridLayout(1, 2, 24, 0));
            statsContainer.setOpaque(false);

            // Card 1: Active Loans
            JPanel cardLoans = createStatCard("Buku Sedang Dipinjam", new Color(0x005fb0));
            lblActiveLoans = new JLabel("0");
            lblActiveLoans.setFont(DesignSystem.displayFont(36f, Font.BOLD));
            lblActiveLoans.setForeground(DesignSystem.ON_SURFACE);
            cardLoans.add(lblActiveLoans, BorderLayout.CENTER);
            statsContainer.add(cardLoans);

            // Card 2: Pending Fines
            JPanel cardFines = createStatCard("Total Denda Belum Dibayar", new Color(0xba1a1a));
            lblPendingFines = new JLabel("Rp 0");
            lblPendingFines.setFont(DesignSystem.displayFont(36f, Font.BOLD));
            lblPendingFines.setForeground(DesignSystem.ON_SURFACE);
            cardFines.add(lblPendingFines, BorderLayout.CENTER);
            statsContainer.add(cardFines);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setOpaque(false);
            centerPanel.add(statsContainer, BorderLayout.NORTH);

            add(centerPanel, BorderLayout.CENTER);
        }

        private JPanel createStatCard(String title, Color accentColor) {
            JPanel card = new JPanel(new BorderLayout(0, 12));
            card.setBackground(DesignSystem.SURFACE_CONTAINER_LOW);
            card.putClientProperty(FlatClientProperties.STYLE, "arc: 16");
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
            ));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(DesignSystem.bodyFont(14f, Font.PLAIN));
            lblTitle.setForeground(DesignSystem.ON_SURFACE_VARIANT);
            card.add(lblTitle, BorderLayout.NORTH);

            return card;
        }

        public void refreshData() {
            int activeLoans = transactionService.getActiveLoansCount(user.getUserId());
            double pendingFines = fineService.getTotalPendingFeesByUser(user.getUserId());

            lblActiveLoans.setText(String.valueOf(activeLoans));
            lblPendingFines.setText(String.format("Rp %,.0f", pendingFines));
        }
    }

    // ─── Nested BorrowedBooksPanel Class ─────────────────────────────────────

    public static class BorrowedBooksPanel extends JPanel {
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

            borrowedTitleLabel = new JLabel("Pinjaman Saya");
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
            try {
                List<Transaction> txs = transactionService.getTransactionsByUser(userId, currentPage, pageSize);
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

        public void refreshTable() {
            loadBorrowedData();
        }
    }

    // ─── Nested FinesLedgerPanel Class ───────────────────────────────────────

    public static class FinesLedgerPanel extends JPanel {
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

            finesTitleLabel = new JLabel("Tagihan Denda Saya");
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
            try {
                List<Fine> fines = fineService.getFinesByUser(userId, currentPage, pageSize);
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

        public void refreshTable() {
            loadFinesData();
        }
    }

    // ─── Nested SearchCatalogPanel Class ─────────────────────────────────────

    public static class SearchCatalogPanel extends JPanel {
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

            catalogTitleLabel = new JLabel("Katalog Perpustakaan");
            catalogTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +6");
            headerPanel.add(catalogTitleLabel, BorderLayout.WEST);

            JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            controlsPanel.setBackground(UIManager.getColor("Panel.background"));

            JTextField txtSearch = new JTextField(20);
            txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari judul, penulis, kata kunci...");
            txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new com.formdev.flatlaf.icons.FlatSearchIcon());
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
            catalogTableModel.setRowCount(0);
            try {
                List<Book> books;
                if (searchQuery == null || searchQuery.trim().isEmpty()) {
                    books = bookService.getAllBooks(currentPage, pageSize);
                } else {
                    books = bookService.searchBooks(searchQuery.trim(), currentPage, pageSize);
                }
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
    }
}
