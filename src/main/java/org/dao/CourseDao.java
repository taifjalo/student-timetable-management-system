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

            List<Object[]> results = em.createNativeQuery(
                            "SELECT c.course_id, c.name, " +
                                    "COALESCE(ct.name, c.name), " +
                                    "c.color_code " +
                                    "FROM courses c " +
                                    "LEFT JOIN course_translations ct " +
                                    "ON ct.course_id = c.course_id AND ct.language_code = ? " +
                                    "WHERE c.course_id = ?"
                    )
                    .setParameter(1, java.util.Locale.getDefault().getLanguage())
                    .setParameter(2, id)
                    .getResultList();

            if (results.isEmpty()) return null;

            Object[] row = results.get(0);

            Course c = new Course();

            c.setId(((Number) row[0]).longValue());
            c.setName((String) row[1]);              // оригинал
            c.setLocalizedName((String) row[2]);     // перевод
            c.setColorCode((String) row[3]);

            return c;
        }
    }

    /**
     * Returns all courses ordered alphabetically by name.
     *
     * @return list of all courses, possibly empty
     */
    public List<Course> findAll() {
        try (EntityManager em = TimetableConnection.createEntityManager()) {

            List<Object[]> results = em.createNativeQuery(
                            "SELECT c.course_id, c.name, " +
                                    "COALESCE(ct.name, c.name), " +
                                    "c.color_code " +
                                    "FROM courses c " +
                                    "LEFT JOIN course_translations ct " +
                                    "ON ct.course_id = c.course_id AND ct.language_code = ? " +
                                    "ORDER BY COALESCE(ct.name, c.name) ASC"
                    )
                    .setParameter(1, java.util.Locale.getDefault().getLanguage())
                    .getResultList();

            List<Course> courses = new java.util.ArrayList<>();

            for (Object[] row : results) {
                Course c = new Course();

                Long id = ((Number) row[0]).longValue();
                String original = (String) row[1];
                String translated = (String) row[2];
                String color = (String) row[3];

                c.setId(id);
                c.setName(original);             // оригинал
                c.setLocalizedName(translated);  // перевод
                c.setColorCode(color);

                courses.add(c);
            }

            return courses;
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

            List<Object[]> results = em.createNativeQuery(
                            "SELECT DISTINCT c.course_id, c.name, " +
                                    "COALESCE(ct.name, c.name), " +
                                    "c.color_code " +
                                    "FROM lessons l " +
                                    "JOIN courses c ON l.course_id = c.course_id " +
                                    "LEFT JOIN course_translations ct " +
                                    "ON ct.course_id = c.course_id AND ct.language_code = ? " +
                                    "WHERE EXISTS (" +
                                    "  SELECT 1 FROM student_profiles sp " +
                                    "  WHERE sp.user_id = ? " +
                                    "  AND sp.group_code IN (" +
                                    "    SELECT ag.group_code FROM assigned_groups ag WHERE ag.lesson_id = l.lesson_id" +
                                    "  )" +
                                    ") OR EXISTS (" +
                                    "  SELECT 1 FROM assigned_users au " +
                                    "  WHERE au.lesson_id = l.lesson_id AND au.user_id = ?" +
                                    ") " +
                                    "ORDER BY COALESCE(ct.name, c.name) ASC"
                    )
                    .setParameter(1, java.util.Locale.getDefault().getLanguage())
                    .setParameter(2, userId)
                    .setParameter(3, userId)
                    .getResultList();

            List<Course> courses = new java.util.ArrayList<>();

            for (Object[] row : results) {
                Course c = new Course();

                c.setId(((Number) row[0]).longValue());
                c.setName((String) row[1]);          // оригинал
                c.setLocalizedName((String) row[2]); // перевод
                c.setColorCode((String) row[3]);

                courses.add(c);
            }

            return courses;
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
