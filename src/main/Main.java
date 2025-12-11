// package main;

// import javax.swing.*;

// /**
//  * Main entry point for Futura virtual world game
//  * 
//  * Launch via custom URL protocol:
//  * futura://open?user=USERNAME&gender=GENDER
//  * 
//  * Example: futura://open?user=Joe&gender=male
//  */
// public class Main {
    
//     private static final String DEFAULT_USERNAME = "Dominique";
//     private static final String DEFAULT_GENDER = "male";
//     private static final String PROTOCOL = "futura://";
    
//     public static void main(String[] args) {
//         // Parse launch arguments from URL scheme
//         LaunchConfig config = parseArguments(args);
        
//         // Debug output
//         System.out.println("Username: " + config.username);
//         System.out.println("Gender: " + config.gender);
        
//         // Launch game on Swing thread
//         SwingUtilities.invokeLater(() -> {
//             new GameWindow(config.username, config.gender);
//         });
//     }
    
//     /**
//      * Parse command line arguments for futura:// protocol
//      * 
//      * Format: futura://open?user=USERNAME&gender=GENDER
//      * 
//      * @param args Command line arguments
//      * @return LaunchConfig with username and gender
//      */
//     private static LaunchConfig parseArguments(String[] args) {
//         String username = DEFAULT_USERNAME;
//         String gender = DEFAULT_GENDER;
        
//         if (args.length > 0 && args[0].startsWith(PROTOCOL)) {
//             // Extract query string after '?'
//             int queryStart = args[0].indexOf("?");
//             if (queryStart != -1) {
//                 String query = args[0].substring(queryStart + 1);
//                 String[] pairs = query.split("&");
                
//                 for (String pair : pairs) {
//                     String[] kv = pair.split("=");
//                     if (kv.length == 2) {
//                         String key = kv[0].toLowerCase();
//                         String value = kv[1];
                        
//                         if (key.equals("user")) {
//                             username = value;
//                         } else if (key.equals("gender")) {
//                             gender = value;
//                         }
//                     }
//                 }
//             }
//         }
        
//         return new LaunchConfig(username, gender);
//     }
    
//     /**
//      * Simple data class for launch configuration
//      */
//     // private static class LaunchConfig {
//     //     final String username;
//     //     final String gender;
        
//     //     LaunchConfig(String username, String gender) {
//     //         this.username = username;
//     //         this.gender = gender;
//     //     }
//     // }
//     private record LaunchConfig(String username, String gender) {}

// }

package main;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Main entry point for Futura virtual world game
 * 
 * Launch via custom URL protocol:
 * futura://open?token=SSO_TOKEN
 * 
 * Example: futura://open?token=abc123xyz789
 */
public class Main {
    
    private static final String PROTOCOL = "futura://";
    private static final String API_BASE_URL = "http://localhost:9090";  // Your Spring Boot server
    
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("  Futura Virtual World Launcher  ");
        System.out.println("=================================");
        System.out.println("Args received: " + java.util.Arrays.toString(args));
        
        // Check if we have arguments
        if (args.length == 0 || !args[0].startsWith(PROTOCOL)) {
            showError("No launch parameters provided!\n\nPlease launch the game from the Futura website.");
            return;
        }
        
        // Parse the SSO token from URL
        String ssoToken = parseToken(args[0]);
        System.out.println("SSO Token: " + (ssoToken != null ? ssoToken.substring(0, Math.min(10, ssoToken.length())) + "..." : "null"));
        
        if (ssoToken == null || ssoToken.isEmpty()) {
            showError("Invalid launch token!\n\nPlease try launching the game again from the website.");
            return;
        }
        
        // Show loading dialog
        JDialog loadingDialog = showLoadingDialog("Validating your session...");
        
        // Validate token with backend (in background thread)
        new Thread(() -> {
            try {
                System.out.println("Validating SSO token with backend...");
                UserData userData = validateSsoToken(ssoToken);
                
                // Close loading dialog
                SwingUtilities.invokeLater(() -> loadingDialog.dispose());
                
                if (userData == null) {
                    showError("Authentication failed!\n\nYour session may have expired.\nPlease try launching the game again from the website.");
                    return;
                }
                
                // ✅ Token is valid! Launch the game
                System.out.println("✅ Authentication successful!");
                System.out.println("Welcome, " + userData.username() + "!");
                
                // Launch game on Swing thread
                SwingUtilities.invokeLater(() -> {
                    new GameWindow(userData.username(), userData.gender());
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> loadingDialog.dispose());
                e.printStackTrace();
                showError("Failed to connect to server!\n\nPlease check your internet connection and try again.");
            }
        }).start();
    }
    
    /**
     * Parse SSO token from URL
     * Format: futura://open?token=abc123xyz789
     */
    private static String parseToken(String url) {
        int queryStart = url.indexOf("?");
        if (queryStart == -1) {
            return null;
        }
        
        String query = url.substring(queryStart + 1);
        String[] pairs = query.split("&");
        
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equalsIgnoreCase("token")) {
                return kv[1];
            }
        }
        
        return null;
    }
    
    /**
     * Validate SSO token with Spring Boot backend
     * Returns UserData if valid, null if invalid
     */
    private static UserData validateSsoToken(String token) {
        HttpURLConnection connection = null;
        
        try {
            URL url = new URL(API_BASE_URL + "/api/sso/validate");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            // Send token in request body
            String jsonBody = "{\"token\": \"" + token + "\"}";
            
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
            
            int responseCode = connection.getResponseCode();
            System.out.println("SSO Validation Response Code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection.getInputStream());
                System.out.println("SSO Validation Response: " + response);
                return parseUserData(response);
            } else {
                String errorResponse = readResponse(connection.getErrorStream());
                System.err.println("SSO Validation Failed: " + errorResponse);
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("SSO Validation Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * Read response from input stream
     */
    private static String readResponse(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }
    
    /**
     * Parse JSON response into UserData
     */
    private static UserData parseUserData(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            
            if (obj.has("valid") && !obj.get("valid").getAsBoolean()) {
                System.err.println("Token invalid: " + obj.get("error").getAsString());
                return null;
            }
            
            Integer userId = obj.get("userId").getAsInt();
            String username = obj.get("username").getAsString();
            String email = obj.has("email") ? obj.get("email").getAsString() : "";
            
            // Default gender for now - you can add this to your backend response later
            String gender = "male";
            
            return new UserData(userId, username, email, gender);
            
        } catch (Exception e) {
            System.err.println("Failed to parse user data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Show loading dialog
     */
    private static JDialog showLoadingDialog(String message) {
        JDialog dialog = new JDialog((Frame) null, "Futura", false);  // non-modal
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JLabel(message, SwingConstants.CENTER), BorderLayout.CENTER);
        dialog.setVisible(true);
        return dialog;
    }
    
    /**
     * Show error dialog and exit
     */
    private static void showError(String message) {
        System.err.println("ERROR: " + message);
        JOptionPane.showMessageDialog(null, message, "Futura - Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
    
    /**
     * User data received from backend after SSO validation
     */
    private record UserData(Integer userId, String username, String email, String gender) {}
}