package com.compass.controller;

import com.compass.model.Complaint;
import com.compass.model.Department;
import com.compass.model.CampusLocation;
import com.compass.service.ComplaintService;
import com.compass.service.DepartmentAnalyticsService;
import com.compass.service.RepositoryFactory;
import com.compass.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;

public class ComplaintFormController {
    @FXML private TextField titleField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private ComboBox<Department> departmentCombo;
    @FXML private ComboBox<CampusLocation> locationCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextField attachmentField;
    @FXML private Label errorLabel;

    private File selectedFile;
    private Runnable onSuccess;
    private Runnable onCancel;
    private Parent root;

    private final ComplaintService complaintService = new ComplaintService();
    private final DepartmentAnalyticsService analyticsService = new DepartmentAnalyticsService();

    @FXML
    private void initialize() {
        categoryCombo.getItems().addAll(
                "Facilities", "Security", "Academic", "Health", "IT", "Other");
        categoryCombo.getSelectionModel().selectFirst();

        for (Complaint.ComplaintPriority p : Complaint.ComplaintPriority.values()) {
            priorityCombo.getItems().add(p.name());
        }
        priorityCombo.getSelectionModel().select("MEDIUM");

        departmentCombo.getItems().addAll(analyticsService.getAllDepartments());
        departmentCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Department item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDepartmentName());
            }
        });
        departmentCombo.setButtonCell(departmentCombo.getCellFactory().call(null));

        locationCombo.getItems().addAll(RepositoryFactory.locations().findAll());
        locationCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CampusLocation item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLocationName());
            }
        });
        locationCombo.setButtonCell(locationCombo.getCellFactory().call(null));
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    public Parent getRoot() {
        return root != null ? root : titleField.getParent().getParent();
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    @FXML
    private void handleBrowse() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select attachment");
        File file = chooser.showOpenDialog(titleField.getScene().getWindow());
        if (file != null) {
            selectedFile = file;
            attachmentField.setText(file.getName());
        }
    }

    @FXML
    private void handleSubmit() {
        hideError();
        if (titleField.getText().isBlank() || descriptionArea.getText().isBlank()) {
            showError("Title and description are required");
            return;
        }
        Department dept = departmentCombo.getValue();
        if (dept == null) {
            showError("Select a department");
            return;
        }
        try {
            Complaint complaint = new Complaint();
            complaint.setStudentId(SessionManager.getInstance().getCurrentUserId());
            complaint.setTitle(titleField.getText().trim());
            complaint.setDescription(descriptionArea.getText().trim());
            complaint.setCategory(categoryCombo.getValue());
            complaint.setPriority(Complaint.ComplaintPriority.valueOf(priorityCombo.getValue()));
            complaint.setAssignedDepartmentId(dept.getDepartmentId());
            complaint.setStatus(Complaint.ComplaintStatus.SUBMITTED);
            CampusLocation loc = locationCombo.getValue();
            if (loc != null) {
                complaint.setLocationId(loc.getLocationId());
            }
            complaintService.submitComplaint(complaint, selectedFile);
            if (onSuccess != null) {
                onSuccess.run();
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        if (onCancel != null) {
            onCancel.run();
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}