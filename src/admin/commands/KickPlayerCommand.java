package admin.commands;

import admin.AdminCommand;
import admin.AdminContext;
import admin.shared.AdminActionFile;
import network.ClientInfo;

/**
 * KICK command - Now uses shared file approach (Approach A)
 * Writes to admin_actions.dat instead of direct execution
 */
public class KickPlayerCommand implements AdminCommand {
    
    @Override
    public void execute(String message, AdminContext context) {
        // Format: KICK <username> [reason]
        String[] parts = message.split(" ", 3);
        
        if (parts.length < 2) {
            context.send("ERROR Usage: KICK <username> [reason]");
            return;
        }
        
        String targetUsername = parts[1];
        String reason = parts.length > 2 ? parts[2] : "Kicked by admin";
        
        // Verify player exists before writing action
        ClientInfo target = context.clientGroup.getClientByName(targetUsername);
        
        if (target == null) {
            context.send("ERROR Player not found: " + targetUsername);
            return;
        }
        
        // Write action to shared file (Approach A)
        String actionId = AdminActionFile.writeKickAction(targetUsername, reason);
        
        context.send("SUCCESS KICK action queued [" + actionId + "] for player: " + targetUsername);
        System.out.println("[ADMIN] KICK queued via file - ID: " + actionId + 
                          ", Target: " + targetUsername + ", Reason: " + reason);
    }
}