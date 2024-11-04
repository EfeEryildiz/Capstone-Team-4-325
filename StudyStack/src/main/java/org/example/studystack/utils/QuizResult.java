package org.example.studystack.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class QuizResult {
    private Date quizDate;
    private double accuracy;
    private long timeSpent;

    public QuizResult() {
        // Default constructor required for Firebase deserialization
    }

    public QuizResult(Date quizDate, double accuracy, long timeSpent) {
        this.quizDate = quizDate;
        this.accuracy = accuracy;
        this.timeSpent = timeSpent;
    }

    // Getters and setters
    public Date getQuizDate() { return quizDate; }
    public void setQuizDate(Date quizDate) { this.quizDate = quizDate; }

    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

    public long getTimeSpent() { return timeSpent; }
    public void setTimeSpent(long timeSpent) { this.timeSpent = timeSpent; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("quizDate", quizDate);
        map.put("accuracy", accuracy);
        map.put("timeSpent", timeSpent);
        return map;
    }
}