package com.kelompok1;

import com.kelompok1.util.CsvSeeder;
import com.kelompok1.config.DatabaseHelper;

public class SeedRunner {
    public static void main(String[] args) {
        DatabaseHelper.initializeDatabase();
        CsvSeeder.seedBooksIfEmpty("Books.csv");
    }
}
