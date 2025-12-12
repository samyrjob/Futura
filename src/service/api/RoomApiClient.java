package service.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.room.Room;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP Client for Room API
 * Communicates with Spring Boot backend
 */
public class RoomApiClient {

    private static final String BASE_URL = "http://localhost:9090/api/rooms";
    private static final int TIMEOUT = 10000;
    
    private final Gson gson;
    private String currentUsername;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public RoomApiClient() {
        this.gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create();
        System.out.println("[ROOM API] Client initialized");
    }

    // ═══════════════════════════════════════════════════════════
    // USERNAME
    // ═══════════════════════════════════════════════════════════

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
        System.out.println("[ROOM API] Username set to: " + username);
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    // ═══════════════════════════════════════════════════════════
    // GET ROOMS
    // ═══════════════════════════════════════════════════════════

    public List<Room> getPublicRooms() {
        try {
            String response = sendGet(BASE_URL + "/public");
            return parseRoomList(response);
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to get public rooms: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Room> getMyRooms() {
        if (currentUsername == null) {
            System.err.println("[ROOM API] No username set for getMyRooms");
            return new ArrayList<>();
        }
        
        try {
            String response = sendGet(BASE_URL + "/my?username=" + currentUsername);
            return parseRoomList(response);
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to get my rooms: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Room getRoom(String roomId) {
        try {
            String response = sendGet(BASE_URL + "/" + roomId);
            return parseRoom(response);
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to get room " + roomId + ": " + e.getMessage());
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CREATE ROOM
    // ═══════════════════════════════════════════════════════════

    public Room createRoom(String roomName) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("roomName", roomName);
            
            String response = sendPost(BASE_URL + "/create", body.toString());
            Room room = parseRoom(response);
            System.out.println("[ROOM API] Created room: " + room.getRoomName());
            return room;
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to create room: " + e.getMessage());
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ENTER / LEAVE ROOM
    // ═══════════════════════════════════════════════════════════

    public boolean enterRoom(String roomId) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("username", currentUsername);
            
            String response = sendPost(BASE_URL + "/" + roomId + "/enter", body.toString());
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            
            boolean success = json.has("success") && json.get("success").getAsBoolean();
            if (success) {
                System.out.println("[ROOM API] Entered room: " + roomId);
            } else {
                String message = json.has("message") ? json.get("message").getAsString() : "Unknown error";
                System.err.println("[ROOM API] Failed to enter room: " + message);
            }
            return success;
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to enter room " + roomId + ": " + e.getMessage());
            return false;
        }
    }

    public boolean enterRoomWithPassword(String roomId, String password) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("username", currentUsername);
            body.addProperty("password", password);
            
            String response = sendPost(BASE_URL + "/" + roomId + "/enter-password", body.toString());
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            
            boolean success = json.has("success") && json.get("success").getAsBoolean();
            if (success) {
                System.out.println("[ROOM API] Entered locked room: " + roomId);
            }
            return success;
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to enter locked room: " + e.getMessage());
            return false;
        }
    }

    public void leaveRoom(String roomId) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("username", currentUsername);
            
            sendPost(BASE_URL + "/" + roomId + "/leave", body.toString());
            System.out.println("[ROOM API] Left room: " + roomId);
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to leave room: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DELETE ROOM
    // ═══════════════════════════════════════════════════════════

    public boolean deleteRoom(String roomId) {
        try {
            sendDelete(BASE_URL + "/" + roomId);
            System.out.println("[ROOM API] Deleted room: " + roomId);
            return true;
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to delete room: " + e.getMessage());
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HTTP METHODS
    // ═══════════════════════════════════════════════════════════

    private String sendGet(String urlString) throws IOException {
        System.out.println("[ROOM API] GET " + urlString);
        
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("X-Username", currentUsername != null ? currentUsername : "");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return readResponse(conn.getInputStream());
        } else {
            String error = readResponse(conn.getErrorStream());
            throw new IOException("HTTP " + responseCode + ": " + error);
        }
    }

    private String sendPost(String urlString, String jsonBody) throws IOException {
        System.out.println("[ROOM API] POST " + urlString);
        
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("X-Username", currentUsername != null ? currentUsername : "");
        conn.setDoOutput(true);
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            return readResponse(conn.getInputStream());
        } else {
            String error = readResponse(conn.getErrorStream());
            throw new IOException("HTTP " + responseCode + ": " + error);
        }
    }

    private void sendDelete(String urlString) throws IOException {
        System.out.println("[ROOM API] DELETE " + urlString);
        
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("X-Username", currentUsername != null ? currentUsername : "");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
            String error = readResponse(conn.getErrorStream());
            throw new IOException("HTTP " + responseCode + ": " + error);
        }
    }

    private String readResponse(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    // ═══════════════════════════════════════════════════════════
    // JSON PARSING
    // ═══════════════════════════════════════════════════════════

    private List<Room> parseRoomList(String json) {
        List<Room> rooms = new ArrayList<>();
        
        try {
            JsonArray array = JsonParser.parseString(json).getAsJsonArray();
            
            for (int i = 0; i < array.size(); i++) {
                Room room = parseRoomFromJson(array.get(i).getAsJsonObject());
                if (room != null) {
                    rooms.add(room);
                }
            }
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to parse room list: " + e.getMessage());
        }
        
        return rooms;
    }

    private Room parseRoom(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            return parseRoomFromJson(obj);
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to parse room: " + e.getMessage());
            return null;
        }
    }

    private Room parseRoomFromJson(JsonObject obj) {
        try {
            String roomId = obj.get("roomId").getAsString();
            String roomName = obj.get("roomName").getAsString();
            String ownerUsername = obj.get("ownerUsername").getAsString();
            
            Room room = new Room(roomName, ownerUsername);
            
            // Use reflection or setter to set roomId (since constructor generates new one)
            // Or modify Room class to accept roomId in constructor
            setRoomId(room, roomId);
            
            // Set room type
            if (obj.has("roomType")) {
                String typeStr = obj.get("roomType").getAsString();
                room.setRoomType(Room.RoomType.valueOf(typeStr));
            }
            
            // Set description
            if (obj.has("description") && !obj.get("description").isJsonNull()) {
                room.setDescription(obj.get("description").getAsString());
            }
            
            // Set player count
            if (obj.has("currentPlayerCount")) {
                room.setCurrentPlayerCount(obj.get("currentPlayerCount").getAsInt());
            }
            
            // Set max players
            if (obj.has("maxPlayers")) {
                room.setMaxPlayers(obj.get("maxPlayers").getAsInt());
            }
            
            // Set dimensions
            if (obj.has("width")) {
                room.setWidth(obj.get("width").getAsInt());
            }
            if (obj.has("height")) {
                room.setHeight(obj.get("height").getAsInt());
            }
            
            return room;
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to parse room JSON: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper to set roomId (Room class generates its own, but we need the server's ID)
     */
    private void setRoomId(Room room, String roomId) {
        try {
            java.lang.reflect.Field field = Room.class.getDeclaredField("roomId");
            field.setAccessible(true);
            field.set(room, roomId);
        } catch (Exception e) {
            System.err.println("[ROOM API] Could not set roomId: " + e.getMessage());
        }
    }
}