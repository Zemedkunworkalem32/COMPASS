package com.compass;

import com.compass.db.DataSeeder;
import com.compass.util.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Campus Complaint Management System - Main Application Entry Point
 */
public class CompassApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(CompassApplication.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting Campus Complaint Management System");
            SceneNavigator.init(primaryStage);
            DataSeeder.seedIfNeeded();
            primaryStage.setOnCloseRequest(event -> {
                logger.info("Application closing");
                System.exit(0);
            });
            SceneNavigator.show("/fxml/login.fxml", "Compass - Campus Complaint Management", 800, 600);
            primaryStage.show();
            logger.info("Application started successfully");
        } catch (Exception e) {
            logger.error("Error starting application", e);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}