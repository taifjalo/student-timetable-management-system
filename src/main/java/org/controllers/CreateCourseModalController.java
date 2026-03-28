package org.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.dao.CourseDao;
import org.entities.Course;
import org.service.CourseService;
import org.service.LocalizationService;

import java.util.ResourceBundle;

public class CreateCourseModalController {

    @FXML private Text modalTitleLabel;
    @FXML private TextField groupNameField;
    @FXML private MenuButton colorPicker;
    @FXML private Button deleteButton;
    @FXML private Button confirmButton;

    private Calendar calendar;
    private CalendarSource calendarSource;
    private boolean isEditMode = false;
    private Style selectedStyle = Style.STYLE1;
    private Long dbCourseId = null;
    private static ResourceBundle bundle = new LocalizationService().getBundle();

    private final CourseService courseService = new CourseService(new CourseDao());

    public void setCalendarSource(CalendarSource calendarSource) {
        this.calendarSource = calendarSource;
    }

    private static final Style[] STYLES = {
        Style.STYLE1, Style.STYLE2, Style.STYLE3, Style.STYLE4,
        Style.STYLE5, Style.STYLE6, Style.STYLE7
    };

    private static final String[] STYLE_NAMES = {
        bundle.getString("color.green"), bundle.getString("color.blue"), bundle.getString("color.yellow"), bundle.getString("color.purple"),
            bundle.getString("color.red"), bundle.getString("color.orange"), bundle.getString("color.brown")
    };

    private static final String[] STYLE_COLORS = {
        "#77C04B", "#418FCB", "#F7D15B", "#9D5B9F",
        "#D0525F", "#F9844B", "#AE663E"
    };

    public void setProps(String courseName) {
        this.isEditMode = false;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
        this.isEditMode = calendar != null;
        if (calendar != null) {
            // Store db course id if attached via user data
            if (calendar.getUserObject() instanceof Course course) {
                this.dbCourseId = course.getId();
            }
            for (Style s : STYLES) {
                if (s.name().equalsIgnoreCase(calendar.getStyle())) {
                    selectedStyle = s;
                    break;
                }
            }
        }
    }

    @FXML
    public void initialize() {
        buildColorMenu();
    }

    private void buildColorMenu() {
        colorPicker.getItems().clear();
        for (int i = 0; i < STYLES.length; i++) {
            final Style style = STYLES[i];
            final String color = STYLE_COLORS[i];
            final String name = STYLE_NAMES[i];

            // Colored dot
            Region dot = new Region();
            dot.setMinSize(12, 12);
            dot.setMaxSize(12, 12);
            dot.setStyle("-fx-background-color: " + color + ";");

            Text label = new Text(name);

            HBox graphic = new HBox(8, dot, label);
            graphic.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            MenuItem item = new MenuItem();
            item.setGraphic(graphic);
            item.setOnAction(e -> selectStyle(style, color, name));
            colorPicker.getItems().add(item);
        }
    }

    private void selectStyle(Style style, String color, String name) {
        selectedStyle = style;
        Region dot = new Region();
        dot.setMinSize(12, 12);
        dot.setMaxSize(12, 12);
        dot.setStyle("-fx-background-color: " + color + ";");
        Text label = new Text(name);
        HBox graphic = new HBox(8, dot, label);
        graphic.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        colorPicker.setGraphic(graphic);
        colorPicker.setText("");
    }

    public void applyProps() {
        if (isEditMode) {
            modalTitleLabel.setText(bundle.getString("modal.edit.course.title"));
            confirmButton.setText(bundle.getString("modal.course.save.button"));
            deleteButton.setVisible(true);
            if (calendar != null) {
                groupNameField.setText(calendar.getName());
                // Show the calendar's current color as the pre-selected option
                for (int i = 0; i < STYLES.length; i++) {
                    if (STYLES[i].name().equalsIgnoreCase(calendar.getStyle())) {
                        selectStyle(STYLES[i], STYLE_COLORS[i], STYLE_NAMES[i]);
                        break;
                    }
                }
            }
        } else {
            modalTitleLabel.setText(bundle.getString("modal.create.course.title"));
            confirmButton.setText(bundle.getString("modal.group.add.button"));
            deleteButton.setVisible(false);
            groupNameField.setText("");
            // Default to first color
            selectStyle(STYLES[0], STYLE_COLORS[0], STYLE_NAMES[0]);
        }
    }

    @FXML
    private void handleConfirm() {
        String name = groupNameField.getText();
        if (name == null || name.isBlank()) {
            System.out.println("Validation failed: name is empty");
            return;
        }

        String colorCode = styleToHex(selectedStyle);
        System.out.println("Attempting to " + (isEditMode ? "update" : "create") + " course:");
        System.out.println("  name       = " + name);
        System.out.println("  colorCode  = " + colorCode);
        System.out.println("  isEditMode = " + isEditMode);
        System.out.println("  dbCourseId = " + dbCourseId);

        try {
            if (isEditMode && dbCourseId != null) {
                // Save to DB
                courseService.updateCourse(dbCourseId, name, colorCode);
                // Re-fetch from DB so the UI reflects exactly what is stored
                Course refreshed = courseService.getCourseById(dbCourseId);
                System.out.println("Course updated and re-fetched from DB. id=" + refreshed.getId()
                        + " name=" + refreshed.getName() + " color=" + refreshed.getColorCode());
                if (calendar != null) {
                    calendar.setName(refreshed.getName());
                    calendar.setStyle(CourseService.colorCodeToStyle(refreshed.getColorCode()));
                    calendar.setUserObject(refreshed);
                }
            } else {
                Course saved = courseService.createCourse(name, colorCode);
                // Re-fetch from DB to get the final persisted state
                Course refreshed = courseService.getCourseById(saved.getId());
                System.out.println("Course created and re-fetched from DB. id=" + refreshed.getId()
                        + " name=" + refreshed.getName() + " color=" + refreshed.getColorCode());
                Calendar newCal = courseService.toCalendar(refreshed);
                if (calendarSource != null) {
                    calendarSource.getCalendars().add(newCal);
                    System.out.println("Calendar added to source tray");
                } else {
                    System.out.println("calendarSource not set, tray won't update");
                }
            }

            closeModal();
        } catch (Exception e) {
            System.out.println("DB operation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        System.out.println("Attempting to delete course. dbCourseId=" + dbCourseId);
        try {
            if (dbCourseId != null) {
                courseService.deleteCourse(dbCourseId);
                System.out.println("Course deleted successfully. id=" + dbCourseId);
                // Remove the calendar row from the source tray
                if (calendarSource != null && calendar != null) {
                    calendarSource.getCalendars().remove(calendar);
                    System.out.println("Calendar removed from source tray");
                }
            } else {
                System.out.println("No dbCourseId set, skipping DB delete");
            }
            closeModal();
        } catch (Exception e) {
            System.out.println("Delete failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void closeModal() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }

    private String styleToHex(Style style) {
        if (style == null) return "#888888";
        return switch (style) {
            case STYLE1 -> "#77C04B"; // green
            case STYLE2 -> "#418FCB"; // blue
            case STYLE3 -> "#F7D15B"; // yellow
            case STYLE4 -> "#9D5B9F"; // purple
            case STYLE5 -> "#D0525F"; // red
            case STYLE6 -> "#F9844B"; // orange
            case STYLE7 -> "#AE663E"; // brown
            default     -> "#888888";
        };
    }
}
