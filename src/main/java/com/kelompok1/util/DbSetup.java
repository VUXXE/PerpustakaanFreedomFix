package com.kelompok1.util;

import com.kelompok1.config.DatabaseHelper;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;

public class DbSetup {
    public static void main(String[] args) {
        System.out.println("Starting Database Setup...");
        
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("0. Dropping all existing tables to start fresh...");
            stmt.execute("DROP TABLE IF EXISTS fines CASCADE");
            stmt.execute("DROP TABLE IF EXISTS transactions CASCADE");
            stmt.execute("DROP TABLE IF EXISTS books CASCADE");
            stmt.execute("DROP TABLE IF EXISTS users CASCADE");
            
            System.out.println("0.5. Re-initializing database schemas...");
            DatabaseHelper.initializeDatabase();

            System.out.println("1. Creating temporary table...");
            stmt.execute("DROP TABLE IF EXISTS temp_books_csv CASCADE");
            stmt.execute("CREATE TABLE temp_books_csv (" +
                    "biblio_id TEXT, gmd_id TEXT, title TEXT, sor TEXT, edition TEXT, " +
                    "isbn_issn TEXT, publisher_id TEXT, publish_year TEXT, \"collation\" TEXT, " +
                    "series_title TEXT, call_number TEXT, language_id TEXT, source TEXT, " +
                    "publish_place_id TEXT, classification TEXT, notes TEXT, image TEXT, " +
                    "file_att TEXT, opac_hide TEXT, promoted TEXT, labels TEXT, " +
                    "frequency_id TEXT, spec_detail_info TEXT, content_type_id TEXT, " +
                    "media_type_id TEXT, carrier_type_id TEXT, input_date TEXT, " +
                    "last_update TEXT, uid TEXT)");
            
            System.out.println("2. Importing data from Books.csv (This might take a few seconds)...");
            CopyManager copyManager = new CopyManager(conn.unwrap(BaseConnection.class));
            try (FileReader reader = new FileReader("Books.csv")) {
                long rowsInserted = copyManager.copyIn("COPY temp_books_csv FROM STDIN WITH (FORMAT csv, DELIMITER ';', HEADER true, QUOTE '\"')", reader);
                System.out.println("Imported " + rowsInserted + " rows to temporary table.");
            }

            stmt.execute("TRUNCATE TABLE books CASCADE");

            System.out.println("4. Transferring mapped data to main books table...");
            stmt.execute("INSERT INTO books (" +
                    "series_title, title, author, call_number, publisher, " +
                    "\"collation\", language, isbn, classification, edition) " +
                    "SELECT " +
                    "series_title, title, sor, call_number, publisher_id, " +
                    "\"collation\", language_id, isbn_issn, classification, edition " +
                    "FROM temp_books_csv");

            System.out.println("5. Cleaning up temporary tables...");
            stmt.execute("DROP TABLE temp_books_csv");

            System.out.println("SUCCESS: Database has been fully setup and populated!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("FAILED: An error occurred.");
        }
    }
}
