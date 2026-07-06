package com.kelompok1.model;

public class Book {
    private int bookId;
    private String seriesTitle;
    private String title;
    private String author;
    private String callNumber;
    private String publisher;
    private String collation;
    private String language;
    private String isbn;
    private String classification;
    private String edition;
    private int totalCopies;
    private int availableCopies;
    private String createdAt;
    private int checkoutCount;

    public Book() {}

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getSeriesTitle() { return seriesTitle; }
    public void setSeriesTitle(String seriesTitle) { this.seriesTitle = seriesTitle; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCallNumber() { return callNumber; }
    public void setCallNumber(String callNumber) { this.callNumber = callNumber; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getCollation() { return collation; }
    public void setCollation(String collation) { this.collation = collation; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getCheckoutCount() { return checkoutCount; }
    public void setCheckoutCount(int checkoutCount) { this.checkoutCount = checkoutCount; }
}
