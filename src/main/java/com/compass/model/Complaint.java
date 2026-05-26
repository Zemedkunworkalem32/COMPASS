package com.compass.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Complaint Model - Represents a student complaint
 * TEAMMATE 2: Zemedkun
 */
public class Complaint implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int complaintId;
    private int studentId;
    private String title;
    private String description;
    private String category;
    private ComplaintPriority priority;
    private ComplaintStatus status;
    private Integer locationId;
    private Integer assignedDepartmentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    private String attachmentPath;
    private String studentName;
    private String departmentName;

    public enum ComplaintPriority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum ComplaintStatus {
        SUBMITTED, UNDER_REVIEW, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED, TRANSFERRED
    }

    // Constructors
    public Complaint() {}

    public Complaint(int studentId, String title, String description, String category) {
        this.studentId = studentId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = ComplaintPriority.MEDIUM;
        this.status = ComplaintStatus.SUBMITTED;
    }

    // Getters and Setters
    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public ComplaintPriority getPriority() { return priority; }
    public void setPriority(ComplaintPriority priority) { this.priority = priority; }

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    public Integer getLocationId() { return locationId; }
    public void setLocationId(Integer locationId) { this.locationId = locationId; }

    public Integer getAssignedDepartmentId() { return assignedDepartmentId; }
    public void setAssignedDepartmentId(Integer assignedDepartmentId) { this.assignedDepartmentId = assignedDepartmentId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    @Override
    public String toString() {
        return "Complaint{" +
                "complaintId=" + complaintId +
                ", studentId=" + studentId +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", category='" + category + '\'' +
                '}';
    }
}
