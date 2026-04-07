package org.controllers;

import dto.ChatPreview;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.entities.User;

/**
 * Controller for a single user row inside the chat search results popup
 * ({@code chat-user-search-results.fxml}).
 * When the user clicks this row, the search popup closes and the selected
 * conversation is opened in the main chat view.
 */
public class ChatUserSearchResultController {

    private User user;
    private ChatController chatController;
    private ChatSearchingContainerController cSCController;

    @FXML
    private Label teacherName;

    /**
     * Binds a user to this row, displaying their full name.
     *
     * @param user the user to display
     */
    public void setUser(User user) {
        this.user = user;
        teacherName.setText(user.getFirstName() + " " + user.getSureName());
    }

    /**
     * Injects the parent chat controller used to open the conversation on selection.
     *
     * @param chatController the owning chat controller
     */
    public void setChatController(ChatController chatController) {
        this.chatController = chatController;
    }

    /**
     * Injects the container controller used to close the search popup on selection.
     *
     * @param cSCController the search container controller
     */
    public void setChatSearchingContainerController(ChatSearchingContainerController cSCController) {
        this.cSCController = cSCController;
    }

    /**
     * FXML action handler — closes the search popup, adds the user to the
     * chat previews list if not already present, and opens their conversation.
     */
    @FXML
    public void chooseUser() {
        cSCController.closeModal();
        ChatPreview chatPreview = new ChatPreview(user.getId(), user.getFirstName(), user.getSureName(), true);
        chatController.addChatPreview(chatPreview);
        chatController.chooseUserFromLeftSide(chatPreview);
    }
}
