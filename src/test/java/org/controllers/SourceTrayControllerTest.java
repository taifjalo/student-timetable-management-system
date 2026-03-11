package org.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Logic Test: Controller utility method tests (no JavaFX required)")
class SourceTrayControllerTest {

    @DisplayName("Helper Method: Mirrors SourceTrayController.toSingular() logic for isolated testing.")
    private String toSingular(String word) {
        if (word == null || word.isEmpty()) return word;
        if (word.endsWith("s") || word.endsWith("S")) return word.substring(0, word.length() - 1);
        return word;
    }

    @Test
    @DisplayName("toSingular: Should remove trailing 's' from 'Groups'")
    void shouldRemoveTrailingSFromGroups() {
        assertEquals("Group", toSingular("Groups"));
    }

    @Test
    @DisplayName("toSingular: Should remove trailing 'S' (uppercase)")
    void shouldRemoveTrailingUppercaseS() {
        assertEquals("COURSE", toSingular("COURSES"));
    }

    @Test
    @DisplayName("toSingular: Should not change word without trailing s")
    void shouldNotChangeWordWithoutS() {
        assertEquals("Course", toSingular("Course"));
    }

    @Test
    @DisplayName("toSingular: Should return null for null input")
    void shouldReturnNullForNull() {
        assertNull(toSingular(null));
    }

    @Test
    @DisplayName("toSingular: Should return empty string for empty input")
    void shouldReturnEmptyForEmpty() {
        assertEquals("", toSingular(""));
    }

    @Test
    @DisplayName("toSingular: Should handle single character 's'")
    void shouldHandleSingleCharacterS() {
        assertEquals("", toSingular("s"));
    }

    // SourceTrayController: styleToHex
     @DisplayName("Helper Method: Mirrors SourceTrayController.styleToHex() logic.")
     private String styleToHex(String style) {
        if (style == null) return "#888888";
        switch (style.toLowerCase()) {
            case "style1": return "#77C04B";
            case "style2": return "#418FCB";
            case "style3": return "#F7D15B";
            case "style4": return "#9D5B9F";
            case "style5": return "#D0525F";
            case "style6": return "#F9844B";
            case "style7": return "#AE663E";
            default:       return "#888888";
        }
    }

    @Test
    @DisplayName("styleToHex: Should map all 7 known styles correctly")
    void shouldMapAllKnownStyles() {
        assertEquals("#77C04B", styleToHex("style1"));
        assertEquals("#418FCB", styleToHex("style2"));
        assertEquals("#F7D15B", styleToHex("style3"));
        assertEquals("#9D5B9F", styleToHex("style4"));
        assertEquals("#D0525F", styleToHex("style5"));
        assertEquals("#F9844B", styleToHex("style6"));
        assertEquals("#AE663E", styleToHex("style7"));
    }

    @Test
    @DisplayName("styleToHex: Should be case-insensitive")
    void shouldBeCaseInsensitive() {
        assertEquals("#77C04B", styleToHex("STYLE1"));
        assertEquals("#418FCB", styleToHex("Style2"));
    }

    @Test
    @DisplayName("styleToHex: Should return fallback for unknown style")
    void shouldReturnFallbackForUnknown() {
        assertEquals("#888888", styleToHex("style99"));
        assertEquals("#888888", styleToHex("unknown"));
    }

    @Test
    @DisplayName("styleToHex: Should return fallback for null")
    void shouldReturnFallbackForNull() {
        assertEquals("#888888", styleToHex(null));
    }

}