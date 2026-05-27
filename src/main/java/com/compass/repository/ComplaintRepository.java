package com.compass.repository;

import com.compass.model.Complaint;
import java.util.List;
import java.util.Optional;

/**
 * Complaint Repository Interface
 * TEAMMATE 2: Zemedkun
 */
public interface ComplaintRepository extends Repository<Complaint, Integer> {
    /**
     * Find complaints by student ID
     */
    List<Complaint> findByStudentId(int studentId);

    /**
     * Find complaints by department ID
     */
    List<Complaint> findByDepartmentId(int departmentId);

    /**
     * Find complaints by status
     */
    List<Complaint> findByStatus(String status);

    /**
     * Find complaints by priority
     */
    List<Complaint> findByPriority(String priority);

    /**
     * Find complaints by location ID
     */
    List<Complaint> findByLocationId(int locationId);

    /**
     * Filter complaints with multiple criteria
     */
    List<Complaint> filterComplaints(Integer studentId, Integer departmentId, 
                                     String status, String priority);

    /**
     * Get pending complaints for a department
     */
    List<Complaint> getPendingComplaints(int departmentId);

    /**
     * Count resolved complaints
     */
    long countResolved();

    /**
     * Count pending complaints
     */
    long countPending();
}
