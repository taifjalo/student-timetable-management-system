package org.service;

import org.dao.LessonDao;
import org.entities.Course;
import org.entities.Lesson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Logic Test: LessonService tests with JUnit 5 & Mockito")
@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    LessonDao lessonDao;

    @InjectMocks
    LessonService lessonService;

    private Course createCourse(Long id, String name, String colorCode) {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        course.setColorCode(colorCode);
        return course;
    }

    private Lesson createLesson(Long id, LocalDateTime start, LocalDateTime end, Course course, String classroom) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setStartAt(start);
        lesson.setEndAt(end);
        lesson.setCourse(course);
        lesson.setClassroom(classroom);
        return lesson;
    }

    @Test
    @DisplayName("Add Lesson: Should successfully add a lesson when all fields are valid")
    void shouldAddLessonSuccessfully() {
        Course course = createCourse(1L, "Math", "#FF0000");
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 10, 30);

        when(lessonDao.findCourseById(1L)).thenReturn(course);

        Lesson result = lessonService.addLesson(start, end, 1L, "A101");

        assertNotNull(result);
        assertEquals("A101", result.getClassroom());
        assertEquals(course, result.getCourse());
        verify(lessonDao).saveLesson(any(Lesson.class));
    }

    @Test
    @DisplayName("Add Lesson: Should throw exception when end time is before start time")
    void shouldThrowExceptionWhenEndTimeBeforeStartTime() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 9, 0);

        assertThrows(IllegalArgumentException.class,
                () -> lessonService.addLesson(start, end, 1L, "A101"));
    }

    @Test
    @DisplayName("Add Lesson: Should throw exception when course not found")
    void shouldThrowExceptionWhenCourseNotFound() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 10, 30);

        when(lessonDao.findCourseById(999L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> lessonService.addLesson(start, end, 999L, "A101"));
    }

    @Test
    @DisplayName("Add Lesson: Should throw exception when fields are null")
    void shouldThrowExceptionWhenFieldsAreNull() {
        assertThrows(IllegalArgumentException.class,
                () -> lessonService.addLesson(null, null, null, null));
    }

    @Test
    @DisplayName("Add Lesson: Should throw exception when classroom is blank")
    void shouldThrowExceptionWhenClassroomIsBlank() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 10, 30);

        assertThrows(IllegalArgumentException.class,
                () -> lessonService.addLesson(start, end, 1L, "   "));
    }

    @Test
    @DisplayName("Get All Lessons: Should return empty list when no lessons exist")
    void shouldReturnEmptyListWhenNoLessons() {
        when(lessonDao.findAllLessons()).thenReturn(Collections.emptyList());

        List<Lesson> result = lessonService.getAllLessons();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Get All Lessons: Should return lessons sorted by start time")
    void shouldReturnAllLessons() {
        Course course = createCourse(1L, "Math", "#FF0000");
        Lesson lesson1 = createLesson(1L, LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 10, 10, 30), course, "A101");
        Lesson lesson2 = createLesson(2L, LocalDateTime.of(2026, 3, 10, 11, 0),
                LocalDateTime.of(2026, 3, 10, 12, 30), course, "B202");

        when(lessonDao.findAllLessons()).thenReturn(Arrays.asList(lesson1, lesson2));

        List<Lesson> result = lessonService.getAllLessons();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Get Lessons By Course: Should return only lessons for given course")
    void shouldReturnLessonsByCourse() {
        Course course = createCourse(1L, "Math", "#FF0000");
        Lesson lesson = createLesson(1L, LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 10, 10, 30), course, "A101");

        when(lessonDao.findLessonsByCourse(1L)).thenReturn(Collections.singletonList(lesson));

        List<Lesson> result = lessonService.getLessonsByCourse(1L);

        assertEquals(1, result.size());
        assertEquals("A101", result.get(0).getClassroom());
    }

    @Test
    @DisplayName("Get Lesson By ID: Should return lesson when found")
    void shouldReturnLessonById() {
        Course course = createCourse(1L, "Math", "#FF0000");
        Lesson lesson = createLesson(1L, LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 10, 10, 30), course, "A101");

        when(lessonDao.findById(1L)).thenReturn(lesson);

        Lesson result = lessonService.getLessonById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Get Lesson By ID: Should throw exception when lesson not found")
    void shouldThrowExceptionWhenLessonNotFound() {
        when(lessonDao.findById(999L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> lessonService.getLessonById(999L));
    }

    @Test
    @DisplayName("Delete Lesson: Should delete lesson when it exists")
    void shouldDeleteLessonWhenExists() {
        Course course = createCourse(1L, "Math", "#FF0000");
        Lesson lesson = createLesson(1L, LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 10, 10, 30), course, "A101");

        when(lessonDao.findById(1L)).thenReturn(lesson);

        lessonService.deleteLesson(1L);

        verify(lessonDao).deleteLesson(1L);
    }

    @Test
    @DisplayName("Delete Lesson: Should throw exception when lesson does not exist")
    void shouldThrowExceptionWhenDeletingNonExistentLesson() {
        when(lessonDao.findById(999L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> lessonService.deleteLesson(999L));
    }
}
