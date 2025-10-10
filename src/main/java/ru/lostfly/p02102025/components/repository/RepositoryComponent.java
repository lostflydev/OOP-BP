package ru.lostfly.p02102025.components.repository;

import ru.lostfly.p02102025.repository.BookRepository;
import ru.lostfly.p02102025.repository.impl.BookRepositoryImpl;

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