// package network;

// import java.io.*;
// import java.net.*;
// import java.util.StringTokenizer;

// /**
//  * ClientHandler - Handles ONE client connection (runs in separate thread)
//  * NOW WITH ROOM SUPPORT!
//  */
// public class ClientHandler extends Thread {
    
//     private Socket socket;
//     private BufferedReader in;
//     private PrintWriter out;
//     private GameServerGroup clientGroup;
    
//     private String clientAddr;
//     private int port;
//     private String playerName;
//     private String currentRoomId = "lobby";  // ✨ NEW - Track current room
    
//     public ClientHandler(Socket socket, GameServerGroup clientGroup) {
//         this.socket = socket;
//         this.clientGroup = clientGroup;
//         this.clientAddr = socket.getInetAddress().getHostAddress();
//         this.port = socket.getPort();
        
//         try {
//             in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//             out = new PrintWriter(socket.getOutputStream(), true);
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }
    
//     @Override
//     public void run() {
//         try {
//             String message;
//             while ((message = in.readLine()) != null) {
//                 System.out.println("Received from " + clientAddr + ":" + port + " - " + message);
//                 processMessage(message);
//             }
//         } catch (IOException e) {
//             System.out.println("Client disconnected: " + clientAddr + ":" + port);
//         } finally {
//             cleanup();
//         }
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // MESSAGE PROCESSING
//     // ═══════════════════════════════════════════════════════════
    
//     private void processMessage(String message) {
//         if (message.startsWith("join")) {
//             handleJoin(message);
//         } else if (message.startsWith("move")) {
//             handleMove(message);
//         } else if (message.startsWith("chat")) {
//             handleChat(message);
//         } else if (message.startsWith("changeRoom")) {
//             handleChangeRoom(message);  // ✨ NEW
//         } else if (message.startsWith("leaveRoom")) {
//             handleLeaveRoom(message);   // ✨ NEW
//         } else if (message.startsWith("wantDetails")) {
//             handleWantDetails(message);
//         } else if (message.startsWith("detailsFor")) {
//             handleDetailsFor(message);
//         } else if (message.startsWith("bye")) {
//             handleBye();
//         }
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // JOIN - Player enters game
//     // ═══════════════════════════════════════════════════════════
    
//     private void handleJoin(String message) {
//         // Format: join <username> <gender> <mapX> <mapY> <direction> [roomId]
//         StringTokenizer st = new StringTokenizer(message);
//         st.nextToken(); // skip "join"
        
//         playerName = st.nextToken();
//         String gender = st.nextToken();
//         int mapX = Integer.parseInt(st.nextToken());
//         int mapY = Integer.parseInt(st.nextToken());
//         String direction = st.nextToken();
        
//         // ✨ NEW - Get room ID (defaults to "lobby" if not provided)
//         if (st.hasMoreTokens()) {
//             currentRoomId = st.nextToken();
//         } else {
//             currentRoomId = "lobby";
//         }
        
//         // Add this client to the group
//         ClientInfo clientInfo = new ClientInfo(
//             out, clientAddr, port, playerName, gender, mapX, mapY, direction, currentRoomId
//         );
//         clientGroup.addClient(clientInfo);
        
//         // ✨ CHANGED - Broadcast join message ONLY to players in same room
//         String joinMsg = "playerJoined " + playerName + " " + gender + " " + 
//                         mapX + " " + mapY + " " + direction;
//         clientGroup.broadcastToRoom(currentRoomId, clientAddr, port, joinMsg);
        
//         // Request details from players in same room
//         clientGroup.broadcastToRoom(currentRoomId, clientAddr, port, 
//                                     "wantDetails " + clientAddr + " " + port);
        
//         System.out.println(playerName + " joined room: " + currentRoomId);
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // MOVE - Player moves
//     // ═══════════════════════════════════════════════════════════
    
//     private void handleMove(String message) {
//         // ✨ CHANGED - Broadcast only to same room
//         if (playerName != null) {
//             String moveMsg = "playerMoved " + playerName + " " + message.substring(5);
//             clientGroup.broadcastToRoom(currentRoomId, clientAddr, port, moveMsg);
//         }
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // CHAT - Player sends chat
//     // ═══════════════════════════════════════════════════════════
    
//     private void handleChat(String message) {
//         // ✨ CHANGED - Broadcast only to same room
//         if (playerName != null) {
//             String chatMsg = "playerChat " + playerName + " " + message.substring(5);
//             clientGroup.broadcastToRoom(currentRoomId, clientAddr, port, chatMsg);
//         }
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // CHANGE ROOM - ✨ NEW - Player switches rooms
//     // ═══════════════════════════════════════════════════════════
    
