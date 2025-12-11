// package controller.room;

// import model.room.Room;
// import main.GameConstants;
// import main.GamePanel;

// import java.util.*;

// /**
//  * RoomController - Business logic for room management
//  * 
//  * Part of MVC Architecture:
//  * - Handles all room operations (create, enter, leave, delete)
//  * - Coordinates with Repository for persistence
//  * - Notifies listeners of room changes
//  * - Does NOT handle UI rendering
//  * 
//  * Observer Pattern:
//  * - Views subscribe to room change events
//  * - Controller notifies when room state changes
//  */
// public class RoomController {
    
//     private GamePanel gp;
//     private RoomRepository repository;
    
//     // Current room state
//     private Room currentRoom;
//     private String currentRoomId;
    
//     // Event listeners
//     private List<RoomChangeListener> listeners;
    
//     // ═══════════════════════════════════════════════════════════
//     // LISTENER INTERFACE
//     // ═══════════════════════════════════════════════════════════
    
//     public interface RoomChangeListener {
//         void onRoomEntered(Room room);
//         void onRoomLeft(Room room);
//         void onRoomCreated(Room room);
//         void onRoomDeleted(Room room);
//         void onRoomListChanged();
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // CONSTRUCTOR
//     // ═══════════════════════════════════════════════════════════
    
//     public RoomController(GamePanel gp) {
//         this.gp = gp;
//         this.repository = new RoomRepository();
//         this.listeners = new ArrayList<>();
        
//         // Load rooms from storage
//         repository.loadAllRooms();
        
//         // Start in lobby
//         currentRoom = repository.findById(GameConstants.LOBBY_ROOM_ID);
//         currentRoomId = GameConstants.LOBBY_ROOM_ID;
        
//         if (currentRoom == null) {
//             System.err.println("[ROOM CTRL] WARNING: Lobby room not found!");
//         } else {
//             System.out.println("[ROOM CTRL] Starting in lobby: " + currentRoom.getRoomName());
//         }
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // LISTENER MANAGEMENT
//     // ═══════════════════════════════════════════════════════════
    
//     public void addListener(RoomChangeListener listener) {
//         listeners.add(listener);
//     }
    
//     public void removeListener(RoomChangeListener listener) {
//         listeners.remove(listener);
//     }
    
//     private void notifyRoomEntered(Room room) {
//         for (RoomChangeListener listener : listeners) {
//             listener.onRoomEntered(room);
//         }
//     }
    
//     private void notifyRoomLeft(Room room) {
//         for (RoomChangeListener listener : listeners) {
//             listener.onRoomLeft(room);
//         }
//     }
    
//     private void notifyRoomCreated(Room room) {
//         for (RoomChangeListener listener : listeners) {
//             listener.onRoomCreated(room);
//         }
//     }
    
//     private void notifyRoomDeleted(Room room) {
//         for (RoomChangeListener listener : listeners) {
//             listener.onRoomDeleted(room);
//         }
//     }
    
//     private void notifyRoomListChanged() {
//         for (RoomChangeListener listener : listeners) {
//             listener.onRoomListChanged();
//         }
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // ROOM CREATION
//     // ═══════════════════════════════════════════════════════════
    
//     /**
//      * Create a new room
//      */
//     public Room createRoom(String roomName, String ownerUsername) {
//         // Validate
//         if (roomName == null || roomName.trim().isEmpty()) {
//             System.err.println("[ROOM CTRL] Cannot create room: name is empty");
//             return null;
//         }
        
//         // Create room
//         Room room = new Room(roomName.trim(), ownerUsername, gp.maxWorldCol, gp.maxWorldRow);
        
//         // Save to repository
//         repository.save(room);
        
//         System.out.println("[ROOM CTRL] Created room: " + room.getRoomName() + " [" + room.getRoomId() + "]");
        
//         // Notify listeners
//         notifyRoomCreated(room);
//         notifyRoomListChanged();
        
//         return room;
//     }
    
//     /**
//      * Create room with custom tile layout
//      */
//     public Room createRoom(String roomName, String ownerUsername, int[][] tileLayout) {
//         Room room = createRoom(roomName, ownerUsername);
//         if (room != null && tileLayout != null) {
//             room.setTileMap(tileLayout);
//             repository.save(room);
//         }
//         return room;
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // ROOM DELETION
//     // ═══════════════════════════════════════════════════════════
    
