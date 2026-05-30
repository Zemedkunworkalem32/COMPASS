DROP DATABASE IF EXISTS compass_db;
CREATE DATABASE compass_db;
USE compass_db;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('STUDENT', 'ADMIN', 'SYSTEM_ADMIN') NOT NULL DEFAULT 'STUDENT',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE complaints (
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
);

CREATE TABLE campus_locations (
    location_id INT PRIMARY KEY AUTO_INCREMENT,
    location_name VARCHAR(100) UNIQUE NOT NULL,
    latitude DECIMAL(10, 6) NOT NULL,
    longitude DECIMAL(10, 6) NOT NULL
);

INSERT INTO users (username, email, full_name, password_hash, role) VALUES
('sysadmin', 'sysadmin@aastu.edu.et', 'System Admin', '$2a$12$PjArFa4JxcyaxRqEq51zderbBsRygiDeQLCsrqTLhVLEInwRcJz/q', 'SYSTEM_ADMIN'),
('admin', 'admin@aastu.edu.et', 'Complaint Admin', '$2a$12$PjArFa4JxcyaxRqEq51zderbBsRygiDeQLCsrqTLhVLEInwRcJz/q', 'ADMIN'),
('student1', 'student1@aastu.edu.et', 'Demo Student', '$2a$12$T1n0FydQ.9NWLR4Q5P7mtePB9UBZhtdkIbVbdQaJSPRNEegxDXgl.', 'STUDENT'),
('student2', 'student2@aastu.edu.et', 'Second Student', '$2a$12$T1n0FydQ.9NWLR4Q5P7mtePB9UBZhtdkIbVbdQaJSPRNEegxDXgl.', 'STUDENT');

INSERT INTO campus_locations (location_id, location_name, latitude, longitude) VALUES
(1, 'Main Gate', 8.885100, 38.809900),
(2, 'Engineering Library', 8.886100, 38.810900),
(3, 'Digital Library', 8.886600, 38.811500),
(4, 'Dorm', 8.887300, 38.812000),
(5, 'Cafe', 8.885800, 38.812600),
(6, 'Administration Building', 8.884900, 38.811200);

INSERT INTO complaints (student_id, title, description, category, status, admin_note, created_at) VALUES
(3, 'Dorm water problem', 'The water supply in the dorm has been interrupted since yesterday evening.', 'Dorm', 'Pending', '', '2026-05-28 09:30:00'),
(3, 'Engineering library computers', 'Several computers in the Engineering Library are not turning on.', 'Library', 'In Progress', 'Admin is checking with the library team.', '2026-05-29 11:10:00'),
(4, 'Cafe queue delay', 'The cafe queue is very slow during lunch time and students miss afternoon classes.', 'Cafe', 'Resolved', 'Resolved after adding one more serving line.', '2026-05-30 08:45:00'),
(4, 'Other campus concern', 'There is a broken bench near the Administration Building.', 'Other', 'Pending', '', '2026-05-30 10:15:00');
