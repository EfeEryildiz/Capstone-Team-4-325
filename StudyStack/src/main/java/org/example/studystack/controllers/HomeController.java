package org.example.studystack.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HomeController {

    @FXML
    private ImageView homeImage;

    @FXML
    private Button logInButton;

    @FXML
    private Button createAccountButton;

    @FXML
    private Label descriptionLabel;

    @FXML
    public void initialize() {


        // Optionally, set up actions for the buttons
        logInButton.setOnAction(event -> handleLogIn());
        createAccountButton.setOnAction(event -> handleCreateAccount());
    }

    private void handleLogIn() {
        // Logic for Log In button
    }

    private void handleCreateAccount() {
        // Logic for Create Account button
    }
}


