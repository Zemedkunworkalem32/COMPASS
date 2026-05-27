package com.compass.db;

import com.compass.util.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton JDBC connection manager.
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;

    private final String url;
    private final String user;
    private final String password;
    private final String driver;

    private DatabaseManager() {
        this.url = AppConfig.get("db.url", "jdbc:mysql://localhost:3306/compass_db");
        this.user = AppConfig.get("db.user", "root");
        this.password = AppConfig.get("db.password", "");
        this.driver = AppConfig.get("db.driver", "com.mysql.cj.jdbc.Driver");
        initializeDriver();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDriver() {
        try {
            Class.forName(driver);
            logger.info("JDBC driver loaded");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load JDBC driver", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2);
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }

    public void setUrl(String url) { throw new UnsupportedOperationException("Use application.properties"); }
    public void setUser(String user) { throw new UnsupportedOperationException("Use application.properties"); }
    public void setPassword(String password) { throw new UnsupportedOperationException("Use application.properties"); }
}
