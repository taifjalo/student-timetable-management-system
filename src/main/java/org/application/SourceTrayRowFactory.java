package org.application;

import com.calendarfx.model.Calendar;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import org.entities.StudentGroup;

/**
 * Builds source-tray row nodes for course and group items.
 */
public class SourceTrayRowFactory {

    public HBox createCalendarRow(Calendar calendar, boolean canEdit, EventHandler<ActionEvent> onEdit) {
        Region colorDot = new Region();
        colorDot.setMinSize(12, 12);
        colorDot.setMaxSize(12, 12);
        colorDot.setStyle("-fx-background-color: " + styleToHex(calendar.getStyle()) + ";");
        HBox.setMargin(colorDot, new javafx.geometry.Insets(0, 6, 0, 0));

        Text nameText = new Text(calendar.getName());
        nameText.setStrokeWidth(0.0);
        calendar.nameProperty().addListener((obs, oldName, newName) -> nameText.setText(newName));

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
        actionButton.setOnAction(onEdit);
        actionButton.setVisible(canEdit);
        actionButton.setManaged(canEdit);

        HBox row = new HBox(colorDot, nameText, spacer, actionButton);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }

    public HBox createGroupRow(StudentGroup group, boolean canEdit, EventHandler<ActionEvent> onEdit) {
        Text groupNameText = new Text(group.getDisplayFieldOfStudies());
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
        actionButton.setOnAction(onEdit);
        actionButton.setVisible(canEdit);
        actionButton.setManaged(canEdit);

        HBox row = new HBox(groupNameText, spacer, actionButton);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }

    private static String styleToHex(String style) {
        if (style == null) return "#888888";
        switch (style.toLowerCase()) {
            case "style1": return "#77C04B";
            case "style2": return "#418FCB";
            case "style3": return "#F7D15B";
            case "style4": return "#9D5B9F";
            case "style5": return "#D0525F";
            case "style6": return "#F9844B";
            case "style7": return "#AE663E";
            default:       return "#888888";
        }
    }
}
