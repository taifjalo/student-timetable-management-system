package org.controllers;

import dto.ChatPreview;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import org.dao.MessageDao;
import org.entities.Message;
import org.service.ChatService;

import java.util.ArrayList;
import java.util.List;


public class ChatController {

    private ObservableList<ChatPreview> messages = FXCollections.observableArrayList();
    private SortedList<ChatPreview> sortedMessages = new SortedList<>(messages);
    private ChatService chatService = new ChatService();


    private void startAutoUpdate() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000);

                    List newMessages = chatService.getChatPreview(5L);

                    Platform.runLater(() -> {
                        messages.setAll(newMessages);
                    });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



    @FXML
    private ImageView closeButton;

    @FXML ListView<ChatPreview> chatUsers;


    @FXML
    private void closeChat(){
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void initialize() {
        messages.setAll(chatService.getChatPreview(5L));
        chatUsers.setItems(messages);
        startAutoUpdate();
        chatUsers.setCellFactory(listView -> new ListCell<>() {

            @Override
            protected void updateItem(ChatPreview preview, boolean empty) {
                super.updateItem(preview, empty);

                if (empty || preview == null) {
                    setGraphic(null);
                } else {
                    try {

                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/chat-view/messages-person.fxml")
                        );

                        Parent root = loader.load();

                        setGraphic(root);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
