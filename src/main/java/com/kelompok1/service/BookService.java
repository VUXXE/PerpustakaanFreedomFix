package com.kelompok1.service;

import com.kelompok1.dao.BookDAO;
import com.kelompok1.model.Book;

import java.sql.SQLException;
import java.util.List;

public class BookService {
    private final BookDAO bookDAO;

    public BookService() {
        this.bookDAO = new BookDAO();
    }

    public boolean addBook(Book book) {
        return bookDAO.addBook(book);
    }

    public List<Book> getAllBooks(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return bookDAO.getAllBooks(pageSize, offset);
    }

    public Book getBookById(int bookId) {
        return bookDAO.getBookById(bookId);
    }

    public boolean updateBook(Book book) {
        return bookDAO.updateBook(book);
    }

    public boolean deleteBook(int bookId) throws SQLException {
        return bookDAO.deleteBook(bookId);
    }

    public List<Book> searchBooks(String query, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return bookDAO.searchBooks(query, pageSize, offset);
    }

    public int getTotalBooksCount() {
        return bookDAO.getTotalBooksCount();
    }

    public List<Book> getTopBooks(int limit) {
        return bookDAO.getTopBooks(limit);
    }
}
