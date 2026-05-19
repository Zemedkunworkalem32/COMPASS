package com.campus.service;

import com.campus.model.Complaint;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BackgroundSyncService {

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledTask;
    private boolean isRunning = false;

    public BackgroundSyncService() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "BackgroundSyncThread");
            t.setDaemon(true);
            return t;
        });
    }

    public void startComplaintRefresh(int studentId, ComplaintService complaintService, 
                                       Consumer<List<Complaint>> onUpdate, int intervalSeconds) {
        if (isRunning) {
            stop();
        }
        
        isRunning = true;
        scheduledTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                Task<List<Complaint>> refreshTask = new Task<>() {
                    @Override
                    protected List<Complaint> call() throws Exception {
                        return complaintService.findStudentComplaints(studentId);
                    }
                };
                
                refreshTask.setOnSucceeded(event -> {
                    List<Complaint> complaints = refreshTask.getValue();
                    Platform.runLater(() -> onUpdate.accept(complaints));
                });
                
                refreshTask.setOnFailed(event -> {
                    Throwable ex = refreshTask.getException();
                    System.err.println("Background sync failed: " + ex.getMessage());
                });
                
                new Thread(refreshTask).start();
                
            } catch (Exception e) {
                System.err.println("Background sync error: " + e.getMessage());
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    public void startDepartmentComplaintRefresh(int departmentId, ComplaintService complaintService,
                                                  Consumer<List<Complaint>> onUpdate, int intervalSeconds) {
        if (isRunning) {
            stop();
        }
        
        isRunning = true;
        scheduledTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                Task<List<Complaint>> refreshTask = new Task<>() {
                    @Override
                    protected List<Complaint> call() throws Exception {
                        return complaintService.findDepartmentComplaints(departmentId);
                    }
                };
                
                refreshTask.setOnSucceeded(event -> {
                    List<Complaint> complaints = refreshTask.getValue();
                    Platform.runLater(() -> onUpdate.accept(complaints));
                });
                
                new Thread(refreshTask).start();
                
            } catch (Exception e) {
                System.err.println("Department background sync error: " + e.getMessage());
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    public void startCustomRefresh(Runnable refreshTask, int intervalSeconds) {
        if (isRunning) {
            stop();
        }
        
        isRunning = true;
        scheduledTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                Platform.runLater(refreshTask);
            } catch (Exception e) {
                System.err.println("Custom refresh error: " + e.getMessage());
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
        }
        isRunning = false;
    }

    public void shutdown() {
        stop();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}