package org.example.studystack.controllers;

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
        BorderPane flashcardsPane = FXMLLoader.load(getClass().getResource("Flashcards.fxml"));
        contentPane.setCenter(flashcardsPane);
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
        BorderPane notebookPane = FXMLLoader.load(getClass().getResource("Notebook.fxml"));
        contentPane.setCenter(notebookPane);
    }
}

