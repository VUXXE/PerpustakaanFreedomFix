package com.kelompok1.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.kelompok1.model.Models.User;
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

        JComboBox<String> comboDate = new JComboBox<>(new String[]{"6 bulan terakhir", "30 hari terakhir", "Tahun ini"});
        comboDate.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: $Panel.background");

        JButton btnTheme = new JButton(DesignSystem.getToggleLabel());
        btnTheme.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btnTheme.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 4, 12, 4, 12");
        btnTheme.addActionListener(e -> {
            DesignSystem.toggleTheme(AdminDashboard.this);
            btnTheme.setText(DesignSystem.getToggleLabel());
        });

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

        rightNav.add(comboDate);
        rightNav.add(btnTheme);
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
        mainContent.add(createHelpPanel(), "Help");

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

    private JPanel createHelpPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(DesignSystem.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        JLabel lbl = new JLabel("<html>"
            + "<span style='font-size:16pt; font-weight:bold; color:#191c1e'>Bantuan &amp; Dukungan</span><br><br>"
            + "<span style='font-size:11pt; color:#5b403d'>Hubungi administrator sistem untuk bantuan lebih lanjut.</span>"
            + "</html>");
        panel.add(lbl);
        return panel;
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
        private final SidebarNavButton btnHelp;

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
            btnDashboard = createNavButton("Beranda",        "Dashboard");
            btnMembers   = createNavButton("Members",        "Members");
            btnAddBooks  = createNavButton("Manajemen Buku", "Books");
            btnCheckout  = createNavButton("Transaksi",      "Transactions");
            btnFines     = createNavButton("Denda",          "Fines");
            btnReports   = createNavButton("Laporan",        "Reports");
            btnSettings  = createNavButton("Pengaturan",     "Settings");
            btnHelp      = createNavButton("Bantuan",        "Help");

            group.add(btnDashboard);
            group.add(btnMembers);
            group.add(btnAddBooks);
            group.add(btnCheckout);
            group.add(btnFines);
            group.add(btnReports);
            group.add(btnSettings);
            group.add(btnHelp);
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
            add(btnHelp);
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
                case "Dashboard"    -> btnDashboard.setSelected(true);
                case "Members"      -> btnMembers.setSelected(true);
                case "Books"        -> btnAddBooks.setSelected(true);
                case "Transactions" -> btnCheckout.setSelected(true);
                case "Fines"        -> btnFines.setSelected(true);
                case "Reports"      -> btnReports.setSelected(true);
                case "Settings"     -> btnSettings.setSelected(true);
                case "Help"         -> btnHelp.setSelected(true);
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
}
