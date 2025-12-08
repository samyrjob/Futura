package admin.commands;

import admin.AdminCommand;
import admin.AdminContext;

public class BroadcastCommand implements AdminCommand {
    
    @Override
    public void execute(String message, AdminContext context) {
        // Format: BROADCAST <message>
        int spaceIndex = message.indexOf(' ');
        if (spaceIndex == -1) {
            context.send("ERROR Usage: BROADCAST <message>");
            return;
        }
        
        String broadcastText = message.substring(spaceIndex + 1);
        
        // Send to ALL players in ALL rooms
        context.clientGroup.broadcastToAll("adminMessage " + broadcastText);
        
        context.send("SUCCESS Broadcast sent");
        System.out.println("[ADMIN] Broadcast: " + broadcastText);
    }
}