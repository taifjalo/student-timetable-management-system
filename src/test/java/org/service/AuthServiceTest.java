package org.service;

import org.dao.UserDao;
import org.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Logic Test: Tests are applied with JUnit 5 & Mockito for func Logic Test purpose")

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserDao userDao;

    @InjectMocks
    AuthService authService;

    // Login Tests:
    @Test
    @DisplayName("Login Test: Should call login when credentials are correct")
    void shouldReturnUserWhenCredentialsAreCorrect() {

        String rawPassword = "12345678";
        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        User user = new User();
        user.setUsername("user");
        user.setPasswordHash(hashed);

        when(userDao.findByUsername("user")).thenReturn(user);

        // Call Login Method:
        User result = authService.login("user", rawPassword);

        assertNotNull(result);
        assertEquals("user", result.getUsername());
    }

    @Test
    @DisplayName("Login Test: Should throw exception when username does not exist")
    void shouldThrowExceptionWhenUsernameNotExist() {

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login("user", "wrongPassword");
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    @DisplayName("Login Test: Should throw exception when password is wrong")
    void shouldThrowExceptionWhenPasswordIncorrect() {
        String rawPassword = "12345678";
        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        User user = new User();
        user.setUsername("user");
        user.setPasswordHash(hashed);

        when(userDao.findByUsername("user")).thenReturn(user);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login("user", "wrongPassword");
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }

    // Register Tests:
    @Test
    @DisplayName("Register Test: Should call save when credentials are correct")
    void shouldRegisterUserWhenCredentialsAreCorrect() {

        // First find if user exist or doesn't exist in the database:
        when(userDao.findByUsername("user")).thenReturn(null);
        // in this condition when calling save() method to save new user don't do anything:
        doNothing().when(userDao).save(any(User.class));

        // Call Register Method
        authService.register(
                "Logic",
                "Testing",
                "user",
                "Testing@test.com",
                "090123456",
                "87654321",
                "Student"
        );

        // Verify the user to regist called one time the save() user method.
        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register Test: Should throw exception if username exists")
    void shouldThrowExceptionWhenUsernameExists() {

        // If user exist
        User existingUser = new User();
        existingUser.setUsername("user");


        // First find if user exist or doesn't exist in the database, if the user exist then Return (existingUser):
        when(userDao.findByUsername("user")).thenReturn(existingUser);


        // Throw RuntimeException when Call Register Method
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(
                "Logic",
                "Testing",
                "user",
                "Testing@test.com",
                "090123456",
                "87654321",
                "Student"
        ));


        assertEquals("Username already exists", exception.getMessage());
        // Verify the user to regist SHOULD NOT called one time the save() user method.
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Register: Should throw when role is null")
    void shouldThrowWhenRoleIsNull() {
        when(userDao.findByUsername("john")).thenReturn(null);

        assertThrows(RuntimeException.class, () ->
                authService.register("John", "Doe", "john",
                        "john@example.com", "040000000", "secret123", null)
        );

        verify(userDao, never()).save(any());
    }


    @Test
    @DisplayName("Register: Should throw when role is blank")
    void shouldThrowWhenRoleIsBlank() {
        when(userDao.findByUsername("john")).thenReturn(null);

        assertThrows(RuntimeException.class, () ->
                authService.register("John", "Doe", "john",
                        "john@example.com", "040000000", "secret123", "   ")
        );

        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("Register: Password should be stored as BCrypt hash, not plain text")
    void passwordShouldBeStoredHashed() {
        when(userDao.findByUsername("alice")).thenReturn(null);
        doNothing().when(userDao).save(any(User.class));

        User result = authService.register(
                "Alice", "Smith", "alice",
                "alice@example.com", "040000001",
                "myPlainPassword", "teacher"
        );

        assertNotEquals("myPlainPassword", result.getPasswordHash());
        assertTrue(BCrypt.checkpw("myPlainPassword", result.getPasswordHash()));
    }


}
