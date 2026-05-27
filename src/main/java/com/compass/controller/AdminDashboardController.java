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
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

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
    }

    @FXML
    private void handleUnderReview() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusMessage.setText("Select a complaint first");
            return;
        }
        complaintService.markUnderReview(selected.getComplaintId());
        statusMessage.setText("Complaint #" + selected.getComplaintId() + " marked UNDER REVIEW");
        refreshAll();
    }

    @FXML
    private void handleAssign() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        Department dept = assignDeptCombo.getValue();
        if (selected == null || dept == null) {
            statusMessage.setText("Select complaint and department");
            return;
        }
        complaintService.assignToDepartment(selected.getComplaintId(), dept.getDepartmentId());
        statusMessage.setText("Assigned complaint #" + selected.getComplaintId() + " to " + dept.getDepartmentName());
        refreshAll();
    }

    @FXML
    private void handleResolve() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusMessage.setText("Select a complaint first");
            return;
        }
        complaintService.resolveComplaint(selected.getComplaintId(), "Resolved by administrator");
        statusMessage.setText("Complaint #" + selected.getComplaintId() + " resolved");
        refreshAll();
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
            analytics.append("Slowest response: ").append(slow.departmentName)
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
        complaintsTable.setItems(FXCollections.observableArrayList(
                complaintService.filter(deptId, status, priority)));
    }
}
