// dao. This folder contains the code for accessing the database.

package org.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.entities.User;
import org.datasource.TimetableConnection;

public class UserDao {

    public void save(User user) {
        EntityManager em = TimetableConnection.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(user);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public User findByUsername(String username) {
        EntityManager em = TimetableConnection.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username",
                            User.class)
                    .setParameter("username", username)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }
}

