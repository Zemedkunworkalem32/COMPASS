package com.campus.util;

import com.campus.model.User;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static final Map<String, Object> session = new HashMap<>();

    private SessionManager() {
    }

    public static void setCurrentUser(User user) {
        session.put("currentUser", user);
    }

    public static User getCurrentUser() {
        return (User) session.get("currentUser");
    }

    public static boolean isAuthenticated() {
        return getCurrentUser() != null;
    }

    public static int getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : -1;
    }

    public static void clearSession() {
        session.clear();
    }
}

