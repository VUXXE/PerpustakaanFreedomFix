package com.kelompok1.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.model.User;
import com.kelompok1.util.DesignSystem;

import javax.swing.*;
import java.awt.*;

public class MemberSidebarPanel extends JPanel {

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
