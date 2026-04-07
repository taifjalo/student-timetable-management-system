package org.dao;

import org.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoTest {

    // For real test integration with DB.
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
    }

    @DisplayName("Helper Method: First Create the user in DB")
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

    @DisplayName("Integration Test: Tests are applied with JUnit 5 for func Save User and Commit")
    @Test
    void testSaveAndFindUserIntegration() {
        // Call create user to Arrange it
        User user = createUser();

        // Then search in the DB to find and return the user:
        User found = userDao.findByUsername(user.getUsername()); // the user from out DB

        assertNotNull(found);
        assertEquals(user.getUsername(), found.getUsername());
    }
}




/*
@DisplayName("Logic Test: Tests are applied with JUnit 5 & Mockito for func Logic Test purpose")
class UserDaoTest {

    @Mock
    private EntityManager em;

    // userDoa depends on EntityManager:
    @InjectMocks
    private UserDao userDao;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("Logic Test: persist() is called when saving user")
    @Test
    void shouldCallPersistWhenSaveUser() {
        // Arrange New User
        User user = new User();
        user.setUsername("mock" + UUID.randomUUID()); // Make always Random Username to pass DB.
        //user.setUsername("mockUser");
        user.setPasswordHash("hashed");
        user.setEmail("mock+" + UUID.randomUUID() + "@test.com"); // Make always Random Email to pass DB.
        //user.setEmail("mock@test.com");
        user.setFirstName("Mock");
        user.setSureName("User");
        // Make always Random phone number to pass DB.
        user.setPhoneNumber("090" + UUID.randomUUID().toString().substring(0, 7));
        //user.setPhoneNumber("0901234565");



        // Act save it
        userDao.save(user);

        // Assert to make sure that the persist() called one time if the test success
        verify(em, times(1)).persist(user);
    }
}
*/





