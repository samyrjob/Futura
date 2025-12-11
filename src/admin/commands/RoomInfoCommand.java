package admin.commands;

import admin.AdminCommand;
import admin.AdminContext;
import network.ClientInfo;

import java.util.List;

public class RoomInfoCommand implements AdminCommand {
    
    @Override
    public void execute(String message, AdminContext context) {
        // Format: ROOM_INFO <roomId>
        String[] parts = message.split(" ");
        
        if (parts.length < 2) {
            context.send("ERROR Usage: ROOM_INFO <roomId>");
            return;
        }
        
        String roomId = parts[1];
        List<ClientInfo> roomPlayers = context.clientGroup.getClientsInRoom(roomId);
        
        if (roomPlayers.isEmpty()) {
            context.send("ROOM_INFO_START");
            context.send("ROOM_ID " + roomId);
            context.send("STATUS EMPTY");
            context.send("PLAYER_COUNT 0");
            context.send("ROOM_INFO_END");
            return;
        }
        
        context.send("ROOM_INFO_START");
        context.send("ROOM_ID " + roomId);
        context.send("STATUS ACTIVE");
        context.send("PLAYER_COUNT " + roomPlayers.size());
        
        // List all players in the room with their positions
        for (ClientInfo player : roomPlayers) {
            // Format: PLAYER_DETAIL <name> <gender> <x> <y> <direction>
            context.send("PLAYER_DETAIL " + 
                        player.playerName + " " + 
                        player.gender + " " + 
                        player.mapX + " " + 
                        player.mapY + " " + 
                        player.direction);
        }
        
        context.send("ROOM_INFO_END");
    }
}