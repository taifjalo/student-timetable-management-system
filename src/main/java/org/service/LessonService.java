package org.service;

import org.dao.LessonDao;
import org.entities.Course;
import org.entities.Lesson;
import org.entities.StudentGroup;
import org.entities.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service that manages lesson lifecycle and sends notifications on changes.
 *
 * <p>There are two overload families:
 * <ul>
 *   <li><b>Full overloads</b> — accept {@link Course}, group and user lists directly;
 *       used by the CalendarFX pop-over where the teacher selects all details.</li>
 *   <li><b>Simplified overloads</b> — accept only IDs and classroom; used by
 *       {@code CreateLessonModalController} where the course is resolved by ID.</li>
 * </ul>
 */
public class LessonService {

    private final LessonDao lessonDao;
    private final NotificationService notificationService;

    /**
     * Creates a {@code LessonService} without notification support.
     *
     * @param lessonDao the DAO used for lesson persistence
     */
    public LessonService(LessonDao lessonDao) {
        this(lessonDao, null);
    }

    /**
     * Creates a {@code LessonService} with notification support.
     *
     * @param lessonDao           the DAO used for lesson persistence
     * @param notificationService the service used to send lesson event notifications
     *                            (may be {@code null} to disable notifications)
     */
    public LessonService(LessonDao lessonDao, NotificationService notificationService) {
        this.lessonDao = lessonDao;
        this.notificationService = notificationService;
    }

    /**
     * Saves a fully-specified lesson with group and user assignments.
     * Used by the CalendarFX pop-over save flow.
     *
     * @param startAt   lesson start date-time
     * @param endAt     lesson end date-time
     * @param course    the course this lesson belongs to (must not be {@code null})
     * @param classroom classroom identifier; defaults to empty string if {@code null}
     * @param groups    student groups to assign to this lesson
     * @param users     individual users to assign to this lesson
     * @return the saved {@link Lesson}
     * @throws IllegalArgumentException if {@code course} is {@code null}
     */
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

    /**
     * Updates a lesson's time, course, classroom and assignments (full overload).
     *
     * @param lessonId  the ID of the lesson to update
     * @param startAt   new start date-time
     * @param endAt     new end date-time
     * @param course    new course assignment
     * @param classroom new classroom identifier
     * @param groups    new group assignments
     * @param users     new individual user assignments
     * @return the updated {@link Lesson}
     * @throws IllegalArgumentException if the lesson is not found
     */
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

    /**
     * Creates and persists a lesson using only a course ID (simplified overload).
     * Validates that start/end times are non-null and that end is after start.
     *
     * @param startAt   lesson start date-time
     * @param endAt     lesson end date-time (must be after {@code startAt})
     * @param courseId  the ID of the course this lesson belongs to
     * @param classroom classroom identifier; must not be blank
     * @return the saved {@link Lesson}
     * @throws IllegalArgumentException if any field is missing or times are invalid
     */
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

    /**
     * Returns all lessons ordered by start time.
     *
     * @return list of all lessons, possibly empty
     */
    public List<Lesson> getAllLessons() {
        return lessonDao.findAllLessons();
    }

    /**
     * Returns lessons for a given course without loading group/user collections.
     *
     * @param courseId the course's primary key
     * @return list of lessons for the course
     */
    public List<Lesson> getLessonsByCourse(Long courseId) {
        return lessonDao.findLessonsByCourse(courseId);
    }

    /**
     * Returns lessons for a course with group and user collections eagerly loaded.
     * Used for the student view where visibility must be evaluated per lesson.
     *
     * @param courseId the course's primary key
     * @return list of fully-loaded lessons
     */
    public List<Lesson> getLessonsByCourseWithGroups(Long courseId) {
        return lessonDao.findLessonsByCourseWithGroups(courseId);
    }

    /**
     * Loads a lesson by ID, throwing if it does not exist.
     *
     * @param lessonId the lesson's primary key
     * @return the {@link Lesson}
     * @throws IllegalArgumentException if the lesson is not found
     */
    public Lesson getLessonById(Long lessonId) {
        Lesson lesson = lessonDao.findById(lessonId);
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson not found with id: " + lessonId);
        }
        return lesson;
    }

    /**
     * Updates a lesson's time and classroom without changing group/user assignments
     * (simplified overload).
     *
     * @param lessonId  the ID of the lesson to update
     * @param startAt   new start date-time
     * @param endAt     new end date-time
     * @param classroom new classroom identifier; must not be blank
     * @return the updated {@link Lesson}
     * @throws IllegalArgumentException if the lesson is not found or times are invalid
     */
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

    /**
     * Deletes a lesson by ID (no notifications).
     *
     * @param lessonId the lesson's primary key
     * @throws IllegalArgumentException if the lesson is not found
     */
    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessonDao.findById(lessonId);
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson not found with id: " + lessonId);
        }
        lessonDao.deleteLesson(lessonId);
    }

    /**
     * Creates a lesson and notifies the given recipients that a new lesson was added.
     *
     * @param startAt      lesson start date-time
     * @param endAt        lesson end date-time
     * @param courseId     the course ID
     * @param classroom    classroom identifier
     * @param recipientIds user IDs to notify (empty list skips notification)
     * @return the saved {@link Lesson}
     */
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

    /**
     * Updates a lesson and notifies the given recipients of the change.
     *
     * @param lessonId     the lesson's primary key
     * @param startAt      new start date-time
     * @param endAt        new end date-time
     * @param classroom    new classroom identifier
     * @param recipientIds user IDs to notify (empty list skips notification)
     * @return the updated {@link Lesson}
     */
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

    /**
     * Deletes a lesson and notifies the given recipients that it was cancelled.
     *
     * @param lessonId     the lesson's primary key
     * @param recipientIds user IDs to notify (empty list skips notification)
     * @throws IllegalArgumentException if the lesson is not found
     */
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

    /**
     * Returns all courses (delegated to {@link LessonDao} for convenience).
     *
     * @return list of all courses, possibly empty
     */
    public List<Course> getAllCourses() {
        return lessonDao.findAllCourses();
    }
}
