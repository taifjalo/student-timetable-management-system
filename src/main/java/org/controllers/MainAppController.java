package org.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import org.application.CustomEntryPopOverContentPane;
import org.dao.CourseDao;
import org.dao.GroupDao;
import org.dao.LessonDao;
import org.entities.Course;
import org.entities.Lesson;
import org.entities.StudentGroup;
import org.service.CourseService;
import org.service.GroupService;
import org.service.LessonService;
import org.service.SessionManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MainAppController {

    @FXML private BorderPane mainRoot;

    // Kept as fields so refresh() can reach them without re-initialising the whole scene
    private CalendarView calendarView;
    private CalendarSource myCalendarSource;
    private SourceTrayController sourceTrayController;
    private NavbarController navbarController;

    @FXML
    public void initialize() {
        try {
            FXMLLoader navbarLoader = new FXMLLoader(getClass().getResource("/timetable-management-navbar.fxml"));
            BorderPane navbar = navbarLoader.load();
            navbarController = navbarLoader.getController();
            mainRoot.setTop(navbar);

            calendarView = new CalendarView();
            mainRoot.setCenter(calendarView);

            navbarController.setCalendarView(calendarView);
            // Give the navbar a handle so the refresh button can call back here
            navbarController.setMainAppController(this);

            myCalendarSource = new CalendarSource("Courses");
            calendarView.getCalendarSources().clear();
            calendarView.getCalendarSources().add(myCalendarSource);
            calendarView.setRequestedTime(LocalTime.now());
            calendarView.setShowSourceTray(false);
            calendarView.setShowToolBar(false);
            calendarView.showWeekPage();

            calendarView.setEntryDetailsPopOverContentCallback(param ->
                    new CustomEntryPopOverContentPane(
                            param.getPopOver(),
                            param.getDateControl(),
                            param.getEntry()));

            calendarView.setEntryEditPolicy(param -> {
                // Students cannot interact with entries at all
                if (!SessionManager.getInstance().isTeacher()) return false;
                // Teachers: no drag-to-move or drag-to-resize
                return switch (param.getEditOperation()) {
                    case MOVE, CHANGE_START, CHANGE_END -> false;
                    default -> true;
                };
            });

            // Students cannot create new entries by clicking on the calendar
            if (!SessionManager.getInstance().isTeacher()) {
                calendarView.setEntryFactory(param -> null);
            }

            Thread updateTimeThread = new Thread("Calendar: Update Time Thread") {
                @Override
                public void run() {
                    while (true) {
                        Platform.runLater(() -> {
                            calendarView.setToday(LocalDate.now());
                            calendarView.setTime(LocalTime.now());
                        });
                        try {
                            sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            updateTimeThread.setPriority(Thread.MIN_PRIORITY);
            updateTimeThread.setDaemon(true);
            updateTimeThread.start();

            sourceTrayController = new SourceTrayController();

            // Load initial data
            loadDataFromDatabase(true);

        } catch (Exception e) {
            System.out.println("Failed to initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refresh() {
        new Thread(() -> {
            try {
                CourseService courseService = new CourseService(new CourseDao());
                LessonService lessonService = new LessonService(new LessonDao());
                GroupService  groupService  = new GroupService(new GroupDao());

                final boolean isTeacher = SessionManager.getInstance().isTeacher();
                final String studentGroupCode = resolveStudentGroupCode(isTeacher);
                final Long studentUserId = isTeacher ? null
                        : (SessionManager.getInstance().getCurrentUser() != null
                                ? SessionManager.getInstance().getCurrentUser().getId() : null);

                List<Course> dbCourses;
                try {
                    dbCourses = courseService.getAllCourses();
                } catch (Exception e) {
                    System.err.println("Refresh: failed to load courses: " + e.getMessage());
                    dbCourses = new ArrayList<>();
                }

                java.util.Map<Course, java.util.List<Lesson>> courseToLessons = new java.util.LinkedHashMap<>();
                for (Course course : dbCourses) {
                    try {
                        List<Lesson> lessons = isTeacher
                                ? lessonService.getLessonsByCourse(course.getId())
                                : lessonService.getLessonsByCourseWithGroups(course.getId());
                        courseToLessons.put(course, lessons);
                    } catch (Exception ex) {
                        System.err.println("Refresh: failed lessons for " + course.getName() + ": " + ex.getMessage());
                        courseToLessons.put(course, new ArrayList<>());
                    }
                }

                List<StudentGroup> groups;
                try {
                    groups = groupService.getAllGroups();
                } catch (Exception e) {
                    System.err.println("Refresh: failed to load groups: " + e.getMessage());
                    groups = new ArrayList<>();
                }

                final List<StudentGroup> finalGroups = groups;
                final java.util.Map<Course, java.util.List<Lesson>> finalMap = courseToLessons;

                Platform.runLater(() -> {
                    try {
                        myCalendarSource.getCalendars().clear();
                        for (java.util.Map.Entry<Course, java.util.List<Lesson>> e : finalMap.entrySet()) {
                            Course course  = e.getKey();
                            Calendar cal   = courseService.toCalendar(course);
                            for (Lesson lesson : e.getValue()) {
                                if (!isTeacher && !lessonVisibleToStudent(lesson, studentGroupCode, studentUserId)) {
                                    continue;
                                }
                                Entry<CustomEntryPopOverContentPane.SavedLesson> entry =
                                        new Entry<>(course.getName());
                                entry.setInterval(new Interval(
                                        lesson.getStartAt().toLocalDate(),
                                        lesson.getStartAt().toLocalTime(),
                                        lesson.getEndAt().toLocalDate(),
                                        lesson.getEndAt().toLocalTime()
                                ));
                                entry.setUserObject(
                                        new CustomEntryPopOverContentPane.SavedLesson(lesson.getId()));
                                cal.addEntry(entry);
                            }
                            myCalendarSource.getCalendars().add(cal);
                        }

                        sourceTrayController.addSourceSectionsToSourceTray(
                                calendarView, myCalendarSource, finalGroups);
                        navbarController.refreshBadge();
                    } finally {
                        navbarController.stopSpin();
                    }
                });
            } catch (Exception e) {
                System.err.println("Refresh failed: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> navbarController.stopSpin());
            }
        }, "refresh-thread").start();
    }


    private void loadDataFromDatabase(boolean firstLoad) {
        CourseService courseService = new CourseService(new CourseDao());
        LessonService lessonService = new LessonService(new LessonDao());
        GroupService  groupService  = new GroupService(new GroupDao());

        myCalendarSource.getCalendars().clear();

        final boolean isTeacher = SessionManager.getInstance().isTeacher();
        final String studentGroupCode = resolveStudentGroupCode(isTeacher);
        final Long studentUserId = isTeacher ? null
                : (SessionManager.getInstance().getCurrentUser() != null
                        ? SessionManager.getInstance().getCurrentUser().getId() : null);

        try {
            List<Course> dbCourses = courseService.getAllCourses();
            System.out.println("Loaded " + dbCourses.size() + " courses from DB");
            for (Course course : dbCourses) {
                Calendar cal = courseService.toCalendar(course);
                try {
                    List<Lesson> lessons = isTeacher
                            ? lessonService.getLessonsByCourse(course.getId())
                            : lessonService.getLessonsByCourseWithGroups(course.getId());
                    System.out.println("  └─ Loaded " + lessons.size() + " lessons for course: " + course.getName());
                    for (Lesson lesson : lessons) {
                        if (!isTeacher && !lessonVisibleToStudent(lesson, studentGroupCode, studentUserId)) {
                            continue;
                        }
                        Entry<CustomEntryPopOverContentPane.SavedLesson> entry =
                                new Entry<>(course.getName());
                        entry.setInterval(new Interval(
                                lesson.getStartAt().toLocalDate(),
                                lesson.getStartAt().toLocalTime(),
                                lesson.getEndAt().toLocalDate(),
                                lesson.getEndAt().toLocalTime()
                        ));
                        entry.setUserObject(new CustomEntryPopOverContentPane.SavedLesson(lesson.getId()));
                        cal.addEntry(entry);
                    }
                } catch (Exception ex) {
                    System.out.println("  └─ Failed to load lessons for course " + course.getName() + ": " + ex.getMessage());
                }
                myCalendarSource.getCalendars().add(cal);
            }
        } catch (Exception e) {
            System.out.println("Failed to load courses: " + e.getMessage());
            e.printStackTrace();
        }

        List<StudentGroup> groups;
        try {
            groups = groupService.getAllGroups();
            System.out.println("Loaded " + groups.size() + " groups from DB");
        } catch (Exception e) {
            System.out.println("Failed to load groups: " + e.getMessage());
            e.printStackTrace();
            groups = new ArrayList<>();
        }
        final List<StudentGroup> finalGroups = groups;

        if (firstLoad) {
            Platform.runLater(() -> {
                calendarView.setShowSourceTray(true);
                calendarView.applyCss();
                calendarView.layout();
                sourceTrayController.addSourceSectionsToSourceTray(calendarView, myCalendarSource, finalGroups);

                SplitPane sp = calendarView.lookupAll(".split-pane").stream()
                        .filter(n -> n instanceof SplitPane)
                        .map(n -> (SplitPane) n)
                        .filter(p -> p.getItems().size() >= 2)
                        .findFirst()
                        .orElse(null);

                if (sp != null && sp.getWidth() > 0) {
                    double sidebarWidth = 300;
                    sp.setDividerPositions(sidebarWidth / sp.getWidth());
                    if (sp.getItems().get(0) instanceof javafx.scene.layout.Region r) {
                        r.setMinWidth(300);
                    }
                }
            });
        } else {
            sourceTrayController.addSourceSectionsToSourceTray(calendarView, myCalendarSource, finalGroups);
        }

        if (!firstLoad) {
            navbarController.refreshBadge();
        }
    }

    private String resolveStudentGroupCode(boolean isTeacher) {
        if (isTeacher) return null;
        org.entities.User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return null;
        try {
            StudentGroup g = new GroupDao().findGroupByUserId(currentUser.getId());
            if (g != null) {
                System.out.println("Student '" + currentUser.getUsername() + "' is in group: " + g.getGroupCode());
            } else {
                System.out.println("Student '" + currentUser.getUsername() + "' has no group assigned in student_profiles.");
            }
            return g != null ? g.getGroupCode() : null;
        } catch (Exception ex) {
            System.err.println("Failed to resolve student group: " + ex.getMessage());
            return null;
        }
    }

    private boolean lessonVisibleToStudent(Lesson lesson, String studentGroupCode, Long studentUserId) {
        // Check individual assignment first (always works regardless of group)
        List<org.entities.User> assignedUsers = lesson.getAssignedUsers();
        if (assignedUsers != null && studentUserId != null) {
            boolean directlyAssigned = assignedUsers.stream()
                    .anyMatch(u -> studentUserId.equals(u.getId()));
            if (directlyAssigned) return true;
        }
        // Check group assignment
        if (studentGroupCode == null) return false;
        List<StudentGroup> assignedGroups = lesson.getAssignedGroups();
        if (assignedGroups == null || assignedGroups.isEmpty()) return false;
        return assignedGroups.stream().anyMatch(g -> studentGroupCode.equals(g.getGroupCode()));
    }
}
