package com.compass.controllers;

import com.compass.config.Session;
import com.compass.models.CompassData;
import com.compass.models.Complaint;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

public class ComplaintFormController {
    @FXML private TextField titleField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextField attachmentField;
    @FXML private Label errorLabel;

    private File selectedFile;
    private Runnable onSuccess;
    private Runnable onCancel;
    private Parent root;

    private final CompassData data = new CompassData();

    @FXML
    private void initialize() {
        categoryCombo.getItems().setAll(CompassData.CATEGORIES);
        categoryCombo.getSelectionModel().select("Other");
        hideError();
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
        chooser.setTitle("Attach one optional file");
        File file = chooser.showOpenDialog(titleField.getScene().getWindow());
        if (file != null) {
            selectedFile = file;
            attachmentField.setText(file.getName());
        }
    }

    @FXML
    private void handleSubmit() {
        hideError();
        try {
            Complaint complaint = new Complaint();
            complaint.setStudentId(Session.userId());
            complaint.setTitle(titleField.getText().trim());
            complaint.setDescription(descriptionArea.getText().trim());
            complaint.setCategory(categoryCombo.getValue());
            data.submitComplaint(complaint, selectedFile);
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
