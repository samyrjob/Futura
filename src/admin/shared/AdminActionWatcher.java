package admin.shared;

import network.ClientInfo;
import network.GameServerGroup;
import main.GameConstants;


import java.util.List;

/**
 * Watches the admin_actions.dat file for pending commands
 * Runs as a background thread in the Game Server
 */
public class AdminActionWatcher extends Thread {
    
    private static final long POLL_INTERVAL_MS = 1000;  // Check every second
    private static final long CLEANUP_INTERVAL_MS = 60000;  // Cleanup every minute
    private static final long MAX_ACTION_AGE_MS = 300000;   // Remove executed actions after 5 minutes
    
    private final GameServerGroup clientGroup;
    private volatile boolean running = true;
    private long lastCleanup = System.currentTimeMillis();
    
    public AdminActionWatcher(GameServerGroup clientGroup) {
        this.clientGroup = clientGroup;
        this.setName("AdminActionWatcher");
        this.setDaemon(true);
    }
    
    @Override
    public void run() {
        System.out.println("[ACTION WATCHER] Started watching admin_actions.dat");
        
        while (running) {
            try {
                // Check for pending actions
                List<AdminAction> pendingActions = AdminActionFile.readPendingActions();
                
                for (AdminAction action : pendingActions) {
                    executeAction(action);
                }
                
                // Periodic cleanup
                if (System.currentTimeMillis() - lastCleanup > CLEANUP_INTERVAL_MS) {
                    AdminActionFile.cleanupOldActions(MAX_ACTION_AGE_MS);
                    lastCleanup = System.currentTimeMillis();
                }
                
                // Wait before next poll
                Thread.sleep(POLL_INTERVAL_MS);
                
            } catch (InterruptedException e) {
                System.out.println("[ACTION WATCHER] Interrupted, stopping...");
                running = false;
            } catch (Exception e) {
                System.err.println("[ACTION WATCHER] Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("[ACTION WATCHER] Stopped");
    }
    
    // ═══════════════════════════════════════════════════════════
    // ACTION EXECUTION
    // ═══════════════════════════════════════════════════════════
    
    private void executeAction(AdminAction action) {
        System.out.println("[ACTION WATCHER] Executing: " + action);
        
        switch (action.getType()) {
            case KICK:
                executeKick(action);
                break;
            case MOVE_PLAYER:
                executeMovePlayer(action);
                break;
        }
        
        // Mark as executed
        AdminActionFile.markActionExecuted(action.getActionId());
    }
    
    private void executeKick(AdminAction action) {
        String username = action.getTargetUsername();
        String reason = action.getReason();
        
        ClientInfo target = clientGroup.getClientByName(username);
        
        if (target == null) {
            System.out.println("[ACTION WATCHER] KICK failed - player not found: " + username);
            return;
        }
        
        String oldRoom = target.currentRoomId;
        
        // Send kick message to player (client will move to lobby)
        target.sendMessage("KICKED " + reason);
        
        // Notify old room that player left
        clientGroup.broadcastToRoom(
            oldRoom,
            target.address,
            target.port,
            "playerLeft " + username
        );
        
        // ✨ UPDATE: Change player's room to lobby on server side (don't remove them)
        target.currentRoomId = GameConstants.LOBBY_ROOM_ID;
        
        // Reset position
        target.mapX = 4;
        target.mapY = 2;
        
        // Notify lobby that player joined
        clientGroup.broadcastToRoom(
            GameConstants.LOBBY_ROOM_ID,
            target.address,
            target.port,
            "playerJoined " + username + " " + target.gender + " " + 
            target.mapX + " " + target.mapY + " " + target.direction
        );
        
        System.out.println("[ACTION WATCHER] KICKED player: " + username + 
                        " (Reason: " + reason + ") - Moved to lobby");
    }
    
    private void executeMovePlayer(AdminAction action) {
        String username = action.getTargetUsername();
        String targetRoomId = action.getTargetRoomId();
        
        ClientInfo target = clientGroup.getClientByName(username);
        
        if (target == null) {
            System.out.println("[ACTION WATCHER] MOVE failed - player not found: " + username);
            return;
        }
        
        String oldRoom = target.currentRoomId;
        
        // Notify old room that player left
        clientGroup.broadcastToRoom(
            oldRoom,
            target.address,
            target.port,
            "playerLeft " + username
        );
        
        // Update player's room
        target.currentRoomId = targetRoomId;
        
        // Tell player to change rooms
        target.sendMessage("forceRoomChange " + targetRoomId);
        
        // Notify new room that player joined
        clientGroup.broadcastToRoom(
            targetRoomId,
            target.address,
            target.port,
            "playerJoined " + username + " " + target.gender + " " + 
            target.mapX + " " + target.mapY + " " + target.direction
        );
        
        System.out.println("[ACTION WATCHER] MOVED player: " + username + " (" + oldRoom + " → " + targetRoomId + ")");
    }
    
    // ═══════════════════════════════════════════════════════════
    // CONTROL
    // ═══════════════════════════════════════════════════════════
    
    public void shutdown() {
        running = false;
        this.interrupt();
    }
}