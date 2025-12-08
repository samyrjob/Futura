package admin.shared;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents an admin action to be executed via shared file
 */
public class AdminAction implements Serializable {
    
    public enum ActionType {
        KICK,
        MOVE_PLAYER
    }
    
    private String actionId;
    private ActionType type;
    private String targetUsername;
    private String targetRoomId;      // Only for MOVE_PLAYER
    private String reason;            // Only for KICK
    private long timestamp;
    private boolean executed;
    
    // ═══════════════════════════════════════════════════════════
    // PRIVATE CONSTRUCTOR - Use factory methods instead
    // ═══════════════════════════════════════════════════════════
    
    private AdminAction(ActionType type, String targetUsername, String targetRoomId, String reason) {
        this.actionId = UUID.randomUUID().toString().substring(0, 8);
        this.type = type;
        this.targetUsername = targetUsername;
        this.targetRoomId = targetRoomId;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
        this.executed = false;
    }
    
    // ═══════════════════════════════════════════════════════════
    // FACTORY METHODS - Use these to create actions
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Create a KICK action
     */
    public static AdminAction createKick(String targetUsername, String reason) {
        return new AdminAction(
            ActionType.KICK,
            targetUsername,
            null,
            reason != null ? reason : "Kicked by admin"
        );
    }
    
    /**
     * Create a MOVE_PLAYER action
     */
    public static AdminAction createMove(String targetUsername, String targetRoomId) {
        return new AdminAction(
            ActionType.MOVE_PLAYER,
            targetUsername,
            targetRoomId,
            null
        );
    }
    
    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════
    
    public String getActionId() {
        return actionId;
    }
    
    public ActionType getType() {
        return type;
    }
    
    public String getTargetUsername() {
        return targetUsername;
    }
    
    public String getTargetRoomId() {
        return targetRoomId;
    }
    
    public String getReason() {
        return reason;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public boolean isExecuted() {
        return executed;
    }
    
    public void markExecuted() {
        this.executed = true;
    }
    
    // ═══════════════════════════════════════════════════════════
    // SERIALIZATION
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Serialize action to string for file storage
     * Format: actionId|type|username|roomId|reason|timestamp|executed
     */
    public String serialize() {
        return String.join("|",
            actionId,
            type.name(),
            targetUsername,
            targetRoomId != null ? targetRoomId : "",
            reason != null ? reason : "",
            String.valueOf(timestamp),
            String.valueOf(executed)
        );
    }
    
    /**
     * Deserialize action from string
     */
    public static AdminAction deserialize(String data) {
        String[] parts = data.split("\\|", -1);  // -1 to keep empty strings
        
        if (parts.length < 7) {
            throw new IllegalArgumentException("Invalid action data: " + data);
        }
        
        String actionId = parts[0];
        ActionType type = ActionType.valueOf(parts[1]);
        String username = parts[2];
        String roomId = parts[3].isEmpty() ? null : parts[3];
        String reason = parts[4].isEmpty() ? null : parts[4];
        long timestamp = Long.parseLong(parts[5]);
        boolean executed = Boolean.parseBoolean(parts[6]);
        
        // Create action using factory method
        AdminAction action;
        if (type == ActionType.KICK) {
            action = AdminAction.createKick(username, reason);
        } else {
            action = AdminAction.createMove(username, roomId);
        }
        
        // Restore original values
        action.actionId = actionId;
        action.timestamp = timestamp;
        action.executed = executed;
        
        return action;
    }
    
    @Override
    public String toString() {
        if (type == ActionType.KICK) {
            return "[" + actionId + "] KICK " + targetUsername + " (" + reason + ")";
        } else {
            return "[" + actionId + "] MOVE " + targetUsername + " → " + targetRoomId;
        }
    }
}