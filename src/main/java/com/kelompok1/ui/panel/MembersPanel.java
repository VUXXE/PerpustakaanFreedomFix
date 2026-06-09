package com.kelompok1.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.model.User;
import com.kelompok1.service.UserService;
import com.kelompok1.util.DesignSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modern side-by-side Members Panel.
 * Displays the member directory list on the left and a live add/edit form card on the right.
 */
public class MembersPanel extends JPanel {
    private final UserService userService;
    private DefaultTableModel usersTableModel;
    private JTable usersTable;
    private JLabel usersTitleLabel;
    
    private int currentPage = 1;
    private final int pageSize = 15;
    private JButton btnPrevPage;
    private JButton btnNextPage;
    private JLabel lblPage;
    private String currentSearchQuery = null;

    // Form fields (right panel)
    private JLabel lblFormTitle;
    private JLabel lblFormSubtitle;
    private JTextField txtFullName;
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JPasswordField txtPassword;
    private JLabel lblPass;
    private JComboBox<String> comboRole;
    private JComboBox<String> comboStatus;
    private JButton btnSave;
    private JLabel lblError;

    private User editingUser = null;

    public MembersPanel() {
        this.userService = new UserService();
        
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        initPanel();
    }

    private void initPanel() {
        // ─── 1. HEADER PANEL (NORTH) ────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIManager.getColor("Panel.background"));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        usersTitleLabel = new JLabel("Direktori Anggota (Memuat...)");
        usersTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +6");
        headerPanel.add(usersTitleLabel, BorderLayout.WEST);
        
        // Controls (Delete only — Add/Edit is handled inline in the form)
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setBackground(UIManager.getColor("Panel.background"));
        
        JButton btnDeleteUser = new JButton("Hapus");
        DesignSystem.applyDangerButton(btnDeleteUser);
        btnDeleteUser.setEnabled(false);
        controlsPanel.add(btnDeleteUser);
        
        headerPanel.add(controlsPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
        
        // ─── 2. CONTENT PANEL (CENTER) ──────────────────────────────────────
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // LEFT COLUMN: Table List
        String[] cols = {"ID Pengguna", "Kode Anggota", "Nama Pengguna", "Nama Lengkap", "Email", "Telepon", "Peran", "Status"};
        usersTableModel = new DefaultTableModel(new Object[][]{}, cols) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        usersTable = UIUtils.createStyledTable(new Object[][]{}, cols);
        usersTable.setModel(usersTableModel);
        
        // Hide User ID column
        usersTable.getColumnModel().getColumn(0).setMinWidth(0);
        usersTable.getColumnModel().getColumn(0).setMaxWidth(0);
        usersTable.getColumnModel().getColumn(0).setWidth(0);
        
        usersTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            boolean hasSelection = selectedRow != -1;
            btnDeleteUser.setEnabled(hasSelection);
            
            if (hasSelection && !e.getValueIsAdjusting()) {
                int userId = (Integer) usersTableModel.getValueAt(selectedRow, 0);
                loadUserIntoForm(userId);
            }
        });
        
