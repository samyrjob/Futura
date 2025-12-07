package network;

import java.util.ArrayList;
import java.util.List;

/**
 * GameServerGroup - Manages all connected clients
 * NOW WITH ROOM-BASED BROADCASTING!
 */
public class GameServerGroup {
    
    private List<ClientInfo> clients;
    
    public GameServerGroup() {
        this.clients = new ArrayList<>();
    }
    
    // ═══════════════════════════════════════════════════════════
    // CLIENT MANAGEMENT
    // ═══════════════════════════════════════════════════════════
    
    public synchronized void addClient(ClientInfo client) {
        clients.add(client);
        System.out.println("Client added: " + client.playerName + 
                          " (Total: " + clients.size() + ")");
    }
    
    public synchronized void removeClient(String address, int port) {
        clients.removeIf(client -> client.matches(address, port));
        System.out.println("Client removed (Total: " + clients.size() + ")");
    }
    
    public synchronized ClientInfo getClient(String address, int port) {
        for (ClientInfo client : clients) {
            if (client.matches(address, port)) {
                return client;
            }
        }
        return null;
    }
    
    // ═══════════════════════════════════════════════════════════
    // BROADCASTING - ✨ ROOM-AWARE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Broadcast message to ALL clients except sender (old method - avoid using)
     */
    @Deprecated
    public synchronized void broadcast(String senderAddr, int senderPort, String message) {
        for (ClientInfo client : clients) {
            if (!client.matches(senderAddr, senderPort)) {
                client.sendMessage(message);
            }
        }
    }
    
    /**
     * ✨ NEW - Broadcast only to players in the same room
     */
    public synchronized void broadcastToRoom(String roomId, String senderAddr, 
                                            int senderPort, String message) {
        int sentCount = 0;
        
        for (ClientInfo client : clients) {
            // Only send if:
            // 1. Not the sender
            // 2. In the same room
            if (!client.matches(senderAddr, senderPort) && 
                client.currentRoomId.equals(roomId)) {
                client.sendMessage(message);
                sentCount++;
            }
        }
        
        System.out.println("Broadcast to room '" + roomId + "': " + message + 
                          " (sent to " + sentCount + " players)");
    }
    
    /**
     * Send message to specific client
     */
    public synchronized void sendTo(String address, int port, String message) {
        for (ClientInfo client : clients) {
            if (client.matches(address, port)) {
                client.sendMessage(message);
                return;
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // ROOM QUERIES
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Get all clients in a specific room
     */
    public synchronized List<ClientInfo> getClientsInRoom(String roomId) {
        List<ClientInfo> roomClients = new ArrayList<>();
        for (ClientInfo client : clients) {
            if (client.currentRoomId.equals(roomId)) {
                roomClients.add(client);
            }
        }
        return roomClients;
    }
    
    /**
     * Get count of players in a room
     */
    public synchronized int getRoomPlayerCount(String roomId) {
        int count = 0;
        for (ClientInfo client : clients) {
            if (client.currentRoomId.equals(roomId)) {
                count++;
            }
        }
        return count;
    }
}