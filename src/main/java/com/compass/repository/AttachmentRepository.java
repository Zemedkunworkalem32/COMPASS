package com.compass.repository;

import com.compass.DatabaseManager;
import com.compass.model.Attachment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;

public class AttachmentRepository {
    private static final String INSERT_SQL =
            "INSERT INTO attachments (complaint_id, file_name, file_type, file_size, file_path, uploaded_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private final Connection connection;

    public AttachmentRepository() throws SQLException {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public AttachmentRepository(Connection connection) {
        this.connection = connection;
    }

    public void saveAll(int complaintId, Iterable<Attachment> attachments) throws SQLException {
        if (attachments == null) {
            return;
        }
        for (Attachment attachment : attachments) {
            saveOne(complaintId, attachment);
        }
    }

    public Attachment saveOne(int complaintId, Attachment attachment) throws SQLException {
        if (attachment == null) {
            return null;
        }

        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setInt(1, complaintId);
            statement.setString(2, attachment.getFileName());
            statement.setString(3, attachment.getFileType());
            statement.setLong(4, attachment.getFileSize());
            statement.setString(5, attachment.getFilePath());

            LocalDateTime uploadedAt = attachment.getUploadedAt();
            if (uploadedAt == null) {
                statement.setNull(6, Types.TIMESTAMP);
            } else {
                statement.setObject(6, uploadedAt);
            }

            statement.executeUpdate();
        }

        return attachment;
    }
}


