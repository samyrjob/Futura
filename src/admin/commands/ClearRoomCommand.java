package admin.commands;

import admin.AdminCommand;
import admin.AdminContext;
import network.ClientInfo;

import java.util.List;

public class ClearRoomCommand implements AdminCommand {
    
    @Override
    public void execute(String message, AdminContext context) {
        // Format: CLEAR_ROOM <roomId>
        String[] parts = message.split(" ");
        
        if (parts.length < 2) {
            context.send("ERROR Usage: CLEAR_ROOM <roomId>");
            return;
        }
        
        String roomId = parts[1];
        
        // Prevent clearing lobby (optional safety)
        if (roomId.equals("lobby")) {
            context.send("ERROR Cannot clear lobby");
            return;
        }
        
        List<ClientInfo> roomPlayers = context.clientGroup.getClientsInRoom(roomId);
        
        if (roomPlayers.isEmpty()) {
            context.send("ERROR Room is already empty: " + roomId);
            return;
        }
        
        int kickedCount = 0;
        
        // Kick each player to lobby
        for (ClientInfo player : roomPlayers) {
            // Tell player they're being moved
            player.sendMessage("forceRoomChange lobby");
            player.sendMessage("adminMessage Room cleared by admin");
            
            // Update their room on server side
            player.currentRoomId = "lobby";
            
            kickedCount++;
        }
        
        context.send("SUCCESS Cleared " + kickedCount + " players from room: " + roomId);
        System.out.println("[ADMIN] Cleared room " + roomId + " (" + kickedCount + " players moved to lobby)");
    }
}