package group.flashcardstudytool.services;

import com.google.firebase.database.*;
import group.flashcardstudytool.models.Flashcard;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FlashcardService {
    private final DatabaseReference dbRef;

    public FlashcardService() {
        this.dbRef = FirebaseDatabase.getInstance().getReference("flashcards");
    }

    public CompletableFuture<Void> addFlashcard(Flashcard flashcard) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        dbRef.child(flashcard.getId()).setValueAsync(flashcard)
                .addListener(() -> future.complete(null), Runnable::run);
        return future;
    }

    public CompletableFuture<Void> updateFlashcard(Flashcard flashcard) {
        return addFlashcard(flashcard);
    }

    public CompletableFuture<Void> deleteFlashcard(String flashcardId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        dbRef.child(flashcardId).removeValueAsync()
                .addListener(() -> future.complete(null), Runnable::run);
        return future;
    }

    public CompletableFuture<List<Flashcard>> getFlashcardsByDeck(String deckId) {
        CompletableFuture<List<Flashcard>> future = new CompletableFuture<>();
        dbRef.orderByChild("deckId").equalTo(deckId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Flashcard> flashcards = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Flashcard flashcard = snapshot.getValue(Flashcard.class);
                    if (flashcard != null) {
                        flashcards.add(flashcard);
                    }
                }
                future.complete(flashcards);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });
        return future;
    }
}