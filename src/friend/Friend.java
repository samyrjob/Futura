package friend;

/**
 * Represents a friend in the contact list
 */
public class Friend {
    
    private String username;
    private String gender;
    private long addedTimestamp;
    private boolean isOnline;
    private String currentRoom;
    
    public Friend(String username, String gender) {
        this.username = username;
        this.gender = gender;
        this.addedTimestamp = System.currentTimeMillis();
        this.isOnline = false;
        this.currentRoom = null;
    }
    
    // For deserialization
    public Friend(String username, String gender, long addedTimestamp) {
        this.username = username;
        this.gender = gender;
        this.addedTimestamp = addedTimestamp;
        this.isOnline = false;
        this.currentRoom = null;
    }
    
    // ═══════════════════════════════════════════════════════════
    // GETTERS & SETTERS
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
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public void setOnline(boolean online) {
        this.isOnline = online;
    }
    
    public String getCurrentRoom() {
        return currentRoom;
    }
    
    public void setCurrentRoom(String room) {
        this.currentRoom = room;
    }
    
    // ═══════════════════════════════════════════════════════════
    // SERIALIZATION (for file storage)
    // ═══════════════════════════════════════════════════════════
    
    public String serialize() {
        return username + "|" + gender + "|" + addedTimestamp;
    }
    
    public static Friend deserialize(String data) {
        String[] parts = data.split("\\|");
        if (parts.length >= 3) {
            return new Friend(parts[0], parts[1], Long.parseLong(parts[2]));
        } else if (parts.length >= 2) {
            return new Friend(parts[0], parts[1]);
        }
        return new Friend(parts[0], "UNKNOWN");
    }
    
    @Override
    public String toString() {
        return username + (isOnline ? " (Online)" : " (Offline)");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Friend) {
            return username.equalsIgnoreCase(((Friend) obj).username);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return username.toLowerCase().hashCode();
    }
}
