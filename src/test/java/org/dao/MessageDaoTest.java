package org.dao;

import org.entities.Message;
import org.entities.User;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageDaoTest {

    private MessageDao messageDao;
    private UserDao userDao;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        messageDao = new MessageDao();
        userDao    = new UserDao();
        user1 = createUser("MsgSender");
        user2 = createUser("MsgRecipient");
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private User createUser(String prefix) {
        User u = new User();
        u.setUsername(prefix + "_" + UUID.randomUUID());
        u.setPasswordHash("hashed");
        u.setEmail(prefix + "_" + UUID.randomUUID() + "@test.com");
        u.setFirstName(prefix);
        u.setSureName("Test");
        u.setPhoneNumber("090" + UUID.randomUUID().toString().replace("-","").substring(0, 7));
        u.setRole("student");
        userDao.save(u);
        return u;
    }

    // ── saveMessage ───────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("saveMessage: persists a message between two users")
    void testSaveMessage() {
        assertDoesNotThrow(() ->
                messageDao.saveMessage(user1.getId(), user2.getId(), "Hello from test!")
        );
    }

    // ── findMessagesBetweenUsers ──────────────────────────────────────────────

    @Test
    @Order(2)
    @DisplayName("findMessagesBetweenUsers: returns saved message")
    void testFindMessagesBetweenUsers() {
        messageDao.saveMessage(user1.getId(), user2.getId(), "Test message content");

        List<Message> messages = messageDao.findMessagesBetweenUsers(
                user1.getId(), user2.getId());

        assertNotNull(messages);
        assertFalse(messages.isEmpty());
        assertTrue(messages.stream().anyMatch(m ->
                m.getContent().equals("Test message content")));
    }

    @Test
    @Order(3)
    @DisplayName("findMessagesBetweenUsers: returns empty for users with no conversation")
    void testFindMessagesBetweenUsersEmpty() {
        User u3 = createUser("NoMsg1");
        User u4 = createUser("NoMsg2");

        List<Message> messages = messageDao.findMessagesBetweenUsers(
                u3.getId(), u4.getId());

        assertNotNull(messages);
        assertTrue(messages.isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("findMessagesBetweenUsers: works with reversed user order")
    void testFindMessagesBetweenUsersReversed() {
        messageDao.saveMessage(user1.getId(), user2.getId(), "Reversed order test");

        List<Message> messages = messageDao.findMessagesBetweenUsers(
                user2.getId(), user1.getId());

        assertNotNull(messages);
        assertFalse(messages.isEmpty());
    }

    // ── findUserMessages ──────────────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("findUserMessages: returns messages where user is sender")
    void testFindUserMessagesAsSender() {
        messageDao.saveMessage(user1.getId(), user2.getId(), "Sender test");

        List<Message> messages = messageDao.findUserMessages(user1.getId());

        assertNotNull(messages);
        assertFalse(messages.isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("findUserMessages: returns messages where user is recipient")
    void testFindUserMessagesAsRecipient() {
        messageDao.saveMessage(user1.getId(), user2.getId(), "Recipient test");

        List<Message> messages = messageDao.findUserMessages(user2.getId());

        assertNotNull(messages);
        assertFalse(messages.isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("findUserMessages: returns empty for user with no messages")
    void testFindUserMessagesEmpty() {
        User lonely = createUser("Lonely");

        List<Message> messages = messageDao.findUserMessages(lonely.getId());

        assertNotNull(messages);
        assertTrue(messages.isEmpty());
    }

    // ── findNewMessagesBetweenUsers ───────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("findNewMessagesBetweenUsers: returns messages after lastMessageId")
    void testFindNewMessagesBetweenUsers() {
        messageDao.saveMessage(user1.getId(), user2.getId(), "Old message");

        List<Message> all = messageDao.findMessagesBetweenUsers(
                user1.getId(), user2.getId());
        assertFalse(all.isEmpty());
        Long lastId = all.get(all.size() - 1).getId();

        messageDao.saveMessage(user1.getId(), user2.getId(), "New message");

        List<Message> newMessages = messageDao.findNewMessagesBetweenUsers(
                user1.getId(), user2.getId(), lastId);

        assertNotNull(newMessages);
        assertFalse(newMessages.isEmpty());
        assertTrue(newMessages.stream().allMatch(m -> m.getId() > lastId));
    }

    @Test
    @Order(9)
    @DisplayName("findNewMessagesBetweenUsers: returns empty when no new messages")
    void testFindNewMessagesBetweenUsersEmpty() {
        messageDao.saveMessage(user1.getId(), user2.getId(), "Only message");

        List<Message> all = messageDao.findMessagesBetweenUsers(
                user1.getId(), user2.getId());
        Long lastId = all.get(all.size() - 1).getId();

        List<Message> newMessages = messageDao.findNewMessagesBetweenUsers(
                user1.getId(), user2.getId(), lastId);

        assertNotNull(newMessages);
        assertTrue(newMessages.isEmpty());
    }

    // ── markMessagesAsRead ────────────────────────────────────────────────────

    @Test
    @Order(10)
    @DisplayName("markMessagesAsRead: executes without error")
    void testMarkMessagesAsRead() {
        messageDao.saveMessage(user1.getId(), user2.getId(), "Unread message");

        assertDoesNotThrow(() ->
                messageDao.markMessagesAsRead(user2.getId(), user1.getId())
        );
    }

    @Test
    @Order(11)
    @DisplayName("markMessagesAsRead: works when no unread messages exist")
    void testMarkMessagesAsReadNoMessages() {
        User u5 = createUser("NoUnread1");
        User u6 = createUser("NoUnread2");

        assertDoesNotThrow(() ->
                messageDao.markMessagesAsRead(u5.getId(), u6.getId())
        );
    }
}