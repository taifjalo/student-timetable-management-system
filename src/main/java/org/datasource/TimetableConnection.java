// datasource. This folder contains the code for connecting to the database.

package org.datasource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Central factory for JPA {@link EntityManager} instances.
 *
 * <p>A single {@link EntityManagerFactory} is created at class-load time from
 * the {@code TimetableUnit} persistence unit defined in
 * {@code META-INF/persistence.xml}. All DAOs obtain a fresh
 * {@link EntityManager} via {@link #createEntityManager()} and close it
 * immediately after use with try-with-resources.
 *
 * <p>This class is not instantiable.
 */
public final class TimetableConnection {

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("TimetableUnit");

    private TimetableConnection() {
    }

    /**
     * Creates and returns a new {@link EntityManager}.
     * Callers are responsible for closing the returned instance, preferably
     * with a try-with-resources block.
     *
     * @return a new {@link EntityManager} backed by the application's persistence unit
     */
    public static EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Closes the shared {@link EntityManagerFactory}, releasing all pooled
     * database connections. Should be called on application shutdown.
     */
    public static void shutdown() {
        if (emf.isOpen()) {
            emf.close();
        }
    }

}
