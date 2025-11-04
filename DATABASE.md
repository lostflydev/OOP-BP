# База данных для Library Management System

## Описание

Проект использует MySQL для хранения данных о книгах, читателях и взятых книгах.

## Структура базы данных

### Таблицы:

1. **books** - книги в библиотеке
   - `isbn` (VARCHAR) - уникальный идентификатор книги
   - `title` (VARCHAR) - название книги
   - `author` (VARCHAR) - автор книги
   - `genre` (VARCHAR) - жанр книги
   - `is_available` (BOOLEAN) - доступность книги
   - `times_read` (INT) - количество раз, когда книгу взяли

2. **readers** - читатели библиотеки
   - `id` (VARCHAR) - уникальный идентификатор читателя
   - `name` (VARCHAR) - имя читателя

3. **borrowed_books** - связь между читателями и взятыми книгами
   - `id` (INT) - автоинкремент
   - `reader_id` (VARCHAR) - ссылка на читателя
   - `book_isbn` (VARCHAR) - ссылка на книгу
   - `borrowed_at` (TIMESTAMP) - время взятия книги
   - `returned_at` (TIMESTAMP) - время возврата книги

## Запуск базы данных

### 1. Запуск через Docker Compose

```bash
# Запустить MySQL в Docker
docker-compose up -d

# Проверить статус контейнера
docker-compose ps

# Посмотреть логи
docker-compose logs -f mysql
```

### 2. Остановка и удаление

```bash
# Остановить контейнер
docker-compose down

# Удалить контейнер вместе с данными
docker-compose down -v
```

## Подключение к базе данных

### Параметры подключения:

- **Host**: localhost
- **Port**: 3306
- **Database**: library_db
- **Username**: library_user
- **Password**: library_pass
- **Root Password**: rootpassword

### Через MySQL CLI (из контейнера):

```bash
# Зайти в контейнер
docker exec -it library_mysql bash

# Подключиться к MySQL как root
mysql -u root -prootpassword library_db

# Или как обычный пользователь
mysql -u library_user -plibrary_pass library_db
```

### Через MySQL CLI (с хоста):

```bash
# Если установлен MySQL клиент на хосте
mysql -h localhost -P 3306 -u library_user -plibrary_pass library_db
```

### Через MySQL Workbench или другие GUI клиенты:

1. Создать новое подключение
2. Hostname: `localhost`
3. Port: `3306`
4. Username: `library_user`
5. Password: `library_pass`
6. Database: `library_db`

## Полезные SQL команды

### Просмотр структуры базы данных

```sql
-- Показать все таблицы
SHOW TABLES;

-- Показать структуру таблицы
DESCRIBE books;
DESCRIBE readers;
DESCRIBE borrowed_books;

-- Показать все базы данных
SHOW DATABASES;
```

### Просмотр данных

```sql
-- Все книги
SELECT * FROM books;

-- Доступные книги
SELECT * FROM books WHERE is_available = TRUE;

-- Все читатели
SELECT * FROM readers;

-- Взятые книги (активные)
SELECT r.name, b.title, bb.borrowed_at
FROM borrowed_books bb
JOIN readers r ON bb.reader_id = r.id
JOIN books b ON bb.book_isbn = b.isbn
WHERE bb.returned_at IS NULL;

-- Количество книг у каждого читателя
SELECT r.id, r.name, COUNT(bb.id) as books_count
FROM readers r
LEFT JOIN borrowed_books bb ON r.id = bb.reader_id AND bb.returned_at IS NULL
GROUP BY r.id, r.name;
```

### Очистка данных

```sql
-- Удалить все записи о взятых книгах
DELETE FROM borrowed_books;

-- Удалить всех читателей
DELETE FROM readers;

-- Удалить все книги
DELETE FROM books;

-- Сбросить все таблицы (ОСТОРОЖНО!)
TRUNCATE TABLE borrowed_books;
TRUNCATE TABLE readers;
TRUNCATE TABLE books;
```

### Добавление тестовых данных

```sql
-- Добавить книгу
INSERT INTO books (isbn, title, author, genre, is_available, times_read)
VALUES ('978-0-13-468599-1', 'Clean Code', 'Robert Martin', 'Programming', TRUE, 0);

-- Добавить читателя
INSERT INTO readers (id, name) VALUES ('R001', 'Иван Иванов');

-- Взять книгу
INSERT INTO borrowed_books (reader_id, book_isbn)
VALUES ('R001', '978-0-13-468599-1');

-- Обновить доступность книги
UPDATE books SET is_available = FALSE WHERE isbn = '978-0-13-468599-1';
```

## Использование в коде

### Подключение к БД

```java
// Получить соединение
Connection conn = DatabaseConnection.getConnection();

// Проверить соединение
boolean isConnected = DatabaseConnection.testConnection();

// Закрыть пул соединений (при завершении приложения)
DatabaseConnection.close();
```

### Использование репозиториев

```java
// BookRepository с БД
BookRepository bookRepo = new BookRepositoryDBImpl();
bookRepo.save(new Book("123", "Test Book", "Author"));
Book book = bookRepo.findByIsbn("123");

// ReaderRepository с БД
ReaderRepository readerRepo = new ReaderRepositoryDBImpl();
readerRepo.save(new Reader("R001", "John Doe"));
Reader reader = readerRepo.findById("R001");
```

## Troubleshooting

### Ошибка подключения

Если не удается подключиться к БД:

1. Проверьте, что контейнер запущен: `docker ps`
2. Проверьте логи: `docker-compose logs mysql`
3. Убедитесь, что порт 3306 не занят другим процессом: `lsof -i :3306` (MacOS/Linux)
4. Дождитесь полной инициализации БД (обычно 10-30 секунд)

### Пересоздание базы данных

Если нужно пересоздать БД с нуля:

```bash
# Остановить и удалить контейнеры с данными
docker-compose down -v

# Запустить заново
docker-compose up -d

# Подождать инициализации
sleep 30

# Проверить логи
docker-compose logs mysql
```

### Бэкап базы данных

```bash
# Создать бэкап
docker exec library_mysql mysqldump -u root -prootpassword library_db > backup.sql

# Восстановить из бэкапа
docker exec -i library_mysql mysql -u root -prootpassword library_db < backup.sql
```

## Производительность

Проект использует HikariCP - высокопроизводительный connection pool для Java. Настройки пула:

- Максимальное количество соединений: 10
- Минимальное количество простаивающих соединений: 2
- Timeout подключения: 30 секунд
- Время жизни соединения: 30 минут

Эти настройки можно изменить в классе `DatabaseConnection.java`.