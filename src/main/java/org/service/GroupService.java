package org.service;

import org.dao.GroupDao;
import org.entities.StudentGroup;
import org.entities.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupService {

    private final GroupDao groupDao;
    private final NotificationService notificationService;

    // Existing constructor — unchanged, backward-compatible
    public GroupService(GroupDao groupDao) {
        this(groupDao, null);
    }

    // New constructor — used when notifications are needed
    public GroupService(GroupDao groupDao, NotificationService notificationService) {
        this.groupDao = groupDao;
        this.notificationService = notificationService;
    }

    public StudentGroup createGroup(String groupName, List<User> members) {
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name is required");
        }
        StudentGroup group = new StudentGroup(groupName, groupName);
        StudentGroup saved = groupDao.saveGroupWithMembers(group, members);
        System.out.println("Created group '" + saved.getGroupCode()
                + "' with " + members.size() + " members");
        if (notificationService != null) {
            for (User member : members) {
                notificationService.notifyStudentAddedToGroup(saved.getGroupCode(), member.getId());
            }
        }
        return saved;
    }

    public StudentGroup updateGroup(String groupCode, String groupName, List<User> members) {
        StudentGroup group = groupDao.findByCode(groupCode);
        if (group == null) throw new IllegalArgumentException("Group not found: " + groupCode);

        // Capture previous members BEFORE saveGroupWithMembers wipes them
        List<User> previousMembers = groupDao.findMembersByGroupCode(groupCode);

        group.setFieldOfStudies(groupName);
        StudentGroup saved = groupDao.saveGroupWithMembers(group, members);
        System.out.println("Updated group '" + saved.getGroupCode()
                + "' with " + members.size() + " members");

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

    public void deleteGroup(String groupCode) {
        groupDao.delete(groupCode);
        System.out.println("Deleted group: " + groupCode);
    }

    public List<StudentGroup> getAllGroups() {
        return groupDao.findAll();
    }

    public List<User> getStudentsInGroup(String groupCode) {
        return groupDao.findMembersByGroupCode(groupCode);
    }
}
