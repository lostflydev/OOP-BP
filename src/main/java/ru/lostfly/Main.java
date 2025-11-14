package ru.lostfly;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.lostfly.components.api.LibraryBot;


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

        LibraryBot bot = new LibraryBot();

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);
    }
}