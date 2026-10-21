CREATE DATABASE IF NOT EXISTS perpustakaan CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE perpustakaan;

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name TEXT NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone TEXT,
    role VARCHAR(50) NOT NULL DEFAULT 'Member' CHECK(role IN ('Admin', 'Member')),
    status VARCHAR(50) NOT NULL DEFAULT 'Active' CHECK(status IN ('Active', 'Suspended')),
    address TEXT,
    member_code VARCHAR(50) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. BOOKS TABLE
CREATE TABLE IF NOT EXISTS books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    series_title VARCHAR(500),
    title VARCHAR(500) NOT NULL,
    author VARCHAR(500),
    call_number VARCHAR(100),
    publisher VARCHAR(100),
    `collation` VARCHAR(255),
    language VARCHAR(50),
    isbn VARCHAR(100),
    classification VARCHAR(100),
    edition VARCHAR(100),
    total_copies INT DEFAULT 3,
    available_copies INT DEFAULT 3,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. TRANSACTIONS TABLE
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    user_id INT NOT NULL,
    issue_date TEXT NOT NULL,
    due_date TEXT NOT NULL,
    return_date TEXT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Issued' CHECK(status IN ('Issued', 'Returned', 'Lost')),
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT
);

-- 4. FINES TABLE
CREATE TABLE IF NOT EXISTS fines (
    fine_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL UNIQUE,
    amount REAL NOT NULL DEFAULT 0.00 CHECK (amount >= 0),
    status VARCHAR(50) NOT NULL DEFAULT 'Unpaid' CHECK(status IN ('Unpaid', 'Paid')),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

-- 5. SETTINGS TABLE
CREATE TABLE IF NOT EXISTS settings (
    `key` VARCHAR(255) PRIMARY KEY,
    value VARCHAR(255) NOT NULL
);

-- Seed default settings
INSERT IGNORE INTO settings (`key`, value) VALUES ('fine_rate', '5000');
INSERT IGNORE INTO settings (`key`, value) VALUES ('borrow_duration', '7');
INSERT IGNORE INTO settings (`key`, value) VALUES ('max_borrow_limit', '3');
