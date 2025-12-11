package admin.shared;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reading/writing admin actions to shared file
 * This is the bridge between Admin Server and Game Server
 */
public class AdminActionFile {
    
    private static final String DATA_DIR = "data/";
    private static final String FILE_PATH = DATA_DIR + "admin_actions.dat";
    private static final Object FILE_LOCK = new Object();
    
    // ═══════════════════════════════════════════════════════════
    // WRITE OPERATIONS (Used by Admin Server)
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Add a new action to the file
     */
    public static void addAction(AdminAction action) {
        synchronized (FILE_LOCK) {
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(FILE_PATH, true))) {  // append mode
                
                writer.write(action.serialize());
                writer.newLine();
                
                System.out.println("[ADMIN FILE] Added action: " + action);
                
            } catch (IOException e) {
                System.err.println("[ADMIN FILE] Failed to write action: " + e.getMessage());
            }
        }
    }
    
/**
 * Write a KICK action
 */
public static String writeKickAction(String username, String reason) {
    AdminAction action = AdminAction.createKick(username, reason);  // ← Changed
    addAction(action);
    return action.getActionId();
}

/**
 * Write a MOVE_PLAYER action
 */
public static String writeMoveAction(String username, String targetRoomId) {
    AdminAction action = AdminAction.createMove(username, targetRoomId);  // ← Changed
    addAction(action);
    return action.getActionId();
}
    
    // ═══════════════════════════════════════════════════════════
    // READ OPERATIONS (Used by Game Server)
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Read all pending (unexecuted) actions from the file
     */
    public static List<AdminAction> readPendingActions() {
        List<AdminAction> actions = new ArrayList<>();
        
        synchronized (FILE_LOCK) {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                return actions;
            }
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    
                    try {
                        AdminAction action = AdminAction.deserialize(line);
                        if (!action.isExecuted()) {
                            actions.add(action);
                        }
                    } catch (Exception e) {
                        System.err.println("[ADMIN FILE] Failed to parse action: " + line);
                    }
                }
            } catch (IOException e) {
                System.err.println("[ADMIN FILE] Failed to read actions: " + e.getMessage());
            }
        }
        
        return actions;
    }
    
    /**
     * Mark an action as executed and update the file
     */
    public static void markActionExecuted(String actionId) {
        synchronized (FILE_LOCK) {
            List<AdminAction> allActions = readAllActions();
            
            for (AdminAction action : allActions) {
                if (action.getActionId().equals(actionId)) {
                    action.markExecuted();
                    break;
                }
            }
            
            // Rewrite file with updated actions
            writeAllActions(allActions);
        }
    }
    
    /**
     * Remove executed actions older than specified age (cleanup)
     */
    public static void cleanupOldActions(long maxAgeMillis) {
        synchronized (FILE_LOCK) {
            List<AdminAction> allActions = readAllActions();
            long now = System.currentTimeMillis();
            
            allActions.removeIf(action -> 
                action.isExecuted() && (now - action.getTimestamp()) > maxAgeMillis
            );
            
            writeAllActions(allActions);
            System.out.println("[ADMIN FILE] Cleanup complete. Remaining actions: " + allActions.size());
        }
    }
    
    /**
     * Clear all actions from file
     */
    public static void clearAllActions() {
        synchronized (FILE_LOCK) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
                // Write empty file
                System.out.println("[ADMIN FILE] Cleared all actions");
            } catch (IOException e) {
                System.err.println("[ADMIN FILE] Failed to clear actions: " + e.getMessage());
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════
    
    private static List<AdminAction> readAllActions() {
        List<AdminAction> actions = new ArrayList<>();
        
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return actions;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                try {
                    actions.add(AdminAction.deserialize(line));
                } catch (Exception e) {
                    // Skip invalid lines
                }
            }
        } catch (IOException e) {
            System.err.println("[ADMIN FILE] Read error: " + e.getMessage());
        }
        
        return actions;
    }
    
    private static void writeAllActions(List<AdminAction> actions) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (AdminAction action : actions) {
                writer.write(action.serialize());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[ADMIN FILE] Write error: " + e.getMessage());
        }
    }
}