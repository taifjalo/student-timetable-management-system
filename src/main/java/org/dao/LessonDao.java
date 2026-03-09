package org.dao;

import jakarta.persistence.EntityManager;
import org.datasource.TimetableConnection;
import org.entities.Course;
import org.entities.Lesson;
import org.entities.StudentGroup;
import org.entities.User;

import java.util.ArrayList;
import java.util.List;

public class LessonDao {

    public Lesson saveLesson(Lesson lesson) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();

            Course managedCourse = em.find(Course.class, lesson.getCourse().getId());
            lesson.setCourse(managedCourse);

            List<StudentGroup> managedGroups = new ArrayList<>();
            for (StudentGroup g : lesson.getAssignedGroups()) {
                StudentGroup managed = em.find(StudentGroup.class, g.getGroupCode());
                if (managed != null) managedGroups.add(managed);
            }
            lesson.setAssignedGroups(managedGroups);

            List<User> managedUsers = new ArrayList<>();
            for (User u : lesson.getAssignedUsers()) {
                User managed = em.find(User.class, u.getId());
                if (managed != null) managedUsers.add(managed);
            }
            lesson.setAssignedUsers(managedUsers);

            em.persist(lesson);
            em.getTransaction().commit();
            return lesson;
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
            List<Lesson> results = em.createQuery(
                    "SELECT DISTINCT l FROM Lesson l " +
                    "LEFT JOIN FETCH l.assignedGroups " +
                    "WHERE l.id = :id", Lesson.class)
                .setParameter("id", lessonId)
                .getResultList();

            if (results.isEmpty()) {
                return null;
            }

            Lesson lesson = results.get(0);
            // Initialize assigned users while the session is still open, but do it
            // separately to avoid Hibernate's multiple-bag fetch problem.
            lesson.getAssignedUsers().size();
            return lesson;
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
            // Calendar loading only needs the lesson rows themselves. Do not fetch
            // both assignedGroups and assignedUsers here because both are List bags.
            return em.createQuery(
                    "SELECT l FROM Lesson l WHERE l.course.id = :courseId ORDER BY l.startAt ASC",
                    Lesson.class)
                .setParameter("courseId", courseId)
                .getResultList();
        }
    }


    public List<Lesson> findLessonsByCourseWithGroups(Long courseId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            // First query: fetch lessons + assignedGroups
            List<Lesson> lessons = em.createQuery(
                    "SELECT DISTINCT l FROM Lesson l " +
                    "LEFT JOIN FETCH l.assignedGroups " +
                    "WHERE l.course.id = :courseId " +
                    "ORDER BY l.startAt ASC",
                    Lesson.class)
                .setParameter("courseId", courseId)
                .getResultList();

            // Second query: fetch assignedUsers for the same lessons (avoids multi-bag)
            if (!lessons.isEmpty()) {
                List<Long> ids = lessons.stream().map(Lesson::getId).toList();
                em.createQuery(
                        "SELECT DISTINCT l FROM Lesson l " +
                        "LEFT JOIN FETCH l.assignedUsers " +
                        "WHERE l.id IN :ids",
                        Lesson.class)
                    .setParameter("ids", ids)
                    .getResultList();
                // Hibernate merges the results into the first-level cache,
                // so the lessons list now has assignedUsers populated too.
            }

            return lessons;
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

    public Lesson updateLesson(Lesson lesson) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();

            Course managedCourse = em.find(Course.class, lesson.getCourse().getId());
            lesson.setCourse(managedCourse);

            List<StudentGroup> managedGroups = new ArrayList<>();
            for (StudentGroup g : lesson.getAssignedGroups()) {
                StudentGroup managed = em.find(StudentGroup.class, g.getGroupCode());
                if (managed != null) managedGroups.add(managed);
            }
            lesson.setAssignedGroups(managedGroups);

            List<User> managedUsers = new ArrayList<>();
            for (User u : lesson.getAssignedUsers()) {
                User managed = em.find(User.class, u.getId());
                if (managed != null) managedUsers.add(managed);
            }
            lesson.setAssignedUsers(managedUsers);

            Lesson merged = em.merge(lesson);
            em.getTransaction().commit();
            return merged;
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
