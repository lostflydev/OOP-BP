package ru.lostfly.repository.impl;

import lombok.extern.slf4j.Slf4j;
import ru.lostfly.config.DatabaseConnection;
import ru.lostfly.domain.book.Book;
import ru.lostfly.domain.reader.Reader;
import ru.lostfly.repository.ReaderRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ReaderRepositoryDBImpl implements ReaderRepository {

    @Override
    public void save(Reader reader) {
        String sql = "INSERT INTO readers (id, name) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reader.getId());
            stmt.setString(2, reader.getName());

            int rowsAffected = stmt.executeUpdate();
            log.info("Reader saved/updated: {}, rows affected: {}", reader.getId(), rowsAffected);

            // Сохраняем взятые книги
            saveBorrowedBooks(reader);

        } catch (SQLException e) {
            log.error("Error saving reader: {}", reader.getId(), e);
            throw new RuntimeException("Failed to save reader", e);
        }
    }

    @Override
    public Reader findById(String id) {
        String sql = "SELECT id, name FROM readers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Reader reader = new Reader(rs.getString("id"), rs.getString("name"));

                // Загружаем взятые книги
                loadBorrowedBooks(reader);

                return reader;
            }

            log.debug("Reader not found with ID: {}", id);
            return null;

        } catch (SQLException e) {
            log.error("Error finding reader by ID: {}", id, e);
            throw new RuntimeException("Failed to find reader", e);
        }
    }

    @Override
    public List<Reader> findAll() {
        String sql = "SELECT id, name FROM readers";
        List<Reader> readers = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Reader reader = new Reader(rs.getString("id"), rs.getString("name"));
                loadBorrowedBooks(reader);
                readers.add(reader);
            }

            log.debug("Found {} readers", readers.size());
            return readers;

        } catch (SQLException e) {
            log.error("Error finding all readers", e);
            throw new RuntimeException("Failed to find readers", e);
        }
    }

    @Override
    public int getTotalReaders() {
        String sql = "SELECT COUNT(*) as total FROM readers";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException e) {
            log.error("Error getting total readers count", e);
            throw new RuntimeException("Failed to get total readers count", e);
        }
    }

    /**
     * Сохранить взятые книги читателя
     */
    private void saveBorrowedBooks(Reader reader) {
        // Сначала удаляем старые записи о взятых книгах (которые не были возвращены)
        String deleteSql = "DELETE FROM borrowed_books WHERE reader_id = ? AND returned_at IS NULL";

        // Затем добавляем актуальные
        String insertSql = "INSERT INTO borrowed_books (reader_id, book_isbn) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            // Удаляем старые записи
            deleteStmt.setString(1, reader.getId());
            deleteStmt.executeUpdate();

            // Добавляем новые
            for (Book book : reader.getBorrowedBooks()) {
                insertStmt.setString(1, reader.getId());
                insertStmt.setString(2, book.getIsbn());
                insertStmt.addBatch();
            }

            if (!reader.getBorrowedBooks().isEmpty()) {
                insertStmt.executeBatch();
                log.debug("Saved {} borrowed books for reader {}", reader.getBorrowedBooks().size(), reader.getId());
            }

        } catch (SQLException e) {
            log.error("Error saving borrowed books for reader: {}", reader.getId(), e);
            throw new RuntimeException("Failed to save borrowed books", e);
        }
    }

    /**
     * Загрузить взятые книги для читателя
     */
    private void loadBorrowedBooks(Reader reader) {
        String sql = "SELECT b.isbn, b.title, b.author, b.genre, b.is_available, b.times_read " +
                "FROM borrowed_books bb " +
                "JOIN books b ON bb.book_isbn = b.isbn " +
                "WHERE bb.reader_id = ? AND bb.returned_at IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reader.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String genre = rs.getString("genre");

                Book book = new Book(isbn, title, author, genre);
                book.setAvailable(rs.getBoolean("is_available"));
                book.setTimesRead(rs.getInt("times_read"));

                reader.addBook(book);
            }

            log.debug("Loaded {} borrowed books for reader {}", reader.getBorrowedBooks().size(), reader.getId());

        } catch (SQLException e) {
            log.error("Error loading borrowed books for reader: {}", reader.getId(), e);
            throw new RuntimeException("Failed to load borrowed books", e);
        }
    }
}