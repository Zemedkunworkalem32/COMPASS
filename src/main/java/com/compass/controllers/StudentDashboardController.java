package com.compass.controllers;

import com.compass.config.Session;
import com.compass.config.View;
import com.compass.models.CompassData;
import com.compass.models.Complaint;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class StudentDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private TableView<Complaint> complaintsTable;
    @FXML private TableColumn<Complaint, String> idCol;
    @FXML private TableColumn<Complaint, String> titleCol;
    @FXML private TableColumn<Complaint, String> categoryCol;
    @FXML private TableColumn<Complaint, String> statusCol;
    @FXML private TableColumn<Complaint, String> noteCol;
    @FXML private TableColumn<Complaint, String> dateCol;

    private final CompassData data = new CompassData();

    @FXML
    private void initialize() {
        welcomeLabel.setText("Welcome, " + Session.username());
        setupTable();
        refreshTable();
    }

    private void setupTable() {
        idCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getComplaintId())));
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        categoryCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().label()));
        noteCol.setCellValueFactory(c -> new SimpleStringProperty(blankToDash(c.getValue().getAdminNote())));
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedAt() == null ? "" : c.getValue().getCreatedAt().toString()));
    }

    private void refreshTable() {
        complaintsTable.setItems(FXCollections.observableArrayList(data.studentComplaints(Session.userId())));
    }

    @FXML
    private void handleSubmitComplaint() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/views/complaint_form.fxml"));
            javafx.scene.Parent root = loader.load();
            ComplaintFormController form = loader.getController();
            form.setRoot(root);

            Stage dialog = new Stage();
            dialog.initOwner(View.stage());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Submit Complaint");
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 520, 380);
            var css = getClass().getResource("/public/css/main.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            dialog.setScene(scene);
            form.setOnSuccess(() -> {
                dialog.close();
                refreshTable();
            });
            form.setOnCancel(dialog::close);
            dialog.showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Could not open form: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleViewDetails() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.INFORMATION, "Select a complaint first.").showAndWait();
            return;
        }

        Alert details = new Alert(Alert.AlertType.INFORMATION);
        details.setTitle("Complaint #" + selected.getComplaintId());
        details.setHeaderText(selected.getTitle());
        VBox content = new VBox(8,
                new Label("Category: " + selected.getCategory()),
                new Label("Status: " + selected.getStatus().label()),
                new Label("Submitted: " + (selected.getCreatedAt() == null ? "" : selected.getCreatedAt())),
                new Label("Description:"),
                readOnlyArea(selected.getDescription(), 4),
                new Label("Admin Notes:"),
                readOnlyArea(blankToDash(selected.getAdminNote()), 3)
        );
        content.setPadding(new Insets(8));
        details.getDialogPane().setContent(content);
        details.showAndWait();
    }

    @FXML
    private void handleOpenMap() {
        View.show("map_view", "Compass - AASTU Campus Map", 1000, 700);
    }

    @FXML
    private void handleLogout() {
        Session.logout();
        View.show("login", "Compass - Login", 800, 600);
    }

    private TextArea readOnlyArea(String text, int rows) {
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(rows);
        return area;
    }

    private String blankToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
