package com.compass.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final String URL = AppConfig.get("db.url", "jdbc:mysql://localhost:3306/compass_db");
    private static final String USER = AppConfig.get("db.user", "root");
    private static final String PASSWORD = AppConfig.get("db.password", "");

    static {
        try {
            Class.forName(AppConfig.get("db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found", e);
        }
    }

    private Database() {}

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean initialize() {
        try (Connection connection = connect()) {
            createTables(connection);
            migrateOldProjectData(connection);
            seedCampusLocations(connection);
            return true;
        } catch (SQLException e) {
            if (isConnectionProblem(e)) {
                return false;
            }
            throw new RuntimeException("Could not initialize database schema", e);
        }
    }

    public static boolean isAvailable() {
        try (Connection connection = connect()) {
            return connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private static void createTables(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS users (
                    user_id INT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    full_name VARCHAR(100) NOT NULL,
                    role ENUM('STUDENT', 'ADMIN', 'SYSTEM_ADMIN') NOT NULL DEFAULT 'STUDENT',
                    is_active BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS complaints (
                    complaint_id INT PRIMARY KEY AUTO_INCREMENT,
                    student_id INT NOT NULL,
                    title VARCHAR(200) NOT NULL,
                    description TEXT NOT NULL,
                    category ENUM('Dorm', 'Library', 'Cafe', 'Other') NOT NULL,
                    status ENUM('Pending', 'In Progress', 'Resolved') NOT NULL DEFAULT 'Pending',
                    attachment_path VARCHAR(500),
                    admin_note TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    resolved_at TIMESTAMP NULL,
                    FOREIGN KEY (student_id) REFERENCES users(user_id)
                )
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS campus_locations (
                    location_id INT PRIMARY KEY AUTO_INCREMENT,
                    location_name VARCHAR(100) UNIQUE NOT NULL,
                    latitude DECIMAL(10, 6) NOT NULL,
                    longitude DECIMAL(10, 6) NOT NULL
                )
                """);
    }

    private static void migrateOldProjectData(Connection connection) throws SQLException {
        addColumnIfMissing(connection, "complaints", "admin_note", "TEXT");
        addColumnIfMissing(connection, "complaints", "resolved_at", "TIMESTAMP NULL");
        addColumnIfMissing(connection, "complaints", "attachment_path", "VARCHAR(500)");

        if (columnExists(connection, "complaints", "resolution_notes")) {
            execute(connection, "UPDATE complaints SET admin_note = resolution_notes WHERE admin_note IS NULL");
        }

        if (columnExists(connection, "users", "role")) {
            execute(connection, "ALTER TABLE users MODIFY role ENUM('STUDENT','ADMIN','SYSTEM_ADMIN','DEPARTMENT_STAFF') NOT NULL DEFAULT 'STUDENT'");
            execute(connection, "UPDATE users SET role='ADMIN' WHERE role='DEPARTMENT_STAFF'");
            execute(connection, "ALTER TABLE users MODIFY role ENUM('STUDENT','ADMIN','SYSTEM_ADMIN') NOT NULL DEFAULT 'STUDENT'");
        }

        if (columnExists(connection, "complaints", "status")) {
            execute(connection, "ALTER TABLE complaints MODIFY status ENUM('Pending','In Progress','Resolved','SUBMITTED','UNDER_REVIEW','ASSIGNED','IN_PROGRESS','CLOSED','REJECTED') NOT NULL DEFAULT 'Pending'");
            execute(connection, "UPDATE complaints SET status='Pending' WHERE status IN ('SUBMITTED','UNDER_REVIEW','ASSIGNED','REJECTED','CLOSED')");
            execute(connection, "UPDATE complaints SET status='In Progress' WHERE status='IN_PROGRESS'");
            execute(connection, "UPDATE complaints SET status='Resolved' WHERE status='RESOLVED'");
            execute(connection, "ALTER TABLE complaints MODIFY status ENUM('Pending','In Progress','Resolved') NOT NULL DEFAULT 'Pending'");
        }

        if (columnExists(connection, "complaints", "category")) {
            execute(connection, "ALTER TABLE complaints MODIFY category VARCHAR(50) NOT NULL");
            execute(connection, "UPDATE complaints SET category='Dorm' WHERE LOWER(category) LIKE '%dorm%'");
            execute(connection, "UPDATE complaints SET category='Library' WHERE LOWER(category) LIKE '%library%'");
            execute(connection, "UPDATE complaints SET category='Cafe' WHERE LOWER(category) LIKE '%cafe%' OR LOWER(category) LIKE '%cafeteria%'");
            execute(connection, "UPDATE complaints SET category='Other' WHERE category NOT IN ('Dorm','Library','Cafe')");
            execute(connection, "ALTER TABLE complaints MODIFY category ENUM('Dorm','Library','Cafe','Other') NOT NULL");
        }
    }

    private static void seedCampusLocations(Connection connection) throws SQLException {
        execute(connection, """
                INSERT IGNORE INTO campus_locations (location_id, location_name, latitude, longitude) VALUES
                    (1, 'Main Gate', 8.885100, 38.809900),
                    (2, 'Engineering Library', 8.886100, 38.810900),
                    (3, 'Digital Library', 8.886600, 38.811500),
                    (4, 'Dorm', 8.887300, 38.812000),
                    (5, 'Cafe', 8.885800, 38.812600),
                    (6, 'Administration Building', 8.884900, 38.811200)
                """);
    }

    private static void addColumnIfMissing(Connection connection, String table, String column, String definition)
            throws SQLException {
        if (!columnExists(connection, table, column)) {
            execute(connection, "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    private static boolean columnExists(Connection connection, String table, String column) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet rs = metadata.getColumns(connection.getCatalog(), null, table, column)) {
            return rs.next();
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static boolean isConnectionProblem(SQLException e) {
        String state = e.getSQLState();
        return state != null && state.startsWith("08");
    }
}
