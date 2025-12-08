package admin.commands;

import admin.AdminCommand;
import admin.AdminContext;
import admin.shared.AdminActionFile;
import network.ClientInfo;

/**
 * MOVE_PLAYER command - Now uses shared file approach (Approach A)
 * Writes to admin_actions.dat instead of direct execution
 */
public class MovePlayerCommand implements AdminCommand {
    
    @Override
    public void execute(String message, AdminContext context) {
        // Format: MOVE_PLAYER <username> <targetRoomId>
        String[] parts = message.split(" ");
        
        if (parts.length < 3) {
            context.send("ERROR Usage: MOVE_PLAYER <username> <targetRoomId>");
            return;
        }
        
        String targetUsername = parts[1];
        String targetRoomId = parts[2];
        
        // Verify player exists before writing action
        ClientInfo target = context.clientGroup.getClientByName(targetUsername);
        
        if (target == null) {
            context.send("ERROR Player not found: " + targetUsername);
            return;
        }
        
        // Check if already in target room
        if (target.currentRoomId.equals(targetRoomId)) {
            context.send("ERROR Player " + targetUsername + " is already in room: " + targetRoomId);
            return;
        }
        
        // Write action to shared file (Approach A)
        String actionId = AdminActionFile.writeMoveAction(targetUsername, targetRoomId);
        
        context.send("SUCCESS MOVE action queued [" + actionId + "] for player: " + 
                    targetUsername + " → " + targetRoomId);
        System.out.println("[ADMIN] MOVE queued via file - ID: " + actionId + 
                          ", Target: " + targetUsername + ", Room: " + targetRoomId);
    }
}