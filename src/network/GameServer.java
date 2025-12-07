package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import network.clientHandler.ClientHandler;

/**
 * GameServer - Main multiplayer server
 * Listens for client connections and creates ClientHandler threads
 */
public class GameServer {
    
    private static final int PORT = 5555;
    private static GameServerGroup clientGroup;
    
    public static void main(String[] args) {
        clientGroup = new GameServerGroup();
        
        System.out.println("===========================================");
        System.out.println("  Futura Multiplayer Server");
        System.out.println("  Port: " + PORT);
        System.out.println("  Room System: ENABLED");
        System.out.println("===========================================");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("\n[NEW CONNECTION] " + 
                                 socket.getInetAddress().getHostAddress() + 
                                 ":" + socket.getPort());
                
                // ✨ UPDATED - Use simplified constructor (only 2 parameters)
                ClientHandler clientHandler = new ClientHandler(socket, clientGroup);
                clientHandler.start();
            }
            
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}