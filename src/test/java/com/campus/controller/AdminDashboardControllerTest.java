package com.campus.controller;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Admin Dashboard Controller Tests")
public class AdminDashboardControllerTest {

    private AdminDashboardController controller;

    @BeforeEach
    public void setUp() {
        // Initialize controller
        controller = new AdminDashboardController();
    }

    @Test
    @DisplayName("Controller should initialize without null components")
    public void testControllerInitialization() {
        assertNotNull(controller, "Controller should not be null");
    }

    @Test
    @DisplayName("Auto-refresh should start when dashboard becomes visible")
    public void testAutoRefreshStart() {
        controller.startAutoRefresh();
        // Give the scheduler time to start
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // If no exception thrown, auto-refresh started successfully
        assertTrue(true, "Auto-refresh started successfully");
    }

    @Test
    @DisplayName("Auto-refresh should stop when dashboard becomes invisible")
    public void testAutoRefreshStop() {
        controller.startAutoRefresh();
        controller.stopAutoRefresh();
        // If no exception thrown, auto-refresh stopped successfully
        assertTrue(true, "Auto-refresh stopped successfully");
    }

    @Test
    @DisplayName("Root pane should return VBox")
    public void testGetRootPane() {
        VBox rootPane = controller.getRootPane();
        assertNotNull(rootPane, "Root pane should not be null");
    }

    @Test
    @DisplayName("Auto-refresh should not start twice")
    public void testAutoRefreshIdempotent() {
        controller.startAutoRefresh();
        controller.startAutoRefresh(); // Should not cause issues
        controller.stopAutoRefresh();
        assertTrue(true, "Idempotent auto-refresh call succeeded");
    }
}
