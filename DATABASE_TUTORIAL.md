# Полное руководство по базам данных для Java разработчиков

## Содержание

1. [Введение в базы данных](#1-введение-в-базы-данных)
2. [Основы SQL](#2-основы-sql)
3. [Проектирование базы данных](#3-проектирование-базы-данных)
4. [Подключение БД к Java проекту](#4-подключение-бд-к-java-проекту)
5. [Docker для баз данных](#5-docker-для-баз-данных)
6. [Практические примеры](#6-практические-примеры)
7. [Best Practices](#7-best-practices)

---

## 1. Введение в базы данных

### Что такое база данных?

**База данных (БД)** — это организованная коллекция структурированных данных, которые хранятся и к которым можно получить доступ электронным способом.

**Система управления базами данных (СУБД)** — это программное обеспечение для создания и управления базами данных.

### Типы баз данных

#### Реляционные БД (SQL)
- **MySQL** - популярная open-source СУБД
- **PostgreSQL** - продвинутая open-source СУБД
- **Oracle** - коммерческая СУБД для крупных предприятий
- **MS SQL Server** - СУБД от Microsoft
- **SQLite** - встраиваемая БД

**Особенности:**
- Данные организованы в таблицы (строки и столбцы)
- Используется SQL для запросов
- ACID транзакции (Atomicity, Consistency, Isolation, Durability)
- Строгая схема данных

#### Нереляционные БД (NoSQL)
- **MongoDB** - документо-ориентированная БД
- **Redis** - key-value хранилище
- **Cassandra** - колоночная БД
- **Neo4j** - графовая БД

**Когда использовать?**
- Большие объемы неструктурированных данных
- Горизонтальное масштабирование
- Гибкая схема данных
- Высокая производительность для специфичных задач

### Зачем нужны базы данных?

1. **Персистентность** - данные сохраняются после завершения программы
2. **Централизация** - единое место для хранения данных
3. **Безопасность** - контроль доступа к данным
4. **Целостность** - гарантия корректности данных
5. **Многопользовательский доступ** - одновременная работа нескольких пользователей
6. **Масштабируемость** - обработка больших объемов данных

---

## 2. Основы SQL

### Что такое SQL?

**SQL (Structured Query Language)** — язык структурированных запросов для работы с реляционными базами данных.

### Категории SQL команд

#### DDL (Data Definition Language) - определение структуры

```sql
-- Создание базы данных
CREATE DATABASE library_db;

-- Использование БД
USE library_db;

-- Создание таблицы
CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    price DECIMAL(10, 2),
    published_date DATE,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Изменение таблицы
ALTER TABLE books ADD COLUMN publisher VARCHAR(200);
ALTER TABLE books MODIFY COLUMN price DECIMAL(12, 2);
ALTER TABLE books DROP COLUMN publisher;

-- Удаление таблицы
DROP TABLE IF EXISTS books;

-- Создание индекса для ускорения поиска
CREATE INDEX idx_author ON books(author);
CREATE INDEX idx_isbn ON books(isbn);
```

#### DML (Data Manipulation Language) - манипуляция данными

```sql
-- INSERT - вставка данных
INSERT INTO books (isbn, title, author, genre, price)
VALUES ('978-0-13-468599-1', 'Clean Code', 'Robert Martin', 'Programming', 29.99);

-- Множественная вставка
INSERT INTO books (isbn, title, author, genre) VALUES
    ('978-0-201-61622-4', 'The Pragmatic Programmer', 'Andrew Hunt', 'Programming'),
    ('978-0-596-00715-8', 'Head First Design Patterns', 'Eric Freeman', 'Programming'),
    ('978-0-321-12742-6', 'Effective Java', 'Joshua Bloch', 'Programming');

-- SELECT - выборка данных
SELECT * FROM books;                              -- все столбцы
SELECT title, author FROM books;                  -- конкретные столбцы
SELECT * FROM books WHERE genre = 'Programming';  -- с условием
SELECT * FROM books WHERE price > 25.00;
SELECT * FROM books WHERE author LIKE '%Martin%'; -- поиск по шаблону

-- UPDATE - обновление данных
UPDATE books
SET price = 34.99, is_available = TRUE
WHERE isbn = '978-0-13-468599-1';

-- DELETE - удаление данных
DELETE FROM books WHERE isbn = '978-0-13-468599-1';
DELETE FROM books WHERE price < 10.00;
```

#### DQL (Data Query Language) - сложные запросы

```sql
-- WHERE - фильтрация
SELECT * FROM books
WHERE genre = 'Programming' AND price < 30.00;

SELECT * FROM books
WHERE author = 'Robert Martin' OR author = 'Joshua Bloch';

-- ORDER BY - сортировка
SELECT * FROM books ORDER BY title ASC;          -- по возрастанию
SELECT * FROM books ORDER BY price DESC;         -- по убыванию
SELECT * FROM books ORDER BY author, title;      -- по нескольким полям

-- LIMIT - ограничение количества
SELECT * FROM books LIMIT 10;                    -- первые 10 записей
SELECT * FROM books LIMIT 5 OFFSET 10;           -- 5 записей, начиная с 11-й

-- GROUP BY - группировка
SELECT author, COUNT(*) as books_count
FROM books
GROUP BY author;

SELECT genre, AVG(price) as avg_price, COUNT(*) as total
FROM books
GROUP BY genre
HAVING COUNT(*) > 5;                             -- фильтр по группам

-- JOIN - объединение таблиц
-- INNER JOIN - только совпадающие записи
SELECT readers.name, books.title, borrowed_books.borrowed_at
FROM borrowed_books
INNER JOIN readers ON borrowed_books.reader_id = readers.id
INNER JOIN books ON borrowed_books.book_isbn = books.isbn;

-- LEFT JOIN - все записи из левой таблицы
SELECT readers.name, COUNT(borrowed_books.id) as books_count
FROM readers
LEFT JOIN borrowed_books ON readers.id = borrowed_books.reader_id
GROUP BY readers.id, readers.name;

-- Подзапросы
SELECT * FROM books
WHERE price > (SELECT AVG(price) FROM books);

-- DISTINCT - уникальные значения
SELECT DISTINCT author FROM books;
SELECT DISTINCT genre FROM books;

-- Агрегатные функции
SELECT
    COUNT(*) as total_books,
    AVG(price) as average_price,
    MIN(price) as min_price,
    MAX(price) as max_price,
    SUM(price) as total_value
FROM books;
```

#### DCL (Data Control Language) - управление доступом

```sql
-- Создание пользователя
CREATE USER 'library_user'@'localhost' IDENTIFIED BY 'library_pass';

-- Предоставление прав
GRANT SELECT, INSERT, UPDATE, DELETE ON library_db.* TO 'library_user'@'localhost';
GRANT ALL PRIVILEGES ON library_db.* TO 'library_admin'@'localhost';

-- Отзыв прав
REVOKE DELETE ON library_db.* FROM 'library_user'@'localhost';

-- Применение изменений
FLUSH PRIVILEGES;
```

#### TCL (Transaction Control Language) - управление транзакциями

```sql
-- Начало транзакции
START TRANSACTION;

-- Выполнение операций
UPDATE books SET is_available = FALSE WHERE isbn = '978-0-13-468599-1';
INSERT INTO borrowed_books (reader_id, book_isbn) VALUES ('R001', '978-0-13-468599-1');

-- Подтверждение изменений
COMMIT;

-- Или откат изменений в случае ошибки
ROLLBACK;

-- Пример транзакции с проверкой
START TRANSACTION;

UPDATE accounts SET balance = balance - 100 WHERE id = 1;
UPDATE accounts SET balance = balance + 100 WHERE id = 2;

-- Если все ок
COMMIT;
-- Если ошибка
-- ROLLBACK;
```

### Важные концепции SQL

#### Первичный ключ (Primary Key)

```sql
-- Один столбец как PK
CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255)
);

-- Составной PK
CREATE TABLE borrowed_books (
    reader_id VARCHAR(50),
    book_isbn VARCHAR(20),
    borrowed_at TIMESTAMP,
    PRIMARY KEY (reader_id, book_isbn)
);
```

#### Внешний ключ (Foreign Key)

```sql
CREATE TABLE borrowed_books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reader_id VARCHAR(50) NOT NULL,
    book_isbn VARCHAR(20) NOT NULL,
    borrowed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Внешние ключи
    FOREIGN KEY (reader_id) REFERENCES readers(id)
        ON DELETE CASCADE      -- при удалении читателя удалятся записи о выдачах
        ON UPDATE CASCADE,     -- при изменении id изменятся ссылки

    FOREIGN KEY (book_isbn) REFERENCES books(isbn)
        ON DELETE RESTRICT     -- запретить удаление книги, если есть активные выдачи
        ON UPDATE CASCADE
);
```

**Варианты ON DELETE/UPDATE:**
- `CASCADE` - каскадное удаление/обновление
- `SET NULL` - установить NULL
- `RESTRICT` - запретить операцию
- `NO ACTION` - не делать ничего (по умолчанию)

#### Ограничения (Constraints)

```sql
CREATE TABLE books (
    isbn VARCHAR(20) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,                    -- обязательное поле
    author VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) CHECK (price >= 0),       -- проверка значения
    rating INT CHECK (rating BETWEEN 1 AND 5),
    email VARCHAR(255) UNIQUE,                      -- уникальное значение
    status VARCHAR(20) DEFAULT 'available'          -- значение по умолчанию
);
```

#### Индексы

```sql
-- Создание индекса
CREATE INDEX idx_author ON books(author);
CREATE INDEX idx_title_author ON books(title, author);

-- Уникальный индекс
CREATE UNIQUE INDEX idx_email ON users(email);

-- Просмотр индексов
SHOW INDEX FROM books;

-- Удаление индекса
DROP INDEX idx_author ON books;
```

**Когда использовать индексы:**
- Столбцы в WHERE, JOIN, ORDER BY
- Внешние ключи
- Уникальные поля

**Когда НЕ использовать:**
- Маленькие таблицы
- Часто обновляемые столбцы
- Столбцы с низкой селективностью (много дубликатов)

---

## 3. Проектирование базы данных

### Нормализация

Процесс организации данных для уменьшения избыточности.

#### 1НФ (Первая нормальная форма)
- Атомарные значения (нельзя хранить списки в одной ячейке)
- Каждая запись уникальна

**Плохо:**
```sql
CREATE TABLE books (
    id INT PRIMARY KEY,
    title VARCHAR(255),
    authors VARCHAR(500)  -- "Robert Martin, Kent Beck, Martin Fowler"
);
```

**Хорошо:**
```sql
CREATE TABLE books (
    id INT PRIMARY KEY,
    title VARCHAR(255)
);

CREATE TABLE authors (
    id INT PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE book_authors (
    book_id INT,
    author_id INT,
    PRIMARY KEY (book_id, author_id)
);
```

#### 2НФ (Вторая нормальная форма)
- Соответствует 1НФ
- Все неключевые атрибуты зависят от первичного ключа целиком

#### 3НФ (Третья нормальная форма)
- Соответствует 2НФ
- Нет транзитивных зависимостей (неключевые атрибуты не зависят друг от друга)

### Типы связей

#### One-to-Many (Один ко многим)

Один читатель может взять много книг.

```sql
CREATE TABLE readers (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE borrowed_books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reader_id VARCHAR(50),
    book_isbn VARCHAR(20),
    FOREIGN KEY (reader_id) REFERENCES readers(id)
);
```

#### Many-to-Many (Многие ко многим)

Книга может иметь много авторов, автор может написать много книг.

```sql
CREATE TABLE books (
    id INT PRIMARY KEY,
    title VARCHAR(255)
);

CREATE TABLE authors (
    id INT PRIMARY KEY,
    name VARCHAR(255)
);

-- Промежуточная таблица
CREATE TABLE book_authors (
    book_id INT,
    author_id INT,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (author_id) REFERENCES authors(id)
);
```

#### One-to-One (Один к одному)

У каждого пользователя один профиль.

```sql
CREATE TABLE users (
    id INT PRIMARY KEY,
    username VARCHAR(50) UNIQUE
);

CREATE TABLE user_profiles (
    user_id INT PRIMARY KEY,
    bio TEXT,
    avatar_url VARCHAR(500),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Проектирование схемы нашего проекта

```sql
-- Главная сущность - Книги
CREATE TABLE books (
    isbn VARCHAR(20) PRIMARY KEY,           -- уникальный идентификатор
    title VARCHAR(255) NOT NULL,            -- название
    author VARCHAR(255) NOT NULL,           -- автор
    genre VARCHAR(100),                     -- жанр
    is_available BOOLEAN DEFAULT TRUE,      -- доступность
    times_read INT DEFAULT 0,               -- счетчик использования
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_author (author),              -- индекс для быстрого поиска
    INDEX idx_genre (genre),
    INDEX idx_available (is_available)
);

-- Читатели
CREATE TABLE readers (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- История выдачи книг
CREATE TABLE borrowed_books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reader_id VARCHAR(50) NOT NULL,
    book_isbn VARCHAR(20) NOT NULL,
    borrowed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    returned_at TIMESTAMP NULL,

    FOREIGN KEY (reader_id) REFERENCES readers(id) ON DELETE CASCADE,
    FOREIGN KEY (book_isbn) REFERENCES books(isbn) ON DELETE CASCADE,

    INDEX idx_reader_id (reader_id),
    INDEX idx_book_isbn (book_isbn),
    INDEX idx_borrowed_at (borrowed_at),
    INDEX idx_active_borrows (reader_id, returned_at)  -- для активных выдач
);
```

---

## 4. Подключение БД к Java проекту

### Архитектура подключения

```
┌─────────────┐
│  Main.java  │
└──────┬──────┘
       │
       ▼
┌────────────────┐
│ Service Layer  │  ← Бизнес-логика
│ LibraryService │
└────────┬───────┘
         │
         ▼
┌──────────────────┐
│ Repository Layer │  ← Работа с БД
│  BookRepository  │
└────────┬─────────┘
         │
         ▼
┌──────────────────────┐
│ DatabaseConnection   │  ← Connection Pool
│    (HikariCP)        │
└──────────┬───────────┘
           │
           ▼
    ┌──────────┐
    │  MySQL   │
    └──────────┘
```

### Шаг 1: Добавление зависимостей (pom.xml)

```xml
<dependencies>
    <!-- MySQL JDBC Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.2.0</version>
    </dependency>

    <!-- HikariCP - Connection Pool -->
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
        <version>5.1.0</version>
    </dependency>

    <!-- Lombok (опционально, для упрощения кода) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.40</version>
        <scope>provided</scope>
    </dependency>

    <!-- Logging -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>2.0.17</version>
    </dependency>
</dependencies>
```

### Шаг 2: Создание класса подключения

`src/main/java/ru/lostfly/config/DatabaseConnection.java`:

```java
package ru.lostfly.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class DatabaseConnection {
    private static HikariDataSource dataSource;

    // Параметры подключения
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String DB_USER = "library_user";
    private static final String DB_PASSWORD = "library_pass";

    // Инициализация пула соединений
    static {
        try {
            HikariConfig config = new HikariConfig();

            // Основные параметры
            config.setJdbcUrl(DB_URL);
            config.setUsername(DB_USER);
            config.setPassword(DB_PASSWORD);

            // Настройки пула
            config.setMaximumPoolSize(10);              // максимум соединений
            config.setMinimumIdle(2);                   // минимум простаивающих
            config.setConnectionTimeout(30000);         // таймаут подключения (30 сек)
            config.setIdleTimeout(600000);              // время простоя (10 мин)
            config.setMaxLifetime(1800000);             // время жизни (30 мин)

            // Дополнительные настройки
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);
            log.info("Database connection pool initialized successfully");

        } catch (Exception e) {
            log.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Unable to initialize database connection pool", e);
        }
    }

    /**
     * Получить соединение с базой данных из пула
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Закрыть пул соединений (вызывать при завершении приложения)
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("Database connection pool closed");
        }
    }

    /**
     * Проверить доступность базы данных
     */
    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isValid(5);
        } catch (SQLException e) {
            log.error("Database connection test failed", e);
            return false;
        }
    }
}
```

### Шаг 3: Создание Repository (паттерн Repository)

`src/main/java/ru/lostfly/repository/BookRepository.java`:

```java
package ru.lostfly.repository;

import ru.lostfly.business.domain.book.Book;

import java.util.List;

public interface BookRepository {
    void save(Book book);

    Book findByIsbn(String isbn);

    List<Book> findAvailableBooks();

    List<Book> findByAuthor(String author);

    int getTotalBooks();
}
```

`src/main/java/ru/lostfly/repository/impl/BookRepositoryDBImpl.java`:

```java
package ru.lostfly.repository.impl;

import lombok.extern.slf4j.Slf4j;
import ru.lostfly.config.DatabaseConnection;
import ru.lostfly.business.domain.book.Book;
import ru.lostfly.business.repository.BookRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BookRepositoryDBImpl implements BookRepository {

    @Override
    public void save(Book book) {
        // SQL запрос с параметрами (защита от SQL injection)
        String sql = "INSERT INTO books (isbn, title, author, genre, is_available, times_read) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "title = VALUES(title), " +
                "author = VALUES(author), " +
                "is_available = VALUES(is_available), " +
                "times_read = VALUES(times_read)";

        // try-with-resources автоматически закроет ресурсы
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Установка параметров (защита от SQL injection)
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, null);  // genre
            stmt.setBoolean(5, book.isAvailable());
            stmt.setInt(6, book.getTimesRead());

            int rowsAffected = stmt.executeUpdate();
            log.info("Book saved/updated: {}, rows affected: {}", book.getIsbn(), rowsAffected);

        } catch (SQLException e) {
            log.error("Error saving book: {}", book.getIsbn(), e);
            throw new RuntimeException("Failed to save book", e);
        }
    }

    @Override
    public Book findByIsbn(String isbn) {
        String sql = "SELECT isbn, title, author, genre, is_available, times_read " +
                "FROM books WHERE isbn = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToBook(rs);
            }

            log.debug("Book not found with ISBN: {}", isbn);
            return null;

        } catch (SQLException e) {
            log.error("Error finding book by ISBN: {}", isbn, e);
            throw new RuntimeException("Failed to find book", e);
        }
    }

    @Override
    public List<Book> findAvailableBooks() {
        String sql = "SELECT isbn, title, author, genre, is_available, times_read " +
                "FROM books WHERE is_available = TRUE";
        List<Book> books = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }

            log.debug("Found {} available books", books.size());
            return books;

        } catch (SQLException e) {
            log.error("Error finding available books", e);
            throw new RuntimeException("Failed to find available books", e);
        }
    }

    @Override
    public List<Book> findByAuthor(String author) {
        String sql = "SELECT isbn, title, author, genre, is_available, times_read " +
                "FROM books WHERE LOWER(author) LIKE LOWER(?)";
        List<Book> books = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + author + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }

            log.debug("Found {} books by author: {}", books.size(), author);
            return books;

        } catch (SQLException e) {
            log.error("Error finding books by author: {}", author, e);
            throw new RuntimeException("Failed to find books by author", e);
        }
    }

    @Override
    public int getTotalBooks() {
        String sql = "SELECT COUNT(*) as total FROM books";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException e) {
            log.error("Error getting total books count", e);
            throw new RuntimeException("Failed to get total books count", e);
        }
    }

    /**
     * Маппинг ResultSet в объект Book
     */
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        String isbn = rs.getString("isbn");
        String title = rs.getString("title");
        String author = rs.getString("author");
        String genre = rs.getString("genre");

        Book book = new Book(isbn, title, author, genre);
        book.setAvailable(rs.getBoolean("is_available"));
        book.setTimesRead(rs.getInt("times_read"));

        return book;
    }
}
```

### Важные концепции JDBC

#### PreparedStatement vs Statement

**НЕ делайте так (уязвимость к SQL injection):**
```java
String sql = "SELECT * FROM books WHERE isbn = '" + isbn + "'";
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery(sql);
```

**Делайте так:**
```java
String sql = "SELECT * FROM books WHERE isbn = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, isbn);
ResultSet rs = stmt.executeQuery();
```

**Преимущества PreparedStatement:**
- Защита от SQL injection
- Лучшая производительность (кэширование плана выполнения)
- Автоматическое экранирование спецсимволов

#### Try-with-resources

```java
// Старый способ (плохо)
Connection conn = null;
PreparedStatement stmt = null;
ResultSet rs = null;
try {
    conn = DatabaseConnection.getConnection();
    stmt = conn.prepareStatement(sql);
    rs = stmt.executeQuery();
    // работа с данными
} catch (SQLException e) {
    e.printStackTrace();
} finally {
    if (rs != null) rs.close();
    if (stmt != null) stmt.close();
    if (conn != null) conn.close();
}

