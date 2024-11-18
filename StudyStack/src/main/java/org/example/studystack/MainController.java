package org.example.studystack;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane contentPane;

    @FXML
    public void initialize() {
        // Load the home page as the default view
        loadHomePage();
    }

    @FXML
    private void loadHomePage() {
        loadView("Home.fxml");
    }

    @FXML
    private void loadFlashcardsPage() {
        loadView("Flashcards.fxml");
    }

    @FXML
    private void loadQuizModePage() {
        loadView("QuizMode.fxml");
    }

    @FXML
    private void loadNotebookPage() {
        loadView("Notebook.fxml");
    }

    // Utility method to load views dynamically into the contentPane
    private void loadView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Node view = loader.load();
            contentPane.setCenter(view); // Set the new view in the center of the BorderPane
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


