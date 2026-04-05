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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField passwordAgainField;
    @FXML private Label messageLabel;

    UserDao userDao = new UserDao();
    AuthService authService = new AuthService(userDao);
    private final LocalizationService localizationService = new LocalizationService();
    ResourceBundle selectedBundle = localizationService.getBundle();

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
            authService.register(username, username, username, username + "@placeholder.com", "000000000", password, "student");
            System.out.println("Registered: " + username);

            navigateToLogin(event);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            messageLabel.setText(selectedBundle.getString("register.invalid.credentials.error") + e.getMessage());
        }
    }

    @FXML
    private void handleGoToLogin(ActionEvent event) {
        try {
            navigateToLogin(event);
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText(selectedBundle.getString("register.failed.credentials.error"));
        }
    }

    private void navigateToLogin(ActionEvent event) throws IOException {
        URL loginFxml = getClass().getResource("/login.fxml");
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
