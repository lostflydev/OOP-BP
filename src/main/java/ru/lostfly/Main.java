package ru.lostfly;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.lostfly.components.api.LibraryBot;
import ru.lostfly.components.api.LibraryRestApi;
import ru.lostfly.components.repository.RepositoryComponent;
import ru.lostfly.components.service.ServiceComponent;


public class Main {

    public static void main(String[] args) throws TelegramApiException {

        // Выбор режима работы:
        // 1. IN_MEMORY - данные хранятся в памяти (ArrayList)
        // 2. DATABASE - данные хранятся в MySQL базе данных

        // Для использования базы данных:
        // - Запустите Docker: docker-compose up -d
        // - Раскомментируйте строку ниже

        // App app = new App(RepositoryComponent.RepositoryMode.DATABASE);

        // По умолчанию используется in-memory режим
        // App app = new App(RepositoryComponent.RepositoryMode.IN_MEMORY);

        // app.start();

        // Инициализация компонентов (используем IN_MEMORY режим)
        // Один и тот же RepositoryComponent будет использоваться и для бота, и для REST API
        RepositoryComponent repositoryComponent = new RepositoryComponent(RepositoryComponent.RepositoryMode.IN_MEMORY);
        ServiceComponent serviceComponent = new ServiceComponent(repositoryComponent);

        // Запуск Telegram бота с общим RepositoryComponent
        LibraryBot bot = new LibraryBot(repositoryComponent);
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);
        System.out.println("Telegram Bot запущен!");

        // Запуск REST API сервера для Mini App с тем же RepositoryComponent
        LibraryRestApi restApi = new LibraryRestApi(repositoryComponent, serviceComponent);
        restApi.start(8080);
        System.out.println("REST API сервер запущен на порту 8080");
        System.out.println("Mini App доступен по адресу: http://localhost:8080");
    }
}