// Современный способ (хорошо)
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql);
     ResultSet rs = stmt.executeQuery()) {
    // работа с данными
} // ресурсы закрываются автоматически
```

#### Connection Pool (HikariCP)

**Зачем нужен пул соединений?**
- Создание соединения с БД - медленная операция (~100-200ms)
- Пул переиспользует соединения
- Контролирует количество одновременных соединений
- Автоматически восстанавливает "протухшие" соединения

**Как работает:**
```
Application → getConnection() → Pool проверяет:
  - Есть свободное соединение? → Возвращает его
  - Можно создать новое? → Создает и возвращает
  - Достигнут лимит? → Ждет освобождения
```

---

## 5. Docker для баз данных

### Что такое Docker?

**Docker** - платформа для разработки, доставки и запуска приложений в контейнерах.

**Контейнер** - изолированная среда выполнения со всеми необходимыми зависимостями.

### Преимущества Docker для БД

1. **Изоляция** - БД работает в отдельной среде
2. **Воспроизводимость** - одинаковое окружение на всех машинах
3. **Простота** - не нужно устанавливать MySQL локально
4. **Версионирование** - легко переключаться между версиями
5. **Очистка** - удаление контейнера не оставляет "мусора"

### Docker Compose для нашего проекта

`docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0                    # образ MySQL версии 8.0
    container_name: library_mysql       # имя контейнера

    environment:                        # переменные окружения
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: library_db
      MYSQL_USER: library_user
      MYSQL_PASSWORD: library_pass

    ports:
      - "3306:3306"                     # проброс порта HOST:CONTAINER

    volumes:
      - mysql_data:/var/lib/mysql       # постоянное хранилище данных
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql  # скрипт инициализации

    networks:
      - library_network                 # внутренняя сеть

    healthcheck:                        # проверка здоровья контейнера
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-prootpassword"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql_data:                           # именованный volume для данных