//     private void handleChangeRoom(String message) {
//         // Format: changeRoom <newRoomId>
//         StringTokenizer st = new StringTokenizer(message);
//         st.nextToken(); // skip "changeRoom"
//         String newRoomId = st.nextToken();
        
//         // Notify old room that player left
//         String leaveMsg = "playerLeft " + playerName;
//         clientGroup.broadcastToRoom(currentRoomId, clientAddr, port, leaveMsg);
        
//         // Update current room
//         String oldRoomId = currentRoomId;
//         currentRoomId = newRoomId;
        
//         // Update client info
//         ClientInfo clientInfo = clientGroup.getClient(clientAddr, port);
//         if (clientInfo != null) {
//             clientInfo.currentRoomId = newRoomId;
//         }
        
//         // Notify new room that player joined
//         String joinMsg = "playerJoined " + playerName + " " + clientInfo.gender + " " +
//                         clientInfo.mapX + " " + clientInfo.mapY + " " + clientInfo.direction;
//         clientGroup.broadcastToRoom(newRoomId, clientAddr, port, joinMsg);
        
//         // Request details from players in new room
//         clientGroup.broadcastToRoom(newRoomId, clientAddr, port,
//                                     "wantDetails " + clientAddr + " " + port);
        
//         System.out.println(playerName + " changed from room '" + oldRoomId + 
//                           "' to room '" + newRoomId + "'");
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // LEAVE ROOM - ✨ NEW - Player explicitly leaves room
//     // ═══════════════════════════════════════════════════════════
    
//     private void handleLeaveRoom(String message) {
//         // Format: leaveRoom <roomId>
//         StringTokenizer st = new StringTokenizer(message);
//         st.nextToken(); // skip "leaveRoom"
//         String roomId = st.nextToken();
        
//         String leaveMsg = "playerLeft " + playerName;
//         clientGroup.broadcastToRoom(roomId, clientAddr, port, leaveMsg);
        
//         System.out.println(playerName + " left room: " + roomId);
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // WANT DETAILS - Request player details
//     // ═══════════════════════════════════════════════════════════
    
//     private void handleWantDetails(String message) {
//         StringTokenizer st = new StringTokenizer(message);
//         st.nextToken(); // skip "wantDetails"
//         String requesterAddr = st.nextToken();
//         int requesterPort = Integer.parseInt(st.nextToken());
        
//         // Only send details if we're in the same room as requester
//         ClientInfo requester = clientGroup.getClient(requesterAddr, requesterPort);
//         if (requester != null && requester.currentRoomId.equals(currentRoomId)) {
//             ClientInfo myInfo = clientGroup.getClient(clientAddr, port);
//             if (myInfo != null) {
//                 clientGroup.sendTo(requesterAddr, requesterPort,
//                     "detailsFor " + playerName + " " + myInfo.gender + " " +
//                     myInfo.mapX + " " + myInfo.mapY + " " + myInfo.direction);
//             }
//         }
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // DETAILS FOR - Receive player details
//     // ═══════════════════════════════════════════════════════════
    
//     private void handleDetailsFor(String message) {
//         // Just forward to client
//         out.println(message);
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // BYE - Player disconnects
//     // ═══════════════════════════════════════════════════════════
    
//     private void handleBye() {
//         cleanup();
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // CLEANUP
//     // ═══════════════════════════════════════════════════════════
    
//     private void cleanup() {
//         try {
//             if (playerName != null) {
//                 // Notify room that player left
//                 clientGroup.broadcastToRoom(currentRoomId, clientAddr, port, 
//                                            "playerLeft " + playerName);
//                 clientGroup.removeClient(clientAddr, port);
//                 System.out.println(playerName + " disconnected from room: " + currentRoomId);
//             }
//             socket.close();
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }
// }

// Simplified ClientHandler

package network.clientHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import network.GameServerGroup;

public class ClientHandler extends Thread {
    
    private static final CommandRegistry registry = new CommandRegistry();
    
    private Socket socket;
    private ClientContext context;
    
    public ClientHandler(Socket socket, GameServerGroup clientGroup) {
        this.socket = socket;
        
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            this.context = new ClientContext(
                out, clientGroup,
                socket.getInetAddress().getHostAddress(),
                socket.getPort(), socket
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void processMessage(String message) {
        int spaceIndex = message.indexOf(' ');
        String commandName = (spaceIndex == -1) ? message : message.substring(0, spaceIndex);
        
        GameCommand command = registry.getCommand(commandName);
        command.execute(message, context);
    }
}