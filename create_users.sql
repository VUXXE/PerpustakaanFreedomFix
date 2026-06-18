CREATE DATABASE IF NOT EXISTS perpustakaan;
USE perpustakaan;
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    role ENUM('Admin', 'Member') DEFAULT 'Member',
    member_code VARCHAR(20) UNIQUE,
    status ENUM('Active', 'Suspended') DEFAULT 'Active',
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
