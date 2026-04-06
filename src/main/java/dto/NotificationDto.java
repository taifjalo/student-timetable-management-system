package dto;

import java.time.LocalDateTime;

/**
 * Read-only data transfer object that carries notification data from the service
 * layer to the UI without exposing JPA entities.
 *
 * <p>The display text ({@code content}) is already resolved to the recipient's
 * locale by the time this DTO is constructed — the DAO pre-renders translations
 * for all supported locales at write time and returns the appropriate one at
 * query time.
 */
public class NotificationDto {

    private final Long notificationId;
    private final String content;
    private final LocalDateTime sentAt;
    private final boolean isRead;

    /**
     * Creates a notification DTO.
     *
     * @param notificationId the notification's primary key
     * @param content        locale-resolved display text
     * @param sentAt         timestamp the notification was sent
     * @param isRead         whether the recipient has read this notification
     */
    public NotificationDto(Long notificationId, String content, LocalDateTime sentAt, boolean isRead) {
        this.notificationId = notificationId;
        this.content        = content;
        this.sentAt         = sentAt;
        this.isRead         = isRead;
    }

    /** Returns the notification's primary key. */
    public Long getNotificationId()  { return notificationId; }
    /** Returns the locale-resolved display text. */
    public String getContent()       { return content; }
    /** Returns the timestamp this notification was sent. */
    public LocalDateTime getSentAt() { return sentAt; }
    /** Returns {@code true} if the recipient has read this notification. */
    public boolean isRead()          { return isRead; }
}
