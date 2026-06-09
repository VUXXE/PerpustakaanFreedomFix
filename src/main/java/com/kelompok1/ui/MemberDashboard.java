package com.kelompok1.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.ui.panel.member.BorrowedBooksPanel;
import com.kelompok1.ui.panel.member.FinesLedgerPanel;
import com.kelompok1.ui.panel.member.SearchCatalogPanel;
import com.kelompok1.util.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class MemberDashboard extends JFrame {

    private com.kelompok1.model.User loggedInUser;

    public MemberDashboard(com.kelompok1.model.User user) {
        this.loggedInUser = user;
        setTitle("Sistem Manajemen Perpustakaan");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Top Navigation Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.putClientProperty(FlatClientProperties.STYLE, "background: $Panel.background; border: 0,0,1,0,$Component.borderColor");
        
        JLabel lblHeader = new JLabel("Selamat datang, " + loggedInUser.getFullName());
        lblHeader.putClientProperty(FlatClientProperties.STYLE, "font: bold +6; foreground: $Label.foreground");
        lblHeader.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        topPanel.add(lblHeader, BorderLayout.WEST);
        
        // Top Right Actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        
        JButton btnTheme = new JButton("Mode Gelap");
        btnTheme.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btnTheme.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        btnTheme.addActionListener(e -> toggleTheme(btnTheme));
        
        JButton btnLogout = new JButton("Keluar");
        btnLogout.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btnLogout.putClientProperty(FlatClientProperties.STYLE, "foreground: $Component.error.focusedBorderColor; arc: 10");
        btnLogout.addActionListener(e -> {
            new LoginView().setVisible(true);
            this.dispose();
        });
        
        actionsPanel.add(btnTheme);
        actionsPanel.add(btnLogout);
        topPanel.add(actionsPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);

        // Main Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.putClientProperty(FlatClientProperties.STYLE, 
            "tabType: pill; " +
            "tabInsets: 8, 20, 8, 20; " +
            "tabAreaInsets: 15, 20, 0, 20; " +
            "tabHeight: 35; " +
            "background: $Window.background");
        
        tabs.addTab("Katalog Buku", new SearchCatalogPanel());
        tabs.addTab("Pinjaman Saya", new BorrowedBooksPanel(loggedInUser.getUserId()));
        tabs.addTab("Tagihan Denda", new FinesLedgerPanel(loggedInUser.getUserId()));
        
        add(tabs, BorderLayout.CENTER);
    }
    
    private void toggleTheme(JButton btnTheme) {
        ThemeManager.toggleTheme(this);
        btnTheme.setText(ThemeManager.getToggleLabel());
    }
}
