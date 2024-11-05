package org.example.studystack;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

public class OpenAIAPIController {

    //Load API Key from the config.properties file
    private static final Properties properties = new Properties();
    static {
        try (InputStream input = OpenAIAPIController.class.getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(input); //Make sure config.properties is located in resources

        } catch (IOException e) {

            e.printStackTrace();


        }
    }
    private static final String OPENAI_API_KEY = properties.getProperty("OPENAI_API_KEY");
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    //Method to generate flashcards from notes
    public static List<Flashcard> generateFlashcards(String noteContent) throws IOException, InterruptedException {
        //Prepare the prompt with this formatting
        String prompt = "Convert the following notes into flashcards. Each flashcard should have a question and an answer.  Match the following format exactly, including the brackets, \n\n" +
                "Format each flashcard as follows:\n" +
                "Q: [Your question here]\n" +
                "A: [Your answer here]\n\n" +
                "Notes:\n" + noteContent;

        //Prepare the request body using json
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("max_tokens", 1500); //Max tokens allowed for a response
        requestBody.put("temperature", 0.7); //This is the creativity of the model

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.put(message);

        requestBody.put("messages", messages);

        //Build HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        //Send request and hope it works
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        System.out.println("OpenAI API Response Status Code: " + response.statusCode()); //Debugging stuff
        System.out.println("OpenAI API Response Body: " + response.body()); //Debugging stuff

        //Parse response
        if (response.statusCode() == 200) {
            JSONObject responseJson = new JSONObject(response.body());
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices.length() == 0) {
                System.err.println("No choices found in the API response.");
                return null;
            }
            JSONObject firstChoice = choices.getJSONObject(0);
            String assistantReply = firstChoice.getJSONObject("message").getString("content").trim();

            System.out.println("Assistant Reply:\n" + assistantReply); //Log reply

            //Parse to extract flash cards
            List<Flashcard> flashcards = FlashcardParser.parseFlashcards(assistantReply);
            if (flashcards.isEmpty()) {
                System.err.println("No flashcards were parsed from the assistant's reply.");
            }
            return flashcards;
        } else {
            //Handle errors from GPT
            System.err.println("OpenAI API Error: " + response.statusCode() + " - " + response.body());
            return null;
        }
    }

    //Method to generate multiple choice options
    public static List<String> generateMultipleChoiceOptions(String question, String correctAnswer) throws IOException, InterruptedException {
        String prompt = "This is a multiple choice question, so create three incorrect but plausible answers for the following question, and include the correct answer as one of the options.  Make sure there are FOUR options/choices in total no matter what.  Thanks\n\n" +
                "Question: " + question + "\n" +
                "Correct Answer: " + correctAnswer + "\n\n" +
                "Options (in JSON array format):";

        //Prepare the request body using json
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("max_tokens", 150); //Max tokens
        requestBody.put("temperature", 0.7); //Creativity

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.put(message);

        requestBody.put("messages", messages);

        //Build the HTTP request, same as other request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        //Send the request
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //Log the response status and body for debugging (MCQ = Multiple Choice Question)
        System.out.println("OpenAI API (MCQ) Response Status Code: " + response.statusCode());
        System.out.println("OpenAI API (MCQ) Response Body: " + response.body());

        //Parse the response
        if (response.statusCode() == 200) {
            JSONObject responseJson = new JSONObject(response.body());
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices.length() == 0) {
                System.err.println("No choices found in the MCQ API response.");
                return null;
            }
            JSONObject firstChoice = choices.getJSONObject(0);
            String assistantReply = firstChoice.getJSONObject("message").getString("content").trim();

            System.out.println("Assistant Reply (MCQ):\n" + assistantReply); //Log reply

            //Parse and extract the options
            JSONArray optionsArray = new JSONArray(assistantReply);
            List<String> options = new ArrayList<>();
            for (int i = 0; i < optionsArray.length(); i++) {
                options.add(optionsArray.getString(i));
            }

            if (options.size() < 4) { //Expecting 4 options total, one being correct
                System.err.println("Insufficient options generated for the question: " + question);
                return null;
            }

            return options;
        } else {
            //Handle errors
            System.err.println("OpenAI API (MCQ) Error: " + response.statusCode() + " - " + response.body());
            return null;
        }
    }
}
