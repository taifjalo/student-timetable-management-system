package org.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CourseTest {

    @Test
    @DisplayName("Course: Default constructor and setters should work correctly")
    void courseDefaultConstructorAndSetters() {
        Course course = new Course();
        course.setId(1L);
        course.setName("Mathematics");
        course.setColorCode("#FF0000");

        assertEquals(1L, course.getId());
        assertEquals("Mathematics", course.getName());
        assertEquals("#FF0000", course.getColorCode());
    }

    @Test
    @DisplayName("Course: Parameterized constructor should set name and colorCode")
    void courseParameterizedConstructor() {
        Course course = new Course("Physics", "#00FF00");

        assertEquals("Physics", course.getName());
        assertEquals("#00FF00", course.getColorCode());
        assertNull(course.getId()); // ID not set yet
    }
}