networks:
  library_network:                      # создание сети
    driver: bridge
```

### Основные команды Docker Compose

```bash
# Запуск всех сервисов в фоновом режиме
docker-compose up -d

# Просмотр запущенных контейнеров
docker-compose ps

# Просмотр логов
docker-compose logs -f mysql

# Остановка сервисов
docker-compose stop

# Остановка и удаление контейнеров
docker-compose down

# Остановка и удаление контейнеров + volumes (удалятся данные!)
docker-compose down -v

# Перезапуск сервиса
docker-compose restart mysql

# Пересборка образа
docker-compose build

# Просмотр использования ресурсов
docker-compose stats
```

### Работа с контейнерами

```bash
# Список всех контейнеров
docker ps -a

# Зайти внутрь контейнера
docker exec -it library_mysql bash

# Выполнить команду в контейнере
docker exec library_mysql mysql -u root -prootpassword -e "SHOW DATABASES;"

# Подключиться к MySQL из контейнера
docker exec -it library_mysql mysql -u library_user -plibrary_pass library_db

# Копирование файлов
docker cp backup.sql library_mysql:/tmp/backup.sql
docker cp library_mysql:/tmp/backup.sql ./backup.sql

# Просмотр логов
docker logs library_mysql
docker logs -f library_mysql  # follow режим
docker logs --tail 100 library_mysql  # последние 100 строк

