package com.kelompok1.service;

import com.kelompok1.config.DatabaseHelper;
import java.sql.*;

public class SettingsService {
    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT value FROM settings WHERE `key` = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("value");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public boolean setSetting(String key, String value) {
        String sql = "INSERT INTO settings (`key`, value) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE value = VALUES(value)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
