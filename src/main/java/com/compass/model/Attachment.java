package com.compass.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Attachment Model - Represents file attachments to complaints
 * TEAMMATE 2: Zemedkun
 */
public class Attachment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int attachmentId;
    private int complaintId;
    private String fileName;
    private String filePath;
    private String fileType;
    private long fileSize;
    private int uploadedBy;
    private LocalDateTime uploadedAt;

    // Constructors
    public Attachment() {}

    public Attachment(int complaintId, String fileName, String filePath) {
        this.complaintId = complaintId;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    // Getters and Setters
    public int getAttachmentId() { return attachmentId; }
    public void setAttachmentId(int attachmentId) { this.attachmentId = attachmentId; }

    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public int getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(int uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    @Override
    public String toString() {
        return "Attachment{" +
                "attachmentId=" + attachmentId +
                ", complaintId=" + complaintId +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }
}
