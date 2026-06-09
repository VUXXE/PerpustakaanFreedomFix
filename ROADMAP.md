# Perpustakaan Freedom - Feature Roadmap

This document outlines the features and technical progress of the library management system.

## Completed Features

### 1. Core Data Management (CRUD Operations) [COMPLETED]
- **Books Management:** Added forms and dialogs to fully `Add` (Tambah Buku), `Edit` (Ubah), and `Delete` (Hapus) books.
- **Members Management:** Added inline forms using `CardLayout` to register new members, update details, and change roles/statuses.

### 2. Library Circulation System (Peminjaman & Pengembalian) [COMPLETED]
- **Issue Book UI:** Implemented a checkout dialog utilizing the custom member code (`MEM-XXXX`) and verifying book availability.
- **Return Book UI:** Implemented a return action that computes overdue days, triggers late denda, and updates records.

### 3. Search and Filtering [COMPLETED]
- **Single Header Search Bar:** Unified search into a single input bar in the dashboard header. Contextual behavior executes appropriate queries based on the active pane.

### 4. Data Pagination [COMPLETED]
- Implemented SQL-level pagination (`LIMIT` and `OFFSET`) for all main tables, using UI buttons ("Sebelumnya" / "Selanjutnya") to efficiently navigate massive collections (e.g. 14,000+ books).

### 5. Fines (Denda) Management Module [COMPLETED]
- Created a dedicated `FinesPanel` (Manajemen Denda) to check user denda ledgers and process payments.

### 6. Unique Member Identification [COMPLETED]
- Implemented sequential alphanumeric member IDs (`MEM-0001` format) acting as a clean facade over integer primary keys.

### 7. UI Translation [COMPLETED]
- Fully translated the entire application from English to **Bahasa Indonesia**.

---

## Remaining / Future Enhancements

### 1. Settings Module Configuration
- Replace the "Panel Pengaturan - Segera Hadir" card with configuration fields for global settings (e.g., daily fine rate, borrowing duration, maximum book limits).

### 2. Help and Support Panel Expansion
- Expand the static support details into an interactive user manual.
