package com.compass.controller;

import com.compass.model.Complaint;
import com.compass.service.ComplaintService;
import com.compass.util.SceneNavigator;
import com.compass.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StaffDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label workloadLabel;
    @FXML private TableView<Complaint> complaintsTable;
    @FXML private TableColumn<Complaint, String> idCol;
    @FXML private TableColumn<Complaint, String> studentCol;
    @FXML private TableColumn<Complaint, String> titleCol;
    @FXML private TableColumn<Complaint, String> priorityCol;
    @FXML private TableColumn<Complaint, String> statusCol;
    @FXML private TableColumn<Complaint, String> dateCol;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea responseNotes;

    private final ComplaintService complaintService = new ComplaintService();

    @FXML
    private void initialize() {
        SessionManager session = SessionManager.getInstance();
        welcomeLabel.setText("Welcome, " + session.getCurrentUsername());
        for (Complaint.ComplaintStatus s : Complaint.ComplaintStatus.values()) {
            statusCombo.getItems().add(s.name());
        }
        statusCombo.getSelectionModel().select("IN_PROGRESS");
        setupTable();
        refreshTable();
    }

    private void setupTable() {
        idCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getComplaintId())));
        studentCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStudentName() != null ? c.getValue().getStudentName() : "—"));
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        priorityCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPriority().name()));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().toString() : ""));
    }

    private void refreshTable() {
        Integer deptId = SessionManager.getInstance().getCurrentUserDepartmentId();
        if (deptId == null) {
            workloadLabel.setText("No department assigned to your account");
            return;
        }
        var list = complaintService.getDepartmentComplaints(deptId);
        complaintsTable.setItems(FXCollections.observableArrayList(list));
        long pending = list.stream().filter(c -> c.getStatus() != Complaint.ComplaintStatus.RESOLVED
                && c.getStatus() != Complaint.ComplaintStatus.CLOSED).count();
        workloadLabel.setText("Workload: " + pending + " active complaint(s)");
    }

    @FXML
    private void handleUpdateStatus() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Complaint.ComplaintStatus status = Complaint.ComplaintStatus.valueOf(statusCombo.getValue());
        complaintService.updateStatus(selected.getComplaintId(), status, responseNotes.getText());
        refreshTable();
    }

    @FXML
    private void handleResolve() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        complaintService.resolveComplaint(selected.getComplaintId(), responseNotes.getText());
        refreshTable();
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().invalidateSession();
        SceneNavigator.show("/fxml/login.fxml", "Compass - Login", 800, 600);
    }
}
