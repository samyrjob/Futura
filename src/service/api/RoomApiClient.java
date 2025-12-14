package service.api;

import model.room.Room;
import java.util.List;
import java.util.Map;

/**
 * HTTP Client for Room API
 * Communicates with Spring Boot backend
 * 
 * Username is stored once via setCurrentUsername()
 * No need to pass username in every method!
 */
public class RoomApiClient {
    
    private final HttpClient http;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public RoomApiClient() {
        this.http = new HttpClient("http://localhost:9090/api/rooms");
        System.out.println("[ROOM API] Client initialized");
    }
    
    // ═══════════════════════════════════════════════════════════
    // USERNAME - Set once, use everywhere
    // ═══════════════════════════════════════════════════════════
    
    public void setCurrentUsername(String username) {
        http.setUsername(username);
        System.out.println("[ROOM API] Username set to: " + username);
    }
    
    public String getCurrentUsername() {
        return http.getUsername();
    }
    
    // ═══════════════════════════════════════════════════════════
    // GET ROOMS
    // ═══════════════════════════════════════════════════════════
    
    public List<Room> getPublicRooms() {
        return http.getList("/public", Room[].class);
    }
    
    public List<Room> getMyRooms() {
        return http.getList("/my?username=" + getCurrentUsername(), Room[].class);
    }
    
    public Room getRoom(String roomId) {
        return http.get("/" + roomId, Room.class);
    }
    
    // ═══════════════════════════════════════════════════════════
    // CREATE ROOM
    // ═══════════════════════════════════════════════════════════
    
    public Room createRoom(String roomName) {
        return http.post("/create", Map.of("roomName", roomName), Room.class);
    }
    
    // ═══════════════════════════════════════════════════════════
    // ENTER / LEAVE ROOM
    // ═══════════════════════════════════════════════════════════
    
    public boolean enterRoom(String roomId) {
        return http.postSuccess("/" + roomId + "/enter", 
            Map.of("username",  getCurrentUsername()));
    }
    
    public boolean enterRoomWithPassword(String roomId, String password) {
        return http.postSuccess("/" + roomId + "/enter-password", 
            Map.of("username",  getCurrentUsername(), "password", password));
    }
    
    public void leaveRoom(String roomId) {
        http.post("/" + roomId + "/leave", 
            Map.of("username",  getCurrentUsername()), Object.class);
    }
    
    // ═══════════════════════════════════════════════════════════
    // DELETE ROOM
    // ═══════════════════════════════════════════════════════════
    
    public boolean deleteRoom(String roomId) {
        return http.delete("/" + roomId);
    }
}