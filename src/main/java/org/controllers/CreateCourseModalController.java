package org.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CreateCourseModalController {

    @FXML private Text modalTitleLabel;
    @FXML private TextField groupNameField;
    @FXML private MenuButton colorPicker;
    @FXML private Button deleteButton;
    @FXML private Button confirmButton;

    private Calendar calendar;
    private boolean isEditMode = false;
    private Style selectedStyle = Style.STYLE1;

    private static final Style[] STYLES = {
        Style.STYLE1, Style.STYLE2, Style.STYLE3, Style.STYLE4,
        Style.STYLE5, Style.STYLE6, Style.STYLE7
    };

    private static final String[] STYLE_NAMES = {
        "Blue", "Orange", "Dark Red", "Green",
        "Purple", "Teal", "Pink"
    };

    private static final String[] STYLE_COLORS = {
        "#6495ED", "#FF8C00", "#8B0000", "#6B8E23",
        "#800080", "#008080", "#C71585"
    };

    public void setProps(String courseName) {
        this.isEditMode = false;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
        this.isEditMode = calendar != null;
        if (calendar != null) {
            // Pre-select the calendar's current style
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
            modalTitleLabel.setText("Edit Course");
            confirmButton.setText("Save");
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
            modalTitleLabel.setText("Create Course");
            confirmButton.setText("Add");
            deleteButton.setVisible(false);
            groupNameField.setText("");
            // Default to first color
            selectStyle(STYLES[0], STYLE_COLORS[0], STYLE_NAMES[0]);
        }
    }

    @FXML
    private void handleConfirm() {
        String name = groupNameField.getText();
        if (calendar != null) {
            calendar.setName(name);
            calendar.setStyle(selectedStyle);
        }
        System.out.println((isEditMode ? "Saving" : "Creating") + " course: " + name + " color: " + selectedStyle);
        closeModal();
    }

    @FXML
    private void handleDelete() {
        System.out.println("Deleting course: " + groupNameField.getText());
        closeModal();
    }

    private void closeModal() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }
}
