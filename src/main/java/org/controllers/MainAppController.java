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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MainAppController {

    @FXML private BorderPane mainRoot;

    @FXML
    public void initialize() {
        try {
            // Load navbar
            FXMLLoader navbarLoader = new FXMLLoader(getClass().getResource("/timetable-management-navbar.fxml"));
            BorderPane navbar = navbarLoader.load();
            NavbarController navbarController = navbarLoader.getController();
            mainRoot.setTop(navbar);

            // Build calendar view
            CalendarView calendarView = new CalendarView();
            mainRoot.setCenter(calendarView);

            navbarController.setCalendarView(calendarView);

            // Fetch courses from DB
            CourseService courseService = new CourseService(new CourseDao());
            LessonService lessonService = new LessonService(new LessonDao());
            CalendarSource myCalendarSource = new CalendarSource("Courses");
            try {
                List<Course> dbCourses = courseService.getAllCourses();
                System.out.println("Loaded " + dbCourses.size() + " courses from DB");
                for (Course course : dbCourses) {
                    Calendar cal = courseService.toCalendar(course);

                    // Load persisted lessons for this course and add as entries
                    try {
                        List<Lesson> lessons = lessonService.getLessonsByCourse(course.getId());
                        System.out.println("  └─ Loaded " + lessons.size() + " lessons for course: " + course.getName());
                        for (Lesson lesson : lessons) {
                            Entry<CustomEntryPopOverContentPane.SavedLesson> entry =
                                    new Entry<>(course.getName());
                            entry.setInterval(new Interval(
                                    lesson.getStartAt().toLocalDate(),
                                    lesson.getStartAt().toLocalTime(),
                                    lesson.getEndAt().toLocalDate(),
                                    lesson.getEndAt().toLocalTime()
                            ));
                            // Mark as saved so closing the popover won't delete it
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

            // Fetch groups from DB
            GroupService groupService = new GroupService(new GroupDao());
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

            // Remove CalendarFX's built-in default source
            calendarView.getCalendarSources().clear();
            calendarView.getCalendarSources().add(myCalendarSource);
            calendarView.setRequestedTime(LocalTime.now());
            // Keep CalendarFX default tray hidden until custom tray content is ready.
            calendarView.setShowSourceTray(false);
            calendarView.setShowToolBar(false);
            calendarView.showWeekPage();

            calendarView.setEntryDetailsPopOverContentCallback(param ->
                    new CustomEntryPopOverContentPane(
                            param.getPopOver(),
                            param.getDateControl(),
                            param.getEntry()));

            // Disable drag-to-move and drag-to-resize on all entries
            calendarView.setEntryEditPolicy(param -> switch (param.getEditOperation()) {
                case MOVE, CHANGE_START, CHANGE_END -> false;
                default -> true;
            });

            // Time update thread
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

            // Build source tray after CalendarFX has laid out
            SourceTrayController sourceTrayController = new SourceTrayController();
            Platform.runLater(() -> {
                // Enable tray only when we are ready to replace default content.
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
                    if (sp.getItems().get(0) instanceof javafx.scene.layout.Region) {
                        ((javafx.scene.layout.Region) sp.getItems().get(0)).setMinWidth(200);
                    }
                }
            });

        } catch (Exception e) {
            System.out.println("Failed to initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
