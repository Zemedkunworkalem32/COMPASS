package com.compass.controller;

import com.compass.model.Complaint;
import com.compass.service.BackgroundSyncService;
import com.compass.service.ComplaintService;
import com.compass.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class MyComplaintsController {
    @FXML
    public TableView<Complaint> complaintsTable;
    @FXML
    public TableColumn<Complaint, Integer> idColumn;
    @FXML
    public TableColumn<Complaint, String> titleColumn;
    @FXML
    public TableColumn<Complaint, String> statusColumn;
    @FXML
    public TableColumn<Complaint, String> priorityColumn;
    @FXML
    public TableColumn<Complaint, String> updatedAtColumn;
    @FXML
    public Button refreshButton;
    @FXML
    public Label loadingLabel;

    private final ComplaintService complaintService;
    private final BackgroundSyncService syncService;
    private final ObservableList<Complaint> complaints = FXCollections.observableArrayList();

    public MyComplaintsController() throws SQLException {
        this.complaintService = new ComplaintService();
        this.syncService = new BackgroundSyncService();
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        updatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
        complaintsTable.setItems(complaints);
        refreshComplaints();
        syncService.startComplaintRefresh(SessionManager.getCurrentUserId(), complaintService, list -> {
            Platform.runLater(() -> {
                complaints.setAll((List<Complaint>) list);
                loadingLabel.setText("Auto-synced at " + java.time.LocalTime.now());
            });
        }, 30);
    }

    @FXML
    public void handleRefresh() {
        refreshComplaints();
    }

    private void refreshComplaints() {
        loadingLabel.setText("Loading... Please wait.");
        Task<List<Complaint>> refreshTask = complaintService.loadStudentComplaintsTask(SessionManager.getCurrentUserId());
        refreshTask.setOnSucceeded(event -> {
            complaints.setAll(refreshTask.getValue());
            loadingLabel.setText("Last refreshed at " + java.time.LocalTime.now());
        });
        refreshTask.setOnFailed(event -> {
            loadingLabel.setText("Failed to refresh complaints.");
        });
        new Thread(refreshTask).start();
    }
}
