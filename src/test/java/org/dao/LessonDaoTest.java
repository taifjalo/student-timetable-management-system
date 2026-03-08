package org.dao;

import org.entities.Course;
import org.entities.Lesson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Test: LessonDao tests with DB integration")
class LessonDaoTest {

    private LessonDao lessonDao;

    @BeforeEach
    void setUp() {
        lessonDao = new LessonDao();
    }

    @DisplayName("Helper Method: Create and save a Course")
    private Course createAndSaveCourse() {
        Course course = new Course();
        course.setName("TestCourse-" + System.currentTimeMillis());
        course.setColorCode("#FF0000");

        lessonDao.saveCourse(course);
        return course;
    }

    @Test
    @DisplayName("Integration Test: Should Save And Retrieve Lesson")
    void shouldSaveAndRetrieveLesson() {
        Course course = createAndSaveCourse();

        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 10, 30);

        Lesson lesson = new Lesson(start, end, course, "A101");
        lessonDao.saveLesson(lesson);

        Lesson found = lessonDao.findById(lesson.getId());

        assertNotNull(found);
        assertEquals("A101", found.getClassroom());
        assertEquals(course.getId(), found.getCourse().getId());
    }

    @Test
    @DisplayName("Integration Test: Should Find Lessons By Course")
    void shouldFindLessonsByCourse() {
        Course course = createAndSaveCourse();

        Lesson lesson1 = new Lesson(
                LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 10, 10, 30),
                course, "A101");
        Lesson lesson2 = new Lesson(
                LocalDateTime.of(2026, 3, 10, 11, 0),
                LocalDateTime.of(2026, 3, 10, 12, 30),
                course, "B202");

        lessonDao.saveLesson(lesson1);
        lessonDao.saveLesson(lesson2);

        List<Lesson> lessons = lessonDao.findLessonsByCourse(course.getId());

        assertTrue(lessons.size() >= 2);
    }

    @Test
    @DisplayName("Integration Test: Should Delete Lesson")
    void shouldDeleteLesson() {
        Course course = createAndSaveCourse();

        Lesson lesson = new Lesson(
                LocalDateTime.of(2026, 3, 10, 14, 0),
                LocalDateTime.of(2026, 3, 10, 15, 30),
                course, "C303");
        lessonDao.saveLesson(lesson);

        Long lessonId = lesson.getId();
        assertNotNull(lessonDao.findById(lessonId));

        lessonDao.deleteLesson(lessonId);

        assertNull(lessonDao.findById(lessonId));
    }
}
