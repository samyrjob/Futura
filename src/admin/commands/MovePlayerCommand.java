package admin.commands;

import admin.AdminCommand;
import admin.AdminContext;
import network.ClientInfo;

public class MovePlayerCommand implements AdminCommand {
    
    @Override
    public void execute(String message, AdminContext context) {
        // Format: MOVE_PLAYER <username> <targetRoomId>
        String[] parts = message.split(" ");
        
        if (parts.length < 3) {
            context.send("ERROR Usage: MOVE_PLAYER <username> <targetRoomId>");
            return;
        }
        
        String username = parts[1];
        String targetRoomId = parts[2];
        
        ClientInfo player = context.clientGroup.getClientByName(username);
        
        if (player == null) {
            context.send("ERROR Player not found: " + username);
            return;
        }
        
        String oldRoom = player.currentRoomId;
        
        // Notify old room that player left
        context.clientGroup.broadcastToRoom(
            oldRoom,
            player.address,
            player.port,
            "playerLeft " + username
        );
        
        // Update player's room
        player.currentRoomId = targetRoomId;
        
        // Tell player to change rooms
        player.sendMessage("forceRoomChange " + targetRoomId);
        
        // Notify new room that player joined
        context.clientGroup.broadcastToRoom(
            targetRoomId,
            player.address,
            player.port,
            "playerJoined " + username + " " + player.gender + " " + 
            player.mapX + " " + player.mapY + " " + player.direction
        );
        
        context.send("SUCCESS Moved " + username + " from " + oldRoom + " to " + targetRoomId);
        System.out.println("[ADMIN] Moved " + username + ": " + oldRoom + " → " + targetRoomId);
    }
}