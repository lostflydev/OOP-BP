package ru.lostfly.business.domain.reader;

import ru.lostfly.business.domain.book.Book;

import java.util.ArrayList;
import java.util.List;

public class Reader {
    private final String id;        // ссылочный
    private final String name;      // ссылочный
    private final List<Book> borrowedBooks; // ссылочный (коллекция объектов)
    private static final int MAX_BOOKS = 3; // примитив-константа

    public Reader(String id, String name) {
        this.id = id;
        this.name = name;
        this.borrowedBooks = new ArrayList<>(); // создаем пустой список
    }

    // Метод возвращает примитив
    public boolean canBorrowMore() {
        return borrowedBooks.size() < MAX_BOOKS;
    }

    // Метод возвращает примитив
    public int getBorrowedBooksCount() {
        return borrowedBooks.size();
    }

    public void addBook(Book book) {
        borrowedBooks.add(book);
    }

    public void removeBook(Book book) {
        borrowedBooks.remove(book);
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<Book> getBorrowedBooks() {
        return new ArrayList<>(borrowedBooks); // Возвращаем КОПИЮ (защита!)
    }
}
