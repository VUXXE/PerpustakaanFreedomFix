-- ============================================================
-- Script: seed_test_fines.sql
-- Purpose: Insert test fine data for Perpustakaan Freedom
-- ============================================================

-- Step 1: Make some existing 'Issued' transactions overdue
-- (set due_date to 5, 10, 15 days ago respectively)
UPDATE transactions
SET due_date = (CURRENT_DATE - INTERVAL '5 days')::text
WHERE transaction_id = (
    SELECT transaction_id FROM transactions WHERE status = 'Issued'
    ORDER BY transaction_id ASC LIMIT 1 OFFSET 0
);

UPDATE transactions
SET due_date = (CURRENT_DATE - INTERVAL '10 days')::text
WHERE transaction_id = (
    SELECT transaction_id FROM transactions WHERE status = 'Issued'
    ORDER BY transaction_id ASC LIMIT 1 OFFSET 1
);

UPDATE transactions
SET due_date = (CURRENT_DATE - INTERVAL '15 days')::text
WHERE transaction_id = (
    SELECT transaction_id FROM transactions WHERE status = 'Issued'
    ORDER BY transaction_id ASC LIMIT 1 OFFSET 2
);

-- Step 2: Insert fines for all overdue 'Issued' transactions
-- Uses fine_rate from settings (default: 5000/day)
INSERT INTO fines (transaction_id, amount)
SELECT
    t.transaction_id,
    (CURRENT_DATE - t.due_date::date) *
    COALESCE(
        (SELECT value::double precision FROM settings WHERE key = 'fine_rate'),
        5000.0
    )
FROM transactions t
WHERE t.status = 'Issued'
  AND CURRENT_DATE > t.due_date::date
ON CONFLICT (transaction_id) DO UPDATE
    SET amount = EXCLUDED.amount,
        updated_at = CURRENT_TIMESTAMP;

-- Step 3: Insert one already-PAID fine from a returned transaction
-- (creates a realistic mix of Paid + Unpaid)
INSERT INTO fines (transaction_id, amount, status)
SELECT
    t.transaction_id,
    35000,
    'Paid'
FROM transactions t
WHERE t.status = 'Returned'
  AND NOT EXISTS (SELECT 1 FROM fines f WHERE f.transaction_id = t.transaction_id)
ORDER BY t.transaction_id DESC
LIMIT 1
ON CONFLICT (transaction_id) DO NOTHING;

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
    (CURRENT_DATE - t.due_date::date) AS days_overdue
FROM fines f
JOIN transactions t ON f.transaction_id = t.transaction_id
JOIN users u ON t.user_id = u.user_id
JOIN books b ON t.book_id = b.book_id
ORDER BY f.fine_id DESC;
