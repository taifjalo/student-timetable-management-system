package org.controllers;

import dto.ChatPreview;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import org.dao.MessageDao;
import org.entities.Message;
import org.service.ChatService;


import java.util.List;



public class ChatController {

    private ObservableList<ChatPreview> chatPreviews = FXCollections.observableArrayList();
    private ChatService chatService = new ChatService();
    private ObservableList<Message> messages = FXCollections.observableArrayList();
    long userId = 5L;
    private MessageDao messageDao;
    private Thread updateChatThread;

    private void startPreviewsAutoUpdate() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000);

                    List newChatPreviews = chatService.getChatPreviews(userId);

                    Platform.runLater(() -> {
                        chatPreviews.setAll(newChatPreviews);
                    });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startChatAutoUpdate(Long id){
        updateChatThread = new Thread(()-> {
            while (true) {
                try {
                    Thread.sleep(3000);

                    List newMessages = messageDao.findMessagesBetweenUsers(userId, id);

                    Platform.runLater(() -> {
                        messages.setAll(newMessages);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @FXML
    private VBox chatRightSide;

    @FXML
    private Label rightSideName;

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
        chatPreviews.setAll(chatService.getChatPreviews(5L));
        chatUsers.setItems(chatPreviews);
        startPreviewsAutoUpdate();
        rightSideVisibility(false);
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
                        cpController.setChatController(ChatController.this);
                        cpController.setChatPreview(preview);
                        cpController.setUserId(userId);
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



    public void rightSideVisibility(Boolean is){
        chatRightSide.setVisible(is);
        chatRightSide.setManaged(is);
    }

    public void setRightSideName(String name, String surname){
        rightSideName.setText(name + " " + surname);
    }
}
