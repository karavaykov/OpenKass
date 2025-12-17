package Kass;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

// Добавлен импорт для SLF4J
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CashierDB {
    // Инициализация логгера
    private static final Logger logger = LoggerFactory.getLogger(CashierDB.class);

    private Connection connection;
    private String dbPath;

    public CashierDB(String dbName) throws SQLException {
        this.dbPath = dbName + ".db";
        initializeDatabase();
    }

    private void initializeDatabase() throws SQLException {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch (SQLException e) {
            // Используем SLF4J для логирования ошибки
            logger.error("Ошибка при подключении к базе данных: {}", e.getMessage(), e);
            throw e;
        }
        createTables();
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Таблица Users (пользователи)
            String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS Users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        role TEXT NOT NULL
                    );
                    """;
            stmt.execute(createUsersTable);

            // Таблица Checks (фискальные чеки)
            String createChecksTable = """
                    CREATE TABLE IF NOT EXISTS Checks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        checkNumber TEXT NOT NULL UNIQUE,
                        date TEXT NOT NULL,
                        totalAmount REAL NOT NULL,
                        cashierId INTEGER NOT NULL,
                        FOREIGN KEY(cashierId) REFERENCES Users(id)
                    );
                    """;
            stmt.execute(createChecksTable);

            // Таблица Goods (фискальные чеки)
            String createGoodsTable = """
                    CREATE TABLE IF NOT EXISTS Goods (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        GoodNumber TEXT NOT NULL UNIQUE,
                        GoodName TEXT NOT NULL,
                        VAT REAL NOT NULL,
                        Country TEXT NOT NULL,
                        Marked BOOLEAN NOT NULL,
                        UnitName TEXT NOT NULL,
                        UnitCode TEXT NOT NULL,
                        Amount REAL NOT NULL,
                        Price REAL NOT NULL,
                        PromotionPrice REAL NOT NULL
                    
                    );
                    """;
            stmt.execute(createGoodsTable);

            logger.info("Таблицы Users и Checks успешно созданы или уже существуют.");
        } catch (SQLException e) {
            logger.error("Ошибка при создании таблиц: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("Ошибка при закрытии соединения: {}", e.getMessage(), e);
        }
        
    }





}