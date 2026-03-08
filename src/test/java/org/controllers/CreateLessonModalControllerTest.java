package org.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CreateLessonModalControllerTest {

    // Verify the controller class exists and can be loaded
    @Test
    void testControllerStructureExists() {
        try {
            Class<?> clazz = Class.forName("org.controllers.CreateLessonModalController");
            assertNotNull(clazz, "CreateLessonModalController class should exist");
        } catch (ClassNotFoundException e) {
            fail("CreateLessonModalController class not found");
        }
    }

    // Verify the hardcoded recipient IDs are set correctly for alerts
    @Test
    void testHardcodedAlertIsPresent() {
        try {
            Class<?> clazz = Class.forName("org.controllers.CreateLessonModalController");
            java.lang.reflect.Field field = clazz.getDeclaredField("HARDCODED_RECIPIENT_IDS");
            field.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            List<Long> recipients = (List<Long>) field.get(null);
            
            assertNotNull(recipients, "HARDCODED_RECIPIENT_IDS should not be null");
            assertFalse(recipients.isEmpty(), "HARDCODED_RECIPIENT_IDS should not be empty");
            assertEquals(1L, recipients.get(0), "Hardcoded recipient ID should be 1L");
        } catch (Exception e) {
            fail("Failed to verify hardcoded alert logic: " + e.getMessage());
        }
    }
}
