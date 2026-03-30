package org.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.dao.UserDao;
import org.entities.User;
import org.mindrot.jbcrypt.BCrypt;
import org.service.LocalizationService;
import org.service.SessionManager;

import java.io.IOException;
import java.util.Locale;
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

    @FXML private MenuButton languageMenuButton;
    @FXML private MenuItem fiItem;
    @FXML private MenuItem enItem;
    @FXML private MenuItem arItem;
    @FXML private MenuItem ruItem;

    private Stage stage;
    private final UserDao userDao = new UserDao();

    private final LocalizationService localizationService = new LocalizationService();
    ResourceBundle selectedBundle = localizationService.getBundle();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        // Header
        displayNameText.setText(user.getFirstName());
        String role = user.getRole() != null ? capitalize(user.getRole()) : selectedBundle.getString("settings.modal.role.label");
        roleText.setText(role);

        // Fields
        usernameField.setText(nullSafe(user.getUsername()));
        firstNameField.setText(nullSafe(user.getFirstName()));
        lastNameField.setText(nullSafe(user.getSureName()));
        emailField.setText(nullSafe(user.getEmail()));
        phoneField.setText(nullSafe(user.getPhoneNumber()));

        applyLanguageMenuSelection();

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
            messageLabel.setText(selectedBundle.getString("setting.modal.save.success.message"));
            System.out.println("Profile saved for: " + updated.getUsername());
        } catch (Exception e) {
            messageLabel.setText(selectedBundle.getString("settings.modal.save.error.message") + e.getMessage());
            System.out.println("Save failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangeLocalization(ActionEvent event) {
        MenuItem selectedItem = (MenuItem) event.getSource();

        if (event.getSource() == fiItem) {
            localizationService.switchLanguage("fi");
        } else if (event.getSource() == enItem) {
            localizationService.switchLanguage("en");
        } else if (event.getSource() == arItem) {
            localizationService.switchLanguage("ar");
        } else if (event.getSource() == ruItem) {
            localizationService.switchLanguage("ru");
        }

        languageMenuButton.setText(selectedItem.getText());
        selectedBundle = localizationService.getBundle();

        // Reload the owner stage so the whole main app uses the new bundle.
        Stage currentStage = getCurrentStage();
        Stage mainStage = currentStage.getOwner() instanceof Stage owner ? owner : currentStage;

        try {
            localizationService.reloadScene(mainStage, "/main-app.fxml");

            // Close settings modal after the main app has been recreated in the new locale.
            if (currentStage != mainStage) {
                currentStage.close();
            }
        } catch (IOException e) {
            messageLabel.setText("Failed to apply language change.");
            e.printStackTrace();
        }
    }

    private void applyLanguageMenuSelection() {
        if (languageMenuButton == null) {
            return;
        }

        Locale currentLocale = localizationService.getCurrentLocale();
        String lang = currentLocale.getLanguage();
        languageMenuButton.setText(switch (lang) {
            case "fi" -> fiItem.getText();
            case "ar" -> arItem.getText();
            case "ru" -> ruItem.getText();
            default   -> enItem.getText();
        });
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
            passwordMessageLabel.setText(selectedBundle.getString("settings.change.password.blank.error.message"));
            return;
        }
        if (!BCrypt.checkpw(current, user.getPasswordHash())) {
            passwordMessageLabel.setText(selectedBundle.getString("settings.change.password.current.error.message"));
            currentPasswordField.clear();
            return;
        }
        if (!newPw.equals(newPwAgain)) {
            passwordMessageLabel.setText(selectedBundle.getString("settings.change.password.validation.error.message"));
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
            passwordMessageLabel.setText(selectedBundle.getString("settings.change.password.success.message"));
            System.out.println("Password changed for: " + updated.getUsername());
        } catch (Exception e) {
            passwordMessageLabel.setText(selectedBundle.getString("settings.change.password.error.message") + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            Stage currentStage = getCurrentStage();
            Stage mainStage = currentStage.getOwner() instanceof Stage owner ? owner : currentStage;

            // Close modal if this controller is shown in one.
            if (currentStage != mainStage) {
                currentStage.close();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"), localizationService.getBundle());
            Parent root = loader.load();
            localizationService.swapSides(root);

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
