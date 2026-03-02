package org.dao;

import jakarta.persistence.EntityManager;
import org.datasource.TimetableConnection;
import org.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class UserDaoTest {

    @DisplayName("Integration Test: Tests are applied with JUnit 5 for func Save User and Commit")

    @Test
    void shouldSaveAndFindUserIntegration() {

        UserDao dao = new UserDao(); // For real test integration with DB.

        // Make New User:
        User user = new User();
        user.setUsername("integrationUser3");
        user.setPasswordHash("hashed3");
        user.setEmail("integration3@test.com");
        user.setFirstName("Integration3");
        user.setSureName("Test3");
        user.setPhoneNumber("0901234563");


        // Save the User:
        dao.save(user);

        // Then search in the DB to find the user:
        User found = dao.findByUsername("integrationUser3"); // the user from out DB
        assertNotNull(found);

        assertEquals("integrationUser3", found.getUsername());


    }
}