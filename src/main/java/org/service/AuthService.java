package org.service;

import org.dao.UserDao;
import org.entities.User;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Service responsible for user authentication and registration.
 * Passwords are hashed with BCrypt before storage and verified with BCrypt on login.
 */
public class AuthService {

    private final UserDao userDao;

    /**
     * Creates an {@code AuthService} with the given DAO.
     * The constructor signature also satisfies {@code @InjectMocks + @Mock} in unit tests.
     *
     * @param userDao the DAO used for user persistence and lookup
     */
    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Registers a new user account.
     *
     * @param firstName the user's first name
     * @param lastName  the user's surname
     * @param username  a unique login username
     * @param email     a unique email address
     * @param phone     phone number (may be a placeholder for forms that don't collect it)
     * @param password  plain-text password; will be BCrypt-hashed before storage
     * @param role      role string, typically {@code "student"} or {@code "teacher"}
     * @return the newly created and persisted {@link User}
     * @throws IllegalArgumentException if the username is already taken or the role is blank
     */
    public User register(String firstName,
                         String lastName,
                         String username,
                         String email,
                         String phone,
                         String password,
                         String role) {


        // reject if username is already taken
        if (userDao.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }

        // save hash password to the database
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // register new account
        User user = new User();
        user.setFirstName(firstName);
        user.setSureName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setPasswordHash(hashedPassword);
        user.setRole(role);

        // save them
        userDao.save(user);
        return user;
    }


    /**
     * Authenticates a user by username and password.
     *
     * @param username the login username
     * @param password the plain-text password to verify
     * @return the authenticated {@link User}
     * @throws IllegalArgumentException if the username does not exist or the password is incorrect
     */
    public User login(String username, String password) {

        // first bring the user
        User user = userDao.findByUsername(username);

        // if the user inputs not null AND passwords hash in database are correct. return the user and log in.
        if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {
            return user;
        } else {
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

}
