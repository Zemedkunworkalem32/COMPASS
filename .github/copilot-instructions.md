# Campus Complaint Management System - Development Guide

## Project Overview
A JavaFX-based Campus Complaint Management & Department Performance Tracking System with:
- MySQL database backend
- Multi-threaded services
- Admin dashboard and analytics
- Campus navigation with Dijkstra's algorithm
- File upload and remote reporting

## Team Structure & Responsibilities

### Teammate 1: Abdissa
**Database + Authentication + Navigation + Core Infrastructure**
- **Packages**: `com.compass.db`, `com.compass.auth`, `com.compass.navigation`
- **Tasks**:
  - Database schema (schema.sql) and DatabaseManager
  - Model classes: User, Complaint, Department, CampusLocation, ComplaintTransfer, Attachment
  - Repository interfaces and implementations
  - AuthService with BCrypt password hashing
  - LoginController and LoginView.fxml
  - CampusGraph with Dijkstra's algorithm
  - MapController with Leaflet integration

### Teammate 2: Zemedkun
**Complaint Management + File Upload + Networking + Analytics**
- **Packages**: `com.compass.service`, `com.compass.util`
- **Tasks**:
  - ComplaintService with CRUD and filtering
  - ComplaintFormController and MyComplaintsController
  - FileUploadUtil with validation
  - RemoteReportingClient with HTTP/retry logic
  - ComplaintTransfer logic
  - Department performance analytics
  - Background sync service

### Teammate 3: Yoseph
**Admin Dashboard + Department Management + UI Shell + Analytics**
- **Packages**: `com.compass.controller` (UI), `com.compass.ui`
- **Tasks**:
  - StudentDashboardController/View (main shell)
  - AdminDashboardController with statistics
  - AdminComplaintController/View
  - AdminDepartmentController/View
  - AdminStudentController/View
  - AdminLocationController/View
  - compass.css styling
  - Auto-refresh daemon thread (30-second polling)

## Shared Responsibilities
- **SessionManager** + **PasswordUtil**: Written by Teammate 1, used by all
- **module-info.java** + **pom.xml**: Modified only with team notification
- All teammates write JUnit 5 tests for their code

## Development Setup

### Prerequisites
- JDK 17+ (Eclipse Temurin)
- Maven 3.9+
- MySQL 8+
- Git
- IntelliJ IDEA Community Edition

### Build & Run
```bash
mvn clean install           # Build project
mvn javafx:run             # Run application
mvn test                   # Run tests
mvn clean package          # Create JAR
```

## Git Workflow

### Branch Strategy
```bash
git checkout -b feature/feature-name          # Create feature branch
git commit -m "feat: add feature description" # Commit with context
git push origin feature/feature-name          # Push to remote
# Open Pull Request and request review
```

### Commit Message Format
- `feat:` New feature
- `fix:` Bug fix
- `refactor:` Code restructuring
- `test:` Test additions
- `docs:` Documentation
- `chore:` Configuration/dependencies

## Code Guidelines

### Java Style
- Use PascalCase for classes
- Use camelCase for methods/variables
- Import statements organized and grouped
- No trailing whitespace

### Database
- Always use PreparedStatements
- Use try-with-resources for connections
- Document foreign key relationships
- Use proper transaction handling

### Testing
- JUnit 5 annotations: `@Test`, `@BeforeEach`, `@ParameterizedTest`
- Mockito for dependencies: `@Mock`, `@InjectMocks`
- Test coverage target: 70%+

### Logging
- Use SLF4J: `LoggerFactory.getLogger(ClassName.class)`
- Configure in `logback.xml`
- Never use System.out.println()

## Module Organization

```
com.compass
├── auth/              → AuthService, LoginController
├── db/               → DatabaseManager, connection handling
├── model/            → POJO entities (User, Complaint, etc.)
├── repository/       → Data access layer
├── service/          → Business logic (ComplaintService, etc.)
├── controller/       → JavaFX controllers (UI logic)
├── ui/               → Custom UI components
├── navigation/       → CampusGraph, NavigationService
└── util/             → PasswordUtil, FileUploadUtil, SessionManager
```

## Configuration Files

### pom.xml
- Dependency versions centralized
- JavaFX, MySQL, JUnit 5, Mockito configured
- Test/build plugins configured
- Only modify with team coordination

### module-info.java
- Exports all packages
- Requires JavaFX, HTTP, SQL, Logging modules
- Only modify when adding new dependencies

### schema.sql
- Complete database schema with 7 tables
- Indexes for performance optimization
- Seed data included for development

## Important Guidelines

⚠️ **Do NOT bypass authentication/session checks**
⚠️ **Do NOT hardcode credentials** - use properties file
⚠️ **Do NOT commit .env or local configs**
⚠️ **Do NOT modify main branch directly** - always PR
⚠️ **Do coordinate pom.xml changes** - notify team

## Useful Resources

- [JavaFX Documentation](https://openjfx.io)
- [MySQL JDBC Driver](https://dev.mysql.com/doc/connector-j/8.0/en/)
- [JUnit 5 Guide](https://junit.org/junit5/docs/current/user-guide/)
- [SLF4J Manual](http://www.slf4j.org/manual.html)
- [BCrypt Hashing](https://www.mindrot.org/projects/jbcrypt/)

## Troubleshooting

### Maven Build Fails
```bash
mvn clean install -U  # Update dependencies
mvn dependency:tree   # Check dependency tree
```

### MySQL Connection Error
- Verify MySQL running: `mysql -u root -p`
- Check database exists: `SHOW DATABASES;`
- Verify credentials in application.properties

### JavaFX Module Issues
- Ensure JDK 17+ in IDE settings
- Check --module-path in VM options
- Rebuild project: Maven > clean install

## Code Review Checklist

Before requesting review:
- [ ] Code follows team guidelines
- [ ] Tests written and passing
- [ ] No hardcoded values
- [ ] Proper error handling
- [ ] Comments for complex logic
- [ ] Database: PreparedStatements used
- [ ] No System.out.println() in code
- [ ] Commit messages are clear

## Common Commands

```bash
# Build and run
mvn clean install && mvn javafx:run

# Run tests
mvn test
mvn test -Dtest=UserRepositoryTest

# Create project documentation
mvn javadoc:javadoc

# Check code style
mvn checkstyle:check

# Package executable JAR
mvn package

# Run specific class
mvn exec:java -Dexec.mainClass="com.compass.CompassApplication"
```

## Development Timeline

- **Week 1**: Setup (completed) + Database + Auth system
- **Week 2**: Core models + Repositories
- **Week 3**: Services + UI basic shell
- **Week 4**: Feature completion + Testing
- **Week 5**: Integration + Bug fixes
- **Week 6**: Final polish + Documentation

## Communication

- **GitHub Issues**: Bug reports and feature requests
- **Pull Requests**: Code reviews and discussions
- **Commits**: Clear messages explaining changes
- **Branches**: Descriptive names (`feature/`, `fix/`, `refactor/`)
