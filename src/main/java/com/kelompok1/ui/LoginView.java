package com.kelompok1.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.service.Services.UserService;
import com.kelompok1.model.Models.User;
import com.kelompok1.util.DesignSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginView extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblError;
    private UserService userService;

    public LoginView() {
        userService = new UserService();
        setTitle("Perpustakaan Freedom — Masuk");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setBackground(DesignSystem.SURFACE);

        // ═══════════════════════════════════════
        //  LEFT — Login Form
        // ═══════════════════════════════════════
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(DesignSystem.SURFACE_CONTAINER_LOWEST);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(DesignSystem.SURFACE_CONTAINER_LOWEST);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 48, 20, 48));

        // Brand badge
        JLabel lblBrand = new JLabel("AKADEMIK");
        lblBrand.setFont(DesignSystem.bodyFont(10f, Font.BOLD));
        lblBrand.setForeground(DesignSystem.PRIMARY);
        lblBrand.putClientProperty(FlatClientProperties.STYLE,
            "background: #fce8e6; foreground: #86000d; opaque: true");
        lblBrand.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        lblBrand.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Heading
        JLabel lblTitle = new JLabel("Perpustakaan Freedom");
        lblTitle.setFont(DesignSystem.displayFont(26f, Font.BOLD));
        lblTitle.setForeground(DesignSystem.ON_SURFACE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Masuk ke akun Anda untuk melanjutkan");
        lblSubtitle.setFont(DesignSystem.bodyFont(13f, Font.PLAIN));
        lblSubtitle.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Separator
        JSeparator divider = new JSeparator();
        divider.setForeground(DesignSystem.OUTLINE_VARIANT);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fields
        JLabel lblUser = new JLabel("Nama Pengguna");
        lblUser.setFont(DesignSystem.bodyFont(12f, Font.BOLD));
        lblUser.setForeground(DesignSystem.ON_SURFACE);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = new JTextField(20);
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Masukkan nama pengguna");
        txtUsername.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtUsername.putClientProperty(FlatClientProperties.STYLE, "margin: 8, 12, 8, 12; arc: 8");
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPass = new JLabel("Kata Sandi");
        lblPass.setFont(DesignSystem.bodyFont(12f, Font.BOLD));
        lblPass.setForeground(DesignSystem.ON_SURFACE);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField(20);
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Masukkan kata sandi");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true; margin: 8, 12, 8, 12; arc: 8");
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error label
        lblError = new JLabel(" ");
        lblError.setFont(DesignSystem.bodyFont(12f, Font.PLAIN));
        lblError.setForeground(DesignSystem.ERROR);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login button — primary red
        btnLogin = new JButton("Masuk");
        btnLogin.setFont(DesignSystem.bodyFont(14f, Font.BOLD));
        DesignSystem.applyPrimaryButton(btnLogin);
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnLogin.addActionListener(this::handleLogin);
        getRootPane().setDefaultButton(btnLogin);

        // Assemble form
        formPanel.add(lblBrand);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(lblTitle);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(lblSubtitle);
        formPanel.add(Box.createVerticalStrut(24));
        formPanel.add(divider);
        formPanel.add(Box.createVerticalStrut(24));
        formPanel.add(lblUser);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(txtUsername);
        formPanel.add(Box.createVerticalStrut(16));
        formPanel.add(lblPass);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(txtPassword);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(lblError);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(btnLogin);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 40, 0, 40);
        leftPanel.add(formPanel, gbc);

        // ═══════════════════════════════════════
        //  RIGHT — Brand panel
        // ═══════════════════════════════════════
        JPanel rightPanel = new JPanel() {
            private Image bgImage;
            {
                try {
                    java.net.URL imgUrl = getClass().getResource("/images/login_bg.png");
                    if (imgUrl != null) bgImage = new ImageIcon(imgUrl).getImage();
                } catch (Exception ignored) {}
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                if (bgImage != null) {
                    double pa = (double) getWidth() / getHeight();
                    double ia = (double) bgImage.getWidth(null) / bgImage.getHeight(null);
                    int w, h, x, y;
                    if (pa > ia) { w = getWidth(); h = (int)(getWidth() / ia); x = 0; y = (getHeight() - h) / 2; }
                    else         { h = getHeight(); w = (int)(getHeight() * ia); y = 0; x = (getWidth() - w) / 2; }
                    g2.drawImage(bgImage, x, y, w, h, this);
                    // Dark overlay for text readability
                    g2.setColor(new Color(20, 10, 10, 140));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    // Fallback gradient — institutional red
                    GradientPaint gp = new GradientPaint(
                        0, 0, DesignSystem.PRIMARY,
                        0, getHeight(), new Color(0x1a0003)
                    );
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }

                // Overlay text
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 220));
                g2.setFont(DesignSystem.displayFont(22f, Font.BOLD));
                String tagline = "Sistem Manajemen";
                String tagline2 = "Perpustakaan Terpadu";
                FontMetrics fm = g2.getFontMetrics();
                int cx = (getWidth() - fm.stringWidth(tagline)) / 2;
                g2.drawString(tagline, cx, getHeight() / 2 - 16);
                cx = (getWidth() - fm.stringWidth(tagline2)) / 2;
                g2.drawString(tagline2, cx, getHeight() / 2 + 20);

                g2.dispose();
            }
        };
        rightPanel.setBackground(DesignSystem.PRIMARY);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    private void handleLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Kolom input tidak boleh kosong.");
            return;
        }

        lblError.setText(" ");

        try {
            User user = userService.authenticate(username, password);
            if (user != null) {
                txtUsername.putClientProperty(FlatClientProperties.OUTLINE, null);
                txtPassword.putClientProperty(FlatClientProperties.OUTLINE, null);
                if ("Admin".equalsIgnoreCase(user.getRole())) {
                    new AdminDashboard(user).setVisible(true);
                } else {
                    new MemberDashboard(user).setVisible(true);
                }
                LoginView.this.dispose();
            } else {
                showError("Nama pengguna atau kata sandi salah.");
            }
        } catch (Exception ex) {
            showError("Kesalahan saat menghubungkan ke database.");
            ex.printStackTrace();
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        txtUsername.putClientProperty(FlatClientProperties.OUTLINE, "error");
        txtPassword.putClientProperty(FlatClientProperties.OUTLINE, "error");
    }
}
