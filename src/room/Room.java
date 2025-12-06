package room;

import object.Furniture;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single room in the game (like Habbo Hotel)
 * Each room has its own tile layout, furniture, and player list
 */
public class Room {
    
    // Room identification
    private final String roomId;           // Unique identifier
    private String roomName;               // Display name
    private String ownerUsername;          // Who created this room
    
    // Room dimensions (same as world dimensions)
    private final int maxCol;
    private final int maxRow;
    
    // Room data
    private int[][] tileMap;               // Tile layout
    private List<Furniture> furniture;     // Placed furniture
    private int maxPlayers;                // Player capacity
    
    // Room type
    private RoomType roomType;
    
    public enum RoomType {
        PUBLIC,    // Anyone can enter
        PRIVATE,   // Only owner and invited players
        APARTMENT  // Personal room
    }
    
    /**
     * Create a new room with default grass floor
     */
    public Room(String roomName, String ownerUsername, int maxCol, int maxRow) {
        this.roomId = UUID.randomUUID().toString();
        this.roomName = roomName;
        this.ownerUsername = ownerUsername;
        this.maxCol = maxCol;
        this.maxRow = maxRow;
        this.furniture = new ArrayList<>();
        this.maxPlayers = 10;
        this.roomType = RoomType.PUBLIC;
        
        // Initialize with default tiles (all grass = 1)
        this.tileMap = new int[maxCol][maxRow];
        for (int col = 0; col < maxCol; col++) {
            for (int row = 0; row < maxRow; row++) {
                tileMap[col][row] = 0;  // Default: 0 wooden floor
            }
        }
    }
    
    /**
     * Create room with custom tile layout
     */
    public Room(String roomId, String roomName, String ownerUsername, 
                int maxCol, int maxRow, int[][] tileMap) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.ownerUsername = ownerUsername;
        this.maxCol = maxCol;
        this.maxRow = maxRow;
        this.tileMap = tileMap;
        this.furniture = new ArrayList<>();
        this.maxPlayers = 10;
        this.roomType = RoomType.PUBLIC;
    }
    
    // ═══════════════════════════════════════════════════════════
    // FURNITURE MANAGEMENT
    // ═══════════════════════════════════════════════════════════
    
    public void addFurniture(Furniture furniture) {
        this.furniture.add(furniture);
    }
    
    public void removeFurniture(Furniture furniture) {
        this.furniture.remove(furniture);
    }
    
    public List<Furniture> getFurniture() {
        return new ArrayList<>(furniture);  // Return copy
    }
    
    public void clearFurniture() {
        furniture.clear();
    }
    
    // ═══════════════════════════════════════════════════════════
    // TILE MANAGEMENT
    // ═══════════════════════════════════════════════════════════
    
    public void setTile(int col, int row, int tileNum) {
        if (col >= 0 && col < maxCol && row >= 0 && row < maxRow) {
            tileMap[col][row] = tileNum;
        }
    }
    
    public int getTile(int col, int row) {
        if (col >= 0 && col < maxCol && row >= 0 && row < maxRow) {
            return tileMap[col][row];
        }
        return 0;
    }
    
    public int[][] getTileMap() {
        return tileMap;
    }
    
    public void setTileMap(int[][] tileMap) {
        this.tileMap = tileMap;
    }
    
    // ═══════════════════════════════════════════════════════════
    // ROOM PROPERTIES
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
    
    public int getMaxCol() {
        return maxCol;
    }
    
    public int getMaxRow() {
        return maxRow;
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    
    public RoomType getRoomType() {
        return roomType;
    }
    
    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }
    
    // ═══════════════════════════════════════════════════════════
    // PERMISSION CHECKS
    // ═══════════════════════════════════════════════════════════
    
    public boolean canEnter(String username) {
        switch (roomType) {
            case PUBLIC:
                return true;
            case PRIVATE:
            case APARTMENT:
                return username.equals(ownerUsername);
            default:
                return false;
        }
    }
    
    public boolean isOwner(String username) {
        return username.equals(ownerUsername);
    }
    
    // ═══════════════════════════════════════════════════════════
    // SERIALIZATION SUPPORT
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Convert room to string for saving
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(roomId).append("|");
        sb.append(roomName).append("|");
        sb.append(ownerUsername).append("|");
        sb.append(maxCol).append("|");
        sb.append(maxRow).append("|");
        sb.append(maxPlayers).append("|");
        sb.append(roomType.name()).append("|");
        
        // Serialize tile map
        for (int col = 0; col < maxCol; col++) {
            for (int row = 0; row < maxRow; row++) {
                sb.append(tileMap[col][row]);
                if (row < maxRow - 1) sb.append(",");
            }
            if (col < maxCol - 1) sb.append(";");
        }
        
        return sb.toString();
    }
    
    /**
     * Create room from string
     */
    public static Room deserialize(String data) {
        String[] parts = data.split("\\|");
        
        String roomId = parts[0];
        String roomName = parts[1];
        String ownerUsername = parts[2];
        int maxCol = Integer.parseInt(parts[3]);
        int maxRow = Integer.parseInt(parts[4]);
        int maxPlayers = Integer.parseInt(parts[5]);
        RoomType roomType = RoomType.valueOf(parts[6]);
        
        // Parse tile map
        int[][] tileMap = new int[maxCol][maxRow];
        String[] cols = parts[7].split(";");
        for (int col = 0; col < maxCol; col++) {
            String[] rows = cols[col].split(",");
            for (int row = 0; row < maxRow; row++) {
                tileMap[col][row] = Integer.parseInt(rows[row]);
            }
        }
        
        Room room = new Room(roomId, roomName, ownerUsername, maxCol, maxRow, tileMap);
        room.setMaxPlayers(maxPlayers);
        room.setRoomType(roomType);
        
        return room;
    }
    
    @Override
    public String toString() {
        return roomName + " (ID: " + roomId.substring(0, 8) + "...) by " + ownerUsername;
    }
}