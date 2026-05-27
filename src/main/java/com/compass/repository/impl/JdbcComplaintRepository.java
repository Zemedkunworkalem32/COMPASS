package com.compass.repository.impl;

import com.compass.db.DatabaseManager;
import com.compass.model.Complaint;
import com.compass.repository.ComplaintRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcComplaintRepository implements ComplaintRepository {

    private static final String BASE_SELECT = """
        SELECT c.*, u.full_name AS student_name, d.department_name
        FROM complaints c
        LEFT JOIN users u ON c.student_id = u.user_id
        LEFT JOIN departments d ON c.assigned_department_id = d.department_id
        """;

    @Override
    public Complaint save(Complaint entity) {
        String sql = """
            INSERT INTO complaints (student_id, title, description, category, priority, status,
                location_id, assigned_department_id, attachment_path)
            VALUES (?,?,?,?,?,?,?,?,?)
            """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindComplaint(ps, entity, true);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setComplaintId(keys.getInt(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save complaint", e);
        }
    }

    @Override
    public Complaint update(Complaint entity) {
        String sql = """
            UPDATE complaints SET title=?, description=?, category=?, priority=?, status=?,
                location_id=?, assigned_department_id=?, attachment_path=?, resolved_at=?, resolution_notes=?
            WHERE complaint_id=?
            """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getTitle());
            ps.setString(2, entity.getDescription());
            ps.setString(3, entity.getCategory());
            ps.setString(4, entity.getPriority().name());
            ps.setString(5, entity.getStatus().name());
            setNullableInt(ps, 6, entity.getLocationId());
            setNullableInt(ps, 7, entity.getAssignedDepartmentId());
            ps.setString(8, entity.getAttachmentPath());
            if (entity.getResolvedAt() != null) {
                ps.setTimestamp(9, Timestamp.valueOf(entity.getResolvedAt()));
            } else {
                ps.setNull(9, Types.TIMESTAMP);
            }
            ps.setString(10, entity.getResolutionNotes());
            ps.setInt(11, entity.getComplaintId());
            ps.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update complaint", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM complaints WHERE complaint_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Complaint> findById(Integer id) {
        List<Complaint> list = query(BASE_SELECT + " WHERE c.complaint_id=?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Complaint> findAll() {
        return query(BASE_SELECT + " ORDER BY c.created_at DESC");
    }

    @Override
    public boolean existsById(Integer id) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM complaints WHERE complaint_id=?")) {
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
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM complaints")) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Complaint> findByStudentId(int studentId) {
        return query(BASE_SELECT + " WHERE c.student_id=? ORDER BY c.created_at DESC", studentId);
    }

    @Override
    public List<Complaint> findByDepartmentId(int departmentId) {
        return query(BASE_SELECT + " WHERE c.assigned_department_id=? ORDER BY c.created_at DESC", departmentId);
    }

    @Override
    public List<Complaint> findByStatus(String status) {
        return query(BASE_SELECT + " WHERE c.status=? ORDER BY c.created_at DESC", status);
    }

    @Override
    public List<Complaint> findByPriority(String priority) {
        return query(BASE_SELECT + " WHERE c.priority=? ORDER BY c.created_at DESC", priority);
    }

    @Override
    public List<Complaint> findByLocationId(int locationId) {
        return query(BASE_SELECT + " WHERE c.location_id=? ORDER BY c.created_at DESC", locationId);
    }

    @Override
    public List<Complaint> filterComplaints(Integer studentId, Integer departmentId, String status, String priority) {
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (studentId != null) {
            sql.append(" AND c.student_id=?");
            params.add(studentId);
        }
        if (departmentId != null) {
            sql.append(" AND c.assigned_department_id=?");
            params.add(departmentId);
        }
        if (status != null && !status.isBlank() && !"ALL".equals(status)) {
            sql.append(" AND c.status=?");
            params.add(status);
        }
        if (priority != null && !priority.isBlank() && !"ALL".equals(priority)) {
            sql.append(" AND c.priority=?");
            params.add(priority);
        }
        sql.append(" ORDER BY c.created_at DESC");
        return query(sql.toString(), params.toArray());
    }

    @Override
    public List<Complaint> getPendingComplaints(int departmentId) {
        return query(BASE_SELECT + """
             WHERE c.assigned_department_id=? AND c.status IN ('ASSIGNED','IN_PROGRESS','UNDER_REVIEW')
             ORDER BY c.priority DESC, c.created_at ASC
            """, departmentId);
    }

    @Override
    public long countResolved() {
        return countByStatus("RESOLVED");
    }

    @Override
    public long countPending() {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                     "SELECT COUNT(*) FROM complaints WHERE status NOT IN ('RESOLVED','CLOSED')")) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long countByStatus(String status) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM complaints WHERE status=?")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void bindComplaint(PreparedStatement ps, Complaint entity, boolean isInsert) throws SQLException {
        ps.setInt(1, entity.getStudentId());
        ps.setString(2, entity.getTitle());
        ps.setString(3, entity.getDescription());
        ps.setString(4, entity.getCategory());
        ps.setString(5, entity.getPriority().name());
        ps.setString(6, entity.getStatus().name());
        setNullableInt(ps, 7, entity.getLocationId());
        setNullableInt(ps, 8, entity.getAssignedDepartmentId());
        ps.setString(9, entity.getAttachmentPath());
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value != null) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    private List<Complaint> query(String sql, Object... params) {
        List<Complaint> list = new ArrayList<>();
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

    private Complaint mapRow(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setComplaintId(rs.getInt("complaint_id"));
        c.setStudentId(rs.getInt("student_id"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setCategory(rs.getString("category"));
        c.setPriority(Complaint.ComplaintPriority.valueOf(rs.getString("priority")));
        c.setStatus(Complaint.ComplaintStatus.valueOf(rs.getString("status")));
        int loc = rs.getInt("location_id");
        if (!rs.wasNull()) {
            c.setLocationId(loc);
        }
        int dept = rs.getInt("assigned_department_id");
        if (!rs.wasNull()) {
            c.setAssignedDepartmentId(dept);
        }
        c.setAttachmentPath(rs.getString("attachment_path"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            c.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            c.setUpdatedAt(updated.toLocalDateTime());
        }
        Timestamp resolved = rs.getTimestamp("resolved_at");
        if (resolved != null) {
            c.setResolvedAt(resolved.toLocalDateTime());
        }
        c.setResolutionNotes(rs.getString("resolution_notes"));
        try {
            c.setStudentName(rs.getString("student_name"));
            c.setDepartmentName(rs.getString("department_name"));
        } catch (SQLException ignored) {
            // joined columns optional
        }
        return c;
    }
}
