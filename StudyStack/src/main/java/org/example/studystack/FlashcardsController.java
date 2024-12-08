package org.example.studystack;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class FlashcardsController {

    @FXML
    private ListView<Deck> decksListView;

    @FXML
    private ListView<Flashcard> flashcardsListView;

    @FXML
    private VBox flashcardEditorVBox;

    @FXML
    private TextField questionField;

    @FXML
    private TextArea answerArea;

    @FXML
    private Button saveFlashcardButton;

    private Deck selectedDeck;

    @FXML
    public void initialize() {
        //Bind the ListView to the decks list from DataStore
        decksListView.setItems(DataStore.getInstance().getDecksList());

        //Initially disable the flashcard editor
        flashcardEditorVBox.setDisable(true);

        //Handle deck selection changes
        decksListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedDeck = newValue;
            if (newValue != null) {
                flashcardsListView.setItems(newValue.getFlashcards());
                flashcardEditorVBox.setDisable(false); //Enable editor
            } else {
                flashcardsListView.setItems(null);
                flashcardEditorVBox.setDisable(true); //Disable editor
            }
        });

        //Hide the flashcard editor initially
        flashcardEditorVBox.setVisible(false);

        //Handle flashcard selection
        flashcardsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            //Incomplete/Optional, we need to implement if we want to display flashcard details or allow editing
        });
    }

    @FXML
    private void handleNewDeck() {
        //Create a window to enter the name of the new deck
        TextInputDialog deckDialog = new TextInputDialog();
        deckDialog.setTitle("New Deck");
        deckDialog.setHeaderText("Create a New Deck");
        deckDialog.setContentText("Enter the name for the new deck:");

        Optional<String> deckResult = deckDialog.showAndWait();
        if (deckResult.isPresent()) {
            String deckName = deckResult.get().trim();
            if (!deckName.isEmpty()) {
                //Check for duplicate deck names
                boolean duplicate = DataStore.getInstance().getDecksList().stream()
                        .anyMatch(deck -> deck.getName().equalsIgnoreCase(deckName));
                if (duplicate) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Duplicate Deck Name");
                    alert.setHeaderText(null);
                    alert.setContentText("A deck with this name already exists. Please choose a different name.");
                    alert.showAndWait();
                } else {
                    Deck newDeck = new Deck(deckName);
                    DataStore.getInstance().getDecksList().add(newDeck);
                }
            } else {
                //Show an error if the deck name is empty
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Deck Name");
                alert.setHeaderText(null);
                alert.setContentText("The deck name cannot be empty.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleNewFlashcard() {
        if (selectedDeck != null) {
            flashcardEditorVBox.setVisible(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Deck Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a deck to add a flashcard.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleSaveFlashcard() {
        String question = questionField.getText().trim();
        String answer = answerArea.getText().trim();

        if (question.isEmpty() || answer.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing Information");
            alert.setHeaderText(null);
            alert.setContentText("Please enter both a question and an answer.");
            alert.showAndWait();
            return;
        }

        Flashcard newFlashcard = new Flashcard(question, answer);

        //Using multithreading to not block any other functionality
        new Thread(() -> {
            try {
                List<String> options = OpenAIAPIController.generateMultipleChoiceOptions(question, answer);
                if (options != null && !options.isEmpty()) {
                    newFlashcard.setOptions(options);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to generate multiple-choice options: " + e.getMessage());
                    alert.showAndWait();
                });
            }

            Platform.runLater(() -> {
                selectedDeck.getFlashcards().add(newFlashcard);
                // Save to Firebase after adding the flashcard
                FirebaseRealtimeDB.saveDeck(selectedDeck);
                
                flashcardEditorVBox.setVisible(false);
                questionField.clear();
                answerArea.clear();
            });
        }).start();
    }
}
