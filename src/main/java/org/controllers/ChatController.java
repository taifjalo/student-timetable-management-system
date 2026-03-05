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
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.stage.StageStyle;
import org.dao.MessageDao;
import org.dao.UserDao;
import org.entities.Message;
import org.entities.User;
import org.service.ChatService;


import java.util.ArrayList;
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

    @FXML
    private TextField chatSearchField;



    private ObservableList<ChatPreview> chatPreviews = FXCollections.observableArrayList();
    private MessageDao messageDao = new MessageDao();
    private UserDao userDao = new UserDao();
    private ChatService chatService = new ChatService(messageDao);
    private ObservableList<Message> messages = FXCollections.observableArrayList();
    SortedList<Message> sortedMessages = new SortedList<>(messages, Comparator.naturalOrder());
    long userId = 86L;
    long otherId;
    private Thread updateChatThread;
    private Thread updatePreviewsThread;

    public void startPreviewsAutoUpdate() {
        updatePreviewsThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10);

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
                    List newMessages;
                    Thread.sleep(10);
                    if (!messages.isEmpty()) {
                        Long lastId = messages.get(messages.size() - 1).getId();
                        newMessages = messageDao.findNewMessagesBetweenUsers(userId, otherId, lastId);
                    }
                    else {
                        newMessages = messageDao.findMessagesBetweenUsers(userId, otherId);
                    }
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

    public void addChatPreview(ChatPreview chatPreview){
        chatPreviews.add(chatPreview);
    }

    public void stopAllThreads(){
        if (updateChatThread!=null) {
            updateChatThread.interrupt();
        }
        updatePreviewsThread.interrupt();
    }



    @FXML
    public void initialize() {
        chatPreviews.setAll(chatService.getChatPreviews(userId));
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
        if (updateChatThread!=null) {
            updateChatThread.interrupt();
        }
        updatePreviewsThread.interrupt();
        stage.close();
    }

    @FXML
    private void sendMessage(){
        System.out.println("Sender: " + userId);
        System.out.println("Recipient: " + otherId);
        messageDao.saveMessage(userId, otherId, messageTypeField.getText());
    }

    @FXML
    private void searchChatUsers(){
        String text = chatSearchField.getText();
        String[] words = text.split("\\s+");
        List<User> users = userDao.findUserByNameSurname(words[0],words[1]);
        if (!users.isEmpty()) {
            try {
                FXMLLoader containerLoader = new FXMLLoader(getClass().getResource("/chat-view/chat-searching-container.fxml"));
                Parent root = containerLoader.load();
                ChatSearchingContainerController chatSearchingContainerController = containerLoader.getController();
                chatSearchingContainerController.setChatController(this);
                List<HBox> userItems = new ArrayList<>();
                for (User user: users){
                    FXMLLoader userLoader = new FXMLLoader(getClass().getResource("/chat-view/chat-user-search-results.fxml"));
                    HBox child = userLoader.load();
                    ChatUserSearchResultController cUSRController =userLoader.getController();
                    cUSRController.setUser(user);
                    cUSRController.setChatController(this);
                    cUSRController.setChatSearchingContainerController(chatSearchingContainerController);
                    userItems.add(child);
                }
                chatSearchingContainerController.addItems(userItems);
                Stage modalStage = new Stage();
                modalStage.setScene(new Scene(root));
                modalStage.initStyle(StageStyle.UNDECORATED);
                modalStage.initModality(Modality.APPLICATION_MODAL);
                modalStage.initOwner(closeButton.getScene().getWindow());
                modalStage.showAndWait();
            } catch (Exception e){
                e.printStackTrace();
            }
        }


    }







}
