package org.example.studystack;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class NotebookController {

    @FXML
    private ListView<Note> notesListView;

    @FXML
    private TextArea noteTextArea;

    @FXML
    private Button newNoteButton;

    @FXML
    private Button deleteNoteButton;

    @FXML
    private Button convertToFlashcardsButton;

    private boolean isUpdatingTextArea = false;

    private Note currentNote = null;

    private static final long SAVE_DELAY_MS = 1000; // 1 second delay
    private Timer saveTimer = new Timer(true);
    private TimerTask pendingSave = null;
    private String lastSavedContent = "";
    private boolean ignoreNextUpdate = false;

    private int lastCaretPosition = 0;
    private boolean isLoadingData = false;

    @FXML
    public void initialize() {
        //Bind the ListView to the notes list from DataStore
        notesListView.setItems(DataStore.getInstance().getNotesList());
        notesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        noteTextArea.setDisable(true); //Have notes area disabled before note creation
        convertToFlashcardsButton.setDisable(true);

        //Selection changes handling
        notesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingTextArea) {
                List<Note> selectedNotes = notesListView.getSelectionModel().getSelectedItems();
                if (!selectedNotes.isEmpty()) {
                    currentNote = selectedNotes.get(selectedNotes.size() - 1); //Last selected note
                    isUpdatingTextArea = true;
                    noteTextArea.setText(currentNote.getContent());
                    noteTextArea.setDisable(false);
                    convertToFlashcardsButton.setDisable(false);
                    isUpdatingTextArea = false;
                } else {
                    noteTextArea.clear();
                    noteTextArea.setDisable(true);
                    convertToFlashcardsButton.setDisable(true);
                }
            }
        });

        // Preserve caret position when typing
        noteTextArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            if (!isLoadingData) {
                lastCaretPosition = newValue.intValue();
            }
        });

        // Modify the text property listener
        noteTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingTextArea && currentNote != null && !ignoreNextUpdate) {
                currentNote.setContent(newValue);
                lastSavedContent = newValue;
                scheduleSave();
            }
            ignoreNextUpdate = false;
        });

        // Modify the selection listener
        notesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingTextArea && newValue != null) {
                if (oldValue != null) {
                    saveCurrentNote();
                }
                currentNote = newValue;
                isUpdatingTextArea = true;
                noteTextArea.setText(newValue.getContent());
                lastSavedContent = newValue.getContent();
                isUpdatingTextArea = false;
            }
        });
    }

    private void scheduleSave() {
        if (pendingSave != null) {
            pendingSave.cancel();
        }
        
        pendingSave = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (currentNote != null && !currentNote.getContent().equals(lastSavedContent)) {
                        FirebaseRealtimeDB.saveNote(currentNote);
                        lastSavedContent = currentNote.getContent();
                    }
                });
            }
        };
        
        saveTimer.schedule(pendingSave, SAVE_DELAY_MS);
    }

    private void saveCurrentNote() {
        if (currentNote != null && !isUpdatingTextArea) {
            FirebaseRealtimeDB.saveNote(currentNote);
        }
    }

    @FXML
    private void handleNewNote() {
        //System.out.println("handleNewNote called"); //Debug stuff

        //Enter title of new note dialog
        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setTitle("New Note");
        titleDialog.setHeaderText("Create a New Note");
        titleDialog.setContentText("Enter the title for the new note:");

        Optional<String> titleResult = titleDialog.showAndWait();
        if (titleResult.isPresent()) {
            String title = titleResult.get().trim();
            if (!title.isEmpty()) {
                //Check for duplicate titles
                boolean duplicate = DataStore.getInstance().getNotesList().stream()
                        .anyMatch(note -> note.getTitle().equalsIgnoreCase(title));
                if (duplicate) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Duplicate Title");
                    alert.setHeaderText(null);
                    alert.setContentText("A note with this title already exists. Please choose a different title.");
                    alert.showAndWait();
                } else {
                    //Create the new note and add it to the DataStore
                    Note newNote = new Note(title, "");
                    DataStore.getInstance().getNotesList().add(newNote);
                    FirebaseRealtimeDB.saveNote(newNote);

                    //System.out.println("New note created: " + title); //Debug stuff

                    notesListView.getSelectionModel().select(newNote);
                }
            } else {
                //Error for empty titles
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Title");
                alert.setHeaderText(null);
                alert.setContentText("The title cannot be empty.");
                alert.showAndWait();
            }
        }
    }

    //Handle Delete Note button
    @FXML
    private void handleDeleteNote() {
        Note selectedNote = notesListView.getSelectionModel().getSelectedItem();
        if (selectedNote != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Delete Note");
            confirmDialog.setHeaderText("Delete \"" + selectedNote.getTitle() + "\"?");
            confirmDialog.setContentText("This action cannot be undone.");

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Clear the text area if we're deleting the current note
                if (selectedNote == currentNote) {
                    noteTextArea.clear();
                    noteTextArea.setDisable(true);
                    currentNote = null;
                }
                
                // Delete from Firebase
                FirebaseRealtimeDB.deleteNote(selectedNote);
                
                // Remove from local list and update UI
                Platform.runLater(() -> {
                    DataStore.getInstance().getNotesList().remove(selectedNote);
                    notesListView.getSelectionModel().clearSelection();
                    convertToFlashcardsButton.setDisable(true);
                });
            }
        }
    }

    //Handle the convert to flashcards
    @FXML
    private void handleConvertToFlashcards() {
        saveCurrentNote(); // Save before converting
        List<Note> selectedNotes = new ArrayList<>(notesListView.getSelectionModel().getSelectedItems());

        if (selectedNotes.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Notes Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a Note to convert to Flashcards.");
            alert.showAndWait();
            return;
        }

        // Using AtomicInteger for thread safe counters
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Start the conversion in a separate thread
        Thread conversionThread = new Thread(() -> {
            for (Note note : selectedNotes) {
                try {
                    String deckName = note.getTitle() + " (AI)";

                    boolean duplicateDeck = DataStore.getInstance().getDecksList().stream()
                            .anyMatch(deck -> deck.getName().equalsIgnoreCase(deckName));
                    if (duplicateDeck) {
                        failureCount.incrementAndGet();
                        continue;
                    }

                    Deck newDeck = new Deck(deckName);
                    List<Flashcard> generatedFlashcards = OpenAIAPIController.generateFlashcards(note.getContent());

                    if (generatedFlashcards != null && !generatedFlashcards.isEmpty()) {
                        Platform.runLater(() -> {
                            DataStore.getInstance().getDecksList().add(newDeck);
                            newDeck.getFlashcards().addAll(generatedFlashcards);
                            FirebaseRealtimeDB.saveDeck(newDeck);
                        });
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    failureCount.incrementAndGet();
                }
            }

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Conversion Complete");
                alert.setHeaderText(null);
                alert.setContentText("Successfully converted " + successCount.get() + " notes to flashcards.");
                alert.showAndWait();
            });
        });

        conversionThread.start();
    }

    // Add this to ensure we save when closing/switching views
    public void onClose() {
        saveCurrentNote();
        if (saveTimer != null) {
            saveTimer.cancel();
        }
    }

    // Add this method to update note content while preserving cursor
    public void updateNoteContent(Note note, String content) {
        if (currentNote != null && note.getTitle().equals(currentNote.getTitle())) {
            // Only update if the content is different from what we last saved
            if (!content.equals(lastSavedContent)) {
                isUpdatingTextArea = true;
                int caretPosition = noteTextArea.getCaretPosition();
                noteTextArea.setText(content);
                noteTextArea.positionCaret(caretPosition);
                lastSavedContent = content;
                isUpdatingTextArea = false;
            }
        }
    }
}
