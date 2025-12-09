package friend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Represents a friend request (sent via Kafka)
 */
public class FriendRequest {
    
    public enum RequestType {
        SEND_REQUEST,
        ACCEPT_REQUEST,
        REJECT_REQUEST,
        CANCEL_REQUEST
    }
    
    private String fromUsername;
    private String toUsername;
    private String fromGender;
    private RequestType type;
    private long timestamp;
    
    public FriendRequest(String fromUsername, String toUsername, String fromGender, RequestType type) {
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.fromGender = fromGender;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Default constructor for Gson
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
    // JSON SERIALIZATION (for Kafka)
    // ═══════════════════════════════════════════════════════════
    
    private static final Gson gson = new GsonBuilder().create();
    
    public String toJson() {
        return gson.toJson(this);
    }
    
    public static FriendRequest fromJson(String json) {
        return gson.fromJson(json, FriendRequest.class);
    }
    
    @Override
    public String toString() {
        return "[" + type + "] " + fromUsername + " → " + toUsername;
    }
}

