// dao. This folder contains the code for accessing the database.

package org.dao;

import jakarta.persistence.EntityManager;
import org.datasource.TimetableConnection;
import org.entities.Message;
import org.entities.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data-access object for {@link Message} entities.
 * Handles persisting, querying, and marking direct messages as read.
 */
public class MessageDao {

    /**
     * Returns all messages where the given user is either the sender or the recipient,
     * ordered by {@code sentAt} descending (most recent first).
     *
     * @param userId the user whose messages to fetch
     * @return list of messages involving the user, possibly empty
     */
    public List<Message> findUserMessages(Long userId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery("SELECT m FROM Message m WHERE m.senderUser.id = :userId or m.recipientUser.id = :userId ORDER BY m.sentAt DESC ", Message.class)
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }

    /**
     * Returns all messages exchanged between two specific users, ordered by ID ascending
     * (i.e. chronological order).
     *
     * @param userId1 one participant's ID
     * @param userId2 the other participant's ID
     * @return list of messages in the conversation, possibly empty
     */
    public List<Message> findMessagesBetweenUsers(Long userId1, Long userId2) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery("select m FROM Message m WHERE (m.senderUser.id = :userId1 and m.recipientUser.id = :userId2) OR (m.senderUser.id = :userId2 and m.recipientUser.id = :userId1) ORDER BY m.id ASC", Message.class)
                    .setParameter("userId1", userId1)
                    .setParameter("userId2", userId2)
                    .getResultList();
        }
    }

    /**
     * Returns messages exchanged between two users that arrived after a given message ID.
     * Used by the polling loop to fetch only new messages since the last known one.
     *
     * @param userId1       one participant's ID
     * @param userId2       the other participant's ID
     * @param lastMessageId the ID of the last message already loaded by the client
     * @return new messages with {@code id > lastMessageId}, ordered ascending
     */
    public List<Message> findNewMessagesBetweenUsers(Long userId1, Long userId2, Long lastMessageId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery("select m FROM Message m WHERE ((m.senderUser.id = :userId1 and m.recipientUser.id = :userId2) OR (m.senderUser.id = :userId2 and m.recipientUser.id = :userId1)) AND m.id > :lastMessageId ORDER BY m.id ASC", Message.class)
                    .setParameter("userId1", userId1)
                    .setParameter("userId2", userId2)
                    .setParameter("lastMessageId", lastMessageId)
                    .getResultList();
        }
    }

    /**
     * Persists a new message from {@code senderId} to {@code recipientId}.
     * Uses {@link EntityManager#getReference} to avoid loading full entities.
     *
     * @param senderId    the sender's user ID
     * @param recipientId the recipient's user ID
     * @param text        the message body text
     */
    public void saveMessage(Long senderId, Long recipientId, String text) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            User sender = em.getReference(User.class, senderId);
            User recipient = em.getReference(User.class, recipientId);
            em.persist(new Message(LocalDateTime.now(), text, sender, recipient));
            em.getTransaction().commit();
        }
    }

    /**
     * Marks all unread messages sent by {@code otherUserId} to {@code currentUserId} as read.
     * Executed as a bulk JPQL UPDATE for efficiency.
     *
     * @param currentUserId the recipient whose inbox is being cleared
     * @param otherUserId   the sender whose messages are being marked as read
     */
    public void markMessagesAsRead(Long currentUserId, Long otherUserId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery(" UPDATE Message m SET m.read = true WHERE m.recipientUser.id = :currentUserId AND m.senderUser.id = :otherUserId AND m.read = false ")
                    .setParameter("currentUserId", currentUserId)
                    .setParameter("otherUserId", otherUserId)
                    .executeUpdate();
            em.getTransaction().commit();
        }
    }

}
