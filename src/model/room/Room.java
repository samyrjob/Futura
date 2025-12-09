package model.room;

import object.Furniture;
import java.util.*;

/**
 * Room - Data model representing a game room
 * 
 * Part of MVC Architecture:
 * - Model: Room.java (this file) - Pure data, no business logic
 * - Controller: RoomController.java - Business logic
 * - Repository: RoomRepository.java - Persistence
 * - View: RoomPanel.java - UI rendering
 */
public class Room {
    
    // ═══════════════════════════════════════════════════════════
    // ROOM TYPES
    // ═══════════════════════════════════════════════════════════
    
    public enum RoomType {
        PUBLIC,     // Anyone can enter
        PRIVATE,    // Owner controls access
        LOCKED      // Password required
    }
    
    // ═══════════════════════════════════════════════════════════
    // FIELDS
    // ═══════════════════════════════════════════════════════════
    
    private final String roomId;
    private String roomName;
    private String ownerUsername;
    private RoomType roomType;
    private String description;
    private int maxPlayers;
    private int currentPlayerCount;
    
    // Room layout
    private int width;
    private int height;
    private int[][] tileMap;
    
    // Furniture in the room
    private List<Furniture> furniture;
    
    // Access control
    private Set<String> allowedUsers;    // For private rooms
    private Set<String> bannedUsers;     // Banned from this room
    private String password;              // For locked rooms
    
    // Timestamps
    private long createdAt;
    private long lastVisited;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Create a new room with generated ID
     */
    public Room(String roomName, String ownerUsername, int width, int height) {
        this.roomId = generateRoomId();
        this.roomName = roomName;
        this.ownerUsername = ownerUsername;
        this.roomType = RoomType.PUBLIC;
        this.description = "";
        this.maxPlayers = 25;
        this.currentPlayerCount = 0;
        this.width = width;
        this.height = height;
        this.tileMap = createDefaultTileMap(width, height);
        this.furniture = new ArrayList<>();
        this.allowedUsers = new HashSet<>();
        this.bannedUsers = new HashSet<>();
        this.password = null;
        this.createdAt = System.currentTimeMillis();
        this.lastVisited = this.createdAt;
    }
    
