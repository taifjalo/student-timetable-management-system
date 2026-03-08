package org.service;

import org.dao.GroupDao;
import org.entities.StudentGroup;
import org.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@DisplayName("Logic Test: Tests are applied with JUnit 5 & Mockito for func Logic Test purpose")
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {
    @Mock
    NotificationService notificationService;

    @Mock
    GroupDao groupeDao;

    @InjectMocks
    GroupService groupService;

    @DisplayName("Helper Method: First Should Create Group")
    private StudentGroup createGroup(String groupCode, String fieldOfStudies) {
        StudentGroup group = new StudentGroup();
        group.setGroupCode(groupCode);
        group.setFieldOfStudies(fieldOfStudies);
        return group;
    }

    @DisplayName("Helper Method: Second Should Create User")
    private User createUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setFirstName(name);
        return user;
    }

    @Test
    @DisplayName("Create Group: Should successfully create a group when all fields are valid ")
    void shouldCreateGroup() {

        StudentGroup group = createGroup("G1", "CS");
        User student = createUser(1L, "Student");

        List<User> members = List.of(student);

        when(groupeDao.saveGroupWithMembers(any(StudentGroup.class), any())).thenReturn(group);

        StudentGroup result = groupService.createGroup("G1", members);

        assertEquals("G1", result.getGroupCode());

        verify(groupeDao).saveGroupWithMembers(any(StudentGroup.class), any());
        verify(notificationService).notifyStudentAddedToGroup("G1", 1L);
    }

    @Test
    @DisplayName("Update Group: Should update group successfully")
    void updateGroup() {

        StudentGroup existing = createGroup("G1", "CS");

        User oldStudent = createUser(1L, "Student");
        User newStudent = createUser(2L, "Student");


        when(groupeDao.findByCode("G1")).thenReturn(existing);
        when(groupeDao.findMembersByGroupCode("G1"))
                .thenReturn(List.of(oldStudent));

        when(groupeDao.saveGroupWithMembers(any(), any()))
                .thenReturn(existing);

        groupService.updateGroup("G1", "SE", List.of(newStudent));

        verify(groupeDao).saveGroupWithMembers(any(), any());

        verify(notificationService)
                .notifyStudentAddedToGroup("G1", 2L);

        verify(notificationService)
                .notifyStudentRemovedFromGroup("G1", 1L);
    }

    @Test
    @DisplayName("Delete Group: Should delete group successfully")
    void deleteGroup() {

        groupService.deleteGroup("G1");

        verify(groupeDao).delete("G1");
    }

    @Test
    @DisplayName("Get All Groups: Should return list of groups")
    void getAllGroups() {

        List<StudentGroup> groups = List.of(
                createGroup("G1","CS"),
                createGroup("G2","IT")
        );

        when(groupeDao.findAll()).thenReturn(groups);

        List<StudentGroup> result = groupService.getAllGroups();

        assertEquals(2, result.size());

        verify(groupeDao).findAll();
    }

    @Test
    @DisplayName("Get Students In Group")
    void getStudentsInGroup() {

        User student = createUser(1L, "Student");

        when(groupeDao.findMembersByGroupCode("G1"))
                .thenReturn(List.of(student));

        List<User> result = groupService.getStudentsInGroup("G1");

        assertEquals(1, result.size());

        verify(groupeDao).findMembersByGroupCode("G1");
    }
}