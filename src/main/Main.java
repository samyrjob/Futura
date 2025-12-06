package main;

import javax.swing.*;

/**
 * Main entry point for Futura virtual world game
 * 
 * Launch via custom URL protocol:
 * futura://open?user=USERNAME&gender=GENDER
 * 
 * Example: futura://open?user=Joe&gender=male
 */
public class Main {
    
    private static final String DEFAULT_USERNAME = "Dominique";
    private static final String DEFAULT_GENDER = "male";
    private static final String PROTOCOL = "futura://";
    
    public static void main(String[] args) {
        // Parse launch arguments from URL scheme
        LaunchConfig config = parseArguments(args);
        
        // Debug output
        System.out.println("Username: " + config.username);
        System.out.println("Gender: " + config.gender);
        
        // Launch game on Swing thread
        SwingUtilities.invokeLater(() -> {
            new GameWindow(config.username, config.gender);
        });
    }
    
    /**
     * Parse command line arguments for futura:// protocol
     * 
     * Format: futura://open?user=USERNAME&gender=GENDER
     * 
     * @param args Command line arguments
     * @return LaunchConfig with username and gender
     */
    private static LaunchConfig parseArguments(String[] args) {
        String username = DEFAULT_USERNAME;
        String gender = DEFAULT_GENDER;
        
        if (args.length > 0 && args[0].startsWith(PROTOCOL)) {
            // Extract query string after '?'
            int queryStart = args[0].indexOf("?");
            if (queryStart != -1) {
                String query = args[0].substring(queryStart + 1);
                String[] pairs = query.split("&");
                
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2) {
                        String key = kv[0].toLowerCase();
                        String value = kv[1];
                        
                        if (key.equals("user")) {
                            username = value;
                        } else if (key.equals("gender")) {
                            gender = value;
                        }
                    }
                }
            }
        }
        
        return new LaunchConfig(username, gender);
    }
    
    /**
     * Simple data class for launch configuration
     */
    private static class LaunchConfig {
        final String username;
        final String gender;
        
        LaunchConfig(String username, String gender) {
            this.username = username;
            this.gender = gender;
        }
    }
}