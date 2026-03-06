package org.application;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.controllers.NavbarController;
import org.controllers.SourceTrayController;
import org.dao.CourseDao;
import org.entities.Course;
import org.service.CourseService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CalendarApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/timetable-management-navbar.fxml"));
        BorderPane navbar = loader.load();
        NavbarController navbarController = loader.getController();

        BorderPane root = new BorderPane();
        root.setTop(navbar);

        CalendarView calendarView = new CalendarView();
        root.setCenter(calendarView);

        // Wire navbar toggle buttons and search to the calendar view
        navbarController.setCalendarView(calendarView);

        // Fetch courses from DB and convert each to a CalendarFX Calendar
        CourseService courseService = new CourseService(new CourseDao());
        CalendarSource myCalendarSource = new CalendarSource("Courses");

        try {
            List<Course> dbCourses = courseService.getAllCourses();
            for (Course course : dbCourses) {
                Calendar cal = courseService.toCalendar(course);
                myCalendarSource.getCalendars().add(cal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> groups = new ArrayList<>();
        groups.add("Group 1");
        groups.add("Group 2");

        calendarView.getCalendarSources().add(myCalendarSource);
        calendarView.setRequestedTime(LocalTime.now());
        calendarView.setShowSourceTray(true);
        calendarView.setShowToolBar(false);
        calendarView.showWeekPage();

        calendarView.setEntryDetailsPopOverContentCallback(param ->
                new CustomEntryPopOverContentPane(
                        param.getPopOver(),
                        param.getDateControl(),
                        param.getEntry()));

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

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("Easytable");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1300);
        primaryStage.setHeight(1000);
        primaryStage.centerOnScreen();
        primaryStage.show();

        SourceTrayController sourceTrayController = new SourceTrayController();

        // Loads after calendarfx has finished loading
        Platform.runLater(() -> {
            calendarView.applyCss();
            calendarView.layout();
            sourceTrayController.addSourceSectionsToSourceTray(calendarView, myCalendarSource, groups);

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
    }

    public static void main(String[] args) {
        launch(args);
    }
}
