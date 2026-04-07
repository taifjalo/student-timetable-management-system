package org.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for a single message bubble cell in the chat view.
 * Used by both {@code sent-message.fxml} and {@code received-message.fxml}.
 */
public class MessageController {

    DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    Label messageBubble;

    @FXML
    Label timeLabel;

    /**
     * Sets the text content displayed in the message bubble.
     *
     * @param text the message body to display
     */
    public void setText(String text) {
        messageBubble.setText(text);
    }

    /**
     * Formats the given date-time and displays it in the time label.
     *
     * @param time the timestamp the message was sent
     */
    public void setTime(LocalDateTime time) {
        String textTime = time.format(formatter);
        timeLabel.setText(textTime);
    }
}
