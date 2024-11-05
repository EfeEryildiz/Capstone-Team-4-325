package org.example.studystack;

import java.util.ArrayList;
import java.util.List;

public class FlashcardParser {

    public static List<Flashcard> parseFlashcards(String assistantReply) {
        List<Flashcard> flashcards = new ArrayList<>();

        if (assistantReply == null || assistantReply.isEmpty()) {
            System.err.println("Assistant reply is empty or null.");
            return flashcards;
        }


        String[] lines = assistantReply.split("\\r?\\n"); //Split the reply into lines
        String question = null;
        String answer = null;
        for (String line : lines) {
            if (line.startsWith("Q:")) {
                question = line.substring(2).trim();
            } else if (line.startsWith("A:")) {
                answer = line.substring(2).trim();
                if (question != null && answer != null) {
                    flashcards.add(new Flashcard(question, answer));
                    question = null;
                    answer = null;
                }
            }
        }

        if (flashcards.isEmpty()) {
            System.err.println("No flashcards were parsed from the assistant's reply.");
            System.err.println("Assistant's Reply:\n" + assistantReply); //Print the reply for debugging
        }

        return flashcards;
    }
}
