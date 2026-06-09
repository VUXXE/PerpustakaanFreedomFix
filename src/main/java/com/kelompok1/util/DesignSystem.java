package com.kelompok1.util;

import javax.swing.*;
import java.awt.*;

/**
 * Academic Precision Design System
 * Central registry for all design tokens, UIManager setup, and component styling helpers.
 */
public class DesignSystem {

    // ─────────────────────────────────────────────
    // COLOR TOKENS
    // ─────────────────────────────────────────────
    public static final Color PRIMARY             = new Color(0x86000d);
    public static final Color PRIMARY_CONTAINER   = new Color(0xaf101a);
    public static final Color ON_PRIMARY          = Color.WHITE;

    public static final Color SECONDARY           = new Color(0x5c5f61);
    public static final Color ON_SECONDARY        = Color.WHITE;
    public static final Color SECONDARY_CONTAINER = new Color(0xdee0e2);

    public static final Color TERTIARY            = new Color(0x2c368e);
    public static final Color ON_TERTIARY         = Color.WHITE;

    public static final Color ERROR               = new Color(0xba1a1a);
    public static final Color ON_ERROR            = Color.WHITE;
    public static final Color ERROR_CONTAINER     = new Color(0xffdad6);

    public static final Color SURFACE                    = new Color(0xf7f9fb);
    public static final Color SURFACE_DIM                = new Color(0xd8dadc);
    public static final Color SURFACE_CONTAINER_LOWEST   = new Color(0xffffff);
    public static final Color SURFACE_CONTAINER_LOW      = new Color(0xf2f4f6);
    public static final Color SURFACE_CONTAINER          = new Color(0xeceef0);
    public static final Color SURFACE_CONTAINER_HIGH     = new Color(0xe6e8ea);
    public static final Color SURFACE_CONTAINER_HIGHEST  = new Color(0xe0e3e5);

    public static final Color ON_SURFACE         = new Color(0x191c1e);
    public static final Color ON_SURFACE_VARIANT = new Color(0x5b403d);

    public static final Color OUTLINE         = new Color(0x8f6f6c);
    public static final Color OUTLINE_VARIANT = new Color(0xe4beba);

    public static final Color SUCCESS = new Color(0x006619);

    // ─────────────────────────────────────────────
    // SHAPE TOKENS  (pixels)
    // ─────────────────────────────────────────────
    public static final int RADIUS_SM      = 4;
    public static final int RADIUS_DEFAULT = 8;
    public static final int RADIUS_MD      = 12;
    public static final int RADIUS_LG      = 16;
    public static final int RADIUS_XL      = 24;

    // ─────────────────────────────────────────────
    // TYPOGRAPHY  (system font fallbacks)
    // ─────────────────────────────────────────────
    // Tries Plus Jakarta Sans → Calibri → Dialog (always present)
    private static final String DISPLAY_FAMILY = pickFont("Plus Jakarta Sans", "Calibri", "Verdana", "Dialog");
    // Tries Inter → Segoe UI → SansSerif
    private static final String BODY_FAMILY    = pickFont("Inter", "Segoe UI", "Helvetica Neue", "SansSerif");

