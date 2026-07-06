package com.kelompok1.service;

import com.kelompok1.config.DatabaseHelper;
import com.kelompok1.model.DailyStats;
import com.kelompok1.model.Transaction;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionService {
    public int issueBook(int userId, int bookId, int daysToBorrow) {
        String insertTx = "INSERT INTO transactions (book_id, user_id, issue_date, due_date) VALUES (?, ?, ?, ?)";
        String updateBook = "UPDATE books SET available_copies = available_copies - 1 WHERE book_id = ? AND available_copies > 0";
        
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement bkStmt = conn.prepareStatement(updateBook);
                 PreparedStatement txStmt = conn.prepareStatement(insertTx, Statement.RETURN_GENERATED_KEYS)) {
                
                bkStmt.setInt(1, bookId);
                int affectedRows = bkStmt.executeUpdate();
                
                if (affectedRows == 0) {
                    conn.rollback();
                    return -1;
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

                try (ResultSet generatedKeys = txStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
                return -1;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
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

    public List<Transaction> getTransactionsByUser(int userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, b.title as bookTitle, u.full_name as memberName, u.member_code as memberCode " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "WHERE t.user_id = ? ORDER BY t.transaction_id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, pageSize);
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

    public List<Transaction> getAllTransactions(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, b.title as bookTitle, u.full_name as memberName, u.member_code as memberCode " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "ORDER BY t.transaction_id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pageSize);
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

    public List<Transaction> searchTransactions(String query, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, b.title as bookTitle, u.full_name as memberName, u.member_code as memberCode " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "WHERE b.title LIKE ? OR u.full_name LIKE ? OR t.status LIKE ? " +
                     "ORDER BY t.transaction_id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String q = "%" + query + "%";
            pstmt.setString(1, q);
            pstmt.setString(2, q);
            pstmt.setString(3, q);
            pstmt.setInt(4, pageSize);
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
        String sql = "SELECT COUNT(*) FROM transactions WHERE status = 'Issued' AND CAST(due_date AS DATE) < CURDATE()";
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

    public List<Map<String, Object>> getOverdueHistory(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.member_code, b.title, b.isbn, t.due_date, COALESCE(f.amount, 0) as fineAmount " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN users u ON t.user_id = u.user_id " +
                     "LEFT JOIN fines f ON t.transaction_id = f.transaction_id " +
                     "WHERE t.status = 'Issued' AND CAST(t.due_date AS DATE) < CURDATE() " +
                     "ORDER BY t.due_date ASC LIMIT ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("memberId", rs.getString("member_code"));
                    map.put("title", rs.getString("title"));
                    map.put("isbn", rs.getString("isbn"));
                    map.put("dueDate", rs.getString("due_date"));
                    map.put("fine", String.format("Rp %,.0f", rs.getDouble("fineAmount")));
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

    public boolean markBookAsLost(int transactionId, int bookId, double fineAmount) {
        String updateTx = "UPDATE transactions SET status = 'Lost' WHERE transaction_id = ?";
        String updateBook = "UPDATE books SET total_copies = GREATEST(0, total_copies - 1) WHERE book_id = ?";
        String insertFine = "INSERT INTO fines (transaction_id, amount, status) VALUES (?, ?, 'Unpaid')";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement txStmt = conn.prepareStatement(updateTx);
                 PreparedStatement bkStmt = conn.prepareStatement(updateBook);
                 PreparedStatement fnStmt = conn.prepareStatement(insertFine)) {

                txStmt.setInt(1, transactionId);
                txStmt.executeUpdate();

                bkStmt.setInt(1, bookId);
                bkStmt.executeUpdate();

                fnStmt.setInt(1, transactionId);
                fnStmt.setDouble(2, fineAmount);
                fnStmt.executeUpdate();

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
}
