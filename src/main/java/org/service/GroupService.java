package org.service;

import org.dao.GroupDao;
import org.entities.StudentGroup;
import org.entities.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service that manages {@link StudentGroup} lifecycle operations.
 * Wraps {@link GroupDao} and fires {@link NotificationService} events whenever
 * group membership changes.
 */
public class GroupService {

    private final GroupDao groupDao;
    private final NotificationService notificationService;

    /**
     * Creates a {@code GroupService} without notification support.
     * Suitable for contexts where notifications are not needed (e.g. read-only queries).
     *
     * @param groupDao the DAO used for group persistence
     */
    public GroupService(GroupDao groupDao) {
        this(groupDao, null);
    }

    /**
     * Creates a {@code GroupService} with notification support.
     *
     * @param groupDao            the DAO used for group persistence
     * @param notificationService the service used to send membership notifications
     *                            (may be {@code null} to disable notifications)
     */
    public GroupService(GroupDao groupDao, NotificationService notificationService) {
        this.groupDao = groupDao;
        this.notificationService = notificationService;
    }

    /**
     * Creates a new group and notifies every initial member that they have been added.
     *
     * @param groupName the unique group code and display name for the new group
     * @param members   the initial list of members to assign
     * @return the persisted {@link StudentGroup}
     * @throws IllegalArgumentException if {@code groupName} is blank
     */
    public StudentGroup createGroup(String groupName, List<User> members) {
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name is required");
        }
        StudentGroup group = new StudentGroup(groupName, groupName);
        StudentGroup saved = groupDao.saveGroupWithMembers(group, members);
        if (notificationService != null) {
            for (User member : members) {
                notificationService.notifyStudentAddedToGroup(saved.getGroupCode(), member.getId());
            }
        }
        return saved;
    }

    /**
     * Updates an existing group's display name and member list.
     * Notifications are sent to users who were added or removed compared to
     * the previous membership snapshot.
     *
     * @param groupCode the code of the group to update
     * @param groupName the new display name
     * @param members   the complete, desired member list after the update
     * @return the updated {@link StudentGroup}
     * @throws IllegalArgumentException if the group is not found
     */
    public StudentGroup updateGroup(String groupCode, String groupName, List<User> members) {
        StudentGroup group = groupDao.findByCode(groupCode);
        if (group == null) throw new IllegalArgumentException("Group not found: " + groupCode);

        // Capture previous members BEFORE saveGroupWithMembers wipes them
        List<User> previousMembers = groupDao.findMembersByGroupCode(groupCode);

        group.setFieldOfStudies(groupName);
        StudentGroup saved = groupDao.saveGroupWithMembers(group, members);
        if (notificationService != null) {
            Set<Long> previousIds = previousMembers.stream()
                    .map(User::getId).collect(Collectors.toSet());
            Set<Long> newIds = members.stream()
                    .map(User::getId).collect(Collectors.toSet());

            for (User member : members) {
                if (!previousIds.contains(member.getId())) {
                    notificationService.notifyStudentAddedToGroup(saved.getGroupCode(), member.getId());
                }
            }
            for (User prev : previousMembers) {
                if (!newIds.contains(prev.getId())) {
                    notificationService.notifyStudentRemovedFromGroup(saved.getGroupCode(), prev.getId());
                }
            }
        }
        return saved;
    }

    /**
     * Deletes the group with the given code.
     *
     * @param groupCode the code of the group to delete
     */
    public void deleteGroup(String groupCode) {
        groupDao.delete(groupCode);
    }

    /**
     * Returns all groups ordered alphabetically.
     *
     * @return list of all groups, possibly empty
     */
    public List<StudentGroup> getAllGroups() {
        return groupDao.findAll();
    }

    /**
     * Returns groups for the given user. If {@code userId} is {@code null}
     * (e.g. for a teacher), all groups are returned.
     *
     * @param userId the student's user ID, or {@code null} to get all groups
     * @return list of groups the user belongs to, or all groups
     */
    public List<StudentGroup> getGroupsForUser(Long userId) {
        if (userId == null) return groupDao.findAll();
        return groupDao.findAllForUser(userId);
    }

    /**
     * Returns the list of students currently assigned to the given group.
     *
     * @param groupCode the group's code
     * @return list of member {@link User}s, possibly empty
     */
    public List<User> getStudentsInGroup(String groupCode) {
        return groupDao.findMembersByGroupCode(groupCode);
    }
}
