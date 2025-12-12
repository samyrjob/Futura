package controller.room;

import main.GamePanel;
import model.room.Room;
import service.api.RoomApiClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Room Controller - Manages rooms via REST API
 * 
 * Features:
 * - Fetches rooms from Spring Boot server
 * - Caches rooms locally for fast access
 * - Notifies listeners of changes
 * - Handles enter/leave/create operations
 */
public class RoomController {

    private GamePanel gp;
    private RoomApiClient apiClient;
    
    // Local cache
    private Map<String, Room> roomCache;
    
    // Current room state
    private Room currentRoom;
    private String currentRoomId;
    
    // Favorites (stored locally for now)
    private Set<String> favoriteRoomIds;
    
    // Listeners
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
        this.apiClient = new RoomApiClient();
        this.roomCache = new ConcurrentHashMap<>();
        this.favoriteRoomIds = new HashSet<>();
        this.listeners = new ArrayList<>();
        
        System.out.println("[ROOM CTRL] Controller created");
    }

    // ═══════════════════════════════════════════════════════════
    // INITIALIZATION (called after SSO validation)
    // ═══════════════════════════════════════════════════════════

    public void initialize(String username) {
        System.out.println("[ROOM CTRL] Initializing for user: " + username);
        
        // Set username for API calls
        apiClient.setCurrentUsername(username);
        
        // Load rooms from server
        refreshRoomCache();
        
        // Enter lobby by default
        Room lobby = roomCache.get("lobby");
        if (lobby != null) {
            System.out.println("[ROOM CTRL] Entering lobby...");
            enterRoom("lobby", username);
        } else {
            System.out.println("[ROOM CTRL] Warning: Lobby room not found!");
        }
        
        System.out.println("[ROOM CTRL] Initialization complete");
    }

    // ═══════════════════════════════════════════════════════════
    // REFRESH ROOMS FROM SERVER
    // ═══════════════════════════════════════════════════════════

    public void refreshRoomCache() {
        System.out.println("[ROOM CTRL] Refreshing room cache from server...");
        
        try {
            List<Room> publicRooms = apiClient.getPublicRooms();
            
            // Clear and repopulate cache
            roomCache.clear();
            for (Room room : publicRooms) {
                roomCache.put(room.getRoomId(), room);
            }
            
            System.out.println("[ROOM CTRL] Loaded " + roomCache.size() + " rooms from server");
            
            // Notify listeners
            notifyRoomListChanged();
            
        } catch (Exception e) {
            System.err.println("[ROOM CTRL] Failed to refresh rooms: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GET ROOMS
    // ═══════════════════════════════════════════════════════════

    public List<Room> getPublicRooms() {
        return roomCache.values().stream()
            .filter(r -> r.getRoomType() == Room.RoomType.PUBLIC)
            .sorted(Comparator.comparing(Room::getRoomName))
            .collect(Collectors.toList());
    }

    public List<Room> getMyRooms(String username) {
        return roomCache.values().stream()
            .filter(r -> r.getOwnerUsername().equalsIgnoreCase(username))
            .sorted(Comparator.comparing(Room::getRoomName))
            .collect(Collectors.toList());
    }

    public List<Room> getFavoriteRooms() {
        return roomCache.values().stream()
            .filter(r -> favoriteRoomIds.contains(r.getRoomId()))
            .sorted(Comparator.comparing(Room::getRoomName))
            .collect(Collectors.toList());
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(roomCache.values());
    }

    public Room getRoom(String roomId) {
        return roomCache.get(roomId);
    }

    // ═══════════════════════════════════════════════════════════
    // CURRENT ROOM
    // ═══════════════════════════════════════════════════════════

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    // ═══════════════════════════════════════════════════════════
    // ENTER ROOM
    // ═══════════════════════════════════════════════════════════

    public boolean enterRoom(String roomId, String username) {
        Room room = roomCache.get(roomId);
        
        if (room == null) {
            // Try to fetch from server
            room = apiClient.getRoom(roomId);
            if (room != null) {
                roomCache.put(roomId, room);
            }
        }
        
        if (room == null) {
            System.err.println("[ROOM CTRL] Room not found: " + roomId);
            return false;
        }
        
        // Check if can enter
        if (!room.canEnter(username)) {
            System.err.println("[ROOM CTRL] Cannot enter room: " + roomId);
            return false;
        }
        
        // Leave current room first
        if (currentRoomId != null && !currentRoomId.equals(roomId)) {
            leaveCurrentRoom();
        }
        
        // Enter via API
        boolean success = apiClient.enterRoom(roomId);
        
        if (success) {
            currentRoom = room;
            currentRoomId = roomId;
            
            // Update game state for the new room
            updateGameForRoom();
            
            // Notify listeners
            notifyRoomEntered(room);
            
            System.out.println("[ROOM CTRL] Entered room: " + room.getRoomName());
        }
        
        return success;
    }

    public boolean enterRoomWithPassword(String roomId, String username, String password) {
        Room room = roomCache.get(roomId);
        
        if (room == null) {
            System.err.println("[ROOM CTRL] Room not found: " + roomId);
            return false;
        }
        
        // Leave current room first
        if (currentRoomId != null && !currentRoomId.equals(roomId)) {
            leaveCurrentRoom();
        }
        
        // Enter via API with password
        boolean success = apiClient.enterRoomWithPassword(roomId, password);
        
        if (success) {
            currentRoom = room;
            currentRoomId = roomId;
            
            updateGameForRoom();
            notifyRoomEntered(room);
            
            System.out.println("[ROOM CTRL] Entered locked room: " + room.getRoomName());
        }
        
        return success;
    }

    // ═══════════════════════════════════════════════════════════
    // LEAVE ROOM
    // ═══════════════════════════════════════════════════════════

    public void leaveCurrentRoom() {
        if (currentRoomId != null) {
            apiClient.leaveRoom(currentRoomId);
            
            Room leftRoom = currentRoom;
            currentRoom = null;
            currentRoomId = null;
            
            if (leftRoom != null) {
                notifyRoomLeft(leftRoom);
            }
            
            System.out.println("[ROOM CTRL] Left room");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CREATE ROOM
    // ═══════════════════════════════════════════════════════════

    public Room createRoom(String roomName, String ownerUsername) {
        Room newRoom = apiClient.createRoom(roomName.trim());
        
        if (newRoom != null) {
            roomCache.put(newRoom.getRoomId(), newRoom);
            notifyRoomCreated(newRoom);
            System.out.println("[ROOM CTRL] Created room: " + newRoom.getRoomName());
        }
        
        return newRoom;
    }

    // ═══════════════════════════════════════════════════════════
    // DELETE ROOM
    // ═══════════════════════════════════════════════════════════

    public boolean deleteRoom(String roomId, String username) {
        Room room = roomCache.get(roomId);
        
        if (room == null) {
            return false;
        }
        
        if (!room.isOwner(username)) {
            System.err.println("[ROOM CTRL] Not owner, cannot delete");
            return false;
        }
        
        boolean success = apiClient.deleteRoom(roomId);
        
        if (success) {
            roomCache.remove(roomId);
            favoriteRoomIds.remove(roomId);
            notifyRoomDeleted(room);
            System.out.println("[ROOM CTRL] Deleted room: " + room.getRoomName());
        }
        
        return success;
    }

    // ═══════════════════════════════════════════════════════════
    // FAVORITES
    // ═══════════════════════════════════════════════════════════

    public void addFavorite(String roomId) {
        favoriteRoomIds.add(roomId);
        notifyRoomListChanged();
    }

    public void removeFavorite(String roomId) {
        favoriteRoomIds.remove(roomId);
        notifyRoomListChanged();
    }

    public boolean isFavorite(String roomId) {
        return favoriteRoomIds.contains(roomId);
    }

    // ═══════════════════════════════════════════════════════════
    // UPDATE PLAYER COUNT (called from WebSocket)
    // ═══════════════════════════════════════════════════════════

    public void updatePlayerCount(String roomId, int count) {
        Room room = roomCache.get(roomId);
        if (room != null) {
            room.setCurrentPlayerCount(count);
            notifyRoomListChanged();
            System.out.println("[ROOM CTRL] Updated player count: " + roomId + " = " + count);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // UPDATE GAME STATE FOR ROOM
    // ═══════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════
// UPDATE GAME STATE FOR ROOM
// ═══════════════════════════════════════════════════════════

private void updateGameForRoom() {
    if (currentRoom == null || gp == null) return;
    
    // Update tile map if available
    // TODO: Uncomment when tileManager supports this
    // if (currentRoom.getTileMap() != null && gp.tileM != null) {
    //     gp.tileM.loadRoomTiles(currentRoom.getTileMap());
    // }
    
    // Reset player position
    // TODO: Adjust based on your Player class field names
    // if (gp.player != null) {
    //     gp.player.x = 4 * 64;  // Adjust to your tile size and field names
    //     gp.player.y = 2 * 64;
    // }
    
    System.out.println("[ROOM CTRL] Game updated for room: " + currentRoom.getRoomName());
}

    // ═══════════════════════════════════════════════════════════
    // LISTENER MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    public void addListener(RoomChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(RoomChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyRoomEntered(Room room) {
        for (RoomChangeListener listener : listeners) {
            try {
                listener.onRoomEntered(room);
            } catch (Exception e) {
                System.err.println("[ROOM CTRL] Listener error: " + e.getMessage());
            }
        }
    }

    private void notifyRoomLeft(Room room) {
        for (RoomChangeListener listener : listeners) {
            try {
                listener.onRoomLeft(room);
            } catch (Exception e) {
                System.err.println("[ROOM CTRL] Listener error: " + e.getMessage());
            }
        }
    }

    private void notifyRoomCreated(Room room) {
        for (RoomChangeListener listener : listeners) {
            try {
                listener.onRoomCreated(room);
            } catch (Exception e) {
                System.err.println("[ROOM CTRL] Listener error: " + e.getMessage());
            }
        }
    }

    private void notifyRoomDeleted(Room room) {
        for (RoomChangeListener listener : listeners) {
            try {
                listener.onRoomDeleted(room);
            } catch (Exception e) {
                System.err.println("[ROOM CTRL] Listener error: " + e.getMessage());
            }
        }
    }

    public void notifyRoomListChanged() {
        for (RoomChangeListener listener : listeners) {
            try {
                listener.onRoomListChanged();
            } catch (Exception e) {
                System.err.println("[ROOM CTRL] Listener error: " + e.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SHUTDOWN
    // ═══════════════════════════════════════════════════════════

    public void shutdown() {
        leaveCurrentRoom();
        listeners.clear();
        roomCache.clear();
        System.out.println("[ROOM CTRL] Shutdown complete");
    }


    // ═══════════════════════════════════════════════════════════
// RETURN TO LOBBY
// ═══════════════════════════════════════════════════════════

    public void returnToLobby() {
        System.out.println("[ROOM CTRL] Returning to lobby...");
        
        // Leave current room
        leaveCurrentRoom();
        
        // Enter lobby
        String username = apiClient.getCurrentUsername();
        if (username != null) {
            enterRoom("lobby", username);
        } else {
            System.err.println("[ROOM CTRL] Cannot return to lobby - no username set");
        }
    }
}