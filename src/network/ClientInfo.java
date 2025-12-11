package network;

import java.io.PrintWriter;

/**
 * ClientInfo - Stores information about one connected client
 * NOW WITH ROOM TRACKING!
 */
public class ClientInfo {
    
    public PrintWriter out;
    public String address;
    public int port;
    public String playerName;
    public String gender;
    public int mapX;
    public int mapY;
    public String direction;
    public String currentRoomId;  // ✨ NEW - Track which room player is in
    
    public ClientInfo(PrintWriter out, String address, int port, String playerName, 
                     String gender, int mapX, int mapY, String direction, String roomId) {
        this.out = out;
        this.address = address;
        this.port = port;
        this.playerName = playerName;
        this.gender = gender;
        this.mapX = mapX;
        this.mapY = mapY;
        this.direction = direction;
        this.currentRoomId = roomId;  // ✨ NEW
    }
    
    /**
     * Check if this client matches the given address and port
     */
    public boolean matches(String address, int port) {
        return this.address.equals(address) && this.port == port;
    }
    
    /**
     * Send a message to this client
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    
    /**
     * Update player position
     */
    public void updatePosition(int mapX, int mapY, String direction) {
        this.mapX = mapX;
        this.mapY = mapY;
        this.direction = direction;
    }
    
    @Override
    public String toString() {
        return playerName + " (" + address + ":" + port + ") in room: " + currentRoomId;
    }
}