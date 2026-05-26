package com.campus.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import com.campus.repository.DepartmentRepository;
import com.campus.repository.LocationRepository;
import com.campus.model.Department;
import com.campus.model.CampusLocation;

import java.util.List;

public class AdminDepartmentController {

    @FXML
    private VBox rootPane;

    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button addButton;

    @FXML
    private TableView<Department> departmentsTable;
    @FXML
    private TableColumn<Department, Integer> idColumn;
    @FXML
    private TableColumn<Department, String> nameColumn;
    @FXML
    private TableColumn<Department, String> emailColumn;
    @FXML
    private TableColumn<Department, Integer> locationColumn;

    @FXML
    private VBox detailsPanel;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<CampusLocation> locationCombo;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    private DepartmentRepository departmentRepository;
    private LocationRepository locationRepository;
    private List<Department> allDepartments;
    private Department selectedDepartment;

    @FXML
    public void initialize() {
        departmentRepository = new DepartmentRepository();
        locationRepository = new LocationRepository();

        setupTableColumns();
        setupEventHandlers();
        loadLocations();
        refreshData();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        emailColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        locationColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getLocationId()).asObject());

        departmentsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        selectedDepartment = newVal;
                        displayDepartmentDetails(newVal);
                    }
                }
        );
    }

    private void setupEventHandlers() {
        searchButton.setOnAction(e -> applyFilter());
        refreshButton.setOnAction(e -> refreshData());
        addButton.setOnAction(e -> openAddDialog());
        updateButton.setOnAction(e -> updateDepartment());
        deleteButton.setOnAction(e -> deleteDepartment());
    }

    private void loadLocations() {
        new Thread(() -> {
            try {
                List<CampusLocation> locations = locationRepository.findAll();
                Platform.runLater(() -> {
                    locationCombo.getItems().clear();
                    locationCombo.getItems().addAll(locations);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load locations: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    public void refreshData() {
        new Thread(() -> {
            try {
                allDepartments = departmentRepository.findAll();
                Platform.runLater(() -> {
                    departmentsTable.getItems().clear();
                    departmentsTable.getItems().addAll(allDepartments);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load departments: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private void applyFilter() {
        String searchText = searchField.getText().toLowerCase();
        List<Department> filtered = allDepartments.stream()
                .filter(d -> d.getName().toLowerCase().contains(searchText) ||
                        d.getEmail().toLowerCase().contains(searchText))
                .toList();

        departmentsTable.getItems().clear();
        departmentsTable.getItems().addAll(filtered);
    }

    private void displayDepartmentDetails(Department department) {
        nameField.setText(department.getName());
        emailField.setText(department.getEmail());
        if (department.getLocationId() != null) {
            locationCombo.getValue();
        }
    }

    private void updateDepartment() {
        if (selectedDepartment == null) {
            showAlert("Warning", "Please select a department first", Alert.AlertType.WARNING);
            return;
        }

        selectedDepartment.setName(nameField.getText());
        selectedDepartment.setEmail(emailField.getText());
        CampusLocation location = locationCombo.getValue();
        if (location != null) {
            selectedDepartment.setLocationId(location.getId());
        }

        new Thread(() -> {
            try {
                departmentRepository.update(selectedDepartment);
                Platform.runLater(() -> {
                    showAlert("Success", "Department updated successfully", Alert.AlertType.INFORMATION);
                    refreshData();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to update department: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private void deleteDepartment() {
        if (selectedDepartment == null) {
            showAlert("Warning", "Please select a department first", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Department");
        confirmDialog.setHeaderText("Are you sure?");
        confirmDialog.setContentText("This action cannot be undone.");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        new Thread(() -> {
            try {
                departmentRepository.delete(selectedDepartment.getId());
                Platform.runLater(() -> {
                    showAlert("Success", "Department deleted successfully", Alert.AlertType.INFORMATION);
                    refreshData();
                    selectedDepartment = null;
                    detailsPanel.setDisable(true);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to delete department: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private void openAddDialog() {
        Dialog<Department> dialog = new Dialog<>();
        dialog.setTitle("Add New Department");
        dialog.setHeaderText("Enter department details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Department name");
        TextField emailField = new TextField();
        emailField.setPromptText("Department email");
        ComboBox<CampusLocation> locCombo = new ComboBox<>(locationCombo.getItems());

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(locCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                Department newDept = new Department();
                newDept.setName(nameField.getText());
                newDept.setEmail(emailField.getText());
                if (locCombo.getValue() != null) {
                    newDept.setLocationId(locCombo.getValue().getId());
                }
                return newDept;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newDept -> {
            new Thread(() -> {
                try {
                    departmentRepository.create(newDept);
                    Platform.runLater(() -> {
                        showAlert("Success", "Department created successfully", Alert.AlertType.INFORMATION);
                        refreshData();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Error", "Failed to create department: " + e.getMessage(), Alert.AlertType.ERROR));
                }
            }).start();
        });
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
