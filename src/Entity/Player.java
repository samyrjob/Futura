package Entity;

import main.GamePanel;
import mouse.MyMouseAdapter;
import network.NetworkManager;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * REFACTORED Player class using COMPOSITION pattern
 * 
 * Each responsibility is delegated to a specialized component:
 * - Movement & Pathfinding → PlayerMovement
 * - Direction calculations → DirectionManager  
 * - Network synchronization → PlayerNetworkSync
 * - Sprite rendering → PlayerRenderer
 * - Animation → PlayerAnimator
 * 
 * This follows the Single Responsibility Principle (SRP)
 */
public class Player extends Entity {

    // ═══════════════════════════════════════════════════════════
    // CORE COMPONENTS (Composition - "has-a" relationships)
    // ═══════════════════════════════════════════════════════════
    
    private final GamePanel gp;
    public final PlayerMovement movement;        // Handles pathfinding & movement
    private final DirectionManager directionMgr;  // Handles direction calculations
    private final PlayerNetworkSync networkSync;  // Handles network updates
    public final PlayerRenderer renderer;        // Handles drawing
    private final PlayerAnimator animator;        // Handles sprite animation


       // ✨ ADD THESE BACK (for backwards compatibility):
    public int spriteX;
    public int spriteY;
    
    // ═══════════════════════════════════════════════════════════
    // PLAYER DATA (Simple state - no logic)
    // ═══════════════════════════════════════════════════════════
    
    public String name;
    public Gender gender;
    public int credits;
    public List<Message> messages = new ArrayList<>();
    
    // Typing indicator
    public boolean isTyping = false;
    private long lastTypingTime = 0;
    private static final long TYPING_TIMEOUT = 3000;
    
    // Input
    public MyMouseAdapter myMouseAdapter;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public Player(GamePanel gp, MyMouseAdapter myMouseAdapter, String name, Gender gender) {
        this.gp = gp;
        this.name = name;
        this.gender = gender;
        this.myMouseAdapter = myMouseAdapter;
        this.credits = 56;
        
        // Initialize all components (Dependency Injection pattern)
        this.movement = new PlayerMovement(gp);
        this.directionMgr = new DirectionManager();
        this.networkSync = new PlayerNetworkSync();
        this.animator = new PlayerAnimator();
        this.renderer = new PlayerRenderer(gp, animator);
        
        // Set initial state
        this.direction = Direction.DIAGONALE_DOWN;
        movement.setPosition(0, 0);  // Start at (0,0)
    }
    
    // ═══════════════════════════════════════════════════════════
    // PUBLIC API (Delegates to components)
    // ═══════════════════════════════════════════════════════════
    
    public void setNetworkManager(NetworkManager nm) {
        networkSync.setNetworkManager(nm);
    }
    
    public void moveTo(int targetCol, int targetRow) {
        movement.moveTo(targetCol, targetRow);
    }
    
    public void update() {
        // Update movement (pathfinding)
        movement.update();
        
        // Update direction based on movement
        if (movement.isMoving()) {
            Direction newDirection = directionMgr.calculateMovementDirection(
                movement.getPreviousX(), movement.getPreviousY(),
                movement.getCurrentX(), movement.getCurrentY()
            );
            if (newDirection != null) {
                this.direction = newDirection;
            }
        }
        
        // Update animation
        animator.update(movement.isMoving());
            // ✨ ADD THIS: Update sprite position (for GamePanel to use)
        this.spriteX = getSpriteX();
        this.spriteY = getSpriteY();
        
        // Update typing status
        updateTypingStatus();
        
        // Send network update
        networkSync.sendUpdate(
            movement.getCurrentX(), 
            movement.getCurrentY(), 
            direction, 
            movement.isMoving()
        );
    }
    
    public void draw(Graphics2D g2d) {
        // Delegate all rendering to renderer
        renderer.draw(g2d, 
            movement.getCurrentX(), 
            movement.getCurrentY(), 
            direction, 
            movement.isMoving(),
            animator.getCurrentFrame(),
            isTyping
        );
    }
    
    public void faceDirection(Direction newDirection) {
        this.direction = newDirection;
        movement.stopMoving();
    }
    
    public Direction calculateDirectionToTarget(int targetX, int targetY) {
        return directionMgr.calculateDirectionToTarget(
            movement.getCurrentX(), 
            movement.getCurrentY(), 
            targetX, 
            targetY
        );
    }
    
    // ═══════════════════════════════════════════════════════════
    // TYPING INDICATOR (Could be extracted to component if needed)
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
    
    public int getCurrentX() {
        return movement.getCurrentX();
    }
    
    public int getCurrentY() {
        return movement.getCurrentY();
    }
    
    public int getSpriteX() {
        return renderer.getSpriteX(movement.getCurrentX(), movement.getCurrentY());
    }
    
    public int getSpriteY() {
        return renderer.getSpriteY(movement.getCurrentX(), movement.getCurrentY());
    }
    
    public boolean contains(int mouseX, int mouseY) {
        return renderer.contains(
            mouseX, mouseY, 
            movement.getCurrentX(), 
            movement.getCurrentY()
        );
    }
    
    // ═══════════════════════════════════════════════════════════
    // MESSAGE CLASS (Inner class - related to Player)
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