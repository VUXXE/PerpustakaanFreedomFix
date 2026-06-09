package com.kelompok1.dao;

import com.kelompok1.config.DatabaseHelper;
import com.kelompok1.model.DailyStats;
import com.kelompok1.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TransactionDAO {

    public boolean issueBook(int userId, int bookId, int daysToBorrow) {
        String insertTx = "INSERT INTO transactions (book_id, user_id, issue_date, due_date) VALUES (?, ?, ?, ?)";
        String updateBook = "UPDATE books SET available_copies = available_copies - 1 WHERE book_id = ? AND available_copies > 0";
        
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement txStmt = conn.prepareStatement(insertTx);
                 PreparedStatement bkStmt = conn.prepareStatement(updateBook)) {
                
                bkStmt.setInt(1, bookId);
                int affectedRows = bkStmt.executeUpdate();
                
                if (affectedRows == 0) {
                    conn.rollback();
                    return false; // Book not available
                }
                
                LocalDate today = LocalDate.now();
                LocalDate dueDate = today.plusDays(daysToBorrow);
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

                txStmt.setInt(1, bookId);
                txStmt.setInt(2, userId);
                txStmt.setString(3, today.format(formatter));
                txStmt.setString(4, dueDate.format(formatter));
                
                txStmt.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean returnBook(int transactionId, int bookId) {
        String updateTx = "UPDATE transactions SET return_date = ?, status = 'Returned' WHERE transaction_id = ?";
        String updateBook = "UPDATE books SET available_copies = available_copies + 1 WHERE book_id = ?";
        
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement txStmt = conn.prepareStatement(updateTx);
                 PreparedStatement bkStmt = conn.prepareStatement(updateBook)) {
                
                txStmt.setString(1, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                txStmt.setInt(2, transactionId);
                txStmt.executeUpdate();
                
                bkStmt.setInt(1, bookId);
                bkStmt.executeUpdate();
                
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Transaction> getTransactionsByUser(int userId, int limit, int offset) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, b.title as bookTitle, u.full_name as memberName, u.member_code as memberCode " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "WHERE t.user_id = ? ORDER BY t.transaction_id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            pstmt.setInt(3, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Transaction> getAllTransactions(int limit, int offset) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, b.title as bookTitle, u.full_name as memberName, u.member_code as memberCode " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "ORDER BY t.transaction_id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Transaction> searchTransactions(String query, int limit, int offset) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, b.title as bookTitle, u.full_name as memberName, u.member_code as memberCode " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "WHERE b.title ILIKE ? OR u.full_name ILIKE ? OR t.status ILIKE ? " +
                     "ORDER BY t.transaction_id DESC LIMIT ? OFFSET ?";
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
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Transaction mapResultSet(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction();
        tx.setTransactionId(rs.getInt("transaction_id"));
        tx.setBookId(rs.getInt("book_id"));
        tx.setUserId(rs.getInt("user_id"));
        tx.setIssueDate(rs.getString("issue_date"));
        tx.setDueDate(rs.getString("due_date"));
        tx.setReturnDate(rs.getString("return_date"));
        tx.setStatus(rs.getString("status"));
        tx.setBookTitle(rs.getString("bookTitle"));
        tx.setMemberName(rs.getString("memberName"));
        tx.setMemberCode(rs.getString("memberCode"));
        return tx;
    }

    // --- Analytics Methods ---
    
    public int getCountByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE status = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getOverdueCount() {
        String sql = "SELECT COUNT(*) FROM transactions WHERE status = 'Issued' AND due_date::date < CURRENT_DATE";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getLostCount() {
        return getCountByStatus("Lost");
    }

    // DailyStats is now a standalone model class: com.kelompok1.model.DailyStats

    public List<DailyStats> getCheckoutStats() {
        List<DailyStats> stats = new ArrayList<>();
        String sql = "SELECT substr(issue_date, 1, 10) as date, " +
                     "SUM(CASE WHEN status = 'Issued' THEN 1 ELSE 0 END) as borrowed, " +
                     "SUM(CASE WHEN status = 'Returned' THEN 1 ELSE 0 END) as returned " +
                     "FROM transactions GROUP BY substr(issue_date, 1, 10) ORDER BY date ASC LIMIT 7";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String dateStr = rs.getString(1);
                if (dateStr == null || dateStr.trim().isEmpty()) {
                    dateStr = "Unknown";
                }
                stats.add(new DailyStats(dateStr, rs.getInt(2), rs.getInt(3)));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }

    public List<Transaction> getRecentCheckouts(int limit) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, b.title as bookTitle, u.full_name as memberName, u.member_code as memberCode " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "ORDER BY t.issue_date DESC LIMIT ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<java.util.Map<String, Object>> getOverdueHistory(int limit) {
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.member_code, b.title, b.isbn, t.due_date, COALESCE(f.amount, 0) as fineAmount " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "LEFT JOIN fines f ON t.transaction_id = f.transaction_id " +
                     "WHERE t.status = 'Issued' AND t.due_date::date < CURRENT_DATE " +
                     "ORDER BY t.due_date ASC LIMIT ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("memberId", rs.getString("member_code"));
                    map.put("title", rs.getString("title"));
                    map.put("isbn", rs.getString("isbn"));
                    map.put("dueDate", rs.getString("due_date"));
                    map.put("fine", "$" + rs.getDouble("fineAmount"));
                    list.add(map);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int getActiveLoansCount(int userId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE user_id = ? AND status = 'Issued'";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
