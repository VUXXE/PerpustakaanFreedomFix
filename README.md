<p align="center">
  <img src="LOGO.png" alt="Perpustakaan Freedom Logo" width="280">
</p>

<h1 align="center">Perpustakaan Freedom</h1>

<p align="center">
  <strong>Sistem Manajemen Perpustakaan Terpadu Modern berbasis Java Swing & MySQL</strong><br>
  <em>Tugas Akhir Mata Kuliah Pemrograman Visual — Kelompok 1</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Tugas%20Akhir-Pemrograman%20Visual-7952B3" alt="Tugas Akhir">
  <img src="https://img.shields.io/badge/Java-20%2B-ED8B00?logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Build-Maven-C71A36?logo=apache-maven&logoColor=white" alt="Maven">
  <img src="https://img.shields.io/badge/Database-MySQL%208.0%2B-4479A1?logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/UI-FlatLaf%203.4.1-1B73E8" alt="FlatLaf">
  <img src="https://img.shields.io/badge/Reporting-JasperReports%206.21.2-F37024" alt="JasperReports">
  <img src="https://img.shields.io/badge/Security-BCrypt-34A853" alt="BCrypt">
</p>

---

## 📖 Tentang Perpustakaan Freedom

**Perpustakaan Freedom** adalah aplikasi desktop manajemen perpustakaan komprehensif yang dirancang untuk mendigitalkan dan mengotomatiskan seluruh alur kerja operasional perpustakaan. Dibangun dengan antarmuka grafis modern bergaya *Academic Precision* menggunakan FlatLaf, aplikasi ini membagi hak akses pengguna ke dalam dua peran utama: **Administrator (Pustakawan)** dan **Anggota (Member)**.

Aplikasi ini mencakup seluruh siklus manajemen perpustakaan, mulai dari katalog buku, keanggotaan, sirkulasi peminjaman dan pengembalian, kalkulasi denda otomatis, analitik data visual dengan diagram interaktif, hingga penerbitan dokumen laporan resmi dan struk transaksi dalam format PDF.

---

## ✨ Fitur Utama

### 1. 🛡️ Akses Berbasis Peran (Role-Based Access Control)
* **Mode Administrator:**
  * **Dashboard Analitik:** Ringkasan metrik instan (total buku, pinjaman aktif, buku terlambat, total denda tertunggak/terbayar) dan visualisasi grafik tren aktivitas.
  * **Manajemen Katalog Buku:** Tambah, edit, hapus, dan cari koleksi buku lengkap dengan nomor panggil (*call number*), ISBN, klasifikasi DDC, penerbit, dan pelacakan ketersediaan eksemplar (*total & available copies*).
  * **Manajemen Anggota:** Pendaftaran anggota baru, pembuatan kode unik otomatis (`MEM-xxxx`), perbaruan profil, dan pengaturan status aktif/suspend (*Active/Suspended*).
  * **Sirkulasi & Transaksi:** Peminjaman dan pengembalian buku, kalkulasi tanggal jatuh tempo (*due date*) otomatis, penanganan status buku (Dipinjam, Dikembalikan, Hilang), serta cetak struk peminjaman.
  * **Manajemen Denda:** Perhitungan otomatis tarif denda per hari bagi transaksi terlambat, pencatatan status denda (*Unpaid / Paid*), pembayaran denda di tempat, dan cetak bukti pembayaran denda.
  * **Laporan & Dokumen PDF:** Cetak dokumen laporan resmi berformat PDF menggunakan template JasperReports (Laporan Buku, Laporan Anggota, Laporan Transaksi, Laporan Denda) serta fitur pratinjau langsung (*Report Viewer*).
  * **Konfigurasi Sistem:** Pengaturan tarif denda harian, batas durasi peminjaman standar, dan batas maksimum buku yang dapat dipinjam sekaligus.
* **Mode Anggota (Member):**
  * **Beranda Anggota:** Tinjauan informasi personal, nomor kartu anggota, dan status peminjaman terkini.
  * **Katalog & Pencarian Buku:** Eksplorasi koleksi perpustakaan dengan ketersediaan eksemplar real-time.
  * **Riwayat Peminjaman:** Pantau tanggal peminjaman, batas pengembalian, dan status buku.
  * **Catatan Denda Pribadi:** Transparansi tagihan denda keterlambatan buku.
  * **Pengaturan Akun:** Pembaruan nomor telepon, alamat, dan ganti kata sandi mandiri.

