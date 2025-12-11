package admin.commands;

import admin.AdminCommand;
import admin.AdminContext;
import network.ClientInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListRoomsCommand implements AdminCommand {
    
    @Override
    public void execute(String message, AdminContext context) {
        List<ClientInfo> allClients = context.clientGroup.getAllClients();
        
        // Count players per room
        Map<String, Integer> roomCounts = new HashMap<>();
        for (ClientInfo client : allClients) {
            String roomId = client.currentRoomId;
            roomCounts.put(roomId, roomCounts.getOrDefault(roomId, 0) + 1);
        }
        
        context.send("ROOMS_START");
        context.send("COUNT " + roomCounts.size());
        
        for (Map.Entry<String, Integer> entry : roomCounts.entrySet()) {
            String roomId = entry.getKey();
            int playerCount = entry.getValue();
            
            // Format: ROOM <roomId> <playerCount>
            context.send("ROOM " + roomId + " " + playerCount);
        }
        
        context.send("ROOMS_END");
    }
}
