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
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.application.EntryPopOverContentPane;
import org.entities.User;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Root controller for the main application scene ({@code main-app.fxml}).
 */
public class MainAppController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainAppController.class);
    private static final String MESSAGES_BUNDLE = "i18n/MessagesBundle";

    @FXML private BorderPane mainRoot;

    private CalendarView calendarView;
    private CalendarSource myCalendarSource;
    private SourceTrayController sourceTrayController;
    private NavbarController navbarController;
    private final LocalizationService localizationService = new LocalizationService();

    ResourceBundle selectedBundle = localizationService.getBundle();

    /**
     * JavaFX initialize callback.
     */
    @FXML
    public void initialize() {
        try {
            FXMLLoader navbarLoader = new FXMLLoader(
                    getClass().getResource("/ui/timetable-management-navbar.fxml"),
                    localizationService.getBundle());
            localizationService.swapSides(mainRoot);
            BorderPane navbar = navbarLoader.load();
            navbarController = navbarLoader.getController();
            mainRoot.setTop(navbar);

            calendarView = new CalendarView();
            mainRoot.setCenter(calendarView);

            calendarView.selectedPageProperty().addListener((obs, o, n) -> localizeCalendarSafe());
            calendarView.dateProperty().addListener((obs, o, n) -> localizeCalendarSafe());
            calendarView.skinProperty().addListener((obs, o, n) -> localizeCalendarSafe());

            navbarController.setCalendarView(calendarView);
            navbarController.setMainAppController(this);

            myCalendarSource = new CalendarSource(selectedBundle.getString("sourcetray.courses.section.title"));
            calendarView.getCalendarSources().clear();
            calendarView.getCalendarSources().add(myCalendarSource);
            calendarView.setRequestedTime(LocalTime.now());
            calendarView.setShowSourceTray(false);
            calendarView.setShowToolBar(false);
            calendarView.showWeekPage();

            calendarView.setEntryDetailsPopOverContentCallback(param ->
                    new EntryPopOverContentPane(
                            param.getPopOver(),
                            param.getDateControl(),
                            param.getEntry()));

            calendarView.setEntryEditPolicy(param -> {
                if (!SessionManager.getInstance().isTeacher()) {
                    return false;
                }
                return switch (param.getEditOperation()) {
                    case MOVE, CHANGE_START, CHANGE_END -> false;
                    default -> true;
                };
            });

            if (!SessionManager.getInstance().isTeacher()) {
                calendarView.setEntryFactory(param -> null);
            }

            startTimeUpdateThread();

            sourceTrayController = new SourceTrayController();

            Platform.runLater(() -> {
                calendarView.setShowSourceTray(true);
                calendarView.applyCss();
                calendarView.layout();
                sourceTrayController.showSkeleton(calendarView);
                loadDataFromDatabase(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startTimeUpdateThread() {
        Thread timeThread = Thread.ofVirtual().name("Calendar: Update Time Thread").start(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Platform.runLater(() -> {
                    calendarView.setToday(LocalDate.now());
                    calendarView.setTime(LocalTime.now());
                });
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        timeThread.toString(); // suppress unused-variable warning; thread runs in background
    }

    /**
     * Re-fetches all courses and lessons from the database on a background thread.
     */
    public void refresh() {
        new Thread(() -> {
            try {
                CourseService courseService = new CourseService(new CourseDao());
                LessonService lessonService = new LessonService(new LessonDao());
                boolean isTeacher = SessionManager.getInstance().isTeacher();
                String studentGroupCode = resolveStudentGroupCode(isTeacher);
                Long studentUserId = resolveStudentUserId(isTeacher);

                List<Course> dbCourses = fetchCourses(courseService, studentUserId, "Refresh");
                Map<Course, List<Lesson>> courseToLessons =
                        buildCourseToLessonsMap(dbCourses, isTeacher, lessonService);

                Platform.runLater(() -> {
                    try {
                        myCalendarSource.getCalendars().clear();
                        populateCalendarEntries(courseService, courseToLessons,
                                isTeacher, studentGroupCode, studentUserId);
                        sourceTrayController.addSourceSectionsToSourceTray(calendarView, myCalendarSource);
                        navbarController.refreshBadge();
                    } finally {
                        navbarController.stopSpin();
                    }
                });
            } catch (Exception e) {
                LOGGER.warn("Refresh failed: {}", e.getMessage());
                Platform.runLater(() -> navbarController.stopSpin());
            }
        }, "refresh-thread").start();
    }

    /**
     * Loads courses and their lessons from the database into the calendar source.
     *
     * @param firstLoad {@code true} on application startup
     */
    private void loadDataFromDatabase(boolean firstLoad) {
        final boolean isTeacher = SessionManager.getInstance().isTeacher();
        final String studentGroupCode = resolveStudentGroupCode(isTeacher);
        final Long studentUserId = resolveStudentUserId(isTeacher);

        navbarController.startSpin();

        new Thread(() -> {
            try {
                CourseService courseService = new CourseService(new CourseDao());
                LessonService lessonService = new LessonService(new LessonDao());

                List<Course> dbCourses = fetchCourses(courseService, studentUserId, "loadData");
                Map<Course, List<Lesson>> courseToLessons =
                        buildCourseToLessonsMap(dbCourses, isTeacher, lessonService);

                final CourseService finalCourseService = courseService;
                Platform.runLater(() -> {
                    try {
                        myCalendarSource.getCalendars().clear();
                        populateCalendarEntries(finalCourseService, courseToLessons,
                                isTeacher, studentGroupCode, studentUserId);
                        sourceTrayController.addSourceSectionsToSourceTray(calendarView, myCalendarSource);
                        navbarController.refreshBadge();
                        if (firstLoad) {
                            applySidebarDivider();
                        }
                    } finally {
                        navbarController.stopSpin();
                    }
                });
            } catch (Exception e) {
                LOGGER.warn("loadDataFromDatabase failed: {}", e.getMessage());
                Platform.runLater(() -> navbarController.stopSpin());
            }
        }, "load-data-thread").start();
    }

    private List<Course> fetchCourses(CourseService courseService, Long studentUserId, String context) {
        try {
            return courseService.getCoursesForUser(studentUserId);
        } catch (Exception e) {
            LOGGER.warn("{}: failed to load courses: {}", context, e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Lesson> fetchLessonsForCourse(Course course, boolean isTeacher, LessonService lessonService) {
        try {
            return isTeacher
                    ? lessonService.getLessonsByCourse(course.getId())
                    : lessonService.getLessonsByCourseWithGroups(course.getId());
        } catch (Exception ex) {
            LOGGER.warn("Failed to load lessons for {}: {}", course.getDisplayName(), ex.getMessage());
            return new ArrayList<>();
        }
    }

    private Map<Course, List<Lesson>> buildCourseToLessonsMap(List<Course> courses,
                                                               boolean isTeacher,
                                                               LessonService lessonService) {
        Map<Course, List<Lesson>> map = new LinkedHashMap<>();
        for (Course course : courses) {
            map.put(course, fetchLessonsForCourse(course, isTeacher, lessonService));
        }
        return map;
    }

    private void populateCalendarEntries(CourseService courseService,
                                          Map<Course, List<Lesson>> courseToLessons,
                                          boolean isTeacher,
                                          String studentGroupCode,
                                          Long studentUserId) {
        for (Map.Entry<Course, List<Lesson>> e : courseToLessons.entrySet()) {
            Course course = e.getKey();
            Calendar<Course> cal = courseService.toCalendar(course);
            for (Lesson lesson : e.getValue()) {
                if (!isTeacher && !lessonVisibleToStudent(lesson, studentGroupCode, studentUserId)) {
                    continue;
                }
                Entry<EntryPopOverContentPane.SavedLesson> entry = new Entry<>(course.getDisplayName());
                entry.setInterval(new Interval(
                        lesson.getStartAt().toLocalDate(), lesson.getStartAt().toLocalTime(),
                        lesson.getEndAt().toLocalDate(),   lesson.getEndAt().toLocalTime()));
                entry.setUserObject(new EntryPopOverContentPane.SavedLesson(lesson.getId()));
                cal.addEntry(entry);
            }
            myCalendarSource.getCalendars().add(cal);
        }
    }

    private void applySidebarDivider() {
        SplitPane sp = calendarView.lookupAll(".split-pane").stream()
                .filter(SplitPane.class::isInstance)
                .map(SplitPane.class::cast)
                .filter(p -> p.getItems().size() >= 2)
                .findFirst()
                .orElse(null);

        if (sp != null && sp.getWidth() > 0) {
            double sidebarWidth = 300;
            sp.setDividerPositions(sidebarWidth / sp.getWidth());
            if (sp.getItems().get(0) instanceof Region r) {
                r.setMinWidth(300);
            }
        }
    }

    private Long resolveStudentUserId(boolean isTeacher) {
        if (isTeacher) {
            return null;
        }
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }

    /**
     * Returns the group code of the current student's assigned group, or {@code null}.
     */
    private String resolveStudentGroupCode(boolean isTeacher) {
        if (isTeacher) {
            return null;
        }
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        try {
            StudentGroup g = new GroupDao().findGroupByUserId(currentUser.getId());
            return g != null ? g.getGroupCode() : null;
        } catch (Exception ex) {
            LOGGER.warn("Failed to resolve student group: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Returns {@code true} if the given lesson should be shown to a specific student.
     */
    private boolean lessonVisibleToStudent(Lesson lesson, String studentGroupCode, Long studentUserId) {
        List<User> assignedUsers = lesson.getAssignedUsers();
        if (studentUserId != null) {
            boolean directlyAssigned = assignedUsers.stream()
                    .anyMatch(u -> studentUserId.equals(u.getId()));
            if (directlyAssigned) {
                return true;
            }
        }
        if (studentGroupCode == null) {
            return false;
        }
        List<StudentGroup> assignedGroups = lesson.getAssignedGroups();
        if (assignedGroups.isEmpty()) {
            return false;
        }
        return assignedGroups.stream().anyMatch(g -> studentGroupCode.equals(g.getGroupCode()));
    }

    /**
     * Translates hard-coded CalendarFX button/label text using the active locale.
     */
    private void localizeCalendar() {
        String todayLocalized = localizationService.getBundle().getString("today.button");
        Set<String> todayVariants = getVariants("today.button");
        calendarView.lookupAll(".button").forEach(node -> {
            if (node instanceof Button b) {
                localizeButton(b, todayLocalized, todayVariants);
            }
        });

        String allDayLocalized = localizationService.getBundle().getString("calendar.label.allDay");
        Set<String> allDayVariants = getVariants("calendar.label.allDay");
        calendarView.lookupAll(".label").forEach(node -> {
            if (node instanceof Label l) {
                localizeLabel(l, allDayLocalized, allDayVariants);
            }
        });
    }

    private void localizeButton(Button b, String localized, Set<String> variants) {
        String text = b.getText();
        if (text != null && (variants.contains(text) || text.equals(localized))) {
            b.setText(localized);
        }
    }

    private void localizeLabel(Label l, String localized, Set<String> variants) {
        String text = l.getText();
        if (text != null && variants.contains(text)) {
            l.setText(localized);
        }
    }

    /**
     * Schedules {@link #localizeCalendar()} to run 5 times at 50 ms intervals.
     */
    private void localizeCalendarSafe() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(50), e -> localizeCalendar())
        );
        timeline.setCycleCount(5);
        timeline.play();
    }

    /**
     * Returns the set of translations for a bundle key across all four supported locales.
     */
    private Set<String> getVariants(String key) {
        return Set.of(
                ResourceBundle.getBundle(MESSAGES_BUNDLE, Locale.of("en", "US")).getString(key),
                ResourceBundle.getBundle(MESSAGES_BUNDLE, Locale.of("ru", "RU")).getString(key),
                ResourceBundle.getBundle(MESSAGES_BUNDLE, Locale.of("fi", "FI")).getString(key),
                ResourceBundle.getBundle(MESSAGES_BUNDLE, Locale.of("ar", "IQ")).getString(key)
        );
    }
}
