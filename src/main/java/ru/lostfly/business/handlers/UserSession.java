package ru.lostfly.business.handlers;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс для хранения состояния сессии пользователя в многошаговых диалогах
 */
public class UserSession {

    private SessionState state;
    private final Map<String, String> data;

    public UserSession() {
        this.state = SessionState.IDLE;
        this.data = new HashMap<>();
    }

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = state;
    }

    public void putData(String key, String value) {
        data.put(key, value);
    }

    public String getData(String key) {
        return data.get(key);
    }

    public void clearData() {
        data.clear();
    }

    public void reset() {
        this.state = SessionState.IDLE;
        this.data.clear();
    }

    /**
     * Перечисление возможных состояний сессии
     */
    public enum SessionState {
        IDLE,                    // Нет активного диалога

        // Добавление книги
        ADD_BOOK_WAITING_ISBN,
        ADD_BOOK_WAITING_TITLE,
        ADD_BOOK_WAITING_AUTHOR,

        // Добавление читателя
        ADD_USER_WAITING_ID,
        ADD_USER_WAITING_NAME,

        // Выдача книги
        BORROW_BOOK_WAITING_ISBN,
        BORROW_BOOK_WAITING_READER_ID,

        // Возврат книги
        RETURN_BOOK_WAITING_ISBN,
        RETURN_BOOK_WAITING_READER_ID
    }
}