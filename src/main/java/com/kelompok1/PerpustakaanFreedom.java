package com.kelompok1;

import com.formdev.flatlaf.FlatLightLaf;
import com.kelompok1.config.DatabaseHelper;
import com.kelompok1.ui.LoginView;
import com.kelompok1.util.DesignSystem;

import javax.swing.*;

public class PerpustakaanFreedom {

    public static void main(String[] args) {
        // 1. Initialize Database
        DatabaseHelper.initializeDatabase();

        // 2. Apply FlatLaf base look-and-feel first (before UIManager overrides)
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf: " + ex.getMessage());
        }

        // 3. Apply Academic Precision design-system tokens on top of FlatLaf
        DesignSystem.applyTheme();

        // 4. Launch UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}
