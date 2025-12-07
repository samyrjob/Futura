package network.clientHandler.commands;

import network.clientHandler.ClientContext;
import network.clientHandler.GameCommand;

class ChatCommand implements GameCommand {
    
    @Override
    public void execute(String message, ClientContext ctx) {
        if (ctx.playerName != null) {
            String chatMsg = "playerChat " + ctx.playerName + " " + message.substring(5);
            ctx.clientGroup.broadcastToRoom(ctx.currentRoomId, ctx.clientAddr, ctx.port, chatMsg);
        }
    }
}