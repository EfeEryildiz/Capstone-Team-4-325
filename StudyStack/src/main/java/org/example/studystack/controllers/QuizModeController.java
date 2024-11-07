package org.example.studystack.controllers;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.studystack.models.FlashCardDeck;
import org.example.studystack.models.FlashCardModel;
import org.example.studystack.services.FlashcardService;
import org.example.studystack.utils.QuizResult;
import java.util.*;

public class QuizModeController {
    @FXML private ComboBox<FlashCardDeck> deckSelector;
    @FXML private Label questionLabel;
    @FXML private TextArea answerArea;
    @FXML private Button checkAnswerButton;
    @FXML private Button nextButton;
    @FXML private Label scoreLabel;
    @FXML private ProgressBar progressBar;

    private FlashcardService flashcardService;
    private List<FlashCardModel> currentQuizCards;
    private int currentCardIndex;
    private int correctAnswers;
    private long quizStartTime;
    private boolean isAnswerRevealed;

    @FXML
    public void initialize() {
        Firestore db = FirestoreOptions.getDefaultInstance().getService();
        String userId = "currentUserId"; /* TODO: Replace with actual user ID */
        flashcardService = new FlashcardService(db, userId);

        loadDecks();
        setupQuizControls();
    }

    private void setupQuizControls() {
        // Initialize buttons and controls
        checkAnswerButton.setDisable(true);
        nextButton.setDisable(true);

        // Set up initial state
        questionLabel.setText("Select a deck to start a quiz");
        answerArea.setWrapText(true);

        // Add listeners for text input
        answerArea.textProperty().addListener((observable, oldValue, newValue) -> {
            checkAnswerButton.setDisable(newValue.trim().isEmpty());
        });

        // Configure progress indicators
        progressBar.setProgress(0);
        scoreLabel.setText("Score: 0/0");

        // Set up deck selector
        deckSelector.setPromptText("Select a deck");
        deckSelector.setCellFactory(lv -> new ListCell<FlashCardDeck>() {
            @Override
            protected void updateItem(FlashCardDeck deck, boolean empty) {
                super.updateItem(deck, empty);
                if (empty || deck == null) {
                    setText(null);
                } else {
                    setText(deck.getName());
                }
            }
        });

        // Set up button handlers
        checkAnswerButton.setOnAction(event -> handleCheckAnswer());
        nextButton.setOnAction(event -> handleNextCard());
    }

    private void loadDecks() {
        try {
            List<FlashCardDeck> decks = flashcardService.getUserDecks();
            deckSelector.getItems().addAll(decks);
            deckSelector.setOnAction(e -> startNewQuiz());
        } catch (Exception e) {
            showAlert("Error", "Failed to load decks: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateQuiz() {
        FlashCardDeck selectedDeck = deckSelector.getValue();
        if (selectedDeck == null) {
            showAlert("Error", "Please select a deck first");
            return;
        }
        startNewQuiz();
    }

    @FXML
    private void handleCheckAnswer() {
        if (currentCardIndex >= currentQuizCards.size()) return;

        FlashCardModel currentCard = currentQuizCards.get(currentCardIndex);
        String userAnswer = answerArea.getText().trim();
        String correctAnswer = currentCard.getBack().trim();

        boolean isCorrect = userAnswer.equalsIgnoreCase(correctAnswer);

        if (!isAnswerRevealed) {
            currentCard.updateSuccessRate(isCorrect);
            if (isCorrect) correctAnswers++;

            // Show correct answer
            answerArea.setText("Your answer: " + userAnswer + "\nCorrect answer: " + correctAnswer);
            answerArea.setEditable(false);
            checkAnswerButton.setDisable(true);
            nextButton.setDisable(false);
            isAnswerRevealed = true;

            // Update Firebase
            flashcardService.updateFlashcard(currentCard.getDeckId(), currentCard);
        }
    }

    @FXML
    private void handleNextCard() {
        currentCardIndex++;
        if (currentCardIndex >= currentQuizCards.size()) {
            finishQuiz();
        } else {
            showNextCard();
        }
    }

    private void startNewQuiz() {
        try {
            FlashCardDeck selectedDeck = deckSelector.getValue();
            currentQuizCards = flashcardService.getDeckFlashcards(selectedDeck.getId());
            Collections.shuffle(currentQuizCards);

            currentCardIndex = 0;
            correctAnswers = 0;
            quizStartTime = System.currentTimeMillis();

            showNextCard();
            updateProgress();
        } catch (Exception e) {
            showAlert("Error", "Failed to start quiz: " + e.getMessage());
        }
    }

    private void showNextCard() {
        FlashCardModel currentCard = currentQuizCards.get(currentCardIndex);
        questionLabel.setText(currentCard.getFront());
        answerArea.clear();
        answerArea.setEditable(true);
        checkAnswerButton.setDisable(false);
        nextButton.setDisable(true);
        isAnswerRevealed = false;
        updateProgress();
    }

    private void finishQuiz() {
        long quizDuration = (System.currentTimeMillis() - quizStartTime) / 1000; // Convert to seconds
        double accuracy = (double) correctAnswers / currentQuizCards.size() * 100;

        QuizResult result = new QuizResult(new Date(), accuracy, quizDuration);

        // Save to Firebase
        DocumentReference resultRef = FirestoreOptions.getDefaultInstance().getService()
                .collection("ProgressReports")
                .document();
        resultRef.set(result.toMap());

        // Show results
        showAlert("Quiz Complete",
                String.format("Accuracy: %.1f%%\nTime taken: %d seconds\nCorrect answers: %d/%d",
                        accuracy, quizDuration, correctAnswers, currentQuizCards.size()));

        // Reset UI
        questionLabel.setText("Select a deck to start a new quiz");
        answerArea.clear();
        checkAnswerButton.setDisable(true);
        nextButton.setDisable(true);
    }

    private void updateProgress() {
        double progress = (double) currentCardIndex / currentQuizCards.size();
        progressBar.setProgress(progress);
        scoreLabel.setText(String.format("Score: %d/%d", correctAnswers, currentQuizCards.size()));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}