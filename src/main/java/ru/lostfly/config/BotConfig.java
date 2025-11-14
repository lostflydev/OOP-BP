package ru.lostfly.config;

import io.github.cdimascio.dotenv.Dotenv;

public class BotConfig {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static String getBotToken() {
        return dotenv.get("TELEGRAM_BOT_TOKEN");
    }

    public static String getBotUsername() {
        return dotenv.get("TELEGRAM_BOT_USERNAME");
    }
}
