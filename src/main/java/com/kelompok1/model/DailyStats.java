package com.kelompok1.model;

public class DailyStats {
    public String date;
    public int borrowed;
    public int returned;

    public DailyStats(String date, int borrowed, int returned) {
        this.date = date;
        this.borrowed = borrowed;
        this.returned = returned;
    }
}
