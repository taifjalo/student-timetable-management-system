package org.dao;

import org.entities.Notification;
import org.entities.NotificationReceiver;
import org.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Test: This class needs only Tests are applied with Integration to DB, without JUnit 5 & Mock logic Tests")
class NotificationDaoTest {

    private final NotificationDao notificationDao = new NotificationDao();
    private final UserDao userDao = new UserDao();

    @DisplayName("Helper Method: First Create the user")
    private User createAndSaveUser() {
        User user = new User();
        user.setUsername("mock" + UUID.randomUUID());
        user.setPasswordHash("hashed");
        user.setEmail("mock+" + UUID.randomUUID() + "@test.com");
        user.setFirstName("Mock");
        user.setSureName("User");
        user.setPhoneNumber("090" + UUID.randomUUID().toString().substring(0, 7));
        user.setRole("student");
        userDao.save(user);
        return user;
    }

    @Test
    @DisplayName("Integration Test: Should Save Notification And Find It By User")
    void shouldSaveNotificationAndFindByUser() {
        User user = createAndSaveUser();

        Notification saved = notificationDao.saveNotification("notification.newLesson", "CS101|Room 202", List.of(user.getId()));

        List<NotificationReceiver> results = notificationDao.findByUserId(user.getId());

        assertFalse(results.isEmpty());
        assertEquals(saved.getId(), results.get(0).getNotification().getId());
    }

    @Test
    @DisplayName("Integration Test: Should Save Notification For Multiple Recipients")
    void shouldSaveNotificationForMultipleRecipients() {
        User user1 = createAndSaveUser();
        User user2 = createAndSaveUser();

        Notification saved = notificationDao.saveNotification("notification.newMessage", "Alice", List.of(user1.getId(), user2.getId()));

        List<NotificationReceiver> results1 = notificationDao.findByUserId(user1.getId());
        List<NotificationReceiver> results2 = notificationDao.findByUserId(user2.getId());

        assertTrue(results1.stream().anyMatch(nr -> saved.getId().equals(nr.getNotification().getId())));
        assertTrue(results2.stream().anyMatch(nr -> saved.getId().equals(nr.getNotification().getId())));
    }

    @Test
    @DisplayName("Integration Test: Should Default isRead To False On Save")
    void shouldDefaultIsReadToFalse() {
        User user = createAndSaveUser();

        notificationDao.saveNotification("notification.groupAdded", "SWD22S", List.of(user.getId()));

        List<NotificationReceiver> results = notificationDao.findByUserId(user.getId());
        NotificationReceiver latest = results.get(0);

        assertFalse(latest.isRead());
    }

    @Test
    @DisplayName("Integration Test: Should Mark Single Notification As Read")
    void shouldMarkSingleNotificationAsRead() {
        User user = createAndSaveUser();

        Notification saved = notificationDao.saveNotification("notification.groupAdded", "SWD22S", List.of(user.getId()));

        notificationDao.markAsRead(user.getId(), saved.getId());

        List<NotificationReceiver> results = notificationDao.findByUserId(user.getId());
        NotificationReceiver nr = results.stream()
            .filter(r -> r.getNotification().getId().equals(saved.getId()))
            .findFirst()
            .orElseThrow();

        assertTrue(nr.isRead());
    }

    @Test
    @DisplayName("Integration Test: Should Mark All Notifications As Read")
    void shouldMarkAllNotificationsAsRead() {
        User user = createAndSaveUser();

        notificationDao.saveNotification("notification.groupAdded", "SWD22S", List.of(user.getId()));
        notificationDao.saveNotification("notification.groupRemoved", "SWD22S", List.of(user.getId()));

        notificationDao.markAllAsRead(user.getId());

        List<NotificationReceiver> results = notificationDao.findByUserId(user.getId());
        assertTrue(results.stream().allMatch(NotificationReceiver::isRead));
    }

    @Test
    @DisplayName("Integration Test: Should Count Unread And Decrease After MarkAllAsRead")
    void shouldCountUnreadAndDecreaseAfterMarkAll() {
        User user = createAndSaveUser();

        notificationDao.saveNotification("notification.groupAdded", "SWD22S", List.of(user.getId()));
        notificationDao.saveNotification("notification.groupRemoved", "SWD22S", List.of(user.getId()));

        long unreadBefore = notificationDao.countUnread(user.getId());
        assertTrue(unreadBefore >= 2);

        notificationDao.markAllAsRead(user.getId());

        long unreadAfter = notificationDao.countUnread(user.getId());
        assertEquals(0, unreadAfter);
    }

    @Test
    @DisplayName("Integration Test: Should Return Empty List When User Has No Notifications")
    void shouldReturnEmptyListForUserWithNoNotifications() {
        User user = createAndSaveUser();

        List<NotificationReceiver> results = notificationDao.findByUserId(user.getId());

        assertTrue(results.isEmpty());
    }
}
