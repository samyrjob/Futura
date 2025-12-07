package network.clientHandler;



import java.util.StringTokenizer;
import network.ClientInfo;

public class ChangeRoomCommand implements GameCommand {
    
    @Override
    public void execute(String message, ClientContext ctx) {
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "changeRoom"
        String newRoomId = st.nextToken();
        
        // Notify old room that player left
        String leaveMsg = "playerLeft " + ctx.playerName;
        ctx.clientGroup.broadcastToRoom(ctx.currentRoomId, ctx.clientAddr, ctx.port, leaveMsg);
        
        // Update current room
        String oldRoomId = ctx.currentRoomId;
        ctx.currentRoomId = newRoomId;
        
        // Update client info
        ClientInfo clientInfo = ctx.clientGroup.getClient(ctx.clientAddr, ctx.port);
        if (clientInfo != null) {
            clientInfo.currentRoomId = newRoomId;
            
            // Notify new room that player joined
            String joinMsg = "playerJoined " + ctx.playerName + " " + clientInfo.gender + " " +
                            clientInfo.mapX + " " + clientInfo.mapY + " " + clientInfo.direction;
            ctx.clientGroup.broadcastToRoom(newRoomId, ctx.clientAddr, ctx.port, joinMsg);
            
            // Request details from players in new room
            ctx.clientGroup.broadcastToRoom(newRoomId, ctx.clientAddr, ctx.port,
                                            "wantDetails " + ctx.clientAddr + " " + ctx.port);
        }
        
        System.out.println(ctx.playerName + " changed from room '" + oldRoomId + 
                          "' to room '" + newRoomId + "'");
    }
}