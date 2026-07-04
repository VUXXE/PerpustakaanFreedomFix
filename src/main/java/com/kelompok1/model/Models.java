package com.kelompok1.model;

public class Models {

    public static class User {
        private int userId;
        private String memberCode;
        private String username;
        private String passwordHash;
        private String fullName;
        private String email;
        private String phone;
        private String role;
        private String status;
        private String address;
        private String createdAt;

        public User() {}

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        
        public String getMemberCode() { return memberCode; }
        public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPasswordHash() { return passwordHash; }
        public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    public static class Book {
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

    public static class Transaction {
        private int transactionId;
        private int bookId;
        private int userId;
        private String issueDate;
        private String dueDate;
        private String returnDate;
        private String status;

        private String bookTitle;
        private String memberName;
        private String memberCode;

        public Transaction() {}

        public int getTransactionId() { return transactionId; }
        public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

        public int getBookId() { return bookId; }
        public void setBookId(int bookId) { this.bookId = bookId; }

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

        public String getIssueDate() { return issueDate; }
        public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }

        public String getReturnDate() { return returnDate; }
        public void setReturnDate(String returnDate) { this.returnDate = returnDate; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getBookTitle() { return bookTitle; }
        public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

        public String getMemberName() { return memberName; }
        public void setMemberName(String memberName) { this.memberName = memberName; }

        public String getMemberCode() { return memberCode; }
        public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    }

    public static class Fine {
        private int fineId;
        private int transactionId;
        private double amount;
        private String status;
        private String updatedAt;

        private String memberName;
        private String memberCode;
        private String bookTitle;

        public Fine() {}

        public int getFineId() { return fineId; }
        public void setFineId(int fineId) { this.fineId = fineId; }

        public int getTransactionId() { return transactionId; }
        public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

        public String getMemberName() { return memberName; }
        public void setMemberName(String memberName) { this.memberName = memberName; }

        public String getBookTitle() { return bookTitle; }
        public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

        public String getMemberCode() { return memberCode; }
        public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    }

    public static class DailyStats {
        public String date;
        public int borrowed;
        public int returned;

        public DailyStats(String date, int borrowed, int returned) {
            this.date = date;
            this.borrowed = borrowed;
            this.returned = returned;
        }
    }
}
