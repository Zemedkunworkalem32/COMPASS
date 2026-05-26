package com.campus.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import com.campus.repository.LocationRepository;
import com.campus.model.CampusLocation;

import java.util.List;

public class AdminLocationController {

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
    private TableView<CampusLocation> locationsTable;
    @FXML
    private TableColumn<CampusLocation, Integer> idColumn;
    @FXML
    private TableColumn<CampusLocation, String> nameColumn;
    @FXML
    private TableColumn<CampusLocation, String> buildingColumn;
    @FXML
    private TableColumn<CampusLocation, String> floorColumn;

    @FXML
    private VBox detailsPanel;
    @FXML
    private TextField nameField;
    @FXML
    private TextField buildingField;
    @FXML
    private TextField floorField;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    private LocationRepository locationRepository;
    private List<CampusLocation> allLocations;
    private CampusLocation selectedLocation;

    @FXML
    public void initialize() {
        locationRepository = new LocationRepository();

        setupTableColumns();
        setupEventHandlers();
        refreshData();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        buildingColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBuilding()));
        floorColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFloor()));

        locationsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        selectedLocation = newVal;
                        displayLocationDetails(newVal);
                    }
                }
        );
    }

    private void setupEventHandlers() {
        searchButton.setOnAction(e -> applyFilter());
        refreshButton.setOnAction(e -> refreshData());
        addButton.setOnAction(e -> openAddDialog());
        updateButton.setOnAction(e -> updateLocation());
        deleteButton.setOnAction(e -> deleteLocation());
    }

    public void refreshData() {
        new Thread(() -> {
            try {
                allLocations = locationRepository.findAll();
                Platform.runLater(() -> {
                    locationsTable.getItems().clear();
                    locationsTable.getItems().addAll(allLocations);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load locations: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private void applyFilter() {
        String searchText = searchField.getText().toLowerCase();
        List<CampusLocation> filtered = allLocations.stream()
                .filter(l -> l.getName().toLowerCase().contains(searchText) ||
                        l.getBuilding().toLowerCase().contains(searchText) ||
                        l.getFloor().toLowerCase().contains(searchText))
                .toList();

        locationsTable.getItems().clear();
        locationsTable.getItems().addAll(filtered);
    }

    private void displayLocationDetails(CampusLocation location) {
        nameField.setText(location.getName());
        buildingField.setText(location.getBuilding());
        floorField.setText(location.getFloor());
    }

    private void updateLocation() {
        if (selectedLocation == null) {
            showAlert("Warning", "Please select a location first", Alert.AlertType.WARNING);
            return;
        }

        selectedLocation.setName(nameField.getText());
        selectedLocation.setBuilding(buildingField.getText());
        selectedLocation.setFloor(floorField.getText());

        new Thread(() -> {
            try {
                locationRepository.update(selectedLocation);
                Platform.runLater(() -> {
                    showAlert("Success", "Location updated successfully", Alert.AlertType.INFORMATION);
                    refreshData();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to update location: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private void deleteLocation() {
        if (selectedLocation == null) {
            showAlert("Warning", "Please select a location first", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Location");
        confirmDialog.setHeaderText("Are you sure?");
        confirmDialog.setContentText("This action cannot be undone.");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        new Thread(() -> {
            try {
                locationRepository.delete(selectedLocation.getId());
                Platform.runLater(() -> {
                    showAlert("Success", "Location deleted successfully", Alert.AlertType.INFORMATION);
                    refreshData();
                    selectedLocation = null;
                    detailsPanel.setDisable(true);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to delete location: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private void openAddDialog() {
        Dialog<CampusLocation> dialog = new Dialog<>();
        dialog.setTitle("Add New Location");
        dialog.setHeaderText("Enter location details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Location name");
        TextField buildingField = new TextField();
        buildingField.setPromptText("Building name");
        TextField floorField = new TextField();
        floorField.setPromptText("Floor");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Building:"), 0, 1);
        grid.add(buildingField, 1, 1);
        grid.add(new Label("Floor:"), 0, 2);
        grid.add(floorField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                CampusLocation newLocation = new CampusLocation();
                newLocation.setName(nameField.getText());
                newLocation.setBuilding(buildingField.getText());
                newLocation.setFloor(floorField.getText());
                return newLocation;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newLocation -> {
            new Thread(() -> {
                try {
                    locationRepository.create(newLocation);
                    Platform.runLater(() -> {
                        showAlert("Success", "Location created successfully", Alert.AlertType.INFORMATION);
                        refreshData();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Error", "Failed to create location: " + e.getMessage(), Alert.AlertType.ERROR));
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
