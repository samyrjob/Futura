package network.clientHandler.commands;


import java.util.StringTokenizer;
import network.clientHandler.ClientContext;
import network.clientHandler.GameCommand;

public class LeaveRoomCommand implements GameCommand {
    
    @Override
    public void execute(String message, ClientContext ctx) {
        // Format: leaveRoom <roomId>
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "leaveRoom"
        String roomId = st.nextToken();
        
        String leaveMsg = "playerLeft " + ctx.playerName;
        ctx.clientGroup.broadcastToRoom(roomId, ctx.clientAddr, ctx.port, leaveMsg);
        
        System.out.println(ctx.playerName + " left room: " + roomId);
    }
}
