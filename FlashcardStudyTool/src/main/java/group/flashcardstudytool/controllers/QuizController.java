package group.flashcardstudytool.controllers;

import group.flashcardstudytool.models.Deck;
import group.flashcardstudytool.models.Flashcard;
import group.flashcardstudytool.models.QuizResult;
import group.flashcardstudytool.services.FlashcardService;
import group.flashcardstudytool.services.QuizService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.List;

public class QuizController {

    @FXML private Label questionLabel;
    @FXML private TextField answerField;
    @FXML private Button submitButton;
    @FXML private Label resultLabel;

    private FlashcardService flashcardService;
    private QuizService quizService;
    private List<Flashcard> flashcards;
    private int currentIndex;
    private int correctAnswers;
    private Deck currentDeck;

    public void initQuiz(Deck deck) {
        flashcardService = new FlashcardService();
        quizService = new QuizService();
        currentDeck = deck;
        loadFlashcards(deck.getId());
    }

    private void loadFlashcards(String deckId) {
        flashcardService.getFlashcardsByDeck(deckId).thenAccept(loadedFlashcards -> {
            flashcards = loadedFlashcards;
            Collections.shuffle(flashcards);
            currentIndex = 0;
            correctAnswers = 0;
            if (!flashcards.isEmpty()) {
                showNextQuestion();
            } else {
                questionLabel.setText("No flashcards available for this deck.");
                submitButton.setDisable(true);
            }
        });
    }

    @FXML
    private void submitAnswer() {
        String userAnswer = answerField.getText().trim().toLowerCase();
        String correctAnswer = flashcards.get(currentIndex).getBack().toLowerCase();

        if (userAnswer.equals(correctAnswer)) {
            correctAnswers++;
            resultLabel.setText("Correct!");
        } else {
            resultLabel.setText("Incorrect. The correct answer is: " + correctAnswer);
        }

        currentIndex++;
        if (currentIndex < flashcards.size()) {
            showNextQuestion();
        } else {
            endQuiz();
        }
    }

    private void showNextQuestion() {
        questionLabel.setText(flashcards.get(currentIndex).getFront());
        answerField.clear();
        resultLabel.setText("");
    }

    private void endQuiz() {
        QuizResult result = new QuizResult(currentDeck.getId(), flashcards.size(), correctAnswers);
        quizService.addQuizResult(result);

        questionLabel.setText("Quiz completed!");
        submitButton.setDisable(true);
        resultLabel.setText(String.format("You got %d out of %d correct.", correctAnswers, flashcards.size()));

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> ((Stage) closeButton.getScene().getWindow()).close());
        submitButton.getParent().getChildrenUnmodifiable().add(closeButton);
    }
}