# Остановка контейнера
docker stop library_mysql

# Запуск остановленного контейнера
docker start library_mysql

# Удаление контейнера
docker rm library_mysql

# Удаление образа
docker rmi mysql:8.0
```

### Volumes (Постоянное хранилище)

**Зачем нужны volumes?**
- Данные контейнера эфемерные (исчезают при удалении)
- Volumes сохраняют данные между перезапусками
- Volumes можно переиспользовать

```bash
# Список volumes
docker volume ls

# Информация о volume
docker volume inspect mysql_data

# Создание volume
docker volume create my_mysql_data

# Удаление volume
docker volume rm mysql_data

# Удаление всех неиспользуемых volumes
docker volume prune
```

### Docker Networks

```bash
# Список сетей
docker network ls

# Информация о сети
docker network inspect library_network

# Создание сети
docker network create my_network

# Подключение контейнера к сети
docker network connect my_network library_mysql

# Отключение контейнера от сети
docker network disconnect my_network library_mysql
```

### Dockerfile vs Docker Compose

**Dockerfile** - инструкции для создания образа
**Docker Compose** - оркестрация нескольких контейнеров

Пример Dockerfile (если бы мы создавали свой образ):

```dockerfile
FROM mysql:8.0

# Установка переменных окружения
ENV MYSQL_DATABASE=library_db
ENV MYSQL_USER=library_user
ENV MYSQL_PASSWORD=library_pass

