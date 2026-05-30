package com.compass.controllers;

import com.compass.config.Session;
import com.compass.config.View;
import com.compass.models.CompassData;
import com.compass.models.Complaint;
import com.compass.models.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Map;

public class AdminDashboardController {
    @FXML private Label headerLabel;
    @FXML private Label totalLabel;
    @FXML private Label pendingLabel;
    @FXML private Label inProgressLabel;
    @FXML private Label resolvedLabel;
    @FXML private Label categoryAnalyticsLabel;
    @FXML private Label statusMessage;

    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea adminNoteArea;

    @FXML private TableView<Complaint> complaintsTable;
    @FXML private TableColumn<Complaint, String> idCol;
    @FXML private TableColumn<Complaint, String> studentCol;
    @FXML private TableColumn<Complaint, String> titleCol;
    @FXML private TableColumn<Complaint, String> categoryCol;
    @FXML private TableColumn<Complaint, String> statusCol;
    @FXML private TableColumn<Complaint, String> noteCol;
    @FXML private TableColumn<Complaint, String> dateCol;

    @FXML private Tab systemAdminTab;
    @FXML private TextField adminNameField;
    @FXML private TextField adminUsernameField;
    @FXML private TextField adminEmailField;
    @FXML private PasswordField adminPasswordField;
    @FXML private TableView<User> adminsTable;
    @FXML private TableColumn<User, String> adminIdCol;
    @FXML private TableColumn<User, String> adminFullNameCol;
    @FXML private TableColumn<User, String> adminUsernameCol;
    @FXML private TableColumn<User, String> adminEmailCol;
    @FXML private TextField studentSearchField;
    @FXML private TableView<User> studentsTable;
    @FXML private TableColumn<User, String> studentIdCol;
    @FXML private TableColumn<User, String> studentNameCol;
    @FXML private TableColumn<User, String> studentUsernameCol;
    @FXML private TableColumn<User, String> studentEmailCol;

    private final CompassData data = new CompassData();

    @FXML
    private void initialize() {
        headerLabel.setText(Session.isSystemAdmin() ? "System Admin Dashboard" : "Admin Dashboard");
        systemAdminTab.setDisable(!Session.isSystemAdmin());
        setupFilters();
        setupComplaintTable();
        setupUserTables();
        refreshAll();
    }

    private void setupFilters() {
        categoryFilter.getItems().setAll("All");
        categoryFilter.getItems().addAll(CompassData.CATEGORIES);
        categoryFilter.getSelectionModel().select("All");

        statusFilter.getItems().setAll("All", "Pending", "In Progress", "Resolved");
        statusFilter.getSelectionModel().select("All");

        statusCombo.getItems().setAll("Pending", "In Progress", "Resolved");
        statusCombo.getSelectionModel().select("In Progress");
    }

