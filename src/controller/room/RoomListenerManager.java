package controller.room;

import model.room.Room;
import java.util.ArrayList;
import java.util.List;

public class RoomListenerManager {
    
    public interface RoomChangeListener {
        void onRoomEntered(Room room);
        void onRoomLeft(Room room);
        void onRoomCreated(Room room);
        void onRoomDeleted(Room room);
        void onRoomListChanged();
    }
    
    private List<RoomChangeListener> listeners;
    
    public RoomListenerManager() {
        this.listeners = new ArrayList<>();
    }
    
    public void addListener(RoomChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(RoomChangeListener listener) {
        listeners.remove(listener);
    }
    
    public void clear() {
        listeners.clear();
    }
    
    public void notifyRoomEntered(Room room) {
        for (RoomChangeListener listener : listeners) {
            try {
                listener.onRoomEntered(room);
            } catch (Exception e) {
                System.err.println("[LISTENER] Error: " + e.getMessage());
            }
        }
    }
    
    public void notifyRoomLeft(Room room) {
        for (RoomChangeListener listener : listeners) {
            try {
                listener.onRoomLeft(room);
            } catch (Exception e) {
                System.err.println("[LISTENER] Error: " + e.getMessage());
            }
        }
    }
    
    public void notifyRoomCreated(Room room) {
        for (RoomChangeListener listener : listeners) {
            try {
                listener.onRoomCreated(room);
            } catch (Exception e) {
                System.err.println("[LISTENER] Error: " + e.getMessage());
            }
        }
    }
    
    public void notifyRoomDeleted(Room room) {
        for (RoomChangeListener listener : listeners) {
            try {
                listener.onRoomDeleted(room);
            } catch (Exception e) {
                System.err.println("[LISTENER] Error: " + e.getMessage());
            }
        }
    }
    
    public void notifyRoomListChanged() {
        for (RoomChangeListener listener : listeners) {
            try {
                listener.onRoomListChanged();
            } catch (Exception e) {
                System.err.println("[LISTENER] Error: " + e.getMessage());
            }
        }
    }
}