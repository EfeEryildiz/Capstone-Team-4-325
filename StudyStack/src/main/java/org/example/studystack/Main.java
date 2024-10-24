package org.example.studystack;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load the Login.fxml file instead of MainView.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        BorderPane root = loader.load();

        // Set the scene with Login layout
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Login Screen");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
