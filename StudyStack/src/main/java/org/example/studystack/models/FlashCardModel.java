package org.example.studystack.models;

import com.google.cloud.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FlashCardModel {
    private String id;
    private String front;
    private String back;
    private String deckId;
    private LocalDateTime createdAt;
    private LocalDateTime lastReviewed;
    private int reviewCount;
    private double successRate;

    public FlashCardModel() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.reviewCount = 0;
        this.successRate = 0.0;
    }

    public FlashCardModel(String front, String back, String deckId) {
        this();
        this.front = front;
        this.back = back;
        this.deckId = deckId;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getFront() { return front; }
    public void setFront(String front) { this.front = front; }
    public String getBack() { return back; }
    public void setBack(String back) { this.back = back; }
    public String getDeckId() { return deckId; }
    public void setDeckId(String deckId) { this.deckId = deckId; }
    public LocalDateTime getLastReviewed() { return lastReviewed; }
    public void setLastReviewed(LocalDateTime lastReviewed) { this.lastReviewed = lastReviewed; }
    public int getReviewCount() { return reviewCount; }
    public void incrementReviewCount() { this.reviewCount++; }
    public double getSuccessRate() { return successRate; }
    public void updateSuccessRate(boolean wasSuccessful) {
        this.successRate = (this.successRate * this.reviewCount + (wasSuccessful ? 1 : 0)) / (this.reviewCount + 1);
        this.reviewCount++;
    }
}