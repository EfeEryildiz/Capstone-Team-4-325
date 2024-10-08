package group.flashcardstudytool;

import group.flashcardstudytool.services.FirebaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FlashcardStudyToolApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FirebaseInitializer.initialize();
        Parent root = FXMLLoader.load(getClass().getResource("main-view.fxml"));
        stage.setTitle("Flashcard Study Tool");
        stage.setScene(new Scene(root, 600, 400));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}