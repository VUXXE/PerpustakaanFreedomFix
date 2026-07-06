-- ============================================================
-- Script: seed_test_fines.sql
-- Purpose: Insert test fine data for Perpustakaan Freedom
-- ============================================================

-- Step 1: Make some existing 'Issued' transactions overdue
-- (set due_date to 5, 10, 15 days ago respectively)
UPDATE transactions
SET due_date = CAST(DATE_SUB(CURDATE(), INTERVAL 5 DAY) AS CHAR)
WHERE transaction_id = (
    SELECT transaction_id FROM transactions WHERE status = 'Issued'
    ORDER BY transaction_id ASC LIMIT 1 OFFSET 0
);

UPDATE transactions
SET due_date = CAST(DATE_SUB(CURDATE(), INTERVAL 10 DAY) AS CHAR)
WHERE transaction_id = (
    SELECT transaction_id FROM transactions WHERE status = 'Issued'
    ORDER BY transaction_id ASC LIMIT 1 OFFSET 1
);

UPDATE transactions
SET due_date = CAST(DATE_SUB(CURDATE(), INTERVAL 15 DAY) AS CHAR)
WHERE transaction_id = (
    SELECT transaction_id FROM transactions WHERE status = 'Issued'
    ORDER BY transaction_id ASC LIMIT 1 OFFSET 2
);

-- Step 2: Insert fines for all overdue 'Issued' transactions
-- Uses fine_rate from settings (default: 5000/day)
INSERT INTO fines (transaction_id, amount)
SELECT
    t.transaction_id,
    DATEDIFF(CURDATE(), CAST(t.due_date AS DATE)) *
    COALESCE(
        (SELECT CAST(value AS DECIMAL(10,2)) FROM settings WHERE `key` = 'fine_rate'),
        5000.0
    )
FROM transactions t
WHERE t.status = 'Issued'
  AND CURDATE() > CAST(t.due_date AS DATE)
ON DUPLICATE KEY UPDATE
    amount = VALUES(amount),
    updated_at = CURRENT_TIMESTAMP;

-- Step 3: Insert one already-PAID fine from a returned transaction
-- (creates a realistic mix of Paid + Unpaid)
INSERT IGNORE INTO fines (transaction_id, amount, status)
SELECT
    t.transaction_id,
    35000,
    'Paid'
FROM transactions t
WHERE t.status = 'Returned'
  AND NOT EXISTS (SELECT 1 FROM fines f WHERE f.transaction_id = t.transaction_id)
ORDER BY t.transaction_id DESC
LIMIT 1;

-- Step 4: Preview the result
SELECT
    f.fine_id,
    u.member_code,
    u.full_name,
    b.title,
    f.amount,
    f.status,
    f.updated_at,
    t.due_date,
    DATEDIFF(CURDATE(), CAST(t.due_date AS DATE)) AS days_overdue
FROM fines f
JOIN transactions t ON f.transaction_id = t.transaction_id
JOIN users u ON t.user_id = u.user_id
JOIN books b ON t.book_id = b.book_id
ORDER BY f.fine_id DESC;

-- Step 5: Tambahan 20 dummy denda untuk testing
-- Kita buat 20 transaksi baru secara acak menggunakan cross join (dibatasi 20)
INSERT INTO transactions (user_id, book_id, issue_date, due_date, return_date, status)
SELECT u.user_id, b.book_id, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 16 DAY), DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'Returned'
FROM users u 
JOIN books b 
LIMIT 20;

-- Setengah lunas (10)
INSERT INTO fines (transaction_id, amount, status, updated_at)
SELECT transaction_id, (RAND() * 50000) + 5000, 'Paid', CURRENT_TIMESTAMP
FROM transactions
ORDER BY transaction_id DESC
LIMIT 10;

-- Setengah belum lunas (10)
INSERT INTO fines (transaction_id, amount, status, updated_at)
SELECT transaction_id, (RAND() * 50000) + 5000, 'Unpaid', CURRENT_TIMESTAMP
FROM transactions
WHERE transaction_id NOT IN (
    SELECT transaction_id FROM fines
)
ORDER BY transaction_id DESC
LIMIT 10;
