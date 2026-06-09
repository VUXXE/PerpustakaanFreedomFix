package com.kelompok1.dao;

import com.kelompok1.config.DatabaseHelper;
import com.kelompok1.model.User;
import com.kelompok1.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND status = 'Active'";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.verifyPassword(password, storedHash)) {
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
        String sql = "INSERT INTO users (username, password_hash, full_name, email, phone, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, PasswordUtil.hashPassword(user.getPasswordHash())); // Using field for raw password temporarily
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setString(6, user.getRole());
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

    public List<User> getAllUsers(int limit, int offset) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id ASC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
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
            sql = "UPDATE users SET username = ?, password_hash = ?, full_name = ?, email = ?, phone = ?, role = ?, status = ? WHERE user_id = ?";
        } else {
            sql = "UPDATE users SET username = ?, full_name = ?, email = ?, phone = ?, role = ?, status = ? WHERE user_id = ?";
        }

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            int idx = 2;
            if (changePassword) {
                pstmt.setString(idx++, PasswordUtil.hashPassword(user.getPasswordHash()));
            }
            pstmt.setString(idx++, user.getFullName());
            pstmt.setString(idx++, user.getEmail());
            pstmt.setString(idx++, user.getPhone());
            pstmt.setString(idx++, user.getRole());
            pstmt.setString(idx++, user.getStatus());
            pstmt.setInt(idx++, user.getUserId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<User> searchUsers(String query, int limit, int offset) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE username ILIKE ? OR full_name ILIKE ? OR email ILIKE ? OR member_code ILIKE ? ORDER BY user_id ASC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String q = "%" + query + "%";
            pstmt.setString(1, q);
            pstmt.setString(2, q);
            pstmt.setString(3, q);
            pstmt.setString(4, q);
            pstmt.setInt(5, limit);
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

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        try {
            user.setMemberCode(rs.getString("member_code"));
        } catch (SQLException e) {
            // member_code might not be returned in some old queries if we had explicit selects, but we use SELECT *
        }
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getString("created_at"));
        return user;
    }

    // --- Analytics Methods ---
    
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
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'Member' AND created_at >= CURRENT_DATE - INTERVAL '30 days'";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
