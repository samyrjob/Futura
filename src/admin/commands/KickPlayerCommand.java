package admin.commands;

import admin.AdminCommand;
import admin.AdminContext;
import network.ClientInfo;

public class KickPlayerCommand implements AdminCommand {
    
    @Override
    public void execute(String message, AdminContext context) {
        // Format: KICK <username>
        String[] parts = message.split(" ");
        if (parts.length < 2) {
            context.send("ERROR Usage: KICK <username>");
            return;
        }
        
        String targetUsername = parts[1];
        ClientInfo target = context.clientGroup.getClientByName(targetUsername);
        
        if (target == null) {
            context.send("ERROR Player not found: " + targetUsername);
            return;
        }
        
        // Send kick message to player
        target.sendMessage("KICKED You have been kicked by admin");
        
        // Notify room
        context.clientGroup.broadcastToRoom(
            target.currentRoomId,
            target.address,
            target.port,
            "playerLeft " + targetUsername
        );
        
        // Remove from server
        context.clientGroup.removeClient(target.address, target.port);
        
        context.send("SUCCESS Kicked player: " + targetUsername);
        System.out.println("[ADMIN] Kicked player: " + targetUsername);
    }
}