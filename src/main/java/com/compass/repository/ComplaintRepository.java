package com.compass.repository;

import com.compass.DatabaseManager;
import com.compass.model.Complaint;
import com.compass.model.ComplaintTransfer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComplaintRepository implements Repository<Complaint> {

    private static final String INSERT_SQL = "INSERT INTO complaints (title, description, student_id, department_id, status, priority) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE complaints SET title = ?, description = ?, department_id = ?, status = ?, priority = ? WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM complaints";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM complaints WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM complaints WHERE id = ?";
    private static final String SELECT_BY_STUDENT_SQL = "SELECT * FROM complaints WHERE student_id = ? ORDER BY updated_at DESC";
    private static final String SELECT_BY_STATUS_SQL = "SELECT * FROM complaints WHERE status = ? ORDER BY updated_at DESC";
    private static final String SEARCH_SQL = "SELECT c.* FROM complaints c JOIN departments d ON c.department_id = d.id WHERE c.title LIKE ? OR c.description LIKE ? OR d.name LIKE ?";

    private final Connection connection;

    public ComplaintRepository() throws SQLException {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    protected ComplaintRepository(boolean skipConnection) {
        this.connection = null;
    }

    @Override
    public Optional<Complaint> findById(int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapComplaint(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Complaint> findAll() throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                complaints.add(mapComplaint(resultSet));
            }
        }
        return complaints;
    }

    @Override
    public Complaint save(Complaint complaint) throws SQLException {
        if (complaint.getId() > 0) {
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
                statement.setString(1, complaint.getTitle());
                statement.setString(2, complaint.getDescription());
                if (complaint.getDepartmentId() == null) {
                    statement.setNull(3, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(3, complaint.getDepartmentId());
                }
                statement.setString(4, complaint.getStatus());
                statement.setString(5, complaint.getPriority());
                statement.setInt(6, complaint.getId());
                statement.executeUpdate();
            }
            return complaint;
        }

        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, complaint.getTitle());
            statement.setString(2, complaint.getDescription());
            statement.setInt(3, complaint.getStudentId());
            if (complaint.getDepartmentId() == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, complaint.getDepartmentId());
            }
            statement.setString(5, complaint.getStatus());
            statement.setString(6, complaint.getPriority());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    complaint.setId(generatedKeys.getInt(1));
                }
            }
        }
        return complaint;
    }

    @Override
    public void delete(int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public List<Complaint> findByStudentId(int studentId) throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_STUDENT_SQL)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    complaints.add(mapComplaint(resultSet));
                }
            }
        }
        return complaints;
    }

    public List<Complaint> findByStatus(String status) throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_STATUS_SQL)) {
            statement.setString(1, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    complaints.add(mapComplaint(resultSet));
                }
            }
        }
        return complaints;
    }

    public List<Complaint> searchComplaints(String searchQuery) throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SEARCH_SQL)) {
            String likeQuery = "%" + searchQuery + "%";
            statement.setString(1, likeQuery);
            statement.setString(2, likeQuery);
            statement.setString(3, likeQuery);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    complaints.add(mapComplaint(resultSet));
                }
            }
        }
        return complaints;
    }

    public List<ComplaintTransfer> getComplaintHistory(int complaintId) throws SQLException {
        String sql = "SELECT * FROM complaint_transfers WHERE complaint_id = ? ORDER BY transferred_at DESC";
        List<ComplaintTransfer> history = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, complaintId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ComplaintTransfer transfer = new ComplaintTransfer();
                    transfer.setId(resultSet.getInt("id"));
                    transfer.setComplaintId(resultSet.getInt("complaint_id"));
                    transfer.setFromDepartmentId(resultSet.getObject("from_department_id", Integer.class));
                    transfer.setToDepartmentId(resultSet.getObject("to_department_id", Integer.class));
                    transfer.setTransferReason(resultSet.getString("transfer_reason"));
                    transfer.setTransferredAt(resultSet.getTimestamp("transferred_at").toLocalDateTime());
                    history.add(transfer);
                }
            }
        }
        return history;
    }

    public Complaint transferComplaint(int complaintId, int newDepartmentId, String reason) throws SQLException {
        Optional<Complaint> optional = findById(complaintId);
        if (optional.isEmpty()) {
            throw new SQLException("Complaint not found: " + complaintId);
        }
        Complaint complaint = optional.get();
        Integer oldDepartment = complaint.getDepartmentId();
        complaint.setDepartmentId(newDepartmentId);
        complaint.setStatus("TRANSFERRED");
        save(complaint);

        String transferSql = "INSERT INTO complaint_transfers (complaint_id, from_department_id, to_department_id, transfer_reason) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(transferSql)) {
            statement.setInt(1, complaintId);
            if (oldDepartment == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, oldDepartment);
            }
            statement.setInt(3, newDepartmentId);
            statement.setString(4, reason);
            statement.executeUpdate();
        }

        return complaint;
    }

    private Complaint mapComplaint(ResultSet resultSet) throws SQLException {
        Complaint complaint = new Complaint();
        complaint.setId(resultSet.getInt("id"));
        complaint.setTitle(resultSet.getString("title"));
        complaint.setDescription(resultSet.getString("description"));
        complaint.setStudentId(resultSet.getInt("student_id"));
        complaint.setDepartmentId(resultSet.getObject("department_id", Integer.class));
        complaint.setStatus(resultSet.getString("status"));
        complaint.setPriority(resultSet.getString("priority"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (createdAt != null) {
            complaint.setCreatedAt(createdAt.toLocalDateTime());
        }
        if (updatedAt != null) {
            complaint.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return complaint;
    }
}
