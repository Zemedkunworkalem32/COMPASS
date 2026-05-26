package com.campus.thread;

import java.util.List;
import java.util.function.Consumer;

import com.campus.model.Complaint;
import com.campus.service.ComplaintService;

import javafx.concurrent.Task;

public class AsyncComplaintLoader extends Task<List<Complaint>> {

    private final ComplaintService complaintService;
    private final int studentId;
    private final Consumer<List<Complaint>> onSuccess;
    private final Consumer<Throwable> onError;

    public AsyncComplaintLoader(ComplaintService complaintService, int studentId,
                                 Consumer<List<Complaint>> onSuccess, Consumer<Throwable> onError) {
        this.complaintService = complaintService;
        this.studentId = studentId;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    @Override
    protected List<Complaint> call() throws Exception {
        updateMessage("Loading complaints...");
        updateProgress(0, 1);
        
        List<Complaint> complaints = complaintService.findStudentComplaints(studentId);
        
        updateProgress(1, 1);
        updateMessage("Loaded " + complaints.size() + " complaints");
        
        return complaints;
    }

    @Override
    protected void succeeded() {
        if (onSuccess != null) {
            onSuccess.accept(getValue());
        }
    }

    @Override
    protected void failed() {
        if (onError != null) {
            onError.accept(getException());
        }
    }
}