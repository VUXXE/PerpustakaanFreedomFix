package com.kelompok1.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.kelompok1.config.DatabaseHelper;
import com.kelompok1.model.Models.Book;
import com.kelompok1.model.Models.DailyStats;
import com.kelompok1.model.Models.Fine;
import com.kelompok1.model.Models.Transaction;
import com.kelompok1.model.Models.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Services {

    // ─── SETTINGS SERVICE ──────────────────────────────────────────────────
    public static class SettingsService {
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

    // ─── USER SERVICE ──────────────────────────────────────────────────────
    public static class UserService {
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

    // ─── BOOK SERVICE ──────────────────────────────────────────────────────
    public static class BookService {
        public boolean addBook(Book book) {
            String sql = "INSERT INTO books (series_title, title, author, call_number, publisher, `collation`, language, isbn, classification, edition, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, book.getSeriesTitle());
                pstmt.setString(2, book.getTitle());
                pstmt.setString(3, book.getAuthor());
                pstmt.setString(4, book.getCallNumber());
                pstmt.setString(5, book.getPublisher());
                pstmt.setString(6, book.getCollation());
                pstmt.setString(7, book.getLanguage());
                pstmt.setString(8, book.getIsbn());
                pstmt.setString(9, book.getClassification());
                pstmt.setString(10, book.getEdition());
                pstmt.setInt(11, book.getTotalCopies());
                pstmt.setInt(12, book.getAvailableCopies());
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        public List<Book> getAllBooks(int page, int pageSize) {
            int offset = (page - 1) * pageSize;
            List<Book> books = new ArrayList<>();
            String sql = "SELECT * FROM books ORDER BY book_id ASC LIMIT ? OFFSET ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, pageSize);
                pstmt.setInt(2, offset);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        books.add(mapResultSetToBook(rs));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return books;
        }

        public Book getBookById(int bookId) {
            String sql = "SELECT * FROM books WHERE book_id = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, bookId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToBook(rs);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        public boolean updateBook(Book book) {
            String sql = "UPDATE books SET series_title = ?, title = ?, author = ?, call_number = ?, publisher = ?, `collation` = ?, language = ?, isbn = ?, classification = ?, edition = ?, total_copies = ?, available_copies = ? WHERE book_id = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, book.getSeriesTitle());
                pstmt.setString(2, book.getTitle());
                pstmt.setString(3, book.getAuthor());
                pstmt.setString(4, book.getCallNumber());
                pstmt.setString(5, book.getPublisher());
                pstmt.setString(6, book.getCollation());
                pstmt.setString(7, book.getLanguage());
                pstmt.setString(8, book.getIsbn());
                pstmt.setString(9, book.getClassification());
                pstmt.setString(10, book.getEdition());
                pstmt.setInt(11, book.getTotalCopies());
                pstmt.setInt(12, book.getAvailableCopies());
                pstmt.setInt(13, book.getBookId());
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        public boolean deleteBook(int bookId) throws SQLException {
            String sql = "DELETE FROM books WHERE book_id = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, bookId);
                return pstmt.executeUpdate() > 0;
            }
        }

        public List<Book> searchBooks(String query, int page, int pageSize) {
            int offset = (page - 1) * pageSize;
            List<Book> books = new ArrayList<>();
            String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR classification LIKE ? ORDER BY book_id ASC LIMIT ? OFFSET ?";
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
                        books.add(mapResultSetToBook(rs));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return books;
        }

        public int getTotalBooksCount() {
            String sql = "SELECT COUNT(*) FROM books";
            try (Connection conn = DatabaseHelper.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }

        public List<Book> getTopBooks(int limit) {
            List<Book> books = new ArrayList<>();
            String sql = "SELECT b.*, COUNT(t.book_id) as checkout_count " +
                         "FROM books b " +
                         "LEFT JOIN transactions t ON b.book_id = t.book_id " +
                         "GROUP BY b.book_id " +
                         "ORDER BY checkout_count DESC LIMIT ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        books.add(mapResultSetToBook(rs));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return books;
        }

        private Book mapResultSetToBook(ResultSet rs) throws SQLException {
            Book book = new Book();
            book.setBookId(rs.getInt("book_id"));
            book.setSeriesTitle(rs.getString("series_title"));
            book.setTitle(rs.getString("title"));
            book.setAuthor(rs.getString("author"));
            book.setCallNumber(rs.getString("call_number"));
            book.setPublisher(rs.getString("publisher"));
            book.setCollation(rs.getString("collation"));
            book.setLanguage(rs.getString("language"));
            book.setIsbn(rs.getString("isbn"));
            book.setClassification(rs.getString("classification"));
            book.setEdition(rs.getString("edition"));
            book.setTotalCopies(rs.getInt("total_copies"));
            book.setAvailableCopies(rs.getInt("available_copies"));
            book.setCreatedAt(rs.getString("created_at"));
            try {
                book.setCheckoutCount(rs.getInt("checkout_count"));
            } catch (SQLException e) {
                book.setCheckoutCount(0);
            }
            return book;
        }
    }

    // ─── TRANSACTION SERVICE ───────────────────────────────────────────────
    public static class TransactionService {
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

    // ─── FINE SERVICE ──────────────────────────────────────────────────────
    public static class FineService {
        public List<Fine> getFinesByUser(int userId, int page, int pageSize) {
            int offset = (page - 1) * pageSize;
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
                pstmt.setInt(2, pageSize);
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

        public List<Fine> getAllFines(int page, int pageSize) {
            int offset = (page - 1) * pageSize;
            List<Fine> fines = new ArrayList<>();
            String sql = "SELECT f.*, u.full_name as memberName, u.member_code as memberCode, b.title as bookTitle " +
                         "FROM fines f " +
                         "JOIN transactions t ON f.transaction_id = t.transaction_id " +
                         "JOIN users u ON t.user_id = u.user_id " +
                         "JOIN books b ON t.book_id = b.book_id " +
                         "ORDER BY f.fine_id DESC LIMIT ? OFFSET ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, pageSize);
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
            String sql = "INSERT IGNORE INTO fines (transaction_id, amount) " +
                         "SELECT transaction_id, DATEDIFF(CURDATE(), CAST(due_date AS DATE)) * COALESCE((SELECT CAST(value AS DECIMAL(10,2)) FROM settings WHERE `key` = 'fine_rate'), 5000.0) " +
                         "FROM transactions " +
                         "WHERE status = 'Issued' AND CURDATE() > CAST(due_date AS DATE)";
            try (Connection conn = DatabaseHelper.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                
                String updateSql = "UPDATE fines SET amount = (SELECT DATEDIFF(CURDATE(), CAST(due_date AS DATE)) * COALESCE((SELECT CAST(value AS DECIMAL(10,2)) FROM settings WHERE `key` = 'fine_rate'), 5000.0) " +
                                   "FROM transactions WHERE transactions.transaction_id = fines.transaction_id) " +
                                   "WHERE status = 'Unpaid'";
                stmt.executeUpdate(updateSql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public List<Fine> searchFines(String query, int page, int pageSize) {
            int offset = (page - 1) * pageSize;
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
                pstmt.setInt(4, pageSize);
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
    }
}
