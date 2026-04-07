package org.dao;

import jakarta.persistence.EntityManager;
import org.datasource.TimetableConnection;
import org.entities.Course;
import org.entities.Lesson;
import org.entities.StudentGroup;
import org.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Data-access object for {@link Lesson} entities.
 * Provides CRUD operations and various query methods used by the calendar view.
 *
 * <p>To avoid Hibernate's "multiple-bag fetch" problem, queries that need both
 * {@code assignedGroups} and {@code assignedUsers} fetch them in separate queries
 * within the same persistence context.
 */
public class LessonDao {

    /**
     * Persists a new lesson, resolving all associated entities (course, groups, users)
     * into managed instances within the same transaction.
     *
     * @param lesson the transient lesson to persist
     * @return the same lesson instance after persistence (ID will be set)
     */
    public Lesson saveLesson(Lesson lesson) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();

            Course managedCourse = em.find(Course.class, lesson.getCourse().getId());
            lesson.setCourse(managedCourse);

            List<StudentGroup> managedGroups = new ArrayList<>();
            for (StudentGroup g : lesson.getAssignedGroups()) {
                StudentGroup managed = em.find(StudentGroup.class, g.getGroupCode());
                if (managed != null) {
                    managedGroups.add(managed);
                }
            }
            lesson.setAssignedGroups(managedGroups);

            List<User> managedUsers = new ArrayList<>();
            for (User u : lesson.getAssignedUsers()) {
                User managed = em.find(User.class, u.getId());
                if (managed != null) {
                    managedUsers.add(managed);
                }
            }
            lesson.setAssignedUsers(managedUsers);

            em.persist(lesson);
            em.getTransaction().commit();
            return lesson;
        }
    }

    /**
     * Loads a lesson by ID, eagerly initialising both {@code assignedGroups} and
     * {@code assignedUsers} while the persistence context is still open.
     *
     * @param lessonId the lesson's primary key
     * @return the fully-initialised {@link Lesson}, or {@code null} if not found
     */
    public Lesson findById(Long lessonId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            List<Lesson> results = em.createQuery(
                    "SELECT DISTINCT l FROM Lesson l "
                    + "LEFT JOIN FETCH l.assignedGroups "
                    + "WHERE l.id = :id", Lesson.class)
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

    /**
     * Returns all lessons ordered by start time ascending.
     *
     * @return list of all lessons, possibly empty
     */
    public List<Lesson> findAllLessons() {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery("SELECT l FROM Lesson l ORDER BY l.startAt ASC", Lesson.class)
                    .getResultList();
        }
    }

    /**
     * Returns lessons for a given course without eagerly loading group or user collections.
     * Suitable for the teacher calendar view where individual assignment details are not needed.
     *
     * @param courseId the course's primary key
     * @return list of lessons for the course, ordered by start time
     */
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

    /**
     * Returns lessons for a course with both {@code assignedGroups} and
     * {@code assignedUsers} eagerly initialised. Used by the student calendar view
     * to evaluate lesson visibility.
     *
     * @param courseId the course's primary key
     * @return list of fully-loaded lessons, ordered by start time
     */
    public List<Lesson> findLessonsByCourseWithGroups(Long courseId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            List<Lesson> lessons = em.createQuery(
                    "SELECT DISTINCT l FROM Lesson l "
                    + "LEFT JOIN FETCH l.assignedGroups "
                    + "WHERE l.course.id = :courseId "
                    + "ORDER BY l.startAt ASC",
                    Lesson.class)
                .setParameter("courseId", courseId)
                .getResultList();

            if (!lessons.isEmpty()) {
                List<Long> ids = lessons.stream().map(Lesson::getId).toList();
                em.createQuery(
                        "SELECT DISTINCT l FROM Lesson l "
                        + "LEFT JOIN FETCH l.assignedUsers "
                        + "WHERE l.id IN :ids",
                        Lesson.class)
                    .setParameter("ids", ids)
                    .getResultList();
            }

            return lessons;
        }
    }

    /**
     * Looks up a course by its primary key. Convenience method used by services
     * that go through {@code LessonDao} for course resolution.
     *
     * @param courseId the course's primary key
     * @return the {@link Course}, or {@code null} if not found
     */
    public Course findCourseById(Long courseId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.find(Course.class, courseId);
        }
    }

    /**
     * Returns all courses ordered alphabetically. Used by the lesson-creation modal
     * to populate the course combo box.
     *
     * @return list of all courses, possibly empty
     */
    public List<Course> findAllCourses() {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery("SELECT c FROM Course c", Course.class)
                    .getResultList();
        }
    }

    /**
     * Merges (updates) an existing lesson, replacing its group and user assignments
     * with the values currently set on the supplied entity.
     *
     * @param lesson the detached lesson with updated fields
     * @return the managed, merged lesson instance
     */
    public Lesson updateLesson(Lesson lesson) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();

            Course managedCourse = em.find(Course.class, lesson.getCourse().getId());
            lesson.setCourse(managedCourse);

            List<StudentGroup> managedGroups = new ArrayList<>();
            for (StudentGroup g : lesson.getAssignedGroups()) {
                StudentGroup managed = em.find(StudentGroup.class, g.getGroupCode());
                if (managed != null) {
                    managedGroups.add(managed);
                }
            }
            lesson.setAssignedGroups(managedGroups);

            List<User> managedUsers = new ArrayList<>();
            for (User u : lesson.getAssignedUsers()) {
                User managed = em.find(User.class, u.getId());
                if (managed != null) {
                    managedUsers.add(managed);
                }
            }
            lesson.setAssignedUsers(managedUsers);

            Lesson merged = em.merge(lesson);
            em.getTransaction().commit();
            return merged;
        }
    }

    /**
     * Deletes the lesson with the given ID. Does nothing if the lesson does not exist.
     *
     * @param lessonId the primary key of the lesson to delete
     */
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
