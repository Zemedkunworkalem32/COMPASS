package com.compass.controller;

import com.compass.model.CampusLocation;
import com.compass.navigation.RouteFinder;
import com.compass.service.RepositoryFactory;
import com.compass.util.SceneNavigator;
import com.compass.util.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Worker;
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

    private List<CampusLocation> locations;
    private final RouteFinder routeFinder = new RouteFinder();
    private boolean isMapReady = false;

    @FXML
    private void initialize() {
        locations = RepositoryFactory.locations().findAll();
        setupLocationCombo(fromCombo);
        setupLocationCombo(toCombo);
        if (!locations.isEmpty()) {
            fromCombo.getSelectionModel().select(0);
            toCombo.getSelectionModel().select(Math.min(1, locations.size() - 1));
        }
        loadMap();
    }

    private void setupLocationCombo(ComboBox<CampusLocation> combo) {
        combo.getItems().addAll(locations);
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CampusLocation item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLocationName());
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CampusLocation item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLocationName());
            }
        });
    }

    private void loadMap() {
        URL mapUrl = getClass().getResource("/web/campus_map.html");
        if (mapUrl != null) {
            javafx.scene.web.WebEngine engine = mapWebView.getEngine();

            engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
                System.out.println("Map Engine Loading State Changed: " + newState);
                if (newState == Worker.State.SUCCEEDED) {
                    isMapReady = true;
                    routeLabel.setText("Map Ready");
                    plotLocations();

                    // Force an initial repaint check
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(150);
                            engine.executeScript("map.invalidateSize();");
                        } catch (InterruptedException ignored) {}
                    });
                }
            });
            engine.load(mapUrl.toExternalForm());
        } else {
            routeLabel.setText("Failed to locate map asset file.");
        }
    }

    private void plotLocations() {
        if (!isMapReady || locations.isEmpty()) return;
        try {
            String js = "setLocations([" + locations.stream()
                    .map(loc -> String.format("{name:'%s',lat:%f,lng:%f}",
                            loc.getLocationName().replace("'", "\\'"),
                            loc.getLatitude(), loc.getLongitude()))
                    .collect(Collectors.joining(",")) + "]);";
            mapWebView.getEngine().executeScript(js);
        } catch (Exception e) {
            System.err.println("Failed to execute setLocations script: " + e.getMessage());
        }
    }

    @FXML
    private void handleFindRoute() {
        if (!isMapReady) {
            routeLabel.setText("Map engine is still loading, please wait...");
            return;
        }

        CampusLocation from = fromCombo.getValue();
        CampusLocation to = toCombo.getValue();
        if (from == null || to == null) {
            routeLabel.setText("Select start and destination buildings");
            return;
        }

        RouteFinder.RouteResult result = routeFinder.findShortestPath(
                from.getLocationId(), to.getLocationId());
        if (result.path().isEmpty()) {
            routeLabel.setText("No route found between selected buildings");
            return;
        }

        routeLabel.setText(String.format("%s (%.0f meters)",
                routeFinder.formatRoute(result.path(), locations),
                result.totalDistanceMeters()));

        try {
            String coordinateData = result.path().stream()
                    .map(id -> locations.stream()
                            .filter(l -> l.getLocationId() == id)
                            .findFirst()
                            .orElse(null))
                    .filter(loc -> loc != null)
                    .map(loc -> String.format("{lat:%f,lng:%f}", loc.getLatitude(), loc.getLongitude()))
                    .collect(Collectors.joining(","));

            String routeJs = "drawRoute([" + coordinateData + "]);";
            mapWebView.getEngine().executeScript(routeJs);
        } catch (Exception e) {
            System.err.println("Failed to execute drawRoute script: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        SessionManager session = SessionManager.getInstance();
        if (session.isAdmin()) {
            SceneNavigator.show("/fxml/admin_dashboard.fxml", "Compass - Admin Dashboard", 1100, 700);
        } else {
            SceneNavigator.show("/fxml/student_dashboard.fxml", "Compass - Student Dashboard", 1000, 650);
        }
    }
}