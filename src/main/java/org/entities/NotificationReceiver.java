package org.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "notification_receivers")
public class NotificationReceiver {

    @Embeddable
    public static class NotificationReceiverId implements Serializable {

        @Column(name = "user_id")
        private Long userId;

        @Column(name = "notification_id")
        private Long notificationId;

        public NotificationReceiverId() {}

        public NotificationReceiverId(Long userId, Long notificationId) {
            this.userId = userId;
            this.notificationId = notificationId;
        }

        public Long getUserId() { return userId; }
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

    public NotificationReceiver() {}

    public NotificationReceiver(User user, Notification notification) {
        this.user = user;
        this.notification = notification;
        this.id = new NotificationReceiverId(user.getId(), notification.getId());
        this.isRead = false;
    }

    public NotificationReceiverId getId() { return id; }
    public void setId(NotificationReceiverId id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Notification getNotification() { return notification; }
    public void setNotification(Notification notification) { this.notification = notification; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
