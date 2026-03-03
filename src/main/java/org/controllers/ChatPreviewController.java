package org.controllers;

import dto.ChatPreview;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.dao.MessageDao;
import org.entities.Message;


public class ChatPreviewController {

    private ChatController chatController;
    private ChatPreview chatPreview;
    private Long userId;
    private MessageDao messageDao;
    private ObservableList<Message> messages = FXCollections.observableArrayList();

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

    public void setChatController(ChatController chatController) {
         this.chatController = chatController;
    }

    public void setChatPreview(ChatPreview chatPreview){
        this.chatPreview=chatPreview;
    }

    public void setUserId(Long id){
        this.userId = id;
    }

    @FXML
    public void openChat(){
        chatController.rightSideVisibility(true);
        chatController.setRightSideName(chatPreview.getName(), chatPreview.getSurname());
    }


}
