package com.kelompok1.model;

public class Fine {
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
