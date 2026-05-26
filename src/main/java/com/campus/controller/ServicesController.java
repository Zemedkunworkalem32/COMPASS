package com.campus.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ServicesController {

    @FXML
    private VBox rootPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox servicesContainer;

    @FXML
    public void initialize() {
        setupServices();
    }

    private void setupServices() {
        servicesContainer.setPadding(new Insets(20));
        servicesContainer.setSpacing(15);

        Label titleLabel = new Label("Campus Services");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 24));
        servicesContainer.getChildren().add(titleLabel);

        addServiceCategory("Housing Services", "Housing@campus.edu",
                "Apply for on-campus housing, manage room assignments, resolve housing-related issues");
        addServiceCategory("IT Support", "ITSupport@campus.edu",
                "Technical assistance, software licenses, network issues, device support");
        addServiceCategory("Facilities Management", "Facilities@campus.edu",
                "Maintenance requests, facility bookings, cleaning services, repairs");
        addServiceCategory("Library Services", "Library@campus.edu",
                "Book access, study room reservations, research assistance, borrowing services");
        addServiceCategory("Student Health", "Health@campus.edu",
                "Medical appointments, health consultations, wellness programs, emergency care");
        addServiceCategory("Academic Support", "AcademicSupport@campus.edu",
                "Tutoring, writing center, academic advising, career counseling");
    }

    private void addServiceCategory(String name, String email, String description) {
        VBox serviceBox = new VBox();
        serviceBox.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-padding: 15;");
        serviceBox.setSpacing(8);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font(null, FontWeight.BOLD, 16));

        Label emailLabel = new Label("Email: " + email);
        emailLabel.setStyle("-fx-text-fill: #0066cc; -fx-font-size: 12;");

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 12;");

        serviceBox.getChildren().addAll(nameLabel, emailLabel, descLabel);
        servicesContainer.getChildren().add(serviceBox);
    }

    public VBox getRootPane() {
        return rootPane;
    }
}
