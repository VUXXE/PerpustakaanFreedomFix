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
    // CARD
    // ─────────────────────────────────────────────

    /** White elevated card with 16px arc and subtle outline border. */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        DesignSystem.applyCard(card);
        return card;
    }

    // ─────────────────────────────────────────────
    // KPI CARD
    // ─────────────────────────────────────────────

    public static JPanel createKPICard(String title, String value, String badgeText, boolean isPositive) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Title row (label-md)
        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblTitle.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        card.add(lblTitle, BorderLayout.NORTH);

        // Value (display-lg style)
        JLabel lblVal = new JLabel(value);
        lblVal.setFont(DesignSystem.displayFont(28f, Font.BOLD));
        lblVal.setForeground(DesignSystem.ON_SURFACE);
        card.add(lblVal, BorderLayout.CENTER);

        // Badge (only shown when badgeText is non-empty)
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

        // Row styling
        table.setRowHeight(44);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(DesignSystem.OUTLINE_VARIANT);
        table.setBackground(DesignSystem.SURFACE_CONTAINER_LOWEST);
        table.setForeground(DesignSystem.ON_SURFACE);
        table.setSelectionBackground(new Color(0x86000d, false) {
            { // 12% opacity tint of primary
            }
        });
        table.setFont(DesignSystem.bodyFont(13f, Font.PLAIN));
        table.setIntercellSpacing(new Dimension(0, 1));

        // Selection highlight — light red tint
        table.setSelectionBackground(new Color(0xFFEBEA));
        table.setSelectionForeground(DesignSystem.ON_SURFACE);

        // FlatLaf overrides
        table.putClientProperty(FlatClientProperties.STYLE,
            "rowHeight: 44; " +
            "showHorizontalLines: true; " +
            "showVerticalLines: false; " +
            "intercellSpacing: 0, 1; " +
            "selectionBackground: #ffebea; " +
            "selectionForeground: #191c1e"
        );

        // Header — uppercase bold, surface-container-low background
        JTableHeader header = table.getTableHeader();
        header.setBackground(DesignSystem.SURFACE_CONTAINER_LOW);
        header.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        header.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 44));
        header.putClientProperty(FlatClientProperties.STYLE,
            "height: 44; font: bold; " +
            "separatorColor: #e4beba; " +
            "bottomSeparatorColor: #e4beba"
        );

        // Uppercase renderer for header cells
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(LEFT);
            }
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

    public static void addFormRow(JPanel panel, GridBagConstraints gbc, String labelText, Component comp, int row) {
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
