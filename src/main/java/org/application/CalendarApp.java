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
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.controlsfx.control.textfield.CustomTextField;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.collections.ListChangeListener;

import java.time.LocalDate;
import java.time.LocalTime;
import java.io.IOException;

public class CalendarApp extends Application {

    @FXML private ToggleButton calViewDay;
    @FXML private ToggleButton calViewWeek;
    @FXML private ToggleButton calViewMonth;
    @FXML private ToggleButton calViewYear;
    @FXML private CustomTextField fieldSearch;

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/timetable-management-navbar.fxml"));
        BorderPane navbar = loader.load();
        CalendarApp controller = loader.getController();

        // Use fields from the loaded controller
        this.fieldSearch = controller.fieldSearch;
        this.calViewDay = controller.calViewDay;
        this.calViewWeek = controller.calViewWeek;
        this.calViewMonth = controller.calViewMonth;
        this.calViewYear = controller.calViewYear;

        BorderPane root = new BorderPane();
        root.setTop(navbar);

        CalendarView calendarView = new CalendarView();

        root.setCenter(calendarView);

        Calendar birthdays = new Calendar("Kuvaus- ja mallintamismenetelmät");
        Calendar holidays = new Calendar("Ohjelmistotekniikka");
        Calendar Math = new Calendar("Matematiikka");

        birthdays.setStyle(Style.STYLE1);
        holidays.setStyle(Style.STYLE2);
        Math.setStyle(Style.STYLE1);

        CalendarSource myCalendarSource = new CalendarSource("Courses");
        myCalendarSource.getCalendars().addAll(birthdays, holidays, Math);

        CalendarSource groupSource = new CalendarSource("Groups");
        Calendar group1 = new Calendar("Group 1");
        Calendar group2 = new Calendar("Group 2");
        group1.setStyle(Style.STYLE3);
        group2.setStyle(Style.STYLE4);
        groupSource.getCalendars().addAll(group1, group2);

        calendarView.getCalendarSources().addAll(myCalendarSource, groupSource);

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
            addSourceSectionsToSourceTray(calendarView, myCalendarSource, groupSource);

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

    private void addSourceSectionsToSourceTray(CalendarView calendarView, CalendarSource courseSource, CalendarSource groupSource) {
        ScrollPane sourceScrollPane = calendarView.lookupAll(".source-view-scroll-pane").stream()
                .filter(ScrollPane.class::isInstance)
                .map(ScrollPane.class::cast)
                .findFirst()
                .orElse(null);

        if (sourceScrollPane == null || sourceScrollPane.getContent() == null) {
            return;
        }

        Node sourceContent = sourceScrollPane.getContent();

        Node coursesSection = createSourceSectionNode(courseSource);
        Node groupsSection = createSourceSectionNode(groupSource);
        if (coursesSection == null || groupsSection == null) {
            return;
        }

        VBox wrapper = new VBox(12, sourceContent, coursesSection, groupsSection);
        wrapper.getStyleClass().add("source-tray-content");
        sourceScrollPane.setFitToWidth(true);
        sourceScrollPane.setContent(wrapper);
    }

    private Node createSourceSectionNode(CalendarSource source) {
        FXMLLoader groupsLoader = new FXMLLoader(getClass().getResource("/sourcetray-groups.fxml"));
        Node sourceSection;
        try {
            sourceSection = groupsLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Text sectionTitleText = (Text) groupsLoader.getNamespace().get("sectionTitleText");
        VBox groupsListContainer = (VBox) groupsLoader.getNamespace().get("groupsListContainer");

        if (sectionTitleText == null || groupsListContainer == null) {
            return null;
        }

        Runnable refreshRows = () -> {
            groupsListContainer.getChildren().clear();
            for (Calendar calendar : source.getCalendars()) {
                groupsListContainer.getChildren().add(createGroupRow(calendar.getName()));
            }
        };

        sectionTitleText.setText(source.getName());
        refreshRows.run();

        source.nameProperty().addListener((obs, oldName, newName) -> sectionTitleText.setText(newName));
        source.getCalendars().addListener((ListChangeListener<Calendar>) change -> refreshRows.run());

        return sourceSection;
    }

    private HBox createGroupRow(String groupName) {
        Text groupNameText = new Text(groupName);
        groupNameText.setStrokeWidth(0.0);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        ImageView editIcon = new ImageView(new Image(getClass().getResource("/images/edit_dots_icon.png").toExternalForm()));
        editIcon.setFitWidth(15.0);
        editIcon.setFitHeight(15.0);
        editIcon.setPickOnBounds(true);
        editIcon.setPreserveRatio(true);

        Button actionButton = new Button();
        actionButton.setMnemonicParsing(false);
        actionButton.setGraphic(editIcon);

        HBox row = new HBox(groupNameText, spacer, actionButton);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }

    @FXML

    private void handleProfileClick(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
