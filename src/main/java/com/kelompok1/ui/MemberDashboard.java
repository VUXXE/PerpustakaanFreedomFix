package com.kelompok1.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.model.User;
import com.kelompok1.model.Book;
import com.kelompok1.model.Transaction;
import com.kelompok1.model.Fine;
import com.kelompok1.service.BookService;
import com.kelompok1.service.TransactionService;
import com.kelompok1.service.FineService;
import com.kelompok1.service.UserService;
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
    private MemberSettingsPanel settingsPanel;

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
        settingsPanel = new MemberSettingsPanel();

        mainContentPanel.add(homePanel, "Dashboard");
        mainContentPanel.add(catalogPanel, "Catalog");
        mainContentPanel.add(borrowedPanel, "Borrowed");
        mainContentPanel.add(finesPanel, "Fines");
        mainContentPanel.add(settingsPanel, "Settings");

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
        private final SidebarNavButton btnSettings;

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
            try {
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.File("LOGO.png"));
                int targetWidth = 180;
                int targetHeight = (img.getHeight() * targetWidth) / img.getWidth();
                java.awt.Image scaled = img.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH);
                lblHeader.setIcon(new ImageIcon(scaled));
                lblHeader.setText("");
            } catch (Exception e) {}
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
            btnDashboard = createNavButton("Beranda",       "Dashboard", IconType.HOME);
            btnCatalog   = createNavButton("Katalog Buku",  "Catalog",   IconType.BOOK);
            btnBorrowed  = createNavButton("Pinjaman Saya", "Borrowed",  IconType.TRANSACTION);
            btnFines     = createNavButton("Tagihan Denda", "Fines",     IconType.FINE);
            btnSettings  = createNavButton("Pengaturan",    "Settings",  IconType.SETTINGS);

            group.add(btnDashboard);
            group.add(btnCatalog);
            group.add(btnBorrowed);
            group.add(btnFines);
            group.add(btnSettings);
            btnDashboard.setSelected(true);

            add(btnDashboard);
            add(Box.createVerticalStrut(8));
            add(btnCatalog);
            add(Box.createVerticalStrut(8));
            add(btnBorrowed);
            add(Box.createVerticalStrut(8));
            add(btnFines);
            add(Box.createVerticalStrut(8));
            add(btnSettings);
            add(Box.createVerticalGlue());

            // Logout button
            JButton btnLogout = new JButton("Keluar");
            btnLogout.setIcon(new SidebarIcon(IconType.LOGOUT));
            btnLogout.setIconTextGap(14);
            btnLogout.setFont(DesignSystem.bodyFont(14f, Font.BOLD));
            btnLogout.setForeground(new Color(0x5f6368));
            btnLogout.setHorizontalAlignment(SwingConstants.LEFT);
            btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnLogout.setOpaque(false);
            btnLogout.setBorderPainted(false);
            btnLogout.setFocusPainted(false);
            btnLogout.setContentAreaFilled(true);
            btnLogout.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
            btnLogout.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; " +
                "margin: 8, 16, 8, 16; " +
                "hoverBackground: #f1f3f4; " +
                "font: +0"
            );
            btnLogout.addActionListener(e -> {
                if (listener != null) {
                    listener.onLogout();
                }
            });
            add(btnLogout);
            add(Box.createVerticalStrut(10));
        }

        private SidebarNavButton createNavButton(String label, String tabName, IconType iconType) {
            SidebarNavButton btn = new SidebarNavButton(label, iconType);
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

        private enum IconType { HOME, USERS, BOOK, TRANSACTION, FINE, DOCUMENT, SETTINGS, LOGOUT }

        private static class SidebarIcon implements Icon {
            private final IconType type;
            public SidebarIcon(IconType type) { this.type = type; }
            @Override public int getIconWidth() { return 20; }
            @Override public int getIconHeight() { return 20; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                boolean selected = false;
                if (c instanceof JToggleButton) {
                    selected = ((JToggleButton)c).isSelected();
                }
                
                // Color matches text color (white if selected, gray if not)
                g2.setColor(selected ? Color.WHITE : new Color(0x5f6368));
                g2.translate(x, y);
                
                switch(type) {
                    case HOME -> {
                        g2.drawPolygon(new int[]{2, 10, 18}, new int[]{10, 2, 10}, 3);
                        g2.drawRect(4, 10, 12, 8);
                        g2.drawRect(8, 14, 4, 4);
                    }
                    case USERS -> {
                        g2.drawOval(6, 2, 6, 6);
                        g2.drawArc(2, 11, 14, 14, 0, 180);
                    }
                    case BOOK -> {
                        g2.drawRect(3, 3, 14, 14);
                        g2.drawLine(10, 3, 10, 17);
                    }
                    case TRANSACTION -> {
                        g2.drawLine(3, 7, 15, 7);
                        g2.drawLine(15, 7, 12, 4);
                        g2.drawLine(5, 13, 17, 13);
                        g2.drawLine(5, 13, 8, 16);
                    }
                    case FINE -> {
                        g2.drawOval(3, 3, 14, 14);
                        g2.drawLine(10, 6, 10, 14);
                        g2.drawArc(7, 6, 6, 4, 90, 180);
                        g2.drawArc(7, 10, 6, 4, 270, 180);
                    }
                    case DOCUMENT -> {
                        g2.drawRect(4, 2, 12, 16);
                        g2.drawLine(7, 6, 13, 6);
                        g2.drawLine(7, 10, 13, 10);
                        g2.drawLine(7, 14, 10, 14);
                    }
                    case SETTINGS -> {
                        g2.drawOval(6, 6, 8, 8);
                        g2.drawLine(10, 2, 10, 4); g2.drawLine(10, 16, 10, 18);
                        g2.drawLine(2, 10, 4, 10); g2.drawLine(16, 10, 18, 10);
                        g2.drawLine(4, 4, 6, 6);   g2.drawLine(14, 14, 16, 16);
                        g2.drawLine(14, 4, 16, 6); g2.drawLine(4, 14, 6, 16);
                    }
                    case LOGOUT -> {
                        g2.drawRect(3, 3, 8, 14);
                        g2.drawLine(16, 10, 8, 10);
                        g2.drawLine(16, 10, 13, 7);
                        g2.drawLine(16, 10, 13, 13);
                    }
                }
                g2.dispose();
            }
        }

        private static class SidebarNavButton extends JToggleButton {
            SidebarNavButton(String label, IconType iconType) {
                super(label);
                setIcon(new SidebarIcon(iconType));
                setIconTextGap(14);
                setFont(DesignSystem.bodyFont(14f, Font.BOLD)); // Always bold for modern look
                setForeground(new Color(0x5f6368));
                setHorizontalAlignment(SwingConstants.LEFT);
                setAlignmentX(Component.LEFT_ALIGNMENT);
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setOpaque(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setContentAreaFilled(true);
                putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
                putClientProperty(FlatClientProperties.STYLE,
                    "arc: 12; " +
                    "margin: 8, 16, 8, 16; " +
                    "selectedBackground: #ba1a1a; " + // Vibrant red instead of blue
                    "selectedForeground: #ffffff; " +
                    "hoverBackground: #f1f3f4; " +
                    "font: +0"
                );
            }

            @Override
            protected void paintComponent(Graphics g) {
                if (isSelected()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0xba1a1a)); // Vibrant red
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                    setForeground(Color.WHITE);
                } else {
                    setForeground(new Color(0x5f6368));
                }
                
                boolean wasFilled = isContentAreaFilled();
                if (isSelected()) {
                    setContentAreaFilled(false);
                }
                super.paintComponent(g);
                setContentAreaFilled(wasFilled);
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

    // ─── Settings Panel (Member Password Change) ─────────────────────────────
    
    private class MemberSettingsPanel extends JPanel {
        private final UserService userService = new UserService();
        private JPasswordField txtOldPassword;
        private JPasswordField txtNewPassword;
        private JPasswordField txtConfirmPassword;

        public MemberSettingsPanel() {
            setLayout(new BorderLayout());
            setBackground(DesignSystem.SURFACE);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(DesignSystem.SURFACE);
            JLabel title = new JLabel("Ubah Kata Sandi");
            title.putClientProperty(FlatClientProperties.STYLE, "font: bold +6");
            headerPanel.add(title, BorderLayout.WEST);
            add(headerPanel, BorderLayout.NORTH);

            JPanel card = UIUtils.createCardPanel(new GridBagLayout());
            card.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.weightx = 1.0;

            int row = 0;

            gbc.gridx = 0; gbc.gridy = row++;
            JLabel lblOld = new JLabel("Kata Sandi Lama");
            lblOld.setFont(DesignSystem.bodyFont(13f, Font.BOLD));
            card.add(lblOld, gbc);

            gbc.gridy = row++;
            txtOldPassword = new JPasswordField(20);
            txtOldPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true; arc: 8; margin: 8, 12, 8, 12");
            card.add(txtOldPassword, gbc);

            gbc.gridy = row++;
            card.add(Box.createVerticalStrut(10), gbc);

            gbc.gridy = row++;
            JLabel lblNew = new JLabel("Kata Sandi Baru");
            lblNew.setFont(DesignSystem.bodyFont(13f, Font.BOLD));
            card.add(lblNew, gbc);

            gbc.gridy = row++;
            txtNewPassword = new JPasswordField(20);
            txtNewPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true; arc: 8; margin: 8, 12, 8, 12");
            card.add(txtNewPassword, gbc);

            gbc.gridy = row++;
            JLabel lblConfirm = new JLabel("Konfirmasi Kata Sandi Baru");
            lblConfirm.setFont(DesignSystem.bodyFont(13f, Font.BOLD));
            card.add(lblConfirm, gbc);

            gbc.gridy = row++;
            txtConfirmPassword = new JPasswordField(20);
            txtConfirmPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true; arc: 8; margin: 8, 12, 8, 12");
            card.add(txtConfirmPassword, gbc);

            gbc.gridy = row++;
            card.add(Box.createVerticalStrut(20), gbc);

            gbc.gridy = row++;
            JButton btnSave = new JButton("Simpan Perubahan");
            DesignSystem.applyPrimaryButton(btnSave);
            btnSave.setFont(DesignSystem.bodyFont(14f, Font.BOLD));
            btnSave.setPreferredSize(new Dimension(0, 44));
            btnSave.addActionListener(e -> handleChangePassword());
            card.add(btnSave, gbc);

            JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
            centerWrapper.setBackground(DesignSystem.SURFACE);
            centerWrapper.add(card);

            add(centerWrapper, BorderLayout.CENTER);
        }

        private void handleChangePassword() {
            String oldPass = new String(txtOldPassword.getPassword());
            String newPass = new String(txtNewPassword.getPassword());
            String confirmPass = new String(txtConfirmPassword.getPassword());

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua kolom harus diisi!", "Kesalahan Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "Kata sandi baru tidak cocok dengan konfirmasi!", "Kesalahan Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (newPass.length() < 6) {
                JOptionPane.showMessageDialog(this, "Kata sandi baru minimal 6 karakter!", "Kesalahan Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                boolean success = userService.changePassword(loggedInUser.getUserId(), oldPass, newPass);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Kata sandi berhasil diubah!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    txtOldPassword.setText("");
                    txtNewPassword.setText("");
                    txtConfirmPassword.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Kata sandi lama salah!", "Kesalahan", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal mengubah kata sandi: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