# Копирование скриптов инициализации
COPY ./database/init.sql /docker-entrypoint-initdb.d/

# Копирование конфигурации MySQL
COPY ./config/my.cnf /etc/mysql/conf.d/

# Открытие порта
EXPOSE 3306
```

---

## 6. Практические примеры

### Пример 1: Полный цикл работы с книгой

```java
public class LibraryExample {
    public static void main(String[] args) {
        BookRepository bookRepo = new BookRepositoryDBImpl();

        // 1. Создание новой книги
        Book newBook = new Book(
            "978-0-13-468599-1",
            "Clean Code",
            "Robert Martin",
            "Programming"
        );

        // 2. Сохранение в БД
        bookRepo.save(newBook);
        System.out.println("Book saved!");

        // 3. Поиск книги
        Book found = bookRepo.findByIsbn("978-0-13-468599-1");
        System.out.println("Found: " + found.getTitle());

        // 4. Обновление книги
        found.setAvailable(false);
        bookRepo.save(found);
        System.out.println("Book updated!");

        // 5. Поиск всех доступных книг
        List<Book> available = bookRepo.findAvailableBooks();
        System.out.println("Available books: " + available.size());

        // 6. Закрытие пула соединений
        DatabaseConnection.close();
    }
}
```

### Пример 2: Транзакции

```java
public void borrowBook(String readerId, String bookIsbn) {
    Connection conn = null;
    try {
        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);  // Начало транзакции

        // 1. Проверка доступности книги
        String checkSql = "SELECT is_available FROM books WHERE isbn = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setString(1, bookIsbn);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next() || !rs.getBoolean("is_available")) {
                throw new RuntimeException("Book not available");
            }
        }

        // 2. Пометка книги как недоступной
        String updateBookSql = "UPDATE books SET is_available = FALSE WHERE isbn = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateBookSql)) {
            stmt.setString(1, bookIsbn);
            stmt.executeUpdate();
        }

        // 3. Создание записи о выдаче
        String insertBorrowSql = "INSERT INTO borrowed_books (reader_id, book_isbn) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertBorrowSql)) {
            stmt.setString(1, readerId);
            stmt.setString(2, bookIsbn);
            stmt.executeUpdate();
        }

        conn.commit();  // Подтверждение транзакции
        log.info("Book borrowed successfully");

    } catch (Exception e) {
        if (conn != null) {
            try {
                conn.rollback();  // Откат при ошибке
                log.error("Transaction rolled back", e);
            } catch (SQLException ex) {
                log.error("Rollback failed", ex);
            }
        }
        throw new RuntimeException("Failed to borrow book", e);
    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                log.error("Failed to close connection", e);
            }
        }
    }
}
```

### Пример 3: Batch операции

```java
public void addMultipleBooks(List<Book> books) {
    String sql = "INSERT INTO books (isbn, title, author, genre, is_available, times_read) " +
                 "VALUES (?, ?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        conn.setAutoCommit(false);

        for (Book book : books) {
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, null);
            stmt.setBoolean(5, book.isAvailable());
            stmt.setInt(6, book.getTimesRead());

            stmt.addBatch();  // Добавление в batch
        }

        int[] results = stmt.executeBatch();  // Выполнение всех запросов
        conn.commit();

        log.info("Added {} books", results.length);

    } catch (SQLException e) {
        log.error("Error adding multiple books", e);
        throw new RuntimeException("Failed to add books", e);
    }
}
```

### Пример 4: Использование DTO для сложных запросов

```java
@Data
@AllArgsConstructor
public class BookStatistics {
    private String isbn;
    private String title;
    private String author;
    private int timesRead;
    private int currentlyBorrowed;
}

