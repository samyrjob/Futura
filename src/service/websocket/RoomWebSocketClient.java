package service.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import controller.room.RoomController;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ClientEndpoint;

import jakarta.websocket.*;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket client for real-time room updates
 * Connects to Spring Boot STOMP endpoint
 */
@ClientEndpoint
public class RoomWebSocketClient {

    private static final String WS_URL = "ws://localhost:9090/ws/websocket";
    
    private Session session;
    private RoomController roomController;
    private final Gson gson = new Gson();
    private volatile boolean connected = false;
    private volatile boolean shouldReconnect = true;
    private ScheduledExecutorService reconnectExecutor;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public RoomWebSocketClient(RoomController roomController) {
        this.roomController = roomController;
        this.reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    // ═══════════════════════════════════════════════════════════
    // CONNECTION
    // ═══════════════════════════════════════════════════════════

    public void connect() {
        if (connected) {
            System.out.println("[WS CLIENT] Already connected");
            return;
        }

        try {
            System.out.println("[WS CLIENT] Connecting to " + WS_URL);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(WS_URL));
        } catch (Exception e) {
            System.err.println("[WS CLIENT] Connection failed: " + e.getMessage());
            scheduleReconnect();
        }
    }

    public void disconnect() {
        shouldReconnect = false;
        
        if (reconnectExecutor != null) {
            reconnectExecutor.shutdownNow();
        }
        
        if (session != null && session.isOpen()) {
            try {
                session.close();
                System.out.println("[WS CLIENT] Disconnected");
            } catch (Exception e) {
                System.err.println("[WS CLIENT] Error closing: " + e.getMessage());
            }
        }
        connected = false;
    }

    private void scheduleReconnect() {
        if (!shouldReconnect) return;
        
        System.out.println("[WS CLIENT] Scheduling reconnect in 5 seconds...");
        reconnectExecutor.schedule(this::connect, 5, TimeUnit.SECONDS);
    }

    // ═══════════════════════════════════════════════════════════
    // WEBSOCKET CALLBACKS
    // ═══════════════════════════════════════════════════════════

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        this.connected = true;
        System.out.println("[WS CLIENT] ✅ Connected to server!");
        
        // Subscribe to room events (STOMP-style)
        subscribeToRooms();
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("[WS CLIENT] 📩 Received: " + message);
        
        try {
            // Parse STOMP-style message or direct JSON
            if (message.contains("\"type\"")) {
                handleRoomEvent(message);
            }
        } catch (Exception e) {
            System.err.println("[WS CLIENT] Error processing message: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.connected = false;
        System.out.println("[WS CLIENT] Connection closed: " + reason.getReasonPhrase());
        scheduleReconnect();
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("[WS CLIENT] Error: " + error.getMessage());
        this.connected = false;
    }

    // ═══════════════════════════════════════════════════════════
    // STOMP MESSAGING
    // ═══════════════════════════════════════════════════════════

    private void subscribeToRooms() {
        // Send STOMP SUBSCRIBE frame
        String subscribeFrame = "SUBSCRIBE\n" +
                "id:sub-0\n" +
                "destination:/topic/rooms\n" +
                "\n\0";
        
        sendMessage(subscribeFrame);
        System.out.println("[WS CLIENT] Subscribed to /topic/rooms");
    }

    private void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                System.err.println("[WS CLIENT] Send error: " + e.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EVENT HANDLING
    // ═══════════════════════════════════════════════════════════

    private void handleRoomEvent(String json) {
        try {
            JsonObject event = JsonParser.parseString(json).getAsJsonObject();
            String type = event.get("type").getAsString();
            
            System.out.println("[WS CLIENT] 🔔 Event type: " + type);
            
            switch (type) {
                case "ROOM_CREATED":
                    System.out.println("[WS CLIENT] 🏠 New room created!");
                    roomController.refreshRoomCache();
                    break;
                    
                case "ROOM_DELETED":
                    String deletedRoomId = event.get("roomId").getAsString();
                    System.out.println("[WS CLIENT] 🗑️ Room deleted: " + deletedRoomId);
                    roomController.refreshRoomCache();
                    break;
                    
                case "ROOM_UPDATED":
                    System.out.println("[WS CLIENT] 🔄 Room updated!");
                    roomController.refreshRoomCache();
                    break;
                    
                case "USER_JOINED":
                    String joinedRoom = event.get("roomId").getAsString();
                    int joinedCount = event.get("playerCount").getAsInt();
                    System.out.println("[WS CLIENT] 👤 User joined room " + joinedRoom + " (now " + joinedCount + " players)");
                    roomController.updatePlayerCount(joinedRoom, joinedCount);
                    break;
                    
                case "USER_LEFT":
                    String leftRoom = event.get("roomId").getAsString();
                    int leftCount = event.get("playerCount").getAsInt();
                    System.out.println("[WS CLIENT] 👤 User left room " + leftRoom + " (now " + leftCount + " players)");
                    roomController.updatePlayerCount(leftRoom, leftCount);
                    break;
                    
                default:
                    System.out.println("[WS CLIENT] Unknown event type: " + type);
            }
        } catch (Exception e) {
            System.err.println("[WS CLIENT] Failed to handle event: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // STATUS
    // ═══════════════════════════════════════════════════════════

    public boolean isConnected() {
        return connected && session != null && session.isOpen();
    }
}