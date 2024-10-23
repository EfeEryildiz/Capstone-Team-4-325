package org.example.studystack;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    private Note selectedNote = null;

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
                    selectedNote = selectedNotes.get(selectedNotes.size() - 1); //Last selected note
                    isUpdatingTextArea = true;
                    noteTextArea.setText(selectedNote.getContent());
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

        //Handle text area changes
        noteTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdatingTextArea) {
                List<Note> selectedNotes = notesListView.getSelectionModel().getSelectedItems();
                if (!selectedNotes.isEmpty()) {
                    isUpdatingTextArea = true;
                    //Update content for the last selected note
                    selectedNotes.get(selectedNotes.size() - 1).setContent(newValue);
                    isUpdatingTextArea = false;
                }
            }
        });
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
        List<Note> selectedNotes = new ArrayList<>(notesListView.getSelectionModel().getSelectedItems());
        if (!selectedNotes.isEmpty()) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete Note(s)");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Are you sure you want to delete the selected note(s)?");
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                DataStore.getInstance().getNotesList().removeAll(selectedNotes);
                noteTextArea.clear();
                noteTextArea.setDisable(true);
                convertToFlashcardsButton.setDisable(true);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select at least one note to delete.");
            alert.showAndWait();
        }
    }

    //Handle the convert to flashcards
    @FXML
    private void handleConvertToFlashcards() {
        List<Note> selectedNotes = new ArrayList<>(notesListView.getSelectionModel().getSelectedItems());

        if (selectedNotes.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Notes Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a Note to convert to Flashcards.");
            alert.showAndWait();
            return;
        }

        //Loading indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        Dialog<Void> dialog = new Dialog<>();
        dialog.setGraphic(progressIndicator);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.setTitle("Converting Notes to Flashcards...");
        dialog.setHeaderText(null);

        //Handle cancel button
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setOnAction(event -> {
            dialog.close();
        });

        //Using AtomicInteger for thread safe counters

        //This system keeps track of how many conversions failed and how many worked, this is to provide useful data to
        //the user
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<String> failedNotes = Collections.synchronizedList(new ArrayList<>());

        //Start the conversion in a separate thread
        new Thread(() -> {
            for (Note note : selectedNotes) {
                try {
                    //Add AI to title of AI generated notes
                    String deckName = note.getTitle() + " (AI)";

                    //Check for duplicate deck names
                    boolean duplicateDeck = DataStore.getInstance().getDecksList().stream()
                            .anyMatch(deck -> deck.getName().equalsIgnoreCase(deckName));
                    if (duplicateDeck) {
                        System.err.println("Deck '" + deckName + "' already exists. Skipping creation for this note.");
                        failedNotes.add(note.getTitle() + " (Duplicate Deck)");
                        failureCount.incrementAndGet();
                        continue; //Skip creating this deck
                    }

                    Deck newDeck = new Deck(deckName);
                    Platform.runLater(() -> {
                        DataStore.getInstance().getDecksList().add(newDeck);
                        System.out.println("Created deck: " + deckName);
                    });

                    String noteContent = note.getContent();

                    //Generate flashcards from the note's content
                    List<Flashcard> generatedFlashcards = OpenAIAPIController.generateFlashcards(noteContent);

                    if (generatedFlashcards != null && !generatedFlashcards.isEmpty()){
                        //Add flashcards to the newly created deck
                        Platform.runLater(() -> {
                            newDeck.getFlashcards().addAll(generatedFlashcards);
                            System.out.println("Added " + generatedFlashcards.size() + " flashcards to deck '" + deckName + "'.");
                        });
                        successCount.incrementAndGet();
                    } else {
                        System.err.println("Failed to generate flashcards for note: " + note.getTitle());
                        failedNotes.add(note.getTitle() + " (No Flashcards Generated)");
                        failureCount.incrementAndGet();
                        Platform.runLater(() -> {
                            DataStore.getInstance().getDecksList().remove(newDeck);
                            System.out.println("Removed empty deck '" + deckName + "'.");
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    failedNotes.add(note.getTitle() + " (" + e.getMessage() + ")");
                    failureCount.incrementAndGet();
                }
            }

            Platform.runLater(() -> { //Use thread to handle flashcard conversion process in background so it does not block the rest of the application.
                dialog.close();

                StringBuilder message = new StringBuilder();
                message.append("Conversion Completed!\n");
                message.append("Successful: ").append(successCount.get()).append("\n");
                message.append("Failed: ").append(failureCount.get()).append("\n");

                if (!failedNotes.isEmpty()) {
                    message.append("\nFailed Notes:\n");
                    for (String failedNote : failedNotes) {
                        message.append("- ").append(failedNote).append("\n");
                    }
                }

//                Alert alert = new Alert(Alert.AlertType.INFORMATION); //Old debug stuff
//                alert.setTitle("Conversion Results");
//                alert.setHeaderText(null);
//                alert.setContentText(message.toString());
//                alert.showAndWait();
            });
        }).start(); //Start the background thread

        dialog.showAndWait();
    }
}
