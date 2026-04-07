package org.service;

import dto.NotificationDto;
import org.dao.NotificationDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Logic Test: NotificationService tests with JUnit 5 & Mockito")
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    NotificationDao notificationDao;

    @InjectMocks
    NotificationService notificationService;

    // notifyLessonAdded

    @Test
    @DisplayName("Lesson Added: Should save notification with course name and classroom")
    void shouldSaveLessonAddedNotification() {
        List<Long> recipients = List.of(1L, 2L);

        notificationService.notifyLessonAdded("Math", "A101", recipients);

        verify(notificationDao).saveNotification(
            "notification.newLesson",
            "Math|A101",
            recipients
        );
    }

    @Test
    @DisplayName("Lesson Added: Should NOT call DAO when recipient list is empty")
    void shouldNotSaveLessonAddedWhenRecipientsEmpty() {
        notificationService.notifyLessonAdded("Math", "A101", Collections.emptyList());

        verifyNoInteractions(notificationDao);
    }

    @Test
    @DisplayName("Lesson Added: Should NOT call DAO when recipient list is null")
    void shouldNotSaveLessonAddedWhenRecipientsNull() {
        notificationService.notifyLessonAdded("Math", "A101", null);

        verifyNoInteractions(notificationDao);
    }

    // notifyLessonUpdated

    @Test
    @DisplayName("Lesson Updated: Should save notification with 'Course ... class changes to ...' format")
    void shouldSaveLessonUpdatedNotification() {
        List<Long> recipients = List.of(3L);

        notificationService.notifyLessonUpdated("Physics", "B202", recipients);

        verify(notificationDao).saveNotification(
            "notification.courseChanged",
            "Physics|B202",
            recipients
        );
    }

    // notifyLessonDeleted

    @Test
    @DisplayName("Lesson Deleted: Should save notification with 'Class {id} was cancelled' format")
    void shouldSaveLessonDeletedNotification() {
        List<Long> recipients = List.of(1L);

        notificationService.notifyLessonDeleted(37294L, recipients);

        verify(notificationDao).saveNotification(
            "notification.lessonCancelled",
            "37294",
            recipients
        );
    }

    @Test
    @DisplayName("Lesson Deleted: Should NOT call DAO when recipient list is empty")
    void shouldNotSaveLessonDeletedWhenRecipientsEmpty() {
        notificationService.notifyLessonDeleted(100L, Collections.emptyList());

        verifyNoInteractions(notificationDao);
    }

    // notifyStudentAddedToGroup

    @Test
    @DisplayName("Group Added: Should save notification with correct group code in content")
    void shouldSaveStudentAddedToGroupNotification() {
        notificationService.notifyStudentAddedToGroup("CS101", 5L);

        verify(notificationDao).saveNotification(
            "notification.groupAdded",
            "CS101",
            List.of(5L)
        );
    }

    @Test
    @DisplayName("Group Added: Should NOT call DAO when recipientId is null")
    void shouldNotSaveStudentAddedWhenRecipientNull() {
        notificationService.notifyStudentAddedToGroup("CS101", null);

        verifyNoInteractions(notificationDao);
    }

    // notifyStudentRemovedFromGroup

    @Test
    @DisplayName("Group Removed: Should save notification with correct group code in content")
    void shouldSaveStudentRemovedFromGroupNotification() {
        notificationService.notifyStudentRemovedFromGroup("CS101", 7L);

        verify(notificationDao).saveNotification(
            "notification.groupRemoved",
            "CS101",
            List.of(7L)
        );
    }

    @Test
    @DisplayName("Group Removed: Should NOT call DAO when recipientId is null")
    void shouldNotSaveStudentRemovedWhenRecipientNull() {
        notificationService.notifyStudentRemovedFromGroup("CS101", null);

        verifyNoInteractions(notificationDao);
    }

    // notifyNewMessage

    @Test
    @DisplayName("New Message: Should save notification with sender first name and exclamation mark")
    void shouldSaveNewMessageNotification() {
        notificationService.notifyNewMessage(1L, "John", 2L);

        verify(notificationDao).saveNotification(
            "notification.newMessage",
            "John",
            List.of(2L)
        );
    }

    @Test
    @DisplayName("New Message: Should NOT notify when sender and recipient are the same user")
    void shouldNotSaveNewMessageWhenSenderEqualsRecipient() {
        notificationService.notifyNewMessage(1L, "John", 1L);

        verifyNoInteractions(notificationDao);
    }

    @Test
    @DisplayName("New Message: Should NOT call DAO when recipientId is null")
    void shouldNotSaveNewMessageWhenRecipientNull() {
        notificationService.notifyNewMessage(1L, "John", null);

        verifyNoInteractions(notificationDao);
    }

    // getNotificationDtosForUser

    @Test
    @DisplayName("Get DTOs: Should return mapped DTOs with correct content, sentAt, and isRead")
    void shouldReturnMappedDtos() {
        LocalDateTime sentAt = LocalDateTime.of(2026, 3, 8, 10, 30);
        Object[] row = {42L, "Test content", Timestamp.valueOf(sentAt), 0};

        when(notificationDao.findTranslatedForUser(1L)).thenReturn(Collections.singletonList(row));

        List<NotificationDto> dtos = notificationService.getNotificationDtosForUser(1L);

        assertEquals(1, dtos.size());
        NotificationDto dto = dtos.get(0);
        assertEquals(42L, dto.getNotificationId());
        assertEquals("Test content", dto.getContent());
        assertEquals(sentAt, dto.getSentAt());
        assertFalse(dto.isRead());
    }

    @Test
    @DisplayName("Get DTOs: Should return empty list when user has no notifications")
    void shouldReturnEmptyDtoListWhenNoNotifications() {
        when(notificationDao.findTranslatedForUser(99L)).thenReturn(Collections.emptyList());

        List<NotificationDto> dtos = notificationService.getNotificationDtosForUser(99L);

        assertTrue(dtos.isEmpty());
    }

    // markAsRead / markAllAsRead / getUnreadCount

    @Test
    @DisplayName("Mark As Read: Should delegate to DAO with correct userId and notificationId")
    void shouldDelegateMarkAsReadToDao() {
        notificationService.markAsRead(1L, 10L);

        verify(notificationDao).markAsRead(1L, 10L);
    }

    @Test
    @DisplayName("Mark All As Read: Should delegate to DAO with correct userId")
    void shouldDelegateMarkAllAsReadToDao() {
        notificationService.markAllAsRead(3L);

        verify(notificationDao).markAllAsRead(3L);
    }

    @Test
    @DisplayName("Get Unread Count: Should return count from DAO")
    void shouldReturnUnreadCountFromDao() {
        when(notificationDao.countUnread(1L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(1L);

        assertEquals(5L, count);
    }
}
