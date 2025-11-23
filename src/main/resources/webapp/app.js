// Инициализация Telegram WebApp
const tg = window.Telegram?.WebApp || {
    // Заглушка для локального тестирования в браузере
    expand: () => {},
    enableClosingConfirmation: () => {},
    themeParams: {}
};

// Проверяем, запущено ли в Telegram
const isInTelegram = !!window.Telegram?.WebApp;
if (isInTelegram) {
    tg.expand();
    tg.enableClosingConfirmation();
} else {
    console.log('⚠️ Запущено вне Telegram - используется режим отладки');
}

// API Base URL - замените на ваш сервер
const API_BASE_URL = 'http://localhost:8080/api';

// Элементы DOM
const tabs = document.querySelectorAll('.tab');
const tabContents = document.querySelectorAll('.tab-content');
const notification = document.getElementById('notification');

// Переключение вкладок
tabs.forEach(tab => {
    tab.addEventListener('click', () => {
        const tabName = tab.dataset.tab;

        tabs.forEach(t => t.classList.remove('active'));
        tabContents.forEach(tc => tc.classList.remove('active'));

        tab.classList.add('active');
        document.getElementById(`${tabName}-tab`).classList.add('active');

        // Загружаем данные при переключении на вкладку
        if (tabName === 'books') {
            loadAvailableBooks();
        } else if (tabName === 'readers') {
            loadReaders();
        }
    });
});

// Показать уведомление
function showNotification(message, type = 'success') {
    notification.textContent = message;
    notification.className = `notification ${type}`;

    setTimeout(() => {
        notification.classList.add('hidden');
    }, 3000);
}

// API запросы
async function apiRequest(endpoint, options = {}) {
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('API Error:', error);
        showNotification('Ошибка соединения с сервером', 'error');
        throw error;
    }
}

// Загрузка доступных книг
async function loadAvailableBooks() {
    const container = document.getElementById('available-books');
    container.innerHTML = '<div class="loading">Загрузка...</div>';

    try {
        const books = await apiRequest('/books/available');

        if (books.length === 0) {
            container.innerHTML = '<div class="empty-state"><p>Нет доступных книг</p></div>';
            return;
        }

        container.innerHTML = books.map(book => `
            <div class="book-item">
                <h3>${book.title}</h3>
                <p>Автор: ${book.author}</p>
                <p>ISBN: ${book.isbn}</p>
                <span class="book-status ${book.available ? 'status-available' : 'status-borrowed'}">
                    ${book.available ? '✓ Доступна' : '✗ Выдана'}
                </span>
            </div>
        `).join('');
    } catch (error) {
        container.innerHTML = '<div class="empty-state"><p>Ошибка загрузки книг</p></div>';
    }
}

// Загрузка читателей
async function loadReaders() {
    const container = document.getElementById('readers-list');
    container.innerHTML = '<div class="loading">Загрузка...</div>';

    try {
        const readers = await apiRequest('/readers');

        if (readers.length === 0) {
            container.innerHTML = '<div class="empty-state"><p>Нет зарегистрированных читателей</p></div>';
            return;
        }

        container.innerHTML = readers.map(reader => `
            <div class="reader-item">
                <h3>${reader.name}</h3>
                <p>ID: ${reader.id}</p>
                <p>Книг на руках: ${reader.borrowedBooksCount || 0}</p>
            </div>
        `).join('');
    } catch (error) {
        container.innerHTML = '<div class="empty-state"><p>Ошибка загрузки читателей</p></div>';
    }
}

// Добавление книги
document.getElementById('add-book-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    const isbn = document.getElementById('book-isbn').value.trim();
    const title = document.getElementById('book-title').value.trim();
    const author = document.getElementById('book-author').value.trim();

    try {
        await apiRequest('/books', {
            method: 'POST',
            body: JSON.stringify({ isbn, title, author }),
        });

        showNotification('Книга успешно добавлена!', 'success');
        e.target.reset();
        loadAvailableBooks();
    } catch (error) {
        showNotification('Ошибка при добавлении книги', 'error');
    }
});

// Поиск книг по автору
document.getElementById('search-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    const author = document.getElementById('search-author').value.trim();
    const resultsContainer = document.getElementById('search-results');

    try {
        const books = await apiRequest(`/books/search?author=${encodeURIComponent(author)}`);

        if (books.length === 0) {
            resultsContainer.innerHTML = '<div class="empty-state"><p>Книги не найдены</p></div>';
            return;
        }

        resultsContainer.innerHTML = books.map(book => `
            <div class="book-item">
                <h3>${book.title}</h3>
                <p>Автор: ${book.author}</p>
                <p>ISBN: ${book.isbn}</p>
                <span class="book-status ${book.available ? 'status-available' : 'status-borrowed'}">
                    ${book.available ? '✓ Доступна' : '✗ Выдана'}
                </span>
            </div>
        `).join('');
    } catch (error) {
        resultsContainer.innerHTML = '<div class="empty-state"><p>Ошибка поиска</p></div>';
    }
});

// Добавление читателя
document.getElementById('add-reader-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    const id = document.getElementById('reader-id').value.trim();
    const name = document.getElementById('reader-name').value.trim();

    try {
        await apiRequest('/readers', {
            method: 'POST',
            body: JSON.stringify({ id, name }),
        });

        showNotification('Читатель успешно добавлен!', 'success');
        e.target.reset();
        loadReaders();
    } catch (error) {
        showNotification('Ошибка при добавлении читателя', 'error');
    }
});

// Выдача книги
document.getElementById('borrow-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    const isbn = document.getElementById('borrow-isbn').value.trim();
    const readerId = document.getElementById('borrow-reader-id').value.trim();

    try {
        await apiRequest('/books/borrow', {
            method: 'POST',
            body: JSON.stringify({ isbn, readerId }),
        });

        showNotification('Книга успешно выдана!', 'success');
        e.target.reset();
    } catch (error) {
        showNotification('Ошибка при выдаче книги', 'error');
    }
});

// Возврат книги
document.getElementById('return-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    const isbn = document.getElementById('return-isbn').value.trim();
    const readerId = document.getElementById('return-reader-id').value.trim();

    try {
        await apiRequest('/books/return', {
            method: 'POST',
            body: JSON.stringify({ isbn, readerId }),
        });

        showNotification('Книга успешно возвращена!', 'success');
        e.target.reset();
    } catch (error) {
        showNotification('Ошибка при возврате книги', 'error');
    }
});

// Загружаем данные при старте
loadAvailableBooks();