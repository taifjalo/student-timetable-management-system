package org.controllers;

import dto.ChatPreview;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import org.service.ChatService;


import java.util.List;



public class ChatController {

    private ObservableList<ChatPreview> chatPreviews = FXCollections.observableArrayList();
    private ChatService chatService = new ChatService();
    long user = 5L;

    private void startAutoUpdate() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000);

                    List newChatPreviews = chatService.getChatPreview(user);

                    Platform.runLater(() -> {
                        chatPreviews.setAll(newChatPreviews);
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
        chatPreviews.setAll(chatService.getChatPreview(5L));
        chatUsers.setItems(chatPreviews);
        startAutoUpdate();
        chatUsers.setCellFactory(listView -> new ListCell<>() {

            @Override
            protected void updateItem(ChatPreview preview, boolean empty) {
                super.updateItem(preview, empty);

                if (empty || preview == null) {
                    setGraphic(null);
                } else {
                    try {

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/chat-view/messages-person.fxml"));
                        Parent root = loader.load();
                        ChatPreviewController cpController = loader.getController();
                        cpController.setTeacherName(preview.getName(), preview.getSurname());
                        cpController.setIsRead(preview.getIsRead());

                        setPadding(Insets.EMPTY);
                        setGraphic(root);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
