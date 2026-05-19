CREATE DATABASE IF NOT EXISTS compass;
USE compass;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS departments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    location_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS campus_locations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    building VARCHAR(100),
    floor VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS complaints (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(250) NOT NULL,
    description TEXT NOT NULL,
    student_id INT NOT NULL,
    department_id INT,
    status VARCHAR(50) NOT NULL DEFAULT 'NEW',
    priority VARCHAR(50) DEFAULT 'NORMAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE TABLE IF NOT EXISTS complaint_transfers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    complaint_id INT NOT NULL,
    from_department_id INT,
    to_department_id INT,
    transfer_reason VARCHAR(255),
    transferred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaints(id),
    FOREIGN KEY (from_department_id) REFERENCES departments(id),
    FOREIGN KEY (to_department_id) REFERENCES departments(id)
);

CREATE TABLE IF NOT EXISTS attachments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    complaint_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    file_path VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaints(id)
);

INSERT INTO users (username, email, password_hash, role)
VALUES
  ('student1', 'student1@campus.edu', 'REPLACE_WITH_HASH', 'STUDENT'),
  ('admin1', 'admin1@campus.edu', 'REPLACE_WITH_HASH', 'ADMIN'),
  ('deptstaff1', 'staff1@campus.edu', 'REPLACE_WITH_HASH', 'DEPARTMENT_STAFF');

INSERT INTO departments (name, email, location_id)
VALUES
  ('Housing', 'housing@campus.edu', 1),
  ('IT Support', 'itsupport@campus.edu', 2),
  ('Facilities', 'facilities@campus.edu', 3);

INSERT INTO campus_locations (name, building, floor)
VALUES
  ('North Campus', 'Maple Hall', 'First Floor'),
  ('South Campus', 'Oak Hall', 'Second Floor'),
  ('East Campus', 'Cedar Building', 'Ground Floor');
