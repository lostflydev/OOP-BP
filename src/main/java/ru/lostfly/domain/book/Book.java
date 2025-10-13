package ru.lostfly.domain.book;

public class Book implements Borrowable{

    private final String isbn;      // ссылочный тип (String)
    private final String title;     // ссылочный
    private final String author;    // ссылочный
    private boolean isAvailable;    // примитив! true/false
    private int timesRead;          // примитив! целое число

    public Book(String isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.isAvailable = true;  // По умолчанию доступна (примитив)
        this.timesRead = 0;       // Еще не читали (примитив)
    }

    @Override
    public String borrow() {
        if (isAvailable) {
            isAvailable = false;
            return "Книга успешно взята";
        } else {
            return "Книга недоступна";
        }
    }

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

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public void setTimesRead(int timesRead) {
        this.timesRead = timesRead;
    }

    @Override
    public String toString() {
        return "Book{" +
                "isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isAvailable=" + isAvailable +
                ", timesRead=" + timesRead +
                '}';
    }
}
