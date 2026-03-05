package org.views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controllers.ChatController;


public class ChatWindow extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/chat-view/messages.fxml"));
        Parent root = loader.load();
        ChatController ChatController = loader.getController();
        ChatController.setUserId(Long.valueOf(getParameters().getRaw().get(0)));
        Scene scene = new Scene(root);
        scene.setFill(null);

        stage.setTitle("My app");
        stage.setScene(scene);
        stage.show();
    }


    public static void main (String[] args) {
            launch(args);
    }
}
