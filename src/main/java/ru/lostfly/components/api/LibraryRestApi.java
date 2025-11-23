package ru.lostfly.components.api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import ru.lostfly.business.domain.book.Book;
import ru.lostfly.business.domain.reader.Reader;
import ru.lostfly.components.repository.RepositoryComponent;
import ru.lostfly.components.service.ServiceComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryRestApi {

    private final RepositoryComponent repositoryComponent;
    private final ServiceComponent serviceComponent;
    private final Javalin app;

    public LibraryRestApi(RepositoryComponent repositoryComponent, ServiceComponent serviceComponent) {
        this.repositoryComponent = repositoryComponent;
        this.serviceComponent = serviceComponent;
        this.app = Javalin.create(config -> {
            // Статические файлы для Mini App
            config.staticFiles.add("/webapp", Location.CLASSPATH);
            // CORS для локальной разработки
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.anyHost();
                });
            });
        });

        setupRoutes();
    }

    private void setupRoutes() {
        // Корневой путь - перенаправляем на index.html
        app.get("/", ctx -> ctx.redirect("/index.html"));

        // API endpoints
        app.get("/api/books/available", this::getAvailableBooks);
        app.get("/api/books/search", this::searchBooksByAuthor);
        app.post("/api/books", this::addBook);
        app.post("/api/books/borrow", this::borrowBook);
        app.post("/api/books/return", this::returnBook);

        app.get("/api/readers", this::getReaders);
        app.post("/api/readers", this::addReader);
    }

    private void getAvailableBooks(Context ctx) {
        try {
            List<Book> books = repositoryComponent.getBookRepository().findAvailableBooks();
            ctx.json(books);
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    private void searchBooksByAuthor(Context ctx) {
        try {
            String author = ctx.queryParam("author");
            if (author == null || author.isBlank()) {
                ctx.status(400).json(Map.of("error", "Author parameter is required"));
                return;
            }

            List<Book> books = repositoryComponent.getBookRepository().findByAuthor(author);
            ctx.json(books);
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    private void addBook(Context ctx) {
        try {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            String isbn = body.get("isbn");
            String title = body.get("title");
            String author = body.get("author");

            if (isbn == null || title == null || author == null) {
                ctx.status(400).json(Map.of("error", "ISBN, title and author are required"));
                return;
            }

            Book book = new Book(isbn, title, author);
            repositoryComponent.getBookRepository().save(book);
            ctx.status(201).json(Map.of("success", true, "message", "Book added successfully"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    private void borrowBook(Context ctx) {
        try {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            String isbn = body.get("isbn");
            String readerId = body.get("readerId");

            if (isbn == null || readerId == null) {
                ctx.status(400).json(Map.of("error", "ISBN and readerId are required"));
                return;
            }

            Reader reader = repositoryComponent.getReaderRepository().findById(readerId);
            if (reader == null) {
                ctx.status(404).json(Map.of("error", "Reader not found"));
                return;
            }

            String result = serviceComponent.getLibraryService().borrowBook(isbn, reader);

            if (result.startsWith("✗")) {
                ctx.status(400).json(Map.of("error", result));
            } else {
                ctx.json(Map.of("success", true, "message", result));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    private void returnBook(Context ctx) {
        try {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            String isbn = body.get("isbn");
            String readerId = body.get("readerId");

            if (isbn == null || readerId == null) {
                ctx.status(400).json(Map.of("error", "ISBN and readerId are required"));
                return;
            }

            Reader reader = repositoryComponent.getReaderRepository().findById(readerId);
            if (reader == null) {
                ctx.status(404).json(Map.of("error", "Reader not found"));
                return;
            }

            Book book = repositoryComponent.getBookRepository().findByIsbn(isbn);
            if (book == null) {
                ctx.status(404).json(Map.of("error", "Book not found"));
                return;
            }

            String result = serviceComponent.getLibraryService().returnBook(book, reader);

            if (result.startsWith("✗")) {
                ctx.status(400).json(Map.of("error", result));
            } else {
                ctx.json(Map.of("success", true, "message", result));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    private void getReaders(Context ctx) {
        try {
            List<Reader> readers = repositoryComponent.getReaderRepository().findAll();
            ctx.json(readers);
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    private void addReader(Context ctx) {
        try {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            String id = body.get("id");
            String name = body.get("name");

            if (id == null || name == null) {
                ctx.status(400).json(Map.of("error", "ID and name are required"));
                return;
            }

            Reader reader = new Reader(id, name);
            repositoryComponent.getReaderRepository().save(reader);
            ctx.status(201).json(Map.of("success", true, "message", "Reader added successfully"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    public void start(int port) {
        app.start(port);
        System.out.println("REST API started on port " + port);
        System.out.println("Mini App available at: http://localhost:" + port);
    }

    public void stop() {
        app.stop();
    }
}