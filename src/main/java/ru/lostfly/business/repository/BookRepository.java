package ru.lostfly.business.repository;

import ru.lostfly.business.domain.book.Book;

import java.util.List;

public interface BookRepository {
    
    /**
     * Сохранить книгу в хранилище
     */
    void save(Book book);
    
    /**
     * Найти книгу по ISBN
     * @return книга или null, если не найдена
     */
    Book findByIsbn(String isbn);
    
    /**
     * Получить все доступные книги
     */
    List<Book> findAvailableBooks();
    
    /**
     * Найти книги по автору
     */
    List<Book> findByAuthor(String author);
    
    /**
     * Получить общее количество книг
     */
    int getTotalBooks();
}