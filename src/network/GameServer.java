package network;

import java.io.*;
import java.net.*;

public class GameServer {
    
    private static final int PORT = 5555;
    private ServerSocket serverSocket;
    private GameServerGroup clientGroup;
    private volatile boolean running = true;
    
    public GameServer() {
        clientGroup = new GameServerGroup();
        System.out.println("Game Server starting on port " + PORT);
        
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started successfully. Waiting for players...");
            
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    String clientAddr = clientSocket.getInetAddress().getHostAddress();
                    int clientPort = clientSocket.getPort();
                    
                    System.out.println("New connection from: " + clientAddr + ":" + clientPort);
                    
                    ClientHandler handler = new ClientHandler(clientSocket, clientGroup, clientAddr, clientPort);
                    handler.start();
                    
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Could not start server on port " + PORT);
            e.printStackTrace();
        }
    }
    
    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        new GameServer();
    }
}
