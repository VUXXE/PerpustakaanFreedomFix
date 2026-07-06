INSERT IGNORE INTO users (username, password_hash, full_name, email, phone, role, status, address, member_code) VALUES
('budi_s', '$2a$10$r9T9VdG/3pT.wG3Yc3H2A.pP/K5sN7qA0yK7J4mR2xJ9dJ3lFzN1W', 'Budi Santoso', 'budi@example.com', '081234567890', 'Member', 'Active', 'Jl. Merdeka No. 10, Jakarta', 'MEMBER-1001'),
('siti_a', '$2a$10$r9T9VdG/3pT.wG3Yc3H2A.pP/K5sN7qA0yK7J4mR2xJ9dJ3lFzN1W', 'Siti Aminah', 'siti@example.com', '081298765432', 'Member', 'Active', 'Jl. Melati No. 5, Bandung', 'MEMBER-1002'),
('andi_r', '$2a$10$r9T9VdG/3pT.wG3Yc3H2A.pP/K5sN7qA0yK7J4mR2xJ9dJ3lFzN1W', 'Andi Rahman', 'andi@example.com', '085612345678', 'Member', 'Active', 'Jl. Pahlawan No. 2, Surabaya', 'MEMBER-1003'),
('rara_k', '$2a$10$r9T9VdG/3pT.wG3Yc3H2A.pP/K5sN7qA0yK7J4mR2xJ9dJ3lFzN1W', 'Rara Kirana', 'rara@example.com', '087812345678', 'Member', 'Active', 'Jl. Kenangan No. 99, Yogyakarta', 'MEMBER-1004'),
('joko_w', '$2a$10$r9T9VdG/3pT.wG3Yc3H2A.pP/K5sN7qA0yK7J4mR2xJ9dJ3lFzN1W', 'Joko Widodo', 'joko@example.com', '081198765432', 'Member', 'Active', 'Jl. Raya Bogor No. 12, Bogor', 'MEMBER-1005');

INSERT IGNORE INTO books (title, author, call_number, publisher, language, isbn, classification, edition, total_copies, available_copies) VALUES
('Laskar Pelangi', 'Andrea Hirata', 'IND-FIC-001', 'Bentang Pustaka', 'Indonesia', '978-979-3062-79-2', '899.221', 'Pertama', 5, 5),
('Bumi Manusia', 'Pramoedya Ananta Toer', 'IND-FIC-002', 'Hasta Mitra', 'Indonesia', '978-979-97312-3-4', '899.221', 'Revisi', 3, 3),
('Clean Code', 'Robert C. Martin', 'COMP-001', 'Prentice Hall', 'English', '978-0132350884', '005.13', 'First', 2, 2),
('Filosofi Teras', 'Henry Manampiring', 'PHI-001', 'Kompas Ilmu', 'Indonesia', '978-602-412-518-9', '100', 'Cetakan ke-12', 4, 4),
('Sapiens: Riwayat Singkat Umat Manusia', 'Yuval Noah Harari', 'HIS-001', 'KPG', 'Indonesia', '978-602-424-697-6', '900', 'Ketiga', 6, 6),
('Harry Potter dan Batu Bertuah', 'J.K. Rowling', 'FIC-003', 'Gramedia', 'Indonesia', '978-979-22-6815-7', '823', 'Pertama', 2, 2),
('Atomic Habits', 'James Clear', 'SELF-001', 'Gramedia Pustaka Utama', 'Indonesia', '978-602-06-3317-6', '158.1', 'Revisi', 10, 10),
('Dune', 'Frank Herbert', 'FIC-004', 'Chilton Books', 'English', '978-0441172719', '813', 'First', 1, 1);
