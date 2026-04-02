package org.controllers;

import dto.ChatPreview;
import javafx.application.Platform;
import javafx.beans.Observable;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.stage.StageStyle;
import org.dao.MessageDao;
import org.dao.NotificationDao;
import org.dao.UserDao;
import org.entities.Message;
import org.entities.User;
import org.service.ChatService;
import org.service.NotificationService;
import org.service.SessionManager;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;



/**
 * Controller for the chat window ({@code messages.fxml}).
 * Displays a sorted list of conversation previews on the left and the active
 * conversation's messages on the right. Two background threads keep both lists
 * up to date while the window is open:
 * <ul>
 *   <li>{@link #startPreviewsAutoUpdate()} — polls for new/changed previews every 500 ms.</li>
 *   <li>{@link #startMessagesAutoUpdate()} — polls for new messages in the open chat every 500 ms.</li>
 * </ul>
 * The preview list is backed by a {@link javafx.collections.transformation.SortedList} that
 * keeps unread conversations at the top.
 */
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



    private ObservableList<ChatPreview> chatPreviews =
            FXCollections.observableArrayList(
                    preview -> new Observable[] { preview.isReadProperty() }
            );
    private SortedList<ChatPreview> chatPreviewsSorted = new SortedList<>(chatPreviews, Comparator.comparing(ChatPreview::getIsRead));
    private MessageDao messageDao = new MessageDao();
    private UserDao userDao = new UserDao();
    private ChatService chatService = new ChatService(messageDao);
    private final NotificationService notificationService = new NotificationService(new NotificationDao());
    private ObservableList<Message> messages = FXCollections.observableArrayList();
    SortedList<Message> sortedMessages = new SortedList<>(messages, Comparator.naturalOrder());
    long userId;
    long otherId;
    private Thread updateChatThread;
    private Thread updatePreviewsThread;

    /**
     * Starts the background thread that polls for new chat previews every 500 ms.
     * Any newly fetched previews are added to {@link #chatPreviews}; if the preview
     * belongs to the currently open conversation it is immediately marked as read.
     */
    public void startPreviewsAutoUpdate() {
        updatePreviewsThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(500);

                    List<ChatPreview> newChatPreviews = chatService.getNewChatPreviews(chatPreviews, userId);

                    Platform.runLater(() -> {
                        for (ChatPreview chatPreview : newChatPreviews) {
                            if (chatPreview.getUserId().equals(otherId)) {
                                chatPreview.setIsRead(true);
                            }
                        }
                        chatPreviews.addAll(newChatPreviews);
                        chatMessages.scrollTo(chatMessages.getItems().size() - 1);
                    });

                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        updatePreviewsThread.start();
    }

    /**
     * Starts the background thread that polls for new messages in the active conversation
     * every 500 ms. Fetches only messages newer than the last known ID when a history
     * already exists, or the full history otherwise. All messages are marked as read
     * both locally and in the database.
     */
    private void startMessagesAutoUpdate(){
        updateChatThread = new Thread(()-> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    List<Message> newMessages;
                    Thread.sleep(500);
                    if (!messages.isEmpty()) {
                        Long lastId = messages.get(messages.size() - 1).getId();
                        newMessages = messageDao.findNewMessagesBetweenUsers(userId, otherId, lastId);
                    }
                    else {
                        newMessages = messageDao.findMessagesBetweenUsers(userId, otherId);
                    }
                    Platform.runLater(() -> {
                        messages.addAll(newMessages);
                        for (Message message: messages){
                            message.setRead(true);
                        }
                    });
                    messageDao.markMessagesAsRead(userId, otherId);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        updateChatThread.start();
    }

    /**
     * Selects the given preview in the left-side list and opens its conversation.
     * Called by {@link ChatUserSearchResultController} when a search result is clicked.
     *
     * @param chatPreview the preview to open
     */
    public void chooseUserFromLeftSide(ChatPreview chatPreview){
        chatUsers.getSelectionModel().select(chatPreview);
        openChat(chatPreview);
    }

    /**
     * Shows or hides the right-side chat panel (message history + input field).
     *
     * @param is {@code true} to show, {@code false} to hide
     */
    public void rightSideVisibility(Boolean is){
        chatRightSide.setVisible(is);
        chatRightSide.setManaged(is);
    }

    /**
     * Opens the conversation for the given preview: marks it as read, shows the
     * right panel, sets the header name, updates {@link #otherId}, and loads messages.
     *
     * @param preview the preview whose conversation should be opened
     */
    public void openChat(ChatPreview preview) {
        preview.setIsRead(true);
        chatUsers.refresh();
        rightSideVisibility(true);
        setRightSideName(preview.getName(), preview.getSurname());
        setOtherId(preview.getUserId());
        loadMessages();
    }

    /**
     * Sets the label shown at the top of the right-side panel to the other user's full name.
     *
     * @param name    the other user's first name
     * @param surname the other user's surname
     */
    public void setRightSideName(String name, String surname){
        rightSideName.setText(name + " " + surname);
    }

    /**
     * Sets the user ID of the conversation partner currently shown on the right side.
     *
     * @param id the other user's DB ID
     */
    public void setOtherId (Long id){
        this.otherId = id;
    }

    /**
     * Sets the ID of the currently logged-in user. Must be called by the parent
     * before the controller is used.
     *
     * @param id the current user's DB ID
     */
    public void setUserId (Long id){
        this.userId = id;
    }

    /**
     * Loads the full message history between the current user and {@link #otherId},
     * marks all messages as read, and starts the auto-update polling thread.
     * Interrupts any previously running message-update thread first.
     */
    public void loadMessages(){
        if (updateChatThread != null) {
            updateChatThread.interrupt();
        }
        messages.setAll(messageDao.findMessagesBetweenUsers(userId, otherId));
        messageDao.markMessagesAsRead(userId, otherId);
        for (Message message: messages){
            message.setRead(true);
        }
        startMessagesAutoUpdate();
    }

    /**
     * Adds a new preview entry to the observable list, causing the left-side list to update.
     * Called when a chat is initiated with a user not yet in the list.
     *
     * @param chatPreview the preview to add
     */
    public void addChatPreview(ChatPreview chatPreview){
        chatPreviews.add(chatPreview);
    }


    /**
     * JavaFX initialize callback — wires up cell factories for both list views,
     * loads initial chat previews, disables mouse interaction on the messages list
     * (read-only display), and starts the preview auto-update thread.
     */
    @FXML
    public void initialize() {
        chatPreviews.setAll(chatService.getChatPreviews(userId));
        chatUsers.setItems(chatPreviewsSorted);
        chatMessages.setSelectionModel(null);
        chatMessages.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> e.consume());
        chatMessages.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> e.consume());
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
                        cpController.bindPreview(preview);

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

    /**
     * FXML action handler for the close button.
     * Interrupts both background polling threads and closes the chat stage.
     */
    @FXML
    private void closeChat(){
        Stage stage = (Stage) closeButton.getScene().getWindow();
        if (updateChatThread!=null) {
            updateChatThread.interrupt();
        }
        updatePreviewsThread.interrupt();
        stage.close();
    }

    /**
     * FXML action handler for the send button.
     * Persists the typed message and sends a new-message notification to the recipient.
     */
    @FXML
    private void sendMessage(){
        messageDao.saveMessage(userId, otherId, messageTypeField.getText());
        notificationService.notifyNewMessage(
                userId,
                SessionManager.getInstance().getCurrentUser().getFirstName(),
                otherId
        );
    }

    /**
     * FXML action handler for the user-search field.
     * Splits the input on whitespace and looks up a user by first name + surname.
     * Requires at least two words; does nothing for single-word input.
     * On match, opens a search-results popup ({@code chat-searching-container.fxml}).
     */
    @FXML
    private void searchChatUsers(){
        String text = chatSearchField.getText();
        String[] words = text.trim().split("\\s+");
        if (words.length < 2) return;
        List<User> users = userDao.findUserByNameSurname(words[0], words[1]);
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
