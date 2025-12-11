package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import admin.AdminServer;
import admin.shared.AdminActionWatcher;
import network.clientHandler.ClientHandler;

/**
 * GameServer - Main multiplayer server
 * Now includes AdminActionWatcher for file-based admin commands
 */
public class GameServer {
    
    private static final int PORT = 5555;
    private static GameServerGroup clientGroup;
    
    public static void main(String[] args) {
        clientGroup = new GameServerGroup();
        
        System.out.println("===========================================");
        System.out.println("  Futura Multiplayer Server");
        System.out.println("  Game Port: " + PORT);
        System.out.println("  Admin Port: 5001");
        System.out.println("  Room System: ENABLED");
        System.out.println("  Admin Actions: FILE-BASED (Approach A)");
        System.out.println("===========================================");
        
        // Start Admin Server
        AdminServer adminServer = new AdminServer(clientGroup);
        adminServer.start();
        
        // ✨ NEW - Start Admin Action Watcher (watches admin_actions.dat)
        AdminActionWatcher actionWatcher = new AdminActionWatcher(clientGroup);
        actionWatcher.start();
        
        // Add shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SERVER] Shutting down...");
            actionWatcher.shutdown();
        }));
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Game server listening on port " + PORT);
            
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("\n[NEW PLAYER] " + 
                                 socket.getInetAddress().getHostAddress() + 
                                 ":" + socket.getPort());
                
                ClientHandler clientHandler = new ClientHandler(socket, clientGroup);
                clientHandler.start();
            }
            
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}