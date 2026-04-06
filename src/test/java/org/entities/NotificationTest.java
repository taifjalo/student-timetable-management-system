package org.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {
    @Test
    @DisplayName("Notification: Parameterized constructor should set sentAt")
    void notificationParameterizedConstructor() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 9, 12, 0);
        Notification notification = new Notification(now);

        assertEquals(now, notification.getSentAt());
        assertNull(notification.getId());
    }

    @Test
    @DisplayName("Notification: Default constructor and setters should work")
    void notificationDefaultConstructorAndSetters() {
        Notification notification = new Notification();
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 8, 0);
        notification.setId(5L);
        notification.setSentAt(time);

        assertEquals(5L, notification.getId());
        assertEquals(time, notification.getSentAt());
    }


}