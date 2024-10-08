package group.flashcardstudytool.controllers;

import group.flashcardstudytool.models.Deck;
import group.flashcardstudytool.models.Flashcard;
import group.flashcardstudytool.services.DeckService;
import group.flashcardstudytool.services.FlashcardService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML private TextField frontText;
    @FXML private TextField backText;
    @FXML private TableView<Flashcard> flashcardTable;
    @FXML private TableColumn<Flashcard, String> frontColumn;
    @FXML private TableColumn<Flashcard, String> backColumn;
    @FXML private Button addButton;
    @FXML private ComboBox<Deck> deckComboBox;
    @FXML private TextField newDeckNameField;

    private FlashcardService flashcardService;
    private DeckService deckService;
    private Flashcard selectedFlashcard;

    @FXML
    public void initialize() {
        flashcardService = new FlashcardService();
        deckService = new DeckService();
        frontColumn.setCellValueFactory(new PropertyValueFactory<>("front"));
        backColumn.setCellValueFactory(new PropertyValueFactory<>("back"));
        setupTableSelection();
        loadDecks();
        deckComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadFlashcards(newVal.getId());
            }
        });
    }

    private void setupTableSelection() {
        flashcardTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedFlashcard = newSelection;
                frontText.setText(selectedFlashcard.getFront());
                backText.setText(selectedFlashcard.getBack());
                addButton.setText("Update Flashcard");
            } else {
                selectedFlashcard = null;
                frontText.clear();
                backText.clear();
                addButton.setText("Add Flashcard");
            }
        });
    }

    @FXML
    public void addOrUpdateFlashcard() {
        String front = frontText.getText().trim();
        String back = backText.getText().trim();
        Deck selectedDeck = deckComboBox.getValue();
        if (!front.isEmpty() && !back.isEmpty() && selectedDeck != null) {
            if (selectedFlashcard == null) {
                Flashcard flashcard = new Flashcard(front, back, selectedDeck.getId());
                flashcardService.addFlashcard(flashcard).thenRun(() -> loadFlashcards(selectedDeck.getId()));
            } else {
                selectedFlashcard.setFront(front);
                selectedFlashcard.setBack(back);
                flashcardService.updateFlashcard(selectedFlashcard).thenRun(() -> loadFlashcards(selectedDeck.getId()));
            }
            frontText.clear();
            backText.clear();
            flashcardTable.getSelectionModel().clearSelection();
        }
    }

    @FXML
    public void deleteFlashcard() {
        if (selectedFlashcard != null) {
            Deck selectedDeck = deckComboBox.getValue();
            flashcardService.deleteFlashcard(selectedFlashcard.getId()).thenRun(() -> loadFlashcards(selectedDeck.getId()));
            frontText.clear();
            backText.clear();
            flashcardTable.getSelectionModel().clearSelection();
        }
    }

    @FXML
    public void addNewDeck() {
        String deckName = newDeckNameField.getText().trim();
        if (!deckName.isEmpty()) {
            Deck newDeck = new Deck(deckName);
            deckService.addDeck(newDeck).thenRun(this::loadDecks);
            newDeckNameField.clear();
        }
    }

    @FXML
    public void startQuiz() {
        Deck selectedDeck = deckComboBox.getValue();
        if (selectedDeck != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/quiz-view.fxml"));
                Parent root = loader.load();
                QuizController quizController = loader.getController();
                quizController.initQuiz(selectedDeck);
                Stage stage = new Stage();
                stage.setTitle("Quiz - " + selectedDeck.getName());
                stage.setScene(new Scene(root, 400, 300));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Could not start the quiz. Please try again.");
            }
        } else {
            showAlert("No Deck Selected", "Please select a deck to start the quiz.");
        }
    }

    private void loadDecks() {
        deckService.getAllDecks().thenAccept(decks -> {
            deckComboBox.getItems().setAll(decks);
            if (!decks.isEmpty()) {
                deckComboBox.getSelectionModel().select(0);
            }
        });
    }

    private void loadFlashcards(String deckId) {
        flashcardService.getFlashcardsByDeck(deckId).thenAccept(flashcards -> {
            flashcardTable.getItems().setAll(flashcards);
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}