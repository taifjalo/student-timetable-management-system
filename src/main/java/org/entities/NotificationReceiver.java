package org.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * JPA entity representing the many-to-many relationship between
 * {@link Notification} and {@link User} (who receives it).
 * Maps to the {@code notification_receivers} table.
 *
 * <p>The composite primary key is {@link NotificationReceiverId} — a pair of
 * {@code (userId, notificationId)}.
 */
@Entity
@Table(name = "notification_receivers")
public class NotificationReceiver {

    /**
     * Composite primary key class for {@link NotificationReceiver}.
     * Combines {@code userId} and {@code notificationId}.
     */
    @Embeddable
    public static class NotificationReceiverId implements Serializable {

        @Column(name = "user_id")
        private Long userId;

        @Column(name = "notification_id")
        private Long notificationId;

        /** Required no-arg constructor for JPA. */
        public NotificationReceiverId() {}

        /**
         * Creates the composite key.
         *
         * @param userId         the recipient user's ID
         * @param notificationId the notification's ID
         */
        public NotificationReceiverId(Long userId, Long notificationId) {
            this.userId = userId;
            this.notificationId = notificationId;
        }

        /** Returns the recipient user ID component of the key. */
        public Long getUserId() { return userId; }
        /** Returns the notification ID component of the key. */
        public Long getNotificationId() { return notificationId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NotificationReceiverId)) return false;
            NotificationReceiverId that = (NotificationReceiverId) o;
            return Objects.equals(userId, that.userId) && Objects.equals(notificationId, that.notificationId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, notificationId);
        }
    }

    @EmbeddedId
    private NotificationReceiverId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("notificationId")
    @JoinColumn(name = "notification_id")
    private Notification notification;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    /** Required no-arg constructor for JPA. */
    public NotificationReceiver() {}

    /**
     * Creates a new unread receiver row linking a user to a notification.
     * The composite key is derived from the supplied objects' IDs.
     *
     * @param user         the recipient
     * @param notification the notification being delivered
     */
    public NotificationReceiver(User user, Notification notification) {
        this.user = user;
        this.notification = notification;
        this.id = new NotificationReceiverId(user.getId(), notification.getId());
        this.isRead = false;
    }

    /** Returns the composite primary key. */
    public NotificationReceiverId getId() { return id; }
    /** Sets the composite primary key (used by JPA; do not call manually). */
    public void setId(NotificationReceiverId id) { this.id = id; }

    /** Returns the recipient user. */
    public User getUser() { return user; }
    /** Sets the recipient user. */
    public void setUser(User user) { this.user = user; }

    /** Returns the associated notification. */
    public Notification getNotification() { return notification; }
    /** Sets the associated notification. */
    public void setNotification(Notification notification) { this.notification = notification; }

    /** Returns {@code true} if the recipient has read this notification. */
    public boolean isRead() { return isRead; }
    /** Marks the notification as read or unread for this recipient. */
    public void setRead(boolean read) { isRead = read; }
}
