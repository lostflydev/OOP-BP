package ru.lostfly.business.domain.book;

public class Book extends LibItem implements Borrowable {

    // Дополнительные поля специфичные для Book
    private String genre;  // Например, жанр книги

    // Конструктор
    public Book(String isbn, String title, String author) {
        super(isbn, title, author);  // Вызываем конструктор родителя
    }

    // Конструктор с жанром
    public Book(String isbn, String title, String author, String genre) {
        super(isbn, title, author);
        this.genre = genre;
    }

    // Реализация интерфейса Borrowable
    @Override
    public String borrow() {
        if (isAvailable()) {  // Используем метод родителя
            setAvailable(false);  // Используем метод родителя
            setTimesRead(getTimesRead() + 1);  // Увеличиваем счетчик прочтений
            return "Книга '" + getTitle() + "' успешно взята";
        } else {
            return "Книга '" + getTitle() + "' недоступна";
        }
    }

    // Реализация абстрактного метода из LibItem
    @Override
    public String getType() {
        return "Book";
    }


    @Override
    public String toString() {
        return "Book{" +
                "isbn='" + getIsbn() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", author='" + getAuthor() + '\'' +
                ", genre='" + genre + '\'' +
                ", isAvailable=" + isAvailable() +
                ", timesRead=" + getTimesRead() +
                '}';
    }
}
