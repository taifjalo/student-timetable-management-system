package org.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.dao.UserDao;
import org.entities.User;
import org.mindrot.jbcrypt.BCrypt;
import org.service.LocalizationService;
import org.service.SessionManager;

import java.util.ResourceBundle;

public class UserSettingsController {

    @FXML private Text displayNameText;
    @FXML private Text roleText;

    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField newPasswordAgainField;

    @FXML private Label passwordMessageLabel;
    @FXML private Label messageLabel;

    private Stage stage;
    private final UserDao userDao = new UserDao();
    LocalizationService localizationService = new LocalizationService();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        // Header
        displayNameText.setText(user.getFirstName());
        String role = user.getRole() != null ? capitalize(user.getRole()) : "Käyttäjä";
        roleText.setText(role);

        // Fields
        usernameField.setText(nullSafe(user.getUsername()));
        firstNameField.setText(nullSafe(user.getFirstName()));
        lastNameField.setText(nullSafe(user.getSureName()));
        emailField.setText(nullSafe(user.getEmail()));
        phoneField.setText(nullSafe(user.getPhoneNumber()));
    }

    @FXML
    private void handleSave() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        user.setUsername(usernameField.getText().trim());
        user.setFirstName(firstNameField.getText().trim());
        user.setSureName(lastNameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setPhoneNumber(phoneField.getText().trim());

        try {
            User updated = userDao.update(user);
            SessionManager.getInstance().login(updated);
            // Refresh header
            displayNameText.setText(updated.getFirstName());
            messageLabel.setText("Tiedot tallennettu.");
            System.out.println("Profile saved for: " + updated.getUsername());
        } catch (Exception e) {
            messageLabel.setText("Tallennus epäonnistui: " + e.getMessage());
            System.out.println("Save failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangePassword() {
        passwordMessageLabel.setText("");
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        String current = currentPasswordField.getText();
        String newPw = newPasswordField.getText();
        String newPwAgain = newPasswordAgainField.getText();

        if (current.isBlank() || newPw.isBlank() || newPwAgain.isBlank()) {
            passwordMessageLabel.setText("Täytä kaikki salasanakentät.");
            return;
        }
        if (!BCrypt.checkpw(current, user.getPasswordHash())) {
            passwordMessageLabel.setText("Nykyinen salasana on väärä.");
            currentPasswordField.clear();
            return;
        }
        if (!newPw.equals(newPwAgain)) {
            passwordMessageLabel.setText("Uudet salasanat eivät täsmää.");
            newPasswordField.clear();
            newPasswordAgainField.clear();
            return;
        }

        user.setPasswordHash(BCrypt.hashpw(newPw, BCrypt.gensalt()));
        try {
            User updated = userDao.update(user);
            SessionManager.getInstance().login(updated);
            currentPasswordField.clear();
            newPasswordField.clear();
            newPasswordAgainField.clear();
            passwordMessageLabel.setStyle("-fx-text-fill: #00956D;");
            passwordMessageLabel.setText("Salasana vaihdettu.");
            System.out.println("Password changed for: " + updated.getUsername());
        } catch (Exception e) {
            passwordMessageLabel.setText("Salasanan vaihto epäonnistui: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            // Close the modal first
            if (stage != null) stage.close();

            // Navigate the main window back to login
            Stage mainStage = (Stage) stage.getOwner();
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            mainStage.setScene(scene);
            mainStage.setMaximized(true);
            mainStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Stage getCurrentStage() {
        return stage != null ? stage : (Stage) messageLabel.getScene().getWindow();
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}






