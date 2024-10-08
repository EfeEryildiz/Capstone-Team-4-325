package group.flashcardstudytool.models;

import java.util.Date;
import java.util.UUID;

public class QuizResult {
    private String id;
    private String deckId;
    private int totalQuestions;
    private int correctAnswers;
    private Date date;

    public QuizResult() {
        this.id = UUID.randomUUID().toString();
        this.date = new Date();
    }

    public QuizResult(String deckId, int totalQuestions, int correctAnswers) {
        this();
        this.deckId = deckId;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDeckId() { return deckId; }
    public void setDeckId(String deckId) { this.deckId = deckId; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
}
