// entity. This folder contains the entity classes. The entity classes are annotated with JPA annotations.
package org.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a direct message between two users.
 * Maps to the {@code messages} table.
 * Messages are sorted naturally by {@code sentAt} via the {@link Comparable} implementation.
 */
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

    /** Required no-arg constructor for JPA. */
    public Message() {}

    /**
     * Creates a new unread message.
     *
     * @param sentAt        timestamp the message was sent
     * @param content       message body text
     * @param senderUser    the user who sent the message
     * @param recipientUser the user who receives the message
     */
    public Message(LocalDateTime sentAt, String content, User senderUser, User recipientUser) {
        this.sentAt = sentAt;
        this.content = content;
        this.senderUser= senderUser;
        this.recipientUser = recipientUser;
        this.read = false;
    }

    /** Returns the surrogate primary key. */
    public Long getId() { return id; }
    /** Returns the timestamp this message was sent. */
    public LocalDateTime getSentAt() { return sentAt; }
    /** Sets the sent timestamp. */
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    /** Returns the message text content. */
    public String getContent() { return content; }
    /** Sets the message text content. */
    public void setContent(String content) { this.content = content; }

    /** Returns {@code true} if the recipient has read this message. */
    public boolean isRead() { return read; }
    /** Marks the message as read or unread. */
    public void setRead(boolean read) { this.read = read; }

    /** Returns the user who sent this message. */
    public User getSenderUser() { return senderUser; }
    /** Sets the sender. */
    public void setSenderUser(User senderUser) { this.senderUser = senderUser; }

    /** Returns the user who should receive this message. */
    public User getRecipientUser() { return recipientUser; }
    /** Sets the recipient. */
    public void setRecipientUser(User recipientUser) { this.recipientUser = recipientUser; }

    /**
     * Compares messages chronologically by {@code sentAt}.
     * Used by {@link java.util.SortedList} in the chat view to keep messages in order.
     */
    @Override
    public int compareTo(Message other){
        return this.sentAt.compareTo(other.sentAt);
    }
}
