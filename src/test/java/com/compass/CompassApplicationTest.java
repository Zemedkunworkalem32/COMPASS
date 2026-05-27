package com.compass;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test class to verify project setup
 */
public class CompassApplicationTest {

    @Test
    public void testApplicationSetup() {
        // Placeholder test - verify project structure is correct
        assertNotNull(CompassApplication.class, "Application class should exist");
    }

    @Test
    public void testDependenciesAvailable() {
        // Test that required dependencies are available
        try {
            // JavaFX
            Class.forName("javafx.application.Application");
            
            // MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // SLF4J
            Class.forName("org.slf4j.Logger");
            
            // JUnit
            Class.forName("org.junit.jupiter.api.Test");
            
            assertTrue(true, "All required dependencies available");
        } catch (ClassNotFoundException e) {
            fail("Missing required dependency: " + e.getMessage());
        }
    }
}
