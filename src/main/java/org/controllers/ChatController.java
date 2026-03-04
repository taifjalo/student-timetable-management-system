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
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import org.dao.MessageDao;
import org.entities.Message;
import org.service.ChatService;


import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;



public class ChatController {

    @FXML
    private VBox chatRightSide;

    @FXML
    private Label rightSideName;

    @FXML
    private ImageView closeButton;

    @FXML
    private ListView<ChatPreview> chatUsers;

    @FXML
    private ListView<Message> chatMessages;

    @FXML
    private TextField messageTypeField;


    private ObservableList<ChatPreview> chatPreviews = FXCollections.observableArrayList();
    private MessageDao messageDao = new MessageDao();
    private ChatService chatService = new ChatService(messageDao);
    private ObservableList<Message> messages = FXCollections.observableArrayList();
    SortedList<Message> sortedMessages = new SortedList<>(messages, Comparator.naturalOrder());
    long userId = 5L;
    long otherId;
    private Thread updateChatThread;
    private Thread updatePreviewsThread;

    private void startPreviewsAutoUpdate() {
        updatePreviewsThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(100);

                    List newChatPreviews = chatService.getNewChatPreviews(chatPreviews, userId);

                    Platform.runLater(() -> {
                        chatPreviews.addAll(newChatPreviews);
                    });

                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        updatePreviewsThread.start();
    }

    private void startMessagesAutoUpdate(){
        updateChatThread = new Thread(()-> {
            while (true) {
                try {
                    Thread.sleep(100);

                    Long lastId = messages.get(messages.size() - 1).getId();

                    List newMessages = messageDao.findNewMessagesBetweenUsers(userId, otherId, lastId);

                    Platform.runLater(() -> {
                        messages.addAll(newMessages);
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        updateChatThread.start();
    }

    public void rightSideVisibility(Boolean is){
        chatRightSide.setVisible(is);
        chatRightSide.setManaged(is);
    }

    public void setRightSideName(String name, String surname){
        rightSideName.setText(name + " " + surname);
    }

    public void setOtherId (Long id){
        this.otherId = id;
    }

    public void loadMessages(){
        messages.setAll(messageDao.findMessagesBetweenUsers(userId, otherId));
        if (updateChatThread != null) {
            updateChatThread.interrupt();
        }
        startMessagesAutoUpdate();
    }


    @FXML
    public void initialize() {
        chatPreviews.setAll(chatService.getChatPreviews(5L));
        chatUsers.setItems(chatPreviews);
        chatMessages.setItems(sortedMessages);
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
        chatMessages.setCellFactory(listView ->  new ListCell<>() {
            @Override
            protected void updateItem(Message message, boolean empty){
                super.updateItem(message, empty);

                if (empty || message == null){
                    setGraphic(null);
                } else {
                    try {

                        FXMLLoader loader;
                        if (message.getSenderUser().getId().equals(userId)) {
                            loader = new FXMLLoader(getClass().getResource("/chat-view/sent-message.fxml"));
                        }
                        else {
                            loader = new FXMLLoader(getClass().getResource("/chat-view/received-message.fxml"));
                        }
                        Parent root = loader.load();
                        MessageController mController = loader.getController();
                        mController.setText(message.getContent());
                        mController.setTime(message.getSentAt());
                        setGraphic(root);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    @FXML
    private void closeChat(){
        Stage stage = (Stage) closeButton.getScene().getWindow();
        updateChatThread.interrupt();
        updatePreviewsThread.interrupt();
        stage.close();
    }

    @FXML
    private void sendMessage(){
        messageDao.saveMessage(userId, otherId, messageTypeField.getText());
    }





}
