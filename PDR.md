# Product Requirements Document: Library Management System

## 1. Product Overview

**Objective:** To develop a robust, serverless desktop Library Management System (LMS) that automates core library operations, including inventory control, user role management, transaction lifecycles, and automated reporting.
**Tech Stack Focus:** The application follows a Model-View-Controller (MVC) architecture built with Java SE, designed via the NetBeans GUI Builder (Matisse), styled with **FlatLaf** for a modern interface, utilizes **iReport** for document generation, and is powered by a lightweight **SQLite** database.

---

## 2. System Architecture & Core Technology

| Layer | Technology | Purpose |
| --- | --- | --- |
| **View (UI)** | Java Swing + FlatLaf | Replaces legacy Swing looks with a clean, modern flat design. Supports native scaling and Dark/Light modes. |
| **Controller** | Core Java | Handles event listeners, input validation, and routes requests to the database. |
| **Model (DB)** | SQLite (via JDBC) | Serverless, single-file relational database (`.db`) embedded with the app. Enforces data integrity. |
| **Reporting** | iReport / JasperReports | Compiles `.jrxml` templates into `.jasper` files for PDF generation and printing. |

---

## 3. Target Audience & User Workflows

### 3.1 Administrator (Librarian)

Has full access to manage the system, override rules, and view global reports.

* **Inventory Control:** Adds, updates, and deletes books. Assigns shelf locations (e.g., "Aisle 3, Rack B").
* **Circulation Desk:** Issues books (validating member borrowing limits and checking available stock) and processes returns.
* **System Overrides:** Can manually waive fines or force-unlock suspended member accounts.

### 3.2 Member (Patron/Student)

Has restricted access to an isolated dashboard tailored specifically to their account.

* **Discovery:** Uses global search to find books and view currently available copies. Cannot edit or delete records.
* **Account Management:** Views a grid of currently borrowed books with visual due-date indicators (e.g., red text for overdue).
* **Fine Ledger:** Views historical and active fines (payments are processed offline at the admin desk, but status is reflected here).

---

## 4. UI/UX Specifications (FlatLaf & NetBeans Integration)

The NetBeans GUI will be enhanced by initializing FlatLaf in the Java `main` method before rendering the UI components.

* **Theming:** Include a toggle switch allowing users to alternate between `FlatLightLaf` and `FlatDarkLaf`.
* **Component Styling:** Apply semantic colors for alerts (e.g., using FlatLaf's built-in error styling for `JTextField` borders during failed input validation).
* **Iconography:** Utilize `FlatSVGIcon` to render sharp, scalable vector graphics for dashboard menus instead of pixelated PNGs.
* **Dashboard Layouts:**
* **Admin MDI:** A modern sidebar navigation (using vertical `JToggleButton`s) controlling a main `CardLayout` content area to allow multitasking without cluttered overlapping windows.
* **Member Dashboard:** A simplified top-navigation bar utilizing `JTabbedPane` styled as modern pill-tabs containing "Search Catalog," "My Books," and "My Fines."



---

## 5. Reporting Requirements (iReport Integration)

Parameters (like `user_id` or `date_range`) will be passed from the Java application via a `HashMap` to the Jasper object. Data is populated using native SQLite queries.

* **Circulation Receipt:** A dynamic, narrow-format template designed for thermal receipt printers containing Member Name, Book Title, Issue Date, and Due Date.
* **Defaulters List:** A tabular report grouping users with outstanding fines, utilizing Jasper's grouping bands to sort by Membership Type.
* **Library Barcodes:** A label template utilizing iReport's native Barcode/QR Code elements to generate scannable stickers for book spines based on their primary keys.
* **Inventory Summary:** A complete PDF catalog of the library's holdings, sorted by category and author.

---

## 6. Database Architecture & Schema (SQLite)

### 6.1 Database Configuration & Logic

* **Connection Profile:** Uses the SQLite JDBC driver (`jdbc:sqlite:library.db`).
* **Foreign Keys:** SQLite disables foreign keys by default. The Java connection utility must execute `PRAGMA foreign_keys = ON;` upon connecting.
* **Stock Management:** `available_copies` count is decremented via Java upon issue and incremented upon return. A database `CHECK` constraint ensures stock cannot drop below `0`.
* **Fine Calculation:** Assessed when a `return_date` is stamped on a transaction based on the days elapsed since the `due_date`.

### 6.2 Complete SQLite DDL Script

```sql
-- 1. USERS TABLE
CREATE TABLE users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phone TEXT,
    role TEXT NOT NULL DEFAULT 'Member' CHECK(role IN ('Admin', 'Member')),
    status TEXT NOT NULL DEFAULT 'Active' CHECK(status IN ('Active', 'Suspended')),
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- 2. BOOKS TABLE
CREATE TABLE books (
    book_id INTEGER PRIMARY KEY AUTOINCREMENT,
    isbn TEXT NOT NULL UNIQUE,
    title TEXT NOT NULL,
    author TEXT NOT NULL,
    category TEXT NOT NULL,
    publisher TEXT,
    shelf_location TEXT,
    total_copies INTEGER NOT NULL CHECK (total_copies >= 0),
    available_copies INTEGER NOT NULL CHECK (available_copies <= total_copies),
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- 3. TRANSACTIONS TABLE
CREATE TABLE transactions (
    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    issue_date TEXT NOT NULL, 
    due_date TEXT NOT NULL,   
    return_date TEXT NULL,    
    status TEXT NOT NULL DEFAULT 'Issued' CHECK(status IN ('Issued', 'Returned', 'Lost')),
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT
);

-- 4. FINES TABLE
CREATE TABLE fines (
    fine_id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_id INTEGER NOT NULL UNIQUE,
    amount REAL NOT NULL DEFAULT 0.00 CHECK (amount >= 0),
    status TEXT NOT NULL DEFAULT 'Unpaid' CHECK(status IN ('Unpaid', 'Paid')),
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

```

### 6.3 Optimization (Indexes)

To ensure NetBeans `JTable` UI updates are instantaneous and iReport generation does not hang the application thread, the following indexes are required:

```sql
CREATE INDEX idx_book_search ON books(title, author, category);
CREATE INDEX idx_transaction_dates ON transactions(issue_date, due_date);
CREATE INDEX idx_fine_status ON fines(status);

```

---

## 7. Non-Functional Requirements

* **Data Integrity:** The system must utilize explicit SQLite transaction blocks (`COMMIT`/`ROLLBACK` via Java JDBC) to ensure operations like issuing a book only process if both the transaction log is created and the inventory is successfully decremented.
* **Security:** Passwords must be hashed using a strong algorithm (like BCrypt) before being passed to the `password_hash` column.
* **Portability:** The database `.db` file, Jasper report templates `.jasper`, and all external libraries (FlatLaf, SQLite JDBC) must be packaged correctly within the final compiled `.jar` distribution to ensure zero-configuration deployment on client machines.
* **Performance:** Generating a standard PDF report via iReport should take no longer than 3 seconds. Database search queries must populate the GUI grids in under 1.5 seconds.