package com.compass.models;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private String fullName;
    private UserRole role = UserRole.STUDENT;
    private boolean active = true;
    private LocalDateTime createdAt;

    public enum UserRole {
        STUDENT, ADMIN, SYSTEM_ADMIN
    }

    public User() {}

    public User(String username, String email, String fullName, UserRole role) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return fullName == null || fullName.isBlank() ? username : fullName;
    }
}
