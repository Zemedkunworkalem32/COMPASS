package com.compass.controllers;

import com.compass.config.Session;
import com.compass.config.View;
import com.compass.models.CompassData;
import com.compass.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final CompassData data = new CompassData();

    @FXML
    private void initialize() {
        hideError();
        usernameField.requestFocus();
    }

    @FXML
    private void handleLogin() {
        hideError();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            showError("Please enter both username and password");
            return;
        }

        try {
            Optional<User> user = data.login(username, password);
            if (user.isEmpty()) {
                showError("Invalid username or password");
                return;
            }
            Session.login(user.get());
            if (user.get().getRole() == User.UserRole.STUDENT) {
                View.show("student_dashboard", "Compass - Student Dashboard", 1000, 650);
            } else {
                View.show("admin_dashboard", "Compass - Admin Dashboard", 1200, 750);
            }
        } catch (Exception e) {
            showError("Login error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        View.show("register", "Compass - Student Registration", 800, 650);
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
