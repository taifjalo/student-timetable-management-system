package org.dao;

import jakarta.persistence.EntityManager;
import org.datasource.TimetableConnection;
import org.entities.StudentGroup;
import org.entities.StudentProfile;
import org.entities.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class GroupDao {

    public StudentGroup save(StudentGroup group) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            StudentGroup saved = em.merge(group);
            em.getTransaction().commit();
            return saved;
        }
    }

    public StudentGroup findByCode(String groupCode) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.find(StudentGroup.class, groupCode);
        }
    }

    public List<StudentGroup> findAll() {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery(
                    "SELECT g FROM StudentGroup g ORDER BY g.fieldOfStudies ASC",
                    StudentGroup.class
            ).getResultList();
        }
    }

    public void delete(String groupCode) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            StudentGroup group = em.find(StudentGroup.class, groupCode);
            if (group != null) em.remove(group);
            em.getTransaction().commit();
        }
    }

    public void assignUserToGroup(EntityManager em, User user, StudentGroup group) {
        StudentProfile profile = em.find(StudentProfile.class, user.getId());
        if (profile == null) {
            profile = new StudentProfile(
                    user,
                    LocalDateTime.now(),
                    LocalDate.now().plusYears(4),
                    group
            );
            em.persist(profile);
        } else {
            profile.setStudentGroup(group);
            em.merge(profile);
        }
    }

    public StudentGroup saveGroupWithMembers(StudentGroup group, List<User> members) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();

            StudentGroup saved = em.merge(group);

            // Clear all existing student_profiles that point to this group_code
            em.createQuery(
                "UPDATE StudentProfile sp SET sp.studentGroup = NULL " +
                "WHERE sp.studentGroup.groupCode = :code"
            ).setParameter("code", saved.getGroupCode()).executeUpdate();

            // Re-assign only the current members
            for (User member : members) {
                User managedUser = em.find(User.class, member.getId());
                if (managedUser != null) {
                    assignUserToGroup(em, managedUser, saved);
                }
            }

            em.getTransaction().commit();
            return saved;
        }
    }

    public List<User> findMembersByGroupCode(String groupCode) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery(
                    "SELECT sp.user FROM StudentProfile sp " +
                    "WHERE sp.studentGroup.groupCode = :code " +
                    "ORDER BY sp.user.firstName ASC, sp.user.sureName ASC",
                    User.class
            ).setParameter("code", groupCode).getResultList();
        }
    }

    public StudentGroup findGroupByUserId(Long userId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            List<StudentGroup> results = em.createQuery(
                    "SELECT sp.studentGroup FROM StudentProfile sp " +
                    "WHERE sp.user.id = :userId AND sp.studentGroup IS NOT NULL",
                    StudentGroup.class
            ).setParameter("userId", userId).getResultList();
            return results.isEmpty() ? null : results.get(0);
        }
    }

    /**
     * Returns only the groups that the given user belongs to.
     * Used so students only see their own group in the source tray.
     */
    public List<StudentGroup> findAllForUser(Long userId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery(
                    "SELECT sp.studentGroup FROM StudentProfile sp " +
                    "WHERE sp.user.id = :userId AND sp.studentGroup IS NOT NULL " +
                    "ORDER BY sp.studentGroup.fieldOfStudies ASC",
                    StudentGroup.class
            ).setParameter("userId", userId).getResultList();
        }
    }
}
