package ru.lostfly.components.api;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.lostfly.business.handlers.TgApiHandler;
import ru.lostfly.components.repository.RepositoryComponent;
import ru.lostfly.config.BotConfig;

import java.util.ArrayList;
import java.util.List;

public class LibraryBot extends TelegramLongPollingBot {

    private TgApiHandler tgApiHandler;

    public LibraryBot(RepositoryComponent repositoryComponent) {
        this.tgApiHandler = new TgApiHandler(repositoryComponent);
    }

    public LibraryBot() {
        this(new RepositoryComponent(RepositoryComponent.RepositoryMode.IN_MEMORY));
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

        // Если команда /webapp - отправляем сообщение с кнопкой Mini App
        if (messageText.equals("/webapp") || messageText.equals("/app")) {
            sendWebAppMessage(chatId);
            return;
        }

        // Передаем chatId в обработчик для поддержки сессий
        String result = tgApiHandler.handleUpdateReceived(chatId, messageText);

        sendMessage(chatId, result);
    }

    private void sendWebAppMessage(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Откройте Mini App для удобного управления библиотекой:");

        // Создаем кнопку с Web App
        InlineKeyboardButton webAppButton = InlineKeyboardButton.builder()
                .text("📱 Открыть библиотеку")
                .webApp(new WebAppInfo("http://localhost:8080"))  // Замените на ваш URL
                .build();

        // Создаем клавиатуру
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(webAppButton));
        keyboard.setKeyboard(rows);

        message.setReplyMarkup(keyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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