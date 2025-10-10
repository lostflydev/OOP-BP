package ru.lostfly.p02102025.components;

import ru.lostfly.p02102025.components.repository.RepositoryComponent;
import ru.lostfly.p02102025.components.service.ServiceComponent;
import ru.lostfly.p02102025.domain.book.Book;
import ru.lostfly.p02102025.domain.reader.Reader;
import ru.lostfly.p02102025.repository.BookRepository;
import ru.lostfly.p02102025.service.LibraryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class App {

    private final RepositoryComponent repositoryComponent;
    private final ServiceComponent serviceComponent;
    private final Map<String, Reader> readers = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);


    public App() {
        this.repositoryComponent = new RepositoryComponent();
        this.serviceComponent = new ServiceComponent(repositoryComponent);
    }


    public void start() {
        printWelcome();


        while (true) {
            System.out.print("\nВведите команду: ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "/add_book" -> addBook();
                case "/add_user" -> addUser();
                case "/borrow_book" -> borrowBook();
                case "/return_book" -> returnBook();
                case "/find_book_by_author" -> findBookByAuthor();
                case "/list_available_books" -> listAvailableBooks();
                case "/help" -> printHelp();
                default -> System.out.println("Неизвестная команда. Введите /help");
            }
        }
    }

    private void addBook() {
        System.out.println("\n--- Добавление книги ---");
        System.out.print("Введите ISBN: ");
        String isbn = scanner.nextLine();
        if ( isbn.length() < 2 ) {
            System.out.print("isbn некправильный");
            return;
        }
        System.out.print("Введите Название: ");
        String title = scanner.nextLine();
        System.out.print("Введите Автор: ");
        String author = scanner.nextLine();

        Book book = new Book(isbn, title, author);
        repositoryComponent.getBookRepository().save(book);
        System.out.println("✓ Книга '" + title + "' добавлена");
    }

    private void addUser() {
        System.out.println("\n--- Добавление читателя ---");
        System.out.print("ID (например, R001): ");
        String id = scanner.nextLine();
        System.out.print("Имя: ");
        String name = scanner.nextLine();

        Reader reader = new Reader(id, name);
        readers.put(id, reader);
        System.out.println("✓ Читатель '" + name + "' добавлен");
    }

// SOLID
// Один метод - одна цель - S single responsibility principle
// Повторение кода - DRY - don't repeat yourself

    private void getBorrowBookInfo() {
        System.out.println("\n--- Выдача книги ---");
        System.out.print("ISBN книги: ");
        String isbn = scanner.nextLine();
        System.out.print("ID читателя: ");
        String readerId = scanner.nextLine();

        // Guard Clause - проверяем читателя
        Reader reader = getReader(readerId);

        borrowBook(reader, isbn);
    }

    private void borrowBook(Reader reader, String isbn) {
        // Делегируем бизнес-логику сервису
        LibraryService service = serviceComponent.getLibraryService();
        String result = service.borrowBook(isbn, reader);
        System.out.println(result);
    }

    private Reader getReader(String readerId) {
        Reader reader = readers.get(readerId);
        if (reader == null) {
            System.out.println("✗ Читатель не найден");
            return null;
        }
        return reader;
    }

    private void returnBook() {
        System.out.println("\n--- Возврат книги ---");
        System.out.print("ISBN книги: ");
        String isbn = scanner.nextLine();
        System.out.print("ID читателя: ");
        String readerId = scanner.nextLine();

        // Guard Clause - проверяем читателя
        Reader reader = getReader(readerId);

        // Guard Clause - проверяем книгу
        BookRepository repo = repositoryComponent.getBookRepository();
        Book book = repo.findByIsbn(isbn);
        if (book == null) {
            System.out.println("✗ Книга не найдена");
            return;
        }

        // Делегируем сервису
        String result = serviceComponent.getLibraryService().returnBook(book, reader);
        System.out.println(result);
    }


    private void findBookByAuthor() {
        System.out.println("\n--- Поиск по автору ---");
        System.out.print("Автор: ");
        String author = scanner.nextLine();

        List<Book> books = repositoryComponent.getBookRepository()
                .findByAuthor(author);


        System.out.println("\nНайдено книг: " + books.size());
        for (Book book : books) {
            String status = book.isAvailable() ? "Доступна" : "Выдана";
            System.out.printf("  • %s [%s]%n", book.getTitle(), status);
        }
    }


    private void listAvailableBooks() {
        System.out.println("\n--- Доступные книги ---");
        List<Book> available = repositoryComponent.getBookRepository()
                .findAvailableBooks();

        if (available.isEmpty()) {
            System.out.println("Нет доступных книг");
            return;
        }

        for (Book book : available) {
            System.out.printf(
                    "  • %s - %s (ISBN: %s)%n",
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn()
            );
        }
    }


    private void printWelcome() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║  Система управления библиотекой       ║");
        System.out.println("╚════════════════════════════════════════╝");
        printHelp();
    }


    private void printHelp() {
        System.out.println("\nДоступные команды:");
        System.out.println("  /add_book            - Добавить книгу");
        System.out.println("  /add_user            - Добавить читателя");
        System.out.println("  /borrow_book         - Выдать книгу");
        System.out.println("  /return_book         - Вернуть книгу");
        System.out.println("  /find_book_by_author - Поиск по автору");
        System.out.println("  /list_available_books- Доступные книги");
        System.out.println("  /help                - Эта справка");
    }

}
