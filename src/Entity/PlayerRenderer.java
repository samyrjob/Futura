package Entity;

import main.GamePanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * PlayerRenderer - Handles all rendering logic
 * 
 * Responsibilities:
 * - Load sprite images
 * - Select correct sprite based on state
 * - Draw player sprite
 * - Draw typing indicator
 * - Coordinate conversion (map → screen)
 * - Hitbox detection
 * 
 * Single Responsibility: RENDERING
 */
public class PlayerRenderer extends Entity {
    
    private final GamePanel gp;
    
    public PlayerRenderer(GamePanel gp) {
        this.gp = gp;
        loadSprites();
    }
    
    // ═══════════════════════════════════════════════════════════
    // SPRITE LOADING
    // ═══════════════════════════════════════════════════════════
    
    private void loadSprites() {
        try {
            // Diagonal up/down
            playerImageDiagonaleUp1 = loadImage("/res/player/sprite-back-mov-1.png");
            playerImageDiagonaleUp2 = loadImage("/res/player/sprite-back-mov-2.png");
            playerImageDiagonaleUp = loadImage("/res/player/sprite-back.png");
            
            playerImageDiagonaleDown1 = loadImage("/res/player/sprite-front-mov-1.png");
            playerImageDiagonaleDown2 = loadImage("/res/player/sprite-front-mov-2.png");
            playerImageDiagonaleDown = loadImage("/res/player/sprite-front.png");
            
            // Left/Right
            playerImageLeft1 = loadImage("/res/player/sprite-left-mov-1.png");
            playerImageLeft2 = loadImage("/res/player/sprite-left-mov-2.png");
            playerImageLeft = loadImage("/res/player/sprite-left.png");
            
            playerImageRight1 = loadImage("/res/player/sprite-right-mov-1.png");
            playerImageRight2 = loadImage("/res/player/sprite-right-mov-2.png");
            playerImageRight = loadImage("/res/player/sprite-right.png");
            
            // Isometric X
            playerImageIsoXLeft1 = loadImage("/res/player/sprite-isoxleft-mov-2.png");
            playerImageIsoXLeft2 = loadImage("/res/player/sprite-isoxleft-mov-1.png");
            playerImageIsoXLeft = loadImage("/res/player/sprite-isoxleft.png");
            
            playerImageIsoXRight1 = loadImage("/res/player/sprite-isoxright-mov-2.png");
            playerImageIsoXRight2 = loadImage("/res/player/sprite-isoxright-mov-1.png");
            playerImageIsoXRight = loadImage("/res/player/sprite-isoxright.png");
            
            // Isometric Y
            playerImageIsoYUp1 = loadImage("/res/player/sprite-isoyup-mov-2.png");
            playerImageIsoYUp2 = loadImage("/res/player/sprite-isoyup-mov-1.png");
            playerImageIsoYUp = loadImage("/res/player/sprite-isoyup.png");
            
            playerImageIsoYDown1 = loadImage("/res/player/sprite-isoydown-mov-1.png");
            playerImageIsoYDown2 = loadImage("/res/player/sprite-isoydown-mov-2.png");
            playerImageIsoYDown = loadImage("/res/player/sprite-isoydown.png");
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading player sprites!");
        }
    }
    
    private BufferedImage loadImage(String path) throws IOException {
        return ImageIO.read(getClass().getResourceAsStream(path));
    }
    
    // ═══════════════════════════════════════════════════════════
    // SPRITE SELECTION
    // ═══════════════════════════════════════════════════════════
    
    private BufferedImage selectSprite(Direction direction, boolean isMoving, int frame) {
        if (isMoving) {
            return selectMovingSprite(direction, frame);
        } else {
            return selectStandingSprite(direction);
        }
    }
    
    private BufferedImage selectMovingSprite(Direction direction, int frame) {
        switch (direction) {
            case DIAGONALE_UP:
                return (frame == 1) ? playerImageDiagonaleUp1 : playerImageDiagonaleUp2;
            case ISO_Y_UP:
                return (frame == 1) ? playerImageIsoYUp1 : playerImageIsoYUp2;
            case ISO_Y_DOWN:
                return (frame == 1) ? playerImageIsoYDown1 : playerImageIsoYDown2;
            case ISO_X_RIGHT:
                return (frame == 1) ? playerImageIsoXRight1 : playerImageIsoXRight2;
            case DIAGONALE_DOWN:
                return (frame == 1) ? playerImageDiagonaleDown1 : playerImageDiagonaleDown2;
            case ISO_X_LEFT:
                return (frame == 1) ? playerImageIsoXLeft1 : playerImageIsoXLeft2;
            case LEFT:
                return (frame == 1) ? playerImageLeft1 : playerImageLeft2;
            case RIGHT:
                return (frame == 1) ? playerImageRight1 : playerImageRight2;
            default:
                return playerImageDiagonaleDown1;
        }
    }
    
