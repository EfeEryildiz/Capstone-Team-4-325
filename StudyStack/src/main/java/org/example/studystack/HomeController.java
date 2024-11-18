package org.example.studystack;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HomeController {
    @FXML
    private Label descriptionLabel;

    @FXML
    private Button viewProgressButton;

    public HomeController() {
        // Default constructor
    }

    @FXML
    public void initialize() {
        // Set up the button action for viewing progress
        viewProgressButton.setOnAction(event -> handleViewProgress());
    }

    private void handleViewProgress() {
        // Logic to handle the "View Notes Progress" button click
        System.out.println("View Notes Progress button clicked!");
        // Replace the above with your actual functionality, e.g., navigating to a new screen
        // or showing a progress dialog.
    }
}



