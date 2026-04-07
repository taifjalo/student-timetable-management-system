package org.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentGroupTest {

    @Test
    @DisplayName("StudentGroup: Default constructor and setters should work correctly")
    void studentGroupDefaultConstructor() {
        StudentGroup group = new StudentGroup();
        group.setGroupCode("CS-A1");
        group.setFieldOfStudies("Computer Science");

        assertEquals("CS-A1", group.getGroupCode());
        assertEquals("Computer Science", group.getFieldOfStudies());
    }

    @Test
    @DisplayName("StudentGroup: Parameterized constructor should set fields")
    void studentGroupParameterizedConstructor() {
        StudentGroup group = new StudentGroup("SE-B2", "Software Engineering");

        assertEquals("SE-B2", group.getGroupCode());
        assertEquals("Software Engineering", group.getFieldOfStudies());
    }
}
