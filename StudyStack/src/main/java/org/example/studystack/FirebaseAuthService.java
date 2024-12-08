package org.example.studystack;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;

public class FirebaseAuthService {
    private static final String apiKey;
    
    static {
        try {
            Properties props = new Properties();
            props.load(FirebaseAuthService.class.getResourceAsStream("/config.properties"));
            apiKey = props.getProperty("firebase.api.key");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Firebase API key", e);
        }
    }

    /**
     * Logs in a user with the given email and password.
     */
    public static boolean login(String email, String password) {
        String firebaseUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;

        try {
            // Create a connection to the Firebase REST API
            URL url = new URL(firebaseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
                // Successful login
                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                System.out.println("Login successful: " + response);
                return true;
            } else {
                // Login failed
                Scanner scanner = new Scanner(conn.getErrorStream());
                String errorResponse = scanner.useDelimiter("\\A").next();
                scanner.close();

                System.out.println("Login failed: " + errorResponse);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates a new user account with the given email and password.
     */
    public static boolean createAccount(String email, String password) {
        String firebaseUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + apiKey;

        try {
            // Create a connection to the Firebase REST API
            URL url = new URL(firebaseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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





