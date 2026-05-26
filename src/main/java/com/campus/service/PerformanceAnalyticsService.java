package com.campus.service;

import com.campus.database.DatabaseManager;
import com.campus.model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PerformanceAnalyticsService {

    private final Connection connection;

    public PerformanceAnalyticsService() throws SQLException {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public List<Department> getAllDepartmentPerformance() throws SQLException {
        List<Department> departments = new ArrayList<>();
        
        String sql = 
            "SELECT d.id, d.department_name, d.description, " +
            "COUNT(c.id) as total_complaints, " +
            "SUM(CASE WHEN c.status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved_complaints, " +
            "SUM(CASE WHEN c.status IN ('PENDING', 'UNDER_REVIEW', 'ASSIGNED', 'IN_PROGRESS') THEN 1 ELSE 0 END) as pending_complaints " +
            "FROM departments d " +
            "LEFT JOIN complaints c ON d.id = c.department_id " +
            "GROUP BY d.id, d.department_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Department dept = new Department();
                dept.setId(rs.getInt("id"));
                dept.setDepartmentName(rs.getString("department_name"));
                dept.setDescription(rs.getString("description"));
                dept.setTotalComplaints(rs.getInt("total_complaints"));
                dept.setResolvedComplaints(rs.getInt("resolved_complaints"));
                dept.setPendingComplaints(rs.getInt("pending_complaints"));
                dept.setAvgResponseTimeHours(calculateAverageResponseTime(dept.getId()));
                dept.setEfficiencyScore(calculateEfficiencyScore(dept));
                departments.add(dept);
            }
        }
        
        return departments;
    }

    public double calculateAverageResponseTime(int departmentId) throws SQLException {
        String sql = 
            "SELECT AVG(TIMESTAMPDIFF(HOUR, created_at, first_response_at)) as avg_hours " +
            "FROM complaints WHERE department_id = ? AND first_response_at IS NOT NULL";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, departmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_hours");
                }
            }
        }
        return 0;
    }

    public double calculateAverageResolutionTime(int departmentId) throws SQLException {
        String sql = 
            "SELECT AVG(TIMESTAMPDIFF(HOUR, created_at, resolved_at)) as avg_hours " +
            "FROM complaints WHERE department_id = ? AND resolved_at IS NOT NULL";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, departmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_hours");
                }
            }
        }
        return 0;
    }

    private double calculateEfficiencyScore(Department dept) {
        if (dept.getTotalComplaints() == 0) {
            return 100.0;
        }
        
        double resolutionRate = (double) dept.getResolvedComplaints() / dept.getTotalComplaints() * 100;
        
        double responseTimeScore;
        if (dept.getAvgResponseTimeHours() <= 0) {
            responseTimeScore = 100;
        } else if (dept.getAvgResponseTimeHours() <= 24) {
            responseTimeScore = 100;
        } else if (dept.getAvgResponseTimeHours() <= 48) {
            responseTimeScore = 80;
        } else if (dept.getAvgResponseTimeHours() <= 72) {
            responseTimeScore = 60;
        } else if (dept.getAvgResponseTimeHours() <= 120) {
            responseTimeScore = 40;
        } else {
            responseTimeScore = 20;
        }
        
        return (resolutionRate * 0.6) + (responseTimeScore * 0.4);
    }

    public Department getBestPerformingDepartment() throws SQLException {
        List<Department> depts = getAllDepartmentPerformance();
        return depts.stream()
                .max((d1, d2) -> Double.compare(d1.getEfficiencyScore(), d2.getEfficiencyScore()))
                .orElse(null);
    }

    public Department getWorstPerformingDepartment() throws SQLException {
        List<Department> depts = getAllDepartmentPerformance();
        return depts.stream()
                .min((d1, d2) -> Double.compare(d1.getEfficiencyScore(), d2.getEfficiencyScore()))
                .orElse(null);
    }

    public double getOverallResolutionRate() throws SQLException {
        String sql = 
            "SELECT COUNT(*) as total, " +
            "SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved " +
            "FROM complaints";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                int resolved = rs.getInt("resolved");
                if (total == 0) return 100.0;
                return (double) resolved / total * 100;
            }
        }
        return 0;
    }

    public double getAverageResponseTimeOverall() throws SQLException {
        String sql = 
            "SELECT AVG(TIMESTAMPDIFF(HOUR, created_at, first_response_at)) as avg_hours " +
            "FROM complaints WHERE first_response_at IS NOT NULL";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("avg_hours");
            }
        }
        return 0;
    }
}