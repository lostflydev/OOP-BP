package ru.lostfly.components.repository;

import ru.lostfly.repository.BookRepository;
import ru.lostfly.repository.impl.BookRepositoryImpl;

public class RepositoryComponent {
    
    private final BookRepository bookRepository;
    
    public RepositoryComponent() {
        // Здесь решаем КАКУЮ реализацию использовать
        // В будущем можно будет менять: new BookRepositoryDatabase()
        this.bookRepository = new BookRepositoryImpl();
    }
    
    /**
     * Получить репозиторий книг.
     * Возвращаем интерфейс, а не конкретный класс!
     */
    public BookRepository getBookRepository() {
        return bookRepository;
    }
}