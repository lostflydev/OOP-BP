package ru.lostfly.business.handlers;

import ru.lostfly.components.repository.RepositoryComponent;
import ru.lostfly.components.service.ServiceComponent;
import ru.lostfly.business.domain.book.Book;
import ru.lostfly.business.domain.reader.Reader;
import ru.lostfly.business.repository.BookRepository;
import ru.lostfly.business.service.LibraryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TgApiHandler {

    private final RepositoryComponent repositoryComponent;
    private final ServiceComponent serviceComponent;
    // Хранилище сессий пользователей: chatId -> UserSession
    private final Map<Long, UserSession> userSessions;


    /**
     * Конструктор по умолчанию - использует in-memory реализацию
     */
    public TgApiHandler() {
        this(RepositoryComponent.RepositoryMode.IN_MEMORY);
    }

    /**
     * Конструктор с существующим RepositoryComponent
     */
    public TgApiHandler(RepositoryComponent repositoryComponent) {
        System.out.println("Инициализация TgApiHandler с существующим RepositoryComponent");
        this.repositoryComponent = repositoryComponent;
        this.serviceComponent = new ServiceComponent(repositoryComponent);
        this.userSessions = new HashMap<>();
        System.out.println("TgApiHandler инициализирован");
    }

    /**
     * Конструктор с выбором режима работы репозитория
     * @param mode IN_MEMORY (в памяти) или DATABASE (MySQL)
     */
    public TgApiHandler(RepositoryComponent.RepositoryMode mode) {
        System.out.println("Инициализация приложения в режиме: " + mode);
        this.repositoryComponent = new RepositoryComponent(mode);
        this.serviceComponent = new ServiceComponent(repositoryComponent);
        this.userSessions = new HashMap<>();
        System.out.println("APP STARTED " + mode);
    }


    /**
     * Обрабатывает входящие сообщения от пользователя
     * @param chatId ID чата пользователя
     * @param messageText полный текст сообщения
     * @return ответ для отправки пользователю
     */
    public String handleUpdateReceived(Long chatId, String messageText) {
        if (messageText == null || messageText.isBlank()) {
            return "Пустое сообщение";
        }

        // Получаем или создаем сессию пользователя
        UserSession session = userSessions.computeIfAbsent(chatId, k -> new UserSession());

        // Если пользователь в активном диалоге, обрабатываем его состояние
        if (session.getState() != UserSession.SessionState.IDLE) {
            return handleSessionState(session, messageText);
        }

        // Обработка команд
        String trimmedMessage = messageText.trim();

        // Команды для отмены текущего диалога
        if (trimmedMessage.equalsIgnoreCase("/cancel")) {
            session.reset();
            return "Текущий диалог отменён.";
        }

        // Разбираем команду и параметры
        String[] parts = trimmedMessage.split("\\s+", 2);
        String command = parts[0];
        String params = parts.length > 1 ? parts[1] : "";

        return switch (command) {
            case "/start" -> getWelcomeMessage();
            case "/help" -> getHelpMessage();
            case "/add_book" -> {
                if (params.isBlank()) {
                    // Запускаем пошаговый диалог
                    session.setState(UserSession.SessionState.ADD_BOOK_WAITING_ISBN);
                    yield "Добавление книги\n\nШаг 1/3: Введите ISBN книги:";
                } else {
                    yield addBook(params);
                }
            }
            case "/add_user" -> {
                if (params.isBlank()) {
                    // Запускаем пошаговый диалог
                    session.setState(UserSession.SessionState.ADD_USER_WAITING_ID);
                    yield "Добавление читателя\n\nШаг 1/2: Введите ID читателя (например, R001):";
                } else {
                    yield addUser(params);
                }
            }
            case "/borrow_book" -> {
                if (params.isBlank()) {
                    // Запускаем пошаговый диалог
                    session.setState(UserSession.SessionState.BORROW_BOOK_WAITING_ISBN);
                    yield "Выдача книги\n\nШаг 1/2: Введите ISBN книги:";
                } else {
                    yield borrowBook(params);
                }
            }
            case "/return_book" -> {
                if (params.isBlank()) {
                    // Запускаем пошаговый диалог
                    session.setState(UserSession.SessionState.RETURN_BOOK_WAITING_ISBN);
                    yield "Возврат книги\n\nШаг 1/2: Введите ISBN книги:";
                } else {
                    yield returnBook(params);
                }
            }
            case "/find_book_by_author" -> findBookByAuthor(params);
            case "/list_available_books" -> listAvailableBooks();
            default -> "Неизвестная команда: " + command + "\nВведите /help для списка команд";
        };
    }

    /**
     * Обрабатывает состояние сессии в многошаговом диалоге
     */
    private String handleSessionState(UserSession session, String input) {
        input = input.trim();

        // Отмена текущего диалога
        if (input.equalsIgnoreCase("/cancel")) {
            session.reset();
            return "Диалог отменён.";
        }

        return switch (session.getState()) {
            // Добавление книги
            case ADD_BOOK_WAITING_ISBN -> {
                if (input.length() < 2) {
                    yield "Ошибка: ISBN слишком короткий. Попробуйте еще раз или введите /cancel для отмены:";
                }
                session.putData("isbn", input);
                session.setState(UserSession.SessionState.ADD_BOOK_WAITING_TITLE);
                yield "Шаг 2/3: Введите название книги:";
            }
            case ADD_BOOK_WAITING_TITLE -> {
                if (input.isEmpty()) {
                    yield "Ошибка: название не может быть пустым. Попробуйте еще раз или введите /cancel:";
                }
                session.putData("title", input);
                session.setState(UserSession.SessionState.ADD_BOOK_WAITING_AUTHOR);
                yield "Шаг 3/3: Введите автора книги:";
            }
            case ADD_BOOK_WAITING_AUTHOR -> {
                if (input.isEmpty()) {
                    yield "Ошибка: автор не может быть пустым. Попробуйте еще раз или введите /cancel:";
                }
                String isbn = session.getData("isbn");
                String title = session.getData("title");
                String result = addBookFromSession(isbn, title, input);
                session.reset();
                yield result;
            }

            // Добавление читателя
            case ADD_USER_WAITING_ID -> {
                if (input.isEmpty()) {
                    yield "Ошибка: ID не может быть пустым. Попробуйте еще раз или введите /cancel:";
                }
                session.putData("id", input);
                session.setState(UserSession.SessionState.ADD_USER_WAITING_NAME);
                yield "Шаг 2/2: Введите имя читателя:";
            }
            case ADD_USER_WAITING_NAME -> {
                if (input.isEmpty()) {
                    yield "Ошибка: имя не может быть пустым. Попробуйте еще раз или введите /cancel:";
                }
                String id = session.getData("id");
                String result = addUserFromSession(id, input);
                session.reset();
                yield result;
            }

            // Выдача книги
            case BORROW_BOOK_WAITING_ISBN -> {
                session.putData("isbn", input);
                session.setState(UserSession.SessionState.BORROW_BOOK_WAITING_READER_ID);
                yield "Шаг 2/2: Введите ID читателя:";
            }
            case BORROW_BOOK_WAITING_READER_ID -> {
                String isbn = session.getData("isbn");
                String result = borrowBook(isbn + "|" + input);
                session.reset();
                yield result;
            }

            // Возврат книги
            case RETURN_BOOK_WAITING_ISBN -> {
                session.putData("isbn", input);
                session.setState(UserSession.SessionState.RETURN_BOOK_WAITING_READER_ID);
                yield "Шаг 2/2: Введите ID читателя:";
            }
            case RETURN_BOOK_WAITING_READER_ID -> {
                String isbn = session.getData("isbn");
                String result = returnBook(isbn + "|" + input);
                session.reset();
                yield result;
            }

            default -> {
                session.reset();
                yield "Произошла ошибка. Попробуйте еще раз.";
            }
        };
    }

    /**
     * Добавление книги из данных сессии
     */
    private String addBookFromSession(String isbn, String title, String author) {
        try {
            Book book = new Book(isbn, title, author);
            repositoryComponent.getBookRepository().save(book);
            return "✓ Книга успешно добавлена:\n" +
                   "  ISBN: " + isbn + "\n" +
                   "  Название: " + title + "\n" +
                   "  Автор: " + author;
        } catch (Exception e) {
            return "✗ Ошибка при добавлении книги: " + e.getMessage();
        }
    }

    /**
     * Добавление читателя из данных сессии
     */
    private String addUserFromSession(String id, String name) {
        try {
            Reader reader = new Reader(id, name);
            repositoryComponent.getReaderRepository().save(reader);
            return "✓ Читатель успешно добавлен:\n" +
                   "  ID: " + id + "\n" +
                   "  Имя: " + name;
        } catch (Exception e) {
            return "✗ Ошибка при добавлении читателя: " + e.getMessage();
        }
    }

    /**
     * Добавление книги
     * Формат: /add_book ISBN|Название|Автор
     * Пример: /add_book 978-5-17-123456-7|Война и мир|Толстой Л.Н.
     */
    private String addBook(String params) {
        if (params.isBlank()) {
            return "Использование: /add_book ISBN|Название|Автор\n" +
                   "Пример: /add_book 978-5-17-123456-7|Война и мир|Толстой Л.Н.";
        }

        String[] parts = params.split("\\|", 3);
        if (parts.length < 3) {
            return "Ошибка: неверный формат. Используйте символ | для разделения\n" +
                   "Формат: /add_book ISBN|Название|Автор";
        }

        String isbn = parts[0].trim();
        String title = parts[1].trim();
        String author = parts[2].trim();

        if (isbn.length() < 2) {
            return "Ошибка: ISBN слишком короткий";
        }

        if (title.isEmpty() || author.isEmpty()) {
            return "Ошибка: название и автор не могут быть пустыми";
        }

        try {
            Book book = new Book(isbn, title, author);
            repositoryComponent.getBookRepository().save(book);
            return "✓ Книга успешно добавлена:\n" +
                   "  ISBN: " + isbn + "\n" +
                   "  Название: " + title + "\n" +
                   "  Автор: " + author;
        } catch (Exception e) {
            return "✗ Ошибка при добавлении книги: " + e.getMessage();
        }
    }

    /**
     * Добавление читателя
     * Формат: /add_user ID|Имя
     * Пример: /add_user R001|Иван Иванов
     */
    private String addUser(String params) {
        if (params.isBlank()) {
            return "Использование: /add_user ID|Имя\n" +
                   "Пример: /add_user R001|Иван Иванов";
        }

        String[] parts = params.split("\\|", 2);
        if (parts.length < 2) {
            return "Ошибка: неверный формат. Используйте символ | для разделения\n" +
                   "Формат: /add_user ID|Имя";
        }

        String id = parts[0].trim();
        String name = parts[1].trim();

        if (id.isEmpty() || name.isEmpty()) {
            return "Ошибка: ID и имя не могут быть пустыми";
        }

        try {
            Reader reader = new Reader(id, name);
            repositoryComponent.getReaderRepository().save(reader);
            return "✓ Читатель успешно добавлен:\n" +
                   "  ID: " + id + "\n" +
                   "  Имя: " + name;
        } catch (Exception e) {
            return "✗ Ошибка при добавлении читателя: " + e.getMessage();
        }
    }

    /**
     * Выдача книги
     * Формат: /borrow_book ISBN|ID_читателя
     * Пример: /borrow_book 978-5-17-123456-7|R001
     */
    private String borrowBook(String params) {
        if (params.isBlank()) {
            return "Использование: /borrow_book ISBN|ID_читателя\n" +
                   "Пример: /borrow_book 978-5-17-123456-7|R001";
        }

        String[] parts = params.split("\\|", 2);
        if (parts.length < 2) {
            return "Ошибка: неверный формат. Используйте символ | для разделения\n" +
                   "Формат: /borrow_book ISBN|ID_читателя";
        }

        String isbn = parts[0].trim();
        String readerId = parts[1].trim();

        try {
            // Проверяем читателя
            Reader reader = repositoryComponent.getReaderRepository().findById(readerId);
            if (reader == null) {
                return "✗ Читатель с ID '" + readerId + "' не найден";
            }

            // Делегируем бизнес-логику сервису
            LibraryService service = serviceComponent.getLibraryService();
            String result = service.borrowBook(isbn, reader);
            return result;
        } catch (Exception e) {
            return "✗ Ошибка при выдаче книги: " + e.getMessage();
        }
    }

    /**
     * Возврат книги
     * Формат: /return_book ISBN|ID_читателя
     * Пример: /return_book 978-5-17-123456-7|R001
     */
    private String returnBook(String params) {
        if (params.isBlank()) {
            return "Использование: /return_book ISBN|ID_читателя\n" +
                   "Пример: /return_book 978-5-17-123456-7|R001";
        }

        String[] parts = params.split("\\|", 2);
        if (parts.length < 2) {
            return "Ошибка: неверный формат. Используйте символ | для разделения\n" +
                   "Формат: /return_book ISBN|ID_читателя";
        }

        String isbn = parts[0].trim();
        String readerId = parts[1].trim();

        try {
            // Проверяем читателя
            Reader reader = repositoryComponent.getReaderRepository().findById(readerId);
            if (reader == null) {
                return "✗ Читатель с ID '" + readerId + "' не найден";
            }

            // Проверяем книгу
            BookRepository repo = repositoryComponent.getBookRepository();
            Book book = repo.findByIsbn(isbn);
            if (book == null) {
                return "✗ Книга с ISBN '" + isbn + "' не найдена";
            }

            // Делегируем сервису
            String result = serviceComponent.getLibraryService().returnBook(book, reader);
            return result;
        } catch (Exception e) {
            return "✗ Ошибка при возврате книги: " + e.getMessage();
        }
    }

    /**
     * Поиск книг по автору
     * Формат: /find_book_by_author Имя_автора
     * Пример: /find_book_by_author Толстой
     */
    private String findBookByAuthor(String author) {
        if (author.isBlank()) {
            return "Использование: /find_book_by_author Имя_автора\n" +
                   "Пример: /find_book_by_author Толстой";
        }

        try {
            List<Book> books = repositoryComponent.getBookRepository()
                    .findByAuthor(author.trim());

            if (books.isEmpty()) {
                return "Книги автора '" + author + "' не найдены";
            }

            StringBuilder response = new StringBuilder();
            response.append("Найдено книг: ").append(books.size()).append("\n\n");
            for (Book book : books) {
                String status = book.isAvailable() ? "✓ Доступна" : "✗ Выдана";
                response.append("• ").append(book.getTitle())
                        .append(" (ISBN: ").append(book.getIsbn()).append(")")
                        .append("\n  ").append(status).append("\n");
            }
            return response.toString();
        } catch (Exception e) {
            return "✗ Ошибка при поиске: " + e.getMessage();
        }
    }

    /**
     * Список доступных книг
     */
    private String listAvailableBooks() {
        try {
            List<Book> available = repositoryComponent.getBookRepository()
                    .findAvailableBooks();

            if (available.isEmpty()) {
                return "В данный момент нет доступных книг";
            }

            StringBuilder response = new StringBuilder();
            response.append("Доступные книги (").append(available.size()).append("):\n\n");
            for (Book book : available) {
                response.append("• ").append(book.getTitle())
                        .append("\n  Автор: ").append(book.getAuthor())
                        .append("\n  ISBN: ").append(book.getIsbn())
                        .append("\n");
            }
            return response.toString();
        } catch (Exception e) {
            return "✗ Ошибка при получении списка книг: " + e.getMessage();
        }
    }

    /**
     * Приветственное сообщение
     */
    private String getWelcomeMessage() {
        return "╔════════════════════════════════════════╗\n" +
               "║   Система управления библиотекой       ║\n" +
               "╚════════════════════════════════════════╝\n\n" +
               "Добро пожаловать! Введите /help для списка команд.";
    }

    /**
     * Справка по командам
     */
    private String getHelpMessage() {
        return """
                📚 Доступные команды:

                /start - Приветствие
                /help - Эта справка
                /webapp или /app - Открыть Mini App (веб-интерфейс)
                /cancel - Отменить текущий диалог

                Управление книгами:
                • /add_book - Добавить книгу (пошаговый режим)
                • /add_book ISBN|Название|Автор - Добавить книгу (быстрый режим)
                  Пример: /add_book 978-5|Война и мир|Толстой

                • /list_available_books - Показать доступные книги

                • /find_book_by_author Автор
                  Пример: /find_book_by_author Толстой

                Управление читателями:
                • /add_user - Добавить читателя (пошаговый режим)
                • /add_user ID|Имя - Добавить читателя (быстрый режим)
                  Пример: /add_user R001|Иван Иванов

                Операции с книгами:
                • /borrow_book - Выдать книгу (пошаговый режим)
                • /borrow_book ISBN|ID_читателя - Выдать книгу (быстрый режим)
                  Пример: /borrow_book 978-5|R001

                • /return_book - Вернуть книгу (пошаговый режим)
                • /return_book ISBN|ID_читателя - Вернуть книгу (быстрый режим)
                  Пример: /return_book 978-5|R001

                💡 Подсказка: Вы можете использовать команды с параметрами
                (быстрый режим) или без них (пошаговый диалог).

                🎯 Для удобного управления используйте Mini App - /webapp
                """;
    }

}
