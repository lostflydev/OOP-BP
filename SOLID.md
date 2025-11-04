### SOLID

1. **Single Responsibility Principle (SRP)**
   Каждый класс должен иметь только одну причину для изменения.

```java
// ✓ ХОРОШО: Каждый класс делает одно дело

// Book - представляет книгу (модель данных)
public class Book extends LibraryItem {
    // Только данные и поведение КНИГИ
}

// BookRepository - хранение и поиск книг
public interface BookRepository {
    void save(Book book);
    Book findByIsbn(String isbn);
    // Только операции с ХРАНЕНИЕМ
}

// LibraryService - бизнес-логика библиотеки
public interface LibraryService {
    String borrowBook(String isbn, Reader reader);
    // Только БИЗНЕС-ПРАВИЛА
}

// App - взаимодействие с пользователем
public class App {
    public void start() {
        // Только ИНТЕРФЕЙС пользователя
    }
}
```
**✗ Анти-пример (ПЛОХО):**

```java
// ПЛОХО: Нарушение SRP - класс делает ВСЁ
public class Library {
    private List<Book> books;  // Хранение
    
    public void save(Book book) { }  // Работа с данными
    
    public void borrowBook() { }  // Бизнес-логика
    
    public void printMenu() { }  // UI
    
    public void connectToDatabase() { }  // Инфраструктура
    
    // ПРИЧИН ДЛЯ ИЗМЕНЕНИЯ: 4!
    // - Изменили формат хранения → меняем класс
    // - Изменили правила выдачи → меняем класс
    // - Изменили UI → меняем класс
    // - Изменили БД → меняем класс
}
```

2. **Open/Closed Principle (OCP)**
   Программные сущности должны быть открыты для расширения, но закрыты для изменения.


```java
// ✓ ХОРОШО: Можем добавить новую реализацию БЕЗ изменения существующего кода

// Интерфейс (закрыт для модификации)
public interface BookRepository {
    void save(Book book);
    Book findByIsbn(String isbn);
}

// Существующая реализация
public class BookRepositoryImpl implements BookRepository {
    // Реализация в памяти
}

// ✓ РАСШИРЕНИЕ: Добавляем новую реализацию - НЕ ТРОГАЯ старую!
public class BookRepositoryDatabase implements BookRepository {
    // Реализация с БД
}

public class BookRepositoryFile implements BookRepository {
    // Реализация с файлами
}

public class BookRepositoryLogging implements BookRepository {
    // Декоратор с логированием
}

// Код использующий BookRepository НЕ ИЗМЕНЯЕТСЯ!
```

3.  Liskov Substitution Principle
    Объекты подтипов должны быть заменяемы объектами базового типа без нарушения корректности программы.

```java
// ✓ ХОРОШО: Можем подставить любую реализацию
public class LibraryServiceImpl implements LibraryService {
    private final BookRepository bookRepository;
    
    public LibraryServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    public String borrowBook(String isbn, Reader reader) {
        // Работает с ЛЮБОЙ реализацией BookRepository!
        Book book = bookRepository.findByIsbn(isbn);
        // ...
    }
}

// Все эти варианты работают ОДИНАКОВО КОРРЕКТНО:
LibraryService service1 = new LibraryServiceImpl(new BookRepositoryImpl());
LibraryService service2 = new LibraryServiceImpl(new BookRepositoryDatabase());
LibraryService service3 = new LibraryServiceImpl(new BookRepositoryCached(...));
LibraryService service4 = new LibraryServiceImpl(new BookRepositoryLogging(...));
```

4. Interface Segregation Principle 
   Клиенты не должны зависеть от интерфейсов, которые они не используют.

```java
interface LibraryRepo {
    
    public void borrowBook() { }  // Бизнес-логика
}

interface LibraryDPRepo {

    public void connectToDatabase() { }  // Инфраструктура
}

class LibImpl implements LibraryRepo with LibraryDPRepo {
    
}

class LibService {
    
    private LibraryRepo library;
    
    
    Book borrowBook(String isbn) {
        library.borrowBook();
    }
}

class LibDBService {

    private LibraryDPRepo library;

    library.connectToDatabase();  // Инфраструктура
}
```

5. Dependency Inversion Principle
   Модули верхнего уровня не должны зависеть от модулей нижнего уровня. 
   Оба типа модулей должны зависеть от абстракций.
   **Пример в коде:**


````java
// ✓ ХОРОШО: Зависимость от интерфейса (абстракции)

// Высокий уровень (бизнес-логика)
public class LibraryServiceImpl implements LibraryService {
    
    // Зависит от ИНТЕРФЕЙСА, а не конкретного класса
    private final BookRepository bookRepository;  // ← Абстракция
    
    public LibraryServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
}

// Низкий уровень (детали реализации)
public class BookRepositoryImpl implements BookRepository {
    // Реализует абстракцию
}

// Зависимости: LibraryServiceImpl → BookRepository ← BookRepositoryImpl
//                                       ↑
//                                  (Абстракция)
```

**Визуализация:**
```
БЕЗ DIP (ПЛОХО):                    С DIP (ХОРОШО):
┌──────────────┐                    ┌──────────────┐
│ LibraryService│                   │LibraryService│
└───────┬──────┘                    └──────┬───────┘
        │ зависит                          │ зависит
        ↓                                   ↓
┌──────────────────┐              ┌─────────────────┐
│BookRepositoryImpl│              │ BookRepository  │ ← Интерфейс
└──────────────────┘              │   (interface)   │
                                  └────────┬────────┘
                                           ↑ реализует
                                  ┌────────┴─────────┐
                                  │BookRepositoryImpl│
                                  └──────────────────┘
````