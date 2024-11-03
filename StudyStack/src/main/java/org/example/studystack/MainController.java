package org.example.studystack;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane contentPane;

    // Load the Home page
    @FXML
    private void loadHomePage() throws IOException {
        VBox homePane = FXMLLoader.load(getClass().getResource("Home.fxml"));
        contentPane.setCenter(homePane);
    }

    // Load the Flashcards page
    @FXML
    private void loadFlashcardsPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Flashcards.fxml"));
        VBox pane = (VBox) loader.load();
        contentPane.setCenter(pane);
    }

    // Load the Quiz Mode page
    @FXML
    private void loadQuizModePage() throws IOException {
        VBox quizModePane = FXMLLoader.load(getClass().getResource("QuizMode.fxml"));
        contentPane.setCenter(quizModePane);  // This works because contentPane is a BorderPane
    }

    // Load the Notebook page
    @FXML
    private void loadNotebookPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Notebook.fxml"));
        VBox pane = (VBox) loader.load();
        contentPane.setCenter(pane);
    }
}

