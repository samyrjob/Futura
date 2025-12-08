package admin;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

/**
 * Handles network communication with AdminServer
 */
public class AdminNetworkClient {
    
    private String host;
    private int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected = false;
    private boolean authenticated = false;
    
    private Thread listenerThread;
    private Consumer<String> messageHandler;
    
    public AdminNetworkClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    // ═══════════════════════════════════════════════════════════
    // CONNECTION
    // ═══════════════════════════════════════════════════════════
    
    public boolean connect() {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            
            // Read initial messages
            String ready = in.readLine();  // ADMIN_SERVER_READY
            String authReq = in.readLine(); // AUTH_REQUIRED
            
            System.out.println("[ADMIN CLIENT] Connected to server");
            System.out.println("[ADMIN CLIENT] " + ready);
            System.out.println("[ADMIN CLIENT] " + authReq);
            
            return true;
            
        } catch (IOException e) {
            System.err.println("[ADMIN CLIENT] Connection failed: " + e.getMessage());
            connected = false;
            return false;
        }
    }
    
    public boolean authenticate(String secretKey) {
        if (!connected) return false;
        
        try {
            out.println("AUTH " + secretKey);
            String response = in.readLine();
            
            if ("AUTH_SUCCESS".equals(response)) {
                authenticated = true;
                System.out.println("[ADMIN CLIENT] Authentication successful");
                startListener();
                return true;
            } else {
                System.out.println("[ADMIN CLIENT] Authentication failed");
                return false;
            }
            
        } catch (IOException e) {
            System.err.println("[ADMIN CLIENT] Auth error: " + e.getMessage());
            return false;
        }
    }
    
    public void disconnect() {
        connected = false;
        authenticated = false;
        
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("[ADMIN CLIENT] Disconnected");
    }
    
    // ═══════════════════════════════════════════════════════════
    // MESSAGE HANDLING
    // ═══════════════════════════════════════════════════════════
    
    public void setMessageHandler(Consumer<String> handler) {
        this.messageHandler = handler;
    }
    
    private void startListener() {
        listenerThread = new Thread(() -> {
            try {
                String line;
                while (connected && (line = in.readLine()) != null) {
                    System.out.println("[ADMIN CLIENT] Received: " + line);
                    if (messageHandler != null) {
                        final String msg = line;
                        javax.swing.SwingUtilities.invokeLater(() -> messageHandler.accept(msg));
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    System.err.println("[ADMIN CLIENT] Connection lost");
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    // ═══════════════════════════════════════════════════════════
    // COMMANDS
    // ═══════════════════════════════════════════════════════════
    
    public void sendCommand(String command) {
        if (connected && authenticated && out != null) {
            out.println(command);
            System.out.println("[ADMIN CLIENT] Sent: " + command);
        }
    }
    
    public void listPlayers() {
        sendCommand("LIST_PLAYERS");
    }
    
    public void listRooms() {
        sendCommand("LIST_ROOMS");
    }
    
    public void getRoomInfo(String roomId) {
        sendCommand("ROOM_INFO " + roomId);
    }
    
    public void kickPlayer(String username) {
        sendCommand("KICK " + username);
    }
    
    public void movePlayer(String username, String roomId) {
        sendCommand("MOVE_PLAYER " + username + " " + roomId);
    }
    
    public void clearRoom(String roomId) {
        sendCommand("CLEAR_ROOM " + roomId);
    }
    
    public void broadcast(String message) {
        sendCommand("BROADCAST " + message);
    }
    
    public void ping() {
        sendCommand("PING");
    }
    
    // ═══════════════════════════════════════════════════════════
    // STATUS
    // ═══════════════════════════════════════════════════════════
    
    public boolean isConnected() {
        return connected;
    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }
}