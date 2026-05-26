# Compass — Campus Complaint Management System

JavaFX desktop application for student complaint submission, department handling, admin oversight, performance analytics, and campus map navigation (Dijkstra shortest path).

## Requirements

- Java JDK 17+
- Maven 3.9+
- MySQL 8+

## Quick start

### 1. Database

```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

Creates database `compass_db` with tables, seed departments, campus locations, and walking routes.

### 2. Configure credentials

Edit `src/main/resources/application.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/compass_db
db.user=root
db.password=your_password
```

### 3. Run

```bash
mvn javafx:run
```

On first run (empty `users` table), demo accounts are created automatically:

| Username | Password | Role |
|----------|----------|------|
| admin | Admin@123 | Admin |
| staff_facilities | Staff@123 | Department staff (Facilities) |
| student1 | Student@123 | Student |

## Features

- **Students:** Register, login, submit complaints with attachments, track status, campus map & routes
- **Admin:** View/filter all complaints, assign departments, update status, analytics dashboard (auto-refresh every 30s)
- **Department staff:** View department complaints, update progress, resolve with notes
- **Technical:** JDBC/MySQL, MVC, BCrypt passwords, file uploads, HttpURLConnection remote reporting, WebView map, multithreading

## Project structure

```
src/main/java/com/compass/
  controller/     JavaFX controllers + FXML
  service/        AuthService, ComplaintService, DepartmentAnalyticsService
  repository/     Interfaces + impl/ JDBC repositories
  model/          User, Complaint, Department, CampusLocation
  navigation/     RouteFinder (Dijkstra)
  util/           Password, session, file upload, remote HTTP client
  db/             DatabaseManager, DataSeeder
src/main/resources/
  fxml/           UI layouts
  css/            Styles
  web/            Campus map HTML (Leaflet)
  db/schema.sql   Database schema
```

## Complaint workflow

1. Student submits → `SUBMITTED`
2. Admin reviews → `UNDER_REVIEW`
3. Admin assigns department → `ASSIGNED`
4. Staff works → `IN_PROGRESS`
5. Resolved → `RESOLVED`

## Build

```bash
mvn clean package
mvn javafx:run
```