    private BufferedImage selectStandingSprite(Direction direction) {
        switch (direction) {
            case DIAGONALE_UP:
                return playerImageDiagonaleUp;
            case DIAGONALE_DOWN:
                return playerImageDiagonaleDown;
            case ISO_Y_UP:
                return playerImageIsoYUp;
            case ISO_Y_DOWN:
                return playerImageIsoYDown;
            case ISO_X_LEFT:
                return playerImageIsoXLeft;
            case ISO_X_RIGHT:
                return playerImageIsoXRight;
            case RIGHT:
                return playerImageRight;
            case LEFT:
                return playerImageLeft;
            default:
                return playerImageDiagonaleDown;
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // COORDINATE CONVERSION
    // ═══════════════════════════════════════════════════════════
    
    public int getSpriteX(int mapX, int mapY) {
        return conversion_from_mapXY_to_tilecenterX(mapX, mapY) - gp.tileSizeWidth;
    }
    
    public int getSpriteY(int mapX, int mapY) {
        return conversion_from_mapXY_to_tilecenterY(mapX, mapY) - (3 * gp.tileSizeHeight);
    }
    
    private int conversion_from_mapXY_to_tilecenterX(int mapX, int mapY) {
        return (mapX - mapY) * (gp.tileSizeWidth / 2) + 
               gp.tile_manager.xOffset + (gp.tileSizeWidth / 2);
    }
    
    private int conversion_from_mapXY_to_tilecenterY(int mapX, int mapY) {
        return (mapX + mapY) * (gp.tileSizeHeight / 2) + 
               gp.tile_manager.yOffset + (gp.tileSizeHeight / 2);
    }
    
    // ═══════════════════════════════════════════════════════════
    // DRAWING
    // ═══════════════════════════════════════════════════════════
    
    public void draw(Graphics2D g2d, int mapX, int mapY, Direction direction, 
                     boolean isMoving, int animFrame, boolean isTyping) {
        // Calculate screen position
        int spriteX = getSpriteX(mapX, mapY);
        int spriteY = getSpriteY(mapX, mapY);
        
        // Select and draw sprite
        BufferedImage sprite = selectSprite(direction, isMoving, animFrame);
        if (sprite != null) {
            g2d.drawImage(sprite, spriteX, spriteY, 
                         2 * gp.tileSizeWidth, 4 * gp.tileSizeHeight, null);
        }
        
        // Draw typing indicator if typing
        if (isTyping) {
            drawTypingBubble(g2d, spriteX, spriteY);
        }
    }
    
    /**
     * Draw typing indicator bubble (Habbo Hotel style!)
     */
    private void drawTypingBubble(Graphics2D g2d, int spriteX, int spriteY) {
        // Calculate bubble position (above sprite)
        int bubbleX = spriteX + gp.tileSizeWidth - 25;
        int bubbleY = spriteY - 15;
        int bubbleWidth = 50;
        int bubbleHeight = 30;
        
        // Draw white bubble with border
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 15, 15);
        
        // Draw "..." text
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("...", bubbleX + 15, bubbleY + 20);
        
        // Draw small triangle pointer
        int[] xPoints = {bubbleX + 20, bubbleX + 25, bubbleX + 30};
        int[] yPoints = {bubbleY + bubbleHeight, bubbleY + bubbleHeight + 8, bubbleY + bubbleHeight};
        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(xPoints, yPoints, 3);
        g2d.setColor(Color.BLACK);
        g2d.drawPolyline(xPoints, yPoints, 3);
    }
    
    // ═══════════════════════════════════════════════════════════
    // HITBOX DETECTION
    // ═══════════════════════════════════════════════════════════
    
    public boolean contains(int mouseX, int mouseY, int mapX, int mapY) {
        int spriteX = getSpriteX(mapX, mapY);
        int spriteY = getSpriteY(mapX, mapY);
        
        int drawnWidth = 2 * gp.tileSizeWidth;
        int drawnHeight = 4 * gp.tileSizeHeight;
        
        // Make hitbox smaller (only character body)
        int hitboxWidth = (int)(drawnWidth * 0.4);
        int hitboxHeight = (int)(drawnHeight * 0.5);
        
        // Center horizontally, position at bottom
        int hitboxX = spriteX + (drawnWidth - hitboxWidth) / 2;
        int hitboxY = spriteY + drawnHeight - hitboxHeight;
        
        return (mouseX >= hitboxX && 
                mouseX <= hitboxX + hitboxWidth &&
                mouseY >= hitboxY && 
                mouseY <= hitboxY + hitboxHeight);
    }
}