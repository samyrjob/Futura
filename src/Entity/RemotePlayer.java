package Entity;

import main.GamePanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class RemotePlayer extends Entity {
    
    private final GamePanel gp;
    public BufferedImage currentSprite;
    
    public int spriteX;
    public int spriteY;
    
    // Movement tracking
    public long moveStartTime;
    public int xInitial, yInitial;
    public int xFinal, yFinal;
    public int xCurrent, yCurrent;
    public boolean in_movement;
    
    // Player info
    public String name;
    public Gender gender;

    // ✨ ADD THIS - Message list for this remote player
    public java.util.List<Message> messages = new java.util.ArrayList<>();
    
    public RemotePlayer(GamePanel gp, String name, Gender gender, int mapX, int mapY, String directionStr) {
        this.gp = gp;
        this.name = name;
        this.gender = gender;
        
        this.xCurrent = mapX;
        this.yCurrent = mapY;
        this.xInitial = mapX;
        this.yInitial = mapY;
        this.xFinal = mapX;
        this.yFinal = mapY;
        
        this.in_movement = false;
        
        // Set direction from string
        try {
            this.direction = Direction.valueOf(directionStr);
        } catch (IllegalArgumentException e) {
            this.direction = Direction.DIAGONALE_DOWN;
        }
        
        loadPlayerImage();
        updateSpritePosition();
        updateCurrentSprite();
    }
    
    private void loadPlayerImage() {
        try {
            // Load all sprite images (same as local player)
            playerImageDiagonaleUp1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-back-mov-1.png"));
            playerImageDiagonaleUp2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-back-mov-2.png"));
            playerImageDiagonaleUp = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-back.png"));
            
            playerImageDiagonaleDown1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-front-mov-1.png"));
            playerImageDiagonaleDown2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-front-mov-2.png"));
            playerImageDiagonaleDown = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-front.png"));
            
            playerImageLeft1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-left-mov-1.png"));
            playerImageLeft2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-left-mov-2.png"));
            playerImageLeft = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-left.png"));
            
            playerImageRight1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-right-mov-1.png"));
            playerImageRight2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-right-mov-2.png"));
            playerImageRight = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-right.png"));
            
            playerImageIsoXLeft1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoxleft-mov-2.png"));
            playerImageIsoXLeft2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoxleft-mov-1.png"));
            playerImageIsoXLeft = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoxleft.png"));
            
            playerImageIsoXRight1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoxright-mov-2.png"));
            playerImageIsoXRight2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoxright-mov-1.png"));
            playerImageIsoXRight = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoxright.png"));
            
            playerImageIsoYUp1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoyup-mov-2.png"));
            playerImageIsoYUp2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoyup-mov-1.png"));
            playerImageIsoYUp = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoyup.png"));
            
            playerImageIsoYDown1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoydown-mov-1.png"));
            playerImageIsoYDown2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoydown-mov-2.png"));
            playerImageIsoYDown = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoydown.png"));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void updatePosition(int mapX, int mapY, String directionStr, boolean inMovement) {
        this.xCurrent = mapX;
        this.yCurrent = mapY;
        this.in_movement = inMovement;
        
        try {
            this.direction = Direction.valueOf(directionStr);
        } catch (IllegalArgumentException e) {
            // Keep current direction if invalid
        }
        
        updateSpritePosition();
    }
    
    private void updateSpritePosition() {
        int tileCenterX = conversion_from_mapXY_to_tilecenterX(xCurrent, yCurrent);
        int tileCenterY = conversion_from_mapXY_to_tilecenterY(xCurrent, yCurrent);
        
        spriteX = tileCenterX - gp.tileSizeWidth;
        spriteY = tileCenterY - (3 * gp.tileSizeHeight);
    }
    
    private int conversion_from_mapXY_to_tilecenterX(int mapX, int mapY) {
        return (mapX - mapY) * (gp.tileSizeWidth / 2) + gp.tile_manager.xOffset + (gp.tileSizeWidth / 2);
    }
    
    private int conversion_from_mapXY_to_tilecenterY(int mapX, int mapY) {
        return (mapX + mapY) * (gp.tileSizeHeight / 2) + gp.tile_manager.yOffset + (gp.tileSizeHeight / 2);
    }
    
    public void update() {
        if (in_movement) {
            SpriteCounter++;
            if (SpriteCounter > 10) {
                SpriteNum = (SpriteNum == 1) ? 2 : 1;
                SpriteCounter = 0;
            }
        }
        updateCurrentSprite();
    }
    
    private void updateCurrentSprite() {
        if (in_movement) {
            switch (direction) {
                case DIAGONALE_UP:
                    currentSprite = (SpriteNum == 1) ? playerImageDiagonaleUp1 : playerImageDiagonaleUp2;
                    break;
                case ISO_Y_UP:
                    currentSprite = (SpriteNum == 1) ? playerImageIsoYUp1 : playerImageIsoYUp2;
                    break;
                case ISO_Y_DOWN:
                    currentSprite = (SpriteNum == 1) ? playerImageIsoYDown1 : playerImageIsoYDown2;
                    break;
                case ISO_X_RIGHT:
                    currentSprite = (SpriteNum == 1) ? playerImageIsoXRight1 : playerImageIsoXRight2;
                    break;
                case DIAGONALE_DOWN:
                    currentSprite = (SpriteNum == 1) ? playerImageDiagonaleDown1 : playerImageDiagonaleDown2;
                    break;
                case ISO_X_LEFT:
                    currentSprite = (SpriteNum == 1) ? playerImageIsoXLeft1 : playerImageIsoXLeft2;
                    break;
                case LEFT:
                    currentSprite = (SpriteNum == 1) ? playerImageLeft1 : playerImageLeft2;
                    break;
                case RIGHT:
                    currentSprite = (SpriteNum == 1) ? playerImageRight1 : playerImageRight2;
                    break;
            }
        } else {
            switch (direction) {
                case DIAGONALE_UP:
                    currentSprite = playerImageDiagonaleUp;
                    break;
                case DIAGONALE_DOWN:
                    currentSprite = playerImageDiagonaleDown;
                    break;
                case ISO_Y_UP:
                    currentSprite = playerImageIsoYUp;
                    break;
                case ISO_Y_DOWN:
                    currentSprite = playerImageIsoYDown;
                    break;
                case ISO_X_LEFT:
                    currentSprite = playerImageIsoXLeft;
                    break;
                case ISO_X_RIGHT:
                    currentSprite = playerImageIsoXRight;
                    break;
                case RIGHT:
                    currentSprite = playerImageRight;
                    break;
                case LEFT:
                    currentSprite = playerImageLeft;
                    break;
            }
        }
    }
    
    public void draw(Graphics2D g2d) {
        if (currentSprite != null) {
            g2d.drawImage(currentSprite, spriteX, spriteY, 2 * gp.tileSizeWidth, 4 * gp.tileSizeHeight, null);
            
            // Draw player name above sprite
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            int nameWidth = fm.stringWidth(name);
            g2d.drawString(name, spriteX + gp.tileSizeWidth - (nameWidth / 2), spriteY - 5);
        }
    }


      // ✨ ADD THIS - Message class for remote player
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
