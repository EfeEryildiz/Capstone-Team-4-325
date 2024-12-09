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
        logger.info("Initializing auth for user: " + uid);
        
        if (isAuthInitialized) {
            logger.info("Auth already initialized for user: " + currentUserUid);
            return;
        }
        
        try {
            currentUserUid = uid;
            isAuthInitialized = true;
            logger.info("Firebase Auth initialized with user: " + uid);
            
            // Load user data after successful auth
            Platform.runLater(() -> {
                try {
                    loadUserData();
                    logger.info("User data loaded successfully");
                } catch (Exception e) {
                    logger.severe("Error loading user data: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.severe("Failed to initialize Firebase Auth: " + e.getMessage());
            isAuthInitialized = false;
            currentUserUid = null;  // Reset on failure
            e.printStackTrace();
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
        logger.info("Setting current user: " + uid + " (called from: " + 
            Thread.currentThread().getStackTrace()[2].getClassName() + ")");
        
        if (uid == null) {
            logger.warning("Attempted to set null user ID");
            return;
        }

        try {
            // Always initialize Firebase first
            initialize();
            
            if (!isInitialized) {
                logger.severe("Failed to initialize Firebase before setting user");
                return;
            }
            
            // Then initialize auth
            initializeAuth(uid);
            
            // Verify initialization was successful
            if (!isAuthInitialized || currentUserUid == null) {
                logger.severe("Auth initialization failed silently");
                return;
            }
            
            logger.info("Current user set successfully: " + uid + 
                       " (Auth Initialized: " + isAuthInitialized + 
                       ", DB Initialized: " + isInitialized + ")");
        } catch (Exception e) {
            logger.severe("Error setting current user: " + e.getMessage());
            e.printStackTrace();
        }
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
        
        // Try to reinitialize if not properly connected
        if (!isInitialized || !isAuthInitialized) {
            logger.info("Attempting to reinitialize Firebase connection...");
            initialize();
            if (currentUserUid != null) {
                initializeAuth(currentUserUid);
            }
        }

        if (!checkAuthentication() || !ensureInitialized()) {
            logger.severe("Cannot save deck: Database not initialized or user not logged in" +
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
            
            logger.info("Deck data to save: " + deckData.toString());

            // Add connection status check
            DatabaseReference connectedRef = database.child(".info/connected");
            connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    logger.info("Firebase connection status: " + (connected ? "Connected" : "Disconnected"));
                    
                    if (connected) {
                        // Save the deck with completion callback
                        userDecksRef.child(deck.getName()).setValue(deckData, (error, ref) -> {
                            if (error == null) {
                                logger.info("Deck saved successfully to: " + ref.getPath().toString());
                            } else {
                                logger.severe("Failed to save deck: " + error.getMessage() + 
                                            "\nDetails: " + error.getDetails() +
                                            "\nCode: " + error.getCode());
                                error.toException().printStackTrace();
                            }
                        });
                    } else {
                        logger.severe("Cannot save deck: Firebase is disconnected");
                        // Try to reconnect
                        initialize();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    logger.severe("Error checking connection: " + error.getMessage());
                }
            });

        } catch (Exception e) {
            logger.severe("Error saving deck: " + e.getMessage());
            e.printStackTrace();
            
            // Try to recover the connection
            try {
                initialize();
                if (currentUserUid != null) {
                    initializeAuth(currentUserUid);
                }
            } catch (Exception reinitError) {
                logger.severe("Failed to recover Firebase connection: " + reinitError.getMessage());
            }
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
        logger.info("Loading user data for: " + currentUserUid);
        
        // Load notes
        DatabaseReference userNotesRef = database.child("users").child(currentUserUid).child("notes");
        userNotesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Platform.runLater(() -> {
                    // Clear existing notes before loading
                    DataStore.getInstance().getNotesList().clear();
                    
                    for (DataSnapshot noteSnapshot : snapshot.getChildren()) {
                        String title = noteSnapshot.child("title").getValue(String.class);
                        String content = noteSnapshot.child("content").getValue(String.class);
                        
                        if (title != null) {
                            Note note = new Note(title, content != null ? content : "");
                            DataStore.getInstance().getNotesList().add(note);
                            logger.info("Loaded note: " + title);
                        }
                    }
                    logger.info("Loaded " + DataStore.getInstance().getNotesList().size() + " notes");
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
                    Map<String, Deck> existingDecks = new HashMap<>();
                    DataStore.getInstance().getDecksList().forEach(deck -> 
                        existingDecks.put(deck.getName(), deck));

                    for (DataSnapshot deckSnapshot : snapshot.getChildren()) {
                        String name = deckSnapshot.child("name").getValue(String.class);
                        if (name != null) {
                            Deck existingDeck = existingDecks.get(name);
                            if (existingDeck == null) {
                                existingDeck = new Deck(name);
                                DataStore.getInstance().getDecksList().add(existingDeck);
                            }
                            
                            existingDeck.getFlashcards().clear();
                            
                            DataSnapshot flashcardsSnapshot = deckSnapshot.child("flashcards");
                            for (DataSnapshot cardSnapshot : flashcardsSnapshot.getChildren()) {
                                String question = cardSnapshot.child("question").getValue(String.class);
                                String answer = cardSnapshot.child("answer").getValue(String.class);
                                if (question != null && answer != null) {
                                    existingDeck.getFlashcards().add(new Flashcard(question, answer));
                                }
                            }
                            logger.info("Loaded deck: " + name + " with " + 
                                      existingDeck.getFlashcards().size() + " flashcards");
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

    public static void clearUser() {
        logger.info("Clearing Firebase user state");
        currentUserUid = null;
        isAuthInitialized = false;
        
        // Remove any existing listeners
        if (database != null) {
            DatabaseReference userRef = database.child("users");
            userRef.removeEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {}
                @Override
                public void onCancelled(DatabaseError error) {}
            });
        }
        
        logger.info("Firebase user state cleared");
    }
}