//     /**
//      * Delete a room (only owner can delete)
//      */
//     public boolean deleteRoom(String roomId, String username) {
//         Room room = repository.findById(roomId);
        
//         // Validations
//         if (room == null) {
//             System.err.println("[ROOM CTRL] Cannot delete: room not found");
//             return false;
//         }
        
//         if (!room.isOwner(username)) {
//             System.err.println("[ROOM CTRL] Cannot delete: not the owner");
//             return false;
//         }
        
//         if (roomId.equals(GameConstants.LOBBY_ROOM_ID)) {
//             System.err.println("[ROOM CTRL] Cannot delete: lobby is protected");
//             return false;
//         }
        
//         // If currently in this room, return to lobby first
//         if (roomId.equals(currentRoomId)) {
//             returnToLobby();
//         }
        
//         // Delete from repository
//         repository.delete(roomId);
        
//         System.out.println("[ROOM CTRL] Deleted room: " + room.getRoomName());
        
//         // Notify listeners
//         notifyRoomDeleted(room);
//         notifyRoomListChanged();
        
//         return true;
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // ROOM NAVIGATION
//     // ═══════════════════════════════════════════════════════════
    
//     /**
//      * Enter a room
//      */
//     public boolean enterRoom(String roomId, String username) {
//         Room room = repository.findById(roomId);
        
//         // Validations
//         if (room == null) {
//             System.err.println("[ROOM CTRL] Cannot enter: room not found - " + roomId);
//             return false;
//         }
        
//         if (!room.canEnter(username)) {
//             System.err.println("[ROOM CTRL] Cannot enter: access denied to " + room.getRoomName());
//             return false;
//         }
        
//         // Leave current room
//         leaveCurrentRoom();
        
//         // Enter new room
//         Room oldRoom = currentRoom;
//         currentRoom = room;
//         currentRoomId = roomId;
//         room.updateLastVisited();
        
//         // Update game state
//         updateGameForRoom();
        
//         // Notify network
//         if (gp.networkManager != null && gp.networkManager.isConnected()) {
//             gp.networkManager.sendRoomChange(roomId);
//         }
        
//         System.out.println("[ROOM CTRL] Entered room: " + room.getRoomName());
        
//         // Notify listeners
//         if (oldRoom != null) {
//             notifyRoomLeft(oldRoom);
//         }
//         notifyRoomEntered(room);
        
//         return true;
//     }
    
//     /**
//      * Enter a locked room with password
//      */
//     public boolean enterRoomWithPassword(String roomId, String username, String password) {
//         Room room = repository.findById(roomId);
        
//         if (room == null) {
//             return false;
//         }
        
//         if (!room.checkPassword(password)) {
//             System.err.println("[ROOM CTRL] Wrong password for room: " + room.getRoomName());
//             return false;
//         }
        
//         return enterRoom(roomId, username);
//     }
    
//     /**
//      * Return to lobby
//      */
//     public void returnToLobby() {
//         enterRoom(GameConstants.LOBBY_ROOM_ID, gp.player.name);
//     }
    
//     /**
//      * Leave current room (internal)
//      */
//     private void leaveCurrentRoom() {
//         if (currentRoom == null) return;
        
//         // Clear furniture
//         gp.furnitureManager.clearFurniture();
        
//         // Notify network
//         if (gp.networkManager != null && gp.networkManager.isConnected()) {
//             gp.networkManager.sendLeaveRoom(currentRoomId);
//         }
//     }
    
//     /**
//      * Update game state for new room
//      */
//     private void updateGameForRoom() {
//         if (currentRoom == null) return;
        
//         // Update tile manager
//         gp.tile_manager.setMapTileNum(currentRoom.getTileMap());
        
//         // Load furniture
//         gp.furnitureManager.clearFurniture();
//         for (object.Furniture furniture : currentRoom.getFurniture()) {
//             gp.furnitureManager.addFurniture(furniture);
//         }
        
//         // Reset player position
//         gp.player.movement.xCurrent = 4;  // Center
//         gp.player.movement.yCurrent = 2;
//         gp.player.updateSpritePosition();
        
//         // Clear remote players
//         gp.removeAllRemotePlayers();
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // ROOM QUERIES
//     // ═══════════════════════════════════════════════════════════
    
//     public Room getCurrentRoom() {
//         return currentRoom;
//     }
    
//     public String getCurrentRoomId() {
//         return currentRoomId;
//     }
    
//     public Room getRoom(String roomId) {
//         return repository.findById(roomId);
//     }
    
//     public List<Room> getAllRooms() {
//         return repository.findAll();
//     }
    
//     public List<Room> getPublicRooms() {
//         return repository.findPublicRooms();
//     }
    
//     public List<Room> getMyRooms(String username) {
//         return repository.findByOwner(username);
//     }
    
//     public List<Room> searchRooms(String query) {
//         return repository.searchByName(query);
//     }
    
//     public int getRoomCount() {
//         return repository.count();
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // FAVORITES
//     // ═══════════════════════════════════════════════════════════
    
//     private List<String> favoriteRoomIds = new ArrayList<>();
    
//     public void addToFavorites(String roomId) {
//         if (!favoriteRoomIds.contains(roomId)) {
//             favoriteRoomIds.add(roomId);
//             repository.saveFavorites(gp.player.name, favoriteRoomIds);
//             System.out.println("[ROOM CTRL] Added to favorites: " + roomId);
//         }
//     }
    
//     public void removeFromFavorites(String roomId) {
//         favoriteRoomIds.remove(roomId);
//         repository.saveFavorites(gp.player.name, favoriteRoomIds);
//         System.out.println("[ROOM CTRL] Removed from favorites: " + roomId);
//     }
    
//     public boolean isFavorite(String roomId) {
//         return favoriteRoomIds.contains(roomId);
//     }
    
//     public List<Room> getFavoriteRooms() {
//         return repository.getFavoriteRooms(gp.player.name);
//     }
    
//     public void loadFavorites() {
//         favoriteRoomIds = repository.loadFavorites(gp.player.name);
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // ROOM SETTINGS (Owner only)
//     // ═══════════════════════════════════════════════════════════
    
//     public boolean updateRoomName(String roomId, String username, String newName) {
//         Room room = repository.findById(roomId);
        
//         if (room == null || !room.isOwner(username)) {
//             return false;
//         }
        
//         room.setRoomName(newName.trim());
//         repository.save(room);
//         notifyRoomListChanged();
        
//         return true;
//     }
    
//     public boolean updateRoomType(String roomId, String username, Room.RoomType newType) {
//         Room room = repository.findById(roomId);
        
//         if (room == null || !room.isOwner(username)) {
//             return false;
//         }
        
//         room.setRoomType(newType);
//         repository.save(room);
//         notifyRoomListChanged();
        
//         return true;
//     }
    
//     public boolean setRoomPassword(String roomId, String username, String password) {
//         Room room = repository.findById(roomId);
        
//         if (room == null || !room.isOwner(username)) {
//             return false;
//         }
        
//         room.setPassword(password);
//         repository.save(room);
        
//         return true;
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // PERSISTENCE
//     // ═══════════════════════════════════════════════════════════
    
//     /**
//      * Save all rooms (call on game exit)
//      */
//     public void saveRooms() {
//         repository.saveAllRooms();
//     }
    
//     /**
//      * Reload rooms from file
//      */
//     public void reloadRooms() {
//         repository.reload();
//         notifyRoomListChanged();
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // CLEANUP
//     // ═══════════════════════════════════════════════════════════
    
//     public void shutdown() {
//         System.out.println("[ROOM CTRL] Shutting down...");
//         saveRooms();
//         listeners.clear();
//     }
// }
package controller.room;

import model.room.Room;
import main.GameConstants;
import main.GamePanel;
import service.api.RoomApiClient;

import java.util.*;

/**
 * RoomController - Business logic for room management
 * 
 * UPDATED: Now uses central server API instead of local file storage
 */
public class RoomController {
    
    private GamePanel gp;
    private RoomApiClient apiClient;  // ✅ NEW - API client
    
    // Local cache for faster access
    private Map<String, Room> roomCache;
    
    // Current room state
    private Room currentRoom;
    private String currentRoomId;
    
    // Event listeners
    private List<RoomChangeListener> listeners;
    
    // ═══════════════════════════════════════════════════════════
    // LISTENER INTERFACE
    // ═══════════════════════════════════════════════════════════
    
    public interface RoomChangeListener {
        void onRoomEntered(Room room);
        void onRoomLeft(Room room);
        void onRoomCreated(Room room);
        void onRoomDeleted(Room room);
        void onRoomListChanged();
    }
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public RoomController(GamePanel gp) {
        this.gp = gp;
        this.apiClient = new RoomApiClient();  // ✅ NEW
        this.roomCache = new HashMap<>();
        this.listeners = new ArrayList<>();
        
        System.out.println("[ROOM CTRL] Initialized with API client");
    }
    
    /**
     * Initialize after player is set (call this after SSO validation)
     */
    public void initialize(String username) {
        // Set username for API calls
        apiClient.setCurrentUsername(username);
        
        // Load rooms from server
        refreshRoomCache();
        
        // Start in lobby
        currentRoom = roomCache.get(GameConstants.LOBBY_ROOM_ID);
        currentRoomId = GameConstants.LOBBY_ROOM_ID;
        
        if (currentRoom == null) {
            System.err.println("[ROOM CTRL] WARNING: Lobby room not found on server!");
            // Try to get it directly
            currentRoom = apiClient.getRoom(GameConstants.LOBBY_ROOM_ID);
            if (currentRoom != null) {
                roomCache.put(currentRoom.getRoomId(), currentRoom);
            }
        }
        
        if (currentRoom != null) {
            System.out.println("[ROOM CTRL] Starting in lobby: " + currentRoom.getRoomName());
            // Notify server we entered lobby
            apiClient.enterRoom(GameConstants.LOBBY_ROOM_ID);
        }
    }
    
    /**
     * Refresh room cache from server
     */
    public void refreshRoomCache() {
        System.out.println("[ROOM CTRL] Refreshing room cache from server...");
        
        List<Room> publicRooms = apiClient.getPublicRooms();
        
        roomCache.clear();
        for (Room room : publicRooms) {
            roomCache.put(room.getRoomId(), room);
        }
        
        System.out.println("[ROOM CTRL] Loaded " + roomCache.size() + " rooms from server");
        notifyRoomListChanged();
    }
    
    // ═══════════════════════════════════════════════════════════
    // LISTENER MANAGEMENT
    // ═══════════════════════════════════════════════════════════
    
    public void addListener(RoomChangeListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(RoomChangeListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyRoomEntered(Room room) {
        for (RoomChangeListener listener : listeners) {
            listener.onRoomEntered(room);
        }
    }
    
    private void notifyRoomLeft(Room room) {
        for (RoomChangeListener listener : listeners) {
            listener.onRoomLeft(room);
        }
    }
    
    private void notifyRoomCreated(Room room) {
        for (RoomChangeListener listener : listeners) {
            listener.onRoomCreated(room);
        }
    }
    
    private void notifyRoomDeleted(Room room) {
        for (RoomChangeListener listener : listeners) {
            listener.onRoomDeleted(room);
        }
    }
    
    private void notifyRoomListChanged() {
        for (RoomChangeListener listener : listeners) {
            listener.onRoomListChanged();
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // ROOM CREATION
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Create a new room via API
     */
    public Room createRoom(String roomName, String ownerUsername) {
        if (roomName == null || roomName.trim().isEmpty()) {
            System.err.println("[ROOM CTRL] Cannot create room: name is empty");
            return null;
        }
        
        // ✅ Create via API
        Room room = apiClient.createRoom(roomName.trim());
        
        if (room != null) {
            // Add to local cache
            roomCache.put(room.getRoomId(), room);
            
            System.out.println("[ROOM CTRL] Created room: " + room.getRoomName() + " [" + room.getRoomId() + "]");
            
            // Notify listeners
            notifyRoomCreated(room);
            notifyRoomListChanged();
        }
        
        return room;
    }
    
    /**
     * Create room with custom tile layout
     */
    public Room createRoom(String roomName, String ownerUsername, int[][] tileLayout) {
        Room room = createRoom(roomName, ownerUsername);
        if (room != null && tileLayout != null) {
            room.setTileMap(tileLayout);
            // TODO: Send tile layout to server
        }
        return room;
    }
    
    // ═══════════════════════════════════════════════════════════
    // ROOM DELETION
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Delete a room via API
     */
    public boolean deleteRoom(String roomId, String username) {
        Room room = roomCache.get(roomId);
        
        if (room == null) {
            System.err.println("[ROOM CTRL] Cannot delete: room not found in cache");
            return false;
        }
        
        if (!room.isOwner(username)) {
            System.err.println("[ROOM CTRL] Cannot delete: not the owner");
            return false;
        }
        
        if (roomId.equals(GameConstants.LOBBY_ROOM_ID)) {
            System.err.println("[ROOM CTRL] Cannot delete: lobby is protected");
            return false;
        }
        
        // If currently in this room, return to lobby first
        if (roomId.equals(currentRoomId)) {
            returnToLobby();
        }
        
        // ✅ Delete via API
        boolean success = apiClient.deleteRoom(roomId);
        
        if (success) {
            roomCache.remove(roomId);
            System.out.println("[ROOM CTRL] Deleted room: " + room.getRoomName());
            
            notifyRoomDeleted(room);
            notifyRoomListChanged();
        }
        
        return success;
    }
    
    // ═══════════════════════════════════════════════════════════
    // ROOM NAVIGATION
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Enter a room via API
     */
    public boolean enterRoom(String roomId, String username) {
        Room room = roomCache.get(roomId);
        
        // If not in cache, try to fetch from server
        if (room == null) {
            room = apiClient.getRoom(roomId);
            if (room != null) {
                roomCache.put(roomId, room);
            }
        }
        
        if (room == null) {
            System.err.println("[ROOM CTRL] Cannot enter: room not found - " + roomId);
            return false;
        }
        
        if (!room.canEnter(username)) {
            System.err.println("[ROOM CTRL] Cannot enter: access denied to " + room.getRoomName());
            return false;
        }
        
        // Leave current room
        leaveCurrentRoom();
        
        // ✅ Notify server we're entering
        boolean success = apiClient.enterRoom(roomId);
        
        if (!success) {
            System.err.println("[ROOM CTRL] Server rejected room entry");
            return false;
        }
        
        // Enter new room
        Room oldRoom = currentRoom;
        currentRoom = room;
        currentRoomId = roomId;
        
        // Update game state
        updateGameForRoom();
        
        // Notify network (for multiplayer sync)
        if (gp.networkManager != null && gp.networkManager.isConnected()) {
            gp.networkManager.sendRoomChange(roomId);
        }
        
        System.out.println("[ROOM CTRL] Entered room: " + room.getRoomName());
        
        // Notify listeners
        if (oldRoom != null) {
            notifyRoomLeft(oldRoom);
        }
        notifyRoomEntered(room);
        
        return true;
    }
    
    /**
     * Enter a locked room with password
     */
    public boolean enterRoomWithPassword(String roomId, String username, String password) {
        Room room = roomCache.get(roomId);
        
        if (room == null) {
            return false;
        }
        
        // ✅ Verify password via API
        boolean success = apiClient.enterRoomWithPassword(roomId, password);
        
        if (!success) {
            System.err.println("[ROOM CTRL] Wrong password for room: " + room.getRoomName());
            return false;
        }
        
        // Password correct, enter room
        leaveCurrentRoom();
        
        Room oldRoom = currentRoom;
        currentRoom = room;
        currentRoomId = roomId;
        
        updateGameForRoom();
        
        if (gp.networkManager != null && gp.networkManager.isConnected()) {
            gp.networkManager.sendRoomChange(roomId);
        }
        
        System.out.println("[ROOM CTRL] Entered locked room: " + room.getRoomName());
        
        if (oldRoom != null) {
            notifyRoomLeft(oldRoom);
        }
        notifyRoomEntered(room);
        
        return true;
    }
    
    /**
     * Return to lobby
     */
    public void returnToLobby() {
        enterRoom(GameConstants.LOBBY_ROOM_ID, gp.player.name);
    }
    
    /**
     * Leave current room
     */
    private void leaveCurrentRoom() {
        if (currentRoom == null) return;
        
        // Clear furniture
        gp.furnitureManager.clearFurniture();
        
        // ✅ Notify server we're leaving
        apiClient.leaveRoom(currentRoomId);
        
        // Notify network
        if (gp.networkManager != null && gp.networkManager.isConnected()) {
            gp.networkManager.sendLeaveRoom(currentRoomId);
        }
    }
    
    /**
     * Update game state for new room
     */
    private void updateGameForRoom() {
        if (currentRoom == null) return;
        
        // Update tile manager
        gp.tile_manager.setMapTileNum(currentRoom.getTileMap());
        
        // Load furniture
        gp.furnitureManager.clearFurniture();
        for (object.Furniture furniture : currentRoom.getFurniture()) {
            gp.furnitureManager.addFurniture(furniture);
        }
        
        // Reset player position
        gp.player.movement.xCurrent = 4;
        gp.player.movement.yCurrent = 2;
        gp.player.updateSpritePosition();
        
        // Clear remote players
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
        Room room = roomCache.get(roomId);
        if (room == null) {
            // Try to fetch from server
            room = apiClient.getRoom(roomId);
            if (room != null) {
                roomCache.put(roomId, room);
            }
        }
        return room;
    }
    
    public List<Room> getAllRooms() {
        return new ArrayList<>(roomCache.values());
    }
    
    public List<Room> getPublicRooms() {
        // ✅ Refresh from server
        List<Room> publicRooms = apiClient.getPublicRooms();
        
        // Update cache
        for (Room room : publicRooms) {
            roomCache.put(room.getRoomId(), room);
        }
        
        return publicRooms;
    }
    
    public List<Room> getMyRooms(String username) {
        return apiClient.getMyRooms();
    }
    
    public List<Room> searchRooms(String query) {
        return apiClient.searchRooms(query);
    }
    
    public int getRoomCount() {
        return roomCache.size();
    }
    
    // ═══════════════════════════════════════════════════════════
    // FAVORITES (stored locally for now)
    // ═══════════════════════════════════════════════════════════
    
    private List<String> favoriteRoomIds = new ArrayList<>();
    
    public void addToFavorites(String roomId) {
        if (!favoriteRoomIds.contains(roomId)) {
            favoriteRoomIds.add(roomId);
            System.out.println("[ROOM CTRL] Added to favorites: " + roomId);
        }
    }
    
    public void removeFromFavorites(String roomId) {
        favoriteRoomIds.remove(roomId);
        System.out.println("[ROOM CTRL] Removed from favorites: " + roomId);
    }
    
    public boolean isFavorite(String roomId) {
        return favoriteRoomIds.contains(roomId);
    }
    
    public List<Room> getFavoriteRooms() {
        List<Room> favorites = new ArrayList<>();
        for (String roomId : favoriteRoomIds) {
            Room room = getRoom(roomId);
            if (room != null) {
                favorites.add(room);
            }
        }
        return favorites;
    }
    
    // ═══════════════════════════════════════════════════════════
    // ROOM SETTINGS
    // ═══════════════════════════════════════════════════════════
    
    public boolean updateRoomName(String roomId, String username, String newName) {
        Room room = roomCache.get(roomId);
        
        if (room == null || !room.isOwner(username)) {
            return false;
        }
        
        // TODO: Send update to server
        room.setRoomName(newName.trim());
        notifyRoomListChanged();
        
        return true;
    }
    
    public boolean updateRoomType(String roomId, String username, Room.RoomType newType) {
        Room room = roomCache.get(roomId);
        
        if (room == null || !room.isOwner(username)) {
            return false;
        }
        
        // TODO: Send update to server
        room.setRoomType(newType);
        notifyRoomListChanged();
        
        return true;
    }
    
    public boolean setRoomPassword(String roomId, String username, String password) {
        Room room = roomCache.get(roomId);
        
        if (room == null || !room.isOwner(username)) {
            return false;
        }
        
        // TODO: Send update to server
        room.setPassword(password);
        
        return true;
    }
    
    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE (No longer needed - server handles this)
    // ═══════════════════════════════════════════════════════════
    
    public void saveRooms() {
        // No longer needed - server handles persistence
        System.out.println("[ROOM CTRL] Save not needed - using central server");
    }
    
    public void reloadRooms() {
        refreshRoomCache();
    }
    
    // ═══════════════════════════════════════════════════════════
    // CLEANUP
    // ═══════════════════════════════════════════════════════════
    
    public void shutdown() {
        System.out.println("[ROOM CTRL] Shutting down...");
        
        // Leave current room
        if (currentRoomId != null) {
            apiClient.leaveRoom(currentRoomId);
        }
        
        listeners.clear();
        roomCache.clear();
    }
}