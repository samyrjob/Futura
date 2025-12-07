package network.clientHandler.commands;



import java.util.StringTokenizer;
import network.ClientInfo;
import network.clientHandler.ClientContext;
import network.clientHandler.GameCommand;

public class WantDetailsCommand implements GameCommand {
    
    @Override
    public void execute(String message, ClientContext ctx) {
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "wantDetails"
        String requesterAddr = st.nextToken();
        int requesterPort = Integer.parseInt(st.nextToken());
        
        // Only send details if we're in the same room as requester
        ClientInfo requester = ctx.clientGroup.getClient(requesterAddr, requesterPort);
        if (requester != null && requester.currentRoomId.equals(ctx.currentRoomId)) {
            ClientInfo myInfo = ctx.clientGroup.getClient(ctx.clientAddr, ctx.port);
            if (myInfo != null) {
                ctx.clientGroup.sendTo(requesterAddr, requesterPort,
                    "detailsFor " + ctx.playerName + " " + myInfo.gender + " " +
                    myInfo.mapX + " " + myInfo.mapY + " " + myInfo.direction);
            }
        }
    }
}
