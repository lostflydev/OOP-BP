package ru.lostfly.components.repository;

import ru.lostfly.business.repository.BookRepository;
import ru.lostfly.business.repository.ReaderRepository;
import ru.lostfly.business.repository.impl.BookRepositoryDBImpl;
import ru.lostfly.business.repository.impl.BookRepositoryImpl;
import ru.lostfly.business.repository.impl.ReaderRepositoryDBImpl;
import ru.lostfly.business.repository.impl.ReaderRepositoryImpl;

public class RepositoryComponent {

    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;

    /**
     * Режим работы репозитория
     */
    public enum RepositoryMode {
        IN_MEMORY,  // В памяти (ArrayList)
        DATABASE    // MySQL база данных
    }

    /**
     * Конструктор по умолчанию - использует in-memory реализацию
     */
    public RepositoryComponent() {
        this(RepositoryMode.IN_MEMORY);
    }

    /**
     * Конструктор с выбором режима работы
     */
    public RepositoryComponent(RepositoryMode mode) {
        switch (mode) {
            case DATABASE:
                this.bookRepository = new BookRepositoryDBImpl();
                this.readerRepository = new ReaderRepositoryDBImpl();
                break;
            case IN_MEMORY:
            default:
                this.bookRepository = new BookRepositoryImpl();
                this.readerRepository = new ReaderRepositoryImpl();
                break;
        }
    }

    /**
     * Получить репозиторий книг.
     * Возвращаем интерфейс, а не конкретный класс!
     */
    public BookRepository getBookRepository() {
        return bookRepository;
    }

    /**
     * Получить репозиторий читателей.
     * Возвращаем интерфейс, а не конкретный класс!
     */
    public ReaderRepository getReaderRepository() {
        return readerRepository;
    }
}