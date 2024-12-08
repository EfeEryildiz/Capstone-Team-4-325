package org.example.studystack;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class SignUpController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;

    @FXML
    public void initialize() {
        // Add listeners for real-time validation
        emailField.textProperty().addListener((observable, oldValue, newValue) -> validateEmail());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> validatePassword());
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> validateConfirmPassword());
    }

    private void validateEmail() {
        String email = emailField.getText();
        if (email.isEmpty()) {
            showError(emailError, "Email is required");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError(emailError, "Please enter a valid email address");
        } else {
            hideError(emailError);
        }
    }

    private void validatePassword() {
        String password = passwordField.getText();
        if (password.isEmpty()) {
            showError(passwordError, "Password is required");
        } else if (password.length() < 6) {
            showError(passwordError, "Password must be at least 6 characters");
        } else {
            hideError(passwordError);
        }
        validateConfirmPassword(); // Revalidate confirm password when password changes
    }

    private void validateConfirmPassword() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if (confirmPassword.isEmpty()) {
            showError(confirmPasswordError, "Please confirm your password");
        } else if (!confirmPassword.equals(password)) {
            showError(confirmPasswordError, "Passwords do not match");
        } else {
            hideError(confirmPasswordError);
        }
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    @FXML
    private void handleSignUp() {
        // Validate all fields first
        validateEmail();
        validatePassword();
        validateConfirmPassword();

        // Check if there are any visible error messages
        if (emailError.isVisible() || passwordError.isVisible() || confirmPasswordError.isVisible()) {
            return;
        }

        String email = emailField.getText();
        String password = passwordField.getText();

        boolean success = FirebaseAuthService.createAccount(email, password);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully!");
            loadLoginView();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create account. Please try again.");
        }
    }

    /**
     * Handles navigation back to the login screen.
     */
    @FXML
    private void handleBackToLogin() {
        loadLoginView();
    }

    /**
     * Utility method to load the login view.
     */
    private void loadLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            Scene loginScene = new Scene(root);

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load the login view.");
        }
    }

    /**
     * Utility method to display alerts.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


