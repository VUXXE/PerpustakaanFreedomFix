package com.kelompok1.dao;

import com.kelompok1.config.DatabaseHelper;
import com.kelompok1.model.Fine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {

    public List<Fine> getFinesByUser(int userId, int limit, int offset) {
        List<Fine> fines = new ArrayList<>();
        String sql = "SELECT f.*, u.full_name as memberName, u.member_code as memberCode, b.title as bookTitle " +
                     "FROM fines f " +
                     "JOIN transactions t ON f.transaction_id = t.transaction_id " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "WHERE t.user_id = ? ORDER BY f.fine_id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            pstmt.setInt(3, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    fines.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fines;
    }

    public List<Fine> getAllFines(int limit, int offset) {
        List<Fine> fines = new ArrayList<>();
        String sql = "SELECT f.*, u.full_name as memberName, u.member_code as memberCode, b.title as bookTitle " +
                     "FROM fines f " +
                     "JOIN transactions t ON f.transaction_id = t.transaction_id " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "ORDER BY f.fine_id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    fines.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fines;
    }

    public boolean payFine(int fineId) {
        String sql = "UPDATE fines SET status = 'Paid' WHERE fine_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fineId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void assessFines() {
        // Logic to calculate overdue transactions and create fines using the dynamic fine rate setting
        String sql = "INSERT IGNORE INTO fines (transaction_id, amount) " +
                     "SELECT transaction_id, DATEDIFF(CURDATE(), CAST(due_date AS DATE)) * COALESCE((SELECT CAST(value AS DECIMAL(10,2)) FROM settings WHERE `key` = 'fine_rate'), 5000.0) " +
                     "FROM transactions " +
                     "WHERE status = 'Issued' AND CURDATE() > CAST(due_date AS DATE)";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            
            // Update existing unpaid fines
            String updateSql = "UPDATE fines SET amount = (SELECT DATEDIFF(CURDATE(), CAST(due_date AS DATE)) * COALESCE((SELECT CAST(value AS DECIMAL(10,2)) FROM settings WHERE `key` = 'fine_rate'), 5000.0) " +
                               "FROM transactions WHERE transactions.transaction_id = fines.transaction_id) " +
                               "WHERE status = 'Unpaid'";
            stmt.executeUpdate(updateSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Fine> searchFines(String query, int limit, int offset) {
        List<Fine> fines = new ArrayList<>();
        String sql = "SELECT f.*, u.full_name as memberName, u.member_code as memberCode, b.title as bookTitle " +
                     "FROM fines f " +
                     "JOIN transactions t ON f.transaction_id = t.transaction_id " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "WHERE u.full_name LIKE ? OR b.title LIKE ? OR f.status LIKE ? " +
                     "ORDER BY f.fine_id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String q = "%" + query + "%";
            pstmt.setString(1, q);
            pstmt.setString(2, q);
            pstmt.setString(3, q);
            pstmt.setInt(4, limit);
            pstmt.setInt(5, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    fines.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fines;
    }

    private Fine mapResultSet(ResultSet rs) throws SQLException {
        Fine fine = new Fine();
        fine.setFineId(rs.getInt("fine_id"));
        fine.setTransactionId(rs.getInt("transaction_id"));
        fine.setAmount(rs.getDouble("amount"));
        fine.setStatus(rs.getString("status"));
        fine.setUpdatedAt(rs.getString("updated_at"));
        fine.setMemberName(rs.getString("memberName"));
        fine.setMemberCode(rs.getString("memberCode"));
        fine.setBookTitle(rs.getString("bookTitle"));
        return fine;
    }

    // --- Analytics Methods ---
    
    public double getTotalPendingFees() {
        String sql = "SELECT SUM(amount) FROM fines WHERE status = 'Unpaid'";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    public double getTotalPendingFeesByUser(int userId) {
        String sql = "SELECT SUM(f.amount) FROM fines f " +
                     "JOIN transactions t ON f.transaction_id = t.transaction_id " +
                     "WHERE f.status = 'Unpaid' AND t.user_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }
}
