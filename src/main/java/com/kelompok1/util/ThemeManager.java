package com.kelompok1.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Centralized theme management for FlatLaf light/dark mode switching.
 * Reusable across AdminDashboard, MemberDashboard, and any future views.
 */
public class ThemeManager {

    private static boolean isDarkMode = false;

    /**
     * Returns whether dark mode is currently active.
     */
    public static boolean isDarkMode() {
        return isDarkMode;
    }

    /**
     * Toggles between FlatLaf light and dark themes, then refreshes the
     * component tree of the given root window.
     *
     * @param rootWindow the top-level window whose component tree should be refreshed
     */
    public static void toggleTheme(Window rootWindow) {
        isDarkMode = !isDarkMode;
        applyTheme(rootWindow);
    }

    /**
     * Applies the current theme (light or dark) to the given root window.
     */
    public static void applyTheme(Window rootWindow) {
        EventQueue.invokeLater(() -> {
            try {
                if (isDarkMode) {
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                } else {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                }
                SwingUtilities.updateComponentTreeUI(rootWindow);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * Returns the localized label for the current theme toggle button.
     */
    public static String getToggleLabel() {
        return isDarkMode ? "Mode Terang" : "Mode Gelap";
    }
}
