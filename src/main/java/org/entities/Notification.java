package org.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "message_key")
    private String messageKey;

    // pipe-separated args matching the bundle key's %s placeholders, e.g. "CS101|Room 202"
    @Column(name = "message_params")
    private String messageParams;

    public Notification() {}

    public Notification(LocalDateTime sentAt, String content) {
        this.sentAt = sentAt;
        this.content = content;
    }

    public Notification(LocalDateTime sentAt, String messageKey, String messageParams) {
        this.sentAt = sentAt;
        this.messageKey = messageKey;
        this.messageParams = messageParams;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessageKey() { return messageKey; }
    public void setMessageKey(String messageKey) { this.messageKey = messageKey; }

    public String getMessageParams() { return messageParams; }
    public void setMessageParams(String messageParams) { this.messageParams = messageParams; }
}
