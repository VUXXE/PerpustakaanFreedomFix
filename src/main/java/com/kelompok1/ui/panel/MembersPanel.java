package com.kelompok1.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.kelompok1.model.User;
import com.kelompok1.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

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
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private JPanel formContainer;

    public MembersPanel() {
        this.userService = new UserService();
        
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        
        mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(UIManager.getColor("Panel.background"));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        initPanel();
        
        add(mainContainer, "Table");
        cardLayout.show(this, "Table");
    }

    private void initPanel() {
        // --- HEADER PANEL (NORTH) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIManager.getColor("Panel.background"));
        
        usersTitleLabel = new JLabel("Direktori Anggota (Memuat...)");
        usersTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +6");
        headerPanel.add(usersTitleLabel, BorderLayout.WEST);
        
        // Controls (Search + Register/Edit/Delete)
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setBackground(UIManager.getColor("Panel.background"));
        
        // Local search bar removed, using header search bar
        
        JButton btnRegisterUser = new JButton("Daftar Anggota");
        btnRegisterUser.putClientProperty(FlatClientProperties.STYLE, "background: $Component.accentColor; foreground: #ffffff; arc: 10");
        
        JButton btnEditUser = new JButton("Ubah");
        btnEditUser.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        btnEditUser.setEnabled(false);
        
        JButton btnDeleteUser = new JButton("Hapus");
        btnDeleteUser.putClientProperty(FlatClientProperties.STYLE, "background: $Component.error.focusedBorderColor; foreground: #ffffff; arc: 10");
        btnDeleteUser.setEnabled(false);
        
        controlsPanel.add(btnRegisterUser);
        controlsPanel.add(btnEditUser);
        controlsPanel.add(btnDeleteUser);
        headerPanel.add(controlsPanel, BorderLayout.EAST);
        
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        
        // --- TABLE (CENTER) ---
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
            boolean hasSelection = usersTable.getSelectedRow() != -1;
            btnEditUser.setEnabled(hasSelection);
            btnDeleteUser.setEnabled(hasSelection);
        });
        
        // Action Listeners
        btnRegisterUser.addActionListener(e -> showUserForm(null));
        
        btnEditUser.addActionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow == -1) return;
            int userId = (Integer) usersTableModel.getValueAt(selectedRow, 0);
            
            btnEditUser.setEnabled(false);
            SwingWorker<User, Void> fetchWorker = new SwingWorker<>() {
                @Override
                protected User doInBackground() {
                    return userService.getUserById(userId);
                }
                @Override
                protected void done() {
                    btnEditUser.setEnabled(true);
                    try {
                        User u = get();
                        if (u != null) {
                            showUserForm(u);
                        } else {
                            JOptionPane.showMessageDialog(MembersPanel.this, "Gagal mengambil detail anggota.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(MembersPanel.this, "Kesalahan saat mengambil data: " + ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            fetchWorker.execute();
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
        
        JScrollPane scroll = new JScrollPane(usersTable);
        mainContainer.add(scroll, BorderLayout.CENTER);
        
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
        
        mainContainer.add(UIUtils.createPaginationPanel(btnPrevPage, btnNextPage, lblPage), BorderLayout.SOUTH);
        
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

    private void showUserForm(User userToEdit) {
        if (formContainer != null) {
            remove(formContainer);
        }
        
        formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(UIManager.getColor("Panel.background"));
        formContainer.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIManager.getColor("Panel.background"));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblHeaderTitle = new JLabel(userToEdit == null ? "Daftar Anggota Baru" : "Ubah Detail Anggota");
        lblHeaderTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +6");
        headerPanel.add(lblHeaderTitle, BorderLayout.NORTH);
        
        if (userToEdit != null && userToEdit.getMemberCode() != null) {
            JLabel lblCode = new JLabel("Kode Anggota: " + userToEdit.getMemberCode());
            lblCode.putClientProperty(FlatClientProperties.STYLE, "foreground: $Component.accentColor; font: bold");
            headerPanel.add(lblCode, BorderLayout.SOUTH);
        } else if (userToEdit == null) {
            JLabel lblSubtitle = new JLabel("Kode Anggota akan dibuat otomatis.");
            lblSubtitle.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: -1");
            headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        }
        formContainer.add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 0.5;

        JTextField txtUsername = UIUtils.createFormTextField("misal: johndoe");
        JPasswordField txtPassword = new JPasswordField(20);
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true; margin: 5, 8, 5, 8; arc: 8");
        if (userToEdit != null) {
            txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Kosongkan jika tidak ingin diubah");
        } else {
            txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Kata Sandi");
        }

        JTextField txtFullName = UIUtils.createFormTextField("misal: John Doe");
        JTextField txtEmail = UIUtils.createFormTextField("misal: johndoe@email.com");
        JTextField txtPhone = UIUtils.createFormTextField("misal: 08123456789");

        JComboBox<String> comboRole = new JComboBox<>(new String[]{"Member", "Admin"});
        comboRole.putClientProperty(FlatClientProperties.STYLE, "margin: 2, 5, 2, 5; arc: 8");
        
        JComboBox<String> comboStatus = new JComboBox<>(new String[]{"Active", "Suspended"});
        comboStatus.putClientProperty(FlatClientProperties.STYLE, "margin: 2, 5, 2, 5; arc: 8");

        if (userToEdit != null) {
            txtUsername.setText(userToEdit.getUsername());
            txtFullName.setText(userToEdit.getFullName());
            txtEmail.setText(userToEdit.getEmail());
            txtPhone.setText(userToEdit.getPhone());
            comboRole.setSelectedItem(userToEdit.getRole());
            comboStatus.setSelectedItem(userToEdit.getStatus());
        }

        // Add fields in a 2-column layout
        int row = 0;
        
        // Row 0
        gbc.gridy = row; gbc.gridx = 0;
        formPanel.add(new JLabel("Nama Lengkap*"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JLabel("Nama Pengguna*"), gbc);
        
        row++;
        gbc.gridy = row; gbc.gridx = 0;
        formPanel.add(txtFullName, gbc);
        gbc.gridx = 1;
        formPanel.add(txtUsername, gbc);

        // Row 2
        row++;
        gbc.gridy = row; gbc.gridx = 0;
        formPanel.add(new JLabel("Email*"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JLabel("Nomor Telepon"), gbc);
        
        row++;
        gbc.gridy = row; gbc.gridx = 0;
        formPanel.add(txtEmail, gbc);
        gbc.gridx = 1;
        formPanel.add(txtPhone, gbc);

        // Row 4
        row++;
        gbc.gridy = row; gbc.gridx = 0;
        formPanel.add(new JLabel(userToEdit == null ? "Kata Sandi*" : "Kata Sandi"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JLabel("Peran Sistem"), gbc);
        
        row++;
        gbc.gridy = row; gbc.gridx = 0;
        formPanel.add(txtPassword, gbc);
        gbc.gridx = 1;
        formPanel.add(comboRole, gbc);

        // Row 6
        row++;
        gbc.gridy = row; gbc.gridx = 0;
        formPanel.add(new JLabel("Status Akun"), gbc);
        
        row++;
        gbc.gridy = row; gbc.gridx = 0;
        formPanel.add(comboStatus, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        formContainer.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setBackground(UIManager.getColor("Panel.background"));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel lblError = new JLabel(" ");
        lblError.putClientProperty(FlatClientProperties.STYLE, "foreground: $Component.error.focusedBorderColor; font: bold -1");
        btnPanel.add(lblError);

        JButton btnCancel = new JButton("Batal");
        btnCancel.addActionListener(e -> cardLayout.show(MembersPanel.this, "Table"));

        JButton btnSave = new JButton("Simpan Anggota");
        btnSave.putClientProperty(FlatClientProperties.STYLE, "background: $Component.accentColor; foreground: #ffffff; font: bold");
        btnSave.addActionListener(e -> {
            String usernameStr = txtUsername.getText().trim();
            String passwordStr = new String(txtPassword.getPassword()).trim();
            String fullNameStr = txtFullName.getText().trim();
            String emailStr = txtEmail.getText().trim();
            String phoneStr = txtPhone.getText().trim();

            boolean isValid = true;
            if (usernameStr.isEmpty()) { txtUsername.putClientProperty(FlatClientProperties.OUTLINE, "error"); isValid = false; }
            else { txtUsername.putClientProperty(FlatClientProperties.OUTLINE, null); }

            if (userToEdit == null && passwordStr.isEmpty()) { txtPassword.putClientProperty(FlatClientProperties.OUTLINE, "error"); isValid = false; }
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

            User user = userToEdit == null ? new User() : userToEdit;
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
                    if (userToEdit == null) {
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
                            cardLayout.show(MembersPanel.this, "Table");
                            JOptionPane.showMessageDialog(MembersPanel.this, "Anggota berhasil disimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                            loadUsersData(currentSearchQuery);
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
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        formContainer.add(btnPanel, BorderLayout.SOUTH);

        add(formContainer, "Form");
        cardLayout.show(this, "Form");
    }
}
