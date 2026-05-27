-- Campus Complaint Management System Database Schema

CREATE DATABASE IF NOT EXISTS compass_db;
USE compass_db;

CREATE TABLE IF NOT EXISTS departments (
    department_id INT PRIMARY KEY AUTO_INCREMENT,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    email VARCHAR(100),
    phone VARCHAR(20),
    response_time_hours INT DEFAULT 24,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('STUDENT', 'ADMIN', 'DEPARTMENT_STAFF') NOT NULL,
    department_id INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

CREATE TABLE IF NOT EXISTS campus_locations (
    location_id INT PRIMARY KEY AUTO_INCREMENT,
    location_name VARCHAR(100) NOT NULL UNIQUE,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    building_code VARCHAR(20),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS campus_edges (
    edge_id INT PRIMARY KEY AUTO_INCREMENT,
    from_location_id INT NOT NULL,
    to_location_id INT NOT NULL,
    distance_meters DOUBLE NOT NULL,
    FOREIGN KEY (from_location_id) REFERENCES campus_locations(location_id),
    FOREIGN KEY (to_location_id) REFERENCES campus_locations(location_id),
    UNIQUE KEY unique_edge (from_location_id, to_location_id)
);

CREATE TABLE IF NOT EXISTS complaints (
    complaint_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    status ENUM('SUBMITTED', 'UNDER_REVIEW', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'TRANSFERRED') DEFAULT 'SUBMITTED',
    location_id INT,
    assigned_department_id INT,
    attachment_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    resolution_notes TEXT,
    FOREIGN KEY (student_id) REFERENCES users(user_id),
    FOREIGN KEY (location_id) REFERENCES campus_locations(location_id),
    FOREIGN KEY (assigned_department_id) REFERENCES departments(department_id)
);

CREATE TABLE IF NOT EXISTS complaint_transfers (
    transfer_id INT PRIMARY KEY AUTO_INCREMENT,
    complaint_id INT NOT NULL,
    from_department_id INT,
    to_department_id INT NOT NULL,
    reason VARCHAR(255),
    transferred_by INT NOT NULL,
    transferred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaints(complaint_id),
    FOREIGN KEY (from_department_id) REFERENCES departments(department_id),
    FOREIGN KEY (to_department_id) REFERENCES departments(department_id),
    FOREIGN KEY (transferred_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS attachments (
    attachment_id INT PRIMARY KEY AUTO_INCREMENT,
    complaint_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    uploaded_by INT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaints(complaint_id),
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id)
);

CREATE INDEX idx_user_department ON users(department_id);
CREATE INDEX idx_complaint_student ON complaints(student_id);
CREATE INDEX idx_complaint_department ON complaints(assigned_department_id);
CREATE INDEX idx_complaint_status ON complaints(status);
CREATE INDEX idx_complaint_created ON complaints(created_at);
CREATE INDEX idx_transfer_complaint ON complaint_transfers(complaint_id);
CREATE INDEX idx_attachment_complaint ON attachments(complaint_id);

INSERT IGNORE INTO departments (department_id, department_name, description, response_time_hours) VALUES
(1, 'Student Affairs', 'Handles general student welfare and support', 24),
(2, 'Facilities', 'Manages campus facilities and maintenance', 48),
(3, 'Security', 'Campus security and safety concerns', 4),
(4, 'Academic Affairs', 'Academic-related complaints and issues', 72),
(5, 'Health Services', 'Health and medical services', 12);

INSERT IGNORE INTO campus_locations (location_id, location_name, latitude, longitude, building_code) VALUES
(1, 'Student Cafeteria', 8.8876349, 38.8100775, 'SC-001'),
(2, 'Library', 8.8852786, 38.8106169, 'LIB-001'),
(3, 'Science Building', 8.8840109, 38.8135765, 'SCI-001'),
(4, 'Administration Building', 8.8848884, 38.809952, 'ADM-001'),
(5, 'Sports Complex', 8.886662, 38.807434, 'SPT-001');

INSERT IGNORE INTO campus_edges (from_location_id, to_location_id, distance_meters) VALUES
(1, 2, 120), (2, 1, 120),
(1, 3, 150), (3, 1, 150),
(1, 4, 200), (4, 1, 200),
(2, 4, 180), (4, 2, 180),
(3, 5, 250), (5, 3, 250),
(4, 5, 300), (5, 4, 300),
(2, 3, 100), (3, 2, 100);
