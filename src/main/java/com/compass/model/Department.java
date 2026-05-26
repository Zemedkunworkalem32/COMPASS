package com.compass.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Department Model - Represents a campus department
 * TEAMMATE 1: Abdissa
 */
public class Department implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int departmentId;
    private String departmentName;
    private String description;
    private String email;
    private String phone;
    private int responseTimeHours;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Department() {}

    public Department(String departmentName, String email) {
        this.departmentName = departmentName;
        this.email = email;
        this.responseTimeHours = 24;
        this.isActive = true;
    }

    // Getters and Setters
    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getResponseTimeHours() { return responseTimeHours; }
    public void setResponseTimeHours(int responseTimeHours) { this.responseTimeHours = responseTimeHours; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Department{" +
                "departmentId=" + departmentId +
                ", departmentName='" + departmentName + '\'' +
                ", email='" + email + '\'' +
                ", responseTimeHours=" + responseTimeHours +
                '}';
    }
}
