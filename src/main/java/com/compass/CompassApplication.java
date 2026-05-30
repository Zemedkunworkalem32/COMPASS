package com.compass;

import com.compass.config.Session;
import com.compass.config.View;
import com.compass.config.Database;
import com.compass.models.CompassData;
import javafx.application.Application;
import javafx.stage.Stage;

public class CompassApplication extends Application {
    private final CompassData data = new CompassData();

    @Override
    public void start(Stage primaryStage) {
        try {
            View.init(primaryStage);
            if (Database.initialize()) {
                data.seedDefaultUsers();
            }
            primaryStage.setOnCloseRequest(event -> {
                Session.logout();
                System.exit(0);
            });
            View.show("login", "Compass - Campus Complaint Management", 800, 600);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