    private void setupComplaintTable() {
        idCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getComplaintId())));
        studentCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        categoryCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().label()));
        noteCol.setCellValueFactory(c -> new SimpleStringProperty(blankToDash(c.getValue().getAdminNote())));
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedAt() == null ? "" : c.getValue().getCreatedAt().toString()));
        complaintsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                statusCombo.getSelectionModel().select(selected.getStatus().label());
                adminNoteArea.setText(selected.getAdminNote());
            }
        });
    }

    private void setupUserTables() {
        adminIdCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getUserId())));
        adminFullNameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        adminUsernameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        adminEmailCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));

        studentIdCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getUserId())));
        studentNameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        studentUsernameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        studentEmailCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
    }

    @FXML
    private void handleFilter() {
        refreshComplaints();
    }

    @FXML
    private void handleRefresh() {
        refreshAll();
        showMessage("Dashboard refreshed");
    }

    @FXML
    private void handleUpdateComplaint() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Select a complaint first");
            return;
        }
        Complaint.ComplaintStatus status = Complaint.ComplaintStatus.fromLabel(statusCombo.getValue());
        data.updateComplaint(selected.getComplaintId(), status, adminNoteArea.getText());
        refreshAll();
        showMessage("Complaint updated");
    }

    @FXML
    private void handleResolveComplaint() {
        statusCombo.getSelectionModel().select("Resolved");
        handleUpdateComplaint();
    }

    @FXML
    private void handleViewDetails() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Select a complaint first");
            return;
        }
        Alert details = new Alert(Alert.AlertType.INFORMATION);
        details.setTitle("Complaint #" + selected.getComplaintId());
        details.setHeaderText(selected.getTitle());
        VBox content = new VBox(8,
                new Label("Student: " + selected.getStudentName()),
                new Label("Category: " + selected.getCategory()),
                new Label("Status: " + selected.getStatus().label()),
                new Label("Description:"),
                readOnlyArea(selected.getDescription(), 4),
                new Label("Admin Notes:"),
                readOnlyArea(blankToDash(selected.getAdminNote()), 3)
        );
        content.setPadding(new Insets(8));
        details.getDialogPane().setContent(content);
        details.showAndWait();
    }

    @FXML
    private void handleAddAdmin() {
        try {
            data.addAdmin(
                    adminUsernameField.getText(),
                    adminEmailField.getText(),
                    adminNameField.getText(),
                    adminPasswordField.getText()
            );
            adminNameField.clear();
            adminUsernameField.clear();
            adminEmailField.clear();
            adminPasswordField.clear();
            refreshAdmins();
            showMessage("Admin account added");
        } catch (Exception e) {
            showMessage(e.getMessage());
        }
    }

    @FXML
    private void handleRemoveAdmin() {
        User selected = adminsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Select an admin first");
            return;
        }
        data.removeAdmin(selected.getUserId());
        refreshAdmins();
        showMessage("Admin account removed");
    }

    @FXML
    private void handleSearchStudents() {
        refreshStudents();
    }

    @FXML
    private void handleOpenMap() {
        View.show("map_view", "Compass - AASTU Campus Map", 1000, 700);
    }

    @FXML
    private void handleLogout() {
        Session.logout();
        View.show("login", "Compass - Login", 800, 600);
    }

    private void refreshAll() {
        refreshStats();
        refreshComplaints();
        if (Session.isSystemAdmin()) {
            refreshAdmins();
            refreshStudents();
        }
    }

    private void refreshStats() {
        totalLabel.setText(String.valueOf(data.totalComplaints()));
        pendingLabel.setText(String.valueOf(data.countByStatus(Complaint.ComplaintStatus.PENDING)));
        inProgressLabel.setText(String.valueOf(data.countByStatus(Complaint.ComplaintStatus.IN_PROGRESS)));
        resolvedLabel.setText(String.valueOf(data.countByStatus(Complaint.ComplaintStatus.RESOLVED)));

        StringBuilder analytics = new StringBuilder();
        for (Map.Entry<String, Long> entry : data.complaintsByCategory().entrySet()) {
            analytics.append(entry.getKey()).append(": ").append(entry.getValue()).append("   ");
        }
        categoryAnalyticsLabel.setText(analytics.toString());
    }

    private void refreshComplaints() {
        String category = categoryFilter.getValue();
        String status = statusFilter.getValue();
        Complaint.ComplaintStatus selectedStatus = "All".equals(status) ? null : Complaint.ComplaintStatus.fromLabel(status);
        complaintsTable.setItems(FXCollections.observableArrayList(data.filterComplaints(category, selectedStatus)));
    }

    private void refreshAdmins() {
        adminsTable.setItems(FXCollections.observableArrayList(data.admins()));
    }

    private void refreshStudents() {
        studentsTable.setItems(FXCollections.observableArrayList(data.students(studentSearchField.getText())));
    }

    private TextArea readOnlyArea(String text, int rows) {
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(rows);
        return area;
    }

    private String blankToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showMessage(String message) {
        statusMessage.setText(message);
    }
}
