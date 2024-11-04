package org.example.studystack.utils;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.example.studystack.utils.QuizResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class FirebaseAnalytics {

    private Firestore db;

    public FirebaseAnalytics() {
        db = FirestoreOptions.getDefaultInstance().getService();
    }

    // Fetch quiz results with an async callback using Consumer
    public void fetchQuizResults(Consumer<List<QuizResult>> callback) {
        ApiFuture<QuerySnapshot> future = db.collection("ProgressReports").get();

        // Process results once retrieval is complete
        try {
            QuerySnapshot snapshot = future.get();  // Blocking call to retrieve data
            List<QuizResult> results = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                QuizResult result = document.toObject(QuizResult.class);
                results.add(result);
            }
            callback.accept(results);  // Pass data to callback
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error fetching results: " + e.getMessage());
        }
    }
}
