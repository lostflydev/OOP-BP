package ru.lostfly.components.tgbotapi;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.lostfly.components.handlers.TgApiHandler;
import ru.lostfly.components.repository.RepositoryComponent;

public class LibraryBot extends TelegramLongPollingBot {

    private TgApiHandler tgApiHandler;

    public LibraryBot() {
        this.tgApiHandler = new TgApiHandler(RepositoryComponent.RepositoryMode.DATABASE);
    }

    @Override
    public String getBotUsername() {
        return BotConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return BotConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Проверяем, что пришло текстовое сообщение
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        // Передаем chatId в обработчик для поддержки сессий
        String result = tgApiHandler.handleUpdateReceived(chatId, messageText);

        sendMessage(chatId, result);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}