package org.example.studystack;

import com.google.firebase.database.*;
import javafx.application.Platform;

import java.util.HashMap;
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
            logger.warning("Cannot save note: No user is currently logged in");
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

    private static void loadUserData() {
        if (currentUserUid == null) return;

        DatabaseReference userNotesRef = database.child("users").child(currentUserUid).child("notes");

        userNotesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Platform.runLater(() -> {
                    for (DataSnapshot noteSnapshot : snapshot.getChildren()) {
                        String title = noteSnapshot.child("title").getValue(String.class);
                        String content = noteSnapshot.child("content").getValue(String.class);
                        if (title != null && content != null) {
                            // Find existing note or create new one
                            Note existingNote = DataStore.getInstance().getNotesList().stream()
                                .filter(n -> n.getTitle().equals(title))
                                .findFirst()
                                .orElse(null);
                                
                            if (existingNote != null) {
                                if (!existingNote.getContent().equals(content)) {
                                    existingNote.setContent(content);
                                }
                            } else {
                                // Add new note
                                DataStore.getInstance().getNotesList().add(new Note(title, content));
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