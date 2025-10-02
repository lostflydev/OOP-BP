package ru.lostfly;

import ru.lostfly.domain.book.Book;
import ru.lostfly.domain.reader.Reader;
import ru.lostfly.repository.BookRepository;
import ru.lostfly.service.LibraryService;

public class Main {

    public static void main(String[] args) {

        BookRepository repository = new BookRepository();

        LibraryService library = new LibraryService(repository);

        // /add_book
        Book book1 = new Book("978-0-13-468599-1",
                "Clean Code",
                "Robert Martin");
        Book book2 = new Book("978-0-13-475759-9",
                "Clean Architecture",
                "Robert Martin");
        Book book3 = new Book("978-0-13-235088-4",
                "Clean Coder",
                "Robert Martin");

        repository.save(book1);
        repository.save(book2);
        repository.save(book3);

// /add_user
        Reader alice = new Reader("R001", "Alice");
        Reader bob = new Reader("R002", "Bob");


        // Операции с демонстрацией циклов и типов
        System.out.println("=== Выдача книг ===");
        // /borrow_book
        System.out.println(library.borrowBook("978-0-13-468599-1", alice));
        System.out.println(library.borrowBook("978-0-13-475759-9", alice));
        System.out.println(library.borrowBook("978-0-13-235088-4", bob));

        // Попытка взять уже выданную книгу
        // /find_book_by_isbn
        System.out.println(library.borrowBook("978-0-13-468599-1", bob));


        System.out.println("\n=== Поиск книг Robert Martin ===");
        var martinBooks = repository.findByAuthor("Robert Martin");
        for (Book book : martinBooks) {  // for-each цикл
            System.out.println("  - " + book.getTitle() +
                    " [" + (book.isAvailable() ? "Доступна" : "Выдана") + "]");
        }

        System.out.println("Hello world!");
    }
}