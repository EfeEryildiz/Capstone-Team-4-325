package org.example.studystack;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class FirebaseAuthService {

    /**
     * Logs in a user with the given email and password.
     */
    public static boolean login(String email, String password) {
        String apiKey = "";
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
        String apiKey = ""; // Replace with your Firebase API key
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

    /* Logs user out */
    public static void logout() {
        System.out.println("User logged out.");
    }
}





