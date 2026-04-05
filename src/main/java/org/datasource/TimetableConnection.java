// datasource. This folder contains the code for connecting to the database.

package org.datasource;
import jakarta.persistence.*;


public class TimetableConnection {

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("TimetableUnit");

    private TimetableConnection () {}

    public static EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public static void shutdown() {
        if (emf.isOpen()) emf.close();
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

}
