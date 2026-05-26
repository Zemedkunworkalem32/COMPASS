package com.campus.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import com.campus.model.User;
import com.campus.util.SessionManager;

import java.io.IOException;

public class StudentDashboardController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private VBox sidebarContainer;

    @FXML
    private Label userLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private Pane contentArea;

    @FXML
    private Button logoutButton;

    private User currentUser;
    private AdminDashboardController adminDashboardController;
    private AdminComplaintController adminComplaintController;
    private AdminDepartmentController adminDepartmentController;
    private AdminStudentController adminStudentController;
    private AdminLocationController adminLocationController;
    private ServicesController servicesController;
    private ComplaintFormController complaintFormController;
    private MyComplaintsController myComplaintsController;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Session Error", "No user logged in", Alert.AlertType.ERROR);
            return;
        }
        userLabel.setText(currentUser.getUsername());
        userRoleLabel.setText(currentUser.getRole());
        buildSidebar();
        loadDefaultView();
        logoutButton.setOnAction(e -> handleLogout());
    }

    private void buildSidebar() {
        sidebarContainer.getChildren().clear();
        sidebarContainer.setPadding(new Insets(10));
        sidebarContainer.setSpacing(10);

        String role = currentUser.getRole();

        if ("ADMIN".equalsIgnoreCase(role)) {
            addSidebarButton("Dashboard", this::loadAdminDashboard);
            addSidebarButton("Complaints", this::loadAdminComplaints);
            addSidebarButton("Departments", this::loadAdminDepartments);
            addSidebarButton("Students", this::loadAdminStudents);
            addSidebarButton("Locations", this::loadAdminLocations);
            addSidebarButton("Services", this::loadServices);
        } else if ("STUDENT".equalsIgnoreCase(role)) {
            addSidebarButton("Submit Complaint", this::loadComplaintForm);
            addSidebarButton("My Complaints", this::loadMyComplaints);
        } else if ("DEPARTMENT_STAFF".equalsIgnoreCase(role)) {
            addSidebarButton("My Complaints", this::loadMyComplaints);
        }
    }

    private void addSidebarButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-padding: 10px; -fx-font-size: 14px;");
        btn.setOnAction(e -> action.run());
        sidebarContainer.getChildren().add(btn);
    }

    private void loadDefaultView() {
        String role = currentUser.getRole();
        if ("ADMIN".equalsIgnoreCase(role)) {
            loadAdminDashboard();
        } else if ("STUDENT".equalsIgnoreCase(role)) {
            loadComplaintForm();
        } else if ("DEPARTMENT_STAFF".equalsIgnoreCase(role)) {
            loadMyComplaints();
        }
    }

    private void loadAdminDashboard() {
        try {
            if (adminDashboardController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_dashboard.fxml"));
                Parent view = loader.load();
                adminDashboardController = loader.getController();
                contentArea.getChildren().setAll(view);
                // Start auto-refresh only when dashboard is visible
                adminDashboardController.startAutoRefresh();
            } else {
                contentArea.getChildren().setAll((Parent) adminDashboardController.getRootPane());
                adminDashboardController.startAutoRefresh();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load Admin Dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadAdminComplaints() {
        try {
            if (adminComplaintController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_complaints.fxml"));
                Parent view = loader.load();
                adminComplaintController = loader.getController();
                contentArea.getChildren().setAll(view);
                adminComplaintController.refreshData();
            } else {
                contentArea.getChildren().setAll((Parent) adminComplaintController.getRootPane());
                adminComplaintController.refreshData();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load Complaints: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadAdminDepartments() {
        try {
            if (adminDepartmentController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_departments.fxml"));
                Parent view = loader.load();
                adminDepartmentController = loader.getController();
                contentArea.getChildren().setAll(view);
                adminDepartmentController.refreshData();
            } else {
                contentArea.getChildren().setAll((Parent) adminDepartmentController.getRootPane());
                adminDepartmentController.refreshData();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load Departments: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadAdminStudents() {
        try {
            if (adminStudentController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_students.fxml"));
                Parent view = loader.load();
                adminStudentController = loader.getController();
                contentArea.getChildren().setAll(view);
                adminStudentController.refreshData();
            } else {
                contentArea.getChildren().setAll((Parent) adminStudentController.getRootPane());
                adminStudentController.refreshData();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load Students: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadAdminLocations() {
        try {
            if (adminLocationController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_locations.fxml"));
                Parent view = loader.load();
                adminLocationController = loader.getController();
                contentArea.getChildren().setAll(view);
                adminLocationController.refreshData();
            } else {
                contentArea.getChildren().setAll((Parent) adminLocationController.getRootPane());
                adminLocationController.refreshData();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load Locations: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadServices() {
        try {
            if (servicesController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/services.fxml"));
                Parent view = loader.load();
                servicesController = loader.getController();
                contentArea.getChildren().setAll(view);
            } else {
                contentArea.getChildren().setAll((Parent) servicesController.getRootPane());
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load Services: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadComplaintForm() {
        try {
            if (complaintFormController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/complaint_form.fxml"));
                Parent view = loader.load();
                complaintFormController = loader.getController();
                contentArea.getChildren().setAll(view);
            } else {
                contentArea.getChildren().setAll((Parent) complaintFormController.getRootPane());
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load Complaint Form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadMyComplaints() {
        try {
            if (myComplaintsController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/my_complaints.fxml"));
                Parent view = loader.load();
                myComplaintsController = loader.getController();
                contentArea.getChildren().setAll(view);
                myComplaintsController.refreshData();
            } else {
                contentArea.getChildren().setAll((Parent) myComplaintsController.getRootPane());
                myComplaintsController.refreshData();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load My Complaints: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleLogout() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Logout");
        confirmDialog.setHeaderText("Are you sure?");
        confirmDialog.setContentText("Do you want to logout?");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            SessionManager.clearSession();
            // Navigate back to login (this should be handled by Main.java)
            Platform.exit();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public BorderPane getRootPane() {
        return rootPane;
    }
}
