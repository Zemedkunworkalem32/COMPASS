package com.compass.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session Manager - Manages user sessions and authentication state
 * SHARED: Used by all teammates
 * Written by: TEAMMATE 1 (Abdissa)
 */
public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static SessionManager instance;
    private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000; // 30 minutes
    
    private int currentUserId;
    private String currentUsername;
    private String currentUserRole;
    private Integer currentUserDepartmentId;
    private long lastActivityTime;
    private boolean isLoggedIn;

    private SessionManager() {
        this.isLoggedIn = false;
        this.lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Get singleton instance of SessionManager
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Create a new session for user
     */
    public void createSession(int userId, String username, String role, Integer departmentId) {
        this.currentUserId = userId;
        this.currentUsername = username;
        this.currentUserRole = role;
        this.currentUserDepartmentId = departmentId;
        this.isLoggedIn = true;
        this.lastActivityTime = System.currentTimeMillis();
        logger.info("Session created for user: {}", username);
    }

    /**
     * Check if session is valid and not expired
     */
    public boolean isSessionValid() {
        if (!isLoggedIn) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActivityTime > SESSION_TIMEOUT_MS) {
            logger.warn("Session expired for user: {}", currentUsername);
            invalidateSession();
            return false;
        }
        
        updateLastActivityTime();
        return true;
    }

    /**
     * Update last activity time (reset timeout)
     */
    public void updateLastActivityTime() {
        this.lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Invalidate (logout) the current session
     */
    public void invalidateSession() {
        logger.info("Session invalidated for user: {}", currentUsername);
        this.isLoggedIn = false;
        this.currentUserId = 0;
        this.currentUsername = null;
        this.currentUserRole = null;
        this.currentUserDepartmentId = null;
    }

    // Getters
    public int getCurrentUserId() { return currentUserId; }
    public String getCurrentUsername() { return currentUsername; }
    public String getCurrentUserRole() { return currentUserRole; }
    public Integer getCurrentUserDepartmentId() { return currentUserDepartmentId; }
    public boolean isLoggedIn() { return isLoggedIn; }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return isLoggedIn && "ADMIN".equals(currentUserRole);
    }

    /**
     * Check if current user is department staff
     */
    public boolean isDepartmentStaff() {
        return isLoggedIn && "DEPARTMENT_STAFF".equals(currentUserRole);
    }

    /**
     * Check if current user is student
     */
    public boolean isStudent() {
        return isLoggedIn && "STUDENT".equals(currentUserRole);
    }

    @Override
    public String toString() {
        return "SessionManager{" +
                "currentUsername='" + currentUsername + '\'' +
                ", currentUserRole='" + currentUserRole + '\'' +
                ", isLoggedIn=" + isLoggedIn +
                '}';
    }
}
