package dto;

import java.time.LocalDateTime;

public class NotificationDto {

    private final Long notificationId;
    private final String messageKey;    // null for old rows — fall back to content
    private final String messageParams;
    private final String content;
    private final LocalDateTime sentAt;
    private final boolean isRead;

    public NotificationDto(Long notificationId, String messageKey, String messageParams,
                           String content, LocalDateTime sentAt, boolean isRead) {
        this.notificationId = notificationId;
        this.messageKey     = messageKey;
        this.messageParams  = messageParams;
        this.content        = content;
        this.sentAt         = sentAt;
        this.isRead         = isRead;
    }

    public Long getNotificationId()  { return notificationId; }
    public String getMessageKey()    { return messageKey; }
    public String getMessageParams() { return messageParams; }
    public String getContent()       { return content; }
    public LocalDateTime getSentAt() { return sentAt; }
    public boolean isRead()          { return isRead; }
}
