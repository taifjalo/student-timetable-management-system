// dao. This folder contains the code for accessing the database.

package org.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.entities.User;
import org.datasource.TimetableConnection;

import java.util.List;

public class UserDao {

    /*
    private EntityManager em;

    // Dependency Injectio
    public UserDao(EntityManager em) {
        this.em = em;
    }
     */
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

    public List<User> findUserByNameSurname(String name1, String name2) {
        try(EntityManager em = TimetableConnection.getEntityManager()){
            return em.createQuery("SELECT u FROM User u WHERE (u.firstName =:name1 and u.sureName = :name2) OR (u.firstName =:name2 and u.sureName = :name1)", User.class)
                    .setParameter("name1", name1)
                    .setParameter("name2", name2)
                    .getResultList();
        }
    }
}

