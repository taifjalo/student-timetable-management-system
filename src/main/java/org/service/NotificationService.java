package org.service;

import dto.NotificationDto;
import org.dao.NotificationDao;
import org.entities.NotificationReceiver;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service that encapsulates all notification-related business logic.
 * Each public {@code notify*} method maps a domain event to a specific i18n key and
 * saves the notification via {@link NotificationDao}.
 *
 * <p>All {@code notify*} methods silently skip the database call when the
 * recipient list is empty or {@code null}, avoiding unnecessary round-trips.
 */
public class NotificationService {

    private final NotificationDao notificationDao;

    /**
     * Creates a {@code NotificationService} with the given DAO.
     *
     * @param notificationDao the DAO used for persistence and querying
     */
    public NotificationService(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    /**
     * Notifies recipients that a new lesson has been added.
     * The message template key is {@code notification.newLesson}; params are
     * {@code courseName|classroom}.
     *
     * @param courseName   name of the course the lesson belongs to
     * @param classroom    classroom identifier (e.g. {@code "A101"})
     * @param recipientIds IDs of users to notify
     */
    public void notifyLessonAdded(String courseName, String classroom, List<Long> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) {
            return;
        }
        notificationDao.saveNotification("notification.newLesson", courseName + "|" + classroom, recipientIds);
    }

    /**
     * Notifies recipients that a lesson has been updated.
     * The message template key is {@code notification.courseChanged}; params are
     * {@code courseName|classroom}.
     *
     * @param courseName   name of the course
     * @param classroom    updated classroom identifier
     * @param recipientIds IDs of users to notify
     */
    public void notifyLessonUpdated(String courseName, String classroom, List<Long> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) {
            return;
        }
        notificationDao.saveNotification("notification.courseChanged", courseName + "|" + classroom, recipientIds);
    }

    /**
     * Notifies recipients that a lesson has been cancelled.
     * The message template key is {@code notification.lessonCancelled}; the param is
     * the lesson ID as a string.
     *
     * @param lessonId     the ID of the cancelled lesson
     * @param recipientIds IDs of users to notify
     */
    public void notifyLessonDeleted(Long lessonId, List<Long> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) {
            return;
        }
        notificationDao.saveNotification("notification.lessonCancelled", lessonId.toString(), recipientIds);
    }

    /**
     * Notifies a student that they have been added to a group.
     * The message template key is {@code notification.groupAdded}; the param is the group code.
     *
     * @param groupCode   the group's code
     * @param recipientId the student's user ID
     */
    public void notifyStudentAddedToGroup(String groupCode, Long recipientId) {
        if (recipientId == null) {
            return;
        }
        notificationDao.saveNotification("notification.groupAdded", groupCode, List.of(recipientId));
    }

    /**
     * Notifies a student that they have been removed from a group.
     * The message template key is {@code notification.groupRemoved}; the param is the group code.
     *
     * @param groupCode   the group's code
     * @param recipientId the student's user ID
     */
    public void notifyStudentRemovedFromGroup(String groupCode, Long recipientId) {
        if (recipientId == null) {
            return;
        }
        notificationDao.saveNotification("notification.groupRemoved", groupCode, List.of(recipientId));
    }

    /**
     * Notifies a user that they have received a new chat message.
     * The message template key is {@code notification.newMessage}; the param is
     * the sender's first name.
     * Does nothing if the sender and recipient are the same user.
     *
     * @param senderId    the sender's user ID (used to prevent self-notification)
     * @param senderName  the sender's first name, used as the format argument
     * @param recipientId the recipient's user ID
     */
    public void notifyNewMessage(Long senderId, String senderName, Long recipientId) {
        if (recipientId == null || recipientId.equals(senderId)) {
            return;
        }
        notificationDao.saveNotification("notification.newMessage", senderName, List.of(recipientId));
    }

    /**
     * Returns all raw notification receiver records for the given user.
     *
     * @param userId the user's ID
     * @return list of {@link NotificationReceiver} records, possibly empty
     */
    public List<NotificationReceiver> getNotificationsForUser(Long userId) {
        return notificationDao.findByUserId(userId);
    }

    /**
     * Returns notifications for the given user as DTOs, ready for display in the UI.
     * The content is already resolved to the JVM default locale by the DAO.
     *
     * @param userId the user's ID
     * @return list of {@link NotificationDto} objects ordered newest-first, possibly empty
     */
    public List<NotificationDto> getNotificationDtosForUser(Long userId) {
        return notificationDao.findTranslatedForUser(userId).stream()
                .filter(row -> row[1] != null && !((String) row[1]).isBlank())
                .map(row -> new NotificationDto(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        row[2] instanceof Timestamp ts ? ts.toLocalDateTime() : (LocalDateTime) row[2],
                        (Boolean) row[3]
                ))
                .toList();
    }

    /**
     * Marks a single notification as read for the given user.
     *
     * @param userId         the recipient's user ID
     * @param notificationId the notification's primary key
     */
    public void markAsRead(Long userId, Long notificationId) {
        notificationDao.markAsRead(userId, notificationId);
    }

    /**
     * Marks all notifications as read for the given user.
     *
     * @param userId the recipient's user ID
     */
    public void markAllAsRead(Long userId) {
        notificationDao.markAllAsRead(userId);
    }

    /**
     * Returns the number of unread notifications for the given user.
     *
     * @param userId the recipient's user ID
     * @return unread notification count (0 if none)
     */
    public long getUnreadCount(Long userId) {
        return notificationDao.countUnread(userId);
    }

}
