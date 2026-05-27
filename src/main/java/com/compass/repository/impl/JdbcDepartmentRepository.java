package com.compass.repository.impl;

import com.compass.db.DatabaseManager;
import com.compass.model.Department;
import com.compass.repository.DepartmentRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcDepartmentRepository implements DepartmentRepository {

    @Override
    public Department save(Department entity) {
        String sql = "INSERT INTO departments (department_name, description, email, phone, response_time_hours, is_active) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindDepartment(ps, entity);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setDepartmentId(keys.getInt(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save department", e);
        }
    }

    @Override
    public Department update(Department entity) {
        String sql = "UPDATE departments SET department_name=?, description=?, email=?, phone=?, response_time_hours=?, is_active=? WHERE department_id=?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindDepartment(ps, entity);
            ps.setInt(7, entity.getDepartmentId());
            ps.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update department", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE departments SET is_active=FALSE WHERE department_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Department> findById(Integer id) {
        return queryOne("SELECT * FROM departments WHERE department_id=?", id);
    }

    @Override
    public List<Department> findAll() {
        return queryList("SELECT * FROM departments ORDER BY department_name");
    }

    @Override
    public boolean existsById(Integer id) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM departments WHERE department_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long count() {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM departments WHERE is_active=TRUE")) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Department> findByDepartmentName(String departmentName) {
        return queryOne("SELECT * FROM departments WHERE department_name=?", departmentName);
    }

    @Override
    public List<Department> findAllActive() {
        return queryList("SELECT * FROM departments WHERE is_active=TRUE ORDER BY department_name");
    }

    @Override
    public long countComplaints(int departmentId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM complaints WHERE assigned_department_id=?")) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void bindDepartment(PreparedStatement ps, Department entity) throws SQLException {
        ps.setString(1, entity.getDepartmentName());
        ps.setString(2, entity.getDescription());
        ps.setString(3, entity.getEmail());
        ps.setString(4, entity.getPhone());
        ps.setInt(5, entity.getResponseTimeHours());
        ps.setBoolean(6, entity.isActive());
    }

    private Optional<Department> queryOne(String sql, Object param) {
        List<Department> list = queryList(sql, param);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private List<Department> queryList(String sql, Object... params) {
        List<Department> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private Department mapRow(ResultSet rs) throws SQLException {
        Department d = new Department();
        d.setDepartmentId(rs.getInt("department_id"));
        d.setDepartmentName(rs.getString("department_name"));
        d.setDescription(rs.getString("description"));
        d.setEmail(rs.getString("email"));
        d.setPhone(rs.getString("phone"));
        d.setResponseTimeHours(rs.getInt("response_time_hours"));
        d.setActive(rs.getBoolean("is_active"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            d.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            d.setUpdatedAt(updated.toLocalDateTime());
        }
        return d;
    }
}
