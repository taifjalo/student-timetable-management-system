package org.controllers;

import dto.ChatPreview;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.entities.User;

public class ChatUserSearchResultController {

    private User user;
    private ChatController chatController;
    private ChatSearchingContainerController cSCController;

    @FXML
    private Label teacherName;

    public void setUser(User user){
        this.user=user;
        teacherName.setText(user.getFirstName() + " " + user.getSureName());
    }

    public void setChatController(ChatController chatController){
        this.chatController = chatController;
    }

    public void setChatSearchingContainerController(ChatSearchingContainerController cSCController){
        this.cSCController = cSCController;
    }

    @FXML
    public void chooseUser(){
        cSCController.closeModal();
        ChatPreview chatPreview = new ChatPreview(user.getId(), user.getFirstName(), user.getSureName(), true);
        chatController.addChatPreview(chatPreview);
        chatController.chooseUserFromLeftSide(chatPreview);
    }
}
