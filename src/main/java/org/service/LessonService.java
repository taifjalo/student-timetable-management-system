package org.service;

import org.dao.LessonDao;
import org.entities.Course;
import org.entities.Lesson;
import org.entities.StudentGroup;
import org.entities.User;

import java.time.LocalDateTime;
import java.util.List;

public class LessonService {

    private final LessonDao lessonDao;
    private final NotificationService notificationService;

    // Existing constructor — unchanged, backward-compatible
    public LessonService(LessonDao lessonDao) {
        this(lessonDao, null);
    }

    // New constructor — used when notifications are needed
    public LessonService(LessonDao lessonDao, NotificationService notificationService) {
        this.lessonDao = lessonDao;
        this.notificationService = notificationService;
    }

    public Lesson saveLesson(LocalDateTime startAt,
                             LocalDateTime endAt,
                             Course course,
                             String classroom,
                             List<StudentGroup> groups,
                             List<User> users) {
        if (course == null) throw new IllegalArgumentException("A lesson must belong to a course.");
        if (classroom == null) classroom = "";
        Lesson lesson = new Lesson(startAt, endAt, course, classroom);
        lesson.setAssignedGroups(groups);
        lesson.setAssignedUsers(users);
        lessonDao.saveLesson(lesson);
        return lesson;
    }

    public Lesson updateLesson(Long lessonId,
                               LocalDateTime startAt,
                               LocalDateTime endAt,
                               Course course,
                               String classroom,
                               List<StudentGroup> groups,
                               List<User> users) {
        Lesson lesson = lessonDao.findById(lessonId);
        if (lesson == null) throw new IllegalArgumentException("Lesson not found: " + lessonId);
        if (classroom == null) classroom = "";
        lesson.setStartAt(startAt);
        lesson.setEndAt(endAt);
        lesson.setCourse(course);
        lesson.setClassroom(classroom);
        lesson.setAssignedGroups(groups);
        lesson.setAssignedUsers(users);
        return lessonDao.updateLesson(lesson);
    }

    public Lesson addLesson(LocalDateTime startAt, LocalDateTime endAt, Long courseId, String classroom) {
        if (startAt == null || endAt == null || courseId == null || classroom == null || classroom.isBlank()) {
            throw new IllegalArgumentException("All lesson fields are required");
        }

        if (endAt.isBefore(startAt) || endAt.isEqual(startAt)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Course course = lessonDao.findCourseById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found with id: " + courseId);
        }

        Lesson lesson = new Lesson(startAt, endAt, course, classroom);
        lessonDao.saveLesson(lesson);
        return lesson;
    }

    public List<Lesson> getAllLessons() {
        return lessonDao.findAllLessons();
    }

    public List<Lesson> getLessonsByCourse(Long courseId) {
        return lessonDao.findLessonsByCourse(courseId);
    }

    public Lesson getLessonById(Long lessonId) {
        Lesson lesson = lessonDao.findById(lessonId);
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson not found with id: " + lessonId);
        }
        return lesson;
    }

    public Lesson updateLesson(Long lessonId, LocalDateTime startAt, LocalDateTime endAt, String classroom) {
        Lesson lesson = getLessonById(lessonId); // Already checks for null

        if (startAt == null || endAt == null || classroom == null || classroom.isBlank()) {
            throw new IllegalArgumentException("All lesson fields are required");
        }

        if (endAt.isBefore(startAt) || endAt.isEqual(startAt)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        lesson.setStartAt(startAt);
        lesson.setEndAt(endAt);
        lesson.setClassroom(classroom);

        lessonDao.updateLesson(lesson);
        return lesson;
    }

    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessonDao.findById(lessonId);
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson not found with id: " + lessonId);
        }
        lessonDao.deleteLesson(lessonId);
    }

    // New overload — calls existing addLesson then notifies recipients
    public Lesson addLesson(LocalDateTime startAt, LocalDateTime endAt, Long courseId, String classroom,
                            List<Long> recipientIds) {
        Lesson lesson = addLesson(startAt, endAt, courseId, classroom);
        if (notificationService != null && recipientIds != null && !recipientIds.isEmpty()) {
            notificationService.notifyLessonAdded(
                lesson.getCourse().getName(),
                lesson.getClassroom(),
                recipientIds
            );
        }
        return lesson;
    }

    // New overload — calls existing updateLesson then notifies recipients
    public Lesson updateLesson(Long lessonId, LocalDateTime startAt, LocalDateTime endAt, String classroom,
                               List<Long> recipientIds) {
        Lesson lesson = updateLesson(lessonId, startAt, endAt, classroom);
        if (notificationService != null && recipientIds != null && !recipientIds.isEmpty()) {
            notificationService.notifyLessonUpdated(
                lesson.getCourse().getName(),
                lesson.getClassroom(),
                recipientIds
            );
        }
        return lesson;
    }

    // New overload — notifies recipients with lesson ID before deletion
    public void deleteLesson(Long lessonId, List<Long> recipientIds) {
        Lesson lesson = lessonDao.findById(lessonId);
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson not found with id: " + lessonId);
        }
        lessonDao.deleteLesson(lessonId);
        if (notificationService != null && recipientIds != null && !recipientIds.isEmpty()) {
            notificationService.notifyLessonDeleted(lessonId, recipientIds);
        }
    }

    public List<Course> getAllCourses() {
        return lessonDao.findAllCourses();
    }
}
