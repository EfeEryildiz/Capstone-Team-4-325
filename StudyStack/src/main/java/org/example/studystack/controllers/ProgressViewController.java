package org.example.studystack.controllers;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.example.studystack.utils.FirebaseAnalytics;
import org.example.studystack.utils.QuizResult;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class ProgressViewController {

    private Label averageAccuracyLabel;
    private Label totalQuizzesLabel;
    private Label totalTimeSpentLabel;
    private LineChart<Number, Number> progressChart;
    private FirebaseAnalytics firebaseAnalytics = new FirebaseAnalytics();

    public void show(Stage stage) {
        try {
            setupUI(stage);

            // Initialize Firebase Analytics
            firebaseAnalytics = new FirebaseAnalytics();

            // Use Platform.runLater to ensure JavaFX thread safety
            Platform.runLater(() -> {
                firebaseAnalytics.fetchQuizResultsSync(results -> {
                    if (results.isEmpty()) {
                        updateUIForNoData();
                    } else {
                        populateStatistics(results);
                    }
                });
            });

        } catch (Exception e) {
            System.err.println("Error in ProgressViewController: " + e.getMessage());
            showError("Error", "Failed to initialize progress view: " + e.getMessage());
        }
    }

    private void updateUIForNoData() {
        Platform.runLater(() -> {
            averageAccuracyLabel.setText("Average Accuracy: No data available");
            totalQuizzesLabel.setText("Total Quizzes Taken: 0");
            totalTimeSpentLabel.setText("Total Time Spent: 0s");
            progressChart.getData().clear();
        });
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    private void setupUI(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root = new VBox(10);
        root.setPadding(new Insets(10));

        // Labels for statistics
        averageAccuracyLabel = new Label("Average Accuracy: Loading...");
        totalQuizzesLabel = new Label("Total Quizzes Taken: Loading...");
        totalTimeSpentLabel = new Label("Total Time Spent: Loading...");

        // LineChart for accuracy over time
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        progressChart = new LineChart<>(xAxis, yAxis);
        progressChart.setTitle("Quiz Accuracy Over Time");

        root.getChildren().addAll(averageAccuracyLabel, totalQuizzesLabel, totalTimeSpentLabel, progressChart);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Fetch data and update the UI
        firebaseAnalytics.fetchQuizResults(this::populateStatistics);
    }

    private void populateStatistics(List<QuizResult> results) {
        if (results.isEmpty()) {
            averageAccuracyLabel.setText("Average Accuracy: No data available");
            totalQuizzesLabel.setText("Total Quizzes Taken: 0");
            totalTimeSpentLabel.setText("Total Time Spent: 0s");
            return;
        }

        double totalAccuracy = 0;
        long totalTime = 0;
        int count = results.size();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Accuracy Progress");

        for (int i = 0; i < count; i++) {
            QuizResult result = results.get(i);
            totalAccuracy += result.getAccuracy();
            totalTime += result.getTimeSpent();
            series.getData().add(new XYChart.Data<>(i + 1, result.getAccuracy()));
        }

        progressChart.getData().add(series);

        averageAccuracyLabel.setText(String.format("Average Accuracy: %.2f%%", totalAccuracy / count));
        totalQuizzesLabel.setText("Total Quizzes Taken: " + count);
        totalTimeSpentLabel.setText("Total Time Spent: " + totalTime + "s");
    }
}