    /**
     * Create room with specific ID (for deserialization)
     */
    public Room(String roomId, String roomName, String ownerUsername) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.ownerUsername = ownerUsername;
        this.roomType = RoomType.PUBLIC;
        this.description = "";
        this.maxPlayers = 25;
        this.currentPlayerCount = 0;
        this.width = 9;
        this.height = 5;
        this.tileMap = createDefaultTileMap(width, height);
        this.furniture = new ArrayList<>();
        this.allowedUsers = new HashSet<>();
        this.bannedUsers = new HashSet<>();
        this.password = null;
        this.createdAt = System.currentTimeMillis();
        this.lastVisited = this.createdAt;
    }
    
    // ═══════════════════════════════════════════════════════════
    // ID GENERATION
    // ═══════════════════════════════════════════════════════════
    
    private String generateRoomId() {
        return "room_" + System.currentTimeMillis() + "_" + 
               (int)(Math.random() * 10000);
    }
    
    // ═══════════════════════════════════════════════════════════
    // DEFAULT TILE MAP
    // ═══════════════════════════════════════════════════════════
    
    private int[][] createDefaultTileMap(int width, int height) {
        int[][] map = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = 1; // Default floor tile
            }
        }
        return map;
    }
    
    // ═══════════════════════════════════════════════════════════
    // ACCESS CONTROL
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Check if a user can enter this room
     */
    public boolean canEnter(String username) {
        // Banned users can't enter
        if (bannedUsers.contains(username.toLowerCase())) {
            return false;
        }
        
        // Owner can always enter
        if (isOwner(username)) {
            return true;
        }
        
        // Check room type
        switch (roomType) {
            case PUBLIC:
                return true;
            case PRIVATE:
                return allowedUsers.contains(username.toLowerCase());
            case LOCKED:
                // Password check handled separately
                return false;
            default:
                return true;
        }
    }
    
    /**
     * Check if user is the room owner
     */
    public boolean isOwner(String username) {
        return ownerUsername.equalsIgnoreCase(username);
    }
    
    /**
     * Check password for locked rooms
     */
    public boolean checkPassword(String inputPassword) {
        if (roomType != RoomType.LOCKED || password == null) {
            return true;
        }
        return password.equals(inputPassword);
    }
    
    // ═══════════════════════════════════════════════════════════
    // USER MANAGEMENT
    // ═══════════════════════════════════════════════════════════
    
    public void addAllowedUser(String username) {
        allowedUsers.add(username.toLowerCase());
    }
    
    public void removeAllowedUser(String username) {
        allowedUsers.remove(username.toLowerCase());
    }
    
    public void banUser(String username) {
        bannedUsers.add(username.toLowerCase());
        allowedUsers.remove(username.toLowerCase());
    }
    
    public void unbanUser(String username) {
        bannedUsers.remove(username.toLowerCase());
    }
    
    // ═══════════════════════════════════════════════════════════
    // FURNITURE MANAGEMENT
    // ═══════════════════════════════════════════════════════════
    
    public void addFurniture(Furniture item) {
        furniture.add(item);
    }
    
    public void removeFurniture(Furniture item) {
        furniture.remove(item);
    }
    
    public List<Furniture> getFurniture() {
        return new ArrayList<>(furniture);
    }
    
    public void clearFurniture() {
        furniture.clear();
    }
    
    // ═══════════════════════════════════════════════════════════
    // GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════
    
    public String getRoomId() {
        return roomId;
    }
    
    public String getRoomName() {
        return roomName;
    }
    
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
    
    public String getOwnerUsername() {
        return ownerUsername;
    }
    
    public RoomType getRoomType() {
        return roomType;
    }
    
    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    
    public int getCurrentPlayerCount() {
        return currentPlayerCount;
    }
    
    public void setCurrentPlayerCount(int count) {
        this.currentPlayerCount = count;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int[][] getTileMap() {
        return tileMap;
    }
    
    public void setTileMap(int[][] tileMap) {
        this.tileMap = tileMap;
        this.width = tileMap.length;
        this.height = tileMap[0].length;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
        if (password != null && !password.isEmpty()) {
            this.roomType = RoomType.LOCKED;
        }
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public long getLastVisited() {
        return lastVisited;
    }
    
    public void updateLastVisited() {
        this.lastVisited = System.currentTimeMillis();
    }
    
    // ═══════════════════════════════════════════════════════════
    // SERIALIZATION
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Serialize room to string for file storage
     * Format: roomId|roomName|ownerUsername|roomType|description|maxPlayers|width|height|tileMapData
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(roomId).append("|");
        sb.append(roomName).append("|");
        sb.append(ownerUsername).append("|");
        sb.append(roomType.name()).append("|");
        sb.append(description != null ? description : "").append("|");
        sb.append(maxPlayers).append("|");
        sb.append(width).append("|");
        sb.append(height).append("|");
        sb.append(serializeTileMap());
        return sb.toString();
    }
    
    private String serializeTileMap() {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x > 0 || y > 0) sb.append(",");
                sb.append(tileMap[x][y]);
            }
        }
        return sb.toString();
    }
    
    /**
     * Deserialize room from string
     */
    public static Room deserialize(String data) {
        String[] parts = data.split("\\|", -1);
        
        if (parts.length < 8) {
            throw new IllegalArgumentException("Invalid room data: " + data);
        }
        
        String roomId = parts[0];
        String roomName = parts[1];
        String ownerUsername = parts[2];
        RoomType roomType = RoomType.valueOf(parts[3]);
        String description = parts[4];
        int maxPlayers = Integer.parseInt(parts[5]);
        int width = Integer.parseInt(parts[6]);
        int height = Integer.parseInt(parts[7]);
        
        Room room = new Room(roomId, roomName, ownerUsername);
        room.roomType = roomType;
        room.description = description;
        room.maxPlayers = maxPlayers;
        room.width = width;
        room.height = height;
        
        // Parse tile map if present
        if (parts.length > 8 && !parts[8].isEmpty()) {
            room.tileMap = deserializeTileMap(parts[8], width, height);
        } else {
            room.tileMap = room.createDefaultTileMap(width, height);
        }
        
        return room;
    }
    
    private static int[][] deserializeTileMap(String data, int width, int height) {
        int[][] map = new int[width][height];
        String[] tiles = data.split(",");
        int index = 0;
        
        for (int x = 0; x < width && index < tiles.length; x++) {
            for (int y = 0; y < height && index < tiles.length; y++) {
                map[x][y] = Integer.parseInt(tiles[index++]);
            }
        }
        
        return map;
    }
    
    // ═══════════════════════════════════════════════════════════
    // TO STRING
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public String toString() {
        return "Room{" +
               "id='" + roomId + '\'' +
               ", name='" + roomName + '\'' +
               ", owner='" + ownerUsername + '\'' +
               ", type=" + roomType +
               '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return roomId.equals(room.roomId);
    }
    
    @Override
    public int hashCode() {
        return roomId.hashCode();
    }
}