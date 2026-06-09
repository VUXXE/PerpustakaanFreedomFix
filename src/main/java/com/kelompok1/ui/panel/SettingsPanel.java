package com.kelompok1.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.service.SettingsService;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final SettingsService settingsService;
    private JSpinner spinDuration;
    private JSpinner spinMaxLimit;
    private JSpinner spinFineRate;
    private JButton btnSave;

    public SettingsPanel() {
        this.settingsService = new SettingsService();
        
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        initPanel();
    }

    private void initPanel() {
        // --- HEADER PANEL (NORTH) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIManager.getColor("Panel.background"));
        
        JLabel title = new JLabel("Pengaturan Sistem");
        title.putClientProperty(FlatClientProperties.STYLE, "font: bold +6");
        headerPanel.add(title, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);

        // --- CONTENT PANEL (CENTER) ---
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(UIManager.getColor("Panel.background"));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Card Panel for Settings Form
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 20; border: 1,1,1,1, $Component.borderColor;");
        card.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.weightx = 1.0;

        // Form Fields
        spinDuration = new JSpinner(new SpinnerNumberModel(7, 1, 90, 1));
        spinDuration.putClientProperty(FlatClientProperties.STYLE, "margin: 5, 8, 5, 8; arc: 8");

        spinMaxLimit = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        spinMaxLimit.putClientProperty(FlatClientProperties.STYLE, "margin: 5, 8, 5, 8; arc: 8");

        spinFineRate = new JSpinner(new SpinnerNumberModel(5000, 0, 100000, 500));
        spinFineRate.putClientProperty(FlatClientProperties.STYLE, "margin: 5, 8, 5, 8; arc: 8");

        // Layout rows
        int row = 0;
        
        // Aturan Peminjaman Section Header
        JLabel lblSection1 = new JLabel("Aturan Peminjaman");
        lblSection1.putClientProperty(FlatClientProperties.STYLE, "font: bold +2; foreground: $Component.accentColor");
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        card.add(lblSection1, gbc);
        
        gbc.gridwidth = 1;
        addFormRow(card, gbc, "Durasi Peminjaman (Hari)*", spinDuration, row++);
        addFormRow(card, gbc, "Batas Maksimal Buku (Eksemplar)*", spinMaxLimit, row++);
        
        // Separator
        JSeparator sep = new JSeparator();
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 15, 15, 15);
        card.add(sep, gbc);
        
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 15, 10, 15);

        // Denda Section Header
        JLabel lblSection2 = new JLabel("Denda & Keuangan");
        lblSection2.putClientProperty(FlatClientProperties.STYLE, "font: bold +2; foreground: $Component.accentColor");
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        card.add(lblSection2, gbc);

        gbc.gridwidth = 1;
        
        // Fine rate panel (with Rp prefix)
        JPanel finePanel = new JPanel(new BorderLayout(5, 0));
        finePanel.setBackground(Color.WHITE);
        JLabel lblRp = new JLabel("Rp");
        lblRp.putClientProperty(FlatClientProperties.STYLE, "font: bold");
        finePanel.add(lblRp, BorderLayout.WEST);
        finePanel.add(spinFineRate, BorderLayout.CENTER);
        
        addFormRow(card, gbc, "Tarif Denda Harian*", finePanel, row++);

        // Save Button Row
        btnSave = new JButton("Simpan Pengaturan");
        btnSave.putClientProperty(FlatClientProperties.STYLE, "background: $Component.accentColor; foreground: #ffffff; font: bold; arc: 10; margin: 8, 20, 8, 20");
        btnSave.addActionListener(e -> saveSettings());
        
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(20, 15, 10, 15);
        card.add(btnSave, gbc);

        mainContent.add(card);
        add(new JScrollPane(mainContent), BorderLayout.CENTER);

        // Initial Load
        loadSettings();
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String labelText, Component comp, int row) {
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Label
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        JLabel label = new JLabel(labelText);
        label.putClientProperty(FlatClientProperties.STYLE, "font: bold");
        panel.add(label, gbc);

        // Component
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    private void loadSettings() {
        btnSave.setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int duration;
            int maxLimit;
            int fineRate;

            @Override
            protected Void doInBackground() {
                duration = Integer.parseInt(settingsService.getSetting("borrow_duration", "7"));
                maxLimit = Integer.parseInt(settingsService.getSetting("max_borrow_limit", "3"));
                fineRate = (int) Double.parseDouble(settingsService.getSetting("fine_rate", "5000"));
                return null;
            }

            @Override
            protected void done() {
                spinDuration.setValue(duration);
                spinMaxLimit.setValue(maxLimit);
                spinFineRate.setValue(fineRate);
                btnSave.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void saveSettings() {
        btnSave.setEnabled(false);
        int duration = (Integer) spinDuration.getValue();
        int maxLimit = (Integer) spinMaxLimit.getValue();
        int fineRate = (Integer) spinFineRate.getValue();

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                boolean success = settingsService.setSetting("borrow_duration", String.valueOf(duration));
                success &= settingsService.setSetting("max_borrow_limit", String.valueOf(maxLimit));
                success &= settingsService.setSetting("fine_rate", String.valueOf(fineRate));
                return success;
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(SettingsPanel.this, "Pengaturan berhasil disimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(SettingsPanel.this, "Gagal menyimpan pengaturan.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(SettingsPanel.this, "Terjadi kesalahan: " + ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnSave.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
