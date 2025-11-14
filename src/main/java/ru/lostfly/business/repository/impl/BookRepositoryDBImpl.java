package ru.lostfly.business.repository.impl;

import lombok.extern.slf4j.Slf4j;
import ru.lostfly.config.DatabaseConnection;
import ru.lostfly.business.domain.book.Book;
import ru.lostfly.business.repository.BookRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BookRepositoryDBImpl implements BookRepository {

    @Override
    public void save(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, genre, is_available, times_read) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "title = VALUES(title), " +
                "author = VALUES(author), " +
                "genre = VALUES(genre), " +
                "is_available = VALUES(is_available), " +
                "times_read = VALUES(times_read)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, null); // genre - пока null, так как в Book нет геттера для genre
            stmt.setBoolean(5, book.isAvailable());
            stmt.setInt(6, book.getTimesRead());

            int rowsAffected = stmt.executeUpdate();
            log.info("Book saved/updated: {}, rows affected: {}", book.getIsbn(), rowsAffected);

        } catch (SQLException e) {
            log.error("Error saving book: {}", book.getIsbn(), e);
            throw new RuntimeException("Failed to save book", e);
        }
    }

    @Override
    public Book findByIsbn(String isbn) {
        String sql = "SELECT isbn, title, author, genre, is_available, times_read FROM books WHERE isbn = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToBook(rs);
            }

            log.debug("Book not found with ISBN: {}", isbn);
            return null;

        } catch (SQLException e) {
            log.error("Error finding book by ISBN: {}", isbn, e);
            throw new RuntimeException("Failed to find book", e);
        }
    }

    @Override
    public List<Book> findAvailableBooks() {
        String sql = "SELECT isbn, title, author, genre, is_available, times_read FROM books WHERE is_available = TRUE";
        List<Book> books = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }

            log.debug("Found {} available books", books.size());
            return books;

        } catch (SQLException e) {
            log.error("Error finding available books", e);
            throw new RuntimeException("Failed to find available books", e);
        }
    }

    @Override
    public List<Book> findByAuthor(String author) {
        String sql = "SELECT isbn, title, author, genre, is_available, times_read FROM books " +
                "WHERE LOWER(author) LIKE LOWER(?)";
        List<Book> books = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + author + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }

            log.debug("Found {} books by author: {}", books.size(), author);
            return books;

        } catch (SQLException e) {
            log.error("Error finding books by author: {}", author, e);
            throw new RuntimeException("Failed to find books by author", e);
        }
    }

    @Override
    public int getTotalBooks() {
        String sql = "SELECT COUNT(*) as total FROM books";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException e) {
            log.error("Error getting total books count", e);
            throw new RuntimeException("Failed to get total books count", e);
        }
    }

    /**
     * Вспомогательный метод для маппинга ResultSet в объект Book
     */
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        String isbn = rs.getString("isbn");
        String title = rs.getString("title");
        String author = rs.getString("author");
        String genre = rs.getString("genre");

        Book book = new Book(isbn, title, author, genre);
        book.setAvailable(rs.getBoolean("is_available"));
        book.setTimesRead(rs.getInt("times_read"));

        return book;
    }
}