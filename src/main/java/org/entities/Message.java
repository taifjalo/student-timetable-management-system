// entity. This folder contains the entity classes. The entity classes are annotated with JPA annotations.
package org.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message implements Comparable<Message>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @ManyToOne
    @JoinColumn(name = "sender_user_id", nullable = false)
    private User senderUser;

    @ManyToOne
    @JoinColumn(name = "recipient_user_id")
    private User recipientUser;

    public Message() {}

    public Message(LocalDateTime sentAt, String content, User senderUser, User recipientUser) {
        this.sentAt = sentAt;
        this.content = content;
        this.senderUser= senderUser;
        this.recipientUser = recipientUser;
        this.read = false;
    }

    public Long getId() { return id; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public User getSenderUser() { return senderUser; }
    public void setSenderUserId(int senderUserId) { this.senderUser = senderUser; }

    public User getRecipientUser() { return recipientUser; }
    public void setRecipientUserId(int recipientUserId) { this.recipientUser = recipientUser; }

    @Override
    public int compareTo(Message other){
        return this.sentAt.compareTo(other.sentAt);
    }
}