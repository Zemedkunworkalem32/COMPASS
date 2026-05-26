package com.compass.repository;

import com.compass.model.User;
import java.util.Optional;

/**
 * User Repository Interface
 * TEAMMATE 1: Abdissa
 */
public interface UserRepository extends Repository<User, Integer> {
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find users by department ID
     */
    java.util.List<User> findByDepartmentId(Integer departmentId);

    /**
     * Find users by role
     */
    java.util.List<User> findByRole(String role);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
}
