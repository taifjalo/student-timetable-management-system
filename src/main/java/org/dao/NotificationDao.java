package org.dao;

import jakarta.persistence.EntityManager;
import org.datasource.TimetableConnection;
import org.entities.Notification;
import org.entities.NotificationReceiver;
import org.entities.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data-access object for {@link Notification} and {@link NotificationReceiver} entities.
 * Handles creating, querying, and marking notifications as read.
 */
public class NotificationDao {

    /**
     * Creates a new localized notification and delivers it to all specified recipients.
     * The notification and its receiver rows are written in a single transaction.
     * An explicit {@code flush()} is called after persisting the notification to ensure
     * the generated ID is available before the receiver rows are created.
     *
     * @param messageKey    i18n bundle key for the notification message template
     * @param messageParams pipe-separated format arguments matching the bundle key's
     *                      {@code %s} placeholders (may be {@code null})
     * @param recipientIds  IDs of users who should receive this notification
     * @return the persisted {@link Notification} with its generated ID set
     */
    public Notification saveNotification(String messageKey, String messageParams, List<Long> recipientIds) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();

            Notification notification = new Notification(LocalDateTime.now(), messageKey, messageParams);
            em.persist(notification);
            em.flush(); // ensure notification_id is generated before creating receivers

            for (Long recipientId : recipientIds) {
                User user = em.getReference(User.class, recipientId);
                em.persist(new NotificationReceiver(user, notification));
            }

            em.getTransaction().commit();
            return notification;
        }
    }

    /**
     * Returns all notification receiver rows for the given user, ordered by
     * notification date descending (newest first). The associated {@link Notification}
     * is eagerly fetched to avoid lazy-load issues after the session closes.
     *
     * @param userId the recipient's user ID
     * @return list of receiver records, possibly empty
     */
    public List<NotificationReceiver> findByUserId(Long userId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery(
                "SELECT nr FROM NotificationReceiver nr " +
                "JOIN FETCH nr.notification n " +
                "WHERE nr.id.userId = :userId " +
                "ORDER BY n.sentAt DESC",
                NotificationReceiver.class
            ).setParameter("userId", userId).getResultList();
        }
    }

    /**
     * Marks a single notification as read for the specified user.
     *
     * @param userId         the recipient's user ID
     * @param notificationId the notification's primary key
     */
    public void markAsRead(Long userId, Long notificationId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery(
                "UPDATE NotificationReceiver nr SET nr.isRead = true " +
                "WHERE nr.id.userId = :userId AND nr.id.notificationId = :notifId"
            ).setParameter("userId", userId)
             .setParameter("notifId", notificationId)
             .executeUpdate();
            em.getTransaction().commit();
        }
    }

    /**
     * Marks all notifications as read for the specified user.
     *
     * @param userId the recipient's user ID
     */
    public void markAllAsRead(Long userId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery(
                "UPDATE NotificationReceiver nr SET nr.isRead = true " +
                "WHERE nr.id.userId = :userId"
            ).setParameter("userId", userId).executeUpdate();
            em.getTransaction().commit();
        }
    }

    /**
     * Returns the number of unread notifications for the specified user.
     *
     * @param userId the recipient's user ID
     * @return count of unread notifications (0 if none)
     */
    public long countUnread(Long userId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return (Long) em.createQuery(
                "SELECT COUNT(nr) FROM NotificationReceiver nr " +
                "WHERE nr.id.userId = :userId AND nr.isRead = false"
            ).setParameter("userId", userId).getSingleResult();
        }
    }
}