    private static String pickFont(String... candidates) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        java.util.Set<String> available = new java.util.HashSet<>(
            java.util.Arrays.asList(ge.getAvailableFontFamilyNames())
        );
        for (String name : candidates) {
            if (available.contains(name)) return name;
        }
        return "Dialog";
    }

    /** Display / heading font — Plus Jakarta Sans Bold (or best available). */
    public static Font displayFont(float sizePt, int style) {
        return new Font(DISPLAY_FAMILY, style, (int) sizePt);
    }

    /** Body / data font — Inter (or best available). */
    public static Font bodyFont(float sizePt, int style) {
        return new Font(BODY_FAMILY, style, (int) sizePt);
    }

    // ─────────────────────────────────────────────
    // THEME APPLICATION  (call once from main)
    // ─────────────────────────────────────────────
    public static void applyTheme() {
        // Arc / rounding
        UIManager.put("Component.arc",         RADIUS_DEFAULT);
        UIManager.put("Button.arc",            RADIUS_DEFAULT);
        UIManager.put("TextComponent.arc",     RADIUS_DEFAULT);
        UIManager.put("ProgressBar.arc",       RADIUS_DEFAULT);
        UIManager.put("CheckBox.arc",          RADIUS_SM);

        // Component behavior
        UIManager.put("Component.arrowType",           "chevron");
        UIManager.put("TabbedPane.showTabSeparators",  true);
        UIManager.put("ScrollBar.showButtons",         false);
        UIManager.put("ScrollBar.width",               8);
        UIManager.put("Button.innerFocusWidth",        0);
        UIManager.put("Component.focusWidth",          2);

        // Accent / brand color  →  FlatLaf uses this for selection, highlights, etc.
        UIManager.put("Component.accentColor",          PRIMARY);

        // Surface colors
        UIManager.put("Panel.background",               SURFACE);
        UIManager.put("RootPane.background",            SURFACE);
        UIManager.put("OptionPane.background",          SURFACE_CONTAINER_LOWEST);
        UIManager.put("Dialog.background",              SURFACE_CONTAINER_LOWEST);

        // List / Table colors
        UIManager.put("List.background",                SURFACE_CONTAINER_LOWEST);
        UIManager.put("Table.background",               SURFACE_CONTAINER_LOWEST);
        UIManager.put("Table.alternateRowColor",        SURFACE_CONTAINER_LOW);
        UIManager.put("TableHeader.background",         SURFACE_CONTAINER_LOW);
        UIManager.put("TableHeader.separatorColor",     OUTLINE_VARIANT);

        // Text colors
        UIManager.put("Label.foreground",               ON_SURFACE);
        UIManager.put("Label.disabledForeground",       ON_SURFACE_VARIANT);
        UIManager.put("TextField.foreground",           ON_SURFACE);
        UIManager.put("TextArea.foreground",            ON_SURFACE);

        // Border/outline
        UIManager.put("Component.borderColor",          OUTLINE_VARIANT);
        UIManager.put("Component.disabledBorderColor",  SURFACE_CONTAINER_HIGH);

        // Global default font → Inter body-md (14px)
        UIManager.put("defaultFont", bodyFont(13f, Font.PLAIN));
    }

    // ─────────────────────────────────────────────
    // COMPONENT STYLING HELPERS
    // ─────────────────────────────────────────────

    /** Solid red primary action button. */
    public static void applyPrimaryButton(JButton btn) {
        btn.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
            "background: #86000d; " +
            "foreground: #ffffff; " +
            "borderWidth: 0; focusWidth: 0; innerFocusWidth: 0; " +
            "arc: 8; margin: 6, 18, 6, 18; font: bold"
        );
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** White background with outline border — secondary action. */
    public static void applySecondaryButton(JButton btn) {
        btn.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
            "background: #ffffff; " +
            "foreground: #191c1e; " +
            "borderColor: #e4beba; borderWidth: 1; " +
            "focusWidth: 0; innerFocusWidth: 0; " +
            "arc: 8; margin: 6, 18, 6, 18"
        );
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Danger/delete — error red. */
    public static void applyDangerButton(JButton btn) {
        btn.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
            "background: #ba1a1a; " +
            "foreground: #ffffff; " +
            "borderWidth: 0; focusWidth: 0; innerFocusWidth: 0; " +
            "arc: 8; margin: 6, 18, 6, 18; font: bold"
        );
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** White card with 16px arc and subtle outline shadow. */
    public static void applyCard(JPanel panel) {
        panel.setBackground(SURFACE_CONTAINER_LOWEST);
        panel.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
            "arc: 16; border: 1, 1, 1, 1, #e4beba"
        );
    }

    /** Standard input field — 8px arc, standard border. */
    public static void applyInputField(JTextField tf) {
        tf.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
            "margin: 6, 10, 6, 10; arc: 8"
        );
    }
}
