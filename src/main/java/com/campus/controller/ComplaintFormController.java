package com.campus.controller;

import com.campus.model.Attachment;
import com.campus.model.Complaint;
import com.campus.service.ComplaintService;
import com.campus.service.FileUploadService;
import com.campus.service.RemoteReportingClient;
import com.campus.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ComplaintFormController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ChoiceBox<String> priorityChoice;
    @FXML private ChoiceBox<String> departmentChoice;
    @FXML private Button attachButton;
    @FXML private Label selectedFilesLabel;
    @FXML private Button submitButton;
    @FXML private Label statusMessage;
    @FXML private ProgressIndicator progressIndicator;

    private final List<File> selectedFiles = new ArrayList<>();
    private ComplaintService complaintService;
    private RemoteReportingClient reportingClient;

    @FXML
    public void initialize() {
        priorityChoice.setItems(FXCollections.observableArrayList("LOW", "MEDIUM", "HIGH", "URGENT"));
        priorityChoice.setValue("MEDIUM");
        
        departmentChoice.setItems(FXCollections.observableArrayList(
            "Auto-assign", "Academic Affairs", "IT Services", "Library", 
            "Student Affairs", "Facilities Management", "Finance Department"
        ));
        departmentChoice.setValue("Auto-assign");
        
        statusMessage.setVisible(false);
        progressIndicator.setVisible(false);
        
        try {
            complaintService = new ComplaintService();
            reportingClient = new RemoteReportingClient("https://your-api.com/api/reports");
        } catch (SQLException e) {
            showMessage("Database error: " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleAttachFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Attach Files");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt"),
            new FileChooser.ExtensionFilter("All Supported", "*.png", "*.jpg", "*.jpeg", "*.pdf", "*.doc", "*.docx")
        );
        
        Stage stage = (Stage) attachButton.getScene().getWindow();
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        
        if (files != null && !files.isEmpty()) {
            selectedFiles.clear();
            
            for (File file : files) {
                if (file.length() > FileUploadService.MAX_FILE_SIZE_BYTES) {
                    showMessage("File " + file.getName() + " exceeds " + 
                        FileUploadService.MAX_FILE_SIZE_MB + "MB limit", true);
                    continue;
                }
                
                String ext = FileUploadService.getFileExtension(file.getName());
                if (!FileUploadService.ALLOWED_ALL_TYPES.contains(ext.toLowerCase())) {
                    showMessage("Unsupported file type: " + ext, true);
                    continue;
                }
                
                selectedFiles.add(file);
            }
            
            if (!selectedFiles.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (File f : selectedFiles) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(f.getName());
                }
                selectedFilesLabel.setText(sb.toString());
            }
        }
    }

    @FXML
    public void handleSubmit() {
        if (!validateForm()) {
            return;
        }
        
        Complaint complaint = new Complaint();
        complaint.setTitle(titleField.getText().trim());
        complaint.setDescription(descriptionArea.getText().trim());
        complaint.setPriority(priorityChoice.getValue());
        complaint.setStudentId(SessionManager.getCurrentUserId());
        
        if (!"Auto-assign".equals(departmentChoice.getValue())) {
            int deptId = getDepartmentId(departmentChoice.getValue());
            complaint.setDepartmentId(deptId);
        }
        
        submitButton.setDisable(true);
        progressIndicator.setVisible(true);
        statusMessage.setVisible(false);
        
        Task<Complaint> submitTask = new Task<>() {
            @Override
            protected Complaint call() throws Exception {
                Complaint saved = complaintService.createComplaint(complaint);
                
                if (!selectedFiles.isEmpty()) {
                    List<Path> filePaths = new ArrayList<>();
                    for (File file : selectedFiles) {
                        filePaths.add(file.toPath());
                    }
                    List<Attachment> attachments = FileUploadService.processAttachments(filePaths, saved.getId());
                    
                    com.campus.repository.AttachmentRepository attachmentRepo = 
                        new com.campus.repository.AttachmentRepository();
                    attachmentRepo.saveAll(saved.getId(), attachments);
                }
                
                if (reportingClient != null) {
                    reportingClient.reportComplaintAsync(saved);
                }
                
                return saved;
            }
        };
        
        submitTask.setOnSucceeded(event -> {
            Complaint saved = submitTask.getValue();
            progressIndicator.setVisible(false);
            showMessage("Complaint #" + saved.getId() + " submitted successfully!", false);
            clearForm();
            submitButton.setDisable(false);
        });
        
        submitTask.setOnFailed(event -> {
            progressIndicator.setVisible(false);
            showMessage("Failed: " + submitTask.getException().getMessage(), true);
            submitButton.setDisable(false);
        });
        
        new Thread(submitTask).start();
    }

    private boolean validateForm() {
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            showMessage("Please enter a complaint title", true);
            titleField.requestFocus();
            return false;
        }
        
        if (titleField.getText().length() < 5) {
            showMessage("Title must be at least 5 characters", true);
            titleField.requestFocus();
            return false;
        }
        
        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            showMessage("Please enter a complaint description", true);
            descriptionArea.requestFocus();
            return false;
        }
        
        if (descriptionArea.getText().length() < 10) {
            showMessage("Description must be at least 10 characters", true);
            descriptionArea.requestFocus();
            return false;
        }
        
        return true;
    }

    private int getDepartmentId(String departmentName) {
        switch (departmentName) {
            case "Academic Affairs": return 1;
            case "IT Services": return 2;
            case "Library": return 3;
            case "Student Affairs": return 4;
            case "Facilities Management": return 5;
            case "Finance Department": return 6;
            default: return 4;
        }
    }

    private void showMessage(String message, boolean isError) {
        Platform.runLater(() -> {
            statusMessage.setText(message);
            statusMessage.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
            statusMessage.setVisible(true);
            
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    Platform.runLater(() -> statusMessage.setVisible(false));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }

    private void clearForm() {
        Platform.runLater(() -> {
            titleField.clear();
            descriptionArea.clear();
            priorityChoice.setValue("MEDIUM");
            departmentChoice.setValue("Auto-assign");
            selectedFiles.clear();
            selectedFilesLabel.setText("No files selected");
        });
    }
}