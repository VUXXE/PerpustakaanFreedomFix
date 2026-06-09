package com.kelompok1.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.model.User;
import com.kelompok1.util.DesignSystem;

import javax.swing.*;
import java.awt.*;

/**
 * Modern floating capsule navigation sidebar.
 * Text-only buttons, larger sizes, and a bold brand-red background when selected.
 */
public class SidebarPanel extends JPanel {

    public interface SidebarListener {
        void onTabSelected(String tabName);
        void onLogout();
    }

    private final SidebarListener listener;
    private final SidebarNavButton btnDashboard;
    private final SidebarNavButton btnMembers;
    private final SidebarNavButton btnAddBooks;
    private final SidebarNavButton btnCheckout;
    private final SidebarNavButton btnFines;
    private final SidebarNavButton btnSettings;
    private final SidebarNavButton btnHelp;

    public SidebarPanel(User loggedInUser, SidebarListener listener) {
        this.listener = listener;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(240, 800));
        // Use soft surface background (light blue-gray) to make selected red cards pop
        setBackground(DesignSystem.SURFACE);
        // Matte right border with empty compound border for spacing
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
        btnMembers   = createNavButton("Anggota",        "Members");
        btnAddBooks  = createNavButton("Manajemen Buku", "Books");
        btnCheckout  = createNavButton("Transaksi",      "Transactions");
        btnFines     = createNavButton("Denda",          "Fines");
        btnSettings  = createNavButton("Pengaturan",     "Settings");
        btnHelp      = createNavButton("Bantuan",        "Help");

        group.add(btnDashboard);
        group.add(btnMembers);
        group.add(btnAddBooks);
        group.add(btnCheckout);
        group.add(btnFines);
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
        add(btnSettings);
        add(Box.createVerticalStrut(8));
        add(btnHelp);
        add(Box.createVerticalGlue());

        // Logout button — bottom-pinned, error red theme, matched to larger height
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

    /**
     * Programmatically sets the selected navigation tab in the sidebar.
     * Useful for search actions that auto-redirect to specific tables.
     */
    public void setSelectedTab(String tabName) {
        switch (tabName) {
            case "Dashboard"    -> btnDashboard.setSelected(true);
            case "Members"      -> btnMembers.setSelected(true);
            case "Books"        -> btnAddBooks.setSelected(true);
            case "Transactions" -> btnCheckout.setSelected(true);
            case "Fines"        -> btnFines.setSelected(true);
            case "Settings"     -> btnSettings.setSelected(true);
            case "Help"         -> btnHelp.setSelected(true);
        }
    }

    // ─────────────────────────────────────────────
    //  SIDEBAR NAV BUTTON
    // ─────────────────────────────────────────────
    private static class SidebarNavButton extends JToggleButton {

        SidebarNavButton(String label) {
            super(label);
            setFont(DesignSystem.bodyFont(14f, Font.PLAIN));
            setForeground(new Color(0x4a4a4a)); // Dark-medium gray text for unselected state
            setHorizontalAlignment(SwingConstants.LEFT);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); // Taller button size
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(true);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(true);
            putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
            putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; " +
                "margin: 12, 20, 12, 20; " + // Larger padding in button
                "selectedBackground: #86000d; " + // Brand red selection background
                "selectedForeground: #ffffff; " + // White text when selected
                "hoverBackground: #eaecf0; " +
                "font: +0"
            );
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isSelected()) {
                // Make text bold when selected (with guard to prevent layout loop)
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
