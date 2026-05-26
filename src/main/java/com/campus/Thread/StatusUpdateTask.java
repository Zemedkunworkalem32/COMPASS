package com.campus.thread;

import java.util.List;
import java.util.function.Consumer;

import com.campus.model.Complaint;
import com.campus.service.ComplaintService;

import javafx.concurrent.Task;

public class StatusUpdateTask extends Task<List<Complaint>> {

    private final ComplaintService complaintService;
    private final int studentId;
    private final Consumer<List<Complaint>> onUpdate;
    private int previousCount = -1;

    public StatusUpdateTask(ComplaintService complaintService, int studentId,
                             Consumer<List<Complaint>> onUpdate) {
        this.complaintService = complaintService;
        this.studentId = studentId;
        this.onUpdate = onUpdate;
    }

    @Override
    protected List<Complaint> call() throws Exception {
        while (!isCancelled()) {
            List<Complaint> complaints = complaintService.findStudentComplaints(studentId);
            
            if (hasChanges(complaints)) {
                updateValue(complaints);
                updateMessage("Status updated at " + java.time.LocalTime.now());
                previousCount = complaints.size();
            }
            
            Thread.sleep(30000);
        }
        return null;
    }

    private boolean hasChanges(List<Complaint> current) {
        if (previousCount != current.size()) {
            return true;
        }
        
        for (Complaint complaint : current) {
            if (complaint.getUpdatedAt() != null) {
                // Check if updated in last 30 seconds
                if (complaint.getUpdatedAt().isAfter(java.time.LocalDateTime.now().minusSeconds(31))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void succeeded() {
        if (onUpdate != null && getValue() != null) {
            onUpdate.accept(getValue());
        }
    }
}