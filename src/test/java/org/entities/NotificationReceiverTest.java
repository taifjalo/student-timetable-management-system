package org.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationReceiverTest {
    @Test
    @DisplayName("NotificationReceiver: Default isRead should be false")
    void notificationReceiverDefaultIsReadFalse() {
        NotificationReceiver nr = new NotificationReceiver();
        assertFalse(nr.isRead());
    }

    @Test
    @DisplayName("NotificationReceiver: setRead should update isRead")
    void notificationReceiverSetRead() {
        NotificationReceiver nr = new NotificationReceiver();
        nr.setRead(true);
        assertTrue(nr.isRead());
        nr.setRead(false);
        assertFalse(nr.isRead());
    }

    @Test
    @DisplayName("NotificationReceiver: setters for user, notification, id")
    void notificationReceiverSetters() {
        User user = new User();
        user.setId(1L);

        Notification notif = new Notification(LocalDateTime.now());
        notif.setId(2L);

        NotificationReceiver.NotificationReceiverId id =
                new NotificationReceiver.NotificationReceiverId(1L, 2L);

        NotificationReceiver nr = new NotificationReceiver();
        nr.setId(id);
        nr.setUser(user);
        nr.setNotification(notif);

        assertEquals(1L, nr.getId().getUserId());
        assertEquals(2L, nr.getId().getNotificationId());
        assertSame(user, nr.getUser());
        assertSame(notif, nr.getNotification());
    }

    @Test
    @DisplayName("NotificationReceiverId: equals and hashCode should work correctly")
    void notificationReceiverIdEquality() {
        NotificationReceiver.NotificationReceiverId id1 =
                new NotificationReceiver.NotificationReceiverId(1L, 2L);
        NotificationReceiver.NotificationReceiverId id2 =
                new NotificationReceiver.NotificationReceiverId(1L, 2L);
        NotificationReceiver.NotificationReceiverId id3 =
                new NotificationReceiver.NotificationReceiverId(3L, 4L);

        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1.hashCode(), id3.hashCode());
        assertEquals(id1, id1); // reflexive
        assertNotEquals(id1, null);
        assertNotEquals(id1, "not an id");
    }

}
