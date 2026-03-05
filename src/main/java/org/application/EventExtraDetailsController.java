package org.application;

import com.calendarfx.model.Entry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.Node;

import java.util.Objects;

public class EventExtraDetailsController {

    private static final String PLACEHOLDER_CLASS_ID  = "8D932";
    private static final String PLACEHOLDER_TEACHER   = "Amir Dirin";

    private final Label    classIdLabel;
    private final Label    teacherLabel;
    private final HBox     groupRow;
    private final FlowPane studentsRow;

    private int groupCounter = 1;

    public EventExtraDetailsController(Entry<?> entry) {

        classIdLabel = new Label(PLACEHOLDER_CLASS_ID);

        teacherLabel = new Label(PLACEHOLDER_TEACHER);

        groupRow = new HBox(6);
        groupRow.setAlignment(Pos.CENTER_LEFT);

        groupRow.getChildren().add(makeGroupMenuButton("Ryhmä " + groupCounter++));

        Button addGroupBtn = new Button("+");
        addGroupBtn.setStyle("-fx-cursor: hand;");
        addGroupBtn.setOnAction(e -> {
            int plusIndex = groupRow.getChildren().indexOf(addGroupBtn);
            groupRow.getChildren().add(plusIndex, makeGroupMenuButton("Ryhmä " + groupCounter++));
        });
        groupRow.getChildren().add(addGroupBtn);

        studentsRow = new FlowPane(6, 6);
        studentsRow.setAlignment(Pos.CENTER_LEFT);

        TextField nameInput = new TextField();
        nameInput.setPromptText("Opiskelijan nimi…");
        nameInput.setPrefWidth(140);

        Button addStudentBtn = new Button("+");
        addStudentBtn.setStyle("-fx-cursor: hand;");

        Runnable addStudent = () -> {
            String name = nameInput.getText().trim();
            if (name.isEmpty()) return;
            studentsRow.getChildren().add(makeStudentChip(name));
            nameInput.clear();
        };

        addStudentBtn.setOnAction(e -> addStudent.run());
        nameInput.setOnAction(e -> addStudent.run());

        HBox inputRow = new HBox(6, nameInput, addStudentBtn);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        studentsRow.getChildren().add(inputRow);
    }

    public Node getClassIdNode()   { return classIdLabel; }
    public Node getTeacherNode()   { return teacherLabel; }
    public Node getGroupRowNode()  { return groupRow; }
    public Node getStudentsNode()  { return studentsRow; }

    private MenuButton makeGroupMenuButton(String name) {
        MenuButton mb = new MenuButton(name);
        mb.setStyle("-fx-cursor: hand;");
        return mb;
    }

    private Button makeStudentChip(String name) {
        ImageView xIcon = null;
        try {
            Image img = new Image(
                    Objects.requireNonNull(
                            getClass().getResource("/images/exit.png")).toExternalForm(),
                    14, 14, true, true);
            xIcon = new ImageView(img);
        } catch (Exception ignored) {}

        Button chip = new Button(name);
        if (xIcon != null) {
            chip.setGraphic(xIcon);
            chip.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
        } else {
            chip.setText(name + "  ×");
        }
        chip.setStyle(
                "-fx-background-radius: 12;" +
                "-fx-border-radius: 12;" +
                "-fx-border-color: #aaa;" +
                "-fx-background-color: #f0f0f0;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 2 8 2 8;");
        chip.setOnAction(e -> studentsRow.getChildren().remove(chip));
        return chip;
    }
}
