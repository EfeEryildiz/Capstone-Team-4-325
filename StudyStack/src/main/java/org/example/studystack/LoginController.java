package org.example.studystack;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    /**
     * Handles the login button action. Validates credentials with Firebase.
     */
    @FXML
    private void handleLogin() {
        String email = usernameField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Please enter both email and password.");
            return;
        }

        boolean success = FirebaseAuthService.login(email, password);
        if (success) {
            loadMainView(); // Navigate to the main screen
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password. Please try again.");
        }
    }

    /**
     * Loads the MainView after successful login.
     */
    private void loadMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
            Parent root = loader.load();

            Scene mainScene = new Scene(root);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(mainScene);
            stage.setTitle("Home");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load the main view.");
        }
    }

    /**
     * Displays an alert dialog with the specified type, title, and message.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleCreateAccount() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SignUpView.fxml"));
            Parent root = loader.load();

            Scene signUpScene = new Scene(root);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(signUpScene);
            stage.setTitle("Create Account");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load the sign-up view.");
        }
    }

}


