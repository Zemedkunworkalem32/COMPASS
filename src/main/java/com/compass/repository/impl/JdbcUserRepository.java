package com.compass.repository.impl;

import com.compass.db.DatabaseManager;
import com.compass.model.User;
import com.compass.repository.UserRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {

    @Override
    public User save(User entity) {
        String sql = """
            INSERT INTO users (username, email, password_hash, full_name, role, department_id, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entity.getUsername());
            ps.setString(2, entity.getEmail());
            ps.setString(3, entity.getPasswordHash());
            ps.setString(4, entity.getFullName());
            ps.setString(5, entity.getRole().name());
            if (entity.getDepartmentId() != null) {
                ps.setInt(6, entity.getDepartmentId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setBoolean(7, entity.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setUserId(keys.getInt(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public User update(User entity) {
        String sql = """
            UPDATE users SET email=?, full_name=?, role=?, department_id=?, is_active=?, password_hash=?
            WHERE user_id=?
            """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getEmail());
            ps.setString(2, entity.getFullName());
            ps.setString(3, entity.getRole().name());
            if (entity.getDepartmentId() != null) {
                ps.setInt(4, entity.getDepartmentId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setBoolean(5, entity.isActive());
            ps.setString(6, entity.getPasswordHash());
            ps.setInt(7, entity.getUserId());
            ps.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "UPDATE users SET is_active=FALSE WHERE user_id=?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        return findOne("SELECT * FROM users WHERE user_id=?", id);
    }

    @Override
    public List<User> findAll() {
        return findMany("SELECT * FROM users WHERE is_active=TRUE ORDER BY full_name");
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT 1 FROM users WHERE user_id=?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE is_active=TRUE")) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findOne("SELECT * FROM users WHERE username=? AND is_active=TRUE", username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findOne("SELECT * FROM users WHERE email=? AND is_active=TRUE", email);
    }

    @Override
    public List<User> findByDepartmentId(Integer departmentId) {
        return findMany("SELECT * FROM users WHERE department_id=? AND is_active=TRUE", departmentId);
    }

    @Override
    public List<User> findByRole(String role) {
        return findMany("SELECT * FROM users WHERE role=? AND is_active=TRUE", role);
    }

    @Override
    public boolean existsByUsername(String username) {
        return exists("username", username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return exists("email", email);
    }

    private boolean exists(String column, String value) {
        String sql = "SELECT 1 FROM users WHERE " + column + "=?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<User> findOne(String sql, Object param) {
        List<User> list = findMany(sql, param);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private List<User> findMany(String sql, Object... params) {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(User.UserRole.valueOf(rs.getString("role")));
        int dept = rs.getInt("department_id");
        if (!rs.wasNull()) {
            user.setDepartmentId(dept);
        }
        user.setActive(rs.getBoolean("is_active"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            user.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            user.setUpdatedAt(updated.toLocalDateTime());
        }
        return user;
    }
}
