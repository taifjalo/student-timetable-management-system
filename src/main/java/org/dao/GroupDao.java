package org.dao;

import jakarta.persistence.EntityManager;
import org.datasource.TimetableConnection;
import org.entities.StudentGroup;
import org.entities.StudentProfile;
import org.entities.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data-access object for {@link StudentGroup} entities and the related
 * {@link StudentProfile} membership records.
 */
public class GroupDao {

    /**
     * Persists or updates a student group using {@code merge}.
     *
     * @param group the group to save or update
     * @return the managed, merged group instance
     */
    public StudentGroup save(StudentGroup group) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            StudentGroup saved = em.merge(group);
            em.getTransaction().commit();
            return saved;
        }
    }

    /**
     * Looks up a group by its natural key (group code).
     *
     * @param groupCode the unique group code (e.g. {@code "SWD22S"})
     * @return the matching {@link StudentGroup}, or {@code null} if not found
     */
    public StudentGroup findByCode(String groupCode) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.find(StudentGroup.class, groupCode);
        }
    }

    /**
     * Returns all groups ordered alphabetically by field of studies.
     *
     * @return list of all groups, possibly empty
     */
    public List<StudentGroup> findAll() {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery(
                    "SELECT g FROM StudentGroup g ORDER BY g.fieldOfStudies ASC",
                    StudentGroup.class
            ).getResultList();
        }
    }

    /**
     * Deletes the group with the given code. Does nothing if the group does not exist.
     *
     * @param groupCode the group code to delete
     */
    public void delete(String groupCode) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            StudentGroup group = em.find(StudentGroup.class, groupCode);
            if (group != null) em.remove(group);
            em.getTransaction().commit();
        }
    }

    /**
     * Assigns a user to a group by creating or updating their {@link StudentProfile}.
     * If the user already has a profile, only their group assignment is updated.
     * Must be called within an active transaction on the supplied {@link EntityManager}.
     *
     * @param em    the active entity manager to use
     * @param user  the managed user entity to assign
     * @param group the managed group entity to assign the user to
     */
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

    /**
     * Saves a group and atomically replaces its full member list.
     * All existing {@link StudentProfile} entries pointing to this group are cleared
     * first, then the new members are re-assigned.
     *
     * @param group   the group to save (will be merged if it already exists)
     * @param members the complete, desired member list for this group
     * @return the managed, saved group instance
     */
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

    /**
     * Returns all users who are assigned to the given group via their student profiles,
     * ordered alphabetically by name.
     *
     * @param groupCode the group code to query
     * @return list of members, possibly empty
     */
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

    /**
     * Returns the group a specific user belongs to via their student profile,
     * or {@code null} if they have no group assignment.
     *
     * @param userId the user's ID
     * @return the user's {@link StudentGroup}, or {@code null}
     */
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
     * Returns all groups the given user belongs to via their student profile.
     *
     * @param userId the user's ID
     * @return list of groups, ordered alphabetically by field of studies
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
