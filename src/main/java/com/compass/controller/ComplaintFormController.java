package com.compass.controller;

import com.compass.model.Complaint;
import com.compass.service.ComplaintService;
import com.compass.service.FileUploadUtil;
import com.compass.service.RemoteReportingClient;
import com.compass.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ComplaintFormController {
    @FXML
    public TextField titleField;
    @FXML
    public TextArea descriptionArea;
    @FXML
    public ChoiceBox<String> priorityChoice;
    @FXML
    public Button attachButton;
    @FXML
    public Label selectedFilesLabel;
    @FXML
    public Button submitButton;
    @FXML
    public Label statusMessage;
    @FXML
    public TextField searchField;

    private final List<File> selectedFiles = new ArrayList<>();
    private final ComplaintService complaintService;
    private final RemoteReportingClient reportingClient;

    public ComplaintFormController() throws SQLException {
        this.complaintService = new ComplaintService();
        this.reportingClient = new RemoteReportingClient("https://example.com/api/reports");
    }

    @FXML
    public void initialize() {
        priorityChoice.setItems(FXCollections.observableArrayList("LOW", "NORMAL", "HIGH"));
        priorityChoice.setValue("NORMAL");
    }

    @FXML
    public void handleAttachFiles(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Attach Files");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt")
        );
        Window window = attachButton.getScene().getWindow();
        List<File> files = chooser.showOpenMultipleDialog(window);
        if (files != null && !files.isEmpty()) {
            selectedFiles.clear();
            selectedFiles.addAll(files);
            selectedFilesLabel.setText(selectedFiles.stream().map(File::getName).collect(Collectors.joining(", ")));
        }
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        statusMessage.setText("");
        Complaint complaint = new Complaint();
        complaint.setTitle(titleField.getText());
        complaint.setDescription(descriptionArea.getText());
        complaint.setPriority(priorityChoice.getValue());
        complaint.setStudentId(SessionManager.getCurrentUserId());
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());

        Task<Complaint> saveTask = new Task<>() {
            @Override
            protected Complaint call() throws Exception {
                Complaint created = complaintService.createComplaint(complaint);
                if (!selectedFiles.isEmpty()) {
                    FileUploadUtil.processAttachments(selectedFiles.stream().map(File::toPath).collect(Collectors.toList()),
                            new java.io.File("uploads").toPath());
                }
                try {
                    reportingClient.reportComplaint(created);
                } catch (IOException | InterruptedException ex) {
                    // remote reporting failure should not block initial save
                }
                return created;
            }
        };

        saveTask.setOnSucceeded(workerStateEvent -> {
            statusMessage.setText("Complaint submitted successfully.");
            clearForm();
        });
        saveTask.setOnFailed(workerStateEvent -> {
            Throwable ex = saveTask.getException();
            statusMessage.setText("Failed to submit complaint: " + ex.getMessage());
        });

        new Thread(saveTask).start();
    }

    private void clearForm() {
        Platform.runLater(() -> {
            titleField.clear();
            descriptionArea.clear();
            selectedFiles.clear();
            selectedFilesLabel.setText("No files selected");
            priorityChoice.setValue("NORMAL");
        });
    }
}
