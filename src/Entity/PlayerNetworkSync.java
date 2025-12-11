package Entity;

import Entity.Entity.Direction;
import network.NetworkManager;

/**
 * PlayerNetworkSync - Handles network synchronization
 * 
 * Responsibilities:
 * - Send position updates to server
 * - Track previous state to avoid redundant updates
 * - Manage network connection
 * 
 * Single Responsibility: NETWORK COMMUNICATION
 */
public class PlayerNetworkSync {
    
    private NetworkManager networkManager;
    
    // Previous state (to detect changes)
    private int prevMapX = -1;
    private int prevMapY = -1;
    private Entity.Direction prevDirection = null;
    private boolean prevInMovement = false;
    
    /**
     * Set the network manager
     */
    public void setNetworkManager(NetworkManager nm) {
        this.networkManager = nm;
    }
    
    /**
     * Send update to server (only if state changed)
     */
    public void sendUpdate(int currentX, int currentY, Direction direction, boolean isMoving) {
        if (networkManager == null || !networkManager.isConnected()) {
            return;
        }
        
        // Only send if something actually changed
        if (hasStateChanged(currentX, currentY, direction, isMoving)) {
            networkManager.sendMoveMessage(currentX, currentY, direction.toString(), isMoving);
            
            // Update previous state
            prevMapX = currentX;
            prevMapY = currentY;
            prevDirection = direction;
            prevInMovement = isMoving;
        }
    }
    
    /**
     * Check if state has changed since last update
     */
    private boolean hasStateChanged(int x, int y, Direction dir, boolean moving) {
        return x != prevMapX || 
               y != prevMapY || 
               dir != prevDirection || 
               moving != prevInMovement;
    }
    
    /**
     * Reset tracking (useful when reconnecting)
     */
    public void reset() {
        prevMapX = -1;
        prevMapY = -1;
        prevDirection = null;
        prevInMovement = false;
    }
}
