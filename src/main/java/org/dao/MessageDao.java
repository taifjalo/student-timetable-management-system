// dao. This folder contains the code for accessing the database.

package org.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.datasource.TimetableConnection;
import org.entities.Message;

import java.util.List;

public class MessageDao {

    public Message find(long id) {
        EntityManager em = TimetableConnection.createEntityManager();
        try {
            return em.find(Message.class, id);
        } finally {
            em.close();
        }
    }

    public List<Message> findAll() {
        EntityManager em = TimetableConnection.createEntityManager();
        try {
            return em.createQuery("SELECT m FROM Message m ORDER BY m.sentAt DESC", Message.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void create(Message message) {
        EntityManager em = TimetableConnection.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(message);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Message update(Message message) {
        EntityManager em = TimetableConnection.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Message managed = em.merge(message);
            tx.commit();
            return managed;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean delete(long id) {
        EntityManager em = TimetableConnection.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Message found = em.find(Message.class, id);
            if (found == null) {
                tx.rollback();
                return false;
            }
            em.remove(found);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}