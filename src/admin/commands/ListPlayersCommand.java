package admin.commands;

import admin.AdminCommand;
import admin.AdminContext;
import network.ClientInfo;

import java.util.List;

public class ListPlayersCommand implements AdminCommand {
    
    @Override
    public void execute(String message, AdminContext context) {
        // Get all clients from the shared GameServerGroup
        List<ClientInfo> allClients = context.clientGroup.getAllClients();
        
        context.send("PLAYERS_START");
        context.send("COUNT " + allClients.size());
        
        for (ClientInfo client : allClients) {
            context.send("PLAYER " + client.playerName + " " + 
                        client.currentRoomId + " " + 
                        client.mapX + " " + client.mapY);
        }
        
        context.send("PLAYERS_END");
    }
}
