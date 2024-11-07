// FlashcardService.java
package org.example.studystack.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.remoteconfig.User;
import org.example.studystack.models.FlashCardModel;
import org.example.studystack.models.FlashCardDeck;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FlashcardService {
    private final Firestore db;
    private final String userId;

    public FlashcardService(Firestore db, String userId) {
        this.db = db;
        this.userId = userId;
    }

    public String getUserId() throws FirebaseAuthException {
        // If you're using Firebase Authentication, you can get the current user's ID
        UserRecord userRecord = FirebaseAuth.getInstance().getUser(userId);
// See the UserRecord reference doc for the contents of userRecord.
        System.out.println("Successfully fetched user data: " + userRecord.getUid());


        // For now, we'll return the userId we stored in the constructor
        return this.userId;
    }


    // Create new deck
    public void createDeck(FlashCardDeck deck) {
        DocumentReference docRef = db.collection("decks").document(deck.getId());
        docRef.set(deck);
    }

    // Add flashcard to deck
    public void addFlashcard(String deckId, FlashCardModel flashcard) {
        DocumentReference deckRef = db.collection("decks").document(deckId);
        DocumentReference cardRef = deckRef.collection("flashcards").document(flashcard.getId());
        cardRef.set(flashcard);
    }

    // Get all decks for user
    public List<FlashCardDeck> getUserDecks() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = db.collection("decks")
                .whereEqualTo("userId", userId)
                .get();

        List<FlashCardDeck> decks = new ArrayList<>();
        QuerySnapshot snapshot = future.get();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            decks.add(doc.toObject(FlashCardDeck.class));
        }
        return decks;
    }

    // Get flashcards in deck
    public List<FlashCardModel> getDeckFlashcards(String deckId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = db.collection("decks")
                .document(deckId)
                .collection("flashcards")
                .get();

        List<FlashCardModel> flashcards = new ArrayList<>();
        QuerySnapshot snapshot = future.get();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            flashcards.add(doc.toObject(FlashCardModel.class));
        }
        return flashcards;
    }

    // Update flashcard
    public void updateFlashcard(String deckId, FlashCardModel flashcard) {
        DocumentReference cardRef = db.collection("decks")
                .document(deckId)
                .collection("flashcards")
                .document(flashcard.getId());
        cardRef.set(flashcard);
    }

    // Delete flashcard
    public void deleteFlashcard(String deckId, String flashcardId) {
        DocumentReference cardRef = db.collection("decks")
                .document(deckId)
                .collection("flashcards")
                .document(flashcardId);
        cardRef.delete();
    }
}