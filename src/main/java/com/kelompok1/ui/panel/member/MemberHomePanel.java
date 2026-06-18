package com.kelompok1.ui.panel.member;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.dao.FineDAO;
import com.kelompok1.dao.TransactionDAO;
import com.kelompok1.model.User;
import com.kelompok1.util.DesignSystem;

import javax.swing.*;
import java.awt.*;

public class MemberHomePanel extends JPanel {

    private final User user;
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final FineDAO fineDAO = new FineDAO();

    private JLabel lblActiveLoans;
    private JLabel lblPendingFines;

    public MemberHomePanel(User user) {
        this.user = user;
        setLayout(new BorderLayout(0, 24));
        setBackground(DesignSystem.SURFACE);
        setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // Welcome Banner
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setOpaque(true);
        welcomePanel.setBackground(DesignSystem.PRIMARY);
        welcomePanel.putClientProperty(FlatClientProperties.STYLE, "arc: 16");
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        JLabel lblGreeting = new JLabel("Halo, " + user.getFullName() + "!");
        lblGreeting.setFont(DesignSystem.displayFont(28f, Font.BOLD));
        lblGreeting.setForeground(Color.WHITE);
        lblGreeting.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubGreeting = new JLabel("Selamat datang kembali di Perpustakaan Freedom. Selamat membaca!");
        lblSubGreeting.setFont(DesignSystem.bodyFont(14f, Font.PLAIN));
        lblSubGreeting.setForeground(new Color(255, 255, 255, 200));
        lblSubGreeting.setAlignmentX(Component.LEFT_ALIGNMENT);

        welcomePanel.add(lblGreeting);
        welcomePanel.add(Box.createVerticalStrut(8));
        welcomePanel.add(lblSubGreeting);

        add(welcomePanel, BorderLayout.NORTH);

        // Stats Container
        JPanel statsContainer = new JPanel(new GridLayout(1, 2, 24, 0));
        statsContainer.setOpaque(false);

        // Card 1: Active Loans
        JPanel cardLoans = createStatCard("Buku Sedang Dipinjam", new Color(0x005fb0));
        lblActiveLoans = new JLabel("0");
        lblActiveLoans.setFont(DesignSystem.displayFont(36f, Font.BOLD));
        lblActiveLoans.setForeground(DesignSystem.ON_SURFACE);
        cardLoans.add(lblActiveLoans, BorderLayout.CENTER);
        statsContainer.add(cardLoans);

        // Card 2: Pending Fines
        JPanel cardFines = createStatCard("Total Denda Belum Dibayar", new Color(0xba1a1a));
        lblPendingFines = new JLabel("Rp 0");
        lblPendingFines.setFont(DesignSystem.displayFont(36f, Font.BOLD));
        lblPendingFines.setForeground(DesignSystem.ON_SURFACE);
        cardFines.add(lblPendingFines, BorderLayout.CENTER);
        statsContainer.add(cardFines);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(statsContainer, BorderLayout.NORTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(DesignSystem.SURFACE_CONTAINER_LOW);
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 16");
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(DesignSystem.bodyFont(14f, Font.PLAIN));
        lblTitle.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        card.add(lblTitle, BorderLayout.NORTH);

        return card;
    }

    public void refreshData() {
        int activeLoans = transactionDAO.getActiveLoansCount(user.getUserId());
        double pendingFines = fineDAO.getTotalPendingFeesByUser(user.getUserId());

        lblActiveLoans.setText(String.valueOf(activeLoans));
        lblPendingFines.setText(String.format("Rp %,.0f", pendingFines));
    }
}
