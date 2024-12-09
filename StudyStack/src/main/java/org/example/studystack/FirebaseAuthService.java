package org.example.studystack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONObject;

public class FirebaseAuthService {
    private static final Logger logger = Logger.getLogger(FirebaseAuthService.class.getName());
    private static final String apiKey;

    static {
        String key = null;
        try {
            // First try to load from config.properties
            try (InputStream input = FirebaseAuthService.class.getResourceAsStream("/config.properties")) {
                if (input != null) {
                    Properties props = new Properties();
                    props.load(input);
                    key = props.getProperty("firebase.api.key");
                }
            }

            // If not found in properties, try environment variable
            if (key == null || key.trim().isEmpty()) {
                key = System.getenv("FIREBASE_API_KEY");
            }

            // If still not found, try system property
            if (key == null || key.trim().isEmpty()) {
                key = System.getProperty("firebase.api.key");
            }

            // Finally, check if we have a key
            if (key == null || key.trim().isEmpty()) {
                throw new RuntimeException("Firebase API key not found. Please set it in config.properties, environment variable FIREBASE_API_KEY, or system property firebase.api.key");
            }

            apiKey = key;
            logger.info("Firebase API key loaded successfully");

        } catch (IOException e) {
            logger.severe("Failed to load Firebase API key: " + e.getMessage());
            throw new RuntimeException("Failed to load Firebase API key", e);
        }
    }

    /**
     * Logs in a user with the given email and password.
     */
    public static boolean login(String email, String password) {
        if (apiKey == null) {
            logger.severe("Firebase API key is not configured");
            return false;
        }

        try {
            String response = makeAuthRequest("signInWithPassword", email, password);
            if (response != null) {
                JSONObject responseJson = new JSONObject(response);
                String uid = responseJson.getString("localId");
                FirebaseRealtimeDB.setCurrentUser(uid);
                return true;
            }
        } catch (Exception e) {
            logger.severe("Login failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Creates a new user account with the given email and password.
     */
    public static boolean createAccount(String email, String password) {
        if (apiKey == null) {
            logger.severe("Firebase API key is not configured");
            return false;
        }

        try {
            String response = makeAuthRequest("signUp", email, password);
            if (response != null) {
                JSONObject responseJson = new JSONObject(response);
                String uid = responseJson.getString("localId");
                // Initialize user data structure
                initializeUserData(uid);
                return true;
            }
        } catch (Exception e) {
            logger.severe("Account creation failed: " + e.getMessage());
        }
        return false;
    }

    private static String makeAuthRequest(String operation, String email, String password) throws Exception {
        String endpoint = operation.equals("signUp") ? "signUp" : "signInWithPassword";
        String firebaseUrl = "https://identitytoolkit.googleapis.com/v1/accounts:" + endpoint + "?key=" + apiKey;

        HttpURLConnection conn = (HttpURLConnection) new URI(firebaseUrl).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String payload = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                email, password
        );

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes());
            os.flush();
        }

        if (conn.getResponseCode() == 200) {
            try (Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A")) {
                return scanner.hasNext() ? scanner.next() : null;
            }
        } else {
            try (Scanner scanner = new Scanner(conn.getErrorStream()).useDelimiter("\\A")) {
                String errorResponse = scanner.hasNext() ? scanner.next() : null;
                logger.severe("Firebase auth error: " + errorResponse);
                return null;
            }
        }
    }

    private static void initializeUserData(String uid) {
        try {
            FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(uid)
                    .setValue(new JSONObject()
                                    .put("notes", new JSONObject())
                                    .put("decks", new JSONObject())
                                    .toString(),
                            (error, ref) -> {
                                if (error != null) {
                                    logger.severe("Error initializing user data: " + error.getMessage());
                                } else {
                                    logger.info("User data initialized successfully for: " + uid);
                                }
                            });
        } catch (Exception e) {
            logger.severe("Failed to initialize user data: " + e.getMessage());
        }
    }
}