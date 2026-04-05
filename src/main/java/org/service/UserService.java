package org.service;

import org.dao.UserDao;
import org.entities.User;

import java.util.List;

public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<User> getAllStudents() {
        return userDao.findAll();
    }

    public List<User> searchStudents(String query) {
        if (query == null || query.isBlank()) {
            return userDao.findAll();
        }
        return userDao.searchByName(query.trim());
    }
}

