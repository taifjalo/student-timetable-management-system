package org.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private int id;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "sender_user_id", nullable = false)
    private Long senderUserId;

    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    protected Message() {}

    public Message(LocalDateTime sentAt, String content, long senderUserId, long recipientUserId) {
        this.sentAt = sentAt;
        this.content = content;
        this.senderUserId = senderUserId;
        this.recipientUserId = recipientUserId;
        this.read = false;
    }

    public int getId() { return id; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Long getSenderUserId() { return senderUserId; }
    public void setSenderUserId(Long senderUserId) { this.senderUserId = senderUserId; }

    public Long getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(Long recipientUserId) { this.recipientUserId = recipientUserId; }
}