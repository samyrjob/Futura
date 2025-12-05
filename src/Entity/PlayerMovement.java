package Entity;

import main.GamePanel;
import pathfinding.PathFinder;
import pathfinding.PathFinder.Node;
import java.util.List;

/**
 * PlayerMovement - Handles all movement and pathfinding logic
 * 
 * Responsibilities:
 * - A* pathfinding
 * - Tile-by-tile movement
 * - Position tracking
 * - Movement state management
 * 
 * Single Responsibility: MOVEMENT
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
    private int xPrevious, yPrevious;  // For direction calculation
    
    // Movement state
    private boolean isMoving;
    private long lastStepTime = 0;
    private static final long STEP_DURATION = 250_000_000; // 250ms per tile
    
    public PlayerMovement(GamePanel gp) {
        this.gp = gp;
        this.pathFinder = new PathFinder(gp.maxWorldCol, gp.maxWorldRow);
        this.isMoving = false;
    }
    
    /**
     * Set initial position
     */
    public void setPosition(int x, int y) {
        this.xCurrent = x;
        this.yCurrent = y;
        this.xPrevious = x;
        this.yPrevious = y;
    }
    
    /**
     * Start moving to a destination (Habbo-style pathfinding)
     */
    public void moveTo(int targetCol, int targetRow) {
        currentPath = pathFinder.findPath(xCurrent, yCurrent, targetCol, targetRow);
        
        if (currentPath != null && !currentPath.isEmpty()) {
            pathIndex = 0;
            isMoving = true;
            lastStepTime = System.nanoTime();
            System.out.println("Path found with " + currentPath.size() + " steps");
        } else {
            System.out.println("No path found!");
            isMoving = false;
        }
    }
    
    /**
     * Update movement - called every frame
     */
    public void update() {
        if (isMoving && currentPath != null && pathIndex < currentPath.size()) {
            long currentTime = System.nanoTime();
            
            // Time to move to next tile?
            if (currentTime - lastStepTime >= STEP_DURATION) {
                // Store previous position (for direction calculation)
                xPrevious = xCurrent;
                yPrevious = yCurrent;
                
                // Move to next tile in path
                Node nextNode = currentPath.get(pathIndex);
                xCurrent = nextNode.col;
                yCurrent = nextNode.row;
                
                pathIndex++;
                lastStepTime = currentTime;
                
                // Reached destination?
                if (pathIndex >= currentPath.size()) {
                    stopMoving();
                }
            }
        } else {
            isMoving = false;
        }
    }
    
    /**
     * Stop current movement
     */
    public void stopMoving() {
        isMoving = false;
        currentPath = null;
        pathIndex = 0;
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
    
    public int getPreviousX() {
        return xPrevious;
    }
    
    public int getPreviousY() {
        return yPrevious;
    }
    
    public boolean isMoving() {
        return isMoving;
    }
}  
