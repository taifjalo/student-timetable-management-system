package org.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.dao.UserDao;
import org.service.AuthService;
import org.service.LocalizationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the registration screen ({@code register.fxml}).
 *
 * <p>The form collects only a username and password. The remaining required
 * user fields (first name, surname, email, phone) are filled with placeholder
 * values derived from the username; they can be updated later in the settings
 * modal.
 */
public class RegisterController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField passwordAgainField;
    @FXML private Label messageLabel;

    UserDao userDao = new UserDao();
    AuthService authService = new AuthService(userDao);
    private final LocalizationService localizationService = new LocalizationService();
    ResourceBundle selectedBundle = localizationService.getBundle();

    /**
     * Handles the "Register" button click.
     * Validates input, registers the new user, and navigates to the login screen.
     *
     * @param event the action event from the button click
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String passwordAgain = passwordAgainField.getText();

        // Validation
        if (username == null || username.isBlank()) {
            messageLabel.setText(selectedBundle.getString("register.username.error"));
            return;
        }
        if (password == null || password.isBlank()) {
            messageLabel.setText(selectedBundle.getString("register.no.password.error"));
            return;
        }
        if (!password.equals(passwordAgain)) {
            messageLabel.setText(selectedBundle.getString("register.password.error"));
            passwordField.clear();
            passwordAgainField.clear();
            return;
        }

        try {
            // Register with username as placeholder for required fields not on this form
            authService.register(username, username, username,
                    username + "@placeholder.com", "000" + username, password, "student");

            navigateToLogin(event);

        } catch (Exception e) {
            messageLabel.setText(selectedBundle.getString("register.invalid.credentials.error") + e.getMessage());
        }
    }

    /**
     * Handles the "Go to login" link click, navigating back to the login screen.
     *
     * @param event the action event from the link click
     */
    @FXML
    private void handleGoToLogin(ActionEvent event) {
        try {
            navigateToLogin(event);
        } catch (Exception e) {
            LOGGER.error("Unexpected error", e);
            messageLabel.setText(selectedBundle.getString("register.failed.credentials.error"));
        }
    }

    /**
     * Loads and displays the login screen, replacing the current scene.
     *
     * @param event the originating action event (used to resolve the current stage)
     * @throws IOException if {@code login.fxml} cannot be loaded
     */
    private void navigateToLogin(ActionEvent event) throws IOException {
        URL loginFxml = getClass().getResource("/ui/auth/login.fxml");
        if (loginFxml == null) {
            throw new IllegalArgumentException("login.fxml not found");
        }

        FXMLLoader loader = new FXMLLoader(loginFxml, localizationService.getBundle());
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
