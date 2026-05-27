package com.compass.controller;

import com.compass.model.CampusLocation;
import com.compass.navigation.RouteFinder;
import com.compass.service.RepositoryFactory;
import com.compass.util.SceneNavigator;
import com.compass.util.SessionManager;
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
        plotLocations();
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
        combo.setButtonCell(combo.getCellFactory().call(null));
    }

    private void loadMap() {
        URL mapUrl = getClass().getResource("/web/campus_map.html");
        if (mapUrl != null) {
            mapWebView.getEngine().load(mapUrl.toExternalForm());
        }
    }

    private void plotLocations() {
        mapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == javafx.concurrent.Worker.State.SUCCEEDED) {
                String js = "setLocations([" + locations.stream()
                        .map(loc -> String.format("{name:'%s',lat:%f,lng:%f}",
                                loc.getLocationName().replace("'", "\\'"),
                                loc.getLatitude(), loc.getLongitude()))
                        .collect(Collectors.joining(",")) + "]);";
                mapWebView.getEngine().executeScript(js);
            }
        });
    }

    @FXML
    private void handleFindRoute() {
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
        String routeJs = "drawRoute([" + result.path().stream()
                .map(id -> locations.stream().filter(l -> l.getLocationId() == id).findFirst().orElse(null))
                .filter(loc -> loc != null)
                .map(loc -> String.format("{lat:%f,lng:%f}", loc.getLatitude(), loc.getLongitude()))
                .collect(Collectors.joining(",")) + "]);";
        mapWebView.getEngine().executeScript(routeJs);
    }

    @FXML
    private void handleBack() {
        SessionManager session = SessionManager.getInstance();
        if (session.isAdmin()) {
            SceneNavigator.show("/fxml/admin_dashboard.fxml", "Compass - Admin", 1100, 700);
        } else {
            SceneNavigator.show("/fxml/student_dashboard.fxml", "Compass - Student", 1000, 650);
        }
    }
}
