package org.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.entities.Course;
import org.junit.jupiter.api.*;


import java.util.List;

@DisplayName("DAO Test: CourseDao")
class CourseDaoTest {

    private CourseDao courseDao;

    @BeforeEach
    void setUp() {
        courseDao = new CourseDao();
    }

    @DisplayName("Helper Method: First Create the Course using entity in the DB")
    private Course createCourse(String courseName, String courseColorCode) {
        Course course = new Course();
        course.setName(courseName);
        course.setColorCode(courseColorCode);
        return course;
    }

    @Test
    @DisplayName("Save: Should persist a course")
    void shouldSaveCourse() {

        Course course = createCourse("Algorithms", "#FF0001");

        Course saved = courseDao.save(course);

        assertNotNull(saved.getId());
        assertEquals("Algorithms", saved.getName());
    }

    @Test
    @DisplayName("FindById: Should return existing course")
    void shouldFindCourseById() {

        Course course = createCourse("Databases",  "#FF0002");
        Course saved = courseDao.save(course);

        Course found = courseDao.findById(saved.getId());

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    @DisplayName("FindAll: Should return course list")
    void shouldReturnAllCourses() {

        courseDao.save(createCourse("Math",  "#FF0003"));
        courseDao.save(createCourse("Physics",   "#FF0004"));

        List<Course> courses = courseDao.findAll();

        assertFalse(courses.isEmpty());
    }

    @Test
    @DisplayName("Delete: Should remove course")
    void shouldDeleteCourse() {

        Course course = courseDao.save(createCourse("Networks",   "#FF0005"));

        courseDao.delete(course.getId());

        Course deleted = courseDao.findById(course.getId());

        assertNull(deleted);
    }
}