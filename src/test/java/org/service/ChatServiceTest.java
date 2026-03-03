package org.service;

import dto.ChatPreview;
import org.dao.MessageDao;
import org.entities.Message;
import org.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.fortuna.ical4j.model.LocationType.other;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("Logic Test: Tests are applied with JUnit 5 & Mockito for func Logic Test purpose")

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    MessageDao messageDao;

    @InjectMocks
    ChatService chatService;

    @DisplayName("Helper Method: First Should Create User")
    private User createUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setFirstName(name);
        return user;
    }

    @DisplayName("Helper Method: Second Should Create Message")
    private Message createMessage(User sender, User recipient, boolean isRead) {
        Message message = new Message();
        message.setSenderUser(sender);
        message.setRecipientUser(recipient);
        message.setRead(isRead);
        return message;
    }


    @DisplayName("No Messages Test: Should Return Empty List When No Messages")
    @Test
    void shouldReturnEmptyListWhenNoMessages() {

        // Find the User with id then return the empty Message list
        when(messageDao.findUserMessages(1L)).thenReturn(Collections.emptyList());

        List<ChatPreview> result =  chatService.getChatPreviews(1L);

        assertTrue(result.isEmpty());
    }

    @DisplayName("Unread Message Test: Should Mark Conversation Unread When User is Recipient and Message is Unread")
    @Test
    void shouldMarkConversationUnreadWhenUserIsRecipientAndMessageIsUnread() {
        // Create users Sender and Recipien
        User senderUser = createUser(1L, "sender");
        User recipientUser = createUser(2L, "recipient");

        // Create read "true" messages sent by sender User
        Message message = createMessage(senderUser, recipientUser, true);

        when(messageDao.findUserMessages(1L)).thenReturn(Collections.singletonList(message));

        List<ChatPreview> result =
                chatService.getChatPreviews(1L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsRead());
    }

    @DisplayName("Duplicate Prevention Test: Should Return Only One Preview PerUser")
    @Test
    void shouldReturnOnlyOnePreviewPerUser() {

        // Create users Sender and Recipien
        User senderUser = createUser(1L, "sender");
        User recipientUser = createUser(2L, "recipient");

        // Create read "true" messages sent by sender User
        Message m1 = createMessage(senderUser, recipientUser, true);
        Message m2 = createMessage(senderUser, recipientUser, true);

        // return the messages of senderUser
        when(messageDao.findUserMessages(1L)).thenReturn(List.of(m1, m2));

        List<ChatPreview> result = chatService.getChatPreviews(1L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Recipient All Read Test: Should Keep Preview Read When All Messages Are Read")
    void shouldKeepPreviewReadWhenAllMessagesAreRead() {

        // Create users Sender and Recipien
        User senderUser = createUser(1L, "sender");
        User recipientUser = createUser(2L, "recipient");

        // Create read "true" messages sent by recipient user
        Message readMessage1 = createMessage( recipientUser, senderUser,true);
        Message readMessage2 = createMessage( recipientUser, senderUser,true);

        when(messageDao.findUserMessages(1L)).thenReturn(List.of(readMessage1, readMessage2));

        List<ChatPreview> result = chatService.getChatPreviews(1L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsRead());
    }

    @DisplayName("Unread Message Test: Should Mark Preview Unread If Any Unread Message Exists For Recipient")
    @Test
    void shouldMarkPreviewUnreadIfAnyUnreadMessageExistsForRecipient() {

        // Create users Sender and Recipien
        User senderUser = createUser(1L, "sender");
        User recipientUser = createUser(2L, "recipient");

        // Create read "true" messages sent by recipient user
        Message readMessage = createMessage( recipientUser, senderUser,true);

        // Create unread "false" messages sent by recipient user
        Message unreadMessage = createMessage(recipientUser, senderUser, false);

        // Create read "true" messages sent by senderUser
        Message sentMessage = createMessage(senderUser, recipientUser, true);

        when(messageDao.findUserMessages(1L)).thenReturn(List.of(readMessage, unreadMessage, sentMessage));

        List<ChatPreview> result = chatService.getChatPreviews(1L);

        assertEquals(1, result.size());

        ChatPreview preview = result.get(0);

        assertFalse(preview.getIsRead());
    }

    @Test
    @DisplayName("Multiple Conversations Test: Should Handle Mixed States Across Users")
    void shouldHandleMultipleConversations() {

        // Create users Sender and Recipien
        User senderUser = createUser(1L, "sender");

        User recipientUser2 = createUser(2L, "recipient2");
        User recipientUser3 = createUser(3L, "recipient3");

        Message u2_unread = createMessage(recipientUser2 , senderUser, false);
        Message u3_read = createMessage(recipientUser3, senderUser, true);

        when(messageDao.findUserMessages(1L))
                .thenReturn(List.of(u2_unread, u3_read));

        List<ChatPreview> result =
                chatService.getChatPreviews(1L);

        assertEquals(2, result.size());

        ChatPreview preview2 = result.stream()
                .filter(p -> p.getUserId().equals(2L))
                .findFirst().orElseThrow();

        ChatPreview preview3 = result.stream()
                .filter(p -> p.getUserId().equals(3L))
                .findFirst().orElseThrow();

        assertFalse(preview2.getIsRead());
        assertTrue(preview3.getIsRead());
    }
}
