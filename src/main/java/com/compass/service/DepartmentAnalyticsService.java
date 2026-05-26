package com.compass.service;

import com.compass.db.DatabaseManager;
import com.compass.model.Complaint;
import com.compass.model.Department;
import com.compass.repository.ComplaintRepository;
import com.compass.repository.DepartmentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Department performance analytics and response-time tracking.
 */
public class DepartmentAnalyticsService {
    private final DepartmentRepository departmentRepository;
    private final ComplaintRepository complaintRepository;

    public DepartmentAnalyticsService() {
        this(RepositoryFactory.departments(), RepositoryFactory.complaints());
    }

    public DepartmentAnalyticsService(DepartmentRepository departmentRepository,
                                      ComplaintRepository complaintRepository) {
        this.departmentRepository = departmentRepository;
        this.complaintRepository = complaintRepository;
    }

    public long getTotalComplaints() {
        return complaintRepository.count();
    }

    public long getResolvedCount() {
        return complaintRepository.countResolved();
    }

    public long getPendingCount() {
        return complaintRepository.countPending();
    }

    public List<DepartmentStats> getDepartmentStats() {
        List<DepartmentStats> stats = new ArrayList<>();
        String sql = """
            SELECT d.department_id, d.department_name,
                   COUNT(c.complaint_id) AS total,
                   SUM(CASE WHEN c.status='RESOLVED' THEN 1 ELSE 0 END) AS resolved,
                   SUM(CASE WHEN c.status NOT IN ('RESOLVED','CLOSED') THEN 1 ELSE 0 END) AS pending,
                   AVG(TIMESTAMPDIFF(HOUR, c.created_at, c.resolved_at)) AS avg_hours
            FROM departments d
            LEFT JOIN complaints c ON d.department_id = c.assigned_department_id
            WHERE d.is_active=TRUE
            GROUP BY d.department_id, d.department_name
            ORDER BY d.department_name
            """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DepartmentStats s = new DepartmentStats();
                s.departmentId = rs.getInt("department_id");
                s.departmentName = rs.getString("department_name");
                s.totalComplaints = rs.getLong("total");
                s.resolvedComplaints = rs.getLong("resolved");
                s.pendingComplaints = rs.getLong("pending");
                double avg = rs.getDouble("avg_hours");
                s.averageResponseHours = rs.wasNull() ? 0 : avg;
                if (s.totalComplaints > 0) {
                    s.efficiencyPercent = (s.resolvedComplaints * 100.0) / s.totalComplaints;
                }
                stats.add(s);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load department stats", e);
        }
        return stats;
    }

    public DepartmentStats getBestPerformingDepartment() {
        return getDepartmentStats().stream()
                .filter(s -> s.totalComplaints > 0)
                .max(Comparator.comparingDouble(DepartmentStats::getEfficiencyPercent))
                .orElse(null);
    }

    public DepartmentStats getSlowestDepartment() {
        return getDepartmentStats().stream()
                .filter(s -> s.averageResponseHours > 0)
                .max(Comparator.comparingDouble(DepartmentStats::getAverageResponseHours))
                .orElse(null);
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAllActive();
    }

    public static class DepartmentStats {
        public int departmentId;
        public String departmentName;
        public long totalComplaints;
        public long resolvedComplaints;
        public long pendingComplaints;
        public double averageResponseHours;
        public double efficiencyPercent;

        public double getEfficiencyPercent() { return efficiencyPercent; }
        public double getAverageResponseHours() { return averageResponseHours; }

        @Override
        public String toString() {
            return String.format("%s: %d total, %d resolved, %.1f%% efficiency, %.1f hrs avg response",
                    departmentName, totalComplaints, resolvedComplaints, efficiencyPercent, averageResponseHours);
        }
    }
}
