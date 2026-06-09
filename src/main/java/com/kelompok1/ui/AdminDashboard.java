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
        
        setTitle("Aplikasi Perpustakaan");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIManager.getColor("Panel.background"));
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());

        // --- SIDEBAR (WEST) ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setBackground(UIManager.getColor("List.background"));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));

        JLabel lblHeader = new JLabel("Perpustakaan Freedom");
        lblHeader.putClientProperty(FlatClientProperties.STYLE, "font: bold +6");
        lblHeader.setBorder(BorderFactory.createEmptyBorder(25, 30, 20, 20));
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblHeader);
        sidebar.add(Box.createVerticalStrut(20));

        ButtonGroup group = new ButtonGroup();
        JToggleButton btnDashboard = createSidebarButton("Beranda");
        JToggleButton btnMembers = createSidebarButton("Anggota");
        JToggleButton btnAddBooks = createSidebarButton("Manajemen Buku");
        JToggleButton btnCheckout = createSidebarButton("Transaksi");
        JToggleButton btnFines = createSidebarButton("Denda");
        JToggleButton btnSettings = createSidebarButton("Pengaturan");
        JToggleButton btnHelp = createSidebarButton("Bantuan");

        btnDashboard.addActionListener(e -> { cardLayout.show(mainContent, "Dashboard"); activeCard = "Dashboard"; });
        btnMembers.addActionListener(e -> { cardLayout.show(mainContent, "Members"); activeCard = "Members"; });
        btnAddBooks.addActionListener(e -> { cardLayout.show(mainContent, "Books"); activeCard = "Books"; });
        btnCheckout.addActionListener(e -> { cardLayout.show(mainContent, "Transactions"); activeCard = "Transactions"; });
        btnFines.addActionListener(e -> { cardLayout.show(mainContent, "Fines"); activeCard = "Fines"; });
        btnSettings.addActionListener(e -> { cardLayout.show(mainContent, "Settings"); activeCard = "Settings"; });
        btnHelp.addActionListener(e -> { cardLayout.show(mainContent, "Help"); activeCard = "Help"; });

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

        JButton btnLogout = new JButton("Keluar");
        btnLogout.putClientProperty(FlatClientProperties.STYLE, 
            "arc: 0; " +
            "margin: 15, 30, 15, 30; " +
            "focusWidth: 0; " +
            "innerFocusWidth: 0; " +
            "background: null; " +
            "borderWidth: 0; " +
            "foreground: $Component.error.focusedBorderColor; " +
            "font: bold");
        btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btnLogout.setHorizontalAlignment(SwingConstants.LEFT);
        btnLogout.addActionListener(e -> {
            new LoginView().setVisible(true);
            this.dispose();
        });
        sidebar.add(btnLogout);
        sidebar.add(Box.createVerticalStrut(20));

        add(sidebar, BorderLayout.WEST);

        // --- MAIN CONTAINER (CENTER) ---
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(UIManager.getColor("Panel.background"));

        // TOP NAVBAR (NORTH of Main Container)
        JPanel topNav = new JPanel(new BorderLayout());
        topNav.setBackground(UIManager.getColor("List.background"));
        topNav.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(15, 30, 15, 30)
        ));
        topNav.setPreferredSize(new Dimension(getWidth(), 70));

        // Search Bar
        JTextField txtSearch = new JTextField(30);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari ISBN, Judul, Penulis, Anggota, dll");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 999; margin: 5, 10, 5, 10");
        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchWrapper.setOpaque(false);
        searchWrapper.add(txtSearch);
        topNav.add(searchWrapper, BorderLayout.WEST);

        // Right Actions (Date, Theme Toggle, Profile)
        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightNav.setOpaque(false);

        JComboBox<String> comboDate = new JComboBox<>(new String[]{"6 bulan terakhir", "30 hari terakhir", "Tahun ini"});
        comboDate.putClientProperty(FlatClientProperties.STYLE, "arc: 999; background: $Window.background");
        comboDate.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));

        JButton btnTheme = new JButton(ThemeManager.getToggleLabel());
        btnTheme.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btnTheme.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        btnTheme.addActionListener(e -> {
            ThemeManager.toggleTheme(AdminDashboard.this);
            btnTheme.setText(ThemeManager.getToggleLabel());
            sidebar.setBackground(UIManager.getColor("List.background"));
            topNav.setBackground(UIManager.getColor("List.background"));
        });

        JLabel lblProfile = new JLabel(loggedInUser.getFullName());
        lblProfile.putClientProperty(FlatClientProperties.STYLE, "font: bold");

        rightNav.add(comboDate);
        rightNav.add(btnTheme);
        rightNav.add(lblProfile);
        topNav.add(rightNav, BorderLayout.EAST);

        mainContainer.add(topNav, BorderLayout.NORTH);

        // DASHBOARD CONTENT AREA (CENTER of Main Container)
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(UIManager.getColor("Panel.background"));
        
        MembersPanel membersPanel = new MembersPanel();
        BooksPanel booksPanel = new BooksPanel();
        TransactionsPanel transactionsPanel = new TransactionsPanel();
        FinesPanel finesPanel = new FinesPanel();
        
        mainContent.add(new DashboardPanel(), "Dashboard");
        mainContent.add(membersPanel, "Members");
        mainContent.add(booksPanel, "Books");
        mainContent.add(transactionsPanel, "Transactions");
        mainContent.add(finesPanel, "Fines");
        mainContent.add(createSettingsPanel(), "Settings");
        mainContent.add(createHelpPanel(), "Help");
        
        txtSearch.addActionListener(e -> {
            String q = txtSearch.getText().trim();
            switch (activeCard) {
                case "Members":
                    membersPanel.setSearchQuery(q);
                    break;
                case "Transactions":
                    transactionsPanel.setSearchQuery(q);
                    break;
                case "Fines":
                    finesPanel.setSearchQuery(q);
                    break;
                default:
                    cardLayout.show(mainContent, "Books");
                    btnAddBooks.setSelected(true);
                    activeCard = "Books";
                    booksPanel.setSearchQuery(q);
                    break;
            }
        });
        
        mainContainer.add(mainContent, BorderLayout.CENTER);

        add(mainContainer, BorderLayout.CENTER);
    }

    private JToggleButton createSidebarButton(String text) {
        JToggleButton btn = new JToggleButton(text);
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btn.putClientProperty(FlatClientProperties.STYLE, 
            "arc: 10; " +
            "margin: 12, 30, 12, 30; " +
            "selectedBackground: $Component.accentColor; " +
            "selectedForeground: #ffffff; " +
            "font: +1");
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        return btn;
    }

    private JPanel createSettingsPanel() {
        return new SettingsPanel();
    }

    private JPanel createHelpPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(UIManager.getColor("Panel.background"));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        JLabel lbl = new JLabel("<html><h3>Bantuan & Dukungan</h3><p>Hubungi administrator sistem untuk bantuan lebih lanjut.</p></html>");
        panel.add(lbl);
        return panel;
    }
}
