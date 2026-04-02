package org.service;

import dto.NotificationDto;
import org.dao.NotificationDao;
import org.entities.NotificationReceiver;

import java.util.List;
import java.util.stream.Collectors;

public class NotificationService {

    private final NotificationDao notificationDao;

    public NotificationService(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }


    public void notifyLessonAdded(String courseName, String classroom, List<Long> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) return;
        notificationDao.saveNotification("notification.newLesson", courseName + "|" + classroom, recipientIds);
    }

    public void notifyLessonUpdated(String courseName, String classroom, List<Long> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) return;
        notificationDao.saveNotification("notification.courseChanged", courseName + "|" + classroom, recipientIds);
    }

    public void notifyLessonDeleted(Long lessonId, List<Long> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) return;
        notificationDao.saveNotification("notification.lessonCancelled", lessonId.toString(), recipientIds);
    }

    public void notifyStudentAddedToGroup(String groupCode, Long recipientId) {
        if (recipientId == null) return;
        notificationDao.saveNotification("notification.groupAdded", groupCode, List.of(recipientId));
    }

    public void notifyStudentRemovedFromGroup(String groupCode, Long recipientId) {
        if (recipientId == null) return;
        notificationDao.saveNotification("notification.groupRemoved", groupCode, List.of(recipientId));
    }

    public void notifyNewMessage(Long senderId, String senderName, Long recipientId) {
        if (recipientId == null || recipientId.equals(senderId)) return;
        notificationDao.saveNotification("notification.newMessage", senderName, List.of(recipientId));
    }


    public List<NotificationReceiver> getNotificationsForUser(Long userId) {
        return notificationDao.findByUserId(userId);
    }

    public List<NotificationDto> getNotificationDtosForUser(Long userId) {
        return notificationDao.findByUserId(userId).stream()
                .map(nr -> new NotificationDto(
                        nr.getNotification().getId(),
                        nr.getNotification().getMessageKey(),
                        nr.getNotification().getMessageParams(),
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
