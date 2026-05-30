package com.compass.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public final class View {
    private static Stage stage;

    private View() {}

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static Stage stage() {
        return stage;
    }

    public static void show(String name, String title, int width, int height) {
        try {
            Parent root = load("/views/" + name + ".fxml");
            Scene scene = new Scene(root, width, height);
            URL css = View.class.getResource("/public/css/main.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.centerOnScreen();
        } catch (IOException e) {
            throw new RuntimeException("Could not load view: " + name, e);
        }
    }

    private static Parent load(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                View.class.getResource(path), "View not found: " + path));
        return loader.load();
    }
}
