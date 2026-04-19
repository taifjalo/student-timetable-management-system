package org.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class StudentProfileTest {

    @Test
    void constructorAndGettersShouldReturnAssignedValues() {
        User user = new User();
        user.setId(100L);
        StudentGroup group = new StudentGroup();
        group.setGroupCode("G-1");
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 8, 0);
        LocalDate end = LocalDate.of(2029, 12, 31);

        StudentProfile profile = new StudentProfile(user, start, end, group);

        assertSame(user, profile.getUser());
        assertEquals(start, profile.getStudyStartDate());
        assertEquals(end, profile.getStudyEndDate());
        assertSame(group, profile.getStudentGroup());
    }

    @Test
    void getUserIdDefaultsToNullWhenNotMappedByJpaYet() {
        StudentProfile profile = new StudentProfile();
        assertNull(profile.getUserId());
    }

    @Test
    void setUserShouldUpdateUserReference() {
        StudentProfile profile = new StudentProfile();
        User user = new User();
        user.setId(7L);

        profile.setUser(user);

        assertSame(user, profile.getUser());
    }

    @Test
    void getStudyStartDateReturnsNullByDefault() {
        StudentProfile profile = new StudentProfile();
        assertNull(profile.getStudyStartDate());
    }

    @Test
    void setStudyStartDateShouldUpdateValue() {
        StudentProfile profile = new StudentProfile();
        LocalDateTime start = LocalDateTime.of(2026, 2, 2, 9, 30);

        profile.setStudyStartDate(start);

        assertEquals(start, profile.getStudyStartDate());
    }

    @Test
    void getStudyEndDateReturnsNullByDefault() {
        StudentProfile profile = new StudentProfile();
        assertNull(profile.getStudyEndDate());
    }

    @Test
    void setStudyEndDateShouldUpdateValue() {
        StudentProfile profile = new StudentProfile();
        LocalDate end = LocalDate.of(2030, 6, 1);

        profile.setStudyEndDate(end);

        assertEquals(end, profile.getStudyEndDate());
    }

    @Test
    void getStudentGroupReturnsNullByDefault() {
        StudentProfile profile = new StudentProfile();
        assertNull(profile.getStudentGroup());
    }

    @Test
    void setStudentGroupShouldUpdateGroupReference() {
        StudentProfile profile = new StudentProfile();
        StudentGroup group = new StudentGroup();
        group.setGroupCode("G-99");

        profile.setStudentGroup(group);

        assertSame(group, profile.getStudentGroup());
    }
}