public List<BookStatistics> getBookStatistics() {
    String sql =
        "SELECT b.isbn, b.title, b.author, b.times_read, " +
        "COUNT(bb.id) as currently_borrowed " +
        "FROM books b " +
        "LEFT JOIN borrowed_books bb ON b.isbn = bb.book_isbn AND bb.returned_at IS NULL " +
        "GROUP BY b.isbn, b.title, b.author, b.times_read " +
        "ORDER BY b.times_read DESC";

    List<BookStatistics> stats = new ArrayList<>();

    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            stats.add(new BookStatistics(
                rs.getString("isbn"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getInt("times_read"),
                rs.getInt("currently_borrowed")
            ));
        }

        return stats;

    } catch (SQLException e) {
        log.error("Error getting book statistics", e);
        throw new RuntimeException("Failed to get statistics", e);
    }
}
```

---

## 7. Best Practices

### Безопасность

#### 1. Всегда используйте PreparedStatement

```java
// ❌ ПЛОХО - SQL Injection уязвимость
String sql = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
// Атака: username = "admin' --"

// ✅ ХОРОШО
String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, username);
stmt.setString(2, password);
```

#### 2. Не храните пароли открытым текстом

```java
// ❌ ПЛОХО
INSERT INTO users (username, password) VALUES ('admin', 'password123');

