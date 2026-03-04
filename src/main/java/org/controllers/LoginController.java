package org.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.dao.UserDao;
import org.entities.User;
import org.service.AuthService;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    // Backend Logic & DB user connection.
    UserDao userDao = new UserDao();
    AuthService authService = new AuthService(userDao);

    @FXML
    private void handleLogin() {

        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            User user = authService.login(username, password);
            messageLabel.setText("Login successful: " + user.getUsername());

            // TODO: switch scene to dashboard

        } catch (Exception e) {
            messageLabel.setText("Login failed: " + e.getMessage());
        }
    }
}
