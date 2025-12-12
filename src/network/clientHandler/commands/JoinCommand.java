// package network.clientHandler.commands;

// import java.util.StringTokenizer;

// import network.ClientInfo;
// import network.clientHandler.ClientContext;
// import network.clientHandler.GameCommand;

// // Individual command classes (can be in separate files)
// public class JoinCommand implements GameCommand {
    
//     @Override
//     public void execute(String message, ClientContext ctx) {
//         StringTokenizer st = new StringTokenizer(message);
//         st.nextToken(); // skip "join"
        
//         ctx.playerName = st.nextToken();
//         String gender = st.nextToken();
//         int mapX = Integer.parseInt(st.nextToken());
//         int mapY = Integer.parseInt(st.nextToken());
//         String direction = st.nextToken();
        
//         if (st.hasMoreTokens()) {
//             ctx.currentRoomId = st.nextToken();
//         }
        
//         ClientInfo clientInfo = new ClientInfo(
//             ctx.out, ctx.clientAddr, ctx.port, ctx.playerName, 
//             gender, mapX, mapY, direction, ctx.currentRoomId
//         );
//         ctx.clientGroup.addClient(clientInfo);
        
//         String joinMsg = "playerJoined " + ctx.playerName + " " + gender + " " + 
//                         mapX + " " + mapY + " " + direction;
//         ctx.clientGroup.broadcastToRoom(ctx.currentRoomId, ctx.clientAddr, ctx.port, joinMsg);
        
//         ctx.clientGroup.broadcastToRoom(ctx.currentRoomId, ctx.clientAddr, ctx.port, 
//                                         "wantDetails " + ctx.clientAddr + " " + ctx.port);
        
//         System.out.println(ctx.playerName + " joined room: " + ctx.currentRoomId);
//     }
// }






package network.clientHandler.commands;

import java.util.List;
import java.util.StringTokenizer;

import network.ClientInfo;
import network.clientHandler.ClientContext;
import network.clientHandler.GameCommand;

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
        
        // ═══════════════════════════════════════════════════════════
        // STEP 1: Send EXISTING players to the NEW player FIRST
        // ═══════════════════════════════════════════════════════════
        List<ClientInfo> existingPlayers = ctx.clientGroup.getClientsInRoom(ctx.currentRoomId);
        
        for (ClientInfo existing : existingPlayers) {
            // Send each existing player's info to the new player
            String existingPlayerMsg = "playerJoined " + existing.playerName + " " + 
                                       existing.gender + " " + existing.mapX + " " + 
                                       existing.mapY + " " + existing.direction;
            ctx.out.println(existingPlayerMsg);
            
            System.out.println("  → Sent existing player to new: " + existing.playerName);
        }
        
        // ═══════════════════════════════════════════════════════════
        // STEP 2: Add the new player to the group
        // ═══════════════════════════════════════════════════════════
        ClientInfo clientInfo = new ClientInfo(
            ctx.out, ctx.clientAddr, ctx.port, ctx.playerName, 
            gender, mapX, mapY, direction, ctx.currentRoomId
        );
        ctx.clientGroup.addClient(clientInfo);
        
        // ═══════════════════════════════════════════════════════════
        // STEP 3: Broadcast NEW player to EXISTING players
        // ═══════════════════════════════════════════════════════════
        String joinMsg = "playerJoined " + ctx.playerName + " " + gender + " " + 
                        mapX + " " + mapY + " " + direction;
        ctx.clientGroup.broadcastToRoom(ctx.currentRoomId, ctx.clientAddr, ctx.port, joinMsg);
        
        System.out.println(ctx.playerName + " joined room: " + ctx.currentRoomId + 
                          " (sent " + existingPlayers.size() + " existing players)");
    }
}