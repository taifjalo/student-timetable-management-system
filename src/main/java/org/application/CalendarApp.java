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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.controllers.CreateCourseModalController;
import org.controllers.CreateGroupModalController;
import org.controlsfx.control.textfield.CustomTextField;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.collections.ListChangeListener;

import java.time.LocalDate;
import java.time.LocalTime;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        ArrayList<String> groups = new ArrayList<>();

        groups.add("Group 1");
        groups.add("Group 2");

        calendarView.getCalendarSources().add(myCalendarSource);

        calendarView.setRequestedTime(LocalTime.now());

        // ADD OWN LOGIC ↓↓↓:

        calendarView.setShowSourceTray(true);
        // Disable default calendarfx navbar
        calendarView.setShowToolBar(false);
        calendarView.showWeekPage();

        // Replace the default event pop-over content with our custom version
        // that adds an "Extra Details" accordion section (location, description, instructor).
        calendarView.setEntryDetailsPopOverContentCallback(param ->
                new CustomEntryPopOverContentPane(
                        param.getPopOver(),
                        param.getDateControl(),
                        param.getEntry()));

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
            addSourceSectionsToSourceTray(calendarView, myCalendarSource, groups);

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

    private void addSourceSectionsToSourceTray(CalendarView calendarView, CalendarSource courseSource, List<String> groups) {
        ScrollPane sourceScrollPane = calendarView.lookupAll(".source-view-scroll-pane").stream()
                .filter(ScrollPane.class::isInstance)
                .map(ScrollPane.class::cast)
                .findFirst()
                .orElse(null);

        if (sourceScrollPane == null || sourceScrollPane.getContent() == null) {
            return;
        }

        Node coursesSection = createSourceSectionNode(courseSource);
        Node groupsSection = createSourceSectionNode("Groups", groups);
        if (coursesSection == null || groupsSection == null) {
            return;
        }

        Separator sectionDivider = new Separator();
        VBox.setMargin(sectionDivider, new javafx.geometry.Insets(20, 0, 0, 0));
        VBox wrapper = new VBox(12, coursesSection, sectionDivider, groupsSection);
        wrapper.getStyleClass().add("source-tray-content");
        sourceScrollPane.setFitToWidth(true);
        sourceScrollPane.setContent(wrapper);
    }

    private Node createSourceSectionNode(CalendarSource source) {
        FXMLLoader groupsLoader = new FXMLLoader(getClass().getResource("/sourcetray-groups.fxml"));
        groupsLoader.setController(this);
        Node sourceSection;
        try {
            sourceSection = groupsLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Label sectionTitleText = (Label) groupsLoader.getNamespace().get("sectionTitleText");
        VBox groupsListContainer = (VBox) groupsLoader.getNamespace().get("groupsListContainer");
        Button addButton = (Button) groupsLoader.getNamespace().get("addButton");

        if (sectionTitleText == null || groupsListContainer == null) {
            return null;
        }

        // Tag the add button with the section name so the handler knows what to create
        if (addButton != null) {
            addButton.setUserData(source.getName());
        }

        Runnable refreshRows = () -> {
            groupsListContainer.getChildren().clear();
            for (Calendar calendar : source.getCalendars()) {
                groupsListContainer.getChildren().add(createCalendarRow(calendar, source.getName()));
            }
        };

        sectionTitleText.setText(source.getName());
        refreshRows.run();

        source.nameProperty().addListener((obs, oldName, newName) -> sectionTitleText.setText(newName));
        source.getCalendars().addListener((ListChangeListener<Calendar>) change -> refreshRows.run());

        return sourceSection;
    }

    private Node createSourceSectionNode(String sectionTitle, List<String> items) {
        FXMLLoader groupsLoader = new FXMLLoader(getClass().getResource("/sourcetray-groups.fxml"));
        groupsLoader.setController(this);
        Node sourceSection;
        try {
            sourceSection = groupsLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Label sectionTitleText = (Label) groupsLoader.getNamespace().get("sectionTitleText");
        VBox groupsListContainer = (VBox) groupsLoader.getNamespace().get("groupsListContainer");
        Button addButton = (Button) groupsLoader.getNamespace().get("addButton");

        if (sectionTitleText == null || groupsListContainer == null) {
            return null;
        }

        // Tag the add button with the section name so the handler knows what to create
        if (addButton != null) {
            addButton.setUserData(sectionTitle);
        }

        sectionTitleText.setText(sectionTitle);
        groupsListContainer.getChildren().clear();
        for (String item : items) {
            groupsListContainer.getChildren().add(createGroupRow(item, sectionTitle));
        }

        return sourceSection;
    }

    private static String styleToHex(String style) {
        if (style == null) return "#888888";
        switch (style.toLowerCase()) {
            case "style1": return "#6495ED";
            case "style2": return "#FF8C00";
            case "style3": return "#8B0000";
            case "style4": return "#6B8E23";
            case "style5": return "#800080";
            case "style6": return "#008080";
            case "style7": return "#C71585";
            default:       return "#888888";
        }
    }

    private HBox createCalendarRow(Calendar calendar, String sectionName) {
        // Colored circle matching the calendar's style
        Region colorDot = new Region();
        colorDot.setMinSize(12, 12);
        colorDot.setMaxSize(12, 12);
        colorDot.setStyle("-fx-background-color: " + styleToHex(calendar.getStyle()) + ";");
        HBox.setMargin(colorDot, new javafx.geometry.Insets(0, 6, 0, 0));

        Text nameText = new Text(calendar.getName());
        nameText.setStrokeWidth(0.0);

        // Keep box color in sync if the style ever changes
        calendar.styleProperty().addListener((obs, oldStyle, newStyle) ->
                colorDot.setStyle("-fx-background-color: " + styleToHex(newStyle) + ";"));

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
        actionButton.setCursor(Cursor.HAND);
        actionButton.setStyle("-fx-background-color: transparent;");
        actionButton.setOnAction(e -> openGroupModal(calendar, toSingular(sectionName), e));

        HBox row = new HBox(colorDot, nameText, spacer, actionButton);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }

    private HBox createGroupRow(String groupName, String sectionName) {
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
        actionButton.setCursor(Cursor.HAND);
        actionButton.setStyle("-fx-background-color: transparent;");
        // Open modal pre-filled with this group's name (edit mode = "props")
        actionButton.setOnAction(e -> openGroupModal(groupName, toSingular(sectionName), e));

        HBox row = new HBox(groupNameText, spacer, actionButton);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }

    @FXML
    private void handleOpenCreateGroupModal(ActionEvent event) {
        Button source = (Button) event.getSource();
        String sectionName = source.getUserData() != null ? source.getUserData().toString() : "Group";
        String singular = toSingular(sectionName);
        if ("Course".equalsIgnoreCase(singular)) {
            openGroupModal((Calendar) null, singular, event);
        } else {
            openGroupModal((String) null, singular, event);
        }
    }

    private String toSingular(String word) {
        if (word == null || word.isEmpty()) return word;
        if (word.endsWith("s") || word.endsWith("S")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    /** Called from createCalendarRow (courses) — has the actual Calendar object */
    private void openGroupModal(Calendar calendar, String sectionName, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/source-tray-course-modal/course-modal.fxml"));
            Parent root = loader.load();
            CreateCourseModalController modalController = loader.getController();
            modalController.setCalendar(calendar); // null = create mode, non-null = edit mode
            modalController.applyProps();
            showModal(root, calendar != null ? calendar.getName() : null, sectionName, event);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to open course modal", e);
        }
    }

    /** Called from createGroupRow (groups) — only has a name string */
    private void openGroupModal(String groupName, String sectionName, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/source-tray-create-modal/create-modal.fxml"));
            Parent root = loader.load();
            CreateGroupModalController modalController = loader.getController();
            if (groupName != null) modalController.setProps(groupName, List.of());
            modalController.setSectionName(sectionName);
            modalController.applyProps();
            showModal(root, groupName, sectionName, event);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to open group modal", e);
        }
    }

    private void showModal(Parent root, String groupName, String sectionName, ActionEvent event) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(groupName != null ? "Edit " + sectionName : "Create " + sectionName);
        dialogStage.setScene(new Scene(root));
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
        dialogStage.setResizable(false);
        dialogStage.showAndWait();
    }

    @FXML
    private void handleAlertsClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/notifications-popup/notifications-popup.fxml"));
            javafx.scene.Node content = loader.load();

            org.controlsfx.control.PopOver popOver = new org.controlsfx.control.PopOver(content);
            popOver.setArrowLocation(org.controlsfx.control.PopOver.ArrowLocation.TOP_RIGHT);
            popOver.setDetachable(false);
            popOver.setAnimated(false);
            popOver.setHeaderAlwaysVisible(false);
            popOver.setArrowSize(10);
            popOver.show((javafx.scene.Node) event.getSource());
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @FXML
    private void handleGoToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/register.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToLogin(ActionEvent event) {
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
