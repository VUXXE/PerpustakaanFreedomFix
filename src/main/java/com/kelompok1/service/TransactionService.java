package com.kelompok1.service;

import com.kelompok1.dao.TransactionDAO;
import com.kelompok1.model.DailyStats;
import com.kelompok1.model.Transaction;

import java.util.List;
import java.util.Map;

public class TransactionService {
    private final TransactionDAO transactionDAO;

    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
    }

    public int issueBook(int userId, int bookId, int daysToBorrow) {
        return transactionDAO.issueBook(userId, bookId, daysToBorrow);
    }

    public boolean returnBook(int transactionId, int bookId) {
        return transactionDAO.returnBook(transactionId, bookId);
    }

    public List<Transaction> getTransactionsByUser(int userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return transactionDAO.getTransactionsByUser(userId, pageSize, offset);
    }

    public List<Transaction> getAllTransactions(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return transactionDAO.getAllTransactions(pageSize, offset);
    }

    public List<Transaction> searchTransactions(String query, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return transactionDAO.searchTransactions(query, pageSize, offset);
    }

    public int getCountByStatus(String status) {
        return transactionDAO.getCountByStatus(status);
    }

    public int getOverdueCount() {
        return transactionDAO.getOverdueCount();
    }

    public int getLostCount() {
        return transactionDAO.getLostCount();
    }

    public List<DailyStats> getCheckoutStats() {
        return transactionDAO.getCheckoutStats();
    }

    public List<Transaction> getRecentCheckouts(int limit) {
        return transactionDAO.getRecentCheckouts(limit);
    }

    public List<Map<String, Object>> getOverdueHistory(int limit) {
        return transactionDAO.getOverdueHistory(limit);
    }

    public int getActiveLoansCount(int userId) {
        return transactionDAO.getActiveLoansCount(userId);
    }
}
