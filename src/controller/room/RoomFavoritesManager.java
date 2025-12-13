package controller.room;

import java.util.HashSet;
import java.util.Set;

public class RoomFavoritesManager {
    
    private Set<String> favoriteRoomIds;
    
    public RoomFavoritesManager() {
        this.favoriteRoomIds = new HashSet<>();
    }
    
    public void add(String roomId) {
        favoriteRoomIds.add(roomId);
    }
    
    public void remove(String roomId) {
        favoriteRoomIds.remove(roomId);
    }
    
    public boolean isFavorite(String roomId) {
        return favoriteRoomIds.contains(roomId);
    }
    
    public Set<String> getAll() {
        return new HashSet<>(favoriteRoomIds);
    }
    
    public void clear() {
        favoriteRoomIds.clear();
    }
}