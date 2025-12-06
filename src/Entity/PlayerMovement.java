// package Entity;

// import main.GamePanel;
// import pathfinding.PathFinder;
// import pathfinding.PathFinder.Node;
// import java.util.List;

// /**
//  * PlayerMovement - Handles all movement and pathfinding logic
//  * 
//  * Responsibilities:
//  * - A* pathfinding
//  * - Tile-by-tile movement
//  * - Position tracking
//  * - Movement state management
//  * 
//  * Single Responsibility: MOVEMENT
//  */
// public class PlayerMovement {
    
//     private final GamePanel gp;
//     private final PathFinder pathFinder;
    
//     // Path data
//     private List<Node> currentPath;
//     private int pathIndex = 0;
    
//     // Position
//     public int xCurrent;
//     public int yCurrent;
//     private int xPrevious, yPrevious;  // For direction calculation
    
//     // Movement state
//     private boolean isMoving;
//     private long lastStepTime = 0;
//     private static final long STEP_DURATION = 250_000_000; // 250ms per tile
    
//     public PlayerMovement(GamePanel gp) {
//         this.gp = gp;
//         this.pathFinder = new PathFinder(gp.maxWorldCol, gp.maxWorldRow);
//         this.isMoving = false;
//     }
    
//     /**
//      * Set initial position
//      */
//     public void setPosition(int x, int y) {
//         this.xCurrent = x;
//         this.yCurrent = y;
//         this.xPrevious = x;
//         this.yPrevious = y;
//     }
    
//     /**
//      * Start moving to a destination (Habbo-style pathfinding)
//      */
//     public void moveTo(int targetCol, int targetRow) {
//         currentPath = pathFinder.findPath(xCurrent, yCurrent, targetCol, targetRow);
        
//         if (currentPath != null && !currentPath.isEmpty()) {
//             pathIndex = 0;
//             isMoving = true;
//             lastStepTime = System.nanoTime();
//             System.out.println("Path found with " + currentPath.size() + " steps");
//         } else {
//             System.out.println("No path found!");
//             isMoving = false;
//         }
//     }
    
//     /**
//      * Update movement - called every frame
//      */
//     public void update() {
//         if (isMoving && currentPath != null && pathIndex < currentPath.size()) {
//             long currentTime = System.nanoTime();
            
//             // Time to move to next tile?
//             if (currentTime - lastStepTime >= STEP_DURATION) {
//                 // Store previous position (for direction calculation)
//                 xPrevious = xCurrent;
//                 yPrevious = yCurrent;
                
//                 // Move to next tile in path
//                 Node nextNode = currentPath.get(pathIndex);
//                 xCurrent = nextNode.col;
//                 yCurrent = nextNode.row;
                
//                 pathIndex++;
//                 lastStepTime = currentTime;
                
//                 // Reached destination?
//                 if (pathIndex >= currentPath.size()) {
//                     stopMoving();
//                 }
//             }
//         } else {
//             isMoving = false;
//         }
//     }
    
//     /**
//      * Stop current movement
//      */
//     public void stopMoving() {
//         isMoving = false;
//         currentPath = null;
//         pathIndex = 0;
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // GETTERS
//     // ═══════════════════════════════════════════════════════════
    
//     public int getCurrentX() {
//         return xCurrent;
//     }
    
//     public int getCurrentY() {
//         return yCurrent;
//     }
    
//     public int getPreviousX() {
//         return xPrevious;
//     }
    
//     public int getPreviousY() {
//         return yPrevious;
//     }
    
//     public boolean isMoving() {
//         return isMoving;
//     }
// }  

package Entity;

import main.GamePanel;
import pathfinding.PathFinder;
import pathfinding.PathFinder.Node;
import java.util.List;

/**
 * PlayerMovement - Handles pathfinding and movement
 * EXACT same logic as working Player.java
 */
public class PlayerMovement {
    
    private final GamePanel gp;
    private final PathFinder pathFinder;
    
    // Path data
    private List<Node> currentPath;
    private int pathIndex = 0;
    
    // Position
    public int xCurrent;
    public int yCurrent;
    
    // Movement state
    public boolean in_movement;
    private long lastStepTime = 0;
    private static final long STEP_DURATION = 250_000_000; // 250ms per tile
    
    public PlayerMovement(GamePanel gp) {
        this.gp = gp;
        this.pathFinder = new PathFinder(gp.maxWorldCol, gp.maxWorldRow);
        this.in_movement = false;
    }
    
    /**
     * Set initial position
     */
    public void setPosition(int x, int y) {
        this.xCurrent = x;
        this.yCurrent = y;
    }
    
    /**
     * Start moving to a destination
     */
    public void moveTo(int targetCol, int targetRow) {
        currentPath = pathFinder.findPath(xCurrent, yCurrent, targetCol, targetRow);
        
        if (currentPath != null && !currentPath.isEmpty()) {
            pathIndex = 0;
            in_movement = true;
            lastStepTime = System.nanoTime();
            System.out.println("Path found with " + currentPath.size() + " steps");
        } else {
            System.out.println("No path found!");
            in_movement = false;
        }
    }
    
    /**
     * Update movement - EXACT same logic as working Player.java
     * Returns the direction for this step, or null if not moving
     */
    public Entity.Direction updateMovement() {
        if (in_movement && currentPath != null && pathIndex < currentPath.size()) {
            long currentTime = System.nanoTime();
            
            // Time to move to next tile?
            if (currentTime - lastStepTime >= STEP_DURATION) {
                // Move to next tile in path
                Node nextNode = currentPath.get(pathIndex);
                
                // Determine direction for this step (BEFORE moving!)
                Entity.Direction newDirection = determineDirection(xCurrent, yCurrent, nextNode.col, nextNode.row);
                
                // Move to the tile
                xCurrent = nextNode.col;
                yCurrent = nextNode.row;
                
                pathIndex++;
                lastStepTime = currentTime;
                
                // Reached destination?
                if (pathIndex >= currentPath.size()) {
                    in_movement = false;
                    currentPath = null;
                    pathIndex = 0;
                }
                
                return newDirection;
            }
        } else {
            in_movement = false;
        }
        
        return null;
    }
    
    /**
     * Stop current movement
     */
    public void stopMoving() {
        in_movement = false;
        currentPath = null;
        pathIndex = 0;
    }
    
    /**
     * Determine direction - EXACT same as working Player.java
     */
    private Entity.Direction determineDirection(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        
        // Diagonal movements
        if (dx == -1 && dy == -1) {
            return Entity.Direction.DIAGONALE_UP;
        } else if (dx == 1 && dy == 1) {
            return Entity.Direction.DIAGONALE_DOWN;
        }
        // Straight movements
        else if (dx == -1 && dy == 0) {
            return Entity.Direction.ISO_X_LEFT;
        } else if (dx == 1 && dy == 0) {
            return Entity.Direction.ISO_X_RIGHT;
        } else if (dx == 0 && dy == -1) {
            return Entity.Direction.ISO_Y_UP;
        } else if (dx == 0 && dy == 1) {
            return Entity.Direction.ISO_Y_DOWN;
        }
        // Mixed diagonal movements
        else if (dx == -1 && dy == 1) {
            return Entity.Direction.LEFT;
        } else if (dx == 1 && dy == -1) {
            return Entity.Direction.RIGHT;
        }
        
        return null;
    }
    
    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════
    
    public int getCurrentX() {
        return xCurrent;
    }
    
    public int getCurrentY() {
        return yCurrent;
    }
    
    public boolean isMoving() {
        return in_movement;
    }
}