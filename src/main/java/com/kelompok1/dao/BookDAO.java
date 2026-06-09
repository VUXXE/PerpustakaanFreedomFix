package com.kelompok1.dao;

import com.kelompok1.config.DatabaseHelper;
import com.kelompok1.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public List<Book> searchBooks(String query, int limit, int offset) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title ILIKE ? OR author ILIKE ? OR classification ILIKE ? ORDER BY book_id ASC LIMIT ? OFFSET ?";
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
                    books.add(mapResultSetToBook(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public List<Book> getAllBooks(int limit, int offset) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY book_id ASC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
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

    public boolean addBook(Book book) {
        String sql = "INSERT INTO books (series_title, title, author, call_number, publisher, \"collation\", language, isbn, classification, edition, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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

    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET series_title = ?, title = ?, author = ?, call_number = ?, publisher = ?, \"collation\" = ?, language = ?, isbn = ?, classification = ?, edition = ?, total_copies = ?, available_copies = ? WHERE book_id = ?";
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
        return book;
    }

    // --- Analytics Methods ---
    
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
        // Rank books by number of transactions (checkouts)
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
}
