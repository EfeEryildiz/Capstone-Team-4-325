package org.example.studystack;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

public class HomeController {
    @FXML
    private Button logoutButton;

    @FXML
    public void initialize() {
        // No initialization needed now
    }

    @FXML
    private void handleLogout() {
        try {
            DataStore.getInstance().signOut();
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();
            Scene loginScene = new Scene(root);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(loginScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



