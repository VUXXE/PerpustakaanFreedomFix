package com.kelompok1.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.ui.panel.member.BorrowedBooksPanel;
import com.kelompok1.ui.panel.member.FinesLedgerPanel;
import com.kelompok1.ui.panel.member.SearchCatalogPanel;
import com.kelompok1.util.DesignSystem;
import com.kelompok1.util.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class MemberDashboard extends JFrame {

    private com.kelompok1.model.User loggedInUser;

    public MemberDashboard(com.kelompok1.model.User user) {
        this.loggedInUser = user;
        setTitle("Perpustakaan Freedom — Anggota");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(DesignSystem.SURFACE);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ─── Top Navigation Bar ───────────────────────────
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(DesignSystem.SURFACE_CONTAINER_LOWEST);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, DesignSystem.OUTLINE_VARIANT),
            BorderFactory.createEmptyBorder(0, 24, 0, 24)
        ));
        topPanel.setPreferredSize(new Dimension(getWidth(), 64));

        // Brand + welcome
        JPanel leftHead = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftHead.setOpaque(false);

        JLabel lblBrand = new JLabel("Perpustakaan Freedom");
        lblBrand.setFont(DesignSystem.displayFont(15f, Font.BOLD));
        lblBrand.setForeground(DesignSystem.PRIMARY);

        JSeparator vSep = new JSeparator(SwingConstants.VERTICAL);
        vSep.setPreferredSize(new Dimension(1, 20));
        vSep.setForeground(DesignSystem.OUTLINE_VARIANT);

        JLabel lblHeader = new JLabel("Selamat datang, " + loggedInUser.getFullName());
        lblHeader.setFont(DesignSystem.bodyFont(13f, Font.PLAIN));
        lblHeader.setForeground(DesignSystem.ON_SURFACE_VARIANT);

        leftHead.add(lblBrand);
        leftHead.add(vSep);
        leftHead.add(lblHeader);
        topPanel.add(leftHead, BorderLayout.WEST);

        // Right actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);

        JButton btnTheme = new JButton(ThemeManager.getToggleLabel());
        btnTheme.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btnTheme.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 4, 12, 4, 12");
        btnTheme.addActionListener(e -> toggleTheme(btnTheme));

        JButton btnLogout = new JButton("Keluar");
        btnLogout.setFont(DesignSystem.bodyFont(13f, Font.BOLD));
        btnLogout.putClientProperty(FlatClientProperties.STYLE,
            "foreground: #ba1a1a; arc: 8; margin: 4, 12, 4, 12; " +
            "background: null; borderWidth: 0; focusWidth: 0");
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            new LoginView().setVisible(true);
            this.dispose();
        });

        actionsPanel.add(btnTheme);
        actionsPanel.add(btnLogout);
        topPanel.add(actionsPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // ─── Tabs ─────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.putClientProperty(FlatClientProperties.STYLE,
            "tabType: pill; " +
            "tabInsets: 8, 20, 8, 20; " +
            "tabAreaInsets: 16, 20, 0, 20; " +
            "tabHeight: 36; " +
            "selectedBackground: #86000d; " +
            "selectedForeground: #ffffff; " +
            "background: $Panel.background"
        );
        tabs.setFont(DesignSystem.bodyFont(13f, Font.PLAIN));

        tabs.addTab("Katalog Buku",  new SearchCatalogPanel());
        tabs.addTab("Pinjaman Saya", new BorrowedBooksPanel(loggedInUser.getUserId()));
        tabs.addTab("Tagihan Denda", new FinesLedgerPanel(loggedInUser.getUserId()));

        add(tabs, BorderLayout.CENTER);
    }

    private void toggleTheme(JButton btnTheme) {
        ThemeManager.toggleTheme(this);
        btnTheme.setText(ThemeManager.getToggleLabel());
    }
}
