package group.flashcardstudytool.services;

import com.google.firebase.database.*;
import group.flashcardstudytool.models.Deck;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DeckService {
    private final DatabaseReference dbRef;

    public DeckService() {
        this.dbRef = FirebaseDatabase.getInstance().getReference("decks");
    }

    public CompletableFuture<Void> addDeck(Deck deck) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        dbRef.child(deck.getId()).setValueAsync(deck)
                .addListener(() -> future.complete(null), Runnable::run);
        return future;
    }

    public CompletableFuture<List<Deck>> getAllDecks() {
        CompletableFuture<List<Deck>> future = new CompletableFuture<>();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Deck> decks = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Deck deck = snapshot.getValue(Deck.class);
                    if (deck != null) {
                        decks.add(deck);
                    }
                }
                future.complete(decks);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });
        return future;
    }
}