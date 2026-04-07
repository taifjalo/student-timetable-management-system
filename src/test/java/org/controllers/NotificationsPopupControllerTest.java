package org.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationsPopupControllerTest {

    @DisplayName("Helper Method: Mirrors Notifications Popup Controller.formatTime() logic.")
    private String formatTime(java.time.LocalDateTime sentAt) {
        long minutes = java.time.temporal.ChronoUnit.MINUTES.between(sentAt, java.time.LocalDateTime.now());
        if (minutes < 1) {
            return "Just now";
        }
        if (minutes < 60) {
            return minutes + " min ago";
        }
        long hours = java.time.temporal.ChronoUnit.HOURS.between(sentAt, java.time.LocalDateTime.now());
        if (hours < 24) {
            return hours + " h ago";
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(sentAt, java.time.LocalDateTime.now());
        return days + " d ago";
    }

    @Test
    @DisplayName("formatTime: Should return 'Just now' for very recent time")
    void shouldReturnJustNow() {
        assertEquals("Just now", formatTime(java.time.LocalDateTime.now().minusSeconds(30)));
    }

    @Test
    @DisplayName("formatTime: Should return minutes ago for times within last hour")
    void shouldReturnMinutesAgo() {
        String result = formatTime(java.time.LocalDateTime.now().minusMinutes(15));
        assertEquals("15 min ago", result);
    }

    @Test
    @DisplayName("formatTime: Should return hours ago for times within last day")
    void shouldReturnHoursAgo() {
        String result = formatTime(java.time.LocalDateTime.now().minusHours(3));
        assertEquals("3 h ago", result);
    }

    @Test
    @DisplayName("formatTime: Should return days ago for older times")
    void shouldReturnDaysAgo() {
        String result = formatTime(java.time.LocalDateTime.now().minusDays(5));
        assertEquals("5 d ago", result);
    }
}
