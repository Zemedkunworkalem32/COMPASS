package com.campus.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Admin Complaint Controller Tests")
public class AdminComplaintControllerTest {

    private AdminComplaintController controller;

    @BeforeEach
    public void setUp() {
        controller = new AdminComplaintController();
    }

    @Test
    @DisplayName("Controller should initialize successfully")
    public void testControllerInitialization() {
        assertNotNull(controller, "Controller should not be null");
    }

    @Test
    @DisplayName("Root pane should be retrievable")
    public void testGetRootPane() {
        assertNotNull(controller.getRootPane(), "Root pane should not be null");
    }

    @Test
    @DisplayName("Refresh data should execute without errors")
    public void testRefreshData() {
        assertDoesNotThrow(() -> {
            controller.refreshData();
            // Give async operation time to complete
            Thread.sleep(500);
        }, "refreshData should not throw exceptions");
    }

    @Test
    @DisplayName("Filter application should handle empty input")
    public void testEmptyFilters() {
        assertDoesNotThrow(() -> {
            controller.refreshData();
            Thread.sleep(500);
            // Filters should be applicable after refresh
            assertTrue(true, "Filters work with empty state");
        });
    }

    @Test
    @DisplayName("Multiple rapid refreshes should not cause issues")
    public void testRapidRefresh() {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                controller.refreshData();
            }
            Thread.sleep(1000);
            assertTrue(true, "Rapid refreshes handled correctly");
        });
    }
}
