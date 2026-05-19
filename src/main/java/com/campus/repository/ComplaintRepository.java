package com.campus.repository;

import com.campus.model.Complaint;
import com.campus.model.ComplaintTransfer;
import com.campus.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComplaintRepository implements Repository<Complaint> {

    private static final String INSERT_SQL = 
        "INSERT INTO complaints (title, description, student_id, department_id, status, priority, attachment_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE complaints SET title = ?, description = ?, department_id = ?, status = ?, priority = ?, attachment_path = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT c.*, u.name as student_name, d.department_name FROM complaints c " +
        "JOIN users u ON c.student_id = u.id " +
        "LEFT JOIN departments d ON c.department_id = d.id ORDER BY c.created_at DESC";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT c.*, u.name as student_name, d.department_name FROM complaints c " +
        "JOIN users u ON c.student_id = u.id " +
        "LEFT JOIN departments d ON c.department_id = d.id WHERE c.id = ?";
    
    private static final String SELECT_BY_STUDENT_SQL = 
        "SELECT c.*, d.department_name FROM complaints c " +
        "LEFT JOIN departments d ON c.department_id = d.id " +
        "WHERE c.student_id = ? ORDER BY c.created_at DESC";
    
    private static final String SELECT_BY_DEPARTMENT_SQL = 
        "SELECT c.*, u.name as student_name FROM complaints c " +
        "JOIN users u ON c.student_id = u.id " +
        "WHERE c.department_id = ? ORDER BY c.created_at DESC";
    
    private static final String SELECT_BY_STATUS_SQL = 
        "SELECT c.*, u.name as student_name, d.department_name FROM complaints c " +
        "JOIN users u ON c.student_id = u.id " +
        "LEFT JOIN departments d ON c.department_id = d.id " +
        "WHERE c.status = ? ORDER BY c.created_at DESC";
    
    private static final String SEARCH_SQL = 
        "SELECT c.*, u.name as student_name, d.department_name FROM complaints c " +
        "JOIN users u ON c.student_id = u.id " +
        "LEFT JOIN departments d ON c.department_id = d.id " +
        "WHERE c.title LIKE ? OR c.description LIKE ? OR d.department_name LIKE ?";
    
    private static final String UPDATE_STATUS_SQL = 
        "UPDATE complaints SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    
    private static final String UPDATE_RESOLVED_SQL = 
        "UPDATE complaints SET status = 'RESOLVED', resolved_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    
    private static final String UPDATE_FIRST_RESPONSE_SQL = 
        "UPDATE complaints SET first_response_at = COALESCE(first_response_at, CURRENT_TIMESTAMP) WHERE id = ?";
    
    private static final String TRANSFER_INSERT_SQL = 
        "INSERT INTO complaint_transfers (complaint_id, from_department_id, to_department_id, transfer_reason) VALUES (?, ?, ?, ?)";
    
    private static final String TRANSFER_HISTORY_SQL = 
        "SELECT * FROM complaint_transfers WHERE complaint_id = ? ORDER BY transferred_at DESC";

    private final Connection connection;

    public ComplaintRepository() throws SQLException {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    @Override
    public Optional<Complaint> findById(int id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapComplaint(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Complaint> findAll() throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            while (rs.next()) {
                complaints.add(mapComplaint(rs));
            }
        }
        return complaints;
    }

    @Override
    public Complaint save(Complaint complaint) throws SQLException {
        if (complaint.getId() > 0) {
            return update(complaint);
        }
        return insert(complaint);
    }

    private Complaint insert(Complaint complaint) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, complaint.getTitle());
            stmt.setString(2, complaint.getDescription());
            stmt.setInt(3, complaint.getStudentId());
            setNullableInt(stmt, 4, complaint.getDepartmentId());
            stmt.setString(5, complaint.getStatus() != null ? complaint.getStatus() : "PENDING");
            stmt.setString(6, complaint.getPriority() != null ? complaint.getPriority() : "MEDIUM");
            stmt.setString(7, complaint.getAttachmentPath());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    complaint.setId(generatedKeys.getInt(1));
                }
            }
        }
        return complaint;
    }

    private Complaint update(Complaint complaint) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, complaint.getTitle());
            stmt.setString(2, complaint.getDescription());
            setNullableInt(stmt, 3, complaint.getDepartmentId());
            stmt.setString(4, complaint.getStatus());
            stmt.setString(5, complaint.getPriority());
            stmt.setString(6, complaint.getAttachmentPath());
            stmt.setInt(7, complaint.getId());
            
            stmt.executeUpdate();
        }
        return complaint;
    }

    @Override
    public void delete(int id) throws SQLException {
        String deleteSql = "DELETE FROM complaints WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Complaint> findByStudentId(int studentId) throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_STUDENT_SQL)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    complaints.add(mapSimpleComplaint(rs));
                }
            }
        }
        return complaints;
    }

    public List<Complaint> findByDepartmentId(int departmentId) throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_DEPARTMENT_SQL)) {
            stmt.setInt(1, departmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Complaint complaint = mapSimpleComplaint(rs);
                    complaint.setStudentName(rs.getString("student_name"));
                    complaints.add(complaint);
                }
            }
        }
        return complaints;
    }

    public List<Complaint> findByStatus(String status) throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_STATUS_SQL)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    complaints.add(mapComplaint(rs));
                }
            }
        }
        return complaints;
    }

    public List<Complaint> searchComplaints(String query) throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        String likeQuery = "%" + query + "%";
        try (PreparedStatement stmt = connection.prepareStatement(SEARCH_SQL)) {
            stmt.setString(1, likeQuery);
            stmt.setString(2, likeQuery);
            stmt.setString(3, likeQuery);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    complaints.add(mapComplaint(rs));
                }
            }
        }
        return complaints;
    }

    public List<Complaint> filterComplaints(String department, String status, String priority, String dateFrom) throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT c.*, u.name as student_name, d.department_name FROM complaints c " +
            "JOIN users u ON c.student_id = u.id " +
            "LEFT JOIN departments d ON c.department_id = d.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (department != null && !department.isEmpty()) {
            sql.append(" AND d.department_name = ?");
            params.add(department);
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND c.status = ?");
            params.add(status);
        }
        if (priority != null && !priority.isEmpty()) {
            sql.append(" AND c.priority = ?");
            params.add(priority);
        }
        if (dateFrom != null && !dateFrom.isEmpty()) {
            sql.append(" AND DATE(c.created_at) >= ?");
            params.add(dateFrom);
        }
        sql.append(" ORDER BY c.created_at DESC");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    complaints.add(mapComplaint(rs));
                }
            }
        }
        return complaints;
    }

    public boolean updateStatus(int complaintId, String status) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_STATUS_SQL)) {
            stmt.setString(1, status);
            stmt.setInt(2, complaintId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean markAsResolved(int complaintId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_RESOLVED_SQL)) {
            stmt.setInt(1, complaintId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean markFirstResponse(int complaintId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_FIRST_RESPONSE_SQL)) {
            stmt.setInt(1, complaintId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Complaint transferComplaint(int complaintId, int toDepartmentId, String reason) throws SQLException {
        Optional<Complaint> optional = findById(complaintId);
        if (optional.isEmpty()) {
            throw new SQLException("Complaint not found: " + complaintId);
        }
        
        Complaint complaint = optional.get();
        Integer fromDepartmentId = complaint.getDepartmentId();
        
        complaint.setDepartmentId(toDepartmentId);
        complaint.setStatus("ASSIGNED");
        update(complaint);
        
        try (PreparedStatement stmt = connection.prepareStatement(TRANSFER_INSERT_SQL)) {
            stmt.setInt(1, complaintId);
            setNullableInt(stmt, 2, fromDepartmentId);
            stmt.setInt(3, toDepartmentId);
            stmt.setString(4, reason);
            stmt.executeUpdate();
        }
        
        return complaint;
    }

    public List<ComplaintTransfer> getTransferHistory(int complaintId) throws SQLException {
        List<ComplaintTransfer> history = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(TRANSFER_HISTORY_SQL)) {
            stmt.setInt(1, complaintId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ComplaintTransfer transfer = new ComplaintTransfer();
                    transfer.setId(rs.getInt("id"));
                    transfer.setComplaintId(rs.getInt("complaint_id"));
                    transfer.setFromDepartmentId(getNullableInt(rs, "from_department_id"));
                    transfer.setToDepartmentId(getNullableInt(rs, "to_department_id"));
                    transfer.setTransferReason(rs.getString("transfer_reason"));
                    transfer.setTransferredAt(rs.getTimestamp("transferred_at").toLocalDateTime());
                    history.add(transfer);
                }
            }
        }
        return history;
    }

    public long countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM complaints WHERE status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    public long countByDepartmentAndStatus(int departmentId, String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM complaints WHERE department_id = ? AND status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, departmentId);
            stmt.setString(2, status);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    private Complaint mapComplaint(ResultSet rs) throws SQLException {
        Complaint complaint = new Complaint();
        complaint.setId(rs.getInt("id"));
        complaint.setTitle(rs.getString("title"));
        complaint.setDescription(rs.getString("description"));
        complaint.setStudentId(rs.getInt("student_id"));
        complaint.setDepartmentId(getNullableInt(rs, "department_id"));
        complaint.setStatus(rs.getString("status"));
        complaint.setPriority(rs.getString("priority"));
        complaint.setAttachmentPath(rs.getString("attachment_path"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        Timestamp resolvedAt = rs.getTimestamp("resolved_at");
        Timestamp firstResponseAt = rs.getTimestamp("first_response_at");
        
        if (createdAt != null) complaint.setCreatedAt(createdAt.toLocalDateTime());
        if (updatedAt != null) complaint.setUpdatedAt(updatedAt.toLocalDateTime());
        if (resolvedAt != null) complaint.setResolvedAt(resolvedAt.toLocalDateTime());
        if (firstResponseAt != null) complaint.setFirstResponseAt(firstResponseAt.toLocalDateTime());
        
        try {
            complaint.setStudentName(rs.getString("student_name"));
        } catch (SQLException ignored) {}
        try {
            complaint.setDepartmentName(rs.getString("department_name"));
        } catch (SQLException ignored) {}
        
        return complaint;
    }

    private Complaint mapSimpleComplaint(ResultSet rs) throws SQLException {
        Complaint complaint = new Complaint();
        complaint.setId(rs.getInt("id"));
        complaint.setTitle(rs.getString("title"));
        complaint.setDescription(rs.getString("description"));
        complaint.setStudentId(rs.getInt("student_id"));
        complaint.setDepartmentId(getNullableInt(rs, "department_id"));
        complaint.setStatus(rs.getString("status"));
        complaint.setPriority(rs.getString("priority"));
        complaint.setAttachmentPath(rs.getString("attachment_path"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        Timestamp resolvedAt = rs.getTimestamp("resolved_at");
        Timestamp firstResponseAt = rs.getTimestamp("first_response_at");
        
        if (createdAt != null) complaint.setCreatedAt(createdAt.toLocalDateTime());
        if (updatedAt != null) complaint.setUpdatedAt(updatedAt.toLocalDateTime());
        if (resolvedAt != null) complaint.setResolvedAt(resolvedAt.toLocalDateTime());
        if (firstResponseAt != null) complaint.setFirstResponseAt(firstResponseAt.toLocalDateTime());
        
        return complaint;
    }

    private void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setInt(index, value);
        }
    }

    private Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
}