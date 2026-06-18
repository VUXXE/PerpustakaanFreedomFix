# Perpustakaan Freedom

Aplikasi manajemen perpustakaan berbasis Java (GUI dengan FlatLaf) menggunakan database MySQL.

## Prasyarat
Sebelum menjalankan aplikasi ini, pastikan Anda telah menginstal:
1. **Java Development Kit (JDK)** versi 20 atau lebih baru.
2. **MySQL Server** (XAMPP, MAMP, atau MySQL Standalone).
3. **Maven** (Opsional, karena semua dependency *JAR* sudah disertakan di folder `target/dependency`).

## 1. Konfigurasi Database (MySQL)

1. Pastikan server MySQL Anda sedang berjalan.
2. Buat sebuah database kosong baru dengan nama `perpustakaan`. Anda bisa melakukannya melalui phpMyAdmin atau MySQL CLI:
   ```sql
   CREATE DATABASE perpustakaan;
   ```
3. Secara bawaan (*default*), aplikasi menggunakan kredensial MySQL berikut:
   - **URL:** `jdbc:mysql://localhost:3306/perpustakaan`
   - **Username:** `root`
   - **Password:** *(kosong)*
   
   Jika MySQL Anda menggunakan password atau username yang berbeda, ubah kredensial tersebut di dalam file:
   `src/main/java/com/kelompok1/config/DatabaseHelper.java`

## 2. Inisialisasi Data & Tabel (Setup)

Aplikasi ini dilengkapi dengan script setup otomatis untuk membuat struktur tabel dan mengimpor data buku dari `Books.csv`.

**Cara Menjalankan Setup:**
Jalankan class `DbSetup.java` yang berada di package `com.kelompok1.util`.
Jika Anda menggunakan terminal (pastikan Anda sudah men-compile programnya terlebih dahulu dengan `mvn clean compile` jika ada perubahan):
```bash
# Menjalankan utilitas DbSetup
java -cp "target/classes:target/dependency/*" com.kelompok1.util.DbSetup
```
*(Catatan untuk pengguna Windows: Gunakan `;` sebagai pemisah classpath, bukan `:`)*
```cmd
java -cp "target/classes;target/dependency/*" com.kelompok1.util.DbSetup
```
Tunggu hingga proses impor puluhan ribu baris data buku selesai.

## 3. Menjalankan Aplikasi Utama

Setelah database berhasil disiapkan, Anda dapat langsung masuk ke aplikasi utama.

**Cara Menjalankan:**
Jalankan class utama `PerpustakaanFreedom.java` yang berada di package `com.kelompok1`.
Melalui terminal:
```bash
# Linux / macOS
java -cp "target/classes:target/dependency/*" com.kelompok1.PerpustakaanFreedom

# Windows
java -cp "target/classes;target/dependency/*" com.kelompok1.PerpustakaanFreedom
```

### Kredensial Login Bawaan (Admin)
Setelah aplikasi terbuka, Anda dapat login menggunakan kredensial admin bawaan berikut:
- **Username:** `admin`
- **Password:** `admin123`

## Tambahan: Data Denda Palsu (Opsional)
Jika Anda ingin menguji fitur denda (fines) di dalam aplikasi, Anda dapat mengimpor file `seed_test_fines.sql` langsung ke database MySQL Anda menggunakan phpMyAdmin atau MySQL CLI. Script ini akan secara otomatis mengubah tanggal peminjaman menjadi kadaluwarsa dan membuat denda.
