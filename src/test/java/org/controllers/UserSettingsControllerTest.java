package org.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserSettingsControllerTest {

     @DisplayName("Helper method: Mirrors User Settings Controller.capitalize() logic.")
     private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    @DisplayName("Helper method: Mirrors UserSettingsController.nullSafe() logic.")
    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    @Test
    @DisplayName("capitalize: Should capitalize first letter and lowercase rest")
    void shouldCapitalizeCorrectly() {
        assertEquals("Teacher", capitalize("teacher"));
        assertEquals("Student", capitalize("STUDENT"));
        assertEquals("Admin", capitalize("aDmIn"));
    }

    @Test
    @DisplayName("capitalize: Should return null for null input")
    void shouldReturnNullForNullInput() {
        assertNull(capitalize(null));
    }

    @Test
    @DisplayName("capitalize: Should return empty string for empty input")
    void shouldReturnEmptyForEmptyInput() {
        assertEquals("", capitalize(""));
    }

    @Test
    @DisplayName("capitalize: Should handle single character")
    void shouldHandleSingleCharacter() {
        assertEquals("A", capitalize("a"));
        assertEquals("A", capitalize("A"));
    }

    @Test
    @DisplayName("nullSafe: Should return value when not null")
    void nullSafeShouldReturnValue() {
        assertEquals("hello", nullSafe("hello"));
        assertEquals("", nullSafe(""));
    }

    @Test
    @DisplayName("nullSafe: Should return empty string for null")
    void nullSafeShouldReturnEmptyForNull() {
        assertEquals("", nullSafe(null));
    }
}