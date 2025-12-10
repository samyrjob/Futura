package model.friend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Model representing a friend request (sent/received via Kafka).
 * Pure data object with JSON serialization for Kafka transport.
 * 
 * Part of MVC architecture - this is the Model layer.
 */
public class FriendRequest {
    
    // ═══════════════════════════════════════════════════════════
    // REQUEST TYPES
    // ═══════════════════════════════════════════════════════════
    
    public enum RequestType {
        SEND_REQUEST,       // Initial friend request
        ACCEPT_REQUEST,     // Accept a received request
        REJECT_REQUEST,     // Reject a received request
        CANCEL_REQUEST      // Cancel a sent request
    }
    
    // ═══════════════════════════════════════════════════════════
    // FIELDS
    // ═══════════════════════════════════════════════════════════
    
    private String fromUsername;
    private String toUsername;
    private String fromGender;
    private RequestType type;
    private long timestamp;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Create a new friend request
     */
    public FriendRequest(String fromUsername, String toUsername, String fromGender, RequestType type) {
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.fromGender = fromGender;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Default constructor for Gson deserialization
     */
    public FriendRequest() {}
    
    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════
    
    public String getFromUsername() {
        return fromUsername;
    }
    
    public String getToUsername() {
        return toUsername;
    }
    
    public String getFromGender() {
        return fromGender;
    }
    
    public RequestType getType() {
        return type;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    // ═══════════════════════════════════════════════════════════
    // JSON SERIALIZATION (for Kafka transport)
    // ═══════════════════════════════════════════════════════════
    
    private static final Gson gson = new GsonBuilder().create();
    
    /**
     * Serialize to JSON string
     */
    public String toJson() {
        return gson.toJson(this);
    }
    
    /**
     * Deserialize from JSON string
     */
    public static FriendRequest fromJson(String json) {
        return gson.fromJson(json, FriendRequest.class);
    }
    
    // ═══════════════════════════════════════════════════════════
    // OBJECT METHODS
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return "[" + type + "] " + fromUsername + " → " + toUsername;
    }
}