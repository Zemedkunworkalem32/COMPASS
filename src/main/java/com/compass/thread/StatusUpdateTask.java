package com.compass.thread;

import com.compass.model.Complaint;
import com.compass.service.ComplaintService;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Periodically polls the database for updated complaint data and delivers
 * the result back on the JavaFX Application Thread.
 *
 * <pre>
 *   StatusUpdateTask task = new StatusUpdateTask(
 *       service, studentId, updatedList -> table.setItems(...)
 *   );
 *   task.setPeriod(Duration.seconds(30));
 *   task.start();
 * </pre>
 */
public class StatusUpdateTask extends ScheduledService<List<Complaint>> {

    private static final Logger logger = LoggerFactory.getLogger(StatusUpdateTask.class);
    private static final double DEFAULT_PERIOD_SECONDS  = 30;
    private static final double DEFAULT_DELAY_SECONDS   = 30;

    public enum PollMode { ALL, BY_STUDENT, BY_DEPARTMENT }

    private final ComplaintService         complaintService;
    private final PollMode                 mode;
    private final int                      entityId;
    private final Consumer<List<Complaint>> onUpdate;

    public StatusUpdateTask(ComplaintService service,
                            Consumer<List<Complaint>> onUpdate) {
        this(service, PollMode.ALL, 0, onUpdate);
    }

    public StatusUpdateTask(ComplaintService service,
                            PollMode mode, int entityId,
                            Consumer<List<Complaint>> onUpdate) {
        this.complaintService = service;
        this.mode             = mode;
        this.entityId         = entityId;
        this.onUpdate         = onUpdate;

        setPeriod(Duration.seconds(DEFAULT_PERIOD_SECONDS));
        setDelay(Duration.seconds(DEFAULT_DELAY_SECONDS));

        setOnSucceeded(e -> {
            List<Complaint> result = getValue();
            Platform.runLater(() -> onUpdate.accept(result));
        });

        setOnFailed(e ->
            logger.warn("StatusUpdateTask failed: {}", getException().getMessage())
        );
    }

    @Override
    protected Task<List<Complaint>> createTask() {
        return new Task<>() {
            @Override
            protected List<Complaint> call() {
                logger.debug("Polling complaints: mode={}, entityId={}", mode, entityId);
                return switch (mode) {
                    case BY_STUDENT    -> complaintService.getStudentComplaints(entityId);
                    case BY_DEPARTMENT -> complaintService.getDepartmentComplaints(entityId);
                    default            -> complaintService.getAllComplaints();
                };
            }
        };
    }
}
