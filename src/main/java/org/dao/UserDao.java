// dao. This folder contains the code for accessing the database.

package org.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.entities.User;
import org.datasource.TimetableConnection;

import java.util.List;

/**
 * Data-access object for {@link User} entities.
 * All methods open a new {@link EntityManager} per call and close it via
 * try-with-resources, so callers do not need to manage the persistence context.
 */
public class UserDao {

    /**
     * Persists a new user to the database.
     * Rolls back the transaction and rethrows if any error occurs.
     *
     * @param user the transient user to persist; its {@code id} will be
     *             populated by the database after a successful commit
     * @throws RuntimeException if the database operation fails (e.g. unique constraint violation)
     */
    public void save(User user) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.persist(user);
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    /**
     * Looks up a user by their unique login username.
     *
     * @param username the username to search for
     * @return the matching {@link User}, or {@code null} if none exists
     */
    public User findByUsername(String username) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username",
                            User.class)
                    .setParameter("username", username)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * Finds users whose first name and surname match the given pair in either order.
     *
     * @param name1 first search token (matched against first name or surname)
     * @param name2 second search token (matched against the remaining name field)
     * @return list of matching users, possibly empty
     */
    public List<User> findUserByNameSurname(String name1, String name2) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            String q = "SELECT u FROM User u WHERE "
                    + "(u.firstName =:name1 and u.sureName = :name2) "
                    + "OR (u.firstName =:name2 and u.sureName = :name1)";
            return em.createQuery(q, User.class)
                    .setParameter("name1", name1)
                    .setParameter("name2", name2)
                    .getResultList();
        }
    }

    /**
     * Returns all users ordered alphabetically by first name then surname.
     *
     * @return list of all users, possibly empty
     */
    public List<User> findAll() {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery(
                    "SELECT u FROM User u ORDER BY u.firstName ASC, u.sureName ASC",
                    User.class
            ).getResultList();
        }
    }

    /**
     * Searches users by name using a case-insensitive LIKE query against first name,
     * surname, and the concatenated full name.
     *
     * @param query search string; surrounding {@code %} wildcards are added automatically
     * @return users whose name matches the query, ordered alphabetically
     */
    public List<User> searchByName(String query) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            String pattern = "%" + query.toLowerCase(java.util.Locale.ROOT) + "%";
            return em.createQuery(
                    "SELECT u FROM User u WHERE LOWER(u.firstName) LIKE :q "
                    + "OR LOWER(u.sureName) LIKE :q "
                    + "OR LOWER(CONCAT(u.firstName, ' ', u.sureName)) LIKE :q "
                    + "ORDER BY u.firstName ASC, u.sureName ASC",
                    User.class
            ).setParameter("q", pattern).getResultList();
        }
    }

    /**
     * Merges (updates) an existing user record in the database.
     *
     * @param user the detached user with updated fields
     * @return the managed, merged user instance
     */
    public User update(User user) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            User merged = em.merge(user);
            em.getTransaction().commit();
            return merged;
        }
    }

}