        btnDeleteUser.addActionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow == -1) return;
            int userId = (Integer) usersTableModel.getValueAt(selectedRow, 0);
            String nameStr = (String) usersTableModel.getValueAt(selectedRow, 3);
            
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin menghapus anggota \"" + nameStr + "\"?\nTindakan ini tidak dapat dibatalkan.",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                SwingWorker<Boolean, Void> deleteWorker = new SwingWorker<>() {
                    private String errorMessage = null;
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        try {
                            return userService.deleteUser(userId);
                        } catch (java.sql.SQLException ex) {
                            if (ex.getSQLState() != null && ex.getSQLState().startsWith("23")) {
                                errorMessage = "Tidak dapat menghapus anggota ini karena mereka memiliki catatan transaksi perpustakaan yang aktif.";
                            } else {
                                errorMessage = ex.getMessage();
                            }
                            throw ex;
                        }
                    }
                    @Override
                    protected void done() {
                        try {
                            boolean success = get();
                            if (success) {
                                JOptionPane.showMessageDialog(MembersPanel.this, "Anggota berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                                loadUsersData(currentSearchQuery);
                                clearForm();
                            } else {
                                JOptionPane.showMessageDialog(MembersPanel.this, "Gagal menghapus anggota.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            String msg = errorMessage != null ? errorMessage : "Terjadi kesalahan: " + ex.getMessage();
                            JOptionPane.showMessageDialog(MembersPanel.this, msg, "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                deleteWorker.execute();
            }
        });
        
        JScrollPane tableScroll = new JScrollPane(usersTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        
        gbc.gridx = 0; gbc.weightx = 0.65; gbc.insets = new Insets(0, 0, 0, 15);
        contentPanel.add(tableScroll, gbc);

        // RIGHT COLUMN: Form sidebar panel inside a rounded card
        JPanel formCard = UIUtils.createCardPanel(new BorderLayout());
        formCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        formCard.setPreferredSize(new Dimension(340, 500));
        
        // Form Header
        JPanel formHeader = new JPanel(new BorderLayout());
        formHeader.setOpaque(false);
        formHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        
        lblFormTitle = new JLabel("Daftar Anggota Baru");
        lblFormTitle.setFont(DesignSystem.displayFont(13f, Font.BOLD));
        lblFormTitle.setForeground(DesignSystem.ON_SURFACE);
        
        lblFormSubtitle = new JLabel("Kode Anggota akan dibuat otomatis.");
        lblFormSubtitle.setFont(DesignSystem.bodyFont(10f, Font.PLAIN));
        lblFormSubtitle.setForeground(UIManager.getColor("Label.disabledForeground"));
        
        JPanel titleWrapper = new JPanel(new GridLayout(2, 1, 2, 2));
        titleWrapper.setOpaque(false);
        titleWrapper.add(lblFormTitle);
        titleWrapper.add(lblFormSubtitle);
        formHeader.add(titleWrapper, BorderLayout.CENTER);
        
        // Add "+ Baru" button to reset form
        JButton btnReset = new JButton("+ Baru");
        DesignSystem.applySecondaryButton(btnReset);
        btnReset.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        btnReset.setMargin(new Insets(2, 8, 2, 8));
        btnReset.addActionListener(e -> clearForm());
        formHeader.add(btnReset, BorderLayout.EAST);
        
        formCard.add(formHeader, BorderLayout.NORTH);

        // Form Fields (stacked vertically inside JScrollPane)
        txtFullName = UIUtils.createFormTextField("misal: John Doe");
        txtUsername = UIUtils.createFormTextField("misal: johndoe");
        txtEmail    = UIUtils.createFormTextField("misal: johndoe@email.com");
        txtPhone    = UIUtils.createFormTextField("misal: 08123456789");
        
        txtPassword = new JPasswordField(20);
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true; margin: 6, 10, 6, 10; arc: 8");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Kata Sandi");

        comboRole = new JComboBox<>(new String[]{"Member", "Admin"});
        comboRole.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        comboRole.setPreferredSize(new Dimension(200, 36));

        comboStatus = new JComboBox<>(new String[]{"Active", "Suspended"});
        comboStatus.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        comboStatus.setPreferredSize(new Dimension(200, 36));

        JPanel formFieldsPanel = new JPanel(new GridBagLayout());
        formFieldsPanel.setOpaque(false);
        GridBagConstraints fGbc = new GridBagConstraints();
        fGbc.fill = GridBagConstraints.HORIZONTAL;
        fGbc.insets = new Insets(4, 0, 4, 0);
        fGbc.weightx = 1.0;
        
        int r = 0;
        
        fGbc.gridy = r++;
        JLabel lblName = new JLabel("Nama Lengkap*");
        lblName.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblName.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formFieldsPanel.add(lblName, fGbc);
        fGbc.gridy = r++; formFieldsPanel.add(txtFullName, fGbc);
        
        fGbc.gridy = r++;
        JLabel lblUser = new JLabel("Nama Pengguna*");
        lblUser.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblUser.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formFieldsPanel.add(lblUser, fGbc);
        fGbc.gridy = r++; formFieldsPanel.add(txtUsername, fGbc);
        
        fGbc.gridy = r++;
        JLabel lblMail = new JLabel("Email*");
        lblMail.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblMail.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formFieldsPanel.add(lblMail, fGbc);
        fGbc.gridy = r++; formFieldsPanel.add(txtEmail, fGbc);
        
        fGbc.gridy = r++;
        JLabel lblPh = new JLabel("Nomor Telepon");
        lblPh.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblPh.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formFieldsPanel.add(lblPh, fGbc);
        fGbc.gridy = r++; formFieldsPanel.add(txtPhone, fGbc);
        
        fGbc.gridy = r++;
        lblPass = new JLabel("Kata Sandi*");
        lblPass.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblPass.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formFieldsPanel.add(lblPass, fGbc);
        fGbc.gridy = r++; formFieldsPanel.add(txtPassword, fGbc);
        
        fGbc.gridy = r++;
        JLabel lblRole = new JLabel("Peran Sistem");
        lblRole.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblRole.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formFieldsPanel.add(lblRole, fGbc);
        fGbc.gridy = r++; formFieldsPanel.add(comboRole, fGbc);
        
        fGbc.gridy = r++;
        JLabel lblStatus = new JLabel("Status Akun");
        lblStatus.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblStatus.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formFieldsPanel.add(lblStatus, fGbc);
        fGbc.gridy = r++; formFieldsPanel.add(comboStatus, fGbc);
        
        fGbc.gridy = r++; fGbc.weighty = 1.0; formFieldsPanel.add(Box.createGlue(), fGbc);

        JScrollPane formScroll = new JScrollPane(formFieldsPanel);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formCard.add(formScroll, BorderLayout.CENTER);

        // Form Footer Buttons
        JPanel formFooter = new JPanel(new BorderLayout(0, 8));
        formFooter.setOpaque(false);
        formFooter.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        lblError = new JLabel(" ");
        lblError.putClientProperty(FlatClientProperties.STYLE, "foreground: $Component.error.focusedBorderColor; font: bold -1");
        formFooter.add(lblError, BorderLayout.NORTH);
        
        btnSave = new JButton("Simpan Anggota");
        DesignSystem.applyPrimaryButton(btnSave);
        formFooter.add(btnSave, BorderLayout.CENTER);
        
        btnSave.addActionListener(e -> saveUser());

        formCard.add(formFooter, BorderLayout.SOUTH);

        gbc.gridx = 1; gbc.weightx = 0.0; gbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(formCard, gbc);

        add(contentPanel, BorderLayout.CENTER);
        
        // ─── 3. PAGINATION PANEL (SOUTH) ────────────────────────────────────
        btnPrevPage = new JButton();
        btnNextPage = new JButton();
        lblPage = new JLabel("Halaman 1");
        
        btnPrevPage.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadUsersData(currentSearchQuery);
            }
        });
        
        btnNextPage.addActionListener(e -> {
            currentPage++;
            loadUsersData(currentSearchQuery);
        });
        
        add(UIUtils.createPaginationPanel(btnPrevPage, btnNextPage, lblPage), BorderLayout.SOUTH);
        
        loadUsersData(null);
    }
    
    public void setSearchQuery(String query) {
        this.currentSearchQuery = query;
        currentPage = 1;
        loadUsersData(query);
    }

    private void loadUsersData(String searchQuery) {
        usersTitleLabel.setText("Direktori Anggota (Memuat...)");
        usersTableModel.setRowCount(0);
        if (btnPrevPage != null) btnPrevPage.setEnabled(false);
        if (btnNextPage != null) btnNextPage.setEnabled(false);
        
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() {
                if (searchQuery == null || searchQuery.trim().isEmpty()) {
                    return userService.getAllUsers(currentPage, pageSize);
                } else {
                    return userService.searchUsers(searchQuery.trim(), currentPage, pageSize);
                }
            }
            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    for (User u : users) {
                        usersTableModel.addRow(new Object[]{
                            u.getUserId(), u.getMemberCode(), u.getUsername(), u.getFullName(),
                            u.getEmail(), u.getPhone(), u.getRole(), u.getStatus()
                        });
                    }
                    if (lblPage != null) {
                        lblPage.setText("Halaman " + currentPage);
                        btnPrevPage.setEnabled(currentPage > 1);
                        btnNextPage.setEnabled(users.size() == pageSize);
                    }
                    usersTitleLabel.setText("Direktori Anggota (" + usersTableModel.getRowCount() + " data ditampilkan)");
                } catch (Exception e) {
                    e.printStackTrace();
                    usersTitleLabel.setText("Direktori Anggota (Gagal memuat data)");
                }
            }
        };
        worker.execute();
    }

    private void loadUserIntoForm(int userId) {
        SwingWorker<User, Void> fetchWorker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return userService.getUserById(userId);
            }
            @Override
            protected void done() {
                try {
                    User u = get();
                    if (u != null) {
                        editingUser = u;
                        lblFormTitle.setText("Ubah Detail Anggota");
                        lblFormSubtitle.setText("Kode Anggota: " + u.getMemberCode());
                        lblFormSubtitle.setForeground(DesignSystem.PRIMARY);
                        
                        txtFullName.setText(u.getFullName());
                        txtUsername.setText(u.getUsername());
                        txtEmail.setText(u.getEmail());
                        txtPhone.setText(u.getPhone());
                        txtPassword.setText("");
                        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Kosongkan jika tidak ingin diubah");
                        lblPass.setText("Kata Sandi (Opsional)");
                        comboRole.setSelectedItem(u.getRole());
                        comboStatus.setSelectedItem(u.getStatus());
                        
                        btnSave.setText("Simpan Perubahan");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        fetchWorker.execute();
    }

    private void clearForm() {
        editingUser = null;
        lblFormTitle.setText("Daftar Anggota Baru");
        lblFormSubtitle.setText("Kode Anggota akan dibuat otomatis.");
        lblFormSubtitle.setForeground(UIManager.getColor("Label.disabledForeground"));
        
        txtFullName.setText("");
        txtUsername.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtPassword.setText("");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Kata Sandi");
        lblPass.setText("Kata Sandi*");
        comboRole.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        
        btnSave.setText("Simpan Anggota");
        usersTable.clearSelection();
    }

    private void saveUser() {
        String usernameStr = txtUsername.getText().trim();
        String passwordStr = new String(txtPassword.getPassword()).trim();
        String fullNameStr = txtFullName.getText().trim();
        String emailStr = txtEmail.getText().trim();
        String phoneStr = txtPhone.getText().trim();

        boolean isValid = true;
        if (usernameStr.isEmpty()) { txtUsername.putClientProperty(FlatClientProperties.OUTLINE, "error"); isValid = false; }
        else { txtUsername.putClientProperty(FlatClientProperties.OUTLINE, null); }

        if (editingUser == null && passwordStr.isEmpty()) { txtPassword.putClientProperty(FlatClientProperties.OUTLINE, "error"); isValid = false; }
        else { txtPassword.putClientProperty(FlatClientProperties.OUTLINE, null); }

        if (fullNameStr.isEmpty()) { txtFullName.putClientProperty(FlatClientProperties.OUTLINE, "error"); isValid = false; }
        else { txtFullName.putClientProperty(FlatClientProperties.OUTLINE, null); }

        if (emailStr.isEmpty() || !emailStr.contains("@") || !emailStr.contains(".")) { txtEmail.putClientProperty(FlatClientProperties.OUTLINE, "error"); isValid = false; }
        else { txtEmail.putClientProperty(FlatClientProperties.OUTLINE, null); }

        if (!isValid) {
            lblError.setText("Silakan perbaiki kesalahan yang ditandai.");
            return;
        }

        lblError.setText(" ");
        btnSave.setEnabled(false);

        User user = editingUser == null ? new User() : editingUser;
        user.setUsername(usernameStr);
        if (!passwordStr.isEmpty()) { user.setPasswordHash(passwordStr); }
        user.setFullName(fullNameStr);
        user.setEmail(emailStr);
        user.setPhone(phoneStr);
        user.setRole((String) comboRole.getSelectedItem());
        user.setStatus((String) comboStatus.getSelectedItem());

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (editingUser == null) {
                    return userService.addUser(user);
                } else {
                    return userService.updateUser(user, !passwordStr.isEmpty());
                }
            }
            @Override
            protected void done() {
                btnSave.setEnabled(true);
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(MembersPanel.this, "Anggota berhasil disimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        loadUsersData(currentSearchQuery);
                        clearForm();
                    } else {
                        lblError.setText("Gagal menyimpan anggota.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    lblError.setText("Kesalahan: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
