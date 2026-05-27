package com.compass.thread;

import com.compass.model.Complaint;
import com.compass.service.ComplaintService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Loads complaints from the database on a background thread so the
 * JavaFX Application Thread is never blocked.
 *
 * <pre>
 *   AsyncComplaintLoader loader = new AsyncComplaintLoader(
 *       complaintService, studentId,
 *       items -> complaintsTable.setItems(items),
 *       err  -> showError(err.getMessage())
 *   );
 *   new Thread(loader).start();
 * </pre>
 */
public class AsyncComplaintLoader extends Task<ObservableList<Complaint>> {

    private static final Logger logger = LoggerFactory.getLogger(AsyncComplaintLoader.class);

    public enum LoadMode { ALL, BY_STUDENT, BY_DEPARTMENT }

    private final ComplaintService complaintService;
    private final LoadMode mode;
    private final int entityId;                       // studentId or departmentId
    private final Consumer<ObservableList<Complaint>> onSuccess;
    private final Consumer<Throwable>                 onFailure;

    /**
     * Load all complaints (admin use).
     */
    public AsyncComplaintLoader(ComplaintService service,
                                Consumer<ObservableList<Complaint>> onSuccess,
                                Consumer<Throwable> onFailure) {
        this(service, LoadMode.ALL, 0, onSuccess, onFailure);
    }

    /**
     * Load complaints filtered by student or department ID.
     */
    public AsyncComplaintLoader(ComplaintService service,
                                LoadMode mode, int entityId,
                                Consumer<ObservableList<Complaint>> onSuccess,
                                Consumer<Throwable> onFailure) {
        this.complaintService = service;
        this.mode             = mode;
        this.entityId         = entityId;
        this.onSuccess        = onSuccess;
        this.onFailure        = onFailure;
        wireCallbacks();
    }

    private void wireCallbacks() {
        setOnSucceeded(e -> onSuccess.accept(getValue()));
        setOnFailed(e   -> {
            logger.error("AsyncComplaintLoader failed", getException());
            if (onFailure != null) {
                onFailure.accept(getException());
            }
        });
    }

    @Override
    protected ObservableList<Complaint> call() {
        logger.debug("Loading complaints: mode={}, entityId={}", mode, entityId);
        List<Complaint> list = switch (mode) {
            case BY_STUDENT    -> complaintService.getStudentComplaints(entityId);
            case BY_DEPARTMENT -> complaintService.getDepartmentComplaints(entityId);
            default            -> complaintService.getAllComplaints();
        };
        return FXCollections.observableArrayList(list);
    }
}
