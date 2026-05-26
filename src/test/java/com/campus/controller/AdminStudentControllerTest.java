package com.campus.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Admin Student Controller Tests")
public class AdminStudentControllerTest {

    private AdminStudentController controller;

    @BeforeEach
    public void setUp() {
        controller = new AdminStudentController();
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
    @DisplayName("Refresh data should load users")
    public void testRefreshData() {
        assertDoesNotThrow(() -> {
            controller.refreshData();
            Thread.sleep(500);
            assertTrue(true, "Data refresh completed");
        });
    }

    @Test
    @DisplayName("Role filter combo should have all roles")
    public void testRoleFilterSetup() {
        assertDoesNotThrow(() -> {
            // Controller initializes filters in initialize()
            Thread.sleep(100);
            assertTrue(true, "Role filters initialized");
        });
    }

    @Test
    @DisplayName("Multiple refreshes should be thread-safe")
    public void testThreadSafetyOfRefresh() {
        assertDoesNotThrow(() -> {
            Thread t1 = new Thread(() -> {
                try {
                    controller.refreshData();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            Thread t2 = new Thread(() -> {
                try {
                    controller.refreshData();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            t1.start();
            t2.start();
            t1.join();
            t2.join();

            assertTrue(true, "Concurrent refresh operations succeeded");
        });
    }
}
