package com.kelompok1.service;

import com.kelompok1.dao.FineDAO;
import com.kelompok1.model.Fine;

import java.util.List;

public class FineService {
    private final FineDAO fineDAO;

    public FineService() {
        this.fineDAO = new FineDAO();
    }

    public List<Fine> getFinesByUser(int userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return fineDAO.getFinesByUser(userId, pageSize, offset);
    }

    public List<Fine> getAllFines(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return fineDAO.getAllFines(pageSize, offset);
    }

    public boolean payFine(int fineId) {
        return fineDAO.payFine(fineId);
    }

    public void assessFines() {
        fineDAO.assessFines();
    }

    public List<Fine> searchFines(String query, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return fineDAO.searchFines(query, pageSize, offset);
    }

    public double getTotalPendingFees() {
        return fineDAO.getTotalPendingFees();
    }
}
