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

    @Test
    @DisplayName("Lesson Updated: Should NOT call DAO when recipient list is empty")
    void shouldNotSaveLessonUpdatedWhenRecipientsEmpty() {
        notificationService.notifyLessonUpdated("Physics", "B202", Collections.emptyList());

        verifyNoInteractions(notificationDao);
    }

    @Test
    @DisplayName("Lesson Updated: Should NOT call DAO when recipient list is null")
    void shouldNotSaveLessonUpdatedWhenRecipientsNull() {
        notificationService.notifyLessonUpdated("Physics", "B202", null);

        verifyNoInteractions(notificationDao);
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

    // getNotificationsForUser

    @Test
    @DisplayName("Get Notifications: Should delegate to DAO and return results")
    void shouldDelegateGetNotificationsForUserToDao() {
        when(notificationDao.findByUserId(1L)).thenReturn(Collections.emptyList());

        notificationService.getNotificationsForUser(1L);

        verify(notificationDao).findByUserId(1L);
    }

    // getNotificationDtosForUser — additional branches

    @Test
    @DisplayName("Get DTOs: Should filter out rows where content is null")
    void shouldFilterOutNullContentRows() {
        LocalDateTime sentAt = LocalDateTime.of(2026, 3, 8, 10, 30);
        Object[] nullRow  = {1L, null,            Timestamp.valueOf(sentAt), Boolean.FALSE};
        Object[] validRow = {2L, "Valid content", Timestamp.valueOf(sentAt), Boolean.FALSE};
        when(notificationDao.findTranslatedForUser(1L)).thenReturn(List.of(nullRow, validRow));

        List<NotificationDto> dtos = notificationService.getNotificationDtosForUser(1L);

        assertEquals(1, dtos.size());
        assertEquals("Valid content", dtos.get(0).getContent());
    }

    @Test
    @DisplayName("Get DTOs: Should filter out rows where content is blank")
    void shouldFilterOutBlankContentRows() {
        LocalDateTime sentAt = LocalDateTime.of(2026, 3, 8, 10, 30);
        Object[] blankRow = {1L, "   ",          Timestamp.valueOf(sentAt), Boolean.FALSE};
        Object[] validRow = {2L, "Real content", Timestamp.valueOf(sentAt), Boolean.FALSE};
        when(notificationDao.findTranslatedForUser(1L)).thenReturn(List.of(blankRow, validRow));

        List<NotificationDto> dtos = notificationService.getNotificationDtosForUser(1L);

        assertEquals(1, dtos.size());
        assertEquals("Real content", dtos.get(0).getContent());
    }

    @Test
    @DisplayName("Get DTOs: Should handle LocalDateTime sentAt directly (not Timestamp)")
    void shouldHandleLocalDateTimeSentAt() {
        LocalDateTime sentAt = LocalDateTime.of(2026, 3, 8, 10, 30);
        Object[] row = {1L, "Content", sentAt, Boolean.FALSE};
        when(notificationDao.findTranslatedForUser(1L)).thenReturn(Collections.singletonList(row));

        List<NotificationDto> dtos = notificationService.getNotificationDtosForUser(1L);

        assertEquals(1, dtos.size());
        assertEquals(sentAt, dtos.get(0).getSentAt());
    }

    @Test
    @DisplayName("Get DTOs: Should handle numeric is_read value (Integer 1 = read)")
    void shouldHandleNumericIsReadValue() {
        LocalDateTime sentAt = LocalDateTime.of(2026, 3, 8, 10, 30);
        Object[] row = {1L, "Content", Timestamp.valueOf(sentAt), 1};
        when(notificationDao.findTranslatedForUser(1L)).thenReturn(Collections.singletonList(row));

        List<NotificationDto> dtos = notificationService.getNotificationDtosForUser(1L);

        assertEquals(1, dtos.size());
        assertTrue(dtos.get(0).isRead());
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
    @DisplayName("Get Unread Count: Should return count of unread DTOs with content")
    void shouldReturnUnreadCountFromDao() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        // 3 unread rows with content, 2 read rows with content — expect count 3
        List<Object[]> rows = List.of(
            new Object[]{1L, "msg1", now, Boolean.FALSE},
            new Object[]{2L, "msg2", now, Boolean.FALSE},
            new Object[]{3L, "msg3", now, Boolean.FALSE},
            new Object[]{4L, "msg4", now, Boolean.TRUE},
            new Object[]{5L, "msg5", now, Boolean.TRUE}
        );
        when(notificationDao.findTranslatedForUser(1L)).thenReturn(rows);

        long count = notificationService.getUnreadCount(1L);

        assertEquals(3L, count);
    }
}
