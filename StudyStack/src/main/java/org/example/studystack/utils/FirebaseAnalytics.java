package org.example.studystack.utils;

import com.google.cloud.firestore.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.google.api.core.ApiFuture;
import com.google.firebase.cloud.FirestoreClient;

import java.util.concurrent.ExecutionException;

public class FirebaseAnalytics {
    private final Firestore db;

    public FirebaseAnalytics() {
        this.db = FirestoreClient.getFirestore();
        if (this.db == null) {
            throw new RuntimeException("Failed to initialize Firestore");
        }
    }

    public void fetchQuizResults(Consumer<List<QuizResult>> callback) {
        try {
            CollectionReference progressReports = db.collection("ProgressReports");
            ApiFuture<QuerySnapshot> future = progressReports.get();

            // Handle the future asynchronously
            future.addListener(() -> {
                try {
                    QuerySnapshot snapshots = future.get();
                    List<QuizResult> results = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        QuizResult result = doc.toObject(QuizResult.class);
                        if (result != null) {
                            results.add(result);
                        }
                    }

                    callback.accept(results);
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error fetching quiz results: " + e.getMessage());
                    callback.accept(new ArrayList<>());
                }
            }, Runnable::run);

        } catch (Exception e) {
            System.err.println("Error in fetchQuizResults: " + e.getMessage());
            callback.accept(new ArrayList<>());
        }
    }

    // Alternative implementation using Future directly
    public void fetchQuizResultsSync(Consumer<List<QuizResult>> callback) {
        try {
            CollectionReference progressReports = db.collection("ProgressReports");
            ApiFuture<QuerySnapshot> future = progressReports.get();

            // Get the results synchronously
            QuerySnapshot snapshots = future.get();
            List<QuizResult> results = new ArrayList<>();

            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                QuizResult result = doc.toObject(QuizResult.class);
                if (result != null) {
                    results.add(result);
                }
            }

            callback.accept(results);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error fetching quiz results: " + e.getMessage());
            callback.accept(new ArrayList<>());
        }
    }

    // Helper method to check if there are any documents in the collection
    public boolean hasQuizResults() {
        try {
            CollectionReference progressReports = db.collection("ProgressReports");
            ApiFuture<QuerySnapshot> future = progressReports.get();
            QuerySnapshot snapshots = future.get();
            return !snapshots.isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking quiz results: " + e.getMessage());
            return false;
        }
    }
}