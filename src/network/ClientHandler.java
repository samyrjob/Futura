package network;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class ClientHandler extends Thread {
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private GameServerGroup clientGroup;
    private String clientAddr;
    private int port;
    private String playerName;
    
    public ClientHandler(Socket socket, GameServerGroup clientGroup, String clientAddr, int port) {
        this.socket = socket;
        this.clientGroup = clientGroup;
        this.clientAddr = clientAddr;
        this.port = port;
        
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error creating streams: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Received: " + line);
                processMessage(line.trim());
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + clientAddr + ":" + port);
        } finally {
            cleanup();
        }
    }
    
    private void processMessage(String message) {
        if (message.startsWith("join")) {
            handleJoin(message);
        } else if (message.startsWith("move")) {
            handleMove(message);
        } else if (message.startsWith("chat")) {
            handleChat(message);
        } else if (message.startsWith("wantDetails")) {
            handleWantDetails(message);
        } else if (message.startsWith("detailsFor")) {
            handleDetailsFor(message);
        } else if (message.startsWith("bye")) {
            handleBye();
        }
    }
    
    private void handleJoin(String message) {
        // Format: join <username> <gender> <mapX> <mapY> <direction>
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "join"
        
        playerName = st.nextToken();
        String gender = st.nextToken();
        int mapX = Integer.parseInt(st.nextToken());
        int mapY = Integer.parseInt(st.nextToken());
        String direction = st.nextToken();
        
        // Add this client to the group
        ClientInfo clientInfo = new ClientInfo(out, clientAddr, port, playerName, gender, mapX, mapY, direction);
        clientGroup.addClient(clientInfo);
        
        // Broadcast join message to all other clients
        String joinMsg = "playerJoined " + playerName + " " + gender + " " + mapX + " " + mapY + " " + direction;
        clientGroup.broadcast(clientAddr, port, joinMsg);
        
        // Request details from all existing players
        clientGroup.broadcast(clientAddr, port, "wantDetails " + clientAddr + " " + port);
    }
    
    private void handleMove(String message) {
        // Format: move <mapX> <mapY> <direction> <inMovement>
        if (playerName != null) {
            String moveMsg = "playerMoved " + playerName + " " + message.substring(5); // Remove "move "
            clientGroup.broadcast(clientAddr, port, moveMsg);
        }
    }
    
    private void handleChat(String message) {
        // Format: chat <text>
        if (playerName != null) {
            String chatMsg = "playerChat " + playerName + " " + message.substring(5); // Remove "chat "
            clientGroup.broadcast(clientAddr, port, chatMsg);
        }
    }
    
    private void handleWantDetails(String message) {
        // Format: wantDetails <requesterAddr> <requesterPort>
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "wantDetails"
        String requesterAddr = st.nextToken();
        int requesterPort = Integer.parseInt(st.nextToken());
        
        // Send our details to the requester
        ClientInfo myInfo = clientGroup.getClient(clientAddr, port);
        if (myInfo != null) {
            String detailsMsg = "detailsFor " + requesterAddr + " " + requesterPort + " " +
                               playerName + " " + myInfo.gender + " " + 
                               myInfo.mapX + " " + myInfo.mapY + " " + myInfo.direction;
            out.println(detailsMsg);
        }
    }
    
    private void handleDetailsFor(String message) {
        // Format: detailsFor <targetAddr> <targetPort> <username> <gender> <mapX> <mapY> <direction>
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "detailsFor"
        String targetAddr = st.nextToken();
        int targetPort = Integer.parseInt(st.nextToken());
        
        String remainingMsg = message.substring(message.indexOf(targetPort + "") + (targetPort + "").length()).trim();
        String detailsMsg = "detailsFor " + remainingMsg;
        
        clientGroup.sendTo(targetAddr, targetPort, detailsMsg);
    }
    
    private void handleBye() {
        if (playerName != null) {
            String byeMsg = "playerLeft " + playerName;
            clientGroup.broadcast(clientAddr, port, byeMsg);
        }
        cleanup();
    }
    
    private void cleanup() {
        try {
            clientGroup.removeClient(clientAddr, port);
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
}
