package controller.room;

import model.room.Room;
import service.api.RoomApiClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RoomCache {
    
    private Map<String, Room> cache;
    private RoomApiClient apiClient;
    
    public RoomCache(RoomApiClient apiClient) {
        this.cache = new ConcurrentHashMap<>();
        this.apiClient = apiClient;
    }
    
    public void refresh() {
        System.out.println("[ROOM CACHE] Refreshing from server...");
        try {
            List<Room> rooms = apiClient.getPublicRooms();
            cache.clear();
            for (Room room : rooms) {
                cache.put(room.getRoomId(), room);
            }
            System.out.println("[ROOM CACHE] Loaded " + cache.size() + " rooms");
        } catch (Exception e) {
            System.err.println("[ROOM CACHE] Refresh failed: " + e.getMessage());
        }
    }
    
    public Room get(String roomId) {
        Room room = cache.get(roomId);
        if (room == null) {
            room = apiClient.getRoom(roomId);
            if (room != null) {
                cache.put(roomId, room);
            }
        }
        return room;
    }
    
    public void put(Room room) {
        cache.put(room.getRoomId(), room);
    }
    
    public void remove(String roomId) {
        cache.remove(roomId);
    }
    
    public void clear() {
        cache.clear();
    }
    
    public List<Room> getAll() {
        return new ArrayList<>(cache.values());
    }
    
    public List<Room> getPublicRooms() {
        return cache.values().stream()
            .filter(r -> r.getRoomType() == Room.RoomType.PUBLIC)
            .sorted(Comparator.comparing(Room::getRoomName))
            .collect(Collectors.toList());
    }
    
    public List<Room> getByOwner(String username) {
        return cache.values().stream()
            .filter(r -> r.getOwnerUsername().equalsIgnoreCase(username))
            .sorted(Comparator.comparing(Room::getRoomName))
            .collect(Collectors.toList());
    }
    
    public List<Room> getByIds(Set<String> roomIds) {
        return cache.values().stream()
            .filter(r -> roomIds.contains(r.getRoomId()))
            .sorted(Comparator.comparing(Room::getRoomName))
            .collect(Collectors.toList());
    }
}