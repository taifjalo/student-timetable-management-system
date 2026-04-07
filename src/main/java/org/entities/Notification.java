package org.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * JPA entity representing a system notification.
 * Maps to the {@code notifications} table.
 *
 * <p>The display text for each locale is stored in a separate
 * {@code notification_translations} table, rendered at creation time
 * from an i18n bundle key and its arguments.
 *
 * <p>Recipients are linked via {@link NotificationReceiver}.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    /** Required no-arg constructor for JPA. */
    public Notification() {
    }

    /**
     * Creates a notification with the given timestamp.
     *
     * @param sentAt timestamp the notification was created
     */
    public Notification(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    /** Returns the surrogate primary key. */
    public Long getId() {
        return id;
    }

    /** Sets the surrogate primary key (used by JPA; do not call manually). */
    public void setId(Long id) {
        this.id = id;
    }

    /** Returns the timestamp this notification was sent. */
    public LocalDateTime getSentAt() {
        return sentAt;
    }

    /** Sets the sent timestamp. */
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
