package network.clientHandler.commands;

import java.util.List;
import java.util.StringTokenizer;
import network.ClientInfo;
import network.clientHandler.ClientContext;
import network.clientHandler.GameCommand;

public class ChangeRoomCommand implements GameCommand {
    
    // ✨ Habbo-style spawn position (corner) for NEW players only
    private static final int SPAWN_X = 0;
    private static final int SPAWN_Y = 0;
    
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
        
        // ═══════════════════════════════════════════════════════════
        // STEP 2: Send EXISTING players in NEW room at ACTUAL position
        // ═══════════════════════════════════════════════════════════
        List<ClientInfo> existingPlayers = ctx.clientGroup.getClientsInRoom(newRoomId);
        
        for (ClientInfo existing : existingPlayers) {
            // ✨ Show existing players at their REAL position
            String existingPlayerMsg = "playerJoined " + existing.playerName + " " + 
                                       existing.gender + " " + existing.mapX + " " + 
                                       existing.mapY + " " + existing.direction;
            ctx.out.println(existingPlayerMsg);
        }
        
        // ═══════════════════════════════════════════════════════════
        // STEP 3: Update this player's room ID and position (corner)
        // ═══════════════════════════════════════════════════════════
        ctx.currentRoomId = newRoomId;
        
        ClientInfo clientInfo = ctx.clientGroup.getClient(ctx.clientAddr, ctx.port);
        if (clientInfo != null) {
            clientInfo.currentRoomId = newRoomId;
            clientInfo.mapX = SPAWN_X;  // ✨ Reset to corner
            clientInfo.mapY = SPAWN_Y;  // ✨ Reset to corner
            
            // ═══════════════════════════════════════════════════════════
            // STEP 4: Notify NEW room - THIS player at corner (Habbo style!)
            // ═══════════════════════════════════════════════════════════
            String joinMsg = "playerJoined " + ctx.playerName + " " + clientInfo.gender + " " +
                            SPAWN_X + " " + SPAWN_Y + " " + clientInfo.direction;  // ✨ At corner
            ctx.clientGroup.broadcastToRoom(newRoomId, ctx.clientAddr, ctx.port, joinMsg);
        }
        
        System.out.println("✅ " + ctx.playerName + " changed: '" + oldRoomId + 
                          "' → '" + newRoomId + "' (appears at corner to others)");
    }
}