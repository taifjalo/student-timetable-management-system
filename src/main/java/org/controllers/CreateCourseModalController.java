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

/**
 * Controller for the course create/edit modal ({@code course-modal.fxml}).
 * Allows a teacher to create a new course or edit/delete an existing one.
 * Each course has a display name and a color that maps to a CalendarFX
 * {@link Style} for calendar rendering.
 */
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
    private final LocalizationService localizationService = new LocalizationService();

    private final CourseService courseService = new CourseService(new CourseDao());

    /**
     * Sets the {@link CalendarSource} so that newly created calendars can be
     * added to the source tray immediately after saving.
     *
     * @param calendarSource the source tray's calendar source
     */
    public void setCalendarSource(CalendarSource calendarSource) {
        this.calendarSource = calendarSource;
    }

    private static final Style[] STYLES = {
        Style.STYLE1, Style.STYLE2, Style.STYLE3, Style.STYLE4,
        Style.STYLE5, Style.STYLE6, Style.STYLE7
    };

    private static final String[] STYLE_COLORS = {
        "#77C04B", "#418FCB", "#F7D15B", "#9D5B9F",
        "#D0525F", "#F9844B", "#AE663E"
    };

    /**
     * Sets the modal to create mode with the given initial name.
     * Deprecated in favour of {@link #setCalendar(Calendar)} for edit mode;
     * kept for backwards-compatible call sites.
     *
     * @param courseName initial name value (currently unused)
     */
    public void setProps(String courseName) {
        this.isEditMode = false;
    }

    /**
     * Binds the modal to an existing calendar entry and switches to edit mode.
     * Extracts the DB course ID from the calendar's user object if available.
     *
     * @param calendar the CalendarFX calendar representing the course to edit,
     *                 or {@code null} to stay in create mode
     */
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

    /**
     * JavaFX initialize callback — builds the localized color picker menu.
     */
    @FXML
    public void initialize() {
        buildColorMenu();
    }

    /**
     * Rebuilds the color picker {@link MenuButton} items using localized color names.
     */
    private void buildColorMenu() {
        String[] styleNames = getStyleNames();
        colorPicker.getItems().clear();
        for (int i = 0; i < STYLES.length; i++) {
            final Style style = STYLES[i];
            final String color = STYLE_COLORS[i];
            final String name = styleNames[i];

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

    /**
     * Applies the chosen style to the color picker button graphic.
     *
     * @param style the CalendarFX style constant
     * @param color the hex color string for the dot
     * @param name  the localized color name label
     */
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

    /**
     * Configures the modal UI for the current mode (create or edit) using
     * the active locale's bundle. Must be called after the FXML is loaded.
     */
    public void applyProps() {
        ResourceBundle bundle = localizationService.getBundle();
        String[] styleNames = getStyleNames();
        buildColorMenu();

        if (isEditMode) {
            modalTitleLabel.setText(bundle.getString("modal.edit.course.title"));
            confirmButton.setText(bundle.getString("modal.course.save.button"));
            deleteButton.setVisible(true);
            if (calendar != null) {
                groupNameField.setText(calendar.getName());
                // Show the calendar's current color as the pre-selected option
                for (int i = 0; i < STYLES.length; i++) {
                    if (STYLES[i].name().equalsIgnoreCase(calendar.getStyle())) {
                        selectStyle(STYLES[i], STYLE_COLORS[i], styleNames[i]);
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
            selectStyle(STYLES[0], STYLE_COLORS[0], styleNames[0]);
        }
    }

    /**
     * Returns the localized color name array in the same order as {@link #STYLES}.
     *
     * @return array of seven localized color name strings
     */
    private String[] getStyleNames() {
        ResourceBundle bundle = localizationService.getBundle();
        return new String[] {
                bundle.getString("color.green"),
                bundle.getString("color.blue"),
                bundle.getString("color.yellow"),
                bundle.getString("color.purple"),
                bundle.getString("color.red"),
                bundle.getString("color.orange"),
                bundle.getString("color.brown")
        };
    }

    /**
     * FXML action handler for the confirm button.
     * Creates or updates the course in the database and updates the source tray calendar.
     */
    @FXML
    private void handleConfirm() {
        String name = groupNameField.getText();
        if (name == null || name.isBlank()) {
            return;
        }

        String colorCode = styleToHex(selectedStyle);

        try {
            if (isEditMode && dbCourseId != null) {
                courseService.updateCourse(dbCourseId, name, colorCode);
                Course refreshed = courseService.getCourseById(dbCourseId);
                if (calendar != null) {
                    calendar.setName(refreshed.getDisplayName());
                    calendar.setStyle(CourseService.colorCodeToStyle(refreshed.getColorCode()));
                    calendar.setUserObject(refreshed);
                }
            } else {
                Course saved = courseService.createCourse(name, colorCode);
                Course refreshed = courseService.getCourseById(saved.getId());
                Calendar newCal = courseService.toCalendar(refreshed);
                if (calendarSource != null) {
                    calendarSource.getCalendars().add(newCal);
                }
            }

            closeModal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * FXML action handler for the delete button.
     * Removes the course from the database and from the source tray.
     */
    @FXML
    private void handleDelete() {
        try {
            if (dbCourseId != null) {
                courseService.deleteCourse(dbCourseId);
                if (calendarSource != null && calendar != null) {
                    calendarSource.getCalendars().remove(calendar);
                }
            }
            closeModal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Closes the modal window. */
    private void closeModal() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Maps a CalendarFX {@link Style} to its corresponding hex color string.
     *
     * @param style the CalendarFX style constant
     * @return hex color string, or {@code "#888888"} for unknown styles
     */
    private String styleToHex(Style style) {
        if (style == null) {
            return "#888888";
        }
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
