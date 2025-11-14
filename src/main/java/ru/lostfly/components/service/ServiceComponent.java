package ru.lostfly.components.service;

import ru.lostfly.components.repository.RepositoryComponent;
import ru.lostfly.business.service.LibraryService;
import ru.lostfly.business.service.impl.LibraryServiceImpl;

public class ServiceComponent {
    
    private final LibraryService libraryService;
    
    /**
     * Создает сервисы с правильными зависимостями.
     * Принимает RepositoryComponent для получения репозиториев.
     */
    public ServiceComponent(RepositoryComponent repositoryComponent) {
        // Создаем сервис с зависимостью от репозитория
        this.libraryService = new LibraryServiceImpl(
            repositoryComponent.getBookRepository()
        );
    }
    
    /**
     * Получить сервис библиотеки.
     */
    public LibraryService getLibraryService() {
        return libraryService;
    }
}