package ru.lostfly.service.impl;

import ru.lostfly.domain.book.Book;
import ru.lostfly.domain.reader.Reader;
import ru.lostfly.repository.BookRepository;
import ru.lostfly.service.LibraryService;

public class LibraryServiceImpl implements LibraryService {
    private final BookRepository bookRepository;

    public LibraryServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public String borrowBook(String isbn, Reader reader) {

        if (!reader.canBorrowMore()) {
            return "Читатель уже взял максимальное количество книг";
        }

        Book book = bookRepository.findByIsbn(isbn);
        if (book == null) {
            return "Книга не найдена";
        }


        String result = book.borrow();
        if (result.equals("Книга успешно взята")) {
            reader.addBook(book);
        } else {
            return result;
        }

        return "Книга успешно взята";
    }

    public String returnBook(Book bookArg, Reader reader) {
        Book book = bookRepository.findByIsbn(bookArg.getIsbn());
        if (book == null) {
            bookRepository.save(bookArg);
        }

        if (!reader.getBorrowedBooks().contains(book)) {
            return "Читатель не взял эту книгу";
        }

        book.setAvailable(true);
        reader.removeBook(book);
        return "Книга успешно возвращена";
    }
}
