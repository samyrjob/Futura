package Entity;
import Entity.Entity.Direction;

/**
 * DirectionManager - Handles all direction calculations
 * 
 * Responsibilities:
 * - Calculate direction from movement vector
 * - Calculate direction to target
 * - Direction utilities
 * 
 * Single Responsibility: DIRECTION LOGIC
 */
public class DirectionManager {
    
    /**
     * Determine direction based on movement from (x1,y1) to (x2,y2)
     * Returns null if no movement occurred
     */
    public Direction calculateMovementDirection(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        
        // No movement
        if (dx == 0 && dy == 0) {
            return null;
        }
        
        // Diagonal movements
        if (dx == -1 && dy == -1) {
            return Direction.DIAGONALE_UP;
        } else if (dx == 1 && dy == 1) {
            return Direction.DIAGONALE_DOWN;
        }
        // Straight movements
        else if (dx == -1 && dy == 0) {
            return Direction.ISO_X_LEFT;
        } else if (dx == 1 && dy == 0) {
            return Direction.ISO_X_RIGHT;
        } else if (dx == 0 && dy == -1) {
            return Direction.ISO_Y_UP;
        } else if (dx == 0 && dy == 1) {
            return Direction.ISO_Y_DOWN;
        }
        // Mixed diagonal movements
        else if (dx == -1 && dy == 1) {
            return Direction.LEFT;
        } else if (dx == 1 && dy == -1) {
            return Direction.RIGHT;
        }
        
        return null;
    }
    
    /**
     * Calculate which direction to face to look at target position
     */
    public Direction calculateDirectionToTarget(int currentX, int currentY, 
                                               int targetX, int targetY) {
        int deltaX = targetX - currentX;
        int deltaY = targetY - currentY;
        
        // Already at target
        if (deltaX == 0 && deltaY == 0) {
            return null; // Keep current direction
        }
        
        // On same ISO_X axis (deltaX == 0), use ISO_Y directions
        if (deltaX == 0) {
            return (deltaY > 0) ? Direction.ISO_Y_DOWN : Direction.ISO_Y_UP;
        } 
        // On same ISO_Y axis (deltaY == 0), use ISO_X directions
        else if (deltaY == 0) {
            return (deltaX > 0) ? Direction.ISO_X_RIGHT : Direction.ISO_X_LEFT;
        }
        // Diagonal detection with tolerance
        else {
            return calculateDiagonalDirection(deltaX, deltaY);
        }
    }
    
    /**
     * Calculate diagonal direction with tolerance
     */
    private Direction calculateDiagonalDirection(int deltaX, int deltaY) {
        // Calculate the ratio to see if it's close to diagonal
        double ratio = (double) Math.abs(deltaX) / Math.abs(deltaY);
        
        // If ratio is between 0.7 and 1.4, consider it diagonal
        boolean isDiagonalish = (ratio >= 0.7 && ratio <= 1.4);
        
        if (isDiagonalish) {
            return getExactDiagonalDirection(deltaX, deltaY);
        } else {
            return getApproximateDiagonalDirection(deltaX, deltaY);
        }
    }
    
    /**
     * Get exact diagonal direction
     */
    private Direction getExactDiagonalDirection(int deltaX, int deltaY) {
        // Bottom-right quadrant → DIAGONALE_DOWN
        if (deltaX > 0 && deltaY > 0) {
            return Direction.DIAGONALE_DOWN;
        }
        // Top-left quadrant → DIAGONALE_UP
        else if (deltaX < 0 && deltaY < 0) {
            return Direction.DIAGONALE_UP;
        }
        // Top-right quadrant → RIGHT
        else if (deltaX > 0 && deltaY < 0) {
            return Direction.RIGHT;
        }
        // Bottom-left quadrant → LEFT
        else {
            return Direction.LEFT;
        }
    }
    
    /**
     * Get approximate direction when not perfectly diagonal
     */
    private Direction getApproximateDiagonalDirection(int deltaX, int deltaY) {
        // Bottom-right quadrant
        if (deltaX > 0 && deltaY > 0) {
            return (Math.abs(deltaX) > Math.abs(deltaY)) ? 
                   Direction.DIAGONALE_DOWN : Direction.DIAGONALE_UP;
        } 
        // Top-right quadrant
        else if (deltaX > 0 && deltaY < 0) {
            return (Math.abs(deltaX) > Math.abs(deltaY)) ? 
                   Direction.RIGHT : Direction.LEFT;
        } 
        // Bottom-left quadrant
        else if (deltaX < 0 && deltaY > 0) {
            return (Math.abs(deltaX) > Math.abs(deltaY)) ? 
                   Direction.LEFT : Direction.RIGHT;
        } 
        // Top-left quadrant
        else {
            return (Math.abs(deltaX) > Math.abs(deltaY)) ? 
                   Direction.DIAGONALE_UP : Direction.DIAGONALE_DOWN;
        }
    }
}


