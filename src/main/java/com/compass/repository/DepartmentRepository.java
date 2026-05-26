package com.compass.repository;

import com.compass.model.Department;

/**
 * Department Repository Interface
 * TEAMMATE 1: Abdissa
 */
public interface DepartmentRepository extends Repository<Department, Integer> {
    /**
     * Find department by name
     */
    java.util.Optional<Department> findByDepartmentName(String departmentName);

    /**
     * Find all active departments
     */
    java.util.List<Department> findAllActive();

    /**
     * Count complaints for a department
     */
    long countComplaints(int departmentId);
}
