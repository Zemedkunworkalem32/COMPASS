# Compass - Campus Complaint Management System

Compass is a small JavaFX desktop application for a university complaint workflow.

## Features

- Students can register, log in, submit complaints, attach a file, and view their own complaints.
- Department staff can view assigned complaints and update or resolve them.
- Administrators can filter complaints, assign departments, resolve complaints, and view simple department statistics.
- A campus map shows locations and shortest routes between buildings.

## Simple MVC Structure

```text
src/main/java/com/compass/
  config/       App configuration, database connection, session, view loading
  controllers/  JavaFX screen controllers
  models/       Data classes plus the small database/model layer

src/main/resources/
  views/        FXML screens
  public/       CSS and map HTML

sql/            Database schema and optional demo seed data
```

## Tech Stack

- Java 21
- JavaFX controls, FXML, and WebView
- MySQL JDBC
- BCrypt password hashing
- JUnit 5 tests
- Maven build

## Run

```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p compass_db < sql/seed_data.sql
mvn javafx:run
```

Edit `src/main/resources/application.properties` if your database credentials differ.

## Test

```bash
mvn test
```

## Demo Accounts

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `Admin@123` | Administrator |
| `staff_facilities` | `Staff@123` | Department staff |
| `student1` | `Student@123` | Student |
