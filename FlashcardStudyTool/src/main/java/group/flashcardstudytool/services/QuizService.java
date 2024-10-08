package group.flashcardstudytool.services;

import com.google.firebase.database.*;
import group.flashcardstudytool.models.QuizResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class QuizService {
    private final DatabaseReference dbRef;

    public QuizService() {
        this.dbRef = FirebaseDatabase.getInstance().getReference("quizResults");
    }

    public CompletableFuture<Void> addQuizResult(QuizResult quizResult) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        dbRef.child(quizResult.getId()).setValueAsync(quizResult)
                .addListener(() -> future.complete(null), Runnable::run);
        return future;
    }

    public CompletableFuture<List<QuizResult>> getQuizResultsByDeck(String deckId) {
        CompletableFuture<List<QuizResult>> future = new CompletableFuture<>();
        dbRef.orderByChild("deckId").equalTo(deckId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<QuizResult> results = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    QuizResult result = snapshot.getValue(QuizResult.class);
                    if (result != null) {
                        results.add(result);
                    }
                }
                future.complete(results);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });
        return future;
    }
}