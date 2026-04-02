package dto;

import java.time.LocalDateTime;

/**
 * Read-only data transfer object that carries notification data from the service
 * layer to the UI without exposing JPA entities.
 *
 * <p>Supports both legacy plain-text notifications (where {@code messageKey} is
 * {@code null} and {@code content} holds the full text) and the new localized
 * format (where {@code messageKey} is an i18n bundle key and {@code messageParams}
 * holds pipe-separated format arguments).
 */
public class NotificationDto {

    private final Long notificationId;
    private final String messageKey;    // null for old rows — fall back to content
    private final String messageParams;
    private final String content;
    private final LocalDateTime sentAt;
    private final boolean isRead;

    /**
     * Creates a notification DTO.
     *
     * @param notificationId the notification's primary key
     * @param messageKey     i18n bundle key, or {@code null} for legacy rows
     * @param messageParams  pipe-separated format arguments, or {@code null}
     * @param content        plain-text fallback content (used when {@code messageKey} is null)
     * @param sentAt         timestamp the notification was sent
     * @param isRead         whether the recipient has read this notification
     */
    public NotificationDto(Long notificationId, String messageKey, String messageParams,
                           String content, LocalDateTime sentAt, boolean isRead) {
        this.notificationId = notificationId;
        this.messageKey     = messageKey;
        this.messageParams  = messageParams;
        this.content        = content;
        this.sentAt         = sentAt;
        this.isRead         = isRead;
    }

    /** Returns the notification's primary key. */
    public Long getNotificationId()  { return notificationId; }
    /** Returns the i18n bundle key, or {@code null} for legacy rows. */
    public String getMessageKey()    { return messageKey; }
    /** Returns the pipe-separated format arguments, or {@code null}. */
    public String getMessageParams() { return messageParams; }
    /** Returns the plain-text content used as a fallback for legacy rows. */
    public String getContent()       { return content; }
    /** Returns the timestamp this notification was sent. */
    public LocalDateTime getSentAt() { return sentAt; }
    /** Returns {@code true} if the recipient has read this notification. */
    public boolean isRead()          { return isRead; }
}
