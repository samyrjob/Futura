package network;

import java.util.ArrayList;
import java.util.List;

public class GameServerGroup {
    
    private List<ClientInfo> clients;
    
    public GameServerGroup() {
        clients = new ArrayList<>();
    }
    
    public synchronized void addClient(ClientInfo client) {
        clients.add(client);
        System.out.println("Player joined: " + client.playerName + " (Total players: " + clients.size() + ")");
    }
    
    public synchronized void removeClient(String addr, int port) {
        ClientInfo toRemove = null;
        for (ClientInfo client : clients) {
            if (client.matches(addr, port)) {
                toRemove = client;
                break;
            }
        }
        
        if (toRemove != null) {
            clients.remove(toRemove);
            System.out.println("Player left: " + toRemove.playerName + " (Total players: " + clients.size() + ")");
        }
    }
    
    public synchronized ClientInfo getClient(String addr, int port) {
        for (ClientInfo client : clients) {
            if (client.matches(addr, port)) {
                return client;
            }
        }
        return null;
    }
    
    public synchronized void broadcast(String senderAddr, int senderPort, String message) {
        for (ClientInfo client : clients) {
            if (!client.matches(senderAddr, senderPort)) {
                client.sendMessage(message);
            }
        }
    }
    
    public synchronized void sendTo(String targetAddr, int targetPort, String message) {
        for (ClientInfo client : clients) {
            if (client.matches(targetAddr, targetPort)) {
                client.sendMessage(message);
                break;
            }
        }
    }
    
    public synchronized void broadcastToAll(String message) {
        for (ClientInfo client : clients) {
            client.sendMessage(message);
        }
    }
}