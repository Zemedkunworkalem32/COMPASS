package com.compass.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password Utility - Handles password hashing and validation
 * SHARED: Used by all teammates
 * Written by: TEAMMATE 1 (Abdissa)
 */
public class PasswordUtil {
    private static final int BCRYPT_STRENGTH = 12;

    /**
     * Hash a plain text password using BCrypt
     * @param plainPassword The plain text password
     * @return The hashed password
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_STRENGTH));
    }

    /**
     * Verify a plain text password against a hash
     * @param plainPassword The plain text password
     * @param hashedPassword The hashed password to verify against
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    /**
     * Check if password meets complexity requirements
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character
     * 
     * @param password The password to validate
     * @return true if password meets requirements
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>?/\\\\|`~].*");
        
        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }
}
