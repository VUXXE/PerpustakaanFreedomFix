package com.kelompok1.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.service.UserService;
import com.kelompok1.model.User;

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
        setTitle("Aplikasi Perpustakaan - Masuk");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setBackground(Color.WHITE);

        // --- Left Panel: Form ---
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Title Area
        JLabel lblLogInto = new JLabel("Masuk ke");
        lblLogInto.putClientProperty(FlatClientProperties.STYLE, "font: -1; foreground: $Label.disabledForeground");
        lblLogInto.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("Perpustakaan Freedom");
        lblTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +8; foreground: #000000");
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input Fields
        txtUsername = new JTextField(20);
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nama Pengguna");
        txtUsername.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtUsername.putClientProperty(FlatClientProperties.STYLE, "margin: 8, 12, 8, 12; arc: 10");
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        
        txtPassword = new JPasswordField(20);
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Kata Sandi");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true; margin: 8, 12, 8, 12; arc: 10");
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        lblError = new JLabel(" ");
        lblError.putClientProperty(FlatClientProperties.STYLE, "foreground: $Component.error.focusedBorderColor; font: bold -1");
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login Button
        btnLogin = new JButton("Masuk");
        btnLogin.putClientProperty(FlatClientProperties.STYLE,
            "background: $Component.accentColor; " +
            "foreground: #ffffff; " +
            "borderWidth: 0; " +
            "focusWidth: 0; " +
            "innerFocusWidth: 0; " +
            "margin: 10, 20, 10, 20; " +
            "arc: 10; " +
            "font: bold");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLogin.addActionListener(this::handleLogin);
        getRootPane().setDefaultButton(btnLogin);

        // Assemble Form
        formPanel.add(lblLogInto);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(lblTitle);
        formPanel.add(Box.createVerticalStrut(30));
        formPanel.add(txtUsername);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(txtPassword);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(lblError);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(btnLogin);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(0, 80, 0, 80);
        leftPanel.add(formPanel, gbc);

        // --- Right Panel: Image ---
        JPanel rightPanel = new JPanel() {
            private Image bgImage;
            {
                try {
                    java.net.URL imgUrl = getClass().getResource("/images/login_bg.png");
                    if (imgUrl != null) {
                        bgImage = new ImageIcon(imgUrl).getImage();
                    } else {
                        System.err.println("Background image not found in resources!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    
                    double panelAspect = (double) getWidth() / getHeight();
                    double imgAspect = (double) bgImage.getWidth(null) / bgImage.getHeight(null);
                    
                    int w, h, x, y;
                    if (panelAspect > imgAspect) {
                        w = getWidth();
                        h = (int) (getWidth() / imgAspect);
                        x = 0;
                        y = (getHeight() - h) / 2;
                    } else {
                        h = getHeight();
                        w = (int) (getHeight() * imgAspect);
                        y = 0;
                        x = (getWidth() - w) / 2;
                    }
                    g2.drawImage(bgImage, x, y, w, h, this);
                    g2.dispose();
                } else {
                    Graphics2D g2 = (Graphics2D) g;
                    java.awt.GradientPaint gp = new java.awt.GradientPaint(0, 0, new Color(211, 47, 47), 0, getHeight(), new Color(20, 20, 20));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

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

        // Disable button and show loading state
        btnLogin.setEnabled(false);
        btnLogin.setText("Masuk...");
        lblError.setText(" ");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return userService.authenticate(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
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
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Masuk");
                    }
                } catch (Exception ex) {
                    showError("Kesalahan saat menghubungkan ke database.");
                    ex.printStackTrace();
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Masuk");
                }
            }
        };
        worker.execute();
    }

    private void showError(String message) {
        lblError.setText(message);
        txtUsername.putClientProperty(FlatClientProperties.OUTLINE, "error");
        txtPassword.putClientProperty(FlatClientProperties.OUTLINE, "error");
    }
}


