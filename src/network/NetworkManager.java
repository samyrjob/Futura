package network;

import java.io.*;
import java.net.*;

import main.GamePanel;

public class NetworkManager {
    
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ServerMessageWatcher watcher;
    private GamePanel gamePanel;
    private boolean connected = false;
    
    public NetworkManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }
    
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            watcher = new ServerMessageWatcher(this, in, gamePanel);
            watcher.start();
            
            connected = true;
            System.out.println("Connected to server successfully");
            return true;
            
        } catch (IOException e) {
            System.err.println("Could not connect to server: " + e.getMessage());
            connected = false;
            return false;
        }
    }
    
    public void sendJoinMessage(String username, String gender, int mapX, int mapY, String direction) {
        if (connected && out != null) {
            String message = "join " + username + " " + gender + " " + mapX + " " + mapY + " " + direction;
            out.println(message);
            System.out.println("Sent join message: " + message);
        }
    }
    
    public void sendMoveMessage(int mapX, int mapY, String direction, boolean inMovement) {
        if (connected && out != null) {
            String message = "move " + mapX + " " + mapY + " " + direction + " " + inMovement;
            out.println(message);
        }
    }
    
    public void sendChatMessage(String text) {
        if (connected && out != null) {
            String message = "chat " + text;
            out.println(message);
        }
    }
    
    public void sendDetailsRequest(String targetAddr, int targetPort) {
        if (connected && out != null) {
            String message = "wantDetails " + targetAddr + " " + targetPort;
            out.println(message);
        }
    }
    
    public void sendDetailsResponse(String targetAddr, int targetPort, String username, 
                                   String gender, int mapX, int mapY, String direction) {
        if (connected && out != null) {
            String message = "detailsFor " + targetAddr + " " + targetPort + " " + 
                           username + " " + gender + " " + mapX + " " + mapY + " " + direction;
            out.println(message);
        }
    }
    
    public void sendByeMessage() {
        if (connected && out != null) {
            out.println("bye");
        }
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (watcher != null) {
                watcher.interrupt();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
}