package com.compass.controllers;

import com.compass.config.Session;
import com.compass.config.View;
import com.compass.models.CampusLocation;
import com.compass.models.CompassData;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class MapController {
    @FXML private WebView mapWebView;
    @FXML private ComboBox<CampusLocation> fromCombo;
    @FXML private ComboBox<CampusLocation> toCombo;
    @FXML private Label routeLabel;

    private final CompassData data = new CompassData();
    private List<CampusLocation> locations;

    @FXML
    private void initialize() {
        locations = data.locations();
        setupLocationCombo(fromCombo);
        setupLocationCombo(toCombo);
        if (!locations.isEmpty()) {
            fromCombo.getSelectionModel().selectFirst();
            toCombo.getSelectionModel().select(Math.min(1, locations.size() - 1));
        }
        loadMap();
    }

    private void setupLocationCombo(ComboBox<CampusLocation> combo) {
        combo.getItems().setAll(locations);
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CampusLocation item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLocationName());
            }
        });
        combo.setButtonCell(combo.getCellFactory().call(null));
    }

    private void loadMap() {
        URL mapUrl = getClass().getResource("/public/web/campus_map.html");
        if (mapUrl != null) {
            mapWebView.getEngine().load(mapUrl.toExternalForm());
            mapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, state) -> {
                if (state == javafx.concurrent.Worker.State.SUCCEEDED) {
                    drawLocations();
                }
            });
        }
    }

    private void drawLocations() {
        String js = "setLocations([" + locations.stream()
                .map(location -> String.format("{name:'%s',lat:%f,lng:%f}",
                        escape(location.getLocationName()),
                        location.getLatitude(),
                        location.getLongitude()))
                .collect(Collectors.joining(",")) + "]);";
        mapWebView.getEngine().executeScript(js);
    }

    @FXML
    private void handleFindRoute() {
        CampusLocation start = fromCombo.getValue();
        CampusLocation destination = toCombo.getValue();
        CompassData.Route route = data.route(start, destination);
        if (route.locations().isEmpty()) {
            routeLabel.setText("Select a start location and destination.");
            return;
        }
        routeLabel.setText(String.format("%s (about %.0f meters)",
                data.formatRoute(route.locations()),
                route.totalDistanceMeters()));

        String routeJs = "drawRoute([" + route.locations().stream()
                .map(location -> String.format("{lat:%f,lng:%f}", location.getLatitude(), location.getLongitude()))
                .collect(Collectors.joining(",")) + "]);";
        mapWebView.getEngine().executeScript(routeJs);
    }

    @FXML
    private void handleBack() {
        if (Session.isAdmin()) {
            View.show("admin_dashboard", "Compass - Admin Dashboard", 1200, 750);
        } else {
            View.show("student_dashboard", "Compass - Student Dashboard", 1000, 650);
        }
    }

    private String escape(String value) {
        return value.replace("'", "\\'");
    }
}
