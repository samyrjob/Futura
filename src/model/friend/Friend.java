package model.friend;

/**
 * Model representing a friend in the contact list.
 * Pure data object (POJO) - no business logic, no UI dependencies.
 * 
 * Part of MVC architecture - this is the Model layer.
 */
public class Friend {
    
    // ═══════════════════════════════════════════════════════════
    // FIELDS
    // ═══════════════════════════════════════════════════════════
    
    private final String username;
    private final String gender;
    private final long addedTimestamp;
    
    // Mutable state (updated at runtime)
    private boolean online;
    private String currentRoom;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════
    
    public Friend(String username, String gender) {
        this.username = username;
        this.gender = gender;
        this.addedTimestamp = System.currentTimeMillis();
        this.online = false;
        this.currentRoom = null;
    }
    
    /**
     * Constructor for deserialization (loading from file)
     */
    public Friend(String username, String gender, long addedTimestamp) {
        this.username = username;
        this.gender = gender;
        this.addedTimestamp = addedTimestamp;
        this.online = false;
        this.currentRoom = null;
    }
    
    // ═══════════════════════════════════════════════════════════
    // GETTERS (Immutable fields)
    // ═══════════════════════════════════════════════════════════
    
    public String getUsername() {
        return username;
    }
    
    public String getGender() {
        return gender;
    }
    
    public long getAddedTimestamp() {
        return addedTimestamp;
    }
    
    // ═══════════════════════════════════════════════════════════
    // GETTERS & SETTERS (Mutable state)
    // ═══════════════════════════════════════════════════════════
    
    public boolean isOnline() {
        return online;
    }
    
    public void setOnline(boolean online) {
        this.online = online;
    }
    
    public String getCurrentRoom() {
        return currentRoom;
    }
    
    public void setCurrentRoom(String room) {
        this.currentRoom = room;
    }
    
    // ═══════════════════════════════════════════════════════════
    // SERIALIZATION (for persistence)
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Serialize to string for file storage
     * Format: username|gender|timestamp
     */
    public String serialize() {
        return username + "|" + gender + "|" + addedTimestamp;
    }
    
    /**
     * Deserialize from string
     */
    public static Friend deserialize(String data) {
        String[] parts = data.split("\\|");
        if (parts.length >= 3) {
            return new Friend(parts[0], parts[1], Long.parseLong(parts[2]));
        } else if (parts.length >= 2) {
            return new Friend(parts[0], parts[1]);
        }
        return new Friend(parts[0], "UNKNOWN");
    }
    
    // ═══════════════════════════════════════════════════════════
    // OBJECT METHODS
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public String toString() {
        return username + (online ? " (Online)" : " (Offline)");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Friend friend = (Friend) obj;
        return username.equalsIgnoreCase(friend.username);
    }
    
    @Override
    public int hashCode() {
        return username.toLowerCase().hashCode();
    }
}