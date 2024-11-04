package org.example.studystack;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.studystack.controllers.ProgressViewController;
import org.example.studystack.utils.FirebaseConnection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the Home.fxml file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
        Parent root = loader.load();

        // Set up the scene
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("StudySync");
        primaryStage.show();

        // Show the Progress View (UI setup)
        ProgressViewController progressView = new ProgressViewController();
        progressView.show(primaryStage);
    }

    public static void main(String[] args) {
        FirebaseConnection.initialize();
        launch(args);
    }
}
