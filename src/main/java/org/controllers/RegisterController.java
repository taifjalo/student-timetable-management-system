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

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField passwordAgainField;
    @FXML private Label messageLabel;

    UserDao userDao = new UserDao();
    AuthService authService = new AuthService(userDao);

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String passwordAgain = passwordAgainField.getText();

        // Validation
        if (username == null || username.isBlank()) {
            messageLabel.setText("Käyttäjänimi on pakollinen.");
            return;
        }
        if (password == null || password.isBlank()) {
            messageLabel.setText("Salasana on pakollinen.");
            return;
        }
        if (!password.equals(passwordAgain)) {
            messageLabel.setText("Salasanat eivät täsmää.");
            passwordField.clear();
            passwordAgainField.clear();
            return;
        }

        try {
            // Register with username as placeholder for required fields not on this form
            authService.register(username, username, username, username + "@placeholder.com", "000000000", password);
            System.out.println("Registered: " + username);

            // Redirect to login
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            messageLabel.setText("Rekisteröinti epäonnistui: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
