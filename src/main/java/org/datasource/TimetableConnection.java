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

}
