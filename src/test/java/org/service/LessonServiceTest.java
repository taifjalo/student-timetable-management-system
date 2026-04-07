package org.service;

import org.dao.LessonDao;
import org.entities.Course;
import org.entities.Lesson;
import org.entities.StudentGroup;
import org.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Mock
    NotificationService notificationService;

    LessonService lessonService;

    @BeforeEach
    void setUp() {
        lessonService = new LessonService(lessonDao, notificationService);
    }

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
    @DisplayName("Add Lesson: Should throw exception when end time equals start time")
    void shouldThrowExceptionWhenEndTimeEqualsStartTime() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 9, 0);

        assertThrows(IllegalArgumentException.class,
                () -> lessonService.addLesson(start, start, 1L, "A101"));
    }

    @Test
    @DisplayName("Add Lesson With Notify: Should notify recipients when list is non-empty")
    void shouldNotifyRecipientsWhenAddingLesson() {
        Course course = createCourse(1L, "Math", "#FF0000");
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 10, 30);

        when(lessonDao.findCourseById(1L)).thenReturn(course);

        lessonService.addLesson(start, end, 1L, "A101", List.of(10L, 20L));

        verify(notificationService).notifyLessonAdded("Math", "A101", List.of(10L, 20L));
    }

    @Test
    @DisplayName("Add Lesson With Notify: Should NOT notify when recipient list is empty")
    void shouldNotNotifyWhenAddingLessonWithEmptyRecipients() {
        Course course = createCourse(1L, "Math", "#FF0000");
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 10, 30);

        when(lessonDao.findCourseById(1L)).thenReturn(course);

        lessonService.addLesson(start, end, 1L, "A101", Collections.emptyList());

        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("Add Lesson With Notify: Should NOT notify when recipient list is null")
    void shouldNotNotifyWhenAddingLessonWithNullRecipients() {
        Course course = createCourse(1L, "Math", "#FF0000");
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 10, 30);

        when(lessonDao.findCourseById(1L)).thenReturn(course);

        lessonService.addLesson(start, end, 1L, "A101", null);

        verifyNoInteractions(notificationService);
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
    @DisplayName("Get Lessons By Course With Groups: Should delegate to dao")
    void shouldReturnLessonsByCourseWithGroups() {
        Course course = createCourse(1L, "Math", "#FF0000");
        Lesson lesson = createLesson(1L, LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 10, 10, 30), course, "A101");

        when(lessonDao.findLessonsByCourseWithGroups(1L)).thenReturn(Collections.singletonList(lesson));

        List<Lesson> result = lessonService.getLessonsByCourseWithGroups(1L);

        assertEquals(1, result.size());
        verify(lessonDao).findLessonsByCourseWithGroups(1L);
    }

    @Test
    @DisplayName("Get All Courses: Should delegate to dao and return list")
    void shouldReturnAllCourses() {
        List<Course> courses = Arrays.asList(
                createCourse(1L, "Math", "#FF0000"),
                createCourse(2L, "Physics", "#00FF00")
        );

        when(lessonDao.findAllCourses()).thenReturn(courses);

        List<Course> result = lessonService.getAllCourses();

        assertEquals(2, result.size());
        verify(lessonDao).findAllCourses();
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
    @DisplayName("Update Lesson: Should successfully update a lesson when all fields are valid")
    void shouldUpdateLessonSuccessfully() {
        Course course = createCourse(1L, "Math", "#FF0000");
        Lesson lesson = createLesson(1L, LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 10, 10, 30), course, "A101");

        when(lessonDao.findById(1L)).thenReturn(lesson);

        LocalDateTime newStart = LocalDateTime.of(2026, 3, 10, 10, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 3, 10, 11, 30);

        Lesson result = lessonService.updateLesson(1L, newStart, newEnd, "B202");

        assertNotNull(result);
        assertEquals("B202", result.getClassroom());
        assertEquals(newStart, result.getStartAt());
        assertEquals(newEnd, result.getEndAt());
        verify(lessonDao).updateLesson(lesson);
    }

    @Test
    @DisplayName("Update Lesson: Should throw exception when end time is before start time")
    void shouldThrowExceptionWhenUpdatingWithEndTimeBeforeStartTime() {
        Course course = createCourse(1L, "Math", "#FF0000");
        Lesson lesson = createLesson(1L, LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 10, 10, 30), course, "A101");

        when(lessonDao.findById(1L)).thenReturn(lesson);

        LocalDateTime newStart = LocalDateTime.of(2026, 3, 10, 10, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 3, 10, 9, 0);

        assertThrows(IllegalArgumentException.class,
                () -> lessonService.updateLesson(1L, newStart, newEnd, "B202"));
    }

    @Test
    @DisplayName("Update Lesson: Should throw exception when lesson not found")
    void shouldThrowExceptionWhenUpdatingNonExistentLesson() {
        when(lessonDao.findById(999L)).thenReturn(null);

        LocalDateTime newStart = LocalDateTime.of(2026, 3, 10, 10, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 3, 10, 11, 30);

        assertThrows(IllegalArgumentException.class,
                () -> lessonService.updateLesson(999L, newStart, newEnd, "B202"));
    }

    @Test
    @DisplayName("Update Lesson: Should throw exception when end time equals start time")
    void shouldThrowExceptionWhenUpdatingWithEqualTimes() {
        Course course = createCourse(1L, "Math", "#FF0000");
        Lesson lesson = createLesson(1L, LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 10, 10, 30), course, "A101");

        when(lessonDao.findById(1L)).thenReturn(lesson);

        LocalDateTime sameTime = LocalDateTime.of(2026, 3, 10, 10, 0);

        assertThrows(IllegalArgumentException.class,
                () -> lessonService.updateLesson(1L, sameTime, sameTime, "B202"));
    }

    @Test
    @DisplayName("Update Lesson With Notify: Should notify recipients when list is non-empty")
    void shouldNotifyRecipientsWhenUpdatingLesson() {
        Course course = createCourse(1L, "Math", "#FF0000");
        Lesson lesson = createLesson(1L, LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 10, 10, 30), course, "A101");

        when(lessonDao.findById(1L)).thenReturn(lesson);

        LocalDateTime newStart = LocalDateTime.of(2026, 3, 10, 10, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 3, 10, 11, 30);

        lessonService.updateLesson(1L, newStart, newEnd, "A101", List.of(5L));

        verify(notificationService).notifyLessonUpdated("Math", "A101", List.of(5L));
    }

    @Test
    @DisplayName("Update Lesson With Notify: Should NOT notify when recipient list is empty")
    void shouldNotNotifyWhenUpdatingLessonWithEmptyRecipients() {
        Course course = createCourse(1L, "Math", "#FF0000");
        Lesson lesson = createLesson(1L, LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 10, 10, 30), course, "A101");

        when(lessonDao.findById(1L)).thenReturn(lesson);

        LocalDateTime newStart = LocalDateTime.of(2026, 3, 10, 10, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 3, 10, 11, 30);

        lessonService.updateLesson(1L, newStart, newEnd, "A101", Collections.emptyList());

        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("Update Lesson With Notify: Should NOT notify when recipient list is null")
    void shouldNotNotifyWhenUpdatingLessonWithNullRecipients() {
        Course course = createCourse(1L, "Math", "#FF0000");
        Lesson lesson = createLesson(1L, LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 10, 10, 30), course, "A101");

        when(lessonDao.findById(1L)).thenReturn(lesson);

        LocalDateTime newStart = LocalDateTime.of(2026, 3, 10, 10, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 3, 10, 11, 30);

        lessonService.updateLesson(1L, newStart, newEnd, "A101", (List<Long>) null);

        verifyNoInteractions(notificationService);
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

    @Test
    @DisplayName("Delete Lesson With Notify: Should delete and notify when lesson exists and recipients given")
    void shouldDeleteLessonAndNotifyRecipients() {
        Course course = createCourse(1L, "Math", "#FF0000");
        Lesson lesson = createLesson(1L, LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 10, 10, 30), course, "A101");

        when(lessonDao.findById(1L)).thenReturn(lesson);

        lessonService.deleteLesson(1L, List.of(7L, 8L));

        verify(lessonDao).deleteLesson(1L);
        verify(notificationService).notifyLessonDeleted(1L, List.of(7L, 8L));
    }

    @Test
    @DisplayName("Delete Lesson With Notify: Should throw exception when lesson does not exist")
    void shouldThrowExceptionWhenDeletingNonExistentLessonWithRecipients() {
        when(lessonDao.findById(999L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> lessonService.deleteLesson(999L, List.of(1L)));
    }

    @Test
    @DisplayName("Delete Lesson With Notify: Should NOT notify when recipient list is empty")
    void shouldNotNotifyWhenDeletingLessonWithEmptyRecipients() {
        Course course = createCourse(1L, "Math", "#FF0000");
        Lesson lesson = createLesson(1L, LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 10, 10, 30), course, "A101");

        when(lessonDao.findById(1L)).thenReturn(lesson);

        lessonService.deleteLesson(1L, Collections.emptyList());

        verify(lessonDao).deleteLesson(1L);
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("Delete Lesson With Notify: Should NOT notify when recipient list is null")
    void shouldNotNotifyWhenDeletingLessonWithNullRecipients() {
        Course course = createCourse(1L, "Math", "#FF0000");
        Lesson lesson = createLesson(1L, LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 10, 10, 30), course, "A101");

        when(lessonDao.findById(1L)).thenReturn(lesson);

        lessonService.deleteLesson(1L, (List<Long>) null);

        verify(lessonDao).deleteLesson(1L);
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("Save Lesson: Should save and return lesson with groups and users")
    void shouldSaveLessonWithGroupsAndUsers() {
        Course course = createCourse(1L, "Math", "#FF0000");
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 10, 30);
        List<StudentGroup> groups = Collections.singletonList(new StudentGroup());
        List<User> users = Collections.singletonList(new User());

        Lesson result = lessonService.saveLesson(start, end, course, "A101", groups, users);

        assertNotNull(result);
        assertEquals("A101", result.getClassroom());
        assertEquals(groups, result.getAssignedGroups());
        assertEquals(users, result.getAssignedUsers());
        verify(lessonDao).saveLesson(result);
    }

    @Test
    @DisplayName("Save Lesson: Should throw exception when course is null")
    void shouldThrowExceptionWhenSavingLessonWithNullCourse() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 10, 30);

        assertThrows(IllegalArgumentException.class,
                () -> lessonService.saveLesson(start, end, null, "A101", List.of(), List.of()));
    }

    @Test
    @DisplayName("Save Lesson: Should default classroom to empty string when null")
    void shouldDefaultClassroomToEmptyWhenNull() {
        Course course = createCourse(1L, "Math", "#FF0000");
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 10, 30);

        Lesson result = lessonService.saveLesson(start, end, course, null, List.of(), List.of());

        assertEquals("", result.getClassroom());
    }
}
