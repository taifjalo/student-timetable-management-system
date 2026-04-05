package org.controllers;

import com.calendarfx.model.Entry;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import org.dao.GroupDao;
import org.dao.UserDao;
import org.entities.Lesson;
import org.entities.StudentGroup;
import org.entities.User;
import org.service.GroupService;
import org.service.LocalizationService;
import org.service.SessionManager;
import org.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class EventExtraDetailsController {

    private final TextField classroomField;
    private final Label teacherLabel;
    private final HBox groupRow;
    private final FlowPane studentsRow;

    private final List<StudentGroup> availableGroups = new ArrayList<>();
    private final List<StudentGroup> selectedGroups = new ArrayList<>();
    private final List<User> selectedUsers = new ArrayList<>();

    private final GroupService groupService = new GroupService(new GroupDao());
    private final UserService userService = new UserService(new UserDao());

    private final LocalizationService localizationService = new LocalizationService();
    ResourceBundle selectedBundle = localizationService.getBundle();

    public EventExtraDetailsController(Entry<?> entry, Lesson existingLesson) {
        classroomField = new TextField();
        classroomField.setPromptText("e.g. A201");
        if (existingLesson != null && existingLesson.getClassroom() != null) {
            classroomField.setText(existingLesson.getClassroom());
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        String teacherName = currentUser != null
                ? currentUser.getFirstName() + " " + currentUser.getSureName()
                : "-";
        teacherLabel = new Label(teacherName);

        groupRow = new HBox(6);
        groupRow.setAlignment(Pos.CENTER_LEFT);

        Button addGroupBtn = new Button(selectedBundle.getString("event.add.group.button"));
        addGroupBtn.setStyle("-fx-cursor: hand;");
        addGroupBtn.setOnAction(e -> showGroupPicker(addGroupBtn));
        groupRow.getChildren().add(addGroupBtn);

        if (existingLesson != null) {
            for (StudentGroup existingGroup : existingLesson.getAssignedGroups()) {
                addGroupChip(existingGroup, addGroupBtn);
            }
        }

        new Thread(() -> {
            try {
                List<StudentGroup> groups = groupService.getAllGroups();
                Platform.runLater(() -> availableGroups.addAll(groups));
            } catch (Exception ex) {
                System.err.println("Failed to load groups: " + ex.getMessage());
            }
        }, "load-groups-for-lesson").start();

        studentsRow = new FlowPane(6, 6);
        studentsRow.setAlignment(Pos.CENTER_LEFT);

        TextField userSearchField = new TextField();
        userSearchField.setPromptText(selectedBundle.getString("event.search.users.prompt"));
        userSearchField.setPrefWidth(180);

        ContextMenu userSuggestions = new ContextMenu();
        userSearchField.textProperty().addListener((obs, oldValue, newValue) -> {
            userSuggestions.hide();
            String query = newValue == null ? "" : newValue.trim();
            if (query.length() < 2) {
                return;
            }

            new Thread(() -> {
                try {
                    List<User> matches = userService.searchStudents(query);
                    Platform.runLater(() -> {
                        userSuggestions.getItems().clear();

                        for (User match : matches) {
                            if (selectedUsers.stream().anyMatch(u -> Objects.equals(u.getId(), match.getId()))) {
                                continue;
                            }
                            String fullName = match.getFirstName() + " " + match.getSureName();
                            MenuItem item = new MenuItem(fullName);
                            item.setOnAction(e -> {
                                addUserChip(match, userSearchField);
                                userSearchField.clear();
                                userSuggestions.hide();
                            });
                            userSuggestions.getItems().add(item);
                        }

                        if (!userSuggestions.getItems().isEmpty() && userSearchField.isFocused()) {
                            userSuggestions.show(userSearchField, javafx.geometry.Side.BOTTOM, 0, 0);
                        }
                    });
                } catch (Exception ex) {
                    System.err.println("Failed to search users: " + ex.getMessage());
                }
            }, "search-users-for-lesson").start();
        });

        userSearchField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                Platform.runLater(userSuggestions::hide);
            }
        });

        studentsRow.getChildren().add(userSearchField);

        if (existingLesson != null) {
            for (User existingUser : existingLesson.getAssignedUsers()) {
                addUserChip(existingUser, userSearchField);
            }
        }
    }

    public Node getClassIdNode() { return classroomField; }
    public Node getTeacherNode() { return teacherLabel; }
    public Node getGroupRowNode() { return groupRow; }
    public Node getStudentsNode() { return studentsRow; }

    public String getClassroom() { return classroomField.getText().trim(); }
    public List<StudentGroup> getSelectedGroups() { return new ArrayList<>(selectedGroups); }
    public List<User> getSelectedUsers() { return new ArrayList<>(selectedUsers); }

    public void setReadOnly(boolean readOnly) {
        if (!readOnly) return;

        String classroomText = classroomField.getText().trim();
        Label classroomLabel = new Label(classroomText.isEmpty() ? "—" : classroomText);
        classroomLabel.setStyle("-fx-padding: 2 0 2 0;");
        // Replace the TextField in its parent with the label
        if (classroomField.getParent() instanceof javafx.scene.layout.GridPane gp) {
            int col = javafx.scene.layout.GridPane.getColumnIndex(classroomField) == null
                    ? 0 : javafx.scene.layout.GridPane.getColumnIndex(classroomField);
            int row = javafx.scene.layout.GridPane.getRowIndex(classroomField) == null
                    ? 0 : javafx.scene.layout.GridPane.getRowIndex(classroomField);
            gp.getChildren().remove(classroomField);
            javafx.scene.layout.GridPane.setColumnIndex(classroomLabel, col);
            javafx.scene.layout.GridPane.setRowIndex(classroomLabel, row);
            gp.getChildren().add(classroomLabel);
        }

        List<Node> toReplace = new ArrayList<>(groupRow.getChildren());
        groupRow.getChildren().clear();
        for (Node n : toReplace) {
            if (n instanceof Button btn) {
                String text = btn.getText();
                // Skip the "Add group" button entirely
                if (text != null && text.startsWith("+")) continue;
                // Convert group chips to plain styled labels
                Label groupLabel = new Label(text);
                groupLabel.setStyle(
                        "-fx-background-radius: 12; -fx-border-radius: 12;" +
                        "-fx-border-color: #aaa; -fx-background-color: #e8f4fd;" +
                        "-fx-padding: 2 8 2 8;");
                groupRow.getChildren().add(groupLabel);
            }
        }
        // If no groups assigned, show a dash
        if (groupRow.getChildren().isEmpty()) {
            groupRow.getChildren().add(new Label("—"));
        }

        List<Node> studentNodes = new ArrayList<>(studentsRow.getChildren());
        studentsRow.getChildren().clear();
        for (Node n : studentNodes) {
            if (n instanceof TextField) continue; // remove search field
            if (n instanceof Button btn) {
                Label userLabel = new Label(btn.getText());
                userLabel.setStyle(
                        "-fx-background-radius: 12; -fx-border-radius: 12;" +
                        "-fx-border-color: #aaa; -fx-background-color: #f0f0f0;" +
                        "-fx-padding: 2 8 2 8;");
                studentsRow.getChildren().add(userLabel);
            }
        }
        if (studentsRow.getChildren().isEmpty()) {
            studentsRow.getChildren().add(new Label("—"));
        }
    }

    private void showGroupPicker(Button addGroupBtn) {
        if (availableGroups.isEmpty()) {
            System.out.println("No groups available yet.");
            return;
        }
        ContextMenu menu = new ContextMenu();
        for (StudentGroup g : availableGroups) {
            if (selectedGroups.stream().anyMatch(s -> s.getGroupCode().equals(g.getGroupCode()))) continue;
            MenuItem item = new MenuItem(g.getFieldOfStudies() + " (" + g.getGroupCode() + ")");
            item.setOnAction(e -> addGroupChip(g, addGroupBtn));
            menu.getItems().add(item);
        }
        if (menu.getItems().isEmpty()) {
            MenuItem none = new MenuItem("All groups already added");
            none.setDisable(true);
            menu.getItems().add(none);
        }
        menu.show(addGroupBtn, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void addGroupChip(StudentGroup group, Button addGroupBtn) {
        if (selectedGroups.stream().anyMatch(g -> g.getGroupCode().equals(group.getGroupCode()))) {
            return;
        }
        selectedGroups.add(group);
        Button chip = new Button(group.getFieldOfStudies());
        chip.setStyle(
                "-fx-background-radius: 12; -fx-border-radius: 12;" +
                "-fx-border-color: #aaa; -fx-background-color: #e8f4fd;" +
                "-fx-cursor: hand; -fx-padding: 2 8 2 8;");
        chip.setOnAction(e -> {
            selectedGroups.removeIf(g -> Objects.equals(g.getGroupCode(), group.getGroupCode()));
            groupRow.getChildren().remove(chip);
        });
        int idx = groupRow.getChildren().indexOf(addGroupBtn);
        groupRow.getChildren().add(idx, chip);
    }

    private void addUserChip(User user, TextField userSearchField) {
        if (selectedUsers.stream().anyMatch(u -> Objects.equals(u.getId(), user.getId()))) {
            return;
        }

        selectedUsers.add(user);

        ImageView xIcon = null;
        try {
            Image img = new Image(
                    Objects.requireNonNull(getClass().getResource("/images/exit.png")).toExternalForm(),
                    14, 14, true, true);
            xIcon = new ImageView(img);
        } catch (Exception ignored) {}

        String fullName = user.getFirstName() + " " + user.getSureName();
        Button chip = new Button(fullName);
        if (xIcon != null) {
            chip.setGraphic(xIcon);
            chip.setContentDisplay(ContentDisplay.RIGHT);
        } else {
            chip.setText(fullName + "  ×");
        }
        chip.setStyle(
                "-fx-background-radius: 12; -fx-border-radius: 12;" +
                "-fx-border-color: #aaa; -fx-background-color: #f0f0f0;" +
                "-fx-cursor: hand; -fx-padding: 2 8 2 8;");
        chip.setOnAction(e -> {
            selectedUsers.removeIf(u -> Objects.equals(u.getId(), user.getId()));
            studentsRow.getChildren().remove(chip);
        });

        int idx = studentsRow.getChildren().indexOf(userSearchField);
        if (idx >= 0) {
            studentsRow.getChildren().add(idx, chip);
        } else {
            studentsRow.getChildren().add(chip);
        }
    }
}
