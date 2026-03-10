package org.service;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import org.dao.CourseDao;
import org.entities.Course;

import java.util.List;

public class CourseService {

    private final CourseDao courseDao;

    public CourseService(CourseDao courseDao) {
        this.courseDao = courseDao;
    }

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

    public Course deleteCourse(Long id) {
        Course course = courseDao.findById(id);
        if (course == null) {
            throw new IllegalArgumentException("Course not found with id: " + id);
        }
        courseDao.delete(id);
        return course;
    }

    public List<Course> getAllCourses() {
        return courseDao.findAll();
    }

    public List<Course> getCoursesForUser(Long userId) {
        if (userId == null) return courseDao.findAll();
        return courseDao.findCoursesForUser(userId);
    }

    public Course getCourseById(Long id) {
        Course course = courseDao.findById(id);
        if (course == null) {
            throw new IllegalArgumentException("Course not found with id: " + id);
        }
        return course;
    }

    public Calendar toCalendar(Course course) {
        Calendar calendar = new Calendar(course.getName());
        calendar.setStyle(colorCodeToStyle(course.getColorCode()));
        calendar.setUserObject(course);
        return calendar;
    }

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



