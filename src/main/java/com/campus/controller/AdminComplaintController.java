package com.campus.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.campus.repository.ComplaintRepository;
import com.campus.repository.DepartmentRepository;
import com.campus.model.Complaint;
import com.campus.model.Department;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminComplaintController {

    @FXML
    private VBox rootPane;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilterCombo;
    @FXML
    private ComboBox<String> priorityFilterCombo;
    @FXML
    private Button searchButton;
    @FXML
    private Button refreshButton;

    @FXML
    private TableView<Complaint> complaintsTable;
    @FXML
    private TableColumn<Complaint, Integer> idColumn;
    @FXML
    private TableColumn<Complaint, String> titleColumn;
    @FXML
    private TableColumn<Complaint, String> statusColumn;
    @FXML
    private TableColumn<Complaint, String> priorityColumn;
    @FXML
    private TableColumn<Complaint, String> studentColumn;

    @FXML
    private VBox detailsPanel;
    @FXML
    private Label complaintIdLabel;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private ComboBox<String> priorityCombo;
    @FXML
    private ComboBox<Department> departmentCombo;
    @FXML
    private Button updateButton;
    @FXML
    private Button assignButton;
    @FXML
    private Button deleteButton;

    private ComplaintRepository complaintRepository;
    private DepartmentRepository departmentRepository;
    private List<Complaint> allComplaints;
    private Complaint selectedComplaint;

    @FXML
    public void initialize() throws SQLException {
        complaintRepository = new ComplaintRepository();
        departmentRepository = new DepartmentRepository();

        setupTableColumns();
        setupFilters();
        setupEventHandlers();
        loadDepartments();
        refreshData();
    }
    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        titleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        priorityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPriority()));
        studentColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getStudentId())));

        complaintsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        selectedComplaint = newVal;
                        displayComplaintDetails(newVal);
                    }
                }
        );
    }
    private void setupFilters() {
        statusFilterCombo.getItems().addAll("All", "NEW", "IN_PROGRESS", "RESOLVED", "CLOSED");
        statusFilterCombo.setValue("All");

        priorityFilterCombo.getItems().addAll("All", "LOW", "NORMAL", "HIGH", "CRITICAL");
        priorityFilterCombo.setValue("All");

        statusCombo.getItems().addAll("NEW", "IN_PROGRESS", "RESOLVED", "CLOSED");
        priorityCombo.getItems().addAll("LOW", "NORMAL", "HIGH", "CRITICAL");
    }

    private void setupEventHandlers() {
        searchButton.setOnAction(e -> applyFilters());
        refreshButton.setOnAction(e -> refreshData());
        updateButton.setOnAction(e -> updateComplaint());
        assignButton.setOnAction(e -> assignComplaint());
        deleteButton.setOnAction(e -> deleteComplaint());

        statusFilterCombo.setOnAction(e -> applyFilters());
        priorityFilterCombo.setOnAction(e -> applyFilters());
    }

    private void loadDepartments() {
        new Thread(() -> {
            try {
                List<Department> departments = departmentRepository.findAll();
                Platform.runLater(() -> {
                    departmentCombo.getItems().clear();
                    departmentCombo.getItems().addAll(departments);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load departments: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    public void refreshData() {
        new Thread(() -> {
            try {
                allComplaints = complaintRepository.findAll();
                Platform.runLater(() -> {
                    complaintsTable.getItems().clear();
                    complaintsTable.getItems().addAll(allComplaints);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load complaints: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String statusFilter = statusFilterCombo.getValue();
        String priorityFilter = priorityFilterCombo.getValue();

        List<Complaint> filtered = new ArrayList<>();

        for (Complaint complaint : allComplaints) {
            if (!statusFilter.equals("All") && !complaint.getStatus().equals(statusFilter)) {
                continue;
            }

            if (!priorityFilter.equals("All") && !complaint.getPriority().equals(priorityFilter)) {
                continue;
            }

            if (!searchText.isEmpty()) {
                if (!complaint.getTitle().toLowerCase().contains(searchText) &&
                        !complaint.getDescription().toLowerCase().contains(searchText) &&
                        !String.valueOf(complaint.getId()).contains(searchText)) {
                    continue;
                }
            }

            filtered.add(complaint);
        }

        complaintsTable.getItems().clear();
        complaintsTable.getItems().addAll(filtered);
    }

    private void displayComplaintDetails(Complaint complaint) {
        complaintIdLabel.setText("Complaint #" + complaint.getId());
        descriptionArea.setText(complaint.getDescription());
        statusCombo.setValue(complaint.getStatus());
        priorityCombo.setValue(complaint.getPriority());

        if (complaint.getDepartmentId() != null) {
            departmentCombo.setValue(new Department()); // Should load the actual department
        }
    }

    private void updateComplaint() {
        if (selectedComplaint == null) {
            showAlert("Warning", "Please select a complaint first", Alert.AlertType.WARNING);
            return;
        }

        selectedComplaint.setStatus(statusCombo.getValue());
        selectedComplaint.setPriority(priorityCombo.getValue());

        new Thread(() -> {
            try {
                complaintRepository.update(selectedComplaint);
                Platform.runLater(() -> {
                    showAlert("Success", "Complaint updated successfully", Alert.AlertType.INFORMATION);
                    refreshData();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to update complaint: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private void assignComplaint() {
        if (selectedComplaint == null) {
            showAlert("Warning", "Please select a complaint first", Alert.AlertType.WARNING);
            return;
        }

        Department selectedDept = departmentCombo.getValue();
        if (selectedDept == null) {
            showAlert("Warning", "Please select a department first", Alert.AlertType.WARNING);
            return;
        }

        selectedComplaint.setDepartmentId(selectedDept.getId());
        selectedComplaint.setStatus("IN_PROGRESS");

        new Thread(() -> {
            try {
                complaintRepository.update(selectedComplaint);
                Platform.runLater(() -> {
                    showAlert("Success", "Complaint assigned to " + selectedDept.getName(), Alert.AlertType.INFORMATION);
                    refreshData();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to assign complaint: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private void deleteComplaint() {
        if (selectedComplaint == null) {
            showAlert("Warning", "Please select a complaint first", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Complaint");
        confirmDialog.setHeaderText("Are you sure?");
        confirmDialog.setContentText("This action cannot be undone.");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        new Thread(() -> {
            try {
                complaintRepository.delete(selectedComplaint.getId());
                Platform.runLater(() -> {
                    showAlert("Success", "Complaint deleted successfully", Alert.AlertType.INFORMATION);
                    refreshData();
                    selectedComplaint = null;
                    detailsPanel.setDisable(true);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to delete complaint: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getRootPane() {
        return rootPane;
    }
}
