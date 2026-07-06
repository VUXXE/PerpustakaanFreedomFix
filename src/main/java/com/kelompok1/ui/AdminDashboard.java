package com.kelompok1.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.kelompok1.model.User;
import com.kelompok1.ui.panel.DashboardPanel;
import com.kelompok1.ui.panel.MembersPanel;
import com.kelompok1.ui.panel.BooksPanel;
import com.kelompok1.ui.panel.TransactionsPanel;
import com.kelompok1.ui.panel.FinesPanel;
import com.kelompok1.ui.panel.ReportsPanel;
import com.kelompok1.ui.panel.SettingsPanel;
import com.kelompok1.util.DesignSystem;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    public interface SidebarListener {
        void onTabSelected(String tabName);
        void onLogout();
    }

    private CardLayout cardLayout;
    private JPanel mainContent;
    private User loggedInUser;
    private String activeCard = "Dashboard";

    public AdminDashboard(User user) {
        this.loggedInUser = user;
        setTitle("Perpustakaan Freedom — Admin");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(DesignSystem.SURFACE);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ═══════════════════════════════════════
        //  SIDEBAR (WEST)
        // ═══════════════════════════════════════
        SidebarPanel sidebar = new SidebarPanel(loggedInUser, new SidebarListener() {
            @Override
            public void onTabSelected(String tabName) {
                cardLayout.show(mainContent, tabName);
                activeCard = tabName;
            }

            @Override
            public void onLogout() {
                new LoginView().setVisible(true);
                AdminDashboard.this.dispose();
            }
        });
        add(sidebar, BorderLayout.WEST);

        // ═══════════════════════════════════════
        //  MAIN CONTAINER (CENTER)
        // ═══════════════════════════════════════
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(DesignSystem.SURFACE);

        // TOP NAVBAR
        JPanel topNav = new JPanel(new BorderLayout());
        topNav.setBackground(DesignSystem.SURFACE_CONTAINER_LOWEST);
        topNav.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, DesignSystem.OUTLINE_VARIANT),
            BorderFactory.createEmptyBorder(0, 28, 0, 28)
        ));
        topNav.setPreferredSize(new Dimension(getWidth(), 64));

        // Search Bar
        JTextField txtSearch = new JTextField(30);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari ISBN, Judul, Penulis, Anggota…");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 999; margin: 5, 12, 5, 12");
        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 16));
        searchWrapper.setOpaque(false);
        searchWrapper.add(txtSearch);
        topNav.add(searchWrapper, BorderLayout.WEST);

        // Right nav
        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        rightNav.setOpaque(false);



        // Profile chip
        JPanel profileChip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        profileChip.setOpaque(false);
        JLabel avatar = new JLabel(String.valueOf(loggedInUser.getFullName().charAt(0)).toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DesignSystem.PRIMARY);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(DesignSystem.displayFont(13f, Font.BOLD));
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        avatar.setPreferredSize(new Dimension(32, 32));
        JLabel lblProfile = new JLabel(loggedInUser.getFullName());
        lblProfile.setFont(DesignSystem.bodyFont(13f, Font.BOLD));
        lblProfile.setForeground(DesignSystem.ON_SURFACE);
        profileChip.add(avatar);
        profileChip.add(lblProfile);


        rightNav.add(profileChip);
        topNav.add(rightNav, BorderLayout.EAST);

        mainContainer.add(topNav, BorderLayout.NORTH);

        // CARD LAYOUT CONTENT AREA
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(DesignSystem.SURFACE);

        MembersPanel membersPanel = new MembersPanel();
        BooksPanel booksPanel = new BooksPanel();
        TransactionsPanel transactionsPanel = new TransactionsPanel();
        FinesPanel finesPanel = new FinesPanel();
        ReportsPanel reportsPanel = new ReportsPanel();

        mainContent.add(new DashboardPanel(), "Dashboard");
        mainContent.add(membersPanel, "Members");
        mainContent.add(booksPanel, "Books");
        mainContent.add(transactionsPanel, "Transactions");
        mainContent.add(finesPanel, "Fines");
        mainContent.add(reportsPanel, "Reports");
        mainContent.add(new SettingsPanel(), "Settings");

        txtSearch.addActionListener(e -> {
            String q = txtSearch.getText().trim();
            switch (activeCard) {
                case "Members"      -> { membersPanel.setSearchQuery(q); }
                case "Transactions" -> { transactionsPanel.setSearchQuery(q); }
                case "Fines"        -> { finesPanel.setSearchQuery(q); }
                default             -> {
                    cardLayout.show(mainContent, "Books");
                    sidebar.setSelectedTab("Books");
                    activeCard = "Books";
                    booksPanel.setSearchQuery(q);
                }
            }
        });

        mainContainer.add(mainContent, BorderLayout.CENTER);
        add(mainContainer, BorderLayout.CENTER);
    }

    // ─── Nested Sidebar Panel Class ──────────────────────────────────────────
    
    private static class SidebarPanel extends JPanel {
        private final SidebarListener listener;
        private final SidebarNavButton btnDashboard;
        private final SidebarNavButton btnMembers;
        private final SidebarNavButton btnAddBooks;
        private final SidebarNavButton btnCheckout;
        private final SidebarNavButton btnFines;
        private final SidebarNavButton btnReports;
        private final SidebarNavButton btnSettings;

        public SidebarPanel(User loggedInUser, SidebarListener listener) {
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
            btnDashboard = createNavButton("Beranda",        "Dashboard",    IconType.HOME);
            btnMembers   = createNavButton("Members",        "Members",      IconType.USERS);
            btnAddBooks  = createNavButton("Manajemen Buku", "Books",        IconType.BOOK);
            btnCheckout  = createNavButton("Transaksi",      "Transactions", IconType.TRANSACTION);
            btnFines     = createNavButton("Denda",          "Fines",        IconType.FINE);
            btnReports   = createNavButton("Laporan",        "Reports",      IconType.DOCUMENT);
            btnSettings  = createNavButton("Pengaturan",     "Settings",     IconType.SETTINGS);

            group.add(btnDashboard);
            group.add(btnMembers);
            group.add(btnAddBooks);
            group.add(btnCheckout);
            group.add(btnFines);
            group.add(btnReports);
            group.add(btnSettings);
            btnDashboard.setSelected(true);

            add(btnDashboard);
            add(Box.createVerticalStrut(8));
            add(btnMembers);
            add(Box.createVerticalStrut(8));
            add(btnAddBooks);
            add(Box.createVerticalStrut(8));
            add(btnCheckout);
            add(Box.createVerticalStrut(8));
            add(btnFines);
            add(Box.createVerticalStrut(8));
            add(btnReports);
            add(Box.createVerticalStrut(8));
            add(btnSettings);
            add(Box.createVerticalStrut(8));
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
                case "Dashboard"    -> btnDashboard.setSelected(true);
                case "Members"      -> btnMembers.setSelected(true);
                case "Books"        -> btnAddBooks.setSelected(true);
                case "Transactions" -> btnCheckout.setSelected(true);
                case "Fines"        -> btnFines.setSelected(true);
                case "Reports"      -> btnReports.setSelected(true);
                case "Settings"     -> btnSettings.setSelected(true);
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
}
