package com.compass.config;

import com.compass.models.User;

public final class Session {
    private static User currentUser;

    private Session() {}

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User user() {
        return currentUser;
    }

    public static int userId() {
        return currentUser == null ? 0 : currentUser.getUserId();
    }

    public static String username() {
        return currentUser == null ? "" : currentUser.getUsername();
    }

    public static boolean isAdmin() {
        return currentUser != null
                && (currentUser.getRole() == User.UserRole.ADMIN
                || currentUser.getRole() == User.UserRole.SYSTEM_ADMIN);
    }

    public static boolean isSystemAdmin() {
        return currentUser != null && currentUser.getRole() == User.UserRole.SYSTEM_ADMIN;
    }
}
