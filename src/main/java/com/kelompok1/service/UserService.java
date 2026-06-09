package com.kelompok1.service;

import com.kelompok1.dao.UserDAO;
import com.kelompok1.model.User;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public User authenticate(String username, String password) {
        return userDAO.authenticate(username, password);
    }

    public boolean addUser(User user) throws SQLException {
        return userDAO.addUser(user);
    }

    public List<User> getAllUsers(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return userDAO.getAllUsers(pageSize, offset);
    }

    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }
    
    public User getUserByMemberCode(String memberCode) {
        return userDAO.getUserByMemberCode(memberCode);
    }

    public boolean updateUser(User user, boolean changePassword) throws SQLException {
        return userDAO.updateUser(user, changePassword);
    }

    public boolean deleteUser(int userId) throws SQLException {
        return userDAO.deleteUser(userId);
    }

    public List<User> searchUsers(String query, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return userDAO.searchUsers(query, pageSize, offset);
    }

    public int getTotalMembersCount() {
        return userDAO.getTotalMembersCount();
    }

    public int getNewMembersCount() {
        return userDAO.getNewMembersCount();
    }
}
