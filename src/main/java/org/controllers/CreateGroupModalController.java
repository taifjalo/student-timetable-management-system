package org.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class CreateGroupModalController {

    @FXML private Text modalTitleLabel;
    @FXML private TextField groupNameField;
    @FXML private ListView<String> studentListView;
    @FXML private Button deleteButton;
    @FXML private Button confirmButton;

    private String initialName = "";
    private List<String> initialStudents = List.of();
    private boolean isEditMode = false;
    private String sectionName = "Item";

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName != null ? sectionName : "Item";
    }

    public void setProps(String groupName, List<String> students) {
        this.initialName = groupName != null ? groupName : "";
        this.initialStudents = students != null ? students : List.of();
        this.isEditMode = true;
    }

    @FXML
    public void initialize() {}

    public void applyProps() {
        if (isEditMode) {
            modalTitleLabel.setText("Edit " + sectionName);
            confirmButton.setText("Save");
            deleteButton.setVisible(true);
        } else {
            modalTitleLabel.setText("Create " + sectionName);
            confirmButton.setText("Add");
            deleteButton.setVisible(false);
        }

        groupNameField.setText(initialName);

        studentListView.getItems().clear();
        studentListView.getItems().addAll(initialStudents);
    }

    @FXML
    private void handleConfirm() {
        String name = groupNameField.getText();
        List<String> students = studentListView.getItems();
        System.out.println((isEditMode ? "Saving" : "Creating") + " group: " + name + " with students: " + students);
        closeModal();
    }

    @FXML
    private void handleDelete() {
        System.out.println("Deleting group: " + groupNameField.getText());
        closeModal();
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



