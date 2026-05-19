package com.compass.service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javafx.concurrent.Task;

public class BackgroundSyncService {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void startComplaintRefresh(int studentId, ComplaintService complaintService, Consumer<List<com.compass.model.Complaint>> callback, int intervalSeconds) {
        scheduler.scheduleAtFixedRate(() -> {
            Task<List<com.compass.model.Complaint>> refreshTask = complaintService.loadStudentComplaintsTask(studentId);
            refreshTask.setOnSucceeded(event -> callback.accept(refreshTask.getValue()));
            refreshTask.setOnFailed(event -> {
                // logging or error handling can be added here
            });
            new Thread(refreshTask).start();
        }, 0, intervalSeconds, TimeUnit.SECONDS);
    }


    public void stop() {
        scheduler.shutdownNow();
    }
}
