package org.service;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import org.dao.CourseDao;
import org.entities.Course;

import java.util.List;

/**
 * Service that handles course management and maps courses to CalendarFX
 * {@link Calendar} objects for display on the timetable.
 */
public class CourseService {

    private final CourseDao courseDao;

    /**
     * Creates a {@code CourseService} with the given DAO.
     *
     * @param courseDao the DAO used for course persistence
     */
    public CourseService(CourseDao courseDao) {
        this.courseDao = courseDao;
    }

    /**
     * Creates and persists a new course.
     *
     * @param name      display name; must not be blank
     * @param colorCode hex color string (e.g. {@code "#77C04B"}); must not be blank
     * @return the saved {@link Course} with its generated ID set
     * @throws IllegalArgumentException if name or colorCode is blank
     */
    public Course createCourse(String name, String colorCode) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Course name is required");
        }
        if (colorCode == null || colorCode.isBlank()) {
            throw new IllegalArgumentException("Color code is required");
        }
        Course course = new Course(name, colorCode);
        return courseDao.save(course);
    }

    /**
     * Updates the name and color of an existing course.
     *
     * @param id        the ID of the course to update
     * @param name      new display name; must not be blank
     * @param colorCode new hex color string
     * @return the updated and saved {@link Course}
     * @throws IllegalArgumentException if the course is not found or the name is blank
     */
    public Course updateCourse(Long id, String name, String colorCode) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Course name is required");
        }
        Course course = courseDao.findById(id);
        if (course == null) {
            throw new IllegalArgumentException("Course not found with id: " + id);
        }
        course.setName(name);
        course.setColorCode(colorCode);
        return courseDao.save(course);
    }

    /**
     * Deletes the course with the given ID.
     *
     * @param id the course's primary key
     * @return the deleted {@link Course} as it was before deletion
     * @throws IllegalArgumentException if no course with the given ID exists
     */
    public Course deleteCourse(Long id) {
        Course course = courseDao.findById(id);
        if (course == null) {
            throw new IllegalArgumentException("Course not found with id: " + id);
        }
        courseDao.delete(id);
        return course;
    }

    /**
     * Returns all courses ordered alphabetically.
     *
     * @return list of all courses, possibly empty
     */
    public List<Course> getAllCourses() {
        return courseDao.findAll();
    }

    /**
     * Returns courses visible to the given user.
     * If {@code userId} is {@code null} (e.g. for a teacher), all courses are returned.
     *
     * @param userId the student's user ID, or {@code null} to get all courses
     * @return list of courses the user can see
     */
    public List<Course> getCoursesForUser(Long userId) {
        if (userId == null) return courseDao.findAll();
        return courseDao.findCoursesForUser(userId);
    }

    /**
     * Looks up a course by ID, throwing if it does not exist.
     *
     * @param id the course's primary key
     * @return the {@link Course}
     * @throws IllegalArgumentException if no course with the given ID exists
     */
    public Course getCourseById(Long id) {
        Course course = courseDao.findById(id);
        if (course == null) {
            throw new IllegalArgumentException("Course not found with id: " + id);
        }
        return course;
    }

    /**
     * Creates a CalendarFX {@link Calendar} from a {@link Course}, applying
     * the course color as the calendar style.
     *
     * @param course the course to convert
     * @return a new {@link Calendar} with the course name and matching color style
     */
    public Calendar toCalendar(Course course) {
        Calendar calendar = new Calendar(course.getName());
        calendar.setStyle(colorCodeToStyle(course.getColorCode()));
        calendar.setUserObject(course);
        return calendar;
    }

    /**
     * Maps a hex color code to the corresponding CalendarFX {@link Style} enum value.
     * Unrecognized codes fall back to {@link Style#STYLE1}.
     *
     * @param colorCode hex color string (e.g. {@code "#77C04B"})
     * @return the matching {@link Style}
     */
    public static Style colorCodeToStyle(String colorCode) {
        if (colorCode == null) return Style.STYLE1;
        return switch (colorCode.toUpperCase()) {
            case "#77C04B" -> Style.STYLE1;
            case "#418FCB" -> Style.STYLE2;
            case "#F7D15B" -> Style.STYLE3;
            case "#9D5B9F" -> Style.STYLE4;
            case "#D0525F" -> Style.STYLE5;
            case "#F9844B" -> Style.STYLE6;
            case "#AE663E" -> Style.STYLE7;
            default        -> Style.STYLE1;
        };
    }
}
