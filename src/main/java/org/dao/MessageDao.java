// dao. This folder contains the code for accessing the database.

package org.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.datasource.TimetableConnection;
import org.entities.Message;
import org.entities.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.LongFunction;

public class MessageDao {


    public List<Message> findUserMessages (Long userId){
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery("SELECT m FROM Message m WHERE m.senderUser.id = :userId or m.recipientUser.id = :userId ORDER BY m.sentAt DESC ", Message.class)
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }

    public List<Message> findMessagesBetweenUsers(Long userId1, Long userId2){
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery("select m FROM Message m WHERE (m.senderUser.id = :userId1 and m.recipientUser.id = :userId2) OR (m.senderUser.id = :userId2 and m.recipientUser.id = :userId1) ORDER BY m.id ASC", Message.class)
                    .setParameter("userId1", userId1)
                    .setParameter("userId2", userId2)
                    .getResultList();
        }
    }

    public List<Message> findNewMessagesBetweenUsers(Long userId1, Long userId2, Long lastMessageId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery("select m FROM Message m WHERE ((m.senderUser.id = :userId1 and m.recipientUser.id = :userId2) OR (m.senderUser.id = :userId2 and m.recipientUser.id = :userId1)) AND m.id > :lastMessageId ORDER BY m.id ASC", Message.class)
                    .setParameter("userId1", userId1)
                    .setParameter("userId2", userId2)
                    .setParameter("lastMessageId", lastMessageId)
                    .getResultList();
        }
    }

    public void saveMessage(Long senderId, Long recipientId, String text) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();

            LocalDateTime sentAt = LocalDateTime.now();
            String content = text;
            User sender = em.getReference(User.class, senderId);
            User recipient = em.getReference(User.class, recipientId);

            Message message = new Message (
                    sentAt,
                    content,
                    sender,
                    recipient
            );

            em.persist(message);
            em.getTransaction().commit();
        }
    }

}