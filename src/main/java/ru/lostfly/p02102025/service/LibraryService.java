package ru.lostfly.p02102025.service;

import ru.lostfly.p02102025.domain.book.Book;
import ru.lostfly.p02102025.domain.reader.Reader;

public interface LibraryService {
    
    /**
     * Выдать книгу читателю
     * @param isbn идентификатор книги
     * @param reader читатель
     * @return сообщение о результате операции
     */
    String borrowBook(String isbn, Reader reader);
    
    /**
     * Вернуть книгу в библиотеку
     * @param book книга для возврата
     * @param reader читатель, возвращающий книгу
     * @return сообщение о результате операции
     */
    String returnBook(Book book, Reader reader);
}