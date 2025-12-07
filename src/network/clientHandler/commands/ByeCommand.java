package network.clientHandler.commands;

import java.io.IOException;

import network.clientHandler.ClientContext;
import network.clientHandler.GameCommand;

public class ByeCommand implements GameCommand {
    
    @Override
    public void execute(String message, ClientContext ctx) {
        try {
            if (ctx.playerName != null) {
                // Notify room that player left
                ctx.clientGroup.broadcastToRoom(ctx.currentRoomId, ctx.clientAddr, ctx.port, 
                                               "playerLeft " + ctx.playerName);
                ctx.clientGroup.removeClient(ctx.clientAddr, ctx.port);
                System.out.println(ctx.playerName + " disconnected from room: " + ctx.currentRoomId);
            }
            ctx.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}