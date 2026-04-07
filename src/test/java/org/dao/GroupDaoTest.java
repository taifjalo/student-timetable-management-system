package org.dao;

import org.entities.StudentGroup;
import org.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DAO Test: GroupDao")
class GroupDaoTest {

    private GroupDao groupDao;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        groupDao = new GroupDao();
        userDao = new UserDao();
    }

    @DisplayName("Helper Method: First Create the Group using entity in the DB")
    private StudentGroup createGroup(String code, String field) {
        StudentGroup group = new StudentGroup();
        group.setGroupCode(code);
        group.setFieldOfStudies(field);
        return group;
    }

    @DisplayName("Helper Method: First Create the Users using entity in the DB")
    private User createUser() {

        // Arrange New User
        User user = new User();
        user.setUsername("mock" + UUID.randomUUID()); // Make always Random Username to pass DB.
        user.setPasswordHash("hashed");
        user.setEmail("mock+" + UUID.randomUUID() + "@test.com"); // Make always Random Email to pass DB.
        user.setFirstName("Mock");
        user.setSureName("User");
        // Make always Random phone number to pass DB.
        user.setPhoneNumber("090" + UUID.randomUUID().toString().substring(0, 7));
        user.setRole("student");

        // Save the User:
        userDao.save(user);

        return user;
    }

    @Test
    @DisplayName("Save: Should persist group")
    void shouldSaveGroup() {

        StudentGroup group = createGroup("CS-A1", "Computer Science");

        StudentGroup saved = groupDao.save(group);

        assertNotNull(saved);
        assertEquals("CS-A1", saved.getGroupCode());
    }

    @Test
    @DisplayName("FindByCode: Should return group")
    void shouldFindGroupByCode() {

        StudentGroup group = createGroup("CS-B1", "Software Engineering");
        groupDao.save(group);

        StudentGroup found = groupDao.findByCode("CS-B1");

        assertNotNull(found);
        assertEquals("CS-B1", found.getGroupCode());
    }

    @Test
    @DisplayName("FindAll: Should return all groups")
    void shouldReturnAllGroups() {

        groupDao.save(createGroup("G1", "Math"));
        groupDao.save(createGroup("G2", "Physics"));

        List<StudentGroup> groups = groupDao.findAll();

        assertFalse(groups.isEmpty());
    }

    @Test
    @DisplayName("Delete: Should remove group")
    void shouldDeleteGroup() {

        groupDao.save(createGroup("DEL1", "Temp"));

        groupDao.delete("DEL1");

        StudentGroup deleted = groupDao.findByCode("DEL1");

        assertNull(deleted);
    }

    @Test
    @DisplayName("SaveGroupWithMembers: Should assign users to group")
    void shouldAssignMembersToGroup() {

        StudentGroup group = createGroup("CS-C1", "AI");

        User user1 = createUser();
        User user2 = createUser();

        groupDao.saveGroupWithMembers(group, List.of(user1, user2));

        List<User> members = groupDao.findMembersByGroupCode("CS-C1");

        assertEquals(2, members.size());
    }

    @Test
    @DisplayName("FindMembersByGroupCode: Should return group members")
    void shouldFindGroupMembers() {

        StudentGroup group = createGroup("CS-D1", "Networks");

        User user = createUser();

        groupDao.saveGroupWithMembers(group, List.of(user));

        List<User> members = groupDao.findMembersByGroupCode("CS-D1");

        assertFalse(members.isEmpty());
    }
}
