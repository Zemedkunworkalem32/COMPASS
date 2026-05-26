## Initial Setup Checklist

### Phase 1: Project Foundation (Week 1)

- [x] Create Maven project structure
- [x] Configure pom.xml with dependencies
- [x] Set up module-info.java
- [x] Create database schema (schema.sql)
- [x] Create Git repository with .gitignore
- [x] Create shared utilities (PasswordUtil, SessionManager)
- [x] Create base model classes

### Phase 2: Database & Authentication (Teammate 1: Abdissa)

- [ ] Implement UserRepository
- [ ] Implement DepartmentRepository
- [ ] Implement LocationRepository
- [ ] Create database connection pooling
- [ ] Implement AuthService
- [ ] Create LoginController and LoginView.fxml
- [ ] Write unit tests for repositories
- [ ] Implement CampusGraph for navigation
- [ ] Integrate Leaflet map

### Phase 3: Complaint Management (Teammate 2: Zemedkun)

- [ ] Implement ComplaintRepository
- [ ] Create ComplaintService with CRUD
- [ ] Create ComplaintFormController and View
- [ ] Implement FileUploadUtil
- [ ] Create file upload functionality
- [ ] Implement RemoteReportingClient
- [ ] Create ComplaintTransfer logic
- [ ] Build analytics service
- [ ] Write comprehensive tests

### Phase 4: Admin Dashboard (Teammate 3: Yoseph)

- [ ] Create StudentDashboardController and View
- [ ] Create AdminDashboardController
- [ ] Implement admin CRUD controllers
- [ ] Create department management UI
- [ ] Create student management UI
- [ ] Create location management UI
- [ ] Write compass.css styling
- [ ] Implement auto-refresh daemon thread
- [ ] Add analytics visualizations

### Phase 5: Integration & Testing

- [ ] Integration testing
- [ ] End-to-end testing
- [ ] Performance testing
- [ ] Security testing
- [ ] Bug fixes and refinements

### Phase 6: Documentation & Deployment

- [ ] Complete JavaDoc
- [ ] Write deployment guide
- [ ] Create user manual
- [ ] Final package and release

## Setup Instructions for Developers

1. Clone the repository
2. Import into IntelliJ IDEA
3. Configure MySQL database
4. Update `application.properties` with credentials
5. Run `mvn clean install`
6. Run `mvn javafx:run` to start development

## Important Reminders

- Always create feature branches
- Never commit directly to main
- Coordinate pom.xml changes
- Don't commit .env or local configurations
- Use PreparedStatements for database queries
- Write tests for your code
- Use SLF4J for logging, not System.out
- Keep session checks in place
