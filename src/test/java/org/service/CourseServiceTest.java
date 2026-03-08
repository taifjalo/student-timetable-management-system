package org.service;

import org.dao.CourseDao;
import org.dao.LessonDao;
import org.entities.Course;
import org.entities.Lesson;
import org.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Logic Test: CourseService tests with JUnit 5 & Mockito")
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    CourseDao courseDao;

    @InjectMocks
    CourseService courseService;

    @DisplayName("Helper Method: First Should Create User")
    private User createUser(Long id, String firstName, String lastName, String userName, String password, String phoneNumber, String email) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setUsername(lastName);
        user.setUsername(userName);
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setEmail(user.getUsername() + "@gmail.com");
        user.setRole("TEACHER");
        user.setPhoneNumber(phoneNumber);
        return user;
    }

    @DisplayName("Helper Method: First Should Create Course")
    private Course createCourse(Long id, String name, String colorCode) {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        course.setColorCode(colorCode);
        return course;
    }

    @Test
    @DisplayName("Add Course: Should successfully add a course when all fields are valid")
    void shouldAddCourseSuccessfully() {
        Course savedCourse = createCourse(1L, "Math", "#FF0000");

        when(courseDao.save(any(Course.class))).thenReturn(savedCourse);

        Course result = courseService.createCourse("Math", "A101");

        assertEquals("Math", result.getName());
        verify(courseDao).save(any(Course.class));
    }

    @Test
    @DisplayName("Add Course: Should Throw Exception When Name Is Blank (Empty)")
    void shouldThrowExceptionWhenNameIsBlank() {

        assertThrows(
                IllegalArgumentException.class,
                () -> courseService.createCourse("", "#FF8C00")
        );
    }

    @Test
    @DisplayName("Update Course: Should successfully update a course when all fields are valid")
    void shouldUpdateCourse() {

        Course existing = createCourse(1L, "Math", "#FF0000");
        when(courseDao.findById(1L)).thenReturn(existing);
        when(courseDao.save(any())).thenReturn(existing);

        Course result = courseService.updateCourse(1L, "Physics", "#800080");
        assertEquals("Physics", result.getName());
        verify(courseDao).save(existing);
    }

    @Test
    @DisplayName("Delete Course: Should successfully Delete a course when all fields are valid")
    void shouldDeleteCourseSuccessfully() {
        Course existing = createCourse(1L, "Math", "#FF0000");
        when(courseDao.findById(1L)).thenReturn(existing);

        courseService.deleteCourse(1L);

        verify(courseDao).delete(1L);
    }

    @Test
    @DisplayName("Color Course: Should Map Color To Style")
    void shouldMapColorToStyle() {

        assertEquals(
                com.calendarfx.model.Calendar.Style.STYLE2,
                CourseService.colorCodeToStyle("#FF8C00")
        );
    }

    @Test
    @DisplayName("Find Course: Should Throw If Course Not Found")
    void shouldThrowIfCourseNotFound() {

        assertThrows(
                IllegalArgumentException.class,
                () -> courseService.updateCourse(1L, "Math", "#FF8C00")
        );
    }
}