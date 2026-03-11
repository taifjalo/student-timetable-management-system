package org.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {
    @Test
    @DisplayName("Notification: Parameterized constructor should set sentAt and content")
    void notificationParameterizedConstructor() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 9, 12, 0);
        Notification notification = new Notification(now, "Test content");

        assertEquals(now, notification.getSentAt());
        assertEquals("Test content", notification.getContent());
        assertNull(notification.getId());
    }

    @Test
    @DisplayName("Notification: Default constructor and setters should work")
    void notificationDefaultConstructorAndSetters() {
        Notification notification = new Notification();
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 8, 0);
        notification.setId(5L);
        notification.setSentAt(time);
        notification.setContent("Hello");

        assertEquals(5L, notification.getId());
        assertEquals(time, notification.getSentAt());
        assertEquals("Hello", notification.getContent());
    }


}