package com.compass.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Loads FXML scenes and navigates between views.
 */
public final class SceneNavigator {
    private static Stage primaryStage;

    private SceneNavigator() {}

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void show(String fxmlPath, String title, int width, int height) {
        try {
            Parent root = loadFXML(fxmlPath);
            Scene scene = new Scene(root, width, height);
            URL css = SceneNavigator.class.getResource("/css/main.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.setWidth(width);
            primaryStage.setHeight(height);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + fxmlPath, e);
        }
    }

    public static <T> T loadController(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                SceneNavigator.class.getResource(fxmlPath), "FXML not found: " + fxmlPath));
        loader.load();
        return loader.getController();
    }

    private static Parent loadFXML(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                SceneNavigator.class.getResource(fxmlPath), "FXML not found: " + fxmlPath));
        return loader.load();
    }
}
