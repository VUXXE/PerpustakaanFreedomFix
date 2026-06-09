package com.kelompok1.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.util.DesignSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Shared UI factory methods — all styled to the Academic Precision design system.
 */
public class UIUtils {

    // ─────────────────────────────────────────────
    // ROUNDED CARD  (custom-painted for proper arc + border)
    // ─────────────────────────────────────────────

    /**
     * Creates a white card panel with genuine 16px rounded corners and a
     * 1px rose-outline border, using custom Graphics2D painting.
     * FlatLaf's STYLE 'arc' on JPanel only clips the fill — the border still
     * appears rectangular, so we paint both ourselves.
     */
    public static JPanel createCardPanel() {
        return createCardPanel(new FlowLayout());
    }

    /** Same as {@link #createCardPanel()} but lets the caller set the layout. */
    public static JPanel createCardPanel(LayoutManager layout) {
        JPanel card = new JPanel(layout) {
            private static final int ARC = 16;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fill rounded background
                g2.setColor(getBackground());
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, ARC, ARC);
                g2.dispose();
                // Let Swing paint child components on top
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Draw 1px rounded outline instead of the default rectangular border
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DesignSystem.OUTLINE_VARIANT);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
                g2.dispose();
            }

            @Override
            public boolean isOpaque() {
                // Non-opaque so the parent SURFACE background shows through the
                // rounded corners instead of a white rectangle bleeding through
                return false;
            }
        };
        card.setBackground(DesignSystem.SURFACE_CONTAINER_LOWEST);
        return card;
    }

    // ─────────────────────────────────────────────
    // KPI CARD
    // ─────────────────────────────────────────────

    public static JPanel createKPICard(String title, String value, String badgeText, boolean isPositive) {
        JPanel card = createCardPanel(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Title row (label-md) — uppercase
        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblTitle.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        card.add(lblTitle, BorderLayout.NORTH);

        // Value — display-lg
        JLabel lblVal = new JLabel(value);
        lblVal.setFont(DesignSystem.displayFont(28f, Font.BOLD));
        lblVal.setForeground(DesignSystem.ON_SURFACE);
        card.add(lblVal, BorderLayout.CENTER);

        // Badge
        if (badgeText != null && !badgeText.isBlank()) {
            JLabel lblBadge = new JLabel(badgeText);
            lblBadge.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
            String badgeBg = isPositive ? "#e6f4ea" : "#fce8e6";
            String badgeFg = isPositive ? "#1e7e34" : "#ba1a1a";
            lblBadge.putClientProperty(FlatClientProperties.STYLE,
                "background: " + badgeBg + "; foreground: " + badgeFg + "; opaque: true");
            lblBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

            JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            south.setOpaque(false);
            south.add(lblBadge);
            card.add(south, BorderLayout.SOUTH);
        }

        return card;
    }

    // ─────────────────────────────────────────────
    // TABLE
    // ─────────────────────────────────────────────

    public static JTable createStyledTable(Object[][] data, String[] cols) {
        JTable table = new JTable(new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });

        table.setRowHeight(44);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(DesignSystem.OUTLINE_VARIANT);
        table.setBackground(DesignSystem.SURFACE_CONTAINER_LOWEST);
        table.setForeground(DesignSystem.ON_SURFACE);
        table.setFont(DesignSystem.bodyFont(13f, Font.PLAIN));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(0xFFEBEA));
        table.setSelectionForeground(DesignSystem.ON_SURFACE);

        table.putClientProperty(FlatClientProperties.STYLE,
            "rowHeight: 44; showHorizontalLines: true; showVerticalLines: false; " +
            "intercellSpacing: 0, 1; selectionBackground: #ffebea; selectionForeground: #191c1e"
        );

        // Header
        JTableHeader header = table.getTableHeader();
        header.setBackground(DesignSystem.SURFACE_CONTAINER_LOW);
        header.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        header.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 44));
        header.putClientProperty(FlatClientProperties.STYLE,
            "height: 44; font: bold; separatorColor: #e4beba; bottomSeparatorColor: #e4beba");

        // Uppercase renderer
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(LEFT); }
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                lbl.setText(val != null ? val.toString().toUpperCase() : "");
                lbl.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
                lbl.setForeground(DesignSystem.ON_SURFACE_VARIANT);
                lbl.setBackground(DesignSystem.SURFACE_CONTAINER_LOW);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, DesignSystem.OUTLINE_VARIANT),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                lbl.setOpaque(true);
                return lbl;
            }
        });

        return table;
    }

    // ─────────────────────────────────────────────
    // FORM HELPERS
    // ─────────────────────────────────────────────

    public static JTextField createFormTextField(String placeholder) {
        JTextField tf = new JTextField(20);
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        DesignSystem.applyInputField(tf);
        return tf;
    }

    public static void addFormRow(JPanel panel, GridBagConstraints gbc,
                                  String labelText, Component comp, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(DesignSystem.bodyFont(13f, Font.BOLD));
        label.setForeground(DesignSystem.ON_SURFACE);
        panel.add(label, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }

    // ─────────────────────────────────────────────
    // TOP-BOOKS LIST ITEM
    // ─────────────────────────────────────────────

    public static JPanel createTopBookItem(String title, String author) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, DesignSystem.OUTLINE_VARIANT),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(DesignSystem.bodyFont(13f, Font.BOLD));
        lblTitle.setForeground(DesignSystem.ON_SURFACE);

        JLabel lblAuthor = new JLabel(author);
        lblAuthor.setFont(DesignSystem.bodyFont(12f, Font.PLAIN));
        lblAuthor.setForeground(DesignSystem.ON_SURFACE_VARIANT);

        JLabel lblAvail = new JLabel("Tersedia");
        lblAvail.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblAvail.putClientProperty(FlatClientProperties.STYLE,
            "background: #e6f4ea; foreground: #1e7e34; opaque: true");
        lblAvail.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        item.add(lblTitle);
        item.add(Box.createVerticalStrut(2));
        item.add(lblAuthor);
        item.add(Box.createVerticalStrut(6));
        item.add(lblAvail);

        return item;
    }

    // ─────────────────────────────────────────────
    // PAGINATION
    // ─────────────────────────────────────────────

    public static JPanel createPaginationPanel(JButton btnPrev, JButton btnNext, JLabel lblPage) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        panel.setBackground(UIManager.getColor("Panel.background"));

        btnPrev.setText("← Sebelumnya");
        DesignSystem.applySecondaryButton(btnPrev);

        lblPage.setFont(DesignSystem.bodyFont(13f, Font.BOLD));
        lblPage.setForeground(DesignSystem.ON_SURFACE);

        btnNext.setText("Selanjutnya →");
        DesignSystem.applySecondaryButton(btnNext);

        panel.add(btnPrev);
        panel.add(lblPage);
        panel.add(btnNext);

        return panel;
    }
}
