package org.example.studystack;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class MainController {

    @FXML
    private BorderPane contentPane;

    @FXML
    private Button homeButton;

    @FXML
    private Button flashcardsButton;

    @FXML
    private Button quizModeButton;

    @FXML
    private Button notebookButton;

    @FXML
    public void initialize() {
        //Home page is default
        loadHomePage();
    }

    @FXML
    private void loadHomePage() {
        loadPage("Home.fxml");
    }

    @FXML
    private void loadFlashcardsPage() {
        loadPage("Flashcards.fxml");
    }

    @FXML
    private void loadQuizModePage() {
        loadPage("QuizMode.fxml");
    }

    @FXML
    private void loadNotebookPage() {
        loadPage("Notebook.fxml");
    }

    private void loadPage(String fxmlFile) {
        try {
            URL fxmlLocation = getClass().getResource("/org/example/studystack/" + fxmlFile);
            if (fxmlLocation == null) {
                throw new IOException("FILE: " + fxmlFile + " not found");
            }

            //Load FXML
            Parent page = FXMLLoader.load(fxmlLocation);

            //Set the loaded page into the center of contentPane
            contentPane.setCenter(page);

        } catch (IOException e) {
            e.printStackTrace();
            //MSG Box for errors
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to Load Page");
            alert.setContentText("An error occurred while loading the page '" + fxmlFile + "': " + e.getMessage());
            alert.showAndWait();
        }
    }
}
