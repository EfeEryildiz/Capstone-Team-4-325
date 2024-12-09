package org.example.studystack;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;
import org.json.JSONObject;
import java.util.logging.Logger;
import java.nio.charset.StandardCharsets;

public class FirebaseAuthService {
    private static final Logger logger = Logger.getLogger(FirebaseAuthService.class.getName());
    private static String API_KEY;
    private static String SIGN_IN_URL;

    static {
        try {
            // Load API key from existing config.properties
            Properties props = new Properties();
            InputStream input = FirebaseAuthService.class.getClassLoader()
                    .getResourceAsStream("config.properties");
            
            if (input == null) {
                throw new RuntimeException("Could not find config.properties in resources");
            }
            
            props.load(input);
            API_KEY = props.getProperty("firebase.api.key");
            
            if (API_KEY == null || API_KEY.trim().isEmpty()) {
                throw new RuntimeException("Firebase API key not found in config.properties");
            }
            
            // Remove the hardcoded "your-api-key" and use the one from config.properties
            SIGN_IN_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;
            logger.info("Firebase Auth Service initialized successfully with API key from config.properties");
            
        } catch (IOException e) {
            logger.severe("Failed to load Firebase configuration: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Firebase Auth Service", e);
        }
    }

    /**
     * Logs in a user with the given email and password.
     */
    public static boolean login(String email, String password) {
        String firebaseUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;

        try {
            // Create connection
            URI uri = new URI(firebaseUrl);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Create payload
            String payload = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                    email, password
            );

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Check response code first
            int responseCode = conn.getResponseCode();
            logger.info("Login response code: " + responseCode);

            // Read the response (error or success)
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            // Log the full response for debugging
            logger.info("Login response: " + response.toString());

            if (responseCode >= 400) {
                JSONObject errorResponse = new JSONObject(response.toString());
                String errorMessage = errorResponse.getJSONObject("error").getString("message");
                logger.severe("Login failed with error: " + errorMessage);
                throw new RuntimeException("Login failed: " + errorMessage);
            }

            // Parse successful response
            JSONObject jsonResponse = new JSONObject(response.toString());
            String localId = jsonResponse.getString("localId");
            
            logger.info("Login successful for user: " + localId);
            FirebaseRealtimeDB.setCurrentUser(localId);
            
            return true;

        } catch (Exception e) {
            logger.severe("Login failed: " + e.getMessage());
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Login failed", e);
        }
    }

    /**
     * Creates a new user account with the given email and password.
     */
    public static boolean createAccount(String email, String password) {
        String firebaseUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY;

        try {
            // Create a connection to the Firebase REST API
            URI uri = new URI(firebaseUrl);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Create the JSON payload
            String payload = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                    email, password
            );

            // Send the request
            OutputStream os = conn.getOutputStream();
            os.write(payload.getBytes());
            os.flush();
            os.close();

            // Handle the response
            if (conn.getResponseCode() == 200) {
                // Successful account creation
                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                System.out.println("Account created successfully: " + response);
                return true;
            } else {
                // Account creation failed
                Scanner scanner = new Scanner(conn.getErrorStream());
                String errorResponse = scanner.useDelimiter("\\A").next();
                scanner.close();

                System.out.println("Account creation failed: " + errorResponse);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}





