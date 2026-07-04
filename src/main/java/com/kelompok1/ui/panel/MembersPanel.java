package com.kelompok1.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.model.Models.User;
import com.kelompok1.service.Services.UserService;
import com.kelompok1.util.DesignSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modern inline Members Panel.
 * Displays the add/edit form at the top in a compact horizontal layout (no card),
 * and the member directory table list at the bottom.
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

    // Form fields (top panel)
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
    private JTextField txtAddress;
    private JButton btnDeleteUser;
    private JButton btnEdit;
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
        // ─── 1. NORTH WRAPPER (FORM AT TOP) ────────────────────────
        JPanel northWrapper = new JPanel();
        northWrapper.setLayout(new BoxLayout(northWrapper, BoxLayout.Y_AXIS));
        northWrapper.setOpaque(false);

        // Form Fields (in a compact 3-row GridBagLayout directly on the panel background)
        txtFullName = UIUtils.createFormTextField("misal: John Doe");
        txtUsername = UIUtils.createFormTextField("misal: johndoe");
        txtEmail    = UIUtils.createFormTextField("misal: johndoe@email.com");
        txtPhone    = UIUtils.createFormTextField("misal: 08123456789");
        
        txtPassword = new JPasswordField(20);
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true; margin: 6, 10, 6, 10; arc: 8");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Kata Sandi");

        // Measure the preferred height of standard form text fields to align JComboBox height precisely
        int tfHeight = txtFullName.getPreferredSize().height;

        comboRole = new JComboBox<>(new String[]{"Member", "Admin"});
        comboRole.putClientProperty(FlatClientProperties.STYLE, "arc: 8; padding: 6, 10, 6, 10");
        comboRole.setPreferredSize(new Dimension(comboRole.getPreferredSize().width, tfHeight));

        comboStatus = new JComboBox<>(new String[]{"Active", "Suspended"});
        comboStatus.putClientProperty(FlatClientProperties.STYLE, "arc: 8; padding: 6, 10, 6, 10");
        comboStatus.setPreferredSize(new Dimension(comboStatus.getPreferredSize().width, tfHeight));

        txtAddress = UIUtils.createFormTextField("misal: Jl. Raya No. 123");

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        // Matte bottom border acting as divider line between form and table
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, DesignSystem.OUTLINE_VARIANT),
            BorderFactory.createEmptyBorder(10, 0, 15, 0)
        ));

        GridBagConstraints fGbc = new GridBagConstraints();
        fGbc.fill = GridBagConstraints.HORIZONTAL;
        fGbc.insets = new Insets(4, 6, 4, 6);
        fGbc.weightx = 0.25;
        
        int r = 0;

        // Row 0: Form Header Title & Status Subtitle
        fGbc.gridy = r;
        fGbc.gridx = 0;
        fGbc.gridwidth = 4;
        fGbc.weightx = 1.0;
        fGbc.insets = new Insets(4, 0, 4, 0); // No left inset to align with table title
        
        lblFormTitle = new JLabel("Form Registrasi Anggota");
        lblFormTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +4");
        lblFormTitle.setForeground(DesignSystem.ON_SURFACE);
        
        lblFormSubtitle = new JLabel("(Kode Anggota akan dibuat otomatis.)");
        lblFormSubtitle.setFont(DesignSystem.bodyFont(10f, Font.PLAIN));
        lblFormSubtitle.setForeground(UIManager.getColor("Label.disabledForeground"));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(lblFormTitle);
        titlePanel.add(Box.createHorizontalStrut(8));
        titlePanel.add(lblFormSubtitle);
        formPanel.add(titlePanel, fGbc);

        // Row 1: Labels for inputs 1-4
        r++;
        fGbc.gridy = r;
        fGbc.gridwidth = 1;
        fGbc.weightx = 0.25;
        fGbc.insets = new Insets(10, 6, 0, 6); // gap above input labels
        
        fGbc.gridx = 0;
        JLabel lblName = new JLabel("Nama Lengkap*");
        lblName.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblName.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblName, fGbc);
        
        fGbc.gridx = 1;
        JLabel lblUser = new JLabel("Nama Pengguna*");
        lblUser.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblUser.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblUser, fGbc);
        
        fGbc.gridx = 2;
        JLabel lblMail = new JLabel("Email*");
        lblMail.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblMail.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblMail, fGbc);
        
        fGbc.gridx = 3;
        JLabel lblPh = new JLabel("Nomor Telepon");
        lblPh.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblPh.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblPh, fGbc);

        // Row 2: Fields 1-4
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(2, 6, 6, 6);
        
        fGbc.gridx = 0; formPanel.add(txtFullName, fGbc);
        fGbc.gridx = 1; formPanel.add(txtUsername, fGbc);
        fGbc.gridx = 2; formPanel.add(txtEmail, fGbc);
        fGbc.gridx = 3; formPanel.add(txtPhone, fGbc);

        // Row 3: Labels for inputs 5-8
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(6, 6, 0, 6);
        
        fGbc.gridx = 0;
        lblPass = new JLabel("Kata Sandi*");
        lblPass.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblPass.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblPass, fGbc);
        
        fGbc.gridx = 1;
        JLabel lblRole = new JLabel("Peran Sistem");
        lblRole.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblRole.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblRole, fGbc);
        
        fGbc.gridx = 2;
        JLabel lblStatus = new JLabel("Status Akun");
        lblStatus.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblStatus.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblStatus, fGbc);
        
        fGbc.gridx = 3;
        JLabel lblAddress = new JLabel("Alamat");
        lblAddress.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblAddress.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        formPanel.add(lblAddress, fGbc);

        // Row 4: Fields 5-8
        r++;
        fGbc.gridy = r;
        fGbc.insets = new Insets(2, 6, 6, 6);
        
        fGbc.gridx = 0; formPanel.add(txtPassword, fGbc);
        fGbc.gridx = 1; formPanel.add(comboRole, fGbc);
        fGbc.gridx = 2; formPanel.add(comboStatus, fGbc);
        fGbc.gridx = 3; formPanel.add(txtAddress, fGbc);

        // Row 5: Action Buttons Panel (Hapus, Edit, Simpan, Batal)
        r++;
        fGbc.gridy = r;
        fGbc.gridx = 0;
        fGbc.gridwidth = 4;
        fGbc.weightx = 1.0;
        fGbc.insets = new Insets(12, 6, 6, 6);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        btnDeleteUser = new JButton("Hapus");
        DesignSystem.applyDangerButton(btnDeleteUser);
        btnDeleteUser.setEnabled(false);
        buttonPanel.add(btnDeleteUser);
        
        btnEdit = new JButton("Edit");
        DesignSystem.applySecondaryButton(btnEdit);
        btnEdit.setEnabled(false);
        buttonPanel.add(btnEdit);
        
        JButton btnCancel = new JButton("Batal");
        DesignSystem.applySecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> clearForm());
        buttonPanel.add(btnCancel);
        
        btnSave = new JButton("Simpan");
        DesignSystem.applyPrimaryButton(btnSave);
        btnSave.addActionListener(e -> saveUser());
        buttonPanel.add(btnSave);
        
        formPanel.add(buttonPanel, fGbc);

        // Delete button logic
        btnDeleteUser.addActionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow == -1 && editingUser == null) return;
            
            final int userId = (editingUser != null) ? editingUser.getUserId() : (Integer) usersTableModel.getValueAt(selectedRow, 0);
            final String nameStr = (editingUser != null) ? editingUser.getFullName() : (String) usersTableModel.getValueAt(selectedRow, 3);
            
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

        // Edit button logic
        btnEdit.addActionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow != -1) {
                int userId = (Integer) usersTableModel.getValueAt(selectedRow, 0);
                loadUserIntoForm(userId);
            }
        });

        // Row 6: Error Label
        r++;
        fGbc.gridy = r;
        fGbc.gridx = 0;
        fGbc.gridwidth = 4;
        fGbc.weightx = 1.0;
        fGbc.insets = new Insets(4, 6, 0, 6);
        
        lblError = new JLabel(" ");
        lblError.putClientProperty(FlatClientProperties.STYLE, "foreground: $Component.error.focusedBorderColor; font: bold -1");
        formPanel.add(lblError, fGbc);

        northWrapper.add(formPanel);
        add(northWrapper, BorderLayout.NORTH);
        
        // ─── 2. TABLE PANEL (CENTER - BELOW FORM) ───────────────────────────
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Title directly above table
        usersTitleLabel = new JLabel("Direktori Anggota (Memuat...)");
        usersTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +4");
        usersTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        centerWrapper.add(usersTitleLabel, BorderLayout.NORTH);
        
        String[] cols = {"ID Pengguna", "Kode Anggota", "Nama Pengguna", "Nama Lengkap", "Email", "Telepon", "Alamat", "Peran", "Status"};
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
            btnEdit.setEnabled(hasSelection);
            btnDeleteUser.setEnabled(hasSelection);
        });

        // Double-click row to edit
        usersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = usersTable.getSelectedRow();
                    if (selectedRow != -1) {
                        int userId = (Integer) usersTableModel.getValueAt(selectedRow, 0);
                        loadUserIntoForm(userId);
                    }
                }
            }
        });
        
        JScrollPane tableScroll = new JScrollPane(usersTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        centerWrapper.add(tableScroll, BorderLayout.CENTER);
        
        add(centerWrapper, BorderLayout.CENTER);
        
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
                            u.getEmail(), u.getPhone(), u.getAddress() != null ? u.getAddress() : "", u.getRole(), u.getStatus()
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
                        lblFormSubtitle.setText("(Kode Anggota: " + u.getMemberCode() + ")");
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
                        txtAddress.setText(u.getAddress() != null ? u.getAddress() : "");
                        
                        btnDeleteUser.setEnabled(true);
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
        lblFormTitle.setText("Form Registrasi Anggota");
        lblFormSubtitle.setText("(Kode Anggota akan dibuat otomatis.)");
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
        txtAddress.setText("");
        
        btnDeleteUser.setEnabled(false);
        btnEdit.setEnabled(false);
        btnSave.setText("Simpan");
        usersTable.clearSelection();
    }

    private void saveUser() {
        String usernameStr = txtUsername.getText().trim();
        String passwordStr = new String(txtPassword.getPassword()).trim();
        String fullNameStr = txtFullName.getText().trim();
        String emailStr = txtEmail.getText().trim();
        String phoneStr = txtPhone.getText().trim();
        String addressStr = txtAddress.getText().trim();

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
        user.setAddress(addressStr);

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
