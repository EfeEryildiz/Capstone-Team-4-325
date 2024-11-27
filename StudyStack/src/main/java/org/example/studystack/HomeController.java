package org.example.studystack;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    @FXML
    private Label descriptionLabel;

    @FXML
    private Button viewProgressButton;

    @FXML
    private Button logoutButton;

    public HomeController() {
        // Default constructor
    }

    @FXML
    public void initialize() {
        // Set up the button action for viewing progress
        viewProgressButton.setOnAction(event -> handleViewProgress());
    }

    private void handleViewProgress() {
        System.out.println("View Notes Progress button clicked!");
        // Replace with actual navigation or functionality
    }

    @FXML
    private void handleLogout() {
        // Log out from Firebase
        FirebaseAuthService.logout();

        try {
            // Load the login.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) logoutButton.getScene().getWindow();

            // Set the scene to the login screen
            Scene loginScene = new Scene(root);
            stage.setScene(loginScene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
