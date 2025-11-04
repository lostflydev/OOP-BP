-- Создание базы данных (если не создалась автоматически)
CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

-- Таблица для книг
CREATE TABLE IF NOT EXISTS books (
    isbn VARCHAR(20) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    is_available BOOLEAN DEFAULT TRUE,
    times_read INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Таблица для читателей
CREATE TABLE IF NOT EXISTS readers (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Таблица для связи читателей и книг (взятые книги)
CREATE TABLE IF NOT EXISTS borrowed_books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reader_id VARCHAR(50) NOT NULL,
    book_isbn VARCHAR(20) NOT NULL,
    borrowed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    returned_at TIMESTAMP NULL,
    FOREIGN KEY (reader_id) REFERENCES readers(id) ON DELETE CASCADE,
    FOREIGN KEY (book_isbn) REFERENCES books(isbn) ON DELETE CASCADE,
    INDEX idx_reader_id (reader_id),
    INDEX idx_book_isbn (book_isbn),
    INDEX idx_borrowed_at (borrowed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Добавление тестовых данных
INSERT INTO books (isbn, title, author, genre, is_available, times_read) VALUES
('978-0-13-468599-1', 'Clean Code', 'Robert Martin', 'Programming', TRUE, 0),
('978-0-201-61622-4', 'The Pragmatic Programmer', 'Andrew Hunt', 'Programming', TRUE, 0),
('978-0-596-00715-8', 'Head First Design Patterns', 'Eric Freeman', 'Programming', TRUE, 0);

INSERT INTO readers (id, name) VALUES
('R001', 'Иван Иванов'),
('R002', 'Мария Петрова'),
('R003', 'Алексей Сидоров');

-- Проверка созданных таблиц
SHOW TABLES;