
package org.service;

import org.dao.UserDao;
import org.entities.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final UserDao userDao;

    // Constructor: @InjectMocks + @Mock@ Dependency for testing.
    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User register(String firstName,
                         String lastName,
                         String username,
                         String email,
                         String phone,
                         String password) {

        // if password exists
        if (userDao.findByUsername(username) != null) {
            throw new RuntimeException("Username already exists");
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

        // save them
        userDao.save(user);
        return user;
    }


    public User login(String username, String password) {

        // first bring the user
        User user = userDao.findByUsername(username);

        // if the user inputs not null AND passwords hash in database are correct. return the user and log in.
        if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {
            return user;
        }
        else {
            throw new RuntimeException("Invalid username or password");
        }
    }

}
