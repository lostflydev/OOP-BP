package ru.lostfly.domain.book;

import java.util.UUID;

public abstract class LibItem {
    private String isbn;      // ссылочный тип (String)
    private String title;     // ссылочный
    private String author;    // ссылочный
    private boolean isAvailable;    // примитив! true/false
    private int timesRead;          // примитив! целое число

    // Конструктор
    public LibItem(String isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.isAvailable = true;  // По умолчанию доступна
        this.timesRead = 0;       // Еще не читали
    }

    // Геттеры
    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public int getTimesRead() {
        return timesRead;
    }

    // Сеттеры
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public void setTimesRead(int timesRead) {
        this.timesRead = timesRead;
    }

    // Абстрактный метод для специфичной логики каждого типа
    public abstract String getType();
}
