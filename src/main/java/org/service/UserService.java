package org.service;

import org.dao.UserDao;
import org.entities.User;

import java.util.List;

/**
 * Service that exposes user query operations for the UI layer.
 * Wraps {@link UserDao} to provide a clean service boundary.
 */
public class UserService {

    private final UserDao userDao;

    /**
     * Creates a {@code UserService} with the given DAO.
     *
     * @param userDao the DAO used for user queries
     */
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Returns all registered users ordered alphabetically.
     *
     * @return list of all users, possibly empty
     */
    public List<User> getAllStudents() {
        return userDao.findAll();
    }

    /**
     * Searches users by name. If the query is blank, all users are returned.
     * The search is case-insensitive and matches against first name, surname,
     * and the concatenated full name.
     *
     * @param query the search string; {@code null} or blank returns all users
     * @return list of matching users, possibly empty
     */
    public List<User> searchStudents(String query) {
        if (query == null || query.isBlank()) {
            return userDao.findAll();
        }
        return userDao.searchByName(query.trim());
    }
}
