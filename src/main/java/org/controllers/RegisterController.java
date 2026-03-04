package org.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.dao.UserDao;
import org.entities.User;
import org.service.AuthService;

public class RegisterController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    // Backend Logic & DB user connection.
    UserDao userDao = new UserDao();
    AuthService authService = new AuthService(userDao);

    @FXML
    private void handleRegister () {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();

        try {
            User user = authService.register(firstName, lastName, username, email, phone, password);
            messageLabel.setText("User registered successfully!" + user.getUsername());


        } catch (Exception e) {
            messageLabel.setText(e.getMessage());
        }

    }

}
