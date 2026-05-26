package com.compass.service;

import com.compass.model.User;
import com.compass.repository.UserRepository;
import com.compass.util.PasswordUtil;
import com.compass.util.SessionManager;

import java.util.Optional;

/**
 * Authentication: login, registration, password hashing.
 */
public class AuthService {
    private final UserRepository userRepository;

    public AuthService() {
        this(RepositoryFactory.users());
    }

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            return Optional.empty();
        }
        SessionManager.getInstance().createSession(
                user.getUserId(),
                user.getUsername(),
                user.getRole().name(),
                user.getDepartmentId()
        );
        return Optional.of(user);
    }

    public User registerStudent(String username, String email, String fullName, String password) {
        validateRegistration(username, email, password);
        User user = new User(username.trim(), email.trim(), fullName.trim(), User.UserRole.STUDENT);
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        return userRepository.save(user);
    }

    public void logout() {
        SessionManager.getInstance().invalidateSession();
    }

    private void validateRegistration(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (!PasswordUtil.isPasswordStrong(password)) {
            throw new IllegalArgumentException(
                    "Password must be 8+ chars with upper, lower, digit, and special character");
        }
    }
}
