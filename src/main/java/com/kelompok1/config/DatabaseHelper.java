package com.kelompok1.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/perpustakaan?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(30000);
        
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            
            // 1. USERS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(255) NOT NULL UNIQUE, " +
                    "password_hash TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "phone TEXT, " +
                    "role VARCHAR(50) NOT NULL DEFAULT 'Member' CHECK(role IN ('Admin', 'Member')), " +
                    "status VARCHAR(50) NOT NULL DEFAULT 'Active' CHECK(status IN ('Active', 'Suspended')), " +
                    "address TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");");
            
            // Alter table to add member_code if it doesn't exist
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN member_code VARCHAR(50) UNIQUE;");
            } catch (SQLException e) {
                // Ignore if column already exists
            }

            // Alter table to add address if it doesn't exist
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN address TEXT;");
            } catch (SQLException e) {
                // Ignore if column already exists
            }
            
            // Early member_code update removed, now handled at the end of initialization

            // 2. BOOKS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS books (" +
                    "book_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "series_title VARCHAR(500), " +
                    "title VARCHAR(500) NOT NULL, " +
                    "author VARCHAR(500), " +
                    "call_number VARCHAR(100), " +
                    "publisher VARCHAR(100), " +
                    "`collation` VARCHAR(255), " +
                    "language VARCHAR(50), " +
                    "isbn VARCHAR(100), " +
                    "classification VARCHAR(100), " +
                    "edition VARCHAR(100), " +
                    "total_copies INT DEFAULT 3, " +
                    "available_copies INT DEFAULT 3, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");");

            // 3. TRANSACTIONS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "transaction_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "book_id INTEGER NOT NULL, " +
                    "user_id INTEGER NOT NULL, " +
                    "issue_date TEXT NOT NULL, " +
                    "due_date TEXT NOT NULL, " +
                    "return_date TEXT NULL, " +
                    "status VARCHAR(50) NOT NULL DEFAULT 'Issued' CHECK(status IN ('Issued', 'Returned', 'Lost')), " +
                    "FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE RESTRICT, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT" +
                    ");");

            // 4. FINES TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS fines (" +
                    "fine_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "transaction_id INTEGER NOT NULL UNIQUE, " +
                    "amount REAL NOT NULL DEFAULT 0.00 CHECK (amount >= 0), " +
                    "status VARCHAR(50) NOT NULL DEFAULT 'Unpaid' CHECK(status IN ('Unpaid', 'Paid')), " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE" +
                    ");");

            // 5. SETTINGS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS settings (" +
                    "`key` VARCHAR(255) PRIMARY KEY, " +
                    "value VARCHAR(255) NOT NULL" +
                    ");");

            // Seed default settings if empty
            stmt.execute("INSERT IGNORE INTO settings (`key`, value) VALUES ('fine_rate', '5000');");
            stmt.execute("INSERT IGNORE INTO settings (`key`, value) VALUES ('borrow_duration', '7');");
            stmt.execute("INSERT IGNORE INTO settings (`key`, value) VALUES ('max_borrow_limit', '3');");

            // Insert default admin if not exists, or update it to ensure the password is correct
            String adminHash = com.kelompok1.service.Services.UserService.hashPassword("admin123");
            stmt.execute("INSERT INTO users (username, password_hash, full_name, email, phone, role, status) " +
                    "VALUES ('admin', '" + adminHash + "', 'System Administrator', 'admin@library.com', '000000', 'Admin', 'Active') " +
                    "ON DUPLICATE KEY UPDATE password_hash = '" + adminHash + "', status = 'Active';");

            // Auto-fill member_code for existing users (including the newly inserted admin)
            stmt.execute("UPDATE users SET member_code = CONCAT('MEM-', LPAD(CAST(user_id AS CHAR), 4, '0')) WHERE member_code IS NULL;");

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
