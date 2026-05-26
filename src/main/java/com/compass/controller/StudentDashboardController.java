package com.compass.controller;

import com.compass.model.Complaint;
import com.compass.service.ComplaintService;
import com.compass.util.SceneNavigator;
import com.compass.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class StudentDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private TableView<Complaint> complaintsTable;
    @FXML private TableColumn<Complaint, String> idCol;
    @FXML private TableColumn<Complaint, String> titleCol;
    @FXML private TableColumn<Complaint, String> deptCol;
    @FXML private TableColumn<Complaint, String> priorityCol;
    @FXML private TableColumn<Complaint, String> statusCol;
    @FXML private TableColumn<Complaint, String> dateCol;

    private final ComplaintService complaintService = new ComplaintService();

    @FXML
    private void initialize() {
        SessionManager session = SessionManager.getInstance();
        welcomeLabel.setText("Welcome, " + session.getCurrentUsername());
        setupTable();
        refreshTable();
    }

    private void setupTable() {
        idCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getComplaintId())));
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        deptCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDepartmentName() != null ? c.getValue().getDepartmentName() : "—"));
        priorityCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPriority().name()));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().toString() : ""));
    }

    private void refreshTable() {
        int studentId = SessionManager.getInstance().getCurrentUserId();
        complaintsTable.setItems(FXCollections.observableArrayList(
                complaintService.getStudentComplaints(studentId)));
    }

    @FXML
    private void handleSubmitComplaint() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/complaint_form.fxml"));
            javafx.scene.Parent root = loader.load();
            ComplaintFormController form = loader.getController();
            form.setRoot(root);
            Stage dialog = new Stage();
            dialog.initOwner(SceneNavigator.getPrimaryStage());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Submit Complaint");
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 520, 480);
            var css = getClass().getResource("/css/main.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            dialog.setScene(scene);
            form.setOnSuccess(() -> {
                dialog.close();
                refreshTable();
            });
            form.setOnCancel(dialog::close);
            dialog.showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Could not open form: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleOpenMap() {
        SceneNavigator.show("/fxml/map_view.fxml", "Compass - Campus Map", 1000, 700);
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().invalidateSession();
        SceneNavigator.show("/fxml/login.fxml", "Compass - Login", 800, 600);
    }
}
