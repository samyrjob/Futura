package network.clientHandler;

import java.io.IOException;
import java.util.StringTokenizer;

import network.ClientInfo;

// Individual command classes (can be in separate files)
public class JoinCommand implements GameCommand {
    
    @Override
    public void execute(String message, ClientContext ctx) {
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "join"
        
        ctx.playerName = st.nextToken();
        String gender = st.nextToken();
        int mapX = Integer.parseInt(st.nextToken());
        int mapY = Integer.parseInt(st.nextToken());
        String direction = st.nextToken();
        
        if (st.hasMoreTokens()) {
            ctx.currentRoomId = st.nextToken();
        }
        
        ClientInfo clientInfo = new ClientInfo(
            ctx.out, ctx.clientAddr, ctx.port, ctx.playerName, 
            gender, mapX, mapY, direction, ctx.currentRoomId
        );
        ctx.clientGroup.addClient(clientInfo);
        
        String joinMsg = "playerJoined " + ctx.playerName + " " + gender + " " + 
                        mapX + " " + mapY + " " + direction;
        ctx.clientGroup.broadcastToRoom(ctx.currentRoomId, ctx.clientAddr, ctx.port, joinMsg);
        
        ctx.clientGroup.broadcastToRoom(ctx.currentRoomId, ctx.clientAddr, ctx.port, 
                                        "wantDetails " + ctx.clientAddr + " " + ctx.port);
        
        System.out.println(ctx.playerName + " joined room: " + ctx.currentRoomId);
    }
}

class MoveCommand implements GameCommand {
    
    @Override
    public void execute(String message, ClientContext ctx) {
        if (ctx.playerName != null) {
            String moveMsg = "playerMoved " + ctx.playerName + " " + message.substring(5);
            ctx.clientGroup.broadcastToRoom(ctx.currentRoomId, ctx.clientAddr, ctx.port, moveMsg);
        }
    }
}

class ChatCommand implements GameCommand {
    
    @Override
    public void execute(String message, ClientContext ctx) {
        if (ctx.playerName != null) {
            String chatMsg = "playerChat " + ctx.playerName + " " + message.substring(5);
            ctx.clientGroup.broadcastToRoom(ctx.currentRoomId, ctx.clientAddr, ctx.port, chatMsg);
        }
    }
}

class ByeCommand implements GameCommand {
    
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


