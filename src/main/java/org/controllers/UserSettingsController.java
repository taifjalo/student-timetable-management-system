package org.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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

/**
 * Controller for the user settings modal ({@code user-settings-modal.fxml}).
 * Lets the logged-in user update their profile fields, change their password,
 * switch the application language, and log out.
 */
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

    /**
     * Injects the owning stage so the controller can close itself or swap scenes
     * without relying on the scene graph traversal.
     *
     * @param stage the stage this modal is displayed in
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * JavaFX initialize callback — pre-fills fields with the current user's data
     * and syncs the language menu button.
     */
    @FXML
    public void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        // Header
        displayNameText.setText(user.getFirstName());
        String role = user.getRole() != null
                ? capitalize(user.getRole())
                : selectedBundle.getString("settings.modal.role.label");
        roleText.setText(role);

        // Fields
        usernameField.setText(nullSafe(user.getUsername()));
        firstNameField.setText(nullSafe(user.getFirstName()));
        lastNameField.setText(nullSafe(user.getSureName()));
        emailField.setText(nullSafe(user.getEmail()));
        phoneField.setText(nullSafe(user.getPhoneNumber()));

        applyLanguageMenuSelection();

    }

    /**
     * FXML action handler for the save button.
     * Persists updated profile fields and refreshes the session user.
     */
    @FXML
    private void handleSave() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        String newUsername = usernameField.getText().trim();

        // Check for duplicate username only when it has changed
        if (!newUsername.equals(user.getUsername())) {
            User existing = userDao.findByUsername(newUsername);
            if (existing != null) {
                String msg = selectedBundle.getString("settings.modal.save.error.message") + " Username already exists";
                messageLabel.setText(msg);
                return;
            }
        }

        user.setUsername(newUsername);
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
        } catch (Exception e) {
            messageLabel.setText(selectedBundle.getString("settings.modal.save.error.message") + e.getMessage());
        }
    }

    /**
     * FXML action handler for language menu items.
     * Switches the active locale, reloads the main app scene, and closes this modal.
     *
     * @param event the action event from the selected menu item
     */
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
        // Force-close the MenuButton popup before any scene work so it doesn't
        // get stranded on screen (visible on top of all other windows) while the
        // JavaFX thread is busy loading the new scene.
        languageMenuButton.hide();
        selectedBundle = localizationService.getBundle();

        // Reload the owner stage so the whole main app uses the new bundle.
        Stage currentStage = getCurrentStage();
        Stage mainStage = currentStage.getOwner() instanceof Stage owner ? owner : currentStage;

        // Close the settings modal first so its popup windows are properly destroyed
        // before the owner stage's scene is replaced.
        if (currentStage != mainStage) {
            currentStage.close();
        }

        try {
            localizationService.reloadScene(mainStage, "/ui/main-app.fxml");
        } catch (IOException e) {
            messageLabel.setText(selectedBundle.getString("settings.modal.language.error"));
            e.printStackTrace();
        }
    }

    /**
     * Syncs the language menu button label to the currently active locale.
     */
    private void applyLanguageMenuSelection() {
        if (languageMenuButton == null) {
            return;
        }

        Locale currentLocale = localizationService.getCurrentLocale();
        String lang = currentLocale.getLanguage();
        String langLabel = switch (lang) {
            case "fi" -> fiItem.getText();
            case "ar" -> arItem.getText();
            case "ru" -> ruItem.getText();
            default   -> enItem.getText();
        };
        languageMenuButton.setText(langLabel);
    }

    /**
     * FXML action handler for the change-password button.
     * Validates the current password and the new password confirmation before
     * BCrypt-hashing and persisting the new password.
     */
    @FXML
    private void handleChangePassword() {
        passwordMessageLabel.setText("");
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

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
            passwordMessageLabel.setText(
                    selectedBundle.getString("settings.change.password.success.message"));
        } catch (Exception e) {
            passwordMessageLabel.setText(
                    selectedBundle.getString("settings.change.password.error.message") + e.getMessage());
        }
    }

    /**
     * FXML action handler for the logout button.
     * Clears the session and navigates to the login screen.
     */
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

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/auth/login.fxml"), localizationService.getBundle());
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

    /**
     * Returns the stage this controller is displayed in.
     * Falls back to resolving it from the scene graph if {@link #setStage(Stage)}
     * was not called.
     *
     * @return the owning {@link Stage}
     */
    private Stage getCurrentStage() {
        return stage != null ? stage : (Stage) messageLabel.getScene().getWindow();
    }

    /**
     * Returns the value if non-null, otherwise returns an empty string.
     *
     * @param value the string to check
     * @return {@code value} or {@code ""}
     */
    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    /**
     * Capitalizes the first character of a string and lowercases the rest.
     *
     * @param s the string to capitalize
     * @return the capitalized string, or {@code s} unchanged if null/empty
     */
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase(java.util.Locale.ROOT)
                + s.substring(1).toLowerCase(java.util.Locale.ROOT);
    }
}
