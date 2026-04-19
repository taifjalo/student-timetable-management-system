package org.dao;

import org.entities.User;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    private User createUser(String firstName, String sureName) {
        User user = new User();
        user.setUsername("mock_" + UUID.randomUUID());
        user.setPasswordHash("hashed");
        user.setEmail("mock_" + UUID.randomUUID() + "@test.com");
        user.setFirstName(firstName);
        user.setSureName(sureName);
        user.setPhoneNumber("090" + UUID.randomUUID().toString().replace("-", "").substring(0, 7));
        user.setRole("student");
        userDao.save(user);
        return user;
    }

    // ── save + findByUsername ────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Integration: save user and find by username")
    void testSaveAndFindByUsername() {
        User user = createUser("Mock", "User");

        User found = userDao.findByUsername(user.getUsername());

        assertNotNull(found);
        assertEquals(user.getUsername(), found.getUsername());
    }

    @Test
    @Order(2)
    @DisplayName("findByUsername returns null for non-existent username")
    void testFindByUsernameNotFound() {
        User found = userDao.findByUsername("nonexistent_user_xyz_999");
        assertNull(found);
    }

    // ── findUserByNameSurname ────────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("Integration: findUserByNameSurname returns matching user")
    void testFindUserByNameSurname() {
        String firstName = "Alpha" + UUID.randomUUID().toString().substring(0, 4);
        String sureName  = "Beta"  + UUID.randomUUID().toString().substring(0, 4);
        createUser(firstName, sureName);

        List<User> result = userDao.findUserByNameSurname(firstName, sureName);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(u ->
                u.getFirstName().equals(firstName) && u.getSureName().equals(sureName)));
    }

    @Test
    @Order(4)
    @DisplayName("Integration: findUserByNameSurname works with reversed order")
    void testFindUserByNameSurnameReversed() {
        String firstName = "Gamma" + UUID.randomUUID().toString().substring(0, 4);
        String sureName  = "Delta" + UUID.randomUUID().toString().substring(0, 4);
        createUser(firstName, sureName);

        // Pass in reversed order — DAO should still find it
        List<User> result = userDao.findUserByNameSurname(sureName, firstName);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("findUserByNameSurname returns empty list when no match")
    void testFindUserByNameSurnameNoMatch() {
        List<User> result = userDao.findUserByNameSurname("NoSuch", "PersonXyz999");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("Integration: findAll returns non-empty list")
    void testFindAll() {
        createUser("FindAll", "Test");

        List<User> all = userDao.findAll();

        assertNotNull(all);
        assertFalse(all.isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("Integration: findAll returns list ordered by firstName")
    void testFindAllOrdered() {
        List<User> all = userDao.findAll();
        assertNotNull(all);
        // Verify ordering: each element's firstName >= previous
        for (int i = 1; i < all.size(); i++) {
            String prev = all.get(i - 1).getFirstName().toLowerCase();
            String curr = all.get(i).getFirstName().toLowerCase();
            assertTrue(prev.compareTo(curr) <= 0,
                    "List should be ordered alphabetically by firstName");
        }
    }

    // ── searchByName ─────────────────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("Integration: searchByName finds user by partial firstName")
    void testSearchByNamePartialFirst() {
        String unique = "Zrqx" + UUID.randomUUID().toString().substring(0, 4);
        createUser(unique, "Search");

        List<User> result = userDao.searchByName(unique.substring(0, 4));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(u -> u.getFirstName().contains(unique.substring(0, 4))));
    }

    @Test
    @Order(9)
    @DisplayName("Integration: searchByName finds user by partial sureName")
    void testSearchByNamePartialSure() {
        String unique = "Ywmv" + UUID.randomUUID().toString().substring(0, 4);
        createUser("Search", unique);

        List<User> result = userDao.searchByName(unique.substring(0, 4));

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(10)
    @DisplayName("Integration: searchByName is case-insensitive")
    void testSearchByNameCaseInsensitive() {
        String unique = "Vxkp" + UUID.randomUUID().toString().substring(0, 4);
        createUser(unique, "CaseTest");

        List<User> result = userDao.searchByName(unique.toLowerCase());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Order(11)
    @DisplayName("searchByName returns empty list for no match")
    void testSearchByNameNoMatch() {
        List<User> result = userDao.searchByName("zzzNobodyExistsXXX999");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    @Order(12)
    @DisplayName("Integration: update changes user email")
    void testUpdate() {
        User user = createUser("Update", "Test");
        String newEmail = "updated_" + UUID.randomUUID() + "@test.com";
        user.setEmail(newEmail);

        User updated = userDao.update(user);

        assertNotNull(updated);
        assertEquals(newEmail, updated.getEmail());
    }

    @Test
    @Order(13)
    @DisplayName("Integration: update changes user role")
    void testUpdateRole() {
        User user = createUser("Role", "Update");
        user.setRole("teacher");

        User updated = userDao.update(user);

        assertNotNull(updated);
        assertEquals("teacher", updated.getRole());
    }
}