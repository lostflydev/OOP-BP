#!/bin/bash

# Скрипт для подключения к MySQL как root пользователь

echo "Подключение к MySQL как ROOT..."
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
echo "  Username: root"
echo "  Password: rootpassword"
echo ""
echo "Для выхода введите: exit"
echo "========================================"
echo ""

# Подключаемся к MySQL как root
docker exec -it library_mysql mysql -u root -prootpassword library_db