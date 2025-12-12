package network.clientHandler.commands;

import java.util.List;
import java.util.StringTokenizer;

import network.ClientInfo;
import network.clientHandler.ClientContext;
import network.clientHandler.GameCommand;

public class JoinCommand implements GameCommand {
    
    // ✨ Habbo-style spawn position (corner) for NEW players only
    private static final int SPAWN_X = 0;
    private static final int SPAWN_Y = 0;
    
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
        
        // ═══════════════════════════════════════════════════════════
        // STEP 1: Send EXISTING players to NEW player at ACTUAL position
        // ═══════════════════════════════════════════════════════════
        List<ClientInfo> existingPlayers = ctx.clientGroup.getClientsInRoom(ctx.currentRoomId);
        
        for (ClientInfo existing : existingPlayers) {
            // ✨ Show existing players at their REAL position
            String existingPlayerMsg = "playerJoined " + existing.playerName + " " + 
                                       existing.gender + " " + existing.mapX + " " + 
                                       existing.mapY + " " + existing.direction;
            ctx.out.println(existingPlayerMsg);
            System.out.println("  → Sent existing player at ACTUAL pos: " + existing.playerName + 
                             " (" + existing.mapX + "," + existing.mapY + ")");
        }
        
        // ═══════════════════════════════════════════════════════════
        // STEP 2: Add the new player to the group (at corner)
        // ═══════════════════════════════════════════════════════════
        ClientInfo clientInfo = new ClientInfo(
            ctx.out, ctx.clientAddr, ctx.port, ctx.playerName, 
            gender, SPAWN_X, SPAWN_Y, direction, ctx.currentRoomId  // ✨ Store at corner
        );
        ctx.clientGroup.addClient(clientInfo);
        
        // ═══════════════════════════════════════════════════════════
        // STEP 3: Broadcast NEW player to EXISTING players (at corner - Habbo style!)
        // ═══════════════════════════════════════════════════════════
        String joinMsg = "playerJoined " + ctx.playerName + " " + gender + " " + 
                        SPAWN_X + " " + SPAWN_Y + " " + direction;  // ✨ NEW player at corner
        ctx.clientGroup.broadcastToRoom(ctx.currentRoomId, ctx.clientAddr, ctx.port, joinMsg);
        
        System.out.println(ctx.playerName + " joined room: " + ctx.currentRoomId + 
                          " (appears at corner to others)");
    }
}