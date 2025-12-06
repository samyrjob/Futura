package room;

import main.GamePanel;
import java.util.*;
import java.io.*;

/**
 * Manages all rooms in the game (like Habbo Hotel room system)
 * Handles room creation, navigation, and persistence
 */
public class RoomManager {
    
    private GamePanel gp;
    private Map<String, Room> rooms;           // All rooms by ID
    private Room currentRoom;                  // Room player is currently in
    private String currentRoomId;
    
    // Default lobby room
    private static final String LOBBY_ID = "lobby";
    
    public RoomManager(GamePanel gp) {
        this.gp = gp;
        this.rooms = new HashMap<>();
        
        // Create default lobby
        createLobby();
        
        // Load saved rooms
        loadRooms();
        
        // Start in lobby
        currentRoom = rooms.get(LOBBY_ID);
        currentRoomId = LOBBY_ID;
    }
    
    // ═══════════════════════════════════════════════════════════
    // ROOM CREATION
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Create the default lobby room
     */
    private void createLobby() {
        Room lobby = new Room("Lobby", "System", gp.maxWorldCol, gp.maxWorldRow);
        lobby.setRoomType(Room.RoomType.PUBLIC);
        lobby.setMaxPlayers(50);
        
        // Load lobby tile layout from existing map
        int[][] lobbyTiles = loadDefaultTileLayout();
        lobby.setTileMap(lobbyTiles);
        
        rooms.put(LOBBY_ID, lobby);
        System.out.println("Lobby created");
    }
    
    /**
     * Create a new room
     */
    public Room createRoom(String roomName, String ownerUsername) {
        Room room = new Room(roomName, ownerUsername, gp.maxWorldCol, gp.maxWorldRow);
        rooms.put(room.getRoomId(), room);
        
        System.out.println("Room created: " + room);
        saveRooms();  // Auto-save
        
        return room;
    }
    
    /**
     * Create room with custom tile layout
     */
    public Room createRoom(String roomName, String ownerUsername, int[][] tileLayout) {
        Room room = new Room(roomName, ownerUsername, gp.maxWorldCol, gp.maxWorldRow);
        room.setTileMap(tileLayout);
        rooms.put(room.getRoomId(), room);
        
        System.out.println("Room created with custom layout: " + room);
        saveRooms();
        
        return room;
    }
    
    /**
     * Delete a room (only owner can delete)
     */
    public boolean deleteRoom(String roomId, String username) {
        Room room = rooms.get(roomId);
        
        if (room == null) return false;
        if (!room.isOwner(username)) return false;
        if (roomId.equals(LOBBY_ID)) return false;  // Can't delete lobby
        
        rooms.remove(roomId);
        System.out.println("Room deleted: " + room);
        saveRooms();
        
        return true;
    }
    
    // ═══════════════════════════════════════════════════════════
    // ROOM NAVIGATION
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Switch to a different room
     */
    public boolean enterRoom(String roomId, String username) {
        Room room = rooms.get(roomId);
        
        if (room == null) {
            System.out.println("Room not found: " + roomId);
            return false;
        }
        
        if (!room.canEnter(username)) {
            System.out.println("Access denied to room: " + room.getRoomName());
            return false;
        }
        
        // Leave current room
        leaveCurrentRoom();
        
        // Enter new room
        currentRoom = room;
        currentRoomId = roomId;
        
        // Update game state
        updateGameForRoom();
        
        // Notify network
        if (gp.networkManager != null && gp.networkManager.isConnected()) {
            gp.networkManager.sendRoomChange(roomId);
        }
        
        System.out.println("Entered room: " + room.getRoomName());
        return true;
    }
    
    /**
     * Return to lobby
     */
    public void returnToLobby() {
        enterRoom(LOBBY_ID, gp.player.name);
    }
    
    /**
     * Leave current room (called when switching rooms)
     */
    private void leaveCurrentRoom() {
        if (currentRoom == null) return;
        
        // Clear current room's furniture from manager
        gp.furnitureManager.clearFurniture();
        
        // Notify network
        if (gp.networkManager != null && gp.networkManager.isConnected()) {
            gp.networkManager.sendLeaveRoom(currentRoomId);
        }
    }
    
    /**
     * Update game state to match current room
     */
    private void updateGameForRoom() {
        if (currentRoom == null) return;
        
        // Update tile manager with room's tile layout
        gp.tile_manager.setMapTileNum(currentRoom.getTileMap());
        
        // Load room's furniture
        gp.furnitureManager.clearFurniture();
        for (object.Furniture furniture : currentRoom.getFurniture()) {
            gp.furnitureManager.addFurniture(furniture);
        }
        
        // Reset player position to room entrance
        gp.player.movement.xCurrent = 4;  // Center of room
        gp.player.movement.yCurrent = 2;
        gp.player.updateSpritePosition();
        
        // Clear remote players (will be repopulated by network)
        gp.removeAllRemotePlayers();
    }
    
    // ═══════════════════════════════════════════════════════════
    // ROOM QUERIES
    // ═══════════════════════════════════════════════════════════
    
    public Room getCurrentRoom() {
        return currentRoom;
    }
    
    public String getCurrentRoomId() {
        return currentRoomId;
    }
    
    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }
    
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }
    
    public List<Room> getPublicRooms() {
        List<Room> publicRooms = new ArrayList<>();
        for (Room room : rooms.values()) {
            if (room.getRoomType() == Room.RoomType.PUBLIC) {
                publicRooms.add(room);
            }
        }
        return publicRooms;
    }
    
    public List<Room> getMyRooms(String username) {
        List<Room> myRooms = new ArrayList<>();
        for (Room room : rooms.values()) {
            if (room.getOwnerUsername().equals(username)) {
                myRooms.add(room);
            }
        }
        return myRooms;
    }
    
    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Save all rooms to file
     */
    public void saveRooms() {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter("rooms.dat"))) {
            
            for (Room room : rooms.values()) {
                writer.write(room.serialize());
                writer.newLine();
            }
            
            System.out.println("Saved " + rooms.size() + " rooms");
            
        } catch (IOException e) {
            System.err.println("Failed to save rooms: " + e.getMessage());
        }
    }
    
    /**
     * Load rooms from file
     */
    public void loadRooms() {
        File file = new File("rooms.dat");
        if (!file.exists()) {
            System.out.println("No saved rooms found");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            
            while ((line = reader.readLine()) != null) {
                try {
                    Room room = Room.deserialize(line);
                    
                    // Don't override lobby
                    if (!room.getRoomId().equals(LOBBY_ID)) {
                        rooms.put(room.getRoomId(), room);
                        count++;
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load room: " + e.getMessage());
                }
            }
            
            System.out.println("Loaded " + count + " rooms");
            
        } catch (IOException e) {
            System.err.println("Failed to load rooms: " + e.getMessage());
        }
    }
    
    /**
     * Load default tile layout from current TileManager
     */
    private int[][] loadDefaultTileLayout() {
        // Copy current tile layout
        int[][] layout = new int[gp.maxWorldCol][gp.maxWorldRow];
        
        if (gp.tile_manager != null && gp.tile_manager.mapTileNum != null) {
            for (int col = 0; col < gp.maxWorldCol; col++) {
                for (int row = 0; row < gp.maxWorldRow; row++) {
                    layout[col][row] = gp.tile_manager.mapTileNum[col][row];
                }
            }
        } else {
            // Default: all grass
            for (int col = 0; col < gp.maxWorldCol; col++) {
                for (int row = 0; row < gp.maxWorldRow; row++) {
                    layout[col][row] = 1;
                }
            }
        }
        
        return layout;
    }


    
}