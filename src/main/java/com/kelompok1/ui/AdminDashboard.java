package com.kelompok1.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.kelompok1.model.User;
import com.kelompok1.ui.panel.DashboardPanel;
import com.kelompok1.ui.panel.MembersPanel;
import com.kelompok1.ui.panel.BooksPanel;
import com.kelompok1.ui.panel.TransactionsPanel;
import com.kelompok1.ui.panel.FinesPanel;
import com.kelompok1.ui.panel.SettingsPanel;
import com.kelompok1.util.DesignSystem;
import com.kelompok1.util.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

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
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setBackground(DesignSystem.SURFACE_CONTAINER);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, DesignSystem.OUTLINE_VARIANT));

        // Brand logo text
        JLabel lblHeader = new JLabel("Perpustakaan Freedom");
        lblHeader.setFont(DesignSystem.displayFont(15f, Font.BOLD));
        lblHeader.setForeground(DesignSystem.PRIMARY);
        lblHeader.setBorder(BorderFactory.createEmptyBorder(28, 24, 24, 20));
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblHeader);

        // Section divider
        JSeparator sep = new JSeparator();
        sep.setForeground(DesignSystem.OUTLINE_VARIANT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(12));

        // Nav buttons
        ButtonGroup group = new ButtonGroup();
        SidebarNavButton btnDashboard    = new SidebarNavButton("Beranda",          "⊞");
        SidebarNavButton btnMembers      = new SidebarNavButton("Anggota",          "♟");
        SidebarNavButton btnAddBooks     = new SidebarNavButton("Manajemen Buku",   "☰");
        SidebarNavButton btnCheckout     = new SidebarNavButton("Transaksi",        "⟳");
        SidebarNavButton btnFines        = new SidebarNavButton("Denda",            "₹");
        SidebarNavButton btnSettings     = new SidebarNavButton("Pengaturan",       "⚙");
        SidebarNavButton btnHelp         = new SidebarNavButton("Bantuan",          "?");

        btnDashboard.addActionListener(e -> { cardLayout.show(mainContent, "Dashboard");    activeCard = "Dashboard"; });
        btnMembers.addActionListener(e -> { cardLayout.show(mainContent, "Members");        activeCard = "Members"; });
        btnAddBooks.addActionListener(e -> { cardLayout.show(mainContent, "Books");         activeCard = "Books"; });
        btnCheckout.addActionListener(e -> { cardLayout.show(mainContent, "Transactions");  activeCard = "Transactions"; });
        btnFines.addActionListener(e -> { cardLayout.show(mainContent, "Fines");            activeCard = "Fines"; });
        btnSettings.addActionListener(e -> { cardLayout.show(mainContent, "Settings");      activeCard = "Settings"; });
        btnHelp.addActionListener(e -> { cardLayout.show(mainContent, "Help");              activeCard = "Help"; });

        group.add(btnDashboard); group.add(btnMembers); group.add(btnAddBooks);
        group.add(btnCheckout); group.add(btnFines); group.add(btnSettings); group.add(btnHelp);
        btnDashboard.setSelected(true);

        sidebar.add(btnDashboard);
        sidebar.add(btnMembers);
        sidebar.add(btnAddBooks);
        sidebar.add(btnCheckout);
        sidebar.add(btnFines);
        sidebar.add(btnSettings);
        sidebar.add(btnHelp);
        sidebar.add(Box.createVerticalGlue());

        // Logout button — bottom-pinned, red text
        JButton btnLogout = new JButton("Keluar");
        btnLogout.setFont(DesignSystem.bodyFont(13f, Font.BOLD));
        btnLogout.putClientProperty(FlatClientProperties.STYLE,
            "arc: 0; margin: 15, 24, 15, 24; focusWidth: 0; innerFocusWidth: 0; " +
            "background: null; borderWidth: 0; " +
            "foreground: #ba1a1a; font: bold");
        btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btnLogout.setHorizontalAlignment(SwingConstants.LEFT);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            new LoginView().setVisible(true);
            this.dispose();
        });
        sidebar.add(btnLogout);
        sidebar.add(Box.createVerticalStrut(16));

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
        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
        searchWrapper.setOpaque(false);
        searchWrapper.add(txtSearch);
        topNav.add(searchWrapper, BorderLayout.WEST);

        // Right nav
        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightNav.setOpaque(false);

        JComboBox<String> comboDate = new JComboBox<>(new String[]{"6 bulan terakhir", "30 hari terakhir", "Tahun ini"});
        comboDate.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: $Panel.background");

        JButton btnTheme = new JButton(ThemeManager.getToggleLabel());
        btnTheme.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btnTheme.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 4, 12, 4, 12");
        btnTheme.addActionListener(e -> {
            ThemeManager.toggleTheme(AdminDashboard.this);
            btnTheme.setText(ThemeManager.getToggleLabel());
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

        mainContent.add(new DashboardPanel(), "Dashboard");
        mainContent.add(membersPanel, "Members");
        mainContent.add(booksPanel, "Books");
        mainContent.add(transactionsPanel, "Transactions");
        mainContent.add(finesPanel, "Fines");
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
                    btnAddBooks.setSelected(true);
                    activeCard = "Books";
                    booksPanel.setSearchQuery(q);
                }
            }
        });

        mainContainer.add(mainContent, BorderLayout.CENTER);
        add(mainContainer, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────
    //  SIDEBAR NAV BUTTON (with red active indicator)
    // ─────────────────────────────────────────────
    private static class SidebarNavButton extends JToggleButton {
        private static final int INDICATOR_W = 3;
        private final String label;

        SidebarNavButton(String label, String icon) {
            super(label);
            this.label = label;
            setFont(DesignSystem.bodyFont(13f, Font.PLAIN));
            setForeground(DesignSystem.ON_SURFACE);
            setHorizontalAlignment(SwingConstants.LEFT);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(true);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(true);
            putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
            putClientProperty(FlatClientProperties.STYLE,
                "arc: 0; " +
                "margin: 12, 24, 12, 24; " +
                "selectedBackground: #f5e8e8; " +
                "selectedForeground: #86000d; " +
                "hoverBackground: #e8eaec; " +
                "font: +0"
            );
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isSelected()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DesignSystem.PRIMARY);
                // 3px red bar on left edge, vertically centered with 8px padding
                int barH = getHeight() - 16;
                int barY = 8;
                g2.fillRoundRect(0, barY, INDICATOR_W, barH, INDICATOR_W, INDICATOR_W);
                // Make text bold when selected
                setFont(DesignSystem.bodyFont(13f, Font.BOLD));
                g2.dispose();
            } else {
                setFont(DesignSystem.bodyFont(13f, Font.PLAIN));
            }
        }
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
}
