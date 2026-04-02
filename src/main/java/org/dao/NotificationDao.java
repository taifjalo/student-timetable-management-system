package org.dao;

import jakarta.persistence.EntityManager;
import org.datasource.TimetableConnection;
import org.entities.Notification;
import org.entities.NotificationReceiver;
import org.entities.User;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationDao {

    public Notification saveNotification(String messageKey, String messageParams, List<Long> recipientIds) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();

            Notification notification = new Notification(LocalDateTime.now(), messageKey, messageParams);
            em.persist(notification);
            em.flush(); // ensure notification_id is generated before creating receivers

            for (Long recipientId : recipientIds) {
                User user = em.getReference(User.class, recipientId);
                em.persist(new NotificationReceiver(user, notification));
            }

            em.getTransaction().commit();
            return notification;
        }
    }

    public List<NotificationReceiver> findByUserId(Long userId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery(
                "SELECT nr FROM NotificationReceiver nr " +
                "JOIN FETCH nr.notification n " +
                "WHERE nr.id.userId = :userId " +
                "ORDER BY n.sentAt DESC",
                NotificationReceiver.class
            ).setParameter("userId", userId).getResultList();
        }
    }

    public void markAsRead(Long userId, Long notificationId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery(
                "UPDATE NotificationReceiver nr SET nr.isRead = true " +
                "WHERE nr.id.userId = :userId AND nr.id.notificationId = :notifId"
            ).setParameter("userId", userId)
             .setParameter("notifId", notificationId)
             .executeUpdate();
            em.getTransaction().commit();
        }
    }

    public void markAllAsRead(Long userId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery(
                "UPDATE NotificationReceiver nr SET nr.isRead = true " +
                "WHERE nr.id.userId = :userId"
            ).setParameter("userId", userId).executeUpdate();
            em.getTransaction().commit();
        }
    }

    public long countUnread(Long userId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return (Long) em.createQuery(
                "SELECT COUNT(nr) FROM NotificationReceiver nr " +
                "WHERE nr.id.userId = :userId AND nr.isRead = false"
            ).setParameter("userId", userId).getSingleResult();
        }
    }
}
