# Compass — Campus Complaint Management System

A JavaFX desktop application that lets students submit and track complaints, department staff resolve them, and administrators oversee everything from a single dashboard.

---

## Team

| Role | Teammate |
|------|---------|
| Models, Repositories, Auth, Map/Navigation | Abdissa |
| Complaint workflow, File upload, Analytics, Async | Zemedkun |
| Admin & Student dashboards, UI/CSS | Yoseph  |

---

## Tech Stack

- **Java 21** with JavaFX 21
- **MySQL 8** via JDBC (no ORM)
- **BCrypt** password hashing
- **Gson** for remote HTTP reporting
- **SLF4J + Logback** logging
- **JUnit 5 + Mockito** testing
- **Maven** build

---

## Project Structure

```
compass/
├── sql/
│   ├── schema.sql          ← Create all tables + seed departments & locations
│   └── seed_data.sql       ← Demo complaints & transfers
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── module-info.java
│   │   │   └── com/compass/
│   │   │       ├── CompassApplication.java
│   │   │       ├── controller/      ← JavaFX controllers (one per screen)
│   │   │       ├── db/              ← DatabaseManager, DataSeeder
│   │   │       ├── model/           ← Complaint, User, Department, …
│   │   │       ├── navigation/      ← Dijkstra RouteFinder
│   │   │       ├── repository/      ← Interfaces + JDBC implementations
│   │   │       ├── service/         ← Business logic (Auth, Complaint, Analytics)
│   │   │       ├── thread/          ← AsyncComplaintLoader, StatusUpdateTask
│   │   │       └── util/            ← Config, Session, Password, FileUpload, Validator
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── logback.xml
│   │       ├── css/main.css
│   │       ├── fxml/               ← One .fxml per screen
│   │       └── web/campus_map.html ← Leaflet map
│   └── test/
│       └── java/com/compass/
│           ├── service/            ← ComplaintServiceTest, FileUploadServiceTest, PerformanceAnalyticsTest
│           ├── repository/         ← ComplaintRepositoryTest (in-memory stub)
│           └── util/               ← ComplaintValidatorTest
└── pom.xml
```

---

## Quick Start

### 1. Database

```sql
mysql -u root -p < sql/schema.sql
mysql -u root -p compass_db < sql/seed_data.sql
```

Edit `src/main/resources/application.properties` if your MySQL credentials differ from the defaults (`root` / empty password).

### 2. Build & Run

```bash
mvn clean package
mvn javafx:run
```

### 3. Run Tests

```bash
mvn test
```

---

## Demo Accounts

| Username | Password | Role |
|----------|----------|------|
| `admin` | `Admin@123` | Administrator |
| `staff_facilities` | `Staff@123` | Department Staff (Facilities) |
| `student1` | `Student@123` | Student |

Accounts are created automatically by `DataSeeder` on first launch if the `users` table is empty.

---

## Key Features

- **Student**: Submit complaints with file attachments, select campus location, track status in real-time.
- **Staff**: View department queue, update status with notes, resolve complaints.
- **Admin**: Filter all complaints by department/status/priority, assign/reassign, view performance analytics (best-performing department, slowest average response time), auto-refreshes every 30 s.
- **Map**: Interactive Leaflet campus map with Dijkstra shortest-path routing between buildings.
- **Security**: BCrypt password hashing, role-based navigation, 30-minute session timeout.
- **Background sync**: `StatusUpdateTask` polls for updates without blocking the UI thread.

---

## Configuration (`application.properties`)

| Key | Default | Description |
|-----|---------|-------------|
| `db.url` | `jdbc:mysql://localhost:3306/compass_db` | JDBC URL |
| `db.user` | `root` | DB username |
| `db.password` | _(empty)_ | DB password |
| `upload.directory` | `./uploads` | Attachment storage path |
| `upload.max.size.mb` | `50` | Max file size in MB |
| `remote.report.enabled` | `false` | Enable HTTP reporting |
| `remote.report.url` | _(empty)_ | Endpoint URL |
