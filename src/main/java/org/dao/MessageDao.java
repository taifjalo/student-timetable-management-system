// dao. This folder contains the code for accessing the database.

package org.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.datasource.TimetableConnection;
import org.entities.Message;
import org.entities.User;

import java.util.List;

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
            return em.createQuery("select m FROM Message m WHERE (m.senderUser.id = :userId1 and m.recipientUser.id = :userId2) OR (m.senderUser.id = :userId2 and m.recipientUser.id = :userId1) ORDER BY m.sentAt ASC", Message.class)
                    .setParameter("userId1", userId1)
                    .setParameter("userId2", userId2)
                    .getResultList();
        }
    }

}