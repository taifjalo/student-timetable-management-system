package org.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LessonTest {
    @Test
    @DisplayName("Lesson: Parameterized constructor should set all fields")
    void lessonParameterizedConstructor() {
        Course course = new Course("Math", "#FF0000");
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 10, 30);

        Lesson lesson = new Lesson(start, end, course, "A101");

        assertEquals(start, lesson.getStartAt());
        assertEquals(end, lesson.getEndAt());
        assertSame(course, lesson.getCourse());
        assertEquals("A101", lesson.getClassroom());
        assertNotNull(lesson.getAssignedGroups());
        assertNotNull(lesson.getAssignedUsers());
        assertTrue(lesson.getAssignedGroups().isEmpty());
        assertTrue(lesson.getAssignedUsers().isEmpty());
    }

    @Test
    @DisplayName("Lesson: setAssignedGroups and setAssignedUsers should update lists")
    void lessonAssignedCollections() {
        Lesson lesson = new Lesson();
        StudentGroup group = new StudentGroup("G1", "CS");
        User user = new User();

        lesson.setAssignedGroups(List.of(group));
        lesson.setAssignedUsers(List.of(user));

        assertEquals(1, lesson.getAssignedGroups().size());
        assertEquals(1, lesson.getAssignedUsers().size());
    }

    @Test
    @DisplayName("Lesson: All setters should work correctly")
    void lessonSetters() {
        Course course = new Course("Physics", "#0000FF");
        LocalDateTime start = LocalDateTime.of(2026, 4, 1, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 1, 11, 0);

        Lesson lesson = new Lesson();
        lesson.setId(42L);
        lesson.setStartAt(start);
        lesson.setEndAt(end);
        lesson.setCourse(course);
        lesson.setClassroom("B202");

        assertEquals(42L, lesson.getId());
        assertEquals(start, lesson.getStartAt());
        assertEquals(end, lesson.getEndAt());
        assertSame(course, lesson.getCourse());
        assertEquals("B202", lesson.getClassroom());
    }

}