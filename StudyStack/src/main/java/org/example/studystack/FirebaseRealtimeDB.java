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
    private static DatabaseReference database;
    private static String currentUserUid = null;
    private static boolean isInitialized = false;
    private static boolean isAuthInitialized = false;

    public static synchronized void initialize() {
        if (isInitialized) {
            logger.info("Firebase Realtime Database already initialized");
            return;
        }

        try {
            // Make sure basic Firebase is initialized first
            FirebaseConnection.initialize();
            
            if (!FirebaseConnection.isInitialized()) {
                throw new RuntimeException("Firebase Connection not initialized");
            }

            database = FirebaseDatabase.getInstance().getReference();
            isInitialized = true;
            
            // If we already have a user ID, initialize auth now
            if (currentUserUid != null && !isAuthInitialized) {
                initializeAuth(currentUserUid);
            }
            
            logger.info("Firebase Realtime Database initialized successfully");
        } catch (Exception e) {
            logger.severe("Failed to initialize Firebase Realtime Database: " + e.getMessage());
            e.printStackTrace();
            database = null;
            throw new RuntimeException("Failed to initialize Firebase Realtime Database", e);
        }
    }

    private static synchronized void initializeAuth(String uid) {
        if (isAuthInitialized) {
            logger.info("Auth already initialized for user: " + currentUserUid);
            return;
        }
        
        try {
            currentUserUid = uid;
            isAuthInitialized = true;
            logger.info("Firebase Auth initialized with user: " + uid);
            loadUserData();
        } catch (Exception e) {
            logger.severe("Failed to initialize Firebase Auth: " + e.getMessage());
            isAuthInitialized = false;
            currentUserUid = null;  // Reset on failure
        }
    }

    private static boolean ensureInitialized() {
        if (!isInitialized) {
            initialize();
        }
        return database != null;
    }

    private static boolean checkAuthentication() {
        logger.info("Checking authentication - User ID: " + currentUserUid + 
                   ", Auth Initialized: " + isAuthInitialized + 
                   ", DB Initialized: " + isInitialized);
        
        if (currentUserUid == null) {
            logger.warning("No user is currently logged in");
            return false;
        }
        if (!isAuthInitialized) {
            logger.warning("Auth not initialized");
            return false;
        }
        if (!isInitialized) {
            logger.warning("Database not initialized");
            return false;
        }
        return true;
    }

    public static void setCurrentUser(String uid) {
        logger.info("Setting current user: " + uid);
        
        if (uid == null) {
            logger.warning("Attempted to set null user ID");
            return;
        }

        // Always initialize Firebase first
        initialize();
        
        if (!isInitialized) {
            logger.severe("Failed to initialize Firebase before setting user");
            return;
        }
        
        // Then initialize auth
        initializeAuth(uid);
        
        logger.info("Current user set successfully: " + uid + 
                   " (Auth Initialized: " + isAuthInitialized + 
                   ", DB Initialized: " + isInitialized + ")");
    }

    public static void saveNote(Note note) {
        if (!checkAuthentication() || note == null || note.getContent() == null) {
            return;
        }

        DatabaseReference userNotesRef = database.child("users")
                .child(currentUserUid)
                .child("notes")
                .child(note.getTitle());

        Map<String, String> noteData = new HashMap<>();
        noteData.put("title", note.getTitle());
        noteData.put("content", note.getContent());

        userNotesRef.updateChildren(new HashMap<>(noteData), (error, ref) -> {
            if (error == null) {
                logger.info("Note saved successfully to: " + ref.getPath().toString());
            } else {
                logger.severe("Failed to save note: " + error.getMessage());
            }
        });
    }

    public static void deleteNote(Note note) {
        if (!checkAuthentication()) {
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
        logger.info("Attempting to save deck: " + deck.getName());
        
        if (!checkAuthentication() || !ensureInitialized()) {
            logger.warning("Cannot save deck: Database not initialized or user not logged in" +
                         " (User: " + currentUserUid + 
                         ", Auth Init: " + isAuthInitialized + 
                         ", DB Init: " + isInitialized + ")");
            return;
        }

        try {
            DatabaseReference userDecksRef = database.child("users").child(currentUserUid).child("decks");
            logger.info("Saving deck to path: " + userDecksRef.getPath().toString());

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
        } catch (Exception e) {
            logger.severe("Error saving deck: " + e.getMessage());
        }
    }

    public static void deleteDeck(Deck deck) {
        if (!checkAuthentication()) {
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

        DatabaseReference userNotesRef = database.child("users").child(currentUserUid).child("notes");
        userNotesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Platform.runLater(() -> {
                    Map<String, Note> existingNotes = new HashMap<>();
                    DataStore.getInstance().getNotesList().forEach(note -> 
                        existingNotes.put(note.getTitle(), note));

                    for (DataSnapshot noteSnapshot : snapshot.getChildren()) {
                        String title = noteSnapshot.child("title").getValue(String.class);
                        String content = noteSnapshot.child("content").getValue(String.class);
                        if (title != null && content != null) {
                            Note existingNote = existingNotes.get(title);
                            if (existingNote != null) {
                                // Only update if content is different
                                if (!content.equals(existingNote.getContent())) {
                                    existingNote.setContent(content);
                                }
                            } else {
                                // Add new note
                                Note newNote = new Note(title, content);
                                DataStore.getInstance().getNotesList().add(newNote);
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                logger.severe("Failed to load notes: " + error.getMessage());
            }
        });
    }
}