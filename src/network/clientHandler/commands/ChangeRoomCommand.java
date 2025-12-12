package network.clientHandler.commands;

import java.util.List;
import java.util.StringTokenizer;
import network.ClientInfo;
import network.clientHandler.ClientContext;
import network.clientHandler.GameCommand;

public class ChangeRoomCommand implements GameCommand {
    
    @Override
    public void execute(String message, ClientContext ctx) {
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "changeRoom"
        String newRoomId = st.nextToken();
        
        String oldRoomId = ctx.currentRoomId;
        
        // ═══════════════════════════════════════════════════════════
        // STEP 1: Notify OLD room that player left
        // ═══════════════════════════════════════════════════════════
        String leaveMsg = "playerLeft " + ctx.playerName;
        ctx.clientGroup.broadcastToRoom(oldRoomId, ctx.clientAddr, ctx.port, leaveMsg);
        System.out.println("  → Told old room '" + oldRoomId + "': " + leaveMsg);
        
        // ═══════════════════════════════════════════════════════════
        // STEP 2: Send EXISTING players in NEW room to this player
        // ═══════════════════════════════════════════════════════════
        List<ClientInfo> existingPlayers = ctx.clientGroup.getClientsInRoom(newRoomId);
        
        for (ClientInfo existing : existingPlayers) {
            String existingPlayerMsg = "playerJoined " + existing.playerName + " " + 
                                       existing.gender + " " + existing.mapX + " " + 
                                       existing.mapY + " " + existing.direction;
            ctx.out.println(existingPlayerMsg);
            System.out.println("  → Sent existing player to newcomer: " + existing.playerName);
        }
        
        // ═══════════════════════════════════════════════════════════
        // STEP 3: Update this player's room ID
        // ═══════════════════════════════════════════════════════════
        ctx.currentRoomId = newRoomId;
        
        ClientInfo clientInfo = ctx.clientGroup.getClient(ctx.clientAddr, ctx.port);
        if (clientInfo != null) {
            clientInfo.currentRoomId = newRoomId;
            
            // ═══════════════════════════════════════════════════════════
            // STEP 4: Notify NEW room that this player joined
            // ═══════════════════════════════════════════════════════════
            String joinMsg = "playerJoined " + ctx.playerName + " " + clientInfo.gender + " " +
                            clientInfo.mapX + " " + clientInfo.mapY + " " + clientInfo.direction;
            ctx.clientGroup.broadcastToRoom(newRoomId, ctx.clientAddr, ctx.port, joinMsg);
            System.out.println("  → Told new room '" + newRoomId + "': " + joinMsg);
        }
        
        System.out.println("✅ " + ctx.playerName + " changed: '" + oldRoomId + "' → '" + newRoomId + "'");
    }
}