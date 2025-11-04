package ru.lostfly.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class DatabaseConnection {
    private static HikariDataSource dataSource;

    // Параметры подключения к БД
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String DB_USER = "library_user";
    private static final String DB_PASSWORD = "library_pass";

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DB_URL);
            config.setUsername(DB_USER);
            config.setPassword(DB_PASSWORD);

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
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            log.error("Database connection test failed", e);
            return false;
        }
    }
}