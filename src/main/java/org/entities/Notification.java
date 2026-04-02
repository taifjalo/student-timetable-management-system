package org.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a system notification.
 * Maps to the {@code notifications} table.
 *
 * <p>Notifications are stored in two modes:
 * <ul>
 *   <li><b>Legacy / plain-text:</b> only {@code content} is set; {@code messageKey}
 *       and {@code messageParams} are {@code null}.</li>
 *   <li><b>Localized:</b> {@code messageKey} holds an i18n bundle key (e.g.
 *       {@code "notification.newLesson"}), and {@code messageParams} holds pipe-separated
 *       format arguments (e.g. {@code "Math|A101"}) for {@link String#format}.</li>
 * </ul>
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

    /** Plain-text fallback content used for legacy rows that have no message key. */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** i18n resource bundle key used to look up the localized message template. */
    @Column(name = "message_key")
    private String messageKey;

    /**
     * Pipe-separated format arguments matching the {@code %s} placeholders in the
     * bundle string for {@code messageKey} (e.g. {@code "Math|A101"}).
     */
    @Column(name = "message_params")
    private String messageParams;

    /** Required no-arg constructor for JPA. */
    public Notification() {}

    /**
     * Creates a legacy plain-text notification.
     *
     * @param sentAt  timestamp the notification was created
     * @param content plain-text message body
     */
    public Notification(LocalDateTime sentAt, String content) {
        this.sentAt = sentAt;
        this.content = content;
    }

    /**
     * Creates a fully localized notification.
     *
     * @param sentAt        timestamp the notification was created
     * @param messageKey    i18n bundle key for the message template
     * @param messageParams pipe-separated format arguments (may be {@code null})
     */
    public Notification(LocalDateTime sentAt, String messageKey, String messageParams) {
        this.sentAt = sentAt;
        this.messageKey = messageKey;
        this.messageParams = messageParams;
    }

    /** Returns the surrogate primary key. */
    public Long getId() { return id; }
    /** Sets the surrogate primary key (used by JPA; do not call manually). */
    public void setId(Long id) { this.id = id; }

    /** Returns the timestamp this notification was sent. */
    public LocalDateTime getSentAt() { return sentAt; }
    /** Sets the sent timestamp. */
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    /** Returns the plain-text content (legacy rows only), or {@code null} for localized rows. */
    public String getContent() { return content; }
    /** Sets the plain-text content. */
    public void setContent(String content) { this.content = content; }

    /** Returns the i18n bundle key, or {@code null} for legacy rows. */
    public String getMessageKey() { return messageKey; }
    /** Sets the i18n bundle key. */
    public void setMessageKey(String messageKey) { this.messageKey = messageKey; }

    /** Returns the pipe-separated format arguments, or {@code null} if none. */
    public String getMessageParams() { return messageParams; }
    /** Sets the pipe-separated format arguments. */
    public void setMessageParams(String messageParams) { this.messageParams = messageParams; }
}
