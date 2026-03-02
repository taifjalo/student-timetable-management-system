package org.views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class ChatWindow extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/chat-view/messages.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.setFill(null);

        stage.setTitle("My app");
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
    }


    public static void main (String[] args) {
            launch();
    }
}
