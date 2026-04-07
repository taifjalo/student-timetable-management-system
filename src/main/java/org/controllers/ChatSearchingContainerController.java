package org.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller for the chat user search results popup ({@code chat-searching-container.fxml}).
 * Acts as a container that holds one {@link ChatUserSearchResultController} row per
 * matching user found by the search.
 */
public class ChatSearchingContainerController {

    @FXML
    private VBox chatSearchingContainer;

    /**
     * Appends a list of pre-built search result rows to the container.
     *
     * @param items the HBox rows, one per matching user
     */
    public void addItems(List<HBox> items) {
        for (HBox item : items) {
            chatSearchingContainer.getChildren().add(item);
        }
    }

    /**
     * Closes the search results popup window.
     */
    public void closeModal() {
        Stage stage = (Stage) chatSearchingContainer.getScene().getWindow();
        stage.close();
    }

}
