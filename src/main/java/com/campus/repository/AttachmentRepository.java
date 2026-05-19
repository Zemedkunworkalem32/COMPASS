package com.campus.repository;

import com.campus.model.Attachment;
import com.campus.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttachmentRepository {

    private static final String INSERT_SQL = 
        "INSERT INTO attachments (complaint_id, file_name, file_type, file_size, file_path) VALUES (?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_COMPLAINT_SQL = 
        "SELECT * FROM attachments WHERE complaint_id = ? ORDER BY uploaded_at DESC";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT * FROM attachments WHERE id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM attachments WHERE id = ?";
    
    private static final String DELETE_BY_COMPLAINT_SQL = 
        "DELETE FROM attachments WHERE complaint_id = ?";

    private final Connection connection;

    public AttachmentRepository() throws SQLException {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public Attachment save(Attachment attachment) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, attachment.getComplaintId());
            stmt.setString(2, attachment.getFileName());
            stmt.setString(3, attachment.getFileType());
            stmt.setLong(4, attachment.getFileSize());
            stmt.setString(5, attachment.getFilePath());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    attachment.setId(generatedKeys.getInt(1));
                }
            }
        }
        return attachment;
    }

    public List<Attachment> saveAll(int complaintId, List<Attachment> attachments) throws SQLException {
        List<Attachment> saved = new ArrayList<>();
        for (Attachment attachment : attachments) {
            attachment.setComplaintId(complaintId);
            saved.add(save(attachment));
        }
        return saved;
    }

    public List<Attachment> findByComplaintId(int complaintId) throws SQLException {
        List<Attachment> attachments = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_COMPLAINT_SQL)) {
            stmt.setInt(1, complaintId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attachments.add(mapAttachment(rs));
                }
            }
        }
        return attachments;
    }

    public Optional<Attachment> findById(int id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapAttachment(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean delete(int id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteByComplaintId(int complaintId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_BY_COMPLAINT_SQL)) {
            stmt.setInt(1, complaintId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Attachment mapAttachment(ResultSet rs) throws SQLException {
        Attachment attachment = new Attachment();
        attachment.setId(rs.getInt("id"));
        attachment.setComplaintId(rs.getInt("complaint_id"));
        attachment.setFileName(rs.getString("file_name"));
        attachment.setFileType(rs.getString("file_type"));
        attachment.setFileSize(rs.getLong("file_size"));
        attachment.setFilePath(rs.getString("file_path"));
        
        Timestamp uploadedAt = rs.getTimestamp("uploaded_at");
        if (uploadedAt != null) {
            attachment.setUploadedAt(uploadedAt.toLocalDateTime());
        }
        
        return attachment;
    }
}