package controller.room;

import main.GamePanel;
import model.room.Room;
import service.api.RoomApiClient;
import service.websocket.RoomWebSocketClient;

import java.util.List;

/**
 * RoomController - Coordinates all room operations
 * 
 * Username is stored once via initialize()
 * No need to pass username in every method!
 */
public class RoomController {

    private GamePanel gp;
    private RoomApiClient apiClient;
    private RoomCache cache;
    private RoomListenerManager listenerManager;
    private RoomFavoritesManager favoritesManager;
    private RoomWebSocketClient webSocketClient;
    
    private Room currentRoom;
    private String currentRoomId;
    private String username;  // ✅ Store username once

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public RoomController(GamePanel gp) {
        this.gp = gp;
        this.apiClient = new RoomApiClient();
        this.cache = new RoomCache(apiClient);
        this.listenerManager = new RoomListenerManager();
        this.favoritesManager = new RoomFavoritesManager();
        
        System.out.println("[ROOM CTRL] Controller created");
    }

    // ═══════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════

    public void initialize(String username) {
        System.out.println("[ROOM CTRL] Initializing for user: " + username);
        
        this.username = username;  // ✅ Store once
        apiClient.setCurrentUsername(username);
        cache.refresh();
        
        Room lobby = cache.get("lobby");
        if (lobby != null) {
            enterRoom("lobby");  // ✅ No username needed
        } else {
            System.out.println("[ROOM CTRL] Warning: Lobby not found!");
        }
    }
    
    public String getUsername() {
        return this.username;
    }

    // ═══════════════════════════════════════════════════════════
    // ROOM ACCESS
    // ═══════════════════════════════════════════════════════════

    public Room getCurrentRoom() { return currentRoom; }
    public String getCurrentRoomId() { return currentRoomId; }
    public Room getRoom(String roomId) { return cache.get(roomId); }
    public List<Room> getAllRooms() { return cache.getAll(); }
    public List<Room> getPublicRooms() { return cache.getPublicRooms(); }
    public List<Room> getFavoriteRooms() { return cache.getByIds(favoritesManager.getAll()); }
    
    public List<Room> getMyRooms() {  // ✅ No username parameter
        return cache.getByOwner(username);
    }
    
    public void refreshRoomCache() {
        cache.refresh();
        listenerManager.notifyRoomListChanged();
    }

    // ═══════════════════════════════════════════════════════════
    // ENTER / LEAVE ROOM
    // ═══════════════════════════════════════════════════════════

    public boolean enterRoom(String roomId) {  // ✅ No username parameter
        Room room = cache.get(roomId);
        
        if (room == null) {
            System.err.println("[ROOM CTRL] Room not found: " + roomId);
            return false;
        }
        
        if (!room.canEnter(username)) {  // ✅ Use stored username
            System.err.println("[ROOM CTRL] Cannot enter room: " + roomId);
            return false;
        }
        
        if (currentRoomId != null && !currentRoomId.equals(roomId)) {
            leaveCurrentRoom();  // ✅ No username needed
        }
        
        boolean success = apiClient.enterRoom(roomId);  // ✅ apiClient knows username
        
        if (success) {
            currentRoom = room;
            currentRoomId = roomId;
            updateGameForRoom();
            listenerManager.notifyRoomEntered(room);
            System.out.println("[ROOM CTRL] Entered room: " + room.getRoomName());
        }
        
        return success;
    }

    public boolean enterRoomWithPassword(String roomId, String password) {  // ✅ No username
        Room room = cache.get(roomId);
        
        if (room == null) {
            System.err.println("[ROOM CTRL] Room not found: " + roomId);
            return false;
        }
        
        if (currentRoomId != null && !currentRoomId.equals(roomId)) {
            leaveCurrentRoom();
        }
        
        boolean success = apiClient.enterRoomWithPassword(roomId, password);
        
        if (success) {
            currentRoom = room;
            currentRoomId = roomId;
            updateGameForRoom();
            listenerManager.notifyRoomEntered(room);
        }
        
        return success;
    }

    public void leaveCurrentRoom() {  // ✅ No username parameter
        if (currentRoomId != null) {
            apiClient.leaveRoom(currentRoomId);  // ✅ apiClient knows username
            Room leftRoom = currentRoom;
            currentRoom = null;
            currentRoomId = null;
            
            if (leftRoom != null) {
                listenerManager.notifyRoomLeft(leftRoom);
            }
        }
    }

    public void returnToLobby() {
        leaveCurrentRoom();  // ✅ Clean!
        enterRoom("lobby");  // ✅ Clean!
    }

    // ═══════════════════════════════════════════════════════════
    // CREATE / DELETE ROOM
    // ═══════════════════════════════════════════════════════════

    public Room createRoom(String roomName) {  // ✅ No ownerUsername parameter
        Room newRoom = apiClient.createRoom(roomName.trim());
        
        if (newRoom != null) {
            cache.put(newRoom);
            listenerManager.notifyRoomCreated(newRoom);
        }
        
        return newRoom;
    }

    public boolean deleteRoom(String roomId) {  // ✅ No username parameter
        Room room = cache.get(roomId);
        
        if (room == null || !room.isOwner(username)) {  // ✅ Use stored username
            return false;
        }
        
        boolean success = apiClient.deleteRoom(roomId);
        
        if (success) {
            cache.remove(roomId);
            favoritesManager.remove(roomId);
            listenerManager.notifyRoomDeleted(room);
        }
        
        return success;
    }

    // ═══════════════════════════════════════════════════════════
    // FAVORITES
    // ═══════════════════════════════════════════════════════════

    public void addFavorite(String roomId) {
        favoritesManager.add(roomId);
        listenerManager.notifyRoomListChanged();
    }

    public void removeFavorite(String roomId) {
        favoritesManager.remove(roomId);
        listenerManager.notifyRoomListChanged();
    }

    public boolean isFavorite(String roomId) {
        return favoritesManager.isFavorite(roomId);
    }

    // ═══════════════════════════════════════════════════════════
    // LISTENERS
    // ═══════════════════════════════════════════════════════════

    public void addListener(RoomListenerManager.RoomChangeListener listener) {
        listenerManager.addListener(listener);
    }

    public void removeListener(RoomListenerManager.RoomChangeListener listener) {
        listenerManager.removeListener(listener);
    }

    public void updatePlayerCount(String roomId, int count) {
        Room room = cache.get(roomId);
        if (room != null) {
            room.setCurrentPlayerCount(count);
            listenerManager.notifyRoomListChanged();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GAME STATE UPDATE
    // ═══════════════════════════════════════════════════════════

    private void updateGameForRoom() {
        if (currentRoom == null || gp == null) return;
        
        // Load tile map
        if (currentRoom.getTileMap() != null && gp.tile_manager != null) {
            gp.tile_manager.setMapTileNum(currentRoom.getTileMap());
        } else {
            gp.tile_manager.loadMap("/res/maps/map01.txt");
        }
        
        // Spawn at corner (Habbo style)
        if (gp.player != null) {
            gp.player.setPosition(0, 0);
        }
        
        // Clear remote players
        gp.removeAllRemotePlayers();
        
        // Notify server
        if (gp.networkManager != null && gp.networkManager.isConnected()) {
            gp.networkManager.sendRoomChange(currentRoomId);
        }
        
        gp.repaint();
    }

    // ═══════════════════════════════════════════════════════════
    // WEBSOCKET
    // ═══════════════════════════════════════════════════════════

    public void startLiveUpdates() {
        if (webSocketClient == null) {
            webSocketClient = new RoomWebSocketClient(this);
        }
        webSocketClient.connect();
    }

    public void stopLiveUpdates() {
        if (webSocketClient != null) {
            webSocketClient.disconnect();
            webSocketClient = null;
        }
    }

    public boolean isLiveUpdatesConnected() {
        return webSocketClient != null && webSocketClient.isConnected();
    }

    // ═══════════════════════════════════════════════════════════
    // SHUTDOWN
    // ═══════════════════════════════════════════════════════════

    public void shutdown() {  // ✅ No username parameter
        stopLiveUpdates();
        leaveCurrentRoom();  // ✅ No username needed
        listenerManager.clear();
        cache.clear();
        favoritesManager.clear();
        System.out.println("[ROOM CTRL] Shutdown complete");
    }
}