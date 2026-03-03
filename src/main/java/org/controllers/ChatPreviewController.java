package org.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class ChatPreviewController {

    @FXML
    private Label teacherName;

    @FXML
    private ImageView isReadDot;

    public void setTeacherName(String name, String surname){
        teacherName.setText(name + " " + surname);
    }

    public void setIsRead(Boolean isRead){
        isReadDot.setVisible(isRead);
    }
}
