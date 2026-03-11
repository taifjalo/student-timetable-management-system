package org.service;

import org.dao.UserDao;
import org.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Logic Test: UserService tests with JUnit 5 & Mockito")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserDao userDao;

    @InjectMocks
    UserService userService;

    private User createUser(Long id, String firstName, String sureName) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setSureName(sureName);
        return user;
    }

    // ── getAllStudents ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("GetAllStudents: Should return all users from DAO")
    void shouldReturnAllStudents() {
        List<User> users = List.of(
                createUser(1L, "Alice", "Smith"),
                createUser(2L, "Bob", "Jones")
        );
        when(userDao.findAll()).thenReturn(users);

        List<User> result = userService.getAllStudents();

        assertEquals(2, result.size());
        verify(userDao).findAll();
    }

    @Test
    @DisplayName("GetAllStudents: Should return empty list when no users exist")
    void shouldReturnEmptyListWhenNoStudents() {
        when(userDao.findAll()).thenReturn(Collections.emptyList());

        List<User> result = userService.getAllStudents();

        assertTrue(result.isEmpty());
        verify(userDao).findAll();
    }

    // ── searchStudents ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("SearchStudents: Should call searchByName with trimmed query")
    void shouldSearchByNameWhenQueryProvided() {
        List<User> users = List.of(createUser(1L, "Alice", "Smith"));
        when(userDao.searchByName("alice")).thenReturn(users);

        List<User> result = userService.searchStudents("  alice  ");

        assertEquals(1, result.size());
        verify(userDao).searchByName("alice");
        verify(userDao, never()).findAll();
    }

    @Test
    @DisplayName("SearchStudents: Should return all when query is null")
    void shouldReturnAllWhenQueryIsNull() {
        List<User> users = List.of(createUser(1L, "Alice", "Smith"));
        when(userDao.findAll()).thenReturn(users);

        List<User> result = userService.searchStudents(null);

        assertEquals(1, result.size());
        verify(userDao).findAll();
        verify(userDao, never()).searchByName(any());
    }

    @Test
    @DisplayName("SearchStudents: Should return all when query is blank")
    void shouldReturnAllWhenQueryIsBlank() {
        List<User> users = List.of(createUser(1L, "Bob", "Jones"));
        when(userDao.findAll()).thenReturn(users);

        List<User> result = userService.searchStudents("   ");

        assertEquals(1, result.size());
        verify(userDao).findAll();
        verify(userDao, never()).searchByName(any());
    }

    @Test
    @DisplayName("SearchStudents: Should return empty list when no match found")
    void shouldReturnEmptyWhenNoMatch() {
        when(userDao.searchByName("xyz")).thenReturn(Collections.emptyList());

        List<User> result = userService.searchStudents("xyz");

        assertTrue(result.isEmpty());
        verify(userDao).searchByName("xyz");
    }
}