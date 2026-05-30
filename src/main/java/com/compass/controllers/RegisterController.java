package com.compass.controllers;

import com.compass.config.View;
import com.compass.models.CompassData;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private final CompassData data = new CompassData();

    @FXML
    private void handleRegister() {
        hideError();
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Passwords do not match");
            return;
        }
        try {
            data.registerStudent(
                    usernameField.getText(),
                    emailField.getText(),
                    fullNameField.getText(),
                    passwordField.getText()
            );
            View.show("login", "Compass - Login", 800, 600);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        View.show("login", "Compass - Login", 800, 600);
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
