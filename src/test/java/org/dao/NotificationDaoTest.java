package org.dao;

import org.entities.Notification;
import org.entities.NotificationReceiver;
import org.entities.User;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NotificationDaoTest {

    private NotificationDao notificationDao;
    private UserDao         userDao;
    private User            testUser;

    @BeforeEach
    void setUp() {
        notificationDao = new NotificationDao();
        userDao         = new UserDao();
        testUser        = createUser("NotifUser");
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

    private Notification saveNotification(Long... recipientIds) {
        return notificationDao.saveNotification(
                "notification.lesson.created",
                "Math|Room 101",
                List.of(recipientIds)
        );
    }

    // ── saveNotification ──────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("saveNotification: persists notification and returns with ID")
    void testSaveNotification() {
        Notification n = saveNotification(testUser.getId());

        assertNotNull(n);
        assertNotNull(n.getId());
    }

    @Test
    @Order(2)
    @DisplayName("saveNotification: works with multiple recipients")
    void testSaveNotificationMultipleRecipients() {
        User user2 = createUser("NotifUser2");

        Notification n = notificationDao.saveNotification(
                "notification.lesson.created",
                "Physics|Room 202",
                List.of(testUser.getId(), user2.getId())
        );

        assertNotNull(n);
        assertNotNull(n.getId());
    }

    @Test
    @Order(3)
    @DisplayName("saveNotification: works with null messageParams")
    void testSaveNotificationNullParams() {
        Notification n = notificationDao.saveNotification(
                "notification.lesson.created",
                null,
                List.of(testUser.getId())
        );

        assertNotNull(n);
        assertNotNull(n.getId());
    }

    // ── findByUserId ──────────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("findByUserId: returns receivers after notification saved")
    void testFindByUserId() {
        saveNotification(testUser.getId());

        List<NotificationReceiver> receivers =
                notificationDao.findByUserId(testUser.getId());

        assertNotNull(receivers);
        assertFalse(receivers.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("findByUserId: returns empty for user with no notifications")
    void testFindByUserIdEmpty() {
        User fresh = createUser("FreshUser");

        List<NotificationReceiver> receivers =
                notificationDao.findByUserId(fresh.getId());

        assertNotNull(receivers);
        assertTrue(receivers.isEmpty());
    }

    // ── findTranslatedForUser ─────────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("findTranslatedForUser: returns rows after notification saved")
    void testFindTranslatedForUser() {
        saveNotification(testUser.getId());

        List<Object[]> rows = notificationDao.findTranslatedForUser(testUser.getId());

        assertNotNull(rows);
        assertFalse(rows.isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("findTranslatedForUser: each row has 4 columns")
    void testFindTranslatedForUserRowStructure() {
        saveNotification(testUser.getId());

        List<Object[]> rows = notificationDao.findTranslatedForUser(testUser.getId());

        assertFalse(rows.isEmpty());
        assertEquals(4, rows.get(0).length);
    }

    @Test
    @Order(8)
    @DisplayName("findTranslatedForUser: returns empty for user with no notifications")
    void testFindTranslatedForUserEmpty() {
        User fresh = createUser("FreshTranslated");

        List<Object[]> rows = notificationDao.findTranslatedForUser(fresh.getId());

        assertNotNull(rows);
        assertTrue(rows.isEmpty());
    }

    // ── countUnread ───────────────────────────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("countUnread: returns > 0 after new notification")
    void testCountUnreadAfterSave() {
        saveNotification(testUser.getId());

        long count = notificationDao.countUnread(testUser.getId());

        assertTrue(count >= 1);
    }

    @Test
    @Order(10)
    @DisplayName("countUnread: returns 0 for user with no notifications")
    void testCountUnreadZero() {
        User fresh = createUser("FreshCount");

        long count = notificationDao.countUnread(fresh.getId());

        assertEquals(0, count);
    }

    // ── markAsRead ────────────────────────────────────────────────────────────

    @Test
    @Order(11)
    @DisplayName("markAsRead: reduces unread count by 1")
    void testMarkAsRead() {
        Notification n = saveNotification(testUser.getId());
        long before = notificationDao.countUnread(testUser.getId());

        notificationDao.markAsRead(testUser.getId(), n.getId());

        long after = notificationDao.countUnread(testUser.getId());

        assertTrue(after < before || after == 0);
    }

    @Test
    @Order(12)
    @DisplayName("markAsRead: does nothing for non-existent notification")
    void testMarkAsReadNonExistent() {
        assertDoesNotThrow(() ->
                notificationDao.markAsRead(testUser.getId(), Long.MAX_VALUE)
        );
    }

    // ── markAllAsRead ─────────────────────────────────────────────────────────

    @Test
    @Order(13)
    @DisplayName("markAllAsRead: sets unread count to 0")
    void testMarkAllAsRead() {
        saveNotification(testUser.getId());
        saveNotification(testUser.getId());

        notificationDao.markAllAsRead(testUser.getId());

        long count = notificationDao.countUnread(testUser.getId());

        assertEquals(0, count);
    }

    @Test
    @Order(14)
    @DisplayName("markAllAsRead: works for user with no notifications")
    void testMarkAllAsReadNoNotifications() {
        User fresh = createUser("FreshMarkAll");

        assertDoesNotThrow(() ->
                notificationDao.markAllAsRead(fresh.getId())
        );
    }
}