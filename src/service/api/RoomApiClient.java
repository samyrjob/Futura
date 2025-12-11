package service.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.room.Room;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RoomApiClient - HTTP client for Room API
 * 
 * Connects Java Swing client to Spring Boot backend
 * Replaces local file storage with centralized server
 */
public class RoomApiClient {

    private static final String BASE_URL = "http://localhost:9090/api/rooms";
    private static final int TIMEOUT = 10000; // 10 seconds
    
    private final Gson gson;
    private String currentUsername;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public RoomApiClient() {
        this.gson = new Gson();
        System.out.println("[ROOM API] Client initialized");
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
        System.out.println("[ROOM API] Username set to: " + username);
    }

    // ═══════════════════════════════════════════════════════════
    // GET ROOMS
    // ═══════════════════════════════════════════════════════════

    /**
     * Get all public rooms
     */
    public List<Room> getPublicRooms() {
        try {
            String response = sendGet(BASE_URL + "/public");
            return parseRoomList(response);
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to get public rooms: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get rooms owned by current user
     */
    public List<Room> getMyRooms() {
        if (currentUsername == null) {
            System.err.println("[ROOM API] Cannot get my rooms: username not set");
            return new ArrayList<>();
        }

        try {
            // Use search by owner endpoint or filter locally
            String response = sendGet(BASE_URL + "/public");
            List<Room> allRooms = parseRoomList(response);
            
            // Filter by owner
            List<Room> myRooms = new ArrayList<>();
            for (Room room : allRooms) {
                if (room.getOwnerUsername().equalsIgnoreCase(currentUsername)) {
                    myRooms.add(room);
                }
            }
            return myRooms;
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to get my rooms: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get room by ID
     */
    public Room getRoom(String roomId) {
        try {
            String response = sendGet(BASE_URL + "/" + roomId);
            return parseRoom(response);
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to get room: " + e.getMessage());
            return null;
        }
    }

    /**
     * Search rooms by name
     */
    public List<Room> searchRooms(String query) {
        try {
            String response = sendGet(BASE_URL + "/search?query=" + encodeUrl(query));
            return parseRoomList(response);
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to search rooms: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CREATE / DELETE ROOMS
    // ═══════════════════════════════════════════════════════════

    /**
     * Create a new room
     */
    public Room createRoom(String roomName) {
        if (currentUsername == null) {
            System.err.println("[ROOM API] Cannot create room: username not set");
            return null;
        }

        try {
            String json = gson.toJson(Map.of(
                "roomName", roomName,
                "ownerUsername", currentUsername
            ));
            
            String response = sendPost(BASE_URL + "/create", json);
            Room room = parseRoom(response);
            
            if (room != null) {
                System.out.println("[ROOM API] ✅ Created room: " + room.getRoomName());
            }
            return room;
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to create room: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete a room
     */
    public boolean deleteRoom(String roomId) {
        try {
            sendDelete(BASE_URL + "/" + roomId + "?username=" + encodeUrl(currentUsername));
            System.out.println("[ROOM API] ✅ Deleted room: " + roomId);
            return true;
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to delete room: " + e.getMessage());
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ENTER / LEAVE ROOMS
    // ═══════════════════════════════════════════════════════════

    /**
     * Enter a room
     */
    public boolean enterRoom(String roomId) {
        if (currentUsername == null) {
            System.err.println("[ROOM API] Cannot enter room: username not set");
            return false;
        }

        try {
            String json = gson.toJson(Map.of("username", currentUsername));
            sendPost(BASE_URL + "/" + roomId + "/enter", json);
            System.out.println("[ROOM API] ✅ Entered room: " + roomId);
            return true;
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to enter room: " + e.getMessage());
            return false;
        }
    }

    /**
     * Enter a locked room with password
     */
    public boolean enterRoomWithPassword(String roomId, String password) {
        if (currentUsername == null) {
            return false;
        }

        try {
            String json = gson.toJson(Map.of(
                "username", currentUsername,
                "password", password
            ));
            sendPost(BASE_URL + "/" + roomId + "/enter-password", json);
            System.out.println("[ROOM API] ✅ Entered locked room: " + roomId);
            return true;
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to enter locked room: " + e.getMessage());
            return false;
        }
    }

    /**
     * Leave a room
     */
    public void leaveRoom(String roomId) {
        if (currentUsername == null) return;

        try {
            String json = gson.toJson(Map.of("username", currentUsername));
            sendPost(BASE_URL + "/" + roomId + "/leave", json);
            System.out.println("[ROOM API] Left room: " + roomId);
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to leave room: " + e.getMessage());
        }
    }

    /**
     * Get users in a room
     */
    public List<String> getUsersInRoom(String roomId) {
        try {
            String response = sendGet(BASE_URL + "/" + roomId + "/users");
            Map<String, Object> result = gson.fromJson(response, 
                new TypeToken<Map<String, Object>>(){}.getType());
            
            @SuppressWarnings("unchecked")
            List<String> users = (List<String>) result.get("users");
            return users != null ? users : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to get users: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HTTP METHODS
    // ═══════════════════════════════════════════════════════════

    private String sendGet(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return readResponse(conn.getInputStream());
        } else {
            throw new IOException("HTTP " + responseCode + ": " + readResponse(conn.getErrorStream()));
        }
    }

    private String sendPost(String urlString, String jsonBody) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-Username", currentUsername); // Custom header for auth
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setDoOutput(true);

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
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("X-Username", currentUsername);
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new IOException("HTTP " + responseCode);
        }
    }

    private String readResponse(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private String encodeUrl(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return value;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PARSING
    // ═══════════════════════════════════════════════════════════

    private List<Room> parseRoomList(String json) {
        try {
            List<Map<String, Object>> rawList = gson.fromJson(json, 
                new TypeToken<List<Map<String, Object>>>(){}.getType());
            
            List<Room> rooms = new ArrayList<>();
            for (Map<String, Object> raw : rawList) {
                Room room = mapToRoom(raw);
                if (room != null) {
                    rooms.add(room);
                }
            }
            return rooms;
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to parse room list: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private Room parseRoom(String json) {
        try {
            Map<String, Object> raw = gson.fromJson(json, 
                new TypeToken<Map<String, Object>>(){}.getType());
            return mapToRoom(raw);
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to parse room: " + e.getMessage());
            return null;
        }
    }

    private Room mapToRoom(Map<String, Object> raw) {
        try {
            String roomId = (String) raw.get("roomId");
            String roomName = (String) raw.get("roomName");
            String ownerUsername = (String) raw.get("ownerUsername");

            Room room = new Room(roomId, roomName, ownerUsername);
            
            // Set room type
            String roomType = (String) raw.get("roomType");
            if (roomType != null) {
                room.setRoomType(Room.RoomType.valueOf(roomType));
            }

            // Set description
            String description = (String) raw.get("description");
            if (description != null) {
                room.setDescription(description);
            }

            // Set player counts
            if (raw.get("maxPlayers") != null) {
                room.setMaxPlayers(((Number) raw.get("maxPlayers")).intValue());
            }
            if (raw.get("currentPlayerCount") != null) {
                room.setCurrentPlayerCount(((Number) raw.get("currentPlayerCount")).intValue());
            }

            return room;
        } catch (Exception e) {
            System.err.println("[ROOM API] Failed to map room: " + e.getMessage());
            return null;
        }
    }
}