### 2. ⚡ Performa & Arsitektur
* **HikariCP Connection Pool:** Manajemen koneksi database berkecepatan tinggi, tangguh, dan efisien.
* **Keamanan Sandi BCrypt:** Password dienkripsi menggunakan hashing BCrypt (salt rounds 12).
* **Inisialisasi Database Otomatis:** Tabel dan akun default dibuat secara otomatis saat aplikasi pertama kali dijalankan tanpa perlu impor SQL manual.
* **Offline-Ready Maven Repository:** Dilengkapi repositori lokal `local-repo/` dengan konfigurasi `-Dmaven.repo.local=local-repo` pada `.mvn/maven.config`, memungkinkan kompilasi dan eksekusi berjalan lancar bahkan tanpa jaringan internet.

---

## 🛠️ Tumpukan Teknologi (Tech Stack)

| Komponen | Teknologi / Pustaka | Deskripsi |
|---|---|---|
| **Bahasa & Platform** | Java 20+ (LTS Java 21 didukung) | Logika sistem dan pemrograman berbasis objek |
| **Build Tool** | Apache Maven 3.9+ | Manajemen dependensi dan siklus pembangunan proyek |
| **GUI Framework** | Java Swing + FlatLaf 3.4.1 | Antarmuka pengguna modern bernuansa terang (*Light Theme*) |
| **Design System** | Academic Precision Tokens | Palet warna dan tipografi konsisten |
| **Database** | MySQL 8.0+ / MariaDB | Penyimpanan data relasional |
| **Database Pool** | HikariCP 5.1.0 | Connection pooling performa tinggi |
| **Kriptografi** | BCrypt (at.favre.lib 0.10.2) | Pengacakan kata sandi satu arah yang aman |
| **Laporan & Dokumen** | JasperReports 6.21.2 | Pembuatan laporan PDF dan struk transaksi |
| **Visualisasi Data** | JFreeChart 1.5.4 | Diagram batang dan grafik statistik analitik |

---

## 📂 Struktur Proyek

```text
PerpustakaanFreedomFix/
├── .mvn/
│   └── maven.config                      # Konfigurasi repositori lokal Maven (-Dmaven.repo.local)
├── local-repo/                           # Paket JAR dependensi Maven offline
├── src/
│   └── main/
│       ├── java/com/kelompok1/
│       │   ├── config/
│       │   │   └── DatabaseHelper.java   # Konfigurasi HikariCP & inisialisasi tabel DB
│       │   ├── model/                    # Entity model (Book, User, Transaction, Fine, DailyStats)
│       │   ├── report/
│       │   │   └── ReportGenerator.java  # Kompiler & engine ekspor JasperReports ke PDF
│       │   ├── service/                  # Business logic (BookService, UserService, TransactionService, dsb.)
│       │   ├── ui/
│       │   │   ├── AdminDashboard.java   # Jendela dashboard administrator utama
│       │   │   ├── MemberDashboard.java  # Jendela dashboard pengguna anggota
│       │   │   ├── LoginView.java        # Form login dengan autentikasi ganda
│       │   │   └── panel/                # Panel konten modular (Books, Members, Fines, Reports, dsb.)
│       │   └── util/
│       │       ├── DataPopulator.java    # Generator data contoh/dummy ke database
│       │       └── DesignSystem.java     # Token UI, palet warna, dan styling FlatLaf
│       └── resources/
│           ├── images/                   # Asset visual latar belakang form login
│           └── reports/                  # Template laporan resmi (*.jrxml)
├── db_setup.sql                          # Skrip referensi skema database
├── dummy_data.sql                        # Skrip contoh data buku dan anggota
├── seed_users.sql                        # Skrip data pengguna awal
├── seed_test_fines.sql                   # Skrip simulasi keterlambatan & denda
├── pom.xml                               # File konfigurasi Maven
└── README.md                             # Dokumentasi proyek
```

---

## 🚀 Panduan Memulai & Instalasi

### 1. Prasyarat Sistem
Sebelum memulai, pastikan perangkat Anda telah terpasang:
* **Java Development Kit (JDK):** Versi 20 atau lebih baru (disarankan OpenJDK / Oracle JDK 21).
* **MySQL Database Server:** Versi 8.0+ (dapat menggunakan XAMPP, Laragon, Docker, atau MySQL Server standalone).
* **Apache Maven:** Versi 3.8+ (opsional jika menggunakan IDE seperti NetBeans atau IntelliJ IDEA).

### 2. Kloning Repositori
```bash
git clone https://github.com/VUXXE/PerpustakaanFreedomFix.git
cd PerpustakaanFreedomFix
```

### 3. Konfigurasi Database MySQL
1. Nyalakan layanan MySQL Anda (misalnya via XAMPP Control Panel).
2. Buat database baru bernama `perpustakaan`:
   ```sql
   CREATE DATABASE perpustakaan;
   ```
3. Secara default, aplikasi mengarah ke kredensial:
   * **Host:** `localhost:3306`
   * **Database:** `perpustakaan`
   * **Username:** `root`
   * **Password:** *(kosong)*
4. Jika server MySQL Anda menggunakan user/password berbeda, sesuaikan variabel di [DatabaseHelper.java](src/main/java/com/kelompok1/config/DatabaseHelper.java):
   ```java
   private static final String DB_URL = "jdbc:mysql://localhost:3306/perpustakaan?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
   private static final String DB_USER = "root";
   private static final String DB_PASSWORD = "password_anda_disini";
   ```

> [!NOTE]
> Anda **tidak perlu mengimpor file SQL skema secara manual**. Aplikasi memiliki fitur *auto-initialization* yang akan membuat tabel-tabel yang dibutuhkan dan akun admin secara otomatis saat aplikasi pertama kali dijalankan.

### 4. Mengisi Data Contoh (Opsional)
Jika Anda ingin langsung menguji aplikasi dengan data contoh buku, anggota, peminjaman aktif, dan denda:
* **Cara A (Melalui Java Runner):**
  Jalankan class [`com.kelompok1.util.DataPopulator`](src/main/java/com/kelompok1/util/DataPopulator.java).
* **Cara B (Melalui File SQL):**
  Impor file `dummy_data.sql` dan `seed_users.sql` ke database `perpustakaan` menggunakan phpMyAdmin atau MySQL Workbench / CLI.

---

## 💻 Cara Menjalankan Aplikasi

### Metode 1: Menggunakan Terminal / Command Line (Maven)
Pastikan berada pada direktori root proyek:

```bash
# 1. Kompilasi aplikasi
mvn compile

# 2. Jalankan aplikasi utama
mvn exec:java
```

### Metode 2: Menggunakan Apache NetBeans
1. Buka **Apache NetBeans**.
2. Pilih menu **File** > **Open Project...**
3. Arahkan ke folder proyek ini (`PerpustakaanFreedomFix`) dan klik **Open Project**.
4. Di panel *Projects*, buka direktori `Source Packages` > `com.kelompok1`.
5. Klik kanan pada file [`PerpustakaanFreedom.java`](src/main/java/com/kelompok1/PerpustakaanFreedom.java) lalu pilih **Run File** (atau tekan `Shift + F6`).

### Metode 3: Menggunakan IntelliJ IDEA
1. Buka IntelliJ IDEA, pilih **Open**, lalu pilih folder proyek `PerpustakaanFreedomFix`.
2. Tunggu IntelliJ mendeteksi file `pom.xml` dan menyelaraskan dependensi Maven.
3. Buka file `src/main/java/com/kelompok1/PerpustakaanFreedom.java`.
4. Klik tombol ikon hijau **Play / Run** di samping fungsi `public static void main`.

---

## 🔑 Kredensial Masuk Bawaan

Saat inisialisasi awal database, sistem secara otomatis mendaftarkan akun Administrator bawaan:

| Peran (Role) | Username | Password | Keterangan |
|---|---|---|---|
| **Administrator** | `admin` | `admin123` | Akses penuh ke seluruh menu & pengaturan |
| **Member (Contoh)** | `budi_s` | *(gunakan data dummy)* | Akses katalog & riwayat anggota |

---

## 📊 Laporan & Cetak PDF

Aplikasi ini telah terintegrasi dengan mesin pelaporan JasperReports yang memungkinkan pencetakan langsung dokumen PDF atau pratinjau interaktif:
* **Laporan Inventaris Buku:** Menampilkan daftar katalog buku, ISBN, klasifikasi, serta eksemplar tersedia.
* **Laporan Keanggotaan:** Menampilkan data seluruh anggota terdaftar beserta status keaktifan.
* **Laporan Sirkulasi & Transaksi:** Riwayat peminjaman, jatuh tempo, serta status pengembalian buku.
* **Laporan Denda:** Laporan denda tertunggak (*defaulters report*) dan denda yang telah diselesaikan.
* **Struk Peminjaman & Bukti Denda:** Slip ringkas berukuran kompak untuk diberikan kepada peminjam.

---

## 🎯 Pemenuhan Kompetensi Mata Kuliah Pemrograman Visual

Aplikasi ini mendemonstrasikan implementasi menyeluruh konsep-konsep kunci dalam **Pemrograman Visual**:
1. **Desain Antarmuka Grafis (GUI Layout & Styling):** Pemanfaatan komponen Java Swing modern (`JFrame`, `JPanel`, `JTable`, `JTabbedPane`, `CardLayout`, `GridBagLayout`) yang dipadukan dengan pustaka FlatLaf dan sistem desain terstandarisasi ([`DesignSystem.java`](src/main/java/com/kelompok1/util/DesignSystem.java)).
2. **Event-Driven Programming:** Penanganan aksi pengguna secara asinkron dan terstruktur melalui event listeners (`ActionListener`, `ChangeListener`, `DocumentListener`) pada Event Dispatch Thread (EDT).
3. **Konektivitas Basis Data Relasional:** Operasi CRUD (*Create, Read, Update, Delete*) lengkap pada entitas relasional (Buku, Pengguna/Anggota, Transaksi Sirkulasi, Denda) dengan arsitektur berkinerja tinggi menggunakan HikariCP Connection Pool.
4. **Validasi Formulir & Kriptografi:** Validasi masukan data formulir secara visual serta pengamanan kata sandi menggunakan hashing standar industri (BCrypt).
5. **Pelaporan & Cetak Dokumen (Reporting):** Integrasi JasperReports untuk pembuatan laporan resmi berformat PDF serta struk transaksi instan.
6. **Visualisasi Data Dinamis:** Pembuatan diagram analitik statistik (*bar chart* dan *line chart*) menggunakan JFreeChart.

---

## 🎓 Informasi Akademik & Pengembang

Proyek ini disusun dan diajukan untuk memenuhi penilaian **Tugas Akhir Mata Kuliah Pemrograman Visual**.

* **Mata Kuliah:** Pemrograman Visual
* **Kelompok:** Kelompok 1
* **Nama Aplikasi:** Sistem Manajemen Perpustakaan Freedom
* **Repositori GitHub:** [VUXXE/PerpustakaanFreedomFix](https://github.com/VUXXE/PerpustakaanFreedomFix)

### 👥 Anggota Tim Kelompok 1

| No | Nama Mahasiswa | NIM | Peran / Pembagian Tugas |
|:--:|---|:---:|---|
| 1 | *(Nama Mahasiswa 1)* | `NIM-1` | Koordinator Proyek & Desain Antarmuka UI/UX (FlatLaf Swing) |
| 2 | *(Nama Mahasiswa 2)* | `NIM-2` | Backend Services & Integrasi Database MySQL (HikariCP) |
| 3 | *(Nama Mahasiswa 3)* | `NIM-3` | Pembuatan Template JasperReports (PDF) & Visualisasi JFreeChart |
| 4 | *(Nama Mahasiswa 4)* | `NIM-4` | Pengujian Sistem (*QA*), Data Populator, & Dokumentasi Teknis |

> [!TIP]
> Anda dapat melengkapi tabel nama dan NIM di atas sesuai dengan daftar anggota resmi kelompok Anda sebelum pengumpulan tugas akhir.

---

## 📄 Lisensi

Proyek ini dikembangkan khusus untuk kepentingan akademik dan penyelesaian Tugas Akhir Mata Kuliah Pemrograman Visual. Hak cipta dilindungi undang-undang kelompok pengembang.
