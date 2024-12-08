package org.example.studystack;

import com.google.firebase.database.*;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FirebaseRealtimeDB {
    private static final Logger logger = Logger.getLogger(FirebaseRealtimeDB.class.getName());
    private static final DatabaseReference database;
    private static String currentUserUid = null;

    static {
        try {
            FirebaseConnection.initialize();
            database = FirebaseDatabase.getInstance().getReference();
            logger.info("Firebase Realtime Database initialized successfully");
        } catch (Exception e) {
            logger.severe("Failed to initialize Firebase Realtime Database: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Firebase Realtime Database", e);
        }
    }

    public static void setCurrentUser(String uid) {
        currentUserUid = uid;
        logger.info("Current user set to: " + uid);
        loadUserData();
    }

    public static void saveNote(Note note) {
        if (currentUserUid == null) {
            return;
        }

        DatabaseReference userNotesRef = database.child("users").child(currentUserUid).child("notes");
        logger.info("Attempting to save note to path: " + userNotesRef.getPath().toString());
        

        Map<String, String> noteData = new HashMap<>();
        noteData.put("title", note.getTitle());
        noteData.put("content", note.getContent());

        userNotesRef.child(note.getTitle()).setValue(noteData, (error, ref) -> {
            if (error == null) {
                logger.info("Note saved successfully to: " + ref.getPath().toString());
            } else {
                logger.severe("Failed to save note: " + error.getMessage() + "\nDetails: " + error.getDetails());
            }
        });
    }

    public static void deleteNote(Note note) {
        if (currentUserUid == null) {
            logger.warning("Cannot delete note: No user is currently logged in");
            return;
        }

        DatabaseReference noteRef = database.child("users")
                                          .child(currentUserUid)
                                          .child("notes")
                                          .child(note.getTitle());

        logger.info("Attempting to delete note at path: " + noteRef.getPath().toString());

        noteRef.removeValue((error, ref) -> {
            if (error == null) {
                logger.info("Note deleted successfully from path: " + ref.getPath().toString());
            } else {
                logger.severe("Failed to delete note: " + error.getMessage());
            }
        });
    }

    public static void saveDeck(Deck deck) {
        if (currentUserUid == null) {
            logger.warning("Cannot save deck: No user is currently logged in");
            return;
        }

        DatabaseReference userDecksRef = database.child("users").child(currentUserUid).child("decks");
        logger.info("Attempting to save deck to path: " + userDecksRef.getPath().toString());

        Map<String, Object> deckData = new HashMap<>();
        deckData.put("name", deck.getName());
        
        
        List<Map<String, String>> flashcardsData = new ArrayList<>();
        for (Flashcard card : deck.getFlashcards()) {
            Map<String, String> cardData = new HashMap<>();
            cardData.put("question", card.getQuestion());
            cardData.put("answer", card.getAnswer());
            flashcardsData.add(cardData);
        }
        deckData.put("flashcards", flashcardsData);

        userDecksRef.child(deck.getName()).setValue(deckData, (error, ref) -> {
            if (error == null) {
                logger.info("Deck saved successfully to: " + ref.getPath().toString());
            } else {
                logger.severe("Failed to save deck: " + error.getMessage());
            }
        });
    }

    public static void deleteDeck(Deck deck) {
        if (currentUserUid == null) {
            logger.warning("Cannot delete deck: No user is currently logged in");
            return;
        }

        DatabaseReference deckRef = database.child("users")
                                          .child(currentUserUid)
                                          .child("decks")
                                          .child(deck.getName());

        logger.info("Attempting to delete deck at path: " + deckRef.getPath().toString());

        deckRef.removeValue((error, ref) -> {
            if (error == null) {
                logger.info("Deck deleted successfully from path: " + ref.getPath().toString());
            } else {
                logger.severe("Failed to delete deck: " + error.getMessage());
            }
        });
    }

    private static void loadUserData() {
        if (currentUserUid == null) return;
        
        // Clear existing data first
        DataStore.getInstance().clear();

        // Load notes
        DatabaseReference userNotesRef = database.child("users").child(currentUserUid).child("notes");
        userNotesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Platform.runLater(() -> {
                    DataStore.getInstance().getNotesList().clear(); // Clear again before loading new data
                    for (DataSnapshot noteSnapshot : snapshot.getChildren()) {
                        String title = noteSnapshot.child("title").getValue(String.class);
                        String content = noteSnapshot.child("content").getValue(String.class);
                        if (title != null && content != null) {
                            Note note = new Note(title, content);
                            DataStore.getInstance().getNotesList().add(note);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                logger.severe("Failed to load notes: " + error.getMessage());
            }
        });

        // Load decks
        DatabaseReference userDecksRef = database.child("users").child(currentUserUid).child("decks");
        userDecksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Platform.runLater(() -> {
                    DataStore.getInstance().getDecksList().clear(); // Clear again before loading new data
                    for (DataSnapshot deckSnapshot : snapshot.getChildren()) {
                        String name = deckSnapshot.child("name").getValue(String.class);
                        if (name != null) {
                            Deck deck = new Deck(name);
                            // Load flashcards if they exist
                            DataSnapshot flashcardsSnapshot = deckSnapshot.child("flashcards");
                            if (flashcardsSnapshot.exists()) {
                                for (DataSnapshot cardSnapshot : flashcardsSnapshot.getChildren()) {
                                    String question = cardSnapshot.child("question").getValue(String.class);
                                    String answer = cardSnapshot.child("answer").getValue(String.class);
                                    if (question != null && answer != null) {
                                        Flashcard card = new Flashcard(question, answer);
                                        // Load options if they exist
                                        DataSnapshot optionsSnapshot = cardSnapshot.child("options");
                                        if (optionsSnapshot.exists()) {
                                            List<String> options = new ArrayList<>();
                                            for (DataSnapshot optionSnapshot : optionsSnapshot.getChildren()) {
                                                String option = optionSnapshot.getValue(String.class);
                                                if (option != null) {
                                                    options.add(option);
                                                }
                                            }
                                            card.setOptions(options);
                                        }
                                        deck.getFlashcards().add(card);
                                    }
                                }
                            }
                            DataStore.getInstance().getDecksList().add(deck);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                logger.severe("Failed to load decks: " + error.getMessage());
            }
        });
    }
}