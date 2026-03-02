package org.dao;

import jakarta.persistence.EntityManager;
import org.datasource.TimetableConnection;
import org.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDaoTest {

    @DisplayName("Integration Test: Tests are applied with JUnit 5 for func Save User and Commit")
    @Test
    void shouldSaveAndFindUserIntegration() {

        UserDao dao = new UserDao(); // For real test integration with DB.

        // Arrange New User
        User user = new User();
        user.setUsername("mock" + UUID.randomUUID());                               // Make always Random Username to pass DB.
        //user.setUsername("mockUser");
        user.setPasswordHash("hashed");
        user.setEmail("mock+" + UUID.randomUUID() + "@test.com");                   // Make always Random Email to pass DB.
        //user.setEmail("mock@test.com");
        user.setFirstName("Mock");
        user.setSureName("User");
        user.setPhoneNumber("090" + UUID.randomUUID().toString().substring(0,7)); // Make always Random phone number to pass DB.
        //user.setPhoneNumber("0901234565");


        // Save the User:
        dao.save(user);

        // Then search in the DB to find the user:
        User found = dao.findByUsername("integrationUser3"); // the user from out DB
        assertNotNull(found);

        assertEquals("integrationUser3", found.getUsername());


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
        user.setUsername("mock" + UUID.randomUUID());                               // Make always Random Username to pass DB.
        //user.setUsername("mockUser");
        user.setPasswordHash("hashed");
        user.setEmail("mock+" + UUID.randomUUID() + "@test.com");                   // Make always Random Email to pass DB.
        //user.setEmail("mock@test.com");
        user.setFirstName("Mock");
        user.setSureName("User");
        user.setPhoneNumber("090" + UUID.randomUUID().toString().substring(0,7)); // Make always Random phone number to pass DB.
        //user.setPhoneNumber("0901234565");



        // Act save it
        userDao.save(user);

        // Assert to make sure that the persist() called one time if the test success
        verify(em, times(1)).persist(user);
    }
}
*/





