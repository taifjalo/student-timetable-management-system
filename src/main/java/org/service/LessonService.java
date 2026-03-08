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

    public LessonService(LessonDao lessonDao) {
        this.lessonDao = lessonDao;
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

    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessonDao.findById(lessonId);
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson not found with id: " + lessonId);
        }
        lessonDao.deleteLesson(lessonId);
    }

    public List<Course> getAllCourses() {
        return lessonDao.findAllCourses();
    }
}
