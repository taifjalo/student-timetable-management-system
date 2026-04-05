package org.dao;

import jakarta.persistence.EntityManager;
import org.datasource.TimetableConnection;
import org.entities.Course;

import java.util.List;

public class CourseDao {

    public Course save(Course course) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            Course saved = em.merge(course);
            em.getTransaction().commit();
            return saved;
        }
    }

    public Course findById(Long id) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.find(Course.class, id);
        }
    }

    public List<Course> findAll() {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery("SELECT c FROM Course c ORDER BY c.name ASC", Course.class)
                    .getResultList();
        }
    }

    /**
     * Returns only the courses that have at least one lesson the given student
     * can see — either via an assigned group (student_profiles → assigned_groups)
     * or via a direct individual assignment (assigned_users).
     */
    public List<Course> findCoursesForUser(Long userId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery(
                    "SELECT DISTINCT l.course FROM Lesson l " +
                    "WHERE EXISTS (" +
                    "  SELECT sp FROM StudentProfile sp " +
                    "  WHERE sp.user.id = :userId " +
                    "  AND sp.studentGroup MEMBER OF l.assignedGroups" +
                    ") OR EXISTS (" +
                    "  SELECT u FROM l.assignedUsers u WHERE u.id = :userId" +
                    ") " +
                    "ORDER BY l.course.name ASC",
                    Course.class
            ).setParameter("userId", userId).getResultList();
        }
    }

    public void delete(Long id) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            Course course = em.find(Course.class, id);
            if (course != null) {
                em.remove(course);
            }
            em.getTransaction().commit();
        }
    }
}

