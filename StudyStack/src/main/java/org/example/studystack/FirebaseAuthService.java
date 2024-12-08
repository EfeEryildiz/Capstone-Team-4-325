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
        try {
            // Initialize Firebase first
            FirebaseConnection.initialize();

            // Create JSON payload
            JSONObject payload = new JSONObject();
            payload.put("email", email);
            payload.put("password", password);
            payload.put("returnSecureToken", true);

            // Make HTTP request
            URL url = new URL(SIGN_IN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send the request
            OutputStream os = conn.getOutputStream();
            os.write(payload.toString().getBytes());
            os.flush();
            os.close();

            // Read response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Parse response
            JSONObject jsonResponse = new JSONObject(response.toString());
            System.out.println("Login successful: " + jsonResponse.toString(2));

            // Set the user ID in FirebaseRealtimeDB
            String localId = jsonResponse.getString("localId");
            FirebaseRealtimeDB.setCurrentUser(localId);

            reader.close();
            conn.disconnect();

            return true;
        } catch (Exception e) {
            logger.severe("Login failed: " + e.getMessage());
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





