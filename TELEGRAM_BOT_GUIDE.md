# 📚 Гайд: Перевод консольного приложения в Telegram бота

## 📋 Содержание
1. [Архитектурные отличия](#1-архитектурные-отличия)
2. [Long Polling vs Webhook](#2-long-polling-vs-webhook)
3. [Подготовка и регистрация бота](#3-подготовка-и-регистрация-бота)
4. [Работа с переменными окружения](#4-работа-с-переменными-окружения)
5. [Выбор библиотеки для Java](#5-выбор-библиотеки-для-java)
6. [Пошаговая миграция](#6-пошаговая-миграция)
7. [Структура проекта](#7-структура-проекта)
8. [Примеры реализации](#8-примеры-реализации)

---

## 1. Архитектурные отличия

### Консольное приложение
```
┌─────────────────────────────────────┐
│        Консольное приложение        │
├─────────────────────────────────────┤
│  • Синхронное выполнение            │
│  • Один пользователь                │
│  • Scanner для ввода                │
│  • System.out для вывода            │
│  • while(true) главный цикл         │
└─────────────────────────────────────┘
         ↓ (пользователь вводит)
    [Terminal Input]
         ↓
    [Command Processing]
         ↓
    [Terminal Output]
```

### Telegram бот
```
┌─────────────────────────────────────┐
│          Telegram бот               │
├─────────────────────────────────────┤
│  • Асинхронная обработка            │
│  • Множество пользователей          │
│  • Update объекты для ввода         │
│  • SendMessage для вывода           │
│  • Event-driven архитектура         │
└─────────────────────────────────────┘

  User 1 ──┐
  User 2 ──┼──→ [Telegram API] ──→ [Your Bot] ──→ [LibraryService]
  User 3 ──┘                              ↓
                                    [Response Queue]
                                          ↓
                                   [Telegram API]
                                          ↓
                                   [Users receive]
```

**Ключевые изменения:**
- **Вместо Scanner** → Update объекты от Telegram
- **Вместо System.out** → методы sendMessage()
- **Вместо while(true)** → обработчики событий (handlers)
- **Сессии пользователей** → каждый пользователь имеет chat_id

---

## 2. Long Polling vs Webhook

### 🔄 Long Polling (Рекомендуется для начала)

```
┌──────────────┐                    ┌─────────────────┐
│   Your Bot   │                    │  Telegram API   │
│  (Server)    │                    │   (Servers)     │
└──────┬───────┘                    └────────┬────────┘
       │                                     │
       │  1. getUpdates request              │
       │────────────────────────────────────>│
       │                                     │
       │  2. Wait... (до 30 сек)            │
       │     [Нет сообщений]                 │
       │                                     │
       │  3. User sends message              │
       │                            ┌────────┤
       │                            │ User   │
       │                            └────────┤
       │                                     │
       │  4. Response with updates           │
       │<────────────────────────────────────│
       │                                     │
       │  5. Process updates                 │
       ├─────────┐                           │
       │         │                           │
       │<────────┘                           │
       │                                     │
       │  6. getUpdates request (offset)     │
       │────────────────────────────────────>│
       │                                     │
       └─────────────────────────────────────┘
```

**Как работает:**
1. Ваш бот **постоянно спрашивает** Telegram: "Есть новые сообщения?"
2. Telegram **держит соединение открытым** до 30 секунд
3. Если приходит сообщение — сразу отдаёт
4. Если нет — возвращает пустой ответ через 30 сек
5. Бот сразу делает новый запрос

**Преимущества:**
- ✅ Проще в настройке (не нужен публичный домен)
- ✅ Работает за NAT/firewall
- ✅ Идеально для разработки и тестирования
- ✅ Подходит для небольших проектов

**Недостатки:**
- ❌ Постоянные HTTP запросы (больше нагрузки)
- ❌ Задержка до 1-2 секунд
- ❌ Не масштабируется для больших нагрузок

### 🪝 Webhook

```
┌──────────────┐                    ┌─────────────────┐
│   Your Bot   │                    │  Telegram API   │
│  (Server)    │                    │   (Servers)     │
│ HTTPS        │                    └────────┬────────┘
│ Public IP    │                             │
└──────┬───────┘                             │
       │                                     │
       │  1. setWebhook(your_url)            │
       │────────────────────────────────────>│
       │                                     │
       │  2. OK, webhook set                 │
       │<────────────────────────────────────│
       │                                     │
       │  ... waiting for users ...          │
       │                                     │
       │                            ┌────────┤
       │                            │ User   │
       │                            │ sends  │
       │                            └────────┤
       │                                     │
       │  3. POST request with update        │
       │<────────────────────────────────────│
       │     [Telegram звонит вам!]          │
       │                                     │
       │  4. Process immediately             │
       ├─────────┐                           │
       │         │                           │
       │<────────┘                           │
       │                                     │
       │  5. HTTP 200 OK                     │
       │────────────────────────────────────>│
       │                                     │
       └─────────────────────────────────────┘
```

**Как работает:**
1. Вы говорите Telegram: "Шли все обновления на мой URL"
2. Telegram **сам вызывает ваш сервер** при новых сообщениях
3. Вы обрабатываете и **сразу отвечаете** (или позже через API)

**Преимущества:**
- ✅ Мгновенная доставка (0 задержки)
- ✅ Меньше нагрузки на сервер
- ✅ Масштабируется лучше
- ✅ Recommended by Telegram для production

**Недостатки:**
- ❌ Нужен **публичный домен** с HTTPS
- ❌ Нужен SSL сертификат (Let's Encrypt бесплатно)
- ❌ Сложнее настройка
- ❌ Не работает на localhost (нужен ngrok для теста)

### 📊 Сравнительная таблица

| Критерий | Long Polling | Webhook |
|----------|--------------|---------|
| Настройка | ⭐⭐⭐ Простая | ⭐ Сложная |
| Localhost разработка | ✅ Да | ❌ Нет (нужен ngrok) |
| Скорость отклика | ~1-2 сек | Мгновенно |
| Публичный домен | Не нужен | Обязателен |
| HTTPS | Не нужен | Обязателен |
| Нагрузка на сервер | Выше | Ниже |
| Масштабирование | До 1000 req/sec | До 100000 req/sec |
| Рекомендация Telegram | Для разработки | Для production |

**Рекомендация для вашего проекта:**
Начните с **Long Polling**, затем переходите на Webhook при деплое на сервер.

---

## 3. Подготовка и регистрация бота

### Шаг 1: Создание бота через BotFather

1. Откройте Telegram и найдите **@BotFather**
2. Отправьте команду `/newbot`
3. Введите имя бота: `Library Management Bot`
4. Введите username: `your_library_bot` (должен заканчиваться на `bot`)
5. BotFather выдаст **токен**:

```
Use this token to access the HTTP API:
1234567890:ABCdefGHIjklMNOpqrsTUVwxyz1234567

Keep your token secure and store it safely,
it can be used by anyone to control your bot.
```

### Шаг 2: Настройка команд (опционально)

Отправьте BotFather команду `/setcommands` и вставьте:

```
start - Начать работу с ботом
help - Показать справку
add_book - Добавить книгу
add_user - Добавить читателя
borrow_book - Выдать книгу
return_book - Вернуть книгу
find_book_by_author - Найти книги по автору
list_available_books - Список доступных книг
```

---

## 4. Работа с переменными окружения

### 🔐 Почему это важно?

**НИКОГДА** не храните токены в коде! Если вы закоммитите токен в Git:
- Любой может управлять вашим ботом
- Рассылать спам от вашего имени
- Удалить все данные

### Вариант 1: `.env` файл (Рекомендуется)

**Структура:**
```
OOP-BP/
├── src/
├── pom.xml
├── .env              ← создайте этот файл
└── .gitignore        ← добавьте .env сюда!
```

**Содержимое `.env`:**
```properties
# Telegram Bot Configuration
TELEGRAM_BOT_TOKEN=1234567890:ABCdefGHIjklMNOpqrsTUVwxyz1234567
TELEGRAM_BOT_USERNAME=your_library_bot

# Database Configuration (у вас уже есть)
DB_URL=jdbc:mysql://localhost:3306/library
DB_USER=root
DB_PASSWORD=your_password

# Application Settings
APP_MODE=IN_MEMORY
# APP_MODE=DATABASE
```

**Обязательно добавьте в `.gitignore`:**
```gitignore
# Environment variables
.env
.env.local
.env.*.local

# IntelliJ IDEA
.idea/
*.iml

# Maven
target/
```

### Вариант 2: System Environment Variables (Linux/Mac)

```bash
# Временно (до перезагрузки терминала)
export TELEGRAM_BOT_TOKEN="1234567890:ABCdefGHIjklMNOpqrsTUVwxyz1234567"

# Постоянно (добавьте в ~/.bashrc или ~/.zshrc)
echo 'export TELEGRAM_BOT_TOKEN="1234567890:..."' >> ~/.bashrc
source ~/.bashrc
```

### Вариант 3: IntelliJ IDEA Environment Variables

1. Run → Edit Configurations
2. В разделе "Environment variables" нажмите иконку папки
3. Добавьте: `TELEGRAM_BOT_TOKEN=...`

### 📦 Добавление библиотеки для работы с .env

Добавьте в `pom.xml`:

```xml
<dependencies>
    <!-- Существующие зависимости -->

    <!-- Для работы с .env файлами -->
    <dependency>
        <groupId>io.github.cdimascio</groupId>
        <artifactId>dotenv-java</artifactId>
        <version>3.0.0</version>
    </dependency>
</dependencies>
```

### 💻 Код для чтения .env

```java
import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("./")           // путь к .env
            .ignoreIfMissing()         // не падать если нет файла
            .load();

    public static String getBotToken() {
        String token = dotenv.get("TELEGRAM_BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException(
                "TELEGRAM_BOT_TOKEN не найден! " +
                "Создайте .env файл или установите переменную окружения"
            );
        }
        return token;
    }

    public static String getBotUsername() {
        return dotenv.get("TELEGRAM_BOT_USERNAME", "library_bot");
    }

    public static String getAppMode() {
        return dotenv.get("APP_MODE", "IN_MEMORY");
    }
}
```

### Альтернатива: System.getenv()

Если не хотите добавлять библиотеку, используйте встроенный метод:

```java
public class Config {
    public static String getBotToken() {
        String token = System.getenv("TELEGRAM_BOT_TOKEN");
        if (token == null) {
            throw new IllegalStateException(
                "Set TELEGRAM_BOT_TOKEN environment variable!"
            );
        }
        return token;
    }
}
```

---

## 5. Выбор библиотеки для Java

### 📚 Популярные библиотеки

#### 1. TelegramBots (Java) - **Рекомендую**

**Официальная библиотека от rubenlagus**

```xml
<dependency>
    <groupId>org.telegram</groupId>
    <artifactId>telegrambots</artifactId>
    <version>6.9.7.1</version>
</dependency>
```

**Преимущества:**
- ✅ Самая популярная (9k+ stars на GitHub)
- ✅ Активно поддерживается
- ✅ Поддержка Long Polling и Webhook
- ✅ Хорошая документация
- ✅ Spring Boot интеграция

#### 2. Telegram Bot API (Kotlin)

```xml
<dependency>
    <groupId>io.github.kotlin-telegram-bot</groupId>
    <artifactId>telegram</artifactId>
    <version>6.1.0</version>
</dependency>
```

Если переходите на Kotlin в будущем.

#### 3. JTelegramBot

Более легковесная альтернатива, но менее популярная.

---

## 6. Пошаговая миграция

### Этап 1: Установка зависимостей

Обновите `pom.xml`:

```xml
<dependencies>
    <!-- Существующие зависимости (Lombok, MySQL, HikariCP) -->

    <!-- Telegram Bot API -->
    <dependency>
        <groupId>org.telegram</groupId>
        <artifactId>telegrambots</artifactId>
        <version>6.9.7.1</version>
    </dependency>

    <!-- Для работы с .env -->
    <dependency>
        <groupId>io.github.cdimascio</groupId>
        <artifactId>dotenv-java</artifactId>
        <version>3.0.0</version>
    </dependency>
</dependencies>
```

### Этап 2: Создание Config класса

```java
package ru.lostfly.config;

import io.github.cdimascio.dotenv.Dotenv;

public class BotConfig {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static String getBotToken() {
        return dotenv.get("TELEGRAM_BOT_TOKEN");
    }

    public static String getBotUsername() {
        return dotenv.get("TELEGRAM_BOT_USERNAME");
    }
}
```

### Этап 3: Создание структуры Telegram бота

**Новая структура:**
```
src/main/java/ru/lostfly/
├── bot/
│   ├── TelegramBot.java              ← Основной класс бота
│   ├── handlers/
│   │   ├── CommandHandler.java       ← Обработка команд
│   │   └── MessageHandler.java       ← Обработка сообщений
│   └── keyboards/
│       └── KeyboardFactory.java      ← Клавиатуры для удобства
├── components/                        ← Существующие классы
├── config/
│   └── BotConfig.java                ← Конфигурация
└── Main.java                          ← Обновлённый main
```

### Этап 4: Маппинг команд

| Консольная команда | Telegram команда | Обработчик |
|--------------------|------------------|------------|
| `/add_book` | `/add_book` или кнопка | addBookHandler() |
| `/add_user` | `/add_user` | addUserHandler() |
| `/borrow_book` | `/borrow_book` | borrowBookHandler() |
| `/return_book` | `/return_book` | returnBookHandler() |
| `/find_book_by_author` | `/find_book_by_author` | findBookHandler() |
| `/list_available_books` | `/list_available_books` | listBooksHandler() |
| `/help` | `/help` или `/start` | helpHandler() |

---

## 7. Структура проекта

### Минимальная структура

```java
// 1. Main.java
public class Main {
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new LibraryBot());
    }
}

// 2. LibraryBot.java
public class LibraryBot extends TelegramLongPollingBot {

    private final RepositoryComponent repositoryComponent;
    private final ServiceComponent serviceComponent;

    @Override
    public String getBotUsername() {
        return BotConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return BotConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Обработка сообщений
    }
}
```

---

## 8. Примеры реализации

### Пример 1: Базовая структура бота

```java
package ru.lostfly.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.lostfly.config.BotConfig;
import ru.lostfly.components.repository.RepositoryComponent;
import ru.lostfly.components.service.ServiceComponent;

public class LibraryBot extends TelegramLongPollingBot {

    private final RepositoryComponent repositoryComponent;
    private final ServiceComponent serviceComponent;

    public LibraryBot() {
        // Инициализация как в вашем TgApiHandler.java
        this.repositoryComponent = new RepositoryComponent(
            RepositoryComponent.RepositoryMode.IN_MEMORY
        );
        this.serviceComponent = new ServiceComponent(repositoryComponent);
    }

    @Override
    public String getBotUsername() {
        return BotConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return BotConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Проверяем, что пришло текстовое сообщение
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        // Обработка команд
        switch (messageText) {
            case "/start", "/help" -> sendHelp(chatId);
            case "/list_available_books" -> listAvailableBooks(chatId);
            case "/add_book" -> startAddBookDialog(chatId);
            // ... другие команды
            default -> sendMessage(chatId, "Неизвестная команда. Используйте /help");
        }
    }

    // Метод для отправки сообщений
    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Справка
    private void sendHelp(Long chatId) {
        String helpText = """
            📚 *Система управления библиотекой*

            Доступные команды:
            /add_book - Добавить книгу
            /add_user - Добавить читателя
            /borrow_book - Выдать книгу
            /return_book - Вернуть книгу
            /find_book_by_author - Поиск по автору
            /list_available_books - Доступные книги
            /help - Эта справка
            """;

        sendMessage(chatId, helpText);
    }

    // Пример: список книг
    private void listAvailableBooks(Long chatId) {
        var books = repositoryComponent.getBookRepository()
            .findAvailableBooks();

        if (books.isEmpty()) {
            sendMessage(chatId, "📭 Нет доступных книг");
            return;
        }

        StringBuilder response = new StringBuilder("📚 *Доступные книги:*\n\n");
        for (var book : books) {
            response.append(String.format(
                "• %s - %s\n  ISBN: %s\n\n",
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn()
            ));
        }

        sendMessage(chatId, response.toString());
    }

    // Начало диалога добавления книги
    private void startAddBookDialog(Long chatId) {
        // Здесь нужна система состояний (см. пример 3)
        sendMessage(chatId, "📖 Введите ISBN книги:");
        // Сохранить состояние пользователя: "ожидает ISBN"
    }
}
```

### Пример 2: Система состояний (State Management)

Для многошаговых диалогов нужно хранить состояние пользователя:

```java
// UserSession.java
public class UserSession {
    private String state;  // "WAITING_ISBN", "WAITING_TITLE", etc.
    private Map<String, String> data = new HashMap<>();

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void putData(String key, String value) {
        data.put(key, value);
    }

    public String getData(String key) {
        return data.get(key);
    }

    public void clear() {
        state = null;
        data.clear();
    }
}

// SessionManager.java
public class SessionManager {
    private final Map<Long, UserSession> sessions = new HashMap<>();

    public UserSession getSession(Long chatId) {
        return sessions.computeIfAbsent(chatId, k -> new UserSession());
    }

    public void clearSession(Long chatId) {
        sessions.remove(chatId);
    }
}
```

**Использование в боте:**

```java
public class LibraryBot extends TelegramLongPollingBot {

    private final SessionManager sessionManager = new SessionManager();

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        UserSession session = sessionManager.getSession(chatId);

        // Если есть активное состояние - обработать его
        if (session.getState() != null) {
            handleState(chatId, text, session);
            return;
        }

        // Иначе обработать как команду
        handleCommand(chatId, text);
    }

    private void handleState(Long chatId, String text, UserSession session) {
        switch (session.getState()) {
            case "WAITING_ISBN" -> {
                if (text.length() < 2) {
                    sendMessage(chatId, "❌ ISBN должен быть длиннее 2 символов");
                    return;
                }
                session.putData("isbn", text);
                session.setState("WAITING_TITLE");
                sendMessage(chatId, "📖 Введите название книги:");
            }

            case "WAITING_TITLE" -> {
                session.putData("title", text);
                session.setState("WAITING_AUTHOR");
                sendMessage(chatId, "✍️ Введите автора:");
            }

            case "WAITING_AUTHOR" -> {
                String isbn = session.getData("isbn");
                String title = session.getData("title");
                String author = text;

                // Создаём книгу
                Book book = new Book(isbn, title, author);
                repositoryComponent.getBookRepository().save(book);

                sendMessage(chatId, "✅ Книга '" + title + "' добавлена!");
                session.clear();
            }

            // Аналогично для других состояний...
        }
    }

    private void handleCommand(Long chatId, String command) {
        switch (command) {
            case "/add_book" -> {
                UserSession session = sessionManager.getSession(chatId);
                session.setState("WAITING_ISBN");
                sendMessage(chatId, "📖 Введите ISBN книги:");
            }
            // ... другие команды
        }
    }
}
```

### Пример 3: Inline клавиатуры

Для удобства пользователей добавьте кнопки:

```java
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class KeyboardFactory {

    public static InlineKeyboardMarkup getMainMenu() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Первая строка
        keyboard.add(List.of(
            createButton("📖 Добавить книгу", "/add_book"),
            createButton("👤 Добавить читателя", "/add_user")
        ));

        // Вторая строка
        keyboard.add(List.of(
            createButton("📚 Выдать книгу", "/borrow_book"),
            createButton("🔄 Вернуть книгу", "/return_book")
        ));

        // Третья строка
        keyboard.add(List.of(
            createButton("🔍 Поиск по автору", "/find_book_by_author"),
            createButton("📋 Доступные книги", "/list_available_books")
        ));

        markup.setKeyboard(keyboard);
        return markup;
    }

    private static InlineKeyboardButton createButton(String text, String callback) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callback);
        return button;
    }
}

// Использование в боте:
private void sendHelp(Long chatId) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText("📚 Выберите действие:");
    message.setReplyMarkup(KeyboardFactory.getMainMenu());

    try {
        execute(message);
    } catch (TelegramApiException e) {
        e.printStackTrace();
    }
}

// Обработка нажатий на кнопки:
@Override
public void onUpdateReceived(Update update) {
    if (update.hasCallbackQuery()) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        handleCommand(chatId, callbackData);
        return;
    }

    // ... обработка обычных сообщений
}
```

### Пример 4: Обновлённый Main.java

```java
package ru.lostfly;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.lostfly.bot.LibraryBot;

public class Main {

    public static void main(String[] args) {
        System.out.println("🤖 Telegram Bot Starting...");

        try {
            // Создаём Telegram Bots API
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            // Регистрируем нашего бота
            LibraryBot bot = new LibraryBot();
            botsApi.registerBot(bot);

            System.out.println("✅ Bot started successfully!");
            System.out.println("📝 Bot username: " + bot.getBotUsername());

        } catch (TelegramApiException e) {
            System.err.println("❌ Failed to start bot: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

---

## 🎯 Рекомендуемый план действий

### Фаза 1: Подготовка (30 минут)
1. ✅ Создать бота через @BotFather
2. ✅ Создать `.env` файл с токеном
3. ✅ Добавить `.env` в `.gitignore`
4. ✅ Обновить `pom.xml` с зависимостями
5. ✅ Создать `BotConfig.java`

### Фаза 2: Базовая реализация (2 часа)
1. ✅ Создать `LibraryBot.java` с Long Polling
2. ✅ Реализовать `/start` и `/help`
3. ✅ Реализовать `/list_available_books` (самая простая команда)
4. ✅ Протестировать базовую работу

### Фаза 3: Диалоги и состояния (3 часа)
1. ✅ Создать `SessionManager` и `UserSession`
2. ✅ Реализовать `/add_book` с диалогом
3. ✅ Реализовать остальные многошаговые команды
4. ✅ Добавить обработку ошибок

### Фаза 4: Улучшения (2 часа)
1. ✅ Добавить inline клавиатуры
2. ✅ Улучшить форматирование сообщений (Markdown)
3. ✅ Добавить эмодзи для удобства
4. ✅ Обработка неизвестных команд

### Фаза 5: Production (опционально)
1. ✅ Перейти на Webhook
2. ✅ Настроить HTTPS и домен
3. ✅ Деплой на сервер (Heroku, Railway, VPS)

---

## 📚 Полезные ресурсы

### Документация
- [Telegram Bot API](https://core.telegram.org/bots/api) - официальная документация
- [TelegramBots Java Library](https://github.com/rubenlagus/TelegramBots) - GitHub репозиторий
- [TelegramBots Wiki](https://github.com/rubenlagus/TelegramBots/wiki) - примеры и гайды

### Инструменты
- [BotFather](https://t.me/BotFather) - создание ботов
- [ngrok](https://ngrok.com/) - тестирование webhook на localhost
- [Postman](https://www.postman.com/) - тестирование Telegram API

---

## ❓ FAQ

**Q: Можно ли запустить бота на localhost?**
A: Да, с Long Polling работает из коробки. Webhook требует публичный домен.

**Q: Как хранить данные между перезапусками?**
A: Используйте ваш DATABASE режим с MySQL или добавьте сериализацию в файл.

**Q: Как обрабатывать множество пользователей?**
A: Каждый пользователь имеет уникальный `chatId`. Используйте `SessionManager` для хранения состояний.

**Q: Нужно ли мне изменять LibraryService?**
A: Нет! Ваша бизнес-логика остаётся той же. Меняется только слой взаимодействия (UI).

**Q: Как деплоить бота?**
A: Railway, Heroku, AWS, VPS - любой сервер с Java 17+.

---

Этот гайд даёт вам полное представление о переходе на Telegram бота. Начните с простого Long Polling бота, затем постепенно добавляйте функциональность!