package ru.lostfly.business.service;

import ru.lostfly.business.domain.book.Book;
import ru.lostfly.business.domain.reader.Reader;

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