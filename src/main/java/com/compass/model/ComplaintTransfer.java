package com.compass.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ComplaintTransfer Model - Tracks complaint reassignment between departments.
 */
public class ComplaintTransfer implements Serializable {
    private static final long serialVersionUID = 1L;

    private int transferId;
    private int complaintId;
    private Integer fromDepartmentId;
    private int toDepartmentId;
    private String reason;
    private int transferredBy;
    private LocalDateTime transferredAt;

    // Denormalised display fields (populated by JOIN queries)
    private String fromDepartmentName;
    private String toDepartmentName;
    private String transferredByUsername;

    public ComplaintTransfer() {}

    public ComplaintTransfer(int complaintId, Integer fromDepartmentId,
                             int toDepartmentId, String reason, int transferredBy) {
        this.complaintId      = complaintId;
        this.fromDepartmentId = fromDepartmentId;
        this.toDepartmentId   = toDepartmentId;
        this.reason           = reason;
        this.transferredBy    = transferredBy;
    }

    // Getters / Setters
    public int getTransferId()                         { return transferId; }
    public void setTransferId(int transferId)          { this.transferId = transferId; }

    public int getComplaintId()                        { return complaintId; }
    public void setComplaintId(int complaintId)        { this.complaintId = complaintId; }

    public Integer getFromDepartmentId()               { return fromDepartmentId; }
    public void setFromDepartmentId(Integer id)        { this.fromDepartmentId = id; }

    public int getToDepartmentId()                     { return toDepartmentId; }
    public void setToDepartmentId(int id)              { this.toDepartmentId = id; }

    public String getReason()                          { return reason; }
    public void setReason(String reason)               { this.reason = reason; }

    public int getTransferredBy()                      { return transferredBy; }
    public void setTransferredBy(int transferredBy)    { this.transferredBy = transferredBy; }

    public LocalDateTime getTransferredAt()            { return transferredAt; }
    public void setTransferredAt(LocalDateTime t)      { this.transferredAt = t; }

    public String getFromDepartmentName()              { return fromDepartmentName; }
    public void setFromDepartmentName(String n)        { this.fromDepartmentName = n; }

    public String getToDepartmentName()                { return toDepartmentName; }
    public void setToDepartmentName(String n)          { this.toDepartmentName = n; }

    public String getTransferredByUsername()           { return transferredByUsername; }
    public void setTransferredByUsername(String u)     { this.transferredByUsername = u; }

    @Override
    public String toString() {
        return "ComplaintTransfer{" +
                "transferId=" + transferId +
                ", complaintId=" + complaintId +
                ", from=" + fromDepartmentId +
                ", to=" + toDepartmentId +
                ", reason='" + reason + '\'' +
                '}';
    }
}
