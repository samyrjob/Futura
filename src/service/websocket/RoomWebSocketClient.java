package service.websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import controller.room.RoomController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.*;

/**
 * WebSocket client for real-time room updates
 * Connects to Spring Boot STOMP endpoint
 */
public class RoomWebSocketClient implements WebSocket.Listener {

    private static final String WS_URL = "ws://localhost:9090/ws/websocket";
    
    private WebSocket webSocket;
    private RoomController roomController;
    private volatile boolean connected = false;
    private volatile boolean shouldReconnect = true;
    private ScheduledExecutorService reconnectExecutor;
    private StringBuilder messageBuffer = new StringBuilder();

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public RoomWebSocketClient(RoomController roomController) {
        this.roomController = roomController;
        this.reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
        System.out.println("[WS CLIENT] Created");
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
            
            HttpClient client = HttpClient.newHttpClient();
            CompletableFuture<WebSocket> wsFuture = client.newWebSocketBuilder()
                .buildAsync(URI.create(WS_URL), this);
            
            webSocket = wsFuture.get(10, TimeUnit.SECONDS);
            connected = true;
            
            System.out.println("[WS CLIENT] ✅ Connected!");
            
            // Subscribe to room events (STOMP protocol)
            subscribeToRooms();
            
        } catch (Exception e) {
            System.err.println("[WS CLIENT] Connection failed: " + e.getMessage());
            connected = false;
            scheduleReconnect();
        }
    }

    public void disconnect() {
        shouldReconnect = false;
        
        if (reconnectExecutor != null) {
            reconnectExecutor.shutdownNow();
        }
        
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client closing");
            System.out.println("[WS CLIENT] Disconnected");
        }
        connected = false;
    }

    private void scheduleReconnect() {
        if (!shouldReconnect) return;
        
        System.out.println("[WS CLIENT] Scheduling reconnect in 5 seconds...");
        reconnectExecutor.schedule(this::connect, 5, TimeUnit.SECONDS);
    }

    // ═══════════════════════════════════════════════════════════
    // STOMP SUBSCRIPTION
    // ═══════════════════════════════════════════════════════════

    private void subscribeToRooms() {
        // Send STOMP CONNECT frame
        String connectFrame = "CONNECT\n" +
                "accept-version:1.2\n" +
                "host:localhost\n" +
                "\n\0";
        
        webSocket.sendText(connectFrame, true);
        
        // Wait a bit then subscribe
        reconnectExecutor.schedule(() -> {
            String subscribeFrame = "SUBSCRIBE\n" +
                    "id:sub-rooms\n" +
                    "destination:/topic/rooms\n" +
                    "\n\0";
            
            webSocket.sendText(subscribeFrame, true);
            System.out.println("[WS CLIENT] Subscribed to /topic/rooms");
        }, 500, TimeUnit.MILLISECONDS);
    }

    // ═══════════════════════════════════════════════════════════
    // WEBSOCKET LISTENER CALLBACKS
    // ═══════════════════════════════════════════════════════════

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("[WS CLIENT] onOpen");
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        messageBuffer.append(data);
        
        if (last) {
            String message = messageBuffer.toString();
            messageBuffer = new StringBuilder();
            
            // Process the message
            processMessage(message);
        }
        
        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("[WS CLIENT] Connection closed: " + reason);
        connected = false;
        scheduleReconnect();
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("[WS CLIENT] Error: " + error.getMessage());
        connected = false;
    }

    // ═══════════════════════════════════════════════════════════
    // MESSAGE PROCESSING
    // ═══════════════════════════════════════════════════════════

    private void processMessage(String message) {
        // STOMP messages have headers and body separated by double newline
        // The body is JSON after the headers
        
        try {
            // Find the JSON part (after empty line before null terminator)
            int bodyStart = message.indexOf("\n\n");
            if (bodyStart == -1) {
                return;
            }
            
            String body = message.substring(bodyStart + 2);
            // Remove STOMP null terminator if present
            body = body.replace("\0", "").trim();
            
            if (body.isEmpty() || !body.startsWith("{")) {
                return;
            }
            
            System.out.println("[WS CLIENT] 📩 Received: " + body);
            handleRoomEvent(body);
            
        } catch (Exception e) {
            // Not a JSON message, ignore (could be CONNECTED frame, etc.)
        }
    }

    private void handleRoomEvent(String json) {
        try {
            JsonObject event = JsonParser.parseString(json).getAsJsonObject();
            
            if (!event.has("type")) {
                return;
            }
            
            String type = event.get("type").getAsString();
            System.out.println("[WS CLIENT] 🔔 Event: " + type);
            
            switch (type) {
                case "ROOM_CREATED":
                    System.out.println("[WS CLIENT] 🏠 New room created!");
                    // Refresh the room list to show new room
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        roomController.refreshRoomCache();
                    });
                    break;
                    
                case "ROOM_DELETED":
                    String deletedRoomId = event.get("roomId").getAsString();
                    System.out.println("[WS CLIENT] 🗑️ Room deleted: " + deletedRoomId);
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        roomController.refreshRoomCache();
                    });
                    break;
                    
                case "ROOM_UPDATED":
                    System.out.println("[WS CLIENT] 🔄 Room updated!");
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        roomController.refreshRoomCache();
                    });
                    break;
                    
                case "USER_JOINED":
                    String joinedRoom = event.get("roomId").getAsString();
                    int joinedCount = event.get("playerCount").getAsInt();
                    String joinedUser = event.has("username") ? event.get("username").getAsString() : "unknown";
                    System.out.println("[WS CLIENT] 👤 " + joinedUser + " joined " + joinedRoom + " (players: " + joinedCount + ")");
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        roomController.updatePlayerCount(joinedRoom, joinedCount);
                    });
                    break;
                    
                case "USER_LEFT":
                    String leftRoom = event.get("roomId").getAsString();
                    int leftCount = event.get("playerCount").getAsInt();
                    String leftUser = event.has("username") ? event.get("username").getAsString() : "unknown";
                    System.out.println("[WS CLIENT] 👤 " + leftUser + " left " + leftRoom + " (players: " + leftCount + ")");
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        roomController.updatePlayerCount(leftRoom, leftCount);
                    });
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
        return connected;
    }
}