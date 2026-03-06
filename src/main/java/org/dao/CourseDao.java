package org.dao;

import jakarta.persistence.EntityManager;
import org.datasource.TimetableConnection;
import org.entities.Course;

import java.util.List;

public class CourseDao {

    public Course save(Course course) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            Course saved = em.merge(course);
            em.getTransaction().commit();
            return saved;
        }
    }

    public Course findById(Long id) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.find(Course.class, id);
        }
    }

    public List<Course> findAll() {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery("SELECT c FROM Course c ORDER BY c.name ASC", Course.class)
                    .getResultList();
        }
    }

    public void delete(Long id) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            Course course = em.find(Course.class, id);
            if (course != null) {
                em.remove(course);
            }
            em.getTransaction().commit();
        }
    }
}