// ✅ ХОРОШО - хешируйте пароли (BCrypt, Argon2)
String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
INSERT INTO users (username, password_hash) VALUES ('admin', '$2a$10$...');
```

#### 3. Используйте разные учетные записи

```sql
-- Пользователь только для чтения
CREATE USER 'app_readonly'@'localhost' IDENTIFIED BY 'password';
GRANT SELECT ON library_db.* TO 'app_readonly'@'localhost';

-- Пользователь приложения
CREATE USER 'app_user'@'localhost' IDENTIFIED BY 'password';
GRANT SELECT, INSERT, UPDATE ON library_db.* TO 'app_user'@'localhost';

-- Админ
CREATE USER 'app_admin'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON library_db.* TO 'app_admin'@'localhost';
```

#### 4. Валидация входных данных

```java
public void save(Book book) {
    // Валидация
    if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
        throw new IllegalArgumentException("ISBN cannot be empty");
    }
    if (book.getTitle() == null || book.getTitle().length() > 255) {
        throw new IllegalArgumentException("Invalid title");
    }

    // Сохранение...
}
```

### Производительность

#### 1. Используйте Connection Pool

```java
// ✅ HikariCP уже реализован в DatabaseConnection
Connection conn = DatabaseConnection.getConnection();
```

#### 2. Закрывайте ресурсы (try-with-resources)

```java
// ✅ Автоматическое закрытие
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    // ...
}
```

#### 3. Используйте индексы правильно

```sql
-- ✅ Индекс для частых запросов
CREATE INDEX idx_author ON books(author);
SELECT * FROM books WHERE author = 'Robert Martin';  -- использует индекс

-- ❌ Индекс не используется
SELECT * FROM books WHERE LOWER(author) = 'robert martin';  -- full scan
```

#### 4. Избегайте N+1 проблемы

```java
// ❌ ПЛОХО - N+1 запросов
List<Reader> readers = readerRepo.findAll();  // 1 запрос
for (Reader reader : readers) {
    List<Book> books = bookRepo.findByReader(reader.getId());  // N запросов
}

