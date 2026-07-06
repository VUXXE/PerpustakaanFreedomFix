package com.kelompok1.util;

import com.kelompok1.config.DatabaseHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DataPopulator {
    public static void main(String[] args) {
        System.out.println("Starting full dummy data population...");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 0. Reset existing dummy data (optional but requested)
            System.out.println("Resetting previous dummy data...");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("DELETE FROM fines");
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM users WHERE role = 'Member'");
            stmt.execute("DELETE FROM books WHERE title IN ('Laskar Pelangi', 'Bumi Manusia', 'Clean Code', 'Filosofi Teras', 'Sapiens: Riwayat Singkat Umat Manusia', 'Harry Potter dan Batu Bertuah', 'Atomic Habits', 'Dune')");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            System.out.println("Previous data cleared.");

            // 1. Insert Users
            String[] userQueries = {
                "INSERT IGNORE INTO users (username, password_hash, full_name, email, phone, role, status, address, member_code) VALUES ('budi_s', '$2a$10$r9T9VdG/3pT.wG3Yc3H2A.pP/K5sN7qA0yK7J4mR2xJ9dJ3lFzN1W', 'Budi Santoso', 'budi@example.com', '081234567890', 'Member', 'Active', 'Jl. Merdeka No. 10, Jakarta', 'MEM-1001')",
                "INSERT IGNORE INTO users (username, password_hash, full_name, email, phone, role, status, address, member_code) VALUES ('siti_a', '$2a$10$r9T9VdG/3pT.wG3Yc3H2A.pP/K5sN7qA0yK7J4mR2xJ9dJ3lFzN1W', 'Siti Aminah', 'siti@example.com', '081298765432', 'Member', 'Active', 'Jl. Melati No. 5, Bandung', 'MEM-1002')",
                "INSERT IGNORE INTO users (username, password_hash, full_name, email, phone, role, status, address, member_code) VALUES ('andi_r', '$2a$10$r9T9VdG/3pT.wG3Yc3H2A.pP/K5sN7qA0yK7J4mR2xJ9dJ3lFzN1W', 'Andi Rahman', 'andi@example.com', '085612345678', 'Member', 'Active', 'Jl. Pahlawan No. 2, Surabaya', 'MEM-1003')",
                "INSERT IGNORE INTO users (username, password_hash, full_name, email, phone, role, status, address, member_code) VALUES ('rara_k', '$2a$10$r9T9VdG/3pT.wG3Yc3H2A.pP/K5sN7qA0yK7J4mR2xJ9dJ3lFzN1W', 'Rara Kirana', 'rara@example.com', '087812345678', 'Member', 'Active', 'Jl. Kenangan No. 99, Yogyakarta', 'MEM-1004')",
                "INSERT IGNORE INTO users (username, password_hash, full_name, email, phone, role, status, address, member_code) VALUES ('joko_w', '$2a$10$r9T9VdG/3pT.wG3Yc3H2A.pP/K5sN7qA0yK7J4mR2xJ9dJ3lFzN1W', 'Joko Widodo', 'joko@example.com', '081198765432', 'Member', 'Active', 'Jl. Raya Bogor No. 12, Bogor', 'MEM-1005')"
            };
            
            for (String q : userQueries) {
                stmt.execute(q);
            }
            System.out.println("Inserted users successfully.");
            
            // 2. Insert Books
            String[] bookQueries = {
                "INSERT IGNORE INTO books (title, author, call_number, publisher, language, isbn, classification, edition, total_copies, available_copies) VALUES ('Laskar Pelangi', 'Andrea Hirata', 'IND-FIC-001', 'Bentang Pustaka', 'Indonesia', '978-979-3062-79-2', '899.221', 'Pertama', 5, 5)",
                "INSERT IGNORE INTO books (title, author, call_number, publisher, language, isbn, classification, edition, total_copies, available_copies) VALUES ('Bumi Manusia', 'Pramoedya Ananta Toer', 'IND-FIC-002', 'Hasta Mitra', 'Indonesia', '978-979-97312-3-4', '899.221', 'Revisi', 3, 3)",
                "INSERT IGNORE INTO books (title, author, call_number, publisher, language, isbn, classification, edition, total_copies, available_copies) VALUES ('Clean Code', 'Robert C. Martin', 'COMP-001', 'Prentice Hall', 'English', '978-0132350884', '005.13', 'First', 2, 2)",
                "INSERT IGNORE INTO books (title, author, call_number, publisher, language, isbn, classification, edition, total_copies, available_copies) VALUES ('Filosofi Teras', 'Henry Manampiring', 'PHI-001', 'Kompas Ilmu', 'Indonesia', '978-602-412-518-9', '100', 'Cetakan ke-12', 4, 4)",
                "INSERT IGNORE INTO books (title, author, call_number, publisher, language, isbn, classification, edition, total_copies, available_copies) VALUES ('Sapiens: Riwayat Singkat Umat Manusia', 'Yuval Noah Harari', 'HIS-001', 'KPG', 'Indonesia', '978-602-424-697-6', '900', 'Ketiga', 6, 6)",
                "INSERT IGNORE INTO books (title, author, call_number, publisher, language, isbn, classification, edition, total_copies, available_copies) VALUES ('Harry Potter dan Batu Bertuah', 'J.K. Rowling', 'FIC-003', 'Gramedia', 'Indonesia', '978-979-22-6815-7', '823', 'Pertama', 2, 2)",
                "INSERT IGNORE INTO books (title, author, call_number, publisher, language, isbn, classification, edition, total_copies, available_copies) VALUES ('Atomic Habits', 'James Clear', 'SELF-001', 'Gramedia Pustaka Utama', 'Indonesia', '978-602-06-3317-6', '158.1', 'Revisi', 10, 10)",
                "INSERT IGNORE INTO books (title, author, call_number, publisher, language, isbn, classification, edition, total_copies, available_copies) VALUES ('Dune', 'Frank Herbert', 'FIC-004', 'Chilton Books', 'English', '978-0441172719', '813', 'First', 1, 1)"
            };
            
            for (String q : bookQueries) {
                stmt.execute(q);
            }
            System.out.println("Inserted books successfully.");

            // Get IDs for some users and books to create dummy transactions
            int budiId = getUserId(conn, "budi_s");
            int sitiId = getUserId(conn, "siti_a");
            int andiId = getUserId(conn, "andi_r");
            int raraId = getUserId(conn, "rara_k");
            
            int book1Id = getBookId(conn, "Laskar Pelangi");
            int book2Id = getBookId(conn, "Bumi Manusia");
            int book3Id = getBookId(conn, "Clean Code");
            int book4Id = getBookId(conn, "Filosofi Teras");
            int book5Id = getBookId(conn, "Dune");

            if (budiId > 0 && book1Id > 0) {
                // 3. Transactions (Pinjaman) & Fines
                String insertTx = "INSERT INTO transactions (book_id, user_id, issue_date, due_date, return_date, status) VALUES (?, ?, ?, ?, ?, ?)";
                String insertFine = "INSERT IGNORE INTO fines (transaction_id, amount, status) VALUES (?, ?, ?)";

                try (PreparedStatement pstmtTx = conn.prepareStatement(insertTx, Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement pstmtFine = conn.prepareStatement(insertFine)) {
                    
                    // a) Normal active loan (Pinjaman biasa, belum telat)
                    int tx1 = insertTransaction(pstmtTx, book1Id, budiId, today.minusDays(2).format(formatter), today.plusDays(5).format(formatter), null, "Issued");
                    updateAvailableCopies(conn, book1Id, -1);
                    System.out.println("Inserted active normal loan. (Tx ID: " + tx1 + ")");

                    // b) Returned loan (Pinjaman dikembalikan tepat waktu)
                    int tx2 = insertTransaction(pstmtTx, book2Id, sitiId, today.minusDays(10).format(formatter), today.minusDays(3).format(formatter), today.minusDays(4).format(formatter), "Returned");
                    System.out.println("Inserted returned loan. (Tx ID: " + tx2 + ")");

                    // c) Late book (Buku telat) -> Belum dikembalikan & Kena denda
                    int tx3 = insertTransaction(pstmtTx, book3Id, andiId, today.minusDays(15).format(formatter), today.minusDays(8).format(formatter), null, "Issued");
                    updateAvailableCopies(conn, book3Id, -1);
                    insertFine(pstmtFine, tx3, 40000.0, "Unpaid");
                    System.out.println("Inserted late loan with fine. (Tx ID: " + tx3 + ")");

                    // d) Late book (Buku telat) -> Sudah dikembalikan telat & Denda dibayar
                    int tx4 = insertTransaction(pstmtTx, book4Id, raraId, today.minusDays(20).format(formatter), today.minusDays(13).format(formatter), today.minusDays(10).format(formatter), "Returned");
                    insertFine(pstmtFine, tx4, 15000.0, "Paid");
                    System.out.println("Inserted returned late loan with paid fine. (Tx ID: " + tx4 + ")");

                    // e) Lost book (Buku hilang)
                    int tx5 = insertTransaction(pstmtTx, book5Id, budiId, today.minusDays(30).format(formatter), today.minusDays(23).format(formatter), null, "Lost");
                    updateAvailableCopies(conn, book5Id, -1);
                    insertFine(pstmtFine, tx5, 150000.0, "Unpaid");
                    System.out.println("Inserted lost book loan with fine. (Tx ID: " + tx5 + ")");

                }
            } else {
                System.out.println("Could not create dummy transactions: users or books not found.");
            }

            System.out.println("Dummy data population completed!");
            
        } catch (Exception e) {
            System.err.println("Error populating data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int getUserId(Connection conn, String username) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id");
            }
        }
        return -1;
    }

    private static int getBookId(Connection conn, String title) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT book_id FROM books WHERE title = ?")) {
            stmt.setString(1, title);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("book_id");
            }
        }
        return -1;
    }

    private static int insertTransaction(PreparedStatement pstmt, int bookId, int userId, String issueDate, String dueDate, String returnDate, String status) throws Exception {
        pstmt.setInt(1, bookId);
        pstmt.setInt(2, userId);
        pstmt.setString(3, issueDate);
        pstmt.setString(4, dueDate);
        pstmt.setString(5, returnDate);
        pstmt.setString(6, status);
        pstmt.executeUpdate();
        try (ResultSet rs = pstmt.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    private static void insertFine(PreparedStatement pstmt, int transactionId, double amount, String status) throws Exception {
        pstmt.setInt(1, transactionId);
        pstmt.setDouble(2, amount);
        pstmt.setString(3, status);
        pstmt.executeUpdate();
    }

    private static void updateAvailableCopies(Connection conn, int bookId, int delta) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE books SET available_copies = available_copies + ? WHERE book_id = ?")) {
            stmt.setInt(1, delta);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
        }
    }
}
