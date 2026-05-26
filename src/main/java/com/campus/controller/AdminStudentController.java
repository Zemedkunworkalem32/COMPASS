package com.campus.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import com.campus.repository.UserRepository;
import com.campus.model.User;

import java.util.List;

public class AdminStudentController {

    @FXML
    private VBox rootPane;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> roleFilterCombo;
    @FXML
    private Button searchButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button addButton;

    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private VBox detailsPanel;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<String> roleCombo;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    private UserRepository userRepository;
    private List<User> allUsers;
    private User selectedUser;

    @FXML
    public void initialize() {
        userRepository = new UserRepository();

        setupTableColumns();
        setupFilters();
        setupEventHandlers();
        refreshData();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        usernameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        emailColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        roleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole()));

        usersTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        selectedUser = newVal;
                        displayUserDetails(newVal);
                    }
                }
        );
    }

    private void setupFilters() {
        roleFilterCombo.getItems().addAll("All", "STUDENT", "ADMIN", "DEPARTMENT_STAFF");
        roleFilterCombo.setValue("All");

        roleCombo.getItems().addAll("STUDENT", "ADMIN", "DEPARTMENT_STAFF");
    }

    private void setupEventHandlers() {
        searchButton.setOnAction(e -> applyFilters());
        refreshButton.setOnAction(e -> refreshData());
        addButton.setOnAction(e -> openAddDialog());
        updateButton.setOnAction(e -> updateUser());
        deleteButton.setOnAction(e -> deleteUser());

        roleFilterCombo.setOnAction(e -> applyFilters());
    }

    public void refreshData() {
        new Thread(() -> {
            try {
                allUsers = userRepository.findAll();
                Platform.runLater(() -> {
                    usersTable.getItems().clear();
                    usersTable.getItems().addAll(allUsers);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load users: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String roleFilter = roleFilterCombo.getValue();

        List<User> filtered = allUsers.stream()
                .filter(u -> {
                    if (!roleFilter.equals("All") && !u.getRole().equals(roleFilter)) {
                        return false;
                    }
                    if (!searchText.isEmpty()) {
                        return u.getUsername().toLowerCase().contains(searchText) ||
                                u.getEmail().toLowerCase().contains(searchText);
                    }
                    return true;
                })
                .toList();

        usersTable.getItems().clear();
        usersTable.getItems().addAll(filtered);
    }

    private void displayUserDetails(User user) {
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        roleCombo.setValue(user.getRole());
    }

    private void updateUser() {
        if (selectedUser == null) {
            showAlert("Warning", "Please select a user first", Alert.AlertType.WARNING);
            return;
        }

        selectedUser.setUsername(usernameField.getText());
        selectedUser.setEmail(emailField.getText());
        selectedUser.setRole(roleCombo.getValue());

        new Thread(() -> {
            try {
                userRepository.update(selectedUser);
                Platform.runLater(() -> {
                    showAlert("Success", "User updated successfully", Alert.AlertType.INFORMATION);
                    refreshData();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to update user: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private void deleteUser() {
        if (selectedUser == null) {
            showAlert("Warning", "Please select a user first", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete User");
        confirmDialog.setHeaderText("Are you sure?");
        confirmDialog.setContentText("This action cannot be undone.");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        new Thread(() -> {
            try {
                userRepository.delete(selectedUser.getId());
                Platform.runLater(() -> {
                    showAlert("Success", "User deleted successfully", Alert.AlertType.INFORMATION);
                    refreshData();
                    selectedUser = null;
                    detailsPanel.setDisable(true);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to delete user: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private void openAddDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Enter user details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        ComboBox<String> roleCombo = new ComboBox<>(this.roleCombo.getItems());
        roleCombo.setValue("STUDENT");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                User newUser = new User();
                newUser.setUsername(usernameField.getText());
                newUser.setEmail(emailField.getText());
                newUser.setPassword(passwordField.getText()); // In real app, would hash this
                newUser.setRole(roleCombo.getValue());
                return newUser;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newUser -> {
            new Thread(() -> {
                try {
                    userRepository.create(newUser);
                    Platform.runLater(() -> {
                        showAlert("Success", "User created successfully", Alert.AlertType.INFORMATION);
                        refreshData();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Error", "Failed to create user: " + e.getMessage(), Alert.AlertType.ERROR));
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
