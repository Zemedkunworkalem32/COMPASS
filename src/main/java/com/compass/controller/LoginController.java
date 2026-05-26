package com.compass.controller;

import com.compass.model.User;
import com.compass.service.AuthService;
import com.compass.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Scene;

import java.util.Optional;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    public Scene getScene() {
        SceneNavigator.show("/fxml/login.fxml", "Compass - Login", 800, 600);
        return SceneNavigator.getPrimaryStage().getScene();
    }

    @FXML
    private void initialize() {
        hideError();
    }

    @FXML
    private void handleLogin() {
        hideError();
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username.isBlank() || password.isBlank()) {
            showError("Enter username and password");
            return;
        }
        Optional<User> user = authService.login(username, password);
        if (user.isEmpty()) {
            showError("Invalid username or password");
            return;
        }
        navigateByRole(user.get());
    }

    @FXML
    private void handleRegister() {
        SceneNavigator.show("/fxml/register.fxml", "Compass - Register", 800, 650);
    }

    private void navigateByRole(User user) {
        switch (user.getRole()) {
            case ADMIN -> SceneNavigator.show("/fxml/admin_dashboard.fxml",
                    "Compass - Admin", 1100, 700);
            case DEPARTMENT_STAFF -> SceneNavigator.show("/fxml/staff_dashboard.fxml",
                    "Compass - Department", 1000, 650);
            default -> SceneNavigator.show("/fxml/student_dashboard.fxml",
                    "Compass - Student", 1000, 650);
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
