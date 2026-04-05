package org.service;

import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationService {

    private static final String BUNDLE_BASE_NAME = "i18n/MessagesBundle";
    private static Locale currentLocale = Locale.ENGLISH;

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME, currentLocale);
    }

    public void setLocale(Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException("Locale cannot be null");
        }
        currentLocale = locale;
        Locale.setDefault(currentLocale);
    }

    public void switchLanguage(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            throw new IllegalArgumentException("Language code cannot be null or blank");
        }

        switch (languageCode.toLowerCase()) {
            case "fi" -> setLocale(new Locale("fi", "FI"));
            case "en" -> setLocale(new Locale("en", "US"));
            case "ar" -> setLocale(new Locale("ar", "IQ"));
            case "ru" -> setLocale(new Locale("ru", "RU"));
            // reachable until these cases are added and language menu items are added to login.fxml.

            default -> throw new IllegalArgumentException("Unsupported language code: " + languageCode);
        }
    }

    public void reloadScene(Stage stage, String fxmlPath) throws IOException {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            throw new IllegalArgumentException("FXML file not found: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(resource, getBundle());
        Parent root = loader.load();
        swapSides(root);

        Scene currentScene = stage.getScene();
        if (currentScene == null) {
            stage.setScene(new Scene(root));
        } else {
            currentScene.setRoot(root);
        }
    }

    public void swapSides(Parent root){
        if (currentLocale.getLanguage().equals("ar")) {
            root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        } else {
            root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }
    }
}
