package dto;

import java.time.LocalDateTime;

public class NotificationDto {

    private final Long notificationId;
    private final String content;
    private final LocalDateTime sentAt;
    private final boolean isRead;

    public NotificationDto(Long notificationId, String content, LocalDateTime sentAt, boolean isRead) {
        this.notificationId = notificationId;
        this.content = content;
        this.sentAt = sentAt;
        this.isRead = isRead;
    }

    public Long getNotificationId() { return notificationId; }
    public String getContent()      { return content; }
    public LocalDateTime getSentAt(){ return sentAt; }
    public boolean isRead()         { return isRead; }
}
