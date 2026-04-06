package org.controllers;

import dto.ChatPreview;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;


/**
 * Controller for a single row in the chat users list ({@code messages-person.fxml}).
 * Displays the other user's name and an unread indicator dot that is reactively
 * bound to the {@link ChatPreview#isReadProperty()}.
 */
public class ChatPreviewController {

    private ChatController chatController;
    private ChatPreview chatPreview;

    @FXML
    private Label teacherName;

    @FXML
    private ImageView isReadDot;

    /**
     * Sets the display name shown in this row.
     *
     * @param name    the other user's first name
     * @param surname the other user's surname
     */
    public void setTeacherName(String name, String surname){
        teacherName.setText(name + " " + surname);
    }

    /**
     * Injects the parent {@link ChatController} so this row can open the conversation
     * when clicked.
     *
     * @param chatController the owning chat controller
     */
    public void setChatController(ChatController chatController) {
         this.chatController = chatController;
    }

    /**
     * Stores the {@link ChatPreview} model associated with this row.
     *
     * @param chatPreview the preview model
     */
    public void setChatPreview(ChatPreview chatPreview){
        this.chatPreview=chatPreview;
    }

    /**
     * Binds the unread indicator dot's visibility to the inverse of the preview's
     * {@code isRead} property, so the dot appears automatically when new messages arrive.
     *
     * @param preview the preview whose read state drives the indicator
     */
    public void bindPreview(ChatPreview preview) {
        isReadDot.visibleProperty().unbind();

        isReadDot.visibleProperty().bind(
                preview.isReadProperty().not()
        );
    }


    /**
     * FXML action handler — opens the full chat conversation for this preview's user.
     */
    @FXML
    public void openChat(){
        chatController.openChat(chatPreview);
    }




}
