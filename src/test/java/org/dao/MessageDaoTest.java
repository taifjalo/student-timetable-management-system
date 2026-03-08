package org.dao;

import org.entities.Message;
import org.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Test: This class needs only Tests are applied with Integration to DB, without JUnit 5 & Mock logic Tests")

class MessageDaoTest {

    private MessageDao messageDao;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        messageDao = new MessageDao();
        userDao = new UserDao();

    }
    @DisplayName("Helper Method: First Create the user")
    private User createAndSaveUser() {

        // Arrange New User
        User user = new User();
        user.setUsername("mock" + UUID.randomUUID());                               // Make always Random Username to pass DB.
        user.setPasswordHash("hashed");
        user.setEmail("mock+" + UUID.randomUUID() + "@test.com");                   // Make always Random Email to pass DB.
        user.setFirstName("Mock");
        user.setSureName("User");
        user.setPhoneNumber("090" + UUID.randomUUID().toString().substring(0,7)); // Make always Random phone number to pass DB.
        user.setRole("student");

        // Save the User:
        userDao.save(user);
        return user;
    }

    @Test
    @DisplayName("Integration Test: Should Save And Retrieve Message Integration")
    void shouldSaveAndRetrieveMessageIntegration() {
        // Create users
        User sender = createAndSaveUser();
        User recipient = createAndSaveUser();

        messageDao.saveMessage(sender.getId(), recipient.getId(), "Hello Integration");

        List<Message> messages =
                messageDao.findMessagesBetweenUsers(
                        sender.getId(),
                        recipient.getId()
                );

        assertEquals(1, messages.size());
        assertEquals("Hello Integration", messages.get(0).getContent());

    }
}