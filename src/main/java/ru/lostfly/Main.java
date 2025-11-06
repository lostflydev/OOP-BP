package ru.lostfly;

import ru.lostfly.components.App;
import ru.lostfly.components.repository.RepositoryComponent;

import java.time.Duration;
import java.time.OffsetDateTime;

public class Main {

    public static void main(String[] args) {
        System.out.println("APP STARTING>>>");

        // Выбор режима работы:
        // 1. IN_MEMORY - данные хранятся в памяти (ArrayList)
        // 2. DATABASE - данные хранятся в MySQL базе данных

        // Для использования базы данных:
        // - Запустите Docker: docker-compose up -d
        // - Раскомментируйте строку ниже

        // App app = new App(RepositoryComponent.RepositoryMode.DATABASE);

        // По умолчанию используется in-memory режим
        App app = new App(RepositoryComponent.RepositoryMode.DATABASE);

        app.start();

        // ytyt
    }
}