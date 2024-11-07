package org.example.studystack;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.studystack.utils.FirebaseConnection;
import org.example.studystack.controllers.ProgressViewController;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize Firebase first
            FirebaseConnection.initialize();

            // Load the main view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("StudyStack");
            primaryStage.show();

            // Create a separate window for progress view
            if (FirebaseConnection.isInitialized()) {
                // Using Platform.runLater to ensure JavaFX thread safety
                javafx.application.Platform.runLater(() -> {
                    try {
                        Stage progressStage = new Stage();
                        progressStage.setTitle("Progress View");
                        ProgressViewController progressView = new ProgressViewController();
                        progressView.show(progressStage);
                    } catch (Exception e) {
                        System.err.println("Error showing progress view: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }

        } catch (Exception e) {
            System.err.println("Application failed to start: " + e.getMessage());
            e.printStackTrace();
            // Show error dialog to user
            showError("Application Error", "Failed to start application: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}