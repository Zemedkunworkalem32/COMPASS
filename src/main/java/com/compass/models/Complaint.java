package com.compass.models;

import java.time.LocalDateTime;

public class Complaint {
    private int complaintId;
    private int studentId;
    private String studentName;
    private String title;
    private String description;
    private String category;
    private ComplaintStatus status = ComplaintStatus.PENDING;
    private String attachmentPath;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public enum ComplaintStatus {
        PENDING("Pending"),
        IN_PROGRESS("In Progress"),
        RESOLVED("Resolved");

        private final String label;

        ComplaintStatus(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public static ComplaintStatus fromLabel(String label) {
            for (ComplaintStatus status : values()) {
                if (status.label.equals(label)) {
                    return status;
                }
            }
            return PENDING;
        }
    }

    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }
    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
