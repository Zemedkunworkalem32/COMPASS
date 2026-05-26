<<<<<<< HEAD
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
=======
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── campus/
│   │           ├── Main.java                          (Entry point - shared)
│   │           │
│   │           ├── repository/
│   │           │   ├── Repository.java                (Generic interface - shared)
│   │           │   ├── UserRepository.java            (Teammate 1)
│   │           │   ├── DepartmentRepository.java      (Teammate 1)
│   │           │   ├── LocationRepository.java        (Teammate 1)
│   │           │   └── ComplaintRepository.java       ✅ ZEMEDKUN - CRUD, JOINs, filtering
│   │           │
│   │           ├── service/
│   │           │   ├── AuthService.java               (Teammate 1)
│   │           │   ├── NavigationService.java         (Teammate 1)
│   │           │   ├── ComplaintService.java          ✅ ZEMEDKUN - validation, routing, async
│   │           │   ├── FileUploadService.java         ✅ ZEMEDKUN - NIO2, validation
│   │           │   ├── RemoteReportingClient.java     ✅ ZEMEDKUN - HTTP, retry logic
│   │           │   ├── BackgroundSyncService.java     ✅ ZEMEDKUN - multithreading, async refresh
│   │           │   └── PerformanceAnalyticsService.java ✅ ZEMEDKUN - response time, efficiency
│   │           │
│   │           ├── controller/
│   │           │   ├── LoginController.java           (Teammate 1)
│   │           │   ├── MapController.java             (Teammate 1)
│   │           │   ├── ComplaintFormController.java   ✅ ZEMEDKUN - submit complaint UI
│   │           │   ├── MyComplaintsController.java    ✅ ZEMEDKUN - tracking/history UI
│   │           │   ├── AdminDashboardController.java  (Teammate 3)
│   │           │   ├── AdminComplaintController.java  (Teammate 3)
│   │           │   ├── AdminDepartmentController.java (Teammate 3)
│   │           │   ├── AdminStudentController.java    (Teammate 3)
│   │           │   ├── AdminLocationController.java   (Teammate 3)
│   │           │   ├── StudentDashboardController.java (Teammate 3)
│   │           │   └── ServicesController.java        (Teammate 3)
│   │           │
│   │           ├── model/
│   │           │   ├── User.java                      (Teammate 1)
│   │           │   ├── Department.java                (Teammate 1)
│   │           │   ├── CampusLocation.java            (Teammate 1)
│   │           │   ├── Complaint.java                 ✅ ZEMEDKUN - complaint model
│   │           │   ├── ComplaintTransfer.java         ✅ ZEMEDKUN - reassignment model
│   │           │   └── Attachment.java                ✅ ZEMEDKUN - file attachment model
│   │           │
│   │           ├── util/
│   │           │   ├── PasswordUtil.java              (shared - Teammate 1)
│   │           │   ├── SessionManager.java            (shared - Teammate 1)
│   │           │   ├── FileUploadUtil.java            ✅ ZEMEDKUN - NIO2, validation
│   │           │   └── ComplaintValidator.java        ✅ ZEMEDKUN - input validation
│   │           │
│   │           ├── database/
│   │           │   └── DatabaseManager.java           (Teammate 1)
│   │           │
│   │           ├── algorithm/
│   │           │   └── Dijkstra.java                  (Teammate 1)
│   │           │
│   │           └── thread/
│   │               ├── AsyncComplaintLoader.java      ✅ ZEMEDKUN - background loading
│   │               └── StatusUpdateTask.java          ✅ ZEMEDKUN - periodic status check
│   │
│   └── resources/
│       ├── views/
│       │   ├── login.fxml                             (Teammate 1)
│       │   ├── complaint_form.fxml                    ✅ ZEMEDKUN - submit complaint screen
│       │   ├── my_complaints.fxml                     ✅ ZEMEDKUN - complaint history screen
│       │   ├── admin_dashboard.fxml                   (Teammate 3)
│       │   ├── admin_complaints.fxml                  (Teammate 3)
│       │   ├── admin_departments.fxml                 (Teammate 3)
│       │   ├── admin_students.fxml                    (Teammate 3)
│       │   ├── admin_locations.fxml                   (Teammate 3)
│       │   ├── student_dashboard.fxml                 (Teammate 3)
│       │   └── services.fxml                          (Teammate 3)
│       │
│       ├── css/
│       │   └── compass.css                            (Teammate 3)
│       │
│       ├── web/
│       │   └── campus_map.html                        (Teammate 1)
│       │
│       └── images/
│           └── (icons for map/file upload)            (shared)
│
├── test/
│   └── java/
│       └── com/
│           └── campus/
│               ├── service/
│               │   ├── ComplaintServiceTest.java      ✅ ZEMEDKUN - unit tests
│               │   ├── FileUploadServiceTest.java     ✅ ZEMEDKUN - unit tests
│               │   └── PerformanceAnalyticsTest.java  ✅ ZEMEDKUN - unit tests
│               │
│               ├── repository/
│               │   └── ComplaintRepositoryTest.java   ✅ ZEMEDKUN - repository tests
│               │
│               └── util/
│                   └── ComplaintValidatorTest.java    ✅ ZEMEDKUN - validation tests
│
└── sql/
    ├── schema.sql                                      (Teammate 1)
    └── seed_data.sql                                   (Teammate 1)
>>>>>>> f07b742d7fde68a608efa047d1f34a8bb1eb6e27
