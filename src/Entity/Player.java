package Entity;

import main.GamePanel;
import network.NetworkManager;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * REFACTORED Player - Uses PlayerMovement component
 * EXACT same behavior as working version
 */
public class Player extends Entity {

    // ═══════════════════════════════════════════════════════════
    // CORE COMPONENTS
    // ═══════════════════════════════════════════════════════════
    
    private final GamePanel gp;
    public final PlayerMovement movement;
    private final PlayerNetworkSync networkSync;
    public final PlayerRenderer renderer;

    public int spriteX;
    public int spriteY;
    
    // ═══════════════════════════════════════════════════════════
    // PLAYER DATA
    // ═══════════════════════════════════════════════════════════
    
    public String name;
    public Gender gender;
    public int credits;
    public List<Message> messages = new ArrayList<>();
    
    // Typing indicator
    public boolean isTyping = false;
    private long lastTypingTime = 0;
    private static final long TYPING_TIMEOUT = 3000;
    
 
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public Player(GamePanel gp,  String name, Gender gender) {
        this.gp = gp;
        this.name = name;
        this.gender = gender;
        this.credits = 56;
        
        // Initialize components
        this.movement = new PlayerMovement(gp);
        this.networkSync = new PlayerNetworkSync();
        this.renderer = new PlayerRenderer(gp);
        
        // Set initial state
        this.direction = Direction.DIAGONALE_DOWN;
        movement.setPosition(0, 0);
        
        // Update sprite position
       // ✅ DO THIS INSTEAD:
        spriteX = 0;
        spriteY = 0;
    }
    
    // ═══════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════
    
    public void setNetworkManager(NetworkManager nm) {
        networkSync.setNetworkManager(nm);
    }
    
    public void moveTo(int targetCol, int targetRow) {
        movement.moveTo(targetCol, targetRow);
    }
    
    /**
     * Update - EXACT same logic as working Player.java
     */
    public void update() {
        // Update movement and get direction if we moved
        Direction newDirection = movement.updateMovement();
        
        // If we moved, update direction AND animate
        if (newDirection != null) {
            this.direction = newDirection;
            
            // Animate sprite (EXACT same as working version)
            SpriteCounter++;
            if (SpriteCounter > 10) {
                SpriteNum = (SpriteNum == 1) ? 2 : 1;
                SpriteCounter = 0;
            }
        }
        
        // Update sprite position (EXACT same as working version)
        spriteX = renderer.getSpriteX(movement.xCurrent, movement.yCurrent);
        spriteY = renderer.getSpriteY(movement.xCurrent, movement.yCurrent);
        
        // Update typing status
        updateTypingStatus();
        
        // Send network update
        networkSync.sendUpdate(
            movement.xCurrent, 
            movement.yCurrent, 
            direction, 
            movement.in_movement
        );
    }
    
    public void draw(Graphics2D g2d) {
        renderer.draw(g2d, 
            movement.xCurrent, 
            movement.yCurrent, 
            direction, 
            movement.in_movement,
            SpriteNum,
            isTyping
        );
    }

    public void updateSpritePosition() {
    spriteX = renderer.getSpriteX(movement.xCurrent, movement.yCurrent);
    spriteY = renderer.getSpriteY(movement.xCurrent, movement.yCurrent);
}
    
    public void faceDirection(Direction newDirection) {
        this.direction = newDirection;
        movement.stopMoving();
    }
    
    public Direction calculateDirectionToTarget(int targetX, int targetY) {
        int deltaX = targetX - movement.xCurrent;
        int deltaY = targetY - movement.yCurrent;
        
        // If already at the same position
        if (deltaX == 0 && deltaY == 0) {
            return direction;
        }
        
        // When on same ISO_X axis (deltaX == 0), use ISO_Y directions
        if (deltaX == 0) {
            return (deltaY > 0) ? Direction.ISO_Y_DOWN : Direction.ISO_Y_UP;
        } 
        // When on same ISO_Y axis (deltaY == 0), use ISO_X directions
        else if (deltaY == 0) {
            return (deltaX > 0) ? Direction.ISO_X_RIGHT : Direction.ISO_X_LEFT;
        }
        // Diagonal detection with tolerance
        else {
            double ratio = (double) Math.abs(deltaX) / Math.abs(deltaY);
            boolean isDiagonalish = (ratio >= 0.7 && ratio <= 1.4);
            
            if (isDiagonalish) {
                if (deltaX > 0 && deltaY > 0) {
                    return Direction.DIAGONALE_DOWN;
                } else if (deltaX < 0 && deltaY < 0) {
                    return Direction.DIAGONALE_UP;
                } else if (deltaX > 0 && deltaY < 0) {
                    return Direction.RIGHT;
                } else if (deltaX < 0 && deltaY > 0) {
                    return Direction.LEFT;
                }
            }
            
            // Not diagonal enough - use ISO directions
            if (deltaX > 0 && deltaY > 0) {
                return (Math.abs(deltaX) > Math.abs(deltaY)) ? Direction.DIAGONALE_DOWN : Direction.DIAGONALE_UP;
            } else if (deltaX > 0 && deltaY < 0) {
                return (Math.abs(deltaX) > Math.abs(deltaY)) ? Direction.RIGHT : Direction.LEFT;
            } else if (deltaX < 0 && deltaY > 0) {
                return (Math.abs(deltaX) > Math.abs(deltaY)) ? Direction.LEFT : Direction.RIGHT;
            } else {
                return (Math.abs(deltaX) > Math.abs(deltaY)) ? Direction.DIAGONALE_UP : Direction.DIAGONALE_DOWN;
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // TYPING INDICATOR
    // ═══════════════════════════════════════════════════════════
    
    public void setTyping(boolean typing) {
        this.isTyping = typing;
        if (typing) {
            lastTypingTime = System.currentTimeMillis();
        }
    }
    
    private void updateTypingStatus() {
        if (isTyping && System.currentTimeMillis() - lastTypingTime > TYPING_TIMEOUT) {
            isTyping = false;
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════
    
    public int getCredits() {
        return credits;
    }
    
    public boolean contains(int mouseX, int mouseY) {
        return renderer.contains(
            mouseX, mouseY, 
            movement.xCurrent, 
            movement.yCurrent
        );
    }
    
    // ═══════════════════════════════════════════════════════════
    // MESSAGE CLASS
    // ═══════════════════════════════════════════════════════════
    
    public static class Message {
        public String text;
        public int y;
        public int adjustedY;

        public Message(String text, int y) {
            this.text = text;
            this.y = y;
        }
    }
}