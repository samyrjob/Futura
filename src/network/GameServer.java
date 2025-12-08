package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import admin.AdminServer;
import network.clientHandler.ClientHandler;

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
        System.out.println("===========================================");
        
        // ✨ NEW - Start Admin Server in separate thread
        AdminServer adminServer = new AdminServer(clientGroup);
        adminServer.start();
        
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