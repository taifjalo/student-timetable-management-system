package org.controllers;

import org.junit.jupiter.api.Test;
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

    // Verify the controller does not have a hardcoded recipient list (recipients are
    // derived from the lesson's assigned users instead of a static field)
    @Test
    void testNoHardcodedRecipientIds() {
        try {
            Class<?> clazz = Class.forName("org.controllers.CreateLessonModalController");
            try {
                clazz.getDeclaredField("HARDCODED_RECIPIENT_IDS");
                fail("HARDCODED_RECIPIENT_IDS field should have been removed");
            } catch (NoSuchFieldException expected) {
                // expected — the field was replaced with dynamic recipient derivation
            }
        } catch (ClassNotFoundException e) {
            fail("CreateLessonModalController class not found");
        }
    }
}
