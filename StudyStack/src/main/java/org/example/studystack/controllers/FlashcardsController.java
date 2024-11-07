package org.example.studystack.controllers;

import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.auth.FirebaseAuthException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.studystack.models.FlashCardDeck;
import org.example.studystack.models.FlashCardModel;
import org.example.studystack.models.FlashCardDeck;
import org.example.studystack.models.FlashCardModel;
import org.example.studystack.services.FlashcardService;
import com.google.cloud.firestore.Firestore;

import java.util.List;

public class FlashcardsController {
    @FXML private ListView<FlashCardDeck> deckListView;
    @FXML private ListView<FlashCardModel> cardListView;
    @FXML private TextField frontTextField;
    @FXML private TextField backTextField;
    @FXML private TextField deckNameField;
    @FXML private TextArea deckDescriptionArea;
    @FXML private Button addDeckButton;
    @FXML private Button addCardButton;
    @FXML private Button editCardButton;
    @FXML private Button deleteCardButton;

    private FlashcardService flashcardService;
    private ObservableList<FlashCardDeck> decks;
    private ObservableList<FlashCardModel> flashcards;

    @FXML
    public void initialize() {
        Firestore db = FirestoreOptions.getDefaultInstance().getService();
        /* TODO: Replace with actual user ID from authentication */
        String userId = "currentUserId";
        flashcardService = new FlashcardService(db, userId);

        decks = FXCollections.observableArrayList();
        flashcards = FXCollections.observableArrayList();

        deckListView.setItems(decks);
        cardListView.setItems(flashcards);

        // Load decks
        loadDecks();

        // Set up listeners
        deckListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldDeck, newDeck) -> {
                    if (newDeck != null) {
                        loadFlashcardsForDeck(newDeck.getId());
                    }
                }
        );

        cardListView.setCellFactory(lv -> new ListCell<FlashCardModel>() {
            @Override
            protected void updateItem(FlashCardModel card, boolean empty) {
                super.updateItem(card, empty);
                if (empty || card == null) {
                    setText(null);
                } else {
                    setText(card.getFront());
                }
            }
        });
    }

    @FXML
    private void handleAddDeck() throws FirebaseAuthException {
        String name = deckNameField.getText().trim();
        String description = deckDescriptionArea.getText().trim();

        if (name.isEmpty()) {
            showAlert("Error", "Deck name cannot be empty");
            return;
        }

        FlashCardDeck newDeck = new FlashCardDeck(name, description, flashcardService.getUserId());
        flashcardService.createDeck(newDeck);
        decks.add(newDeck);
        clearDeckFields();
    }

    @FXML
    private void handleAddCard() {
        FlashCardDeck selectedDeck = deckListView.getSelectionModel().getSelectedItem();
        if (selectedDeck == null) {
            showAlert("Error", "Please select a deck first");
            return;
        }

        String front = frontTextField.getText().trim();
        String back = backTextField.getText().trim();

        if (front.isEmpty() || back.isEmpty()) {
            showAlert("Error", "Both sides of the flashcard must be filled");
            return;
        }

        FlashCardModel newCard = new FlashCardModel(front, back, selectedDeck.getId());
        flashcardService.addFlashcard(selectedDeck.getId(), newCard);
        flashcards.add(newCard);
        clearCardFields();
    }

    private void loadDecks() {
        try {
            List<FlashCardDeck> userDecks = flashcardService.getUserDecks();
            decks.setAll(userDecks);
        } catch (Exception e) {
            showAlert("Error", "Failed to load decks: " + e.getMessage());
        }
    }

    private void loadFlashcardsForDeck(String deckId) {
        try {
            List<FlashCardModel> deckCards = flashcardService.getDeckFlashcards(deckId);
            flashcards.setAll(deckCards);
        } catch (Exception e) {
            showAlert("Error", "Failed to load flashcards: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearDeckFields() {
        deckNameField.clear();
        deckDescriptionArea.clear();
    }

    private void clearCardFields() {
        frontTextField.clear();
        backTextField.clear();
    }
}