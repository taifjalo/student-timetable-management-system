package org.service;

import org.dao.GroupDao;
import org.entities.StudentGroup;
import org.entities.User;

import java.util.List;

public class GroupService {

    private final GroupDao groupDao;

    public GroupService(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public StudentGroup createGroup(String groupName, List<User> members) {
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name is required");
        }
        StudentGroup group = new StudentGroup(groupName, groupName);
        StudentGroup saved = groupDao.saveGroupWithMembers(group, members);
        System.out.println("Created group '" + saved.getGroupCode()
                + "' with " + members.size() + " members");
        return saved;
    }

    public StudentGroup updateGroup(String groupCode, String groupName, List<User> members) {
        StudentGroup group = groupDao.findByCode(groupCode);
        if (group == null) throw new IllegalArgumentException("Group not found: " + groupCode);
        group.setFieldOfStudies(groupName);
        StudentGroup saved = groupDao.saveGroupWithMembers(group, members);
        System.out.println("Updated group '" + saved.getGroupCode()
                + "' with " + members.size() + " members");
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
