-- Script to create a books table and import data from Books.csv using PostgreSQL
-- To run this script, open terminal and run:
-- psql "postgresql://postgres:021177Hersa021177@db.darwxjnptymxwcpjwstr.supabase.co:5432/postgres" -f db_setup.sql

-- 1. Create a temporary table to hold all 29 columns from the CSV
DROP TABLE IF EXISTS temp_books_csv;
CREATE TABLE temp_books_csv (
    biblio_id TEXT, gmd_id TEXT, title TEXT, sor TEXT, edition TEXT,
    isbn_issn TEXT, publisher_id TEXT, publish_year TEXT, collation TEXT,
    series_title TEXT, call_number TEXT, language_id TEXT, source TEXT,
    publish_place_id TEXT, classification TEXT, notes TEXT, image TEXT,
    file_att TEXT, opac_hide TEXT, promoted TEXT, labels TEXT,
    frequency_id TEXT, spec_detail_info TEXT, content_type_id TEXT,
    media_type_id TEXT, carrier_type_id TEXT, input_date TEXT,
    last_update TEXT, uid TEXT
);

-- 2. Import all data from CSV into the temporary table
-- Note: LOAD DATA LOCAL INFILE requires the mysql client to be run with --local-infile=1
LOAD DATA LOCAL INFILE 'Books.csv' INTO TABLE temp_books_csv FIELDS TERMINATED BY ';' ENCLOSED BY '"' IGNORE 1 LINES;

-- 3. Create the clean table with exactly the columns requested
CREATE TABLE IF NOT EXISTS books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    series_title VARCHAR(500),
    title VARCHAR(500) NOT NULL,
    author VARCHAR(500),
    call_number VARCHAR(100),
    publisher VARCHAR(100),
    collation VARCHAR(255),
    language VARCHAR(50),
    isbn VARCHAR(100),
    classification VARCHAR(100),
    edition VARCHAR(100),
    total_copies INT DEFAULT 3,
    available_copies INT DEFAULT 3,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Clear the table if it already exists before importing
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE books;
SET FOREIGN_KEY_CHECKS = 1;

-- 4. Transfer only the requested columns from temp to the actual table
INSERT INTO books (
    series_title, title, author, call_number, publisher, 
    collation, language, isbn, classification, edition
)
SELECT 
    series_title, 
    title, 
    sor,
    call_number, 
    publisher_id, 
    collation, 
    language_id, 
    isbn_issn, 
    classification, 
    edition 
FROM temp_books_csv;

-- 5. Clean up temporary table
DROP TABLE temp_books_csv;