// ✅ ХОРОШО - 1 запрос с JOIN
SELECT r.*, b.*
FROM readers r
LEFT JOIN borrowed_books bb ON r.id = bb.reader_id
LEFT JOIN books b ON bb.book_isbn = b.isbn;
```

#### 5. Используйте LIMIT для больших выборок

```java
// ✅ Пагинация
String sql = "SELECT * FROM books LIMIT ? OFFSET ?";
stmt.setInt(1, pageSize);
stmt.setInt(2, pageNumber * pageSize);
```

#### 6. Batch операции для множественных INSERT/UPDATE

```java
// ✅ Batch вместо цикла отдельных запросов
for (Book book : books) {
    stmt.setString(1, book.getIsbn());
    stmt.addBatch();
}
stmt.executeBatch();
```

### Архитектура

#### 1. Используйте паттерн Repository

```
Domain Objects (Book, Reader)
       ↕
Repository Interface
       ↕
Repository Implementation (DB logic)
       ↕
Database
```

#### 2. Разделение ответственности

```java
// ❌ ПЛОХО - все в одном классе
public class Library {
    public void addBook() { /* SQL здесь */ }
    public void validateBook() { /* */ }
    public void displayBook() { /* */ }
}

// ✅ ХОРОШО
public class BookRepository { /* только работа с БД */ }
public class BookValidator { /* только валидация */ }
public class BookService { /* бизнес-логика */ }
public class BookController { /* UI/API */ }
```

#### 3. Обработка ошибок

```java
public Book findByIsbn(String isbn) {
    try {
        // БД операции
    } catch (SQLException e) {
        log.error("Database error finding book: {}", isbn, e);
        throw new RepositoryException("Failed to find book", e);
    }
}
```

### Миграции БД

Используйте инструменты для версионирования схемы БД:

#### Flyway

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
    <version>9.22.0</version>
</dependency>
```

```sql
-- V1__create_books_table.sql
CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);

-- V2__add_author_column.sql
ALTER TABLE books ADD COLUMN author VARCHAR(255);

-- V3__add_isbn_column.sql
ALTER TABLE books ADD COLUMN isbn VARCHAR(20) UNIQUE;
```

### Конфигурация через переменные окружения

```java
// ✅ Не храните credentials в коде
private static final String DB_URL = System.getenv("DB_URL");
private static final String DB_USER = System.getenv("DB_USER");
private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");
```

```bash
# .env файл (не коммитить в Git!)
DB_URL=jdbc:mysql://localhost:3306/library_db
DB_USER=library_user
DB_PASSWORD=library_pass
```

### Тестирование

```java
// Используйте тестовую БД для unit тестов
@Test
public void testSaveBook() {
    // Arrange
    BookRepository repo = new BookRepositoryDBImpl();
    Book book = new Book("123", "Test Book", "Author");

    // Act
    repo.save(book);
    Book found = repo.findByIsbn("123");

    // Assert
    assertEquals("Test Book", found.getTitle());
}
```

---

## Полезные ссылки

### Документация
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [JDBC Tutorial](https://docs.oracle.com/javase/tutorial/jdbc/)
- [HikariCP GitHub](https://github.com/brettwooldridge/HikariCP)
- [Docker Documentation](https://docs.docker.com/)

### SQL практика
- [SQLBolt](https://sqlbolt.com/) - интерактивные упражнения
- [LeetCode Database](https://leetcode.com/problemset/database/) - задачи по SQL
- [HackerRank SQL](https://www.hackerrank.com/domains/sql)

### Книги
- "SQL для простых смертных" - Мартин Грубер
- "High Performance MySQL" - Baron Schwartz
- "Java Persistence with Hibernate" - Christian Bauer

---

## Заключение

Этот гайд покрывает основы работы с базами данных в Java проектах. В нашем проекте реализованы:

1. ✅ Структурированная схема БД с нормализацией
2. ✅ Connection Pool для эффективного управления соединениями
3. ✅ Repository паттерн для разделения логики
4. ✅ PreparedStatement для защиты от SQL injection
5. ✅ Docker Compose для изоляции и воспроизводимости
6. ✅ Логирование и обработка ошибок
7. ✅ Try-with-resources для автоматического управления ресурсами

**Следующие шаги для углубления знаний:**
- ORM фреймворки (Hibernate, JPA)
- NoSQL базы данных (MongoDB, Redis)
- Миграции БД (Flyway, Liquibase)
- Кэширование (Redis, Memcached)
- Репликация и шардинг
- Monitoring и оптимизация запросов