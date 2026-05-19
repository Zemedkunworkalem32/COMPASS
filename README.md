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
