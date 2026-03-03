package org.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageController {

    DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    Label messageBubble;

    @FXML
    Label timeLabel;

    public void setText(String text){
        messageBubble.setText(text);
    }

    public void setTime (LocalDateTime time){
        String textTime = time.format(formatter);
        timeLabel.setText(textTime);
    }
}
