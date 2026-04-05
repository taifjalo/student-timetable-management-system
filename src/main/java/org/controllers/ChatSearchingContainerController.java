package org.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class ChatSearchingContainerController {
    
    private ChatController chatController;
    
    @FXML
    private VBox chatSearchingContainer;

    public void addItems(List<HBox> items){
        for (HBox item: items){
            chatSearchingContainer.getChildren().add(item);
        }
    }

    public void closeModal(){
        Stage stage = (Stage) chatSearchingContainer.getScene().getWindow();
        stage.close();
    }

    public void setChatController(ChatController chatController) {
        this.chatController = chatController;
    }
}
