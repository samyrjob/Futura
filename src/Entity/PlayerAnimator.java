package Entity;

/**
 * PlayerAnimator - Handles sprite animation
 * 
 * Responsibilities:
 * - Animate sprite frames
 * - Track animation state
 * - Provide current frame number
 * 
 * Single Responsibility: ANIMATION
 */
public class PlayerAnimator {
    
    private int spriteNum = 1;           // Current frame (1 or 2)
    private int spriteCounter = 0;       // Counter for frame timing
    private static final int ANIMATION_SPEED = 10; // Frames before switching
    
    /**
     * Update animation (called every frame)
     * @param isMoving - whether player is currently moving
     */
    public void update(boolean isMoving) {
        if (isMoving) {
            spriteCounter++;
            if (spriteCounter > ANIMATION_SPEED) {
                // Toggle between frame 1 and 2
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        } else {
            // Reset to frame 1 when standing still
            spriteNum = 1;
            spriteCounter = 0;
        }
    }
    
    /**
     * Get current animation frame (1 or 2)
     */
    public int getCurrentFrame() {
        return spriteNum;
    }
    
    /**
     * Reset animation to initial state
     */
    public void reset() {
        spriteNum = 1;
        spriteCounter = 0;
    }
}