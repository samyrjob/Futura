package network.clientHandler.commands;

import java.util.StringTokenizer;

import network.ClientInfo;
import network.clientHandler.ClientContext;
import network.clientHandler.GameCommand;

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






