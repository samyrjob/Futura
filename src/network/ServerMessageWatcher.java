package network;

import java.io.*;
import java.util.StringTokenizer;

import main.GameConstants;
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
    
    // ═══════════════════════════════════════════════════════════
    // MESSAGE ROUTER
    // ═══════════════════════════════════════════════════════════
    
    private void processMessage(String message) {
        // Player events
        if (message.startsWith("playerJoined")) {
            handlePlayerJoined(message);
        } else if (message.startsWith("playerMoved")) {
            handlePlayerMoved(message);
        } else if (message.startsWith("playerChat")) {
            handlePlayerChat(message);
        } else if (message.startsWith("playerLeft")) {
            handlePlayerLeft(message);
        } 
        // Details exchange
        else if (message.startsWith("wantDetails")) {
            handleWantDetails(message);
        } else if (message.startsWith("detailsFor")) {
            handleDetailsFor(message);
        }
        // ✨ Admin commands - FIXED
        else if (message.startsWith("forceRoomChange")) {
            handleForceRoomChange(message);
        } else if (message.startsWith("adminMessage")) {
            handleAdminMessage(message);
        } else if (message.startsWith("KICKED")) {
            handleKicked(message);
        }
        // Unknown
        else {
            System.out.println("Unknown message from server: " + message);
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // PLAYER EVENT HANDLERS
    // ═══════════════════════════════════════════════════════════
        
    private void handlePlayerJoined(String message) {
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "playerJoined"
        
        String username = st.nextToken();
        String genderStr = st.nextToken();
        int mapX = Integer.parseInt(st.nextToken());
        int mapY = Integer.parseInt(st.nextToken());
        String directionStr = st.nextToken();
        
        // ═══════════════════════════════════════════════════════════
        // ✅ FIX: Don't create a remote player for YOURSELF!
        // ═══════════════════════════════════════════════════════════
        if (gamePanel.player != null && username.equals(gamePanel.player.name)) {
            System.out.println("[CLIENT] Ignoring playerJoined for self: " + username);
            return;
        }
        
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
    
    // ═══════════════════════════════════════════════════════════
    // DETAILS EXCHANGE HANDLERS
    // ═══════════════════════════════════════════════════════════
    
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
    // ✨ ADMIN COMMAND HANDLERS - FIXED VERSION
    // ═══════════════════════════════════════════════════════════
    
    private void handleForceRoomChange(String message) {
        // Format: forceRoomChange <roomId>
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "forceRoomChange"
        
        if (!st.hasMoreTokens()) {
            System.err.println("[CLIENT] Invalid forceRoomChange message - no room ID");
            return;
        }
        
        String targetRoomId = st.nextToken();
        System.out.println("[CLIENT] Admin forcing room change to: " + targetRoomId);
        
        // ✨ ACTUALLY change the room on the client side
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gamePanel.roomController != null) {
                // Check if room exists
                model.room.Room targetRoom = gamePanel.roomController.getRoom(targetRoomId);
                
                if (targetRoom != null) {
                    // Room exists - enter it
                    boolean success = gamePanel.roomController.enterRoom(targetRoomId);
                    
                    if (success) {
                        System.out.println("[CLIENT] Successfully moved to room: " + targetRoomId);
                        
                        // Show notification to player
                        showAdminNotification("You have been moved to room: " + targetRoom.getRoomName());
                    } else {
                        System.err.println("[CLIENT] Failed to enter room: " + targetRoomId);
                    }
                } else {
                    // Room doesn't exist on client - go to lobby instead
                    System.out.println("[CLIENT] Room not found locally, going to lobby");
                    gamePanel.roomController.returnToLobby();
                    showAdminNotification("Admin moved you - room not found, returned to lobby");
                }
                
                // Close the room navigator if open
                if (gamePanel.roomNavigator != null && gamePanel.roomNavigator.isVisible()) {
                    gamePanel.roomNavigator.toggle();
                }
                
                // Repaint to show changes
                gamePanel.repaint();
            } else {
                System.err.println("[CLIENT] RoomController is null!");
            }
        });
    }
    
    private void handleAdminMessage(String message) {
        // Format: adminMessage <text...>
        int spaceIndex = message.indexOf(' ');
        
        if (spaceIndex == -1) {
            System.err.println("[CLIENT] Invalid adminMessage format");
            return;
        }
        
        String text = message.substring(spaceIndex + 1);
        System.out.println("[CLIENT] Admin broadcast: " + text);
        
        // Show as chat bubble above player
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gamePanel.player != null) {
                int bubbleY = gamePanel.player.spriteY + 50;
                gamePanel.player.messages.add(
                    new Entity.Player.Message("[ADMIN]: " + text, bubbleY)
                );
                gamePanel.repaint();
            }
        });
    }
    
    private void handleKicked(String message) {
        // Format: KICKED <reason...>
        String reason = "No reason provided";
        
        if (message.length() > 7) {
            reason = message.substring(7); // Skip "KICKED "
        }
        
        System.out.println("[CLIENT] KICKED by admin: " + reason);
        
        final String finalReason = reason;
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            // ✨ Instead of disconnecting, move player to lobby
            if (gamePanel.roomController != null) {
                // Return to lobby
                gamePanel.roomController.returnToLobby();
                
                // Reset player position
                gamePanel.player.movement.xCurrent = 4;
                gamePanel.player.movement.yCurrent = 2;
                gamePanel.player.updateSpritePosition();
                
                // Close any open windows
                if (gamePanel.roomNavigator != null && gamePanel.roomNavigator.isVisible()) {
                    gamePanel.roomNavigator.toggle();
                }
                
                // Show kick message
                javax.swing.JOptionPane.showMessageDialog(
                    gamePanel,
                    "You have been kicked by an admin:\n\n" + finalReason + "\n\nYou have been returned to the lobby.",
                    "Kicked by Admin",
                    javax.swing.JOptionPane.WARNING_MESSAGE
                );
                
                // Show notification in game
                int bubbleY = gamePanel.player.spriteY + 50;
                gamePanel.player.messages.add(
                    new Entity.Player.Message("[ADMIN]: You were kicked - " + finalReason, bubbleY)
                );
                
                gamePanel.repaint();
            }
            
            // Notify server that we're now in lobby
            if (networkManager != null && networkManager.isConnected()) {
                networkManager.sendRoomChange(GameConstants.LOBBY_ROOM_ID);
            }
        });
    }
    
    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════
    
    private void showAdminNotification(String text) {
        if (gamePanel.player != null) {
            int bubbleY = gamePanel.player.spriteY + 50;
            gamePanel.player.messages.add(
                new Entity.Player.Message("[SYSTEM]: " + text, bubbleY)
            );
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // CONTROL
    // ═══════════════════════════════════════════════════════════
    
    public void stopWatching() {
        running = false;
        this.interrupt();
    }
}