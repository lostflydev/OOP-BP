package ru.lostfly.repository.impl;

import ru.lostfly.domain.book.Book;
import ru.lostfly.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;

public class BookRepositoryImpl implements BookRepository {
    private final List<Book> books = new ArrayList<>();

    public void save(Book book) {
        books.add(book);
    }

    public Book findByIsbn(String isbn) {
        for (Book book : books) {  // for-each
            if (book.getIsbn().equals(isbn)) {  // equals для String!
                return book;
            }
        }
        return null;  // не нашли
    }


    public List<Book> findAvailableBooks() {
        List<Book> available = new ArrayList<>();
        for (Book book : books) {
            if (book.isAvailable()) {  // примитив boolean
                available.add(book);
            }
        }
        return available;
    }

    public List<Book> findByAuthor(String author) {
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.getAuthor().equalsIgnoreCase(author)) {  // игнорируем регистр
                result.add(book);
            }
        }
        return result;
    }


    public int getTotalBooks() {
        return books.size();
    }
}
