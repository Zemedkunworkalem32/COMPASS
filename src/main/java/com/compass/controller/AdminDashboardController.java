package com.compass.controller;

import com.compass.model.Complaint;
import com.compass.model.Department;
import com.compass.service.ComplaintService;
import com.compass.service.DepartmentAnalyticsService;
import com.compass.util.SceneNavigator;
import com.compass.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class AdminDashboardController {
    @FXML private Label totalLabel;
    @FXML private Label resolvedLabel;
    @FXML private Label pendingLabel;
    @FXML private Label analyticsLabel;
    @FXML private Label statusMessage;
    @FXML private ComboBox<Department> deptFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> priorityFilter;
    @FXML private ComboBox<Department> assignDeptCombo;
    @FXML private TableView<Complaint> complaintsTable;
    @FXML private TableColumn<Complaint, String> idCol;
    @FXML private TableColumn<Complaint, String> studentCol;
    @FXML private TableColumn<Complaint, String> titleCol;
    @FXML private TableColumn<Complaint, String> deptCol;
    @FXML private TableColumn<Complaint, String> priorityCol;
    @FXML private TableColumn<Complaint, String> statusCol;
    @FXML private TableColumn<Complaint, String> dateCol;

    private final ComplaintService complaintService = new ComplaintService();
    private final DepartmentAnalyticsService analyticsService = new DepartmentAnalyticsService();
    private ScheduledService<Void> refreshService;

    @FXML
    private void initialize() {
        setupDepartmentCombo(deptFilter, true);
        setupDepartmentCombo(assignDeptCombo, false);

        statusFilter.getItems().addAll("ALL", "SUBMITTED", "UNDER_REVIEW", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED");
        statusFilter.getSelectionModel().select("ALL");

        priorityFilter.getItems().addAll("ALL", "LOW", "MEDIUM", "HIGH", "CRITICAL");
        priorityFilter.getSelectionModel().select("ALL");

        setupTable();
        setupContextMenu();
        setupDoubleClick();
        refreshAll();
        startAutoRefresh();
    }

    private void setupDepartmentCombo(ComboBox<Department> combo, boolean includeAll) {
        if (includeAll) {
            Department all = new Department();
            all.setDepartmentId(-1);
            all.setDepartmentName("All Departments");
            combo.getItems().add(all);
        }
        combo.getItems().addAll(analyticsService.getAllDepartments());
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Department item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDepartmentName());
            }
        });
        combo.setButtonCell(combo.getCellFactory().call(null));
        if (includeAll) {
            combo.getSelectionModel().selectFirst();
        }
    }

    private void setupTable() {
        idCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getComplaintId())));
        studentCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStudentName() != null ? c.getValue().getStudentName() : "—"));
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        deptCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDepartmentName() != null ? c.getValue().getDepartmentName() : "—"));
        priorityCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPriority().name()));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().toString() : ""));
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem viewDetails = new MenuItem("View Details");
        MenuItem markUnderReview = new MenuItem("Mark Under Review");
        MenuItem assignItem = new MenuItem("Assign to Department");
        MenuItem resolveItem = new MenuItem("Mark Resolved");
        MenuItem refreshItem = new MenuItem("Refresh");

        viewDetails.setOnAction(e -> handleViewDetails());
        markUnderReview.setOnAction(e -> handleUnderReview());
        assignItem.setOnAction(e -> handleAssign());
        resolveItem.setOnAction(e -> handleResolve());
        refreshItem.setOnAction(e -> handleRefresh());

        contextMenu.getItems().addAll(viewDetails, new SeparatorMenuItem(),
                markUnderReview, assignItem, resolveItem,
                new SeparatorMenuItem(), refreshItem);
        complaintsTable.setContextMenu(contextMenu);
    }

    private void setupDoubleClick() {
        complaintsTable.setRowFactory(tv -> {
            TableRow<Complaint> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewDetails();
                }
            });
            return row;
        });
    }

    private void startAutoRefresh() {
        refreshService = new ScheduledService<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        Platform.runLater(AdminDashboardController.this::refreshAll);
                        return null;
                    }
                };
            }
        };
        refreshService.setPeriod(javafx.util.Duration.seconds(30));
        refreshService.setDelay(javafx.util.Duration.seconds(30));
        refreshService.start();
    }

    @FXML
    private void handleFilter() {
        refreshComplaints();
    }

    @FXML
    private void handleRefresh() {
        refreshAll();
        showTemporaryMessage("Complaints refreshed", "#38a169");
    }

    @FXML
    private void handleViewDetails() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showTemporaryMessage("Please select a complaint first", "#c53030");
            return;
        }
        showComplaintDetailsDialog(selected);
    }

    @FXML
    private void handleUnderReview() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showTemporaryMessage("Please select a complaint first", "#c53030");
            return;
        }
        try {
            complaintService.markUnderReview(selected.getComplaintId());
            showTemporaryMessage("Complaint #" + selected.getComplaintId() + " marked as UNDER REVIEW", "#38a169");
            refreshAll();
        } catch (Exception e) {
            showTemporaryMessage("Error: " + e.getMessage(), "#c53030");
        }
    }

    @FXML
    private void handleAssign() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        Department dept = assignDeptCombo.getValue();

        if (selected == null) {
            showTemporaryMessage("Please select a complaint first", "#c53030");
            return;
        }
        if (dept == null) {
            showTemporaryMessage("Please select a department to assign to", "#c53030");
            return;
        }
        try {
            complaintService.assignToDepartment(selected.getComplaintId(), dept.getDepartmentId());
            showTemporaryMessage("Complaint #" + selected.getComplaintId() + " assigned to " + dept.getDepartmentName(), "#38a169");
            refreshAll();
        } catch (Exception e) {
            showTemporaryMessage("Error: " + e.getMessage(), "#c53030");
        }
    }

    @FXML
    private void handleResolve() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showTemporaryMessage("Please select a complaint first", "#c53030");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Resolve Complaint");
        confirm.setHeaderText("Resolve Complaint #" + selected.getComplaintId());
        confirm.setContentText("Do you want to mark this complaint as RESOLVED?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                complaintService.resolveComplaint(selected.getComplaintId(), "Resolved by administrator");
                showTemporaryMessage("Complaint #" + selected.getComplaintId() + " resolved", "#38a169");
                refreshAll();
            } catch (Exception e) {
                showTemporaryMessage("Error: " + e.getMessage(), "#c53030");
            }
        }
    }

    @FXML
    private void handleOpenMap() {
        SceneNavigator.show("/fxml/map_view.fxml", "Compass - Campus Map", 1000, 700);
    }

    @FXML
    private void handleLogout() {
        if (refreshService != null) {
            refreshService.cancel();
        }
        SessionManager.getInstance().invalidateSession();
        SceneNavigator.show("/fxml/login.fxml", "Compass - Login", 800, 600);
    }

    private void refreshAll() {
        totalLabel.setText(String.valueOf(analyticsService.getTotalComplaints()));
        resolvedLabel.setText(String.valueOf(analyticsService.getResolvedCount()));
        pendingLabel.setText(String.valueOf(analyticsService.getPendingCount()));

        var best = analyticsService.getBestPerformingDepartment();
        var slow = analyticsService.getSlowestDepartment();
        StringBuilder analytics = new StringBuilder();
        if (best != null) {
            analytics.append("Best: ").append(best.departmentName)
                    .append(String.format(" (%.0f%% resolved). ", best.efficiencyPercent));
        }
        if (slow != null) {
            analytics.append("Slowest: ").append(slow.departmentName)
                    .append(String.format(" (%.1f hrs avg).", slow.averageResponseHours));
        }
        analyticsLabel.setText(analytics.length() > 0 ? analytics.toString() : "No analytics yet");
        refreshComplaints();
    }

    private void refreshComplaints() {
        Department dept = deptFilter.getValue();
        Integer deptId = (dept != null && dept.getDepartmentId() > 0) ? dept.getDepartmentId() : null;
        String status = statusFilter.getValue();
        String priority = priorityFilter.getValue();

        var complaints = complaintService.filter(deptId, status, priority);
        complaintsTable.setItems(FXCollections.observableArrayList(complaints));
    }

    private void showComplaintDetailsDialog(Complaint complaint) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Complaint Details - #" + complaint.getComplaintId());
        dialog.setHeaderText(complaint.getTitle());

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label studentInfo = new Label("Student: " + (complaint.getStudentName() != null ? complaint.getStudentName() : "Unknown"));
        Label deptInfo = new Label("Department: " + (complaint.getDepartmentName() != null ? complaint.getDepartmentName() : "Not assigned"));
        Label priorityInfo = new Label("Priority: " + complaint.getPriority().name());
        Label statusInfo = new Label("Status: " + complaint.getStatus().name());
        Label dateInfo = new Label("Created: " + (complaint.getCreatedAt() != null ? complaint.getCreatedAt() : "Unknown"));

        Separator separator = new Separator();
        Label descLabel = new Label("Description:");
        TextArea descriptionArea = new TextArea(complaint.getDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(6);

        content.getChildren().addAll(
                studentInfo, deptInfo, priorityInfo, statusInfo, dateInfo,
                separator, descLabel, descriptionArea
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private void showTemporaryMessage(String message, String color) {
        statusMessage.setText(message);
        statusMessage.setStyle("-fx-text-fill: " + color + ";");
        statusMessage.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> {
                    statusMessage.setText("");
                    statusMessage.setStyle("");
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}