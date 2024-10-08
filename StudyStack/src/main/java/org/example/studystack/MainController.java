package org.example.studystack;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane contentPane;

    // Load the Home page
    @FXML
    private void loadHomePage() throws IOException {
        BorderPane homePane = FXMLLoader.load(getClass().getResource("Home.fxml"));
        contentPane.setCenter(homePane);
    }

    // Load the Flashcards page
    @FXML
    private void loadFlashcardsPage() throws IOException {
        BorderPane flashcardsPane = FXMLLoader.load(getClass().getResource("Flashcards.fxml"));
        contentPane.setCenter(flashcardsPane);
    }

    // Load the Quiz Mode page
    @FXML
    private void loadQuizModePage() throws IOException {
        BorderPane quizModePane = FXMLLoader.load(getClass().getResource("QuizMode.fxml"));
        contentPane.setCenter(quizModePane);
    }

    // Load the Notebook page
    @FXML
    private void loadNotebookPage() throws IOException {
        BorderPane notebookPane = FXMLLoader.load(getClass().getResource("Notebook.fxml"));
        contentPane.setCenter(notebookPane);
    }
}

