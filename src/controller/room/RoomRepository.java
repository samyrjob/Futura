package controller.room;

import model.room.Room;
import main.GameConstants;

import java.io.*;
import java.util.*;

/**
 * RoomRepository - Handles room persistence
 * 
 * Part of MVC Architecture:
 * - Responsible ONLY for saving/loading rooms to/from files
 * - No business logic here
 * 
 * Storage:
 * - rooms.dat: All rooms in the game
 * - rooms/favorites_{username}.dat: User's favorite rooms
 */
public class RoomRepository {
    
    private static final String ROOMS_FILE = "rooms.dat";
    private static final String ROOMS_DIR = "rooms/";
    
    // In-memory cache
    private Map<String, Room> roomCache;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public RoomRepository() {
        this.roomCache = new HashMap<>();
        
        // Ensure rooms directory exists
        new File(ROOMS_DIR).mkdirs();
    }
    
    // ═══════════════════════════════════════════════════════════
    // ROOM CRUD OPERATIONS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Save a room to the cache (and persist)
     */
    public void save(Room room) {
        roomCache.put(room.getRoomId(), room);
        saveAllRooms();
        System.out.println("[ROOM REPO] Saved room: " + room.getRoomName());
    }
    
    /**
     * Delete a room from cache and storage
     */
    public void delete(String roomId) {
        Room removed = roomCache.remove(roomId);
        if (removed != null) {
            saveAllRooms();
            System.out.println("[ROOM REPO] Deleted room: " + removed.getRoomName());
        }
    }
    
    /**
     * Get a room by ID
     */
    public Room findById(String roomId) {
        return roomCache.get(roomId);
    }
    
    /**
     * Get all rooms
     */
    public List<Room> findAll() {
        return new ArrayList<>(roomCache.values());
    }
    
    /**
     * Check if room exists
     */
    public boolean exists(String roomId) {
        return roomCache.containsKey(roomId);
    }
    
    // ═══════════════════════════════════════════════════════════
    // QUERY METHODS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Get all public rooms
     */
    public List<Room> findPublicRooms() {
        List<Room> publicRooms = new ArrayList<>();
        for (Room room : roomCache.values()) {
            if (room.getRoomType() == Room.RoomType.PUBLIC) {
                publicRooms.add(room);
            }
        }
        // Sort by last visited (most recent first)
        publicRooms.sort((a, b) -> Long.compare(b.getLastVisited(), a.getLastVisited()));
        return publicRooms;
    }
    
    /**
     * Get rooms owned by a specific user
     */
    public List<Room> findByOwner(String username) {
        List<Room> userRooms = new ArrayList<>();
        for (Room room : roomCache.values()) {
            if (room.getOwnerUsername().equalsIgnoreCase(username)) {
                userRooms.add(room);
            }
        }
        // Sort by creation date (newest first)
        userRooms.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        return userRooms;
    }
    
    /**
     * Search rooms by name
     */
    public List<Room> searchByName(String query) {
        List<Room> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (Room room : roomCache.values()) {
            if (room.getRoomName().toLowerCase().contains(lowerQuery)) {
                results.add(room);
            }
        }
        return results;
    }
    
    /**
     * Get room count
     */
    public int count() {
        return roomCache.size();
    }
    
    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE - SAVE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Save all rooms to file
     */
    public void saveAllRooms() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ROOMS_FILE))) {
            for (Room room : roomCache.values()) {
                writer.write(room.serialize());
                writer.newLine();
            }
            System.out.println("[ROOM REPO] Saved " + roomCache.size() + " rooms to file");
        } catch (IOException e) {
            System.err.println("[ROOM REPO] Failed to save rooms: " + e.getMessage());
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE - LOAD
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Load all rooms from file
     */
    public void loadAllRooms() {
        File file = new File(ROOMS_FILE);
        
        if (!file.exists()) {
            System.out.println("[ROOM REPO] No rooms file found, starting fresh");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                try {
                    Room room = Room.deserialize(line);
                    roomCache.put(room.getRoomId(), room);
                    count++;
                    System.out.println("[ROOM REPO] Loaded: " + room.getRoomName() + " [" + room.getRoomId() + "]");
                } catch (Exception e) {
                    System.err.println("[ROOM REPO] Failed to parse room: " + e.getMessage());
                }
            }
            
            System.out.println("[ROOM REPO] Loaded " + count + " rooms from file");
            
            // Verify lobby exists
            if (roomCache.containsKey(GameConstants.LOBBY_ROOM_ID)) {
                System.out.println("[ROOM REPO] ✓ Lobby room found");
            } else {
                System.out.println("[ROOM REPO] ✗ WARNING: Lobby room not found!");
            }
            
        } catch (IOException e) {
            System.err.println("[ROOM REPO] Failed to load rooms: " + e.getMessage());
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // FAVORITES
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Get path to user's favorites file
     */
    private String getFavoritesPath(String username) {
        return ROOMS_DIR + "favorites_" + username.toLowerCase() + ".dat";
    }
    
    /**
     * Save user's favorite rooms
     */
    public void saveFavorites(String username, List<String> roomIds) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFavoritesPath(username)))) {
            for (String roomId : roomIds) {
                writer.write(roomId);
                writer.newLine();
            }
            System.out.println("[ROOM REPO] Saved " + roomIds.size() + " favorites for " + username);
        } catch (IOException e) {
            System.err.println("[ROOM REPO] Failed to save favorites: " + e.getMessage());
        }
    }
    
    /**
     * Load user's favorite rooms
     */
    public List<String> loadFavorites(String username) {
        List<String> favorites = new ArrayList<>();
        File file = new File(getFavoritesPath(username));
        
        if (!file.exists()) {
            return favorites;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    favorites.add(line.trim());
                }
            }
            System.out.println("[ROOM REPO] Loaded " + favorites.size() + " favorites for " + username);
        } catch (IOException e) {
            System.err.println("[ROOM REPO] Failed to load favorites: " + e.getMessage());
        }
        
        return favorites;
    }
    
    /**
     * Get favorite rooms as Room objects
     */
    public List<Room> getFavoriteRooms(String username) {
        List<Room> favoriteRooms = new ArrayList<>();
        List<String> favoriteIds = loadFavorites(username);
        
        for (String roomId : favoriteIds) {
            Room room = roomCache.get(roomId);
            if (room != null) {
                favoriteRooms.add(room);
            }
        }
        
        return favoriteRooms;
    }
    
    // ═══════════════════════════════════════════════════════════
    // CACHE MANAGEMENT
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Clear the cache (for testing or reset)
     */
    public void clearCache() {
        roomCache.clear();
    }
    
    /**
     * Reload rooms from file
     */
    public void reload() {
        clearCache();
        loadAllRooms();
    }
}