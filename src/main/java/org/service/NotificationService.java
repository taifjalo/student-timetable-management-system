package org.service;

import dto.NotificationDto;
import org.dao.NotificationDao;
import org.entities.NotificationReceiver;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class NotificationService {

    private final NotificationDao notificationDao;

    private static final LocalizationService localizationService = new LocalizationService();
    static ResourceBundle selectedBundle = localizationService.getBundle();

    public NotificationService(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    // ── Lesson events ──────────────────────────────────────────────────────────

    public void notifyLessonAdded(String courseName, String classroom, List<Long> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) return;
        String content = String.format("New lesson: %s in %s", courseName, classroom);
        notificationDao.saveNotification(content, recipientIds);
    }

    public void notifyLessonUpdated(String courseName, String classroom, List<Long> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) return;
        String content = String.format("Course %s class changes to %s", courseName, classroom);
        notificationDao.saveNotification(content, recipientIds);
    }

    public void notifyLessonDeleted(Long lessonId, List<Long> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) return;
        String content = "Class " + lessonId + " was cancelled";
        notificationDao.saveNotification(content, recipientIds);
    }

    // ── Group events ───────────────────────────────────────────────────────────

    public void notifyStudentAddedToGroup(String groupCode, Long recipientId) {
        if (recipientId == null) return;
        String content = "You have been added to group " + groupCode;
        notificationDao.saveNotification(content, List.of(recipientId));
    }

    public void notifyStudentRemovedFromGroup(String groupCode, Long recipientId) {
        if (recipientId == null) return;
        String content = "You have been removed from group " + groupCode;
        notificationDao.saveNotification(content, List.of(recipientId));
    }

    // ── Message event ──────────────────────────────────────────────────────────

    public void notifyNewMessage(Long senderId, String senderName, Long recipientId) {
        if (recipientId == null || recipientId.equals(senderId)) return;
        String content = "You have a new message from " + senderName + "!";
        notificationDao.saveNotification(content, List.of(recipientId));
    }

    // ── Query ──────────────────────────────────────────────────────────────────

    public List<NotificationReceiver> getNotificationsForUser(Long userId) {
        return notificationDao.findByUserId(userId);
    }

    public List<NotificationDto> getNotificationDtosForUser(Long userId) {
        return notificationDao.findByUserId(userId).stream()
            .map(nr -> new NotificationDto(
                nr.getNotification().getId(),
                nr.getNotification().getContent(),
                nr.getNotification().getSentAt(),
                nr.isRead()
            ))
            .collect(Collectors.toList());
    }

    public void markAsRead(Long userId, Long notificationId) {
        notificationDao.markAsRead(userId, notificationId);
    }

    public void markAllAsRead(Long userId) {
        notificationDao.markAllAsRead(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationDao.countUnread(userId);
    }

}
