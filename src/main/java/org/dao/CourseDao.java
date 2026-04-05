package org.dao;

import jakarta.persistence.EntityManager;
import org.datasource.TimetableConnection;
import org.entities.Course;

import java.util.List;

/**
 * Data-access object for {@link Course} entities.
 * Handles CRUD operations and visibility-based queries for the calendar view.
 */
public class CourseDao {

    /**
     * Persists or updates a course using {@code merge}.
     * If the course has no ID it is inserted; otherwise the existing row is updated.
     *
     * @param course the course to save or update
     * @return the managed, merged course instance (use the returned object after calling this)
     */
    public Course save(Course course) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            Course saved = em.merge(course);
            em.getTransaction().commit();
            return saved;
        }
    }

    /**
     * Looks up a course by its primary key.
     *
     * @param id the course ID
     * @return the matching {@link Course}, or {@code null} if not found
     */
    public Course findById(Long id) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.find(Course.class, id);
        }
    }

    /**
     * Returns all courses ordered alphabetically by name.
     *
     * @return list of all courses, possibly empty
     */
    public List<Course> findAll() {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery("SELECT c FROM Course c ORDER BY c.name ASC", Course.class)
                    .getResultList();
        }
    }

    /**
     * Returns only the courses that have at least one lesson visible to the given student.
     * A lesson is visible if the student belongs to one of its assigned groups
     * (via {@code student_profiles → assigned_groups}) or is directly assigned
     * (via {@code assigned_users}).
     *
     * @param userId the student's user ID
     * @return list of courses the student can see, ordered by course name
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

    /**
     * Deletes the course with the given ID. Does nothing if no such course exists.
     *
     * @param id the ID of the course to delete
     */
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
