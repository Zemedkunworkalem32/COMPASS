package com.campus.controller;

import com.campus.model.Complaint;
import com.campus.service.BackgroundSyncService;
import com.campus.service.ComplaintService;
import com.campus.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyComplaintsController {

    @FXML private TableView<Complaint> complaintsTable;
    @FXML private TableColumn<Complaint, Integer> idColumn;
    @FXML private TableColumn<Complaint, String> titleColumn;
    @FXML private TableColumn<Complaint, String> departmentColumn;
    @FXML private TableColumn<Complaint, String> statusColumn;
    @FXML private TableColumn<Complaint, String> priorityColumn;
    @FXML private TableColumn<Complaint, String> updatedAtColumn;
    @FXML private Button refreshButton;
    @FXML private Label loadingLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label totalCountLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label resolvedCountLabel;

    private ComplaintService complaintService;
    private BackgroundSyncService syncService;
    private final ObservableList<Complaint> complaints = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        
        try {
            complaintService = new ComplaintService();
            syncService = new BackgroundSyncService();
        } catch (SQLException e) {
            loadingLabel.setText("Database error: " + e.getMessage());
        }
        
        complaintsTable.setItems(complaints);
        refreshComplaints();
        startAutoRefresh();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        updatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
        
        statusColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "RESOLVED":
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case "PENDING":
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                        case "IN_PROGRESS":
                            setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: gray;");
                    }
                }
            }
        });
        
        complaintsTable.setRowFactory(tv -> {
            TableRow<Complaint> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showComplaintDetails(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("ALL", "PENDING", "UNDER_REVIEW", "ASSIGNED", "IN_PROGRESS", "RESOLVED"));
        statusFilter.setValue("ALL");
        statusFilter.setOnAction(e -> filterComplaints());
        
        searchField.textProperty().addListener((obs, old, val) -> filterComplaints());
    }

    private void startAutoRefresh() {
        if (complaintService != null && SessionManager.isAuthenticated()) {
            syncService.startComplaintRefresh(
                SessionManager.getCurrentUserId(),
                complaintService,
                this::updateComplaintsList,
                30
            );
        }
    }

    private void updateComplaintsList(List<Complaint> newComplaints) {
        Platform.runLater(() -> {
            complaints.setAll(newComplaints);
            filterComplaints();
            updateStats();
            loadingLabel.setText("Updated: " + 
                java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
    }

    @FXML
    public void handleRefresh() {
        refreshComplaints();
    }

    private void refreshComplaints() {
        if (complaintService == null) {
            loadingLabel.setText("Service not initialized");
            return;
        }
        
        loadingLabel.setText("Loading...");
        refreshButton.setDisable(true);
        
        Task<List<Complaint>> refreshTask = complaintService.loadStudentComplaintsTask(
            SessionManager.getCurrentUserId()
        );
        
        refreshTask.setOnSucceeded(event -> {
            complaints.setAll(refreshTask.getValue());
            filterComplaints();
            updateStats();
            loadingLabel.setText("Loaded " + complaints.size() + " complaints");
            refreshButton.setDisable(false);
        });
        
        refreshTask.setOnFailed(event -> {
            loadingLabel.setText("Failed to load");
            refreshButton.setDisable(false);
        });
        
        new Thread(refreshTask).start();
    }

    private void filterComplaints() {
        String searchText = searchField.getText().toLowerCase();
        String status = statusFilter.getValue();
        
        ObservableList<Complaint> filtered = FXCollections.observableArrayList();
        
        for (Complaint complaint : complaints) {
            boolean matchesSearch = searchText.isEmpty() || 
                complaint.getTitle().toLowerCase().contains(searchText);
            
            boolean matchesStatus = "ALL".equals(status) || complaint.getStatus().equals(status);
            
            if (matchesSearch && matchesStatus) {
                filtered.add(complaint);
            }
        }
        
        complaintsTable.setItems(filtered);
        totalCountLabel.setText(String.valueOf(filtered.size()));
    }

    private void updateStats() {
        long pending = complaints.stream()
            .filter(c -> !"RESOLVED".equals(c.getStatus()))
            .count();
        long resolved = complaints.stream()
            .filter(c -> "RESOLVED".equals(c.getStatus()))
            .count();
        
        pendingCountLabel.setText(String.valueOf(pending));
        resolvedCountLabel.setText(String.valueOf(resolved));
    }

    private void showComplaintDetails(Complaint complaint) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Complaint #" + complaint.getId());
        dialog.setHeaderText(complaint.getTitle());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        TextArea descriptionArea = new TextArea(complaint.getDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(6);
        
        Label statusLabel = new Label("Status: " + complaint.getStatus());
        Label priorityLabel = new Label("Priority: " + complaint.getPriority());
        Label deptLabel = new Label("Department: " + 
            (complaint.getDepartmentName() != null ? complaint.getDepartmentName() : "Not assigned"));
        Label createdLabel = new Label("Created: " + 
            (complaint.getCreatedAt() != null ? complaint.getCreatedAt().format(formatter) : "N/A"));
        
        content.getChildren().addAll(descriptionArea, statusLabel, priorityLabel, deptLabel, createdLabel);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    public void shutdown() {
        if (syncService != null) {
            syncService.shutdown();
        }
    }
}