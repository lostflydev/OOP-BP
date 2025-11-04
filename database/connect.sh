#!/bin/bash

# Скрипт для подключения к MySQL в Docker контейнере

echo "Подключение к MySQL базе данных library_db..."
echo ""

# Проверяем, запущен ли контейнер
if ! docker ps | grep -q library_mysql; then
    echo "Ошибка: Контейнер library_mysql не запущен!"
    echo "Запустите базу данных командой: docker-compose up -d"
    exit 1
fi

echo "Параметры подключения:"
echo "  Host: localhost"
echo "  Database: library_db"
echo "  Username: library_user"
echo "  Password: library_pass"
echo ""
echo "Для выхода введите: exit"
echo "========================================"
echo ""

# Подключаемся к MySQL
docker exec -it library_mysql mysql -u library_user -plibrary_pass library_db