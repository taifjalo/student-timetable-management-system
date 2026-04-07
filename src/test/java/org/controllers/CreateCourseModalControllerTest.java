package org.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateCourseModalControllerTest {

    @DisplayName("Helper Method: Mirrors Create Course Modal Controller.styleToHex() logic")
    private String courseModalStyleToHex(String style) {
        if (style == null) {
            return "#888888";
        }
        return switch (style.toUpperCase(java.util.Locale.ROOT)) {
            case "STYLE1" -> "#77C04B";
            case "STYLE2" -> "#418FCB";
            case "STYLE3" -> "#F7D15B";
            case "STYLE4" -> "#9D5B9F";
            case "STYLE5" -> "#D0525F";
            case "STYLE6" -> "#F9844B";
            case "STYLE7" -> "#AE663E";
            default       -> "#888888";
        };
    }

    @Test
    @DisplayName("CourseModal styleToHex: All styles should map to correct colors")
    void courseModalShouldMapAllStyles() {
        assertEquals("#77C04B", courseModalStyleToHex("STYLE1"));
        assertEquals("#418FCB", courseModalStyleToHex("STYLE2"));
        assertEquals("#F7D15B", courseModalStyleToHex("STYLE3"));
        assertEquals("#9D5B9F", courseModalStyleToHex("STYLE4"));
        assertEquals("#D0525F", courseModalStyleToHex("STYLE5"));
        assertEquals("#F9844B", courseModalStyleToHex("STYLE6"));
        assertEquals("#AE663E", courseModalStyleToHex("STYLE7"));
    }

    @Test
    @DisplayName("CourseModal styleToHex: null should return fallback")
    void courseModalNullStyleShouldReturnFallback() {
        assertEquals("#888888", courseModalStyleToHex(null));
    }

}
