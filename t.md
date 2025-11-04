| Аспект                         | Интерфейс                       | Абстрактный класс               |
| ------------------------------ | ------------------------------- | ------------------------------- |
| **Множественное наследование** | ✓ Да (implements несколько)     | ✗ Нет (extends только один)     |
| **Поля**                       | Только константы (static final) | Любые поля (private, protected) |
| **Конструктор**                | ✗ Нет                           | ✓ Да                            | |
| **Когда использовать**         | "Умеет делать" (контракт)       | "Является" (базовая реализация) |


**Интерфейс** = "Что умеет делать объект" (контракт, способность)

- Borrowable - "можно взять"
- Comparable - "можно сравнивать"
- Serializable - "можно сериализовать"

**Абстрактный класс** = "Что такое объект" (базовая реализация, общие черты)

- Animal → Dog, Cat (общие поля: age, name; общие методы: eat(), sleep())
- LibraryItem → Book, Magazine (общие поля: title, id)

-----

    public class Box<T> {
        private T value;

        public void set(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }
    }

    public class Calculator {
        public int add(int a, int b) {
            return a + b;
        }

        public double add(double a, double b) {
            return a + b;
        }

        public String add(String a, String b) {
            return a + b;
        }

        public String add(String a) {
            return a + a;
        }
    }

    public void main(String[] args) {


// Полиморфизм

// 1 Параметрический полиморфизм (Generics)

// Использование:
Box<String> stringBox = new Box<>();
stringBox.set("Привет");
String s = stringBox.get();

        Box<Integer> intBox = new Box<>();
        intBox.set(42);
        Integer i = intBox.get();



// Ad-hoc полиморфизм  (перегрузка методов)


// Использование:
Calculator calc = new Calculator();
int sum1 = calc.add(1, 2);           // вызовет int версию
double sum2 = calc.add(1.5, 2.3);    // вызовет double версию
String sum3 = calc.add("Hello", "World"); // вызовет String версию



// Полиморфизм подтипов

        public void processItem(Borrowable item) {
            if (item.isAvailable()) {
                item.borrow();  // Вызовется правильная реализация!
                System.out.println("Предмет выдан");
            }
        }

// Можем передать любой Borrowable объект
processItem(new Book("123", "Clean Code", "Martin"));
processItem(new Magazine("1234-5678", "Java Magazine", 42));

