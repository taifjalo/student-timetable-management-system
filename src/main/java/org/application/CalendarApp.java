package org.application;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.textfield.CustomTextField;

import java.time.LocalDate;
import java.time.LocalTime;

public class CalendarApp extends Application {

    @FXML private ToggleButton calViewDay;
    @FXML private ToggleButton calViewWeek;
    @FXML private ToggleButton calViewMonth;
    @FXML private ToggleButton calViewYear;
    @FXML private CustomTextField fieldSearch;

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/timetable-management-navbar.fxml"));
        loader.setController(this);
        BorderPane navbar = loader.load();

        BorderPane root = new BorderPane();
        root.setTop(navbar);

        CalendarView calendarView = new CalendarView(); // (1)

        root.setCenter(calendarView);

        Calendar birthdays = new Calendar("Birthdays"); // (2)
        Calendar holidays = new Calendar("Holidays");
        Calendar Math = new Calendar("Math");

        birthdays.setStyle(Style.STYLE1); // (3)
        holidays.setStyle(Style.STYLE2);
        Math.setStyle(Style.STYLE1);

        CalendarSource myCalendarSource = new CalendarSource("My Calendars"); // (4)
        myCalendarSource.getCalendars().addAll(birthdays, holidays, Math);

        calendarView.getCalendarSources().addAll(myCalendarSource); // (5)

        calendarView.setRequestedTime(LocalTime.now());

        // ADD OWN LOGIC ↓↓↓:

        calendarView.setShowSourceTray(true);
        // Disable default calendarfx navbar
        calendarView.setShowToolBar(false);
        calendarView.showWeekPage();

        // Bind search field to CalendarView search
        fieldSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            calendarView.getSearchField().setText(newVal);
        });

        calViewDay.setOnAction(e -> {
            calendarView.showDayPage();
            calViewDay.setSelected(true);
        });
        calViewWeek.setOnAction(e -> {
            calendarView.showWeekPage();
            calViewWeek.setSelected(true);
        });
        calViewMonth.setOnAction(e -> {
            calendarView.showMonthPage();
            calViewMonth.setSelected(true);
        });
        calViewYear.setOnAction(e -> {
            calendarView.showYearPage();
            calViewYear.setSelected(true);
        });

        // END OF OWN LOGIC ↑↑↑

        Thread updateTimeThread = new Thread("Calendar: Update Time Thread") {
            @Override
            public void run() {
                while (true) {
                    Platform.runLater(() -> {
                        calendarView.setToday(LocalDate.now());
                        calendarView.setTime(LocalTime.now());
                    });

                    try {
                        // update every 10 seconds
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

        Platform.runLater(() -> {
            calendarView.applyCss();
            calendarView.layout();

            SplitPane sp = calendarView.lookupAll(".split-pane").stream()
                    .filter(n -> n instanceof SplitPane)
                    .map(n -> (SplitPane) n)
                    .filter(p -> p.getItems().size() >= 2)
                    .findFirst()
                    .orElse(null);

            if (sp != null && sp.getWidth() > 0) {
                double sidebarWidth = 300;
                sp.setDividerPositions(sidebarWidth / sp.getWidth());

                // Set minimum width for sidebar (in this case 250px)
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