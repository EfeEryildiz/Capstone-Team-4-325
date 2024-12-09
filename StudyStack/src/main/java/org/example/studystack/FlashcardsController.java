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
    private Button saveFlashcardButton;

    @FXML
    private Button deleteButton;

    private Deck selectedDeck;

    @FXML
    public void initialize() {
        //Bind the ListView to the decks list from DataStore
        decksListView.setItems(DataStore.getInstance().getDecksList());

        //Handle deck selection changes
        decksListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedDeck = newValue;
            if (newValue != null) {
                flashcardsListView.setItems(newValue.getFlashcards());
                deleteButton.setText("Delete Deck");
            } else {
                flashcardsListView.setItems(null);
            }
        });

        //Handle flashcard selection
        flashcardsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                deleteButton.setText("Delete Card");
            } else if (selectedDeck != null) {
                deleteButton.setText("Delete Deck");
            }
        });

        // Add click handler for the decksListView
        decksListView.setOnMouseClicked(event -> {
            // When clicking a deck (even if already selected), update button text
            deleteButton.setText("Delete Deck");
            // Clear any selected flashcard
            flashcardsListView.getSelectionModel().clearSelection();
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
            // Create a new dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("New Flashcard");
            dialog.setHeaderText("Create a New Flashcard");

            // Create the dialog content
            VBox content = new VBox(10);
            TextField questionField = new TextField();
            questionField.setPromptText("Enter question here...");
            TextArea answerArea = new TextArea();
            answerArea.setPromptText("Enter answer here...");
            answerArea.setPrefRowCount(3);
            answerArea.setWrapText(true);
            
            content.getChildren().addAll(
                new Label("Question:"),
                questionField,
                new Label("Answer:"),
                answerArea
            );
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Show dialog and handle result
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String question = questionField.getText().trim();
                String answer = answerArea.getText().trim();

                if (!question.isEmpty() && !answer.isEmpty()) {
                    Flashcard newFlashcard = new Flashcard(question, answer);

                    // Using multithreading to not block any other functionality
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
                        });
                    }).start();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Missing Information");
                    alert.setHeaderText(null);
                    alert.setContentText("Please enter both a question and an answer.");
                    alert.showAndWait();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Deck Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a deck to add a flashcard.");
            alert.showAndWait();
        }
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
