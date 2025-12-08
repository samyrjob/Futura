package network;

import java.io.*;
import java.util.StringTokenizer;

import main.GamePanel;
import Entity.Entity.Gender;

public class ServerMessageWatcher extends Thread {
    
    private NetworkManager networkManager;
    private BufferedReader in;
    private GamePanel gamePanel;
    private volatile boolean running = true;
    
    public ServerMessageWatcher(NetworkManager networkManager, BufferedReader in, GamePanel gamePanel) {
        this.networkManager = networkManager;
        this.in = in;
        this.gamePanel = gamePanel;
    }
    
    @Override
    public void run() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                System.out.println("Received from server: " + line);
                processMessage(line.trim());
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Connection to server lost: " + e.getMessage());
            }
        } finally {
            running = false;
        }
    }
    
    private void processMessage(String message) {
        if (message.startsWith("playerJoined")) {
            handlePlayerJoined(message);
        } else if (message.startsWith("playerMoved")) {
            handlePlayerMoved(message);
        } else if (message.startsWith("playerChat")) {
            handlePlayerChat(message);
        } else if (message.startsWith("playerLeft")) {
            handlePlayerLeft(message);
        } else if (message.startsWith("wantDetails")) {
            handleWantDetails(message);
        } else if (message.startsWith("detailsFor")) {
            handleDetailsFor(message);
        } else if (message.startsWith("forceRoomChange")) {
            handleForceRoomChange(message);  // ✨ NEW
        } else if (message.startsWith("adminMessage")) {
            handleAdminMessage(message);     // ✨ NEW
        } else if (message.startsWith("KICKED")) {
            handleKicked(message);           // ✨ NEW
        }   // Unknown message
        else {
            System.out.println("Unknown message from server: " + message);
        }
    }
    
    private void handlePlayerJoined(String message) {
        // Format: playerJoined <username> <gender> <mapX> <mapY> <direction>
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "playerJoined"
        
        String username = st.nextToken();
        String genderStr = st.nextToken();
        int mapX = Integer.parseInt(st.nextToken());
        int mapY = Integer.parseInt(st.nextToken());
        String directionStr = st.nextToken();
        
        Gender gender = genderStr.equalsIgnoreCase("FEMALE") ? Gender.FEMALE : Gender.MALE;
        
        gamePanel.addRemotePlayer(username, gender, mapX, mapY, directionStr);
    }
    
    private void handlePlayerMoved(String message) {
        // Format: playerMoved <username> <mapX> <mapY> <direction> <inMovement>
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "playerMoved"
        
        String username = st.nextToken();
        int mapX = Integer.parseInt(st.nextToken());
        int mapY = Integer.parseInt(st.nextToken());
        String direction = st.nextToken();
        boolean inMovement = Boolean.parseBoolean(st.nextToken());
        
        gamePanel.updateRemotePlayer(username, mapX, mapY, direction, inMovement);
    }
    
    private void handlePlayerChat(String message) {
        // Format: playerChat <username> <text...>
        int firstSpace = message.indexOf(' ');
        int secondSpace = message.indexOf(' ', firstSpace + 1);
        
        if (secondSpace != -1) {
            String username = message.substring(firstSpace + 1, secondSpace);
            String chatText = message.substring(secondSpace + 1);
            
            gamePanel.addRemotePlayerChat(username, chatText);
        }
    }
    
    private void handlePlayerLeft(String message) {
        // Format: playerLeft <username>
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "playerLeft"
        
        String username = st.nextToken();
        gamePanel.removeRemotePlayer(username);
    }
    
    private void handleWantDetails(String message) {
        // Format: wantDetails <requesterAddr> <requesterPort>
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "wantDetails"
        
        String requesterAddr = st.nextToken();
        int requesterPort = Integer.parseInt(st.nextToken());
        
        // Send our player's details
        if (gamePanel.player != null) {
            networkManager.sendDetailsResponse(
                requesterAddr, 
                requesterPort,
                gamePanel.player.name,
                gamePanel.player.gender.toString(),
                gamePanel.player.movement.xCurrent,
                gamePanel.player.movement.yCurrent,
                gamePanel.player.direction.toString()
            );
        }
    }
    
    private void handleDetailsFor(String message) {
        // Format: detailsFor <username> <gender> <mapX> <mapY> <direction>
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "detailsFor"
        
        String username = st.nextToken();
        String genderStr = st.nextToken();
        int mapX = Integer.parseInt(st.nextToken());
        int mapY = Integer.parseInt(st.nextToken());
        String directionStr = st.nextToken();
        
        Gender gender = genderStr.equalsIgnoreCase("FEMALE") ? Gender.FEMALE : Gender.MALE;
        
        gamePanel.addRemotePlayer(username, gender, mapX, mapY, directionStr);
    }


    // ═══════════════════════════════════════════════════════════
    // ADMIN COMMAND HANDLERS
    // ═══════════════════════════════════════════════════════════

    private void handleForceRoomChange(String message) {
        // Format: forceRoomChange <roomId>
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "forceRoomChange"
        
        if (!st.hasMoreTokens()) {
            System.err.println("Invalid forceRoomChange message");
            return;
        }
        
        String targetRoomId = st.nextToken();
        System.out.println("[ADMIN] Forcing room change to: " + targetRoomId);
        
        // Switch rooms using RoomManager
        if (gamePanel.roomManager != null) {
            // Run on EDT to avoid threading issues with Swing
            javax.swing.SwingUtilities.invokeLater(() -> {
                gamePanel.roomManager.enterRoom(targetRoomId, gamePanel.player.name);
            });
        }
    }

    private void handleAdminMessage(String message) {
        // Format: adminMessage <text...>
        int spaceIndex = message.indexOf(' ');
        
        if (spaceIndex == -1) {
            System.err.println("Invalid adminMessage format");
            return;
        }
        
        String text = message.substring(spaceIndex + 1);
        System.out.println("[ADMIN BROADCAST] " + text);
        
        // Show as chat bubble above player
        javax.swing.SwingUtilities.invokeLater(() -> {
            int bubbleY = gamePanel.player.spriteY + 50;
            gamePanel.player.messages.add(
                new Entity.Player.Message("[ADMIN]: " + text, bubbleY)
            );
        });
    }

    private void handleKicked(String message) {
        // Format: KICKED <reason...>
        String reason = "No reason provided";
        
        if (message.length() > 7) {
            reason = message.substring(7); // Skip "KICKED "
        }
        
        System.out.println("[KICKED] " + reason);
        
        final String finalReason = reason;
        
        // Disconnect and show dialog on EDT
        javax.swing.SwingUtilities.invokeLater(() -> {
            // Stop watching for messages
            running = false;
            
            // Disconnect from server
            networkManager.disconnect();
            
            // Show message to user
            javax.swing.JOptionPane.showMessageDialog(
                gamePanel,
                "You have been kicked from the server:\n\n" + finalReason,
                "Kicked by Admin",
                javax.swing.JOptionPane.WARNING_MESSAGE
            );
            
            // Optional: Return to main menu or exit
            // System.exit(0);
        });
    }
}