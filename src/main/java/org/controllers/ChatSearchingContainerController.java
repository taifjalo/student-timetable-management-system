package org.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class ChatSearchingContainerController {

    @FXML
    private VBox chatSearchingContainer;

    public void addItems(List<HBox> items){
        for (HBox item: items){
            chatSearchingContainer.getChildren().add(item);
        }
    }
}
