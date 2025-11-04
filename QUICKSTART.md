# Быстрый старт с базой данных

## Шаг 1: Запуск MySQL в Docker

```bash
# Запустить MySQL контейнер
docker-compose up -d

# Проверить, что контейнер запущен
docker ps | grep library_mysql

# Посмотреть логи (опционально)
docker-compose logs -f mysql
```

Подождите 10-30 секунд, пока база данных полностью инициализируется.

## Шаг 2: Проверка подключения

Подключитесь к базе данных одним из способов:

### Вариант А: Через скрипт (рекомендуется)
```bash
./database/connect.sh
```

### Вариант Б: Напрямую через Docker
```bash
docker exec -it library_mysql mysql -u library_user -plibrary_pass library_db
```

### Вариант В: MySQL Workbench или другой GUI клиент
- Host: `localhost`
- Port: `3306`
- Username: `library_user`
- Password: `library_pass`
- Database: `library_db`

## Шаг 3: Проверка таблиц

После подключения к базе выполните:

```sql
SHOW TABLES;
```

Должны быть видны 3 таблицы:
- `books`
- `readers`
- `borrowed_books`

Просмотр тестовых данных:
```sql
SELECT * FROM books;
SELECT * FROM readers;
```

## Шаг 4: Сборка проекта

```bash
mvn clean compile
```

## Шаг 5: Запуск приложения с базой данных

Откройте `src/main/java/ru/lostfly/Main.java` и раскомментируйте строку:

```java
App app = new App(RepositoryComponent.RepositoryMode.DATABASE);
```

Закомментируйте строку:
```java
// App app = new App();
```

Запустите приложение:
```bash
mvn exec:java -Dexec.mainClass="ru.lostfly.Main"
```

Или через IDE (IntelliJ IDEA / Eclipse).

## Шаг 6: Проверка работы

В приложении попробуйте:

1. Просмотреть доступные книги:
   ```
   /list_available_books
   ```

2. Добавить нового читателя:
   ```
   /add_user
   ```

3. Взять книгу:
   ```
   /borrow_book
   ```

4. Проверить в базе данных:
   ```sql
   -- В другом терминале подключитесь к БД
   ./database/connect.sh

   -- Посмотрите активные взятия книг
   SELECT r.name, b.title, bb.borrowed_at
   FROM borrowed_books bb
   JOIN readers r ON bb.reader_id = r.id
   JOIN books b ON bb.book_isbn = b.isbn
   WHERE bb.returned_at IS NULL;
   ```

## Остановка

```bash
# Остановить приложение: Ctrl+C

# Остановить Docker контейнер
docker-compose down

# Остановить и удалить все данные
docker-compose down -v
```

## Переключение режимов

Приложение поддерживает два режима:

1. **IN_MEMORY** (по умолчанию) - данные в памяти, исчезают при перезапуске
2. **DATABASE** - данные в MySQL, сохраняются между запусками

Переключение в `Main.java`:
```java
// In-memory режим
App app = new App();

// Database режим
App app = new App(RepositoryComponent.RepositoryMode.DATABASE);
```

## Troubleshooting

### Ошибка подключения к БД

1. Проверьте, что Docker контейнер запущен:
   ```bash
   docker ps
   ```

2. Проверьте логи:
   ```bash
   docker-compose logs mysql
   ```

3. Перезапустите контейнер:
   ```bash
   docker-compose restart
   ```

### Ошибка компиляции Lombok

Выполните:
```bash
mvn clean compile -U
```

### База данных не создается

Пересоздайте контейнер:
```bash
docker-compose down -v
docker-compose up -d
sleep 30
docker-compose logs mysql
```

## Полезные команды

```bash
# Просмотр всех книг в БД
docker exec library_mysql mysql -u library_user -plibrary_pass library_db -e "SELECT * FROM books;"

# Просмотр всех читателей
docker exec library_mysql mysql -u library_user -plibrary_pass library_db -e "SELECT * FROM readers;"

# Бэкап базы данных
docker exec library_mysql mysqldump -u root -prootpassword library_db > backup_$(date +%Y%m%d).sql

# Восстановление из бэкапа
docker exec -i library_mysql mysql -u root -prootpassword library_db < backup.sql
```

## Документация

Подробная документация доступна в файле `DATABASE.md`.