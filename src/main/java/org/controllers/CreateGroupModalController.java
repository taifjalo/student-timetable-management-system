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
import org.dao.NotificationDao;
import org.dao.UserDao;
import org.entities.StudentGroup;
import org.entities.User;
import org.service.GroupService;
import org.service.LocalizationService;
import org.service.NotificationService;
import org.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the group create/edit modal ({@code group-modal.fxml}).
 * Displays two lists side-by-side: all students on the right and current group
 * members on the left. Users can click to move students between the lists.
 * Supports both create and edit modes.
 */
public class CreateGroupModalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateGroupModalController.class);
    private static final String UNEXPECTED_ERROR = "Unexpected error";

    @FXML private Text modalTitleLabel;
    @FXML private TextField groupNameField;
    @FXML private CustomTextField fieldSearch;
    @FXML private ListView<User> groupStudentListView;
    @FXML private ListView<User> studentListView;
    @FXML private Button deleteButton;
    @FXML private Button confirmButton;

    private String initialName = "";
    private boolean isEditMode = false;
    private StudentGroup existingGroup = null;
    private ObservableList<StudentGroup> groupsList = null;

    private final ResourceBundle bundle = new LocalizationService().getBundle();

    private final UserService userService = new UserService(new UserDao());
    private final GroupService groupService = new GroupService(
            new GroupDao(), new NotificationService(new NotificationDao()));

    /**
     * Injects the observable groups list so the source tray updates live when a
     * group is created or deleted.
     *
     * @param groupsList the live list backing the source tray groups section
     */
    public void setGroupsList(ObservableList<StudentGroup> groupsList) {
        this.groupsList = groupsList;
    }

    /**
     * Switches the modal to edit mode with the given pre-populated name.
     *
     * @param groupName the existing group name to pre-fill
     * @param students  unused legacy parameter
     */
    public void setProps(String groupName, List<String> students) {
        this.initialName = groupName != null ? groupName : "";
        this.isEditMode = true;
    }

    /**
     * Switches the modal to edit mode and binds it to an existing group.
     * The group's current members are loaded asynchronously in {@link #applyProps()}.
     *
     * @param group the {@link StudentGroup} to edit
     */
    public void setGroup(StudentGroup group) {
        this.existingGroup = group;
        this.initialName = group.getDisplayFieldOfStudies();
        this.isEditMode = true;
    }

    /**
     * JavaFX initialize callback — sets up list cell factories, click handlers,
     * the search listener, and starts an async load of all students.
     */
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
                Platform.runLater(() -> studentListView.getItems().setAll(all));
            } catch (Exception e) {
                LOGGER.error(UNEXPECTED_ERROR, e);
            }
        }, "load-students-thread").start();
    }

    /**
     * Filters the all-students list using an async name search.
     *
     * @param query the search string typed by the user
     */
    private void filterStudents(String query) {
        new Thread(() -> {
            try {
                List<User> results = userService.searchStudents(query);
                Platform.runLater(() -> studentListView.getItems().setAll(results));
            } catch (Exception e) {
                LOGGER.error(UNEXPECTED_ERROR, e);
            }
        }, "search-students-thread").start();
    }

    /**
     * Configures modal labels and buttons for create or edit mode, and
     * asynchronously pre-populates the group member list in edit mode.
     * Must be called after the FXML is loaded.
     */
    public void applyProps() {
        if (isEditMode) {
            modalTitleLabel.setText(bundle.getString("modal.edit.group.title"));
            confirmButton.setText(bundle.getString("modal.course.save.button"));
            deleteButton.setVisible(true);

            if (existingGroup != null) {
                new Thread(() -> {
                    try {
                        List<User> current = groupService.getStudentsInGroup(existingGroup.getGroupCode());
                        Platform.runLater(() -> groupStudentListView.getItems().setAll(current));
                    } catch (Exception e) {
                        LOGGER.error(UNEXPECTED_ERROR, e);
                    }
                }, "load-group-students-thread").start();
            }
        } else {
            modalTitleLabel.setText(bundle.getString("modal.create.group.title"));
            confirmButton.setText(bundle.getString("modal.group.add.button"));

            deleteButton.setVisible(false);
        }
        groupNameField.setText(initialName);
    }

    /**
     * FXML action handler for the confirm button.
     * Creates or updates the group with the current member list in a background thread.
     */
    @FXML
    private void handleConfirm() {
        String name = groupNameField.getText();
        if (name == null || name.isBlank()) {
            return;
        }

        List<User> students = new java.util.ArrayList<>(groupStudentListView.getItems());

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
                LOGGER.error(UNEXPECTED_ERROR, e);
            }
        }, "save-group-thread").start();
    }

    /**
     * FXML action handler for the delete button.
     * Deletes the group from the database in a background thread and removes it
     * from the observable groups list.
     */
    @FXML
    private void handleDelete() {
        if (existingGroup == null) {
            closeModal();
            return;
        }
        new Thread(() -> {
            try {
                groupService.deleteGroup(existingGroup.getGroupCode());
                if (groupsList != null) {
                    Platform.runLater(() -> groupsList.remove(existingGroup));
                }
                Platform.runLater(this::closeModal);
            } catch (Exception e) {
                LOGGER.error(UNEXPECTED_ERROR, e);
            }
        }, "delete-group-thread").start();
    }

    /**
     * FXML action handler for the cancel button — closes the modal without saving.
     */
    @FXML
    private void handleCancel() {
        closeModal();
    }

    /** Closes the modal window. */
    private void closeModal() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }
}
