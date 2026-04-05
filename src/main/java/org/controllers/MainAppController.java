package org.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.view.CalendarView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import org.application.CustomEntryPopOverContentPane;
import org.dao.CourseDao;
import org.dao.GroupDao;
import org.dao.LessonDao;
import org.entities.Course;
import org.entities.Lesson;
import org.entities.StudentGroup;
import org.service.CourseService;
import org.service.LessonService;
import org.service.LocalizationService;
import org.service.SessionManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Root controller for the main application scene ({@code main-app.fxml}).
 * Owns the {@link CalendarView}, the {@link CalendarSource} for courses, and
 * coordinates the {@link NavbarController} and {@link SourceTrayController}.
 *
 * <p>On initialization it:
 * <ol>
 *   <li>Loads the navbar FXML and injects it into the top of the border pane.</li>
 *   <li>Creates the {@link CalendarView} and configures entry interactions
 *       (teachers can create/edit; students are read-only).</li>
 *   <li>Calls {@link #loadDataFromDatabase(boolean)} to populate courses and
 *       lessons from the database and build the source tray.</li>
 *   <li>Starts a daemon thread that keeps the calendar's today/time current.</li>
 * </ol>
 *
 * <p>The {@link #refresh()} method re-fetches all data on a background thread
 * and is triggered by the navbar refresh button.
 */
public class MainAppController {

    @FXML private BorderPane mainRoot;

    // Kept as fields so refresh() can reach them without re-initialising the whole scene
    private CalendarView calendarView;
    private CalendarSource myCalendarSource;
    private SourceTrayController sourceTrayController;
    private NavbarController navbarController;
    private final LocalizationService localizationService = new LocalizationService();

    ResourceBundle selectedBundle = localizationService.getBundle();

    /**
     * JavaFX initialize callback — sets up the full main-app scene including the
     * navbar, calendar view, source tray, and initial database load.
     */
    @FXML
    public void initialize() {
        try {

            FXMLLoader navbarLoader = new FXMLLoader(getClass().getResource("/timetable-management-navbar.fxml"), localizationService.getBundle());
            localizationService.swapSides(mainRoot);
            BorderPane navbar = navbarLoader.load();
            navbarController = navbarLoader.getController();
            mainRoot.setTop(navbar);

            System.out.println(Locale.getDefault());
            calendarView = new CalendarView();
            mainRoot.setCenter(calendarView);

            calendarView.selectedPageProperty().addListener((obs, o, n) ->
                    localizeCalendarSafe());

            calendarView.dateProperty().addListener((obs, o, n) ->
                    localizeCalendarSafe());

            calendarView.skinProperty().addListener((obs, o, n) ->
                    localizeCalendarSafe());

            navbarController.setCalendarView(calendarView);
            // Give the navbar a handle so the refresh button can call back here
            navbarController.setMainAppController(this);

            myCalendarSource = new CalendarSource(selectedBundle.getString("sourcetray.courses.section.title"));
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

    /**
     * Re-fetches all courses and lessons from the database on a background thread
     * and rebuilds the calendar entries and source tray on the JavaFX thread.
     * Called by the navbar refresh button; the spinner animation is stopped in
     * {@code finally} regardless of outcome.
     */
    public void refresh() {
        new Thread(() -> {
            try {
                CourseService courseService = new CourseService(new CourseDao());
                LessonService lessonService = new LessonService(new LessonDao());

                final boolean isTeacher = SessionManager.getInstance().isTeacher();
                final String studentGroupCode = resolveStudentGroupCode(isTeacher);
                final Long studentUserId = isTeacher ? null
                        : (SessionManager.getInstance().getCurrentUser() != null
                                ? SessionManager.getInstance().getCurrentUser().getId() : null);

                List<Course> dbCourses;
                try {
                    dbCourses = courseService.getCoursesForUser(studentUserId);
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

                        // SourceTrayController fetches its own groups
                        sourceTrayController.addSourceSectionsToSourceTray(calendarView, myCalendarSource);
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


    /**
     * Loads courses and their lessons from the database into the calendar source.
     * On the first load it also shows the source tray and sets the sidebar divider position.
     * Students see only lessons assigned to their group or directly to them.
     *
     * @param firstLoad {@code true} on application startup — triggers source-tray setup
     *                  and divider positioning on the JavaFX thread
     */
    private void loadDataFromDatabase(boolean firstLoad) {
        CourseService courseService = new CourseService(new CourseDao());
        LessonService lessonService = new LessonService(new LessonDao());

        myCalendarSource.getCalendars().clear();

        final boolean isTeacher = SessionManager.getInstance().isTeacher();
        final String studentGroupCode = resolveStudentGroupCode(isTeacher);
        final Long studentUserId = isTeacher ? null : (SessionManager.getInstance().getCurrentUser() != null ? SessionManager.getInstance().getCurrentUser().getId() : null);

        try {
            List<Course> dbCourses = courseService.getCoursesForUser(studentUserId);
            System.out.println("Loaded " + dbCourses.size() + " courses from DB");
            for (Course course : dbCourses) {
                Calendar cal = courseService.toCalendar(course);
                try {
                    List<Lesson> lessons = isTeacher
                            ? lessonService.getLessonsByCourse(course.getId())
                            : lessonService.getLessonsByCourseWithGroups(course.getId());
                    System.out.println("Loaded " + lessons.size() + " lessons for course: " + course.getName());
                    for (Lesson lesson : lessons) {
                        if (!isTeacher && !lessonVisibleToStudent(lesson, studentGroupCode, studentUserId)) {
                            continue;
                        }
                        Entry<CustomEntryPopOverContentPane.SavedLesson> entry = new Entry<>(course.getName());
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
                    System.out.println("Failed to load lessons for course " + course.getName() + ": " + ex.getMessage());
                }
                myCalendarSource.getCalendars().add(cal);
            }
        } catch (Exception e) {
            System.out.println("Failed to load courses: " + e.getMessage());
            e.printStackTrace();
        }

        if (firstLoad) {
            Platform.runLater(() -> {
                calendarView.setShowSourceTray(true);
                calendarView.applyCss();
                calendarView.layout();
                sourceTrayController.addSourceSectionsToSourceTray(calendarView, myCalendarSource);

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
            sourceTrayController.addSourceSectionsToSourceTray(calendarView, myCalendarSource);
        }

        if (!firstLoad) {
            navbarController.refreshBadge();
        }
    }

    /**
     * Returns the group code of the current student's assigned group, or {@code null}
     * if the user is a teacher, has no group, or the lookup fails.
     *
     * @param isTeacher {@code true} if the current session user is a teacher
     * @return the group code string, or {@code null}
     */
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

    /**
     * Returns {@code true} if the given lesson should be shown to a specific student.
     * A lesson is visible if the student is directly assigned to it OR their group is assigned.
     *
     * @param lesson           the lesson to check
     * @param studentGroupCode the student's group code, or {@code null} if unassigned
     * @param studentUserId    the student's DB user ID, or {@code null} if unknown
     * @return {@code true} if the lesson is visible to this student
     */
    private boolean lessonVisibleToStudent(Lesson lesson, String studentGroupCode, Long studentUserId) {
        // Check individual assignment first
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

    /**
     * Walks the CalendarFX node tree and replaces hard-coded English button/label
     * text with the active locale's translations. Targets the "Today" button and
     * the "All Day" label, matching against all four supported locale variants so
     * the replacement is idempotent regardless of the previous locale.
     */
    private void localizeCalendar() {

        // берём переводы один раз
        String todayLocalized = localizationService.getBundle().getString("today.button");

        Set<String> todayVariants = getVariants("today.button");

        calendarView.lookupAll(".button").forEach(node -> {
            if (node instanceof Button b) {

                String text = b.getText();
                if (text == null) return;

                if (todayVariants.contains(text) || text.equals(todayLocalized)) {
                    b.setText(todayLocalized);
                }
            }
        });

        String allDayLocalized = localizationService.getBundle().getString("calendar.label.allDay");
        Set<String> allDayVariants = getVariants("calendar.label.allDay");
        calendarView.lookupAll(".label").forEach(node -> {
            if (node instanceof Label l) {

                String text = l.getText();
                if (text == null) return;

                if (allDayVariants.contains(text)) {
                    l.setText(allDayLocalized);
                }
            }
        });
    }

    /**
     * Schedules {@link #localizeCalendar()} to run 5 times at 50 ms intervals via a
     * {@link Timeline}. Repeated attempts compensate for CalendarFX nodes that are
     * created lazily after a page switch or skin change.
     */
    private void localizeCalendarSafe() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(50), e -> localizeCalendar())
        );
        timeline.setCycleCount(5); // 5 попыток
        timeline.play();
    }

    /**
     * Returns the set of translations for a bundle key across all four supported
     * locales (en_US, ru_RU, fi_FI, ar_IQ). Used by {@link #localizeCalendar()} to
     * recognize a label regardless of the locale it was rendered in.
     *
     * @param key the resource bundle key to look up
     * @return a {@link Set} of all four translated strings
     */
    private Set<String> getVariants(String key) {
        return Set.of(
                ResourceBundle.getBundle("i18n/MessagesBundle", new Locale("en","US")).getString(key),
                ResourceBundle.getBundle("i18n/MessagesBundle", new Locale("ru", "RU")).getString(key),
                ResourceBundle.getBundle("i18n/MessagesBundle", new Locale("fi", "FI")).getString(key),
                ResourceBundle.getBundle("i18n/MessagesBundle", new Locale("ar", "IQ")).getString(key)
        );
    }
}
