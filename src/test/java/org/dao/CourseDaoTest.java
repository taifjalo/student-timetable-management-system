package org.dao;

import org.entities.Course;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CourseDaoTest {

    private CourseDao courseDao;

    @BeforeEach
    void setUp() {
        courseDao = new CourseDao();
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private Course createCourse(String name, String color) {
        Course c = new Course();
        c.setName(name);
        c.setColorCode(color);
        return courseDao.save(c);
    }

    // ── save (new) ────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("save: persists a new course and assigns an ID")
    void testSaveNewCourse() {
        Course saved = createCourse("TestCourse_A", "#FF0000");

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("TestCourse_A", saved.getName());

        // Cleanup
        courseDao.delete(saved.getId());
    }

    @Test
    @Order(2)
    @DisplayName("save: updates an existing course via merge")
    void testSaveExistingCourseUpdates() {
        Course saved = createCourse("TestCourse_B", "#00FF00");
        saved.setName("TestCourse_B_Updated");

        Course updated = courseDao.save(saved);

        assertNotNull(updated);
        assertEquals("TestCourse_B_Updated", updated.getName());

        courseDao.delete(updated.getId());
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("findById: returns course for valid ID")
    void testFindByIdFound() {
        Course saved = createCourse("TestCourse_C", "#0000FF");

        Course found = courseDao.findById(saved.getId());

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());

        courseDao.delete(saved.getId());
    }

    @Test
    @Order(4)
    @DisplayName("findById: returns null for non-existent ID")
    void testFindByIdNotFound() {
        Course found = courseDao.findById(Long.MAX_VALUE);
        assertNull(found);
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("findAll: returns non-empty list after save")
    void testFindAllNotEmpty() {
        Course saved = createCourse("TestCourse_D", "#FFFF00");

        List<Course> all = courseDao.findAll();

        assertNotNull(all);
        assertFalse(all.isEmpty());

        courseDao.delete(saved.getId());
    }

    @Test
    @Order(6)
    @DisplayName("findAll: contains saved course")
    void testFindAllContainsSaved() {
        Course saved = createCourse("TestCourse_E_Unique", "#FF00FF");

        List<Course> all = courseDao.findAll();

        assertTrue(all.stream().anyMatch(c -> c.getId().equals(saved.getId())));

        courseDao.delete(saved.getId());
    }

    // ── findCoursesForUser ────────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("findCoursesForUser: returns list (possibly empty) for valid user")
    void testFindCoursesForUser() {
        // Use user ID 1 — may be empty if no lessons assigned, but should not throw
        List<Course> courses = courseDao.findCoursesForUser(1L);
        assertNotNull(courses);
    }

    @Test
    @Order(8)
    @DisplayName("findCoursesForUser: returns empty list for non-existent user")
    void testFindCoursesForUserNonExistent() {
        List<Course> courses = courseDao.findCoursesForUser(Long.MAX_VALUE);
        assertNotNull(courses);
        assertTrue(courses.isEmpty());
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("delete: removes existing course")
    void testDeleteExisting() {
        Course saved = createCourse("TestCourse_F", "#123456");
        Long id = saved.getId();

        courseDao.delete(id);

        assertNull(courseDao.findById(id));
    }

    @Test
    @Order(10)
    @DisplayName("delete: does nothing for non-existent ID")
    void testDeleteNonExistent() {
        assertDoesNotThrow(() -> courseDao.delete(Long.MAX_VALUE));
    }
}