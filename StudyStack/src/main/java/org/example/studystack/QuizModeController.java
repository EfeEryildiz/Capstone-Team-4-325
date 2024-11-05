package org.example.studystack;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.*;

public class QuizModeController {

    @FXML
    private VBox quizVBox;

    @FXML
    private Label questionLabel;

    private ToggleGroup optionsGroup;

    @FXML
    private VBox optionsVBox;

    @FXML
    private Label feedbackLabel;

    @FXML
    private Button submitButton;

    @FXML
    private ComboBox<Deck> deckComboBox;

    @FXML
    private Button startQuizButton;

    private List<Flashcard> flashcards;
    private int currentIndex = 0;
    private int score = 0;

    private Map<Flashcard, List<String>> multipleChoiceOptions;

    @FXML
    public void initialize() {
        //Bind the ComboBox to the decks list from DataStore
        deckComboBox.setItems(DataStore.getInstance().getDecksList());

        //Customize the ComboBox display
        deckComboBox.setCellFactory(param -> new ListCell<Deck>() {
            @Override
            protected void updateItem(Deck item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });
        deckComboBox.setButtonCell(new ListCell<Deck>() {
            @Override
            protected void updateItem(Deck item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });

        //Hide quiz components initially
        quizVBox.setVisible(false);
    }

    //Handle start quiz button
    @FXML
    private void handleStartQuiz() {
        Deck selectedDeck = deckComboBox.getSelectionModel().getSelectedItem();
        if (selectedDeck != null && !selectedDeck.getFlashcards().isEmpty()) {
            flashcards = new ArrayList<>(selectedDeck.getFlashcards());
            currentIndex = 0;
            score = 0;

            //Initialize the multiple choice options map
            multipleChoiceOptions = new HashMap<>();

            //Show a loading indicator
            ProgressIndicator progressIndicator = new ProgressIndicator();
            Dialog<Void> dialog = new Dialog<>();
            dialog.setGraphic(progressIndicator);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            dialog.setTitle("Preparing Quiz...");
            dialog.setHeaderText(null);

            //Prepare options in a separate thread
            new Thread(() -> {
                try {
                    for (Flashcard flashcard : flashcards) {
                        if (flashcard.getOptions() != null && !flashcard.getOptions().isEmpty()) {
                            multipleChoiceOptions.put(flashcard, new ArrayList<>(flashcard.getOptions()));
                        } else {
                            //Generate options and store them
                            List<String> options = OpenAIAPIController.generateMultipleChoiceOptions(flashcard.getQuestion(), flashcard.getAnswer());
                            if (options != null && !options.isEmpty()) {
                                flashcard.setOptions(options);
                                multipleChoiceOptions.put(flashcard, new ArrayList<>(options));
                            } else {
                                //Handle error
                                multipleChoiceOptions.put(flashcard, new ArrayList<>(Collections.singletonList(flashcard.getAnswer())));
                            }
                        }
                    }

                    Platform.runLater(() -> {
                        dialog.close();
                        //Start quiz
                        quizVBox.setVisible(true);
                        startQuizButton.setDisable(true);
                        deckComboBox.setDisable(true);
                        displayCurrentQuestion();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        dialog.close();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("An error occurred while preparing the quiz: " + e.getMessage());
                        alert.showAndWait();
                    });
                }
            }).start();

            dialog.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Deck Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a deck with flashcards to start the quiz.");
            alert.showAndWait();
        }
    }

    private void displayCurrentQuestion() {
        Flashcard currentFlashcard = flashcards.get(currentIndex);
        questionLabel.setText("Question " + (currentIndex + 1) + ": " + currentFlashcard.getQuestion());
        feedbackLabel.setText("");

        //Clear previous options
        optionsVBox.getChildren().clear();
        optionsGroup = new ToggleGroup();

        List<String> options = new ArrayList<>(multipleChoiceOptions.get(currentFlashcard));

        //Shuffle options
        Collections.shuffle(options);

        for (String option : options) {
            RadioButton radioButton = new RadioButton(option);
            radioButton.setToggleGroup(optionsGroup);
            optionsVBox.getChildren().add(radioButton);
        }
    }

    //Handle Submit Answer button action
    @FXML
    private void handleSubmitAnswer() {
        Flashcard currentFlashcard = flashcards.get(currentIndex);

        //Get selected option
        RadioButton selectedOption = (RadioButton) optionsGroup.getSelectedToggle();
        if (selectedOption != null) {
            String userAnswer = selectedOption.getText();
            String correctAnswer = currentFlashcard.getAnswer();

            if (userAnswer.equalsIgnoreCase(correctAnswer.trim())) {
                feedbackLabel.setText("Correct!");
                score++;
            } else {
                feedbackLabel.setText("Incorrect. The correct answer is: " + correctAnswer);
            }

            currentIndex++;
            if (currentIndex < flashcards.size()) {
                displayCurrentQuestion();
            } else {
                //Quiz finished
                questionLabel.setText("Quiz completed! Your score: " + score + " out of " + flashcards.size());
                optionsVBox.getChildren().clear();
                submitButton.setDisable(true);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Option Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select an option before submitting.");
            alert.showAndWait();
        }
    }
}
