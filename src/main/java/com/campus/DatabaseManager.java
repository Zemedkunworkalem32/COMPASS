package com.campus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/compass?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "password";

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() throws SQLException {
        String url = System.getProperty("db.url", System.getenv().getOrDefault("DB_URL", DEFAULT_URL));
        String user = System.getProperty("db.user", System.getenv().getOrDefault("DB_USER", DEFAULT_USER));
        String password = System.getProperty("db.password", System.getenv().getOrDefault("DB_PASSWORD", DEFAULT_PASSWORD));
        this.connection = DriverManager.getConnection(url, user, password);
    }

    public static synchronized DatabaseManager getInstance() throws SQLException {
        if (instance == null || instance.getConnection().isClosed()) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}

