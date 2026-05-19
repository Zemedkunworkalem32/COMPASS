package com.compass.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class ComplaintTransfer {
    private int id;
    private int complaintId;
    private Integer fromDepartmentId;
    private Integer toDepartmentId;
    private String transferReason;
    private LocalDateTime transferredAt;

    public ComplaintTransfer() {
    }

    public ComplaintTransfer(int id, int complaintId, Integer fromDepartmentId, Integer toDepartmentId, String transferReason, LocalDateTime transferredAt) {
        this.id = id;
        this.complaintId = complaintId;
        this.fromDepartmentId = fromDepartmentId;
        this.toDepartmentId = toDepartmentId;
        this.transferReason = transferReason;
        this.transferredAt = transferredAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(int complaintId) {
        this.complaintId = complaintId;
    }

    public Integer getFromDepartmentId() {
        return fromDepartmentId;
    }

    public void setFromDepartmentId(Integer fromDepartmentId) {
        this.fromDepartmentId = fromDepartmentId;
    }

    public Integer getToDepartmentId() {
        return toDepartmentId;
    }

    public void setToDepartmentId(Integer toDepartmentId) {
        this.toDepartmentId = toDepartmentId;
    }

    public String getTransferReason() {
        return transferReason;
    }

    public void setTransferReason(String transferReason) {
        this.transferReason = transferReason;
    }

    public LocalDateTime getTransferredAt() {
        return transferredAt;
    }

    public void setTransferredAt(LocalDateTime transferredAt) {
        this.transferredAt = transferredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComplaintTransfer)) return false;
        ComplaintTransfer that = (ComplaintTransfer) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
