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

    @FXML
    private Button deleteButton;

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
                flashcardEditorVBox.setDisable(false);
                deleteButton.setText("Delete Deck");
            } else {
                flashcardsListView.setItems(null);
                flashcardEditorVBox.setDisable(true);
            }
        });

        //Hide the flashcard editor initially
        flashcardEditorVBox.setVisible(false);

        //Handle flashcard selection
        flashcardsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                deleteButton.setText("Delete Flashcard");
            } else if (selectedDeck != null) {
                deleteButton.setText("Delete Deck");
            }
        });
    }

    @FXML
    private void handleNewDeck() {
        TextInputDialog deckDialog = new TextInputDialog();
        deckDialog.setTitle("New Deck");
        deckDialog.setHeaderText("Create a New Deck");
        deckDialog.setContentText("Enter the name for the new deck:");

        Optional<String> deckResult = deckDialog.showAndWait();
        if (deckResult.isPresent()) {
            String deckName = deckResult.get().trim();
            if (!deckName.isEmpty()) {
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
                    FirebaseRealtimeDB.saveDeck(newDeck);
                }
            } else {
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
                FirebaseRealtimeDB.saveDeck(selectedDeck);
                
                flashcardEditorVBox.setVisible(false);
                questionField.clear();
                answerArea.clear();
            });
        }).start();
    }

    @FXML
    private void handleDelete() {
        Deck selectedDeck = decksListView.getSelectionModel().getSelectedItem();
        Flashcard selectedFlashcard = flashcardsListView.getSelectionModel().getSelectedItem();
        
        if (selectedFlashcard != null) {
            // Delete Flashcard
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Delete Flashcard");
            confirmDialog.setHeaderText("Delete this flashcard?");
            confirmDialog.setContentText("Question: " + selectedFlashcard.getQuestion());

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                selectedDeck.getFlashcards().remove(selectedFlashcard);
                flashcardsListView.getSelectionModel().clearSelection();
                flashcardEditorVBox.setVisible(false);
                questionField.clear();
                answerArea.clear();
                FirebaseRealtimeDB.saveDeck(selectedDeck);
            }
        } else if (selectedDeck != null) {
            // Delete Deck
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Delete Deck");
            confirmDialog.setHeaderText("Delete this deck?");
            confirmDialog.setContentText("Deck: " + selectedDeck.getName());

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                DataStore.getInstance().getDecksList().remove(selectedDeck);
                FirebaseRealtimeDB.deleteDeck(selectedDeck);
                decksListView.getSelectionModel().clearSelection();
                flashcardsListView.setItems(null);
                flashcardEditorVBox.setVisible(false);
                questionField.clear();
                answerArea.clear();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a deck or flashcard to delete.");
            alert.showAndWait();
        }
    }
}
