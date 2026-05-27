package com.compass.db;

import com.compass.model.User;
import com.compass.repository.UserRepository;
import com.compass.service.RepositoryFactory;
import com.compass.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seeds default demo accounts when the database is empty.
 */
public final class DataSeeder {
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private DataSeeder() {}

    public static void seedIfNeeded() {
        if (!DatabaseManager.getInstance().testConnection()) {
            logger.warn("Database not available — skipping seed. Import schema.sql and check application.properties.");
            return;
        }
        UserRepository users = RepositoryFactory.users();
        if (users.count() > 0) {
            return;
        }
        logger.info("Seeding default users...");
        createUser(users, "admin", "admin@campus.edu", "System Administrator", "Admin@123", User.UserRole.ADMIN, null);
        createUser(users, "staff_facilities", "facilities@campus.edu", "Facilities Staff", "Staff@123", User.UserRole.DEPARTMENT_STAFF, 2);
        createUser(users, "student1", "student1@campus.edu", "Demo Student", "Student@123", User.UserRole.STUDENT, null);
        logger.info("Default users created: admin / staff_facilities / student1 (passwords end with @123)");
    }

    private static void createUser(UserRepository repo, String username, String email,
                                   String fullName, String password, User.UserRole role, Integer deptId) {
        User user = new User(username, email, fullName, role);
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setDepartmentId(deptId);
        repo.save(user);
    }
}
