package org.example.studystack;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class QuizModeController {

    @FXML
    public void initialize() {
        // Initialize the Quiz Mode section if needed
    }

    // Action for "Create Quiz" button
    @FXML
    private void handleCreateQuiz() {
        // You can add the logic for creating a quiz here
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Create Quiz");
        alert.setHeaderText(null);
        alert.setContentText("Create Quiz functionality will be implemented here.");
        alert.showAndWait();
    }

    // Action for "Take Quiz" button
    @FXML
    private void handleTakeQuiz() {
        // You can add the logic for taking a quiz here
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Take Quiz");
        alert.setHeaderText(null);
        alert.setContentText("Take Quiz functionality will be implemented here.");
        alert.showAndWait();
    }
}


