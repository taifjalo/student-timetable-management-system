package org.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class CalendarApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"), bundle);

//        Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("Easytable");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
