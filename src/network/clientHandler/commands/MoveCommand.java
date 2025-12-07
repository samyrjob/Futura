package network.clientHandler.commands;

import network.clientHandler.ClientContext;
import network.clientHandler.GameCommand;

public class MoveCommand implements GameCommand {
    
    @Override
    public void execute(String message, ClientContext ctx) {
        if (ctx.playerName != null) {
            String moveMsg = "playerMoved " + ctx.playerName + " " + message.substring(5);
            ctx.clientGroup.broadcastToRoom(ctx.currentRoomId, ctx.clientAddr, ctx.port, moveMsg);
        }
    }
}
