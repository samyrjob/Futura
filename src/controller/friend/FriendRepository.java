package controller.friend;

import model.friend.Friend;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for Friend persistence operations.
 * Handles saving/loading friends to/from files.
 * 
 * Part of MVC architecture - this is the Controller layer (persistence).
 * Single Responsibility: Only handles file I/O, no business logic.
 */
public class FriendRepository {
    
    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════
    
    private static final String FRIENDS_DIR = "data/friends/";
    private static final String FILE_PREFIX = "friends_";
    private static final String FILE_EXTENSION = ".dat";
    
    // ═══════════════════════════════════════════════════════════
    // INSTANCE STATE
    // ═══════════════════════════════════════════════════════════
    
    private final String playerUsername;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public FriendRepository(String playerUsername) {
        this.playerUsername = playerUsername;
        ensureDirectoryExists();
    }
    
    // ═══════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Save friends list to file
     * 
     * @param friends List of friends to save
     * @return true if successful, false otherwise
     */
    public boolean save(List<Friend> friends) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFilePath()))) {
            for (Friend friend : friends) {
                writer.write(friend.serialize());
                writer.newLine();
            }
            System.out.println("[FRIEND REPO] Saved " + friends.size() + " friends to file");
            return true;
        } catch (IOException e) {
            System.err.println("[FRIEND REPO] Failed to save friends: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load friends list from file
     * 
     * @return List of friends, empty list if file doesn't exist or error occurs
     */
    public List<Friend> load() {
        List<Friend> friends = new ArrayList<>();
        File file = new File(getFilePath());
        
        if (!file.exists()) {
            System.out.println("[FRIEND REPO] No friends file found for: " + playerUsername);
            return friends;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        Friend friend = Friend.deserialize(line);
                        friends.add(friend);
                    } catch (Exception e) {
                        System.err.println("[FRIEND REPO] Failed to parse friend entry: " + line);
                    }
                }
            }
            System.out.println("[FRIEND REPO] Loaded " + friends.size() + " friends from file");
        } catch (IOException e) {
            System.err.println("[FRIEND REPO] Failed to load friends: " + e.getMessage());
        }
        
        return friends;
    }
    
    /**
     * Check if a friends file exists for this player
     */
    public boolean exists() {
        return new File(getFilePath()).exists();
    }
    
    /**
     * Delete the friends file
     */
    public boolean delete() {
        File file = new File(getFilePath());
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }
    
    // ═══════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════
    
    private String getFilePath() {
        return FRIENDS_DIR + FILE_PREFIX + playerUsername + FILE_EXTENSION;
    }
    
    private void ensureDirectoryExists() {
        File dir = new File(FRIENDS_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("[FRIEND REPO] Created friends directory");
            }
        }
    }
}