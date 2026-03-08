package org.dao;

import jakarta.persistence.EntityManager;
import org.datasource.TimetableConnection;
import org.entities.Course;
import org.entities.Lesson;

import java.util.List;

public class LessonDao {

    public void saveLesson(Lesson lesson) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(lesson);
            em.getTransaction().commit();
        }
    }

    public void updateLesson(Lesson lesson) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(lesson);
            em.getTransaction().commit();
        }
    }


    public void saveCourse(Course course) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(course);
            em.getTransaction().commit();
        }
    }

    public Lesson findById(Long lessonId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.find(Lesson.class, lessonId);
        }
    }

    public List<Lesson> findAllLessons() {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery("SELECT l FROM Lesson l ORDER BY l.startAt ASC", Lesson.class)
                    .getResultList();
        }
    }

    public List<Lesson> findLessonsByCourse(Long courseId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery("SELECT l FROM Lesson l WHERE l.course.id = :courseId ORDER BY l.startAt ASC", Lesson.class)
                    .setParameter("courseId", courseId)
                    .getResultList();
        }
    }

    public Course findCourseById(Long courseId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.find(Course.class, courseId);
        }
    }

    public List<Course> findAllCourses() {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery("SELECT c FROM Course c", Course.class)
                    .getResultList();
        }
    }

    public void deleteLesson(Long lessonId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            Lesson lesson = em.find(Lesson.class, lessonId);
            if (lesson != null) {
                em.remove(lesson);
            }
            em.getTransaction().commit();
        }
    }
}
