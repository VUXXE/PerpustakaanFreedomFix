package com.kelompok1.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.kelompok1.config.DatabaseHelper;
import com.kelompok1.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    public static String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        return result.verified;
    }

    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND status = 'Active'";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (verifyPassword(password, storedHash)) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, phone, role, address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, hashPassword(user.getPasswordHash()));
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setString(6, user.getRole());
            pstmt.setString(7, user.getAddress());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int userId = rs.getInt(1);
                        String memberCode = String.format("MEM-%04d", userId);
                        user.setUserId(userId);
                        user.setMemberCode(memberCode);
                        
                        try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE users SET member_code = ? WHERE user_id = ?")) {
                            updateStmt.setString(1, memberCode);
                            updateStmt.setInt(2, userId);
                            updateStmt.executeUpdate();
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public List<User> getAllUsers(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id ASC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public User getUserByMemberCode(String memberCode) {
        String sql = "SELECT * FROM users WHERE member_code = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, memberCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateUser(User user, boolean changePassword) throws SQLException {
        String sql;
        if (changePassword) {
            sql = "UPDATE users SET username = ?, password_hash = ?, full_name = ?, email = ?, phone = ?, role = ?, status = ?, address = ? WHERE user_id = ?";
        } else {
            sql = "UPDATE users SET username = ?, full_name = ?, email = ?, phone = ?, role = ?, status = ?, address = ? WHERE user_id = ?";
        }

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            int idx = 2;
            if (changePassword) {
                pstmt.setString(idx++, hashPassword(user.getPasswordHash()));
            }
            pstmt.setString(idx++, user.getFullName());
            pstmt.setString(idx++, user.getEmail());
            pstmt.setString(idx++, user.getPhone());
            pstmt.setString(idx++, user.getRole());
            pstmt.setString(idx++, user.getStatus());
            pstmt.setString(idx++, user.getAddress());
            pstmt.setInt(idx++, user.getUserId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) throws SQLException {
        User user = getUserById(userId);
        if (user != null && verifyPassword(oldPassword, user.getPasswordHash())) {
            String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, hashPassword(newPassword));
                pstmt.setInt(2, userId);
                return pstmt.executeUpdate() > 0;
            }
        }
        return false;
    }

    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<User> searchUsers(String query, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE username LIKE ? OR full_name LIKE ? OR email LIKE ? OR member_code LIKE ? ORDER BY user_id ASC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String q = "%" + query + "%";
            pstmt.setString(1, q);
            pstmt.setString(2, q);
            pstmt.setString(3, q);
            pstmt.setString(4, q);
            pstmt.setInt(5, pageSize);
            pstmt.setInt(6, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public int getTotalMembersCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'Member'";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getNewMembersCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'Member' AND created_at >= CURRENT_DATE - INTERVAL 30 DAY";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        try {
            user.setMemberCode(rs.getString("member_code"));
        } catch (SQLException e) {
            // ignore
        }
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        try {
            user.setAddress(rs.getString("address"));
        } catch (SQLException e) {
            // ignore
        }
        user.setCreatedAt(rs.getString("created_at"));
        return user;
    }
}
