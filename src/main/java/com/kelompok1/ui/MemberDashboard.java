package com.kelompok1.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.ui.panel.member.BorrowedBooksPanel;
import com.kelompok1.ui.panel.member.FinesLedgerPanel;
import com.kelompok1.ui.panel.member.SearchCatalogPanel;
import com.kelompok1.ui.panel.member.MemberHomePanel;
import com.kelompok1.util.DesignSystem;
import com.kelompok1.util.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class MemberDashboard extends JFrame implements MemberSidebarPanel.SidebarListener {

    private com.kelompok1.model.User loggedInUser;
    
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private MemberSidebarPanel sidebar;

    private MemberHomePanel homePanel;
    private SearchCatalogPanel catalogPanel;
    private BorrowedBooksPanel borrowedPanel;
    private FinesLedgerPanel finesPanel;

    public MemberDashboard(com.kelompok1.model.User user) {
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
        sidebar = new MemberSidebarPanel(loggedInUser, this);
        add(sidebar, BorderLayout.WEST);

        // 2. Main Area Container
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(DesignSystem.SURFACE);

        // Top Bar (Theme Toggle)
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        topBar.setBackground(DesignSystem.SURFACE);
        JButton btnTheme = new JButton(ThemeManager.getToggleLabel());
        btnTheme.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btnTheme.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 4, 12, 4, 12");
        btnTheme.addActionListener(e -> {
            ThemeManager.toggleTheme(this);
            btnTheme.setText(ThemeManager.getToggleLabel());
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

    @Override
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

    @Override
    public void onLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin keluar?", "Konfirmasi Keluar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginView().setVisible(true);
            this.dispose();
        }
    }
}
