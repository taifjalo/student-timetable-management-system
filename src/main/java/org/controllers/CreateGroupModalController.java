package org.controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.CustomTextField;
import org.dao.GroupDao;
import org.dao.UserDao;
import org.entities.StudentGroup;
import org.entities.User;
import org.service.GroupService;
import org.service.UserService;

import java.util.List;

public class CreateGroupModalController {

    @FXML private Text modalTitleLabel;
    @FXML private TextField groupNameField;
    @FXML private CustomTextField fieldSearch;
    @FXML private ListView<User> groupStudentListView;
    @FXML private ListView<User> studentListView;
    @FXML private Button deleteButton;
    @FXML private Button confirmButton;

    private String initialName = "";
    private boolean isEditMode = false;
    private String sectionName = "Item";
    private StudentGroup existingGroup = null;
    private ObservableList<StudentGroup> groupsList = null;

    private final UserService userService = new UserService(new UserDao());
    private final GroupService groupService = new GroupService(new GroupDao());

    public void setGroupsList(ObservableList<StudentGroup> groupsList) {
        this.groupsList = groupsList;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName != null ? sectionName : "Item";
    }

    public void setProps(String groupName, List<String> students) {
        this.initialName = groupName != null ? groupName : "";
        this.isEditMode = true;
    }

    /** Called when opening in edit mode with an existing StudentGroup. */
    public void setGroup(StudentGroup group) {
        this.existingGroup = group;
        this.initialName = group.getFieldOfStudies();
        this.isEditMode = true;
    }

    @FXML
    public void initialize() {
        groupStudentListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getFirstName() + " " + user.getSureName());
            }
        });

        studentListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getFirstName() + " " + user.getSureName());
            }
        });

        // Click in all-students list → add to group (if not already there)
        studentListView.setOnMouseClicked(e -> {
            User selected = studentListView.getSelectionModel().getSelectedItem();
            if (selected != null && !groupStudentListView.getItems().contains(selected)) {
                groupStudentListView.getItems().add(selected);
            }
            studentListView.getSelectionModel().clearSelection();
        });

        // Click in group list → remove from group
        groupStudentListView.setOnMouseClicked(e -> {
            User selected = groupStudentListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                groupStudentListView.getItems().remove(selected);
            }
        });

        fieldSearch.textProperty().addListener((obs, oldVal, newVal) -> filterStudents(newVal));

        // Load all students from DB in background
        new Thread(() -> {
            try {
                List<User> all = userService.getAllStudents();
                System.out.println("Loaded " + all.size() + " students from DB");
                Platform.runLater(() -> studentListView.getItems().setAll(all));
            } catch (Exception e) {
                System.out.println("Failed to load students: " + e.getMessage());
                e.printStackTrace();
            }
        }, "load-students-thread").start();
    }

    private void filterStudents(String query) {
        new Thread(() -> {
            try {
                List<User> results = userService.searchStudents(query);
                Platform.runLater(() -> studentListView.getItems().setAll(results));
            } catch (Exception e) {
                System.out.println("Search failed: " + e.getMessage());
                e.printStackTrace();
            }
        }, "search-students-thread").start();
    }

    public void applyProps() {
        if (isEditMode) {
            modalTitleLabel.setText("Edit " + sectionName);
            confirmButton.setText("Save");
            deleteButton.setVisible(true);

            if (existingGroup != null) {
                new Thread(() -> {
                    try {
                        List<User> current = groupService.getStudentsInGroup(existingGroup.getGroupCode());
                        Platform.runLater(() -> groupStudentListView.getItems().setAll(current));
                    } catch (Exception e) {
                        System.out.println("Failed to load group students: " + e.getMessage());
                        e.printStackTrace();
                    }
                }, "load-group-students-thread").start();
            }
        } else {
            modalTitleLabel.setText("Create " + sectionName);
            confirmButton.setText("Add");
            deleteButton.setVisible(false);
        }
        groupNameField.setText(initialName);
    }

    @FXML
    private void handleConfirm() {
        String name = groupNameField.getText();
        if (name == null || name.isBlank()) {
            System.out.println("Validation failed: name is empty");
            return;
        }

        List<User> students = new java.util.ArrayList<>(groupStudentListView.getItems());
        System.out.println("Attempting to " + (isEditMode ? "update" : "create")
                + " group: " + name + " with " + students.size() + " students");

        new Thread(() -> {
            try {
                if (isEditMode && existingGroup != null) {
                    groupService.updateGroup(existingGroup.getGroupCode(), name, students);
                } else {
                    StudentGroup saved = groupService.createGroup(name, students);
                    if (groupsList != null) {
                        Platform.runLater(() -> groupsList.add(saved));
                    }
                }
                Platform.runLater(this::closeModal);
            } catch (Exception e) {
                System.out.println("Failed to save group: " + e.getMessage());
                e.printStackTrace();
            }
        }, "save-group-thread").start();
    }

    @FXML
    private void handleDelete() {
        if (existingGroup == null) { closeModal(); return; }
        System.out.println("Deleting group: " + existingGroup.getGroupCode());
        new Thread(() -> {
            try {
                groupService.deleteGroup(existingGroup.getGroupCode());
                if (groupsList != null) {
                    Platform.runLater(() -> groupsList.remove(existingGroup));
                }
                Platform.runLater(this::closeModal);
            } catch (Exception e) {
                System.out.println("Failed to delete group: " + e.getMessage());
                e.printStackTrace();
            }
        }, "delete-group-thread").start();
    }

    @FXML
    private void handleCancel() {
        closeModal();
    }

    private void closeModal() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }
}



