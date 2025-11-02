// package Entity;

// import main.GamePanel;
// import mouse.MyMouseAdapter;
// import network.NetworkManager;
// import pathfinding.PathFinder;
// import pathfinding.PathFinder.Node;
// import javax.imageio.ImageIO;
// import java.awt.*;
// import java.awt.image.BufferedImage;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.List;

// public class Player extends Entity {

//     private final GamePanel gp;
//     public MyMouseAdapter myMouseAdapter;
//     private NetworkManager networkManager;

//     public BufferedImage currentSprite;

//     public int spriteX;
//     public int spriteY;

//     // PATHFINDING - Habbo Hotel Style
//     private PathFinder pathFinder;
//     private List<Node> currentPath;
//     private int pathIndex = 0;
//     private long lastStepTime = 0;
//     private static final long STEP_DURATION = 250_000_000; // 250ms per tile (Habbo-style speed)

//     // Position tracking
//     public int xCurrent, yCurrent;
//     public boolean in_movement;

//     // INFORMATION ABOUT THE PLAYER
//     public int credits;
//     public String name;
//     public Gender gender;
//     public List<Message> messages = new ArrayList<>();
    
//     // Network tracking
//     private int prevMapX = -1;
//     private int prevMapY = -1;
//     private Direction prevDirection = null;
//     private boolean prevInMovement = false;

//     public Player(GamePanel gp, MyMouseAdapter myMouseAdapter, String name, Gender gender) {
//         this.gp = gp;
//         this.name = name;
//         this.gender = gender;

//         // Initialize pathfinder
//         pathFinder = new PathFinder(gp.maxWorldCol, gp.maxWorldRow);

//         // Start at (0,0)
//         xCurrent = 0;
//         yCurrent = 0;

//         spriteX = conversion_from_mapXY_to_spriteX(xCurrent, yCurrent);
//         spriteY = conversion_from_mapXY_to_spriteY(xCurrent, yCurrent);

//         this.in_movement = false;
//         direction = Direction.DIAGONALE_DOWN;
//         this.myMouseAdapter = myMouseAdapter;

//         loadPlayerImage();
//         currentSprite = playerImageDiagonaleDown;

//         credits = 56;
//     }
    
//     public void setNetworkManager(NetworkManager nm) {
//         this.networkManager = nm;
//     }

//     // Load player sprites
//     private void loadPlayerImage() {
//         try {
//             playerImageDiagonaleUp1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-back-mov-1.png"));
//             playerImageDiagonaleUp2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-back-mov-2.png"));
//             playerImageDiagonaleUp = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-back.png"));
            
//             playerImageDiagonaleDown1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-front-mov-1.png"));
//             playerImageDiagonaleDown2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-front-mov-2.png"));
//             playerImageDiagonaleDown = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-front.png"));
            
//             playerImageLeft1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-left-mov-1.png"));
//             playerImageLeft2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-left-mov-2.png"));
//             playerImageLeft = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-left.png"));
            
//             playerImageRight1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-right-mov-1.png"));
//             playerImageRight2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-right-mov-2.png"));
//             playerImageRight = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-right.png"));

//             playerImageIsoXLeft1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoxleft-mov-2.png"));
//             playerImageIsoXLeft2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoxleft-mov-1.png"));
//             playerImageIsoXLeft = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoxleft.png"));

//             playerImageIsoXRight1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoxright-mov-2.png"));
//             playerImageIsoXRight2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoxright-mov-1.png"));
//             playerImageIsoXRight = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoxright.png"));

//             playerImageIsoYUp1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoyup-mov-2.png"));
//             playerImageIsoYUp2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoyup-mov-1.png"));
//             playerImageIsoYUp = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoyup.png"));

//             playerImageIsoYDown1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoydown-mov-1.png"));
//             playerImageIsoYDown2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoydown-mov-2.png"));
//             playerImageIsoYDown = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-isoydown.png"));
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }

//     // Coordinate conversions
//     int conversion_from_mapXY_to_tilecenterX(int mapX, int mapY){
//         return (mapX - mapY)*(gp.tileSizeWidth / 2) + gp.tile_manager.xOffset + (gp.tileSizeWidth / 2);
//     }
    
//     int conversion_from_mapXY_to_tilecenterY(int mapX, int mapY){
//         return (mapX + mapY)*(gp.tileSizeHeight/2)  + gp.tile_manager.yOffset + (gp.tileSizeHeight/ 2);
//     }

//     int conversion_from_mapXY_to_spriteX(int mapX, int mapY){
//         return conversion_from_mapXY_to_tilecenterX(mapX, mapY) - gp.tileSizeWidth;
//     }
    
//     int conversion_from_mapXY_to_spriteY(int mapX, int mapY){
//         return conversion_from_mapXY_to_tilecenterY(mapX, mapY) - (3*gp.tileSizeHeight);
//     }

//     public int getCredits(){
//         return credits;
//     }

//     /**
//      * Start moving to a destination (Habbo-style pathfinding)
//      */
//     public void moveTo(int targetCol, int targetRow) {
//         // Find path using A*
//         currentPath = pathFinder.findPath(xCurrent, yCurrent, targetCol, targetRow);
        
//         if (currentPath != null && !currentPath.isEmpty()) {
//             pathIndex = 0;
//             in_movement = true;
//             lastStepTime = System.nanoTime();
//             System.out.println("Path found with " + currentPath.size() + " steps");
//         } else {
//             System.out.println("No path found!");
//             in_movement = false;
//         }
//     }

//     /**
//      * Update player position - Habbo Hotel style (tile by tile)
//      */
//     public void update() {
//         if (in_movement && currentPath != null && pathIndex < currentPath.size()) {
//             long currentTime = System.nanoTime();
            
//             // Time to move to next tile?
//             if (currentTime - lastStepTime >= STEP_DURATION) {
//                 // Move to next tile in path
//                 Node nextNode = currentPath.get(pathIndex);
                
//                 // Determine direction for this step
//                 determineDirection(xCurrent, yCurrent, nextNode.col, nextNode.row);
                
//                 // Move to the tile
//                 xCurrent = nextNode.col;
//                 yCurrent = nextNode.row;
                
//                 pathIndex++;
//                 lastStepTime = currentTime;
                
//                 // Animate sprite
//                 SpriteCounter++;
//                 if (SpriteCounter > 10) {
//                     SpriteNum = (SpriteNum == 1) ? 2 : 1;
//                     SpriteCounter = 0;
//                 }
                
//                 // Reached destination?
//                 if (pathIndex >= currentPath.size()) {
//                     in_movement = false;
//                     currentPath = null;
//                     pathIndex = 0;
//                 }
//             }
//         } else {
//             in_movement = false;
//         }

//         // Update sprite position on screen
//         spriteX = conversion_from_mapXY_to_spriteX(xCurrent, yCurrent);
//         spriteY = conversion_from_mapXY_to_spriteY(xCurrent, yCurrent);

//         // Send network update
//         sendNetworkUpdate();
//     }

//     /**
//      * Determine direction based on movement from (x1,y1) to (x2,y2)
//      */
//     private void determineDirection(int x1, int y1, int x2, int y2) {
//         int dx = x2 - x1;
//         int dy = y2 - y1;
        
//         // Diagonal movements
//         if (dx == -1 && dy == -1) {
//             direction = Direction.DIAGONALE_UP;
//         } else if (dx == 1 && dy == 1) {
//             direction = Direction.DIAGONALE_DOWN;
//         }
//         // Straight movements
//         else if (dx == -1 && dy == 0) {
//             direction = Direction.ISO_X_LEFT;
//         } else if (dx == 1 && dy == 0) {
//             direction = Direction.ISO_X_RIGHT;
//         } else if (dx == 0 && dy == -1) {
//             direction = Direction.ISO_Y_UP;
//         } else if (dx == 0 && dy == 1) {
//             direction = Direction.ISO_Y_DOWN;
//         }
//         // Mixed diagonal movements
//         else if (dx == -1 && dy == 1) {
//             direction = Direction.LEFT;
//         } else if (dx == 1 && dy == -1) {
//             direction = Direction.RIGHT;
//         }
//     }
    
//     private void sendNetworkUpdate() {
//         if (networkManager != null && networkManager.isConnected()) {
//             if (xCurrent != prevMapX || yCurrent != prevMapY || 
//                 direction != prevDirection || in_movement != prevInMovement) {
                
//                 networkManager.sendMoveMessage(xCurrent, yCurrent, direction.toString(), in_movement);
                
//                 prevMapX = xCurrent;
//                 prevMapY = yCurrent;
//                 prevDirection = direction;
//                 prevInMovement = in_movement;
//             }
//         }
//     }

//     /**
//      * Draw player sprite
//      */
//     public void draw_player(Graphics2D g2d){
//         if (in_movement) {
//             switch (direction) {
//                 case DIAGONALE_UP:
//                     currentSprite = (SpriteNum == 1) ? playerImageDiagonaleUp1 : playerImageDiagonaleUp2;
//                     break;
//                 case ISO_Y_UP:
//                     currentSprite = (SpriteNum == 1) ? playerImageIsoYUp1 : playerImageIsoYUp2;
//                     break;
//                 case ISO_Y_DOWN:
//                     currentSprite = (SpriteNum == 1) ? playerImageIsoYDown1 : playerImageIsoYDown2;
//                     break;
//                 case ISO_X_RIGHT:
//                     currentSprite = (SpriteNum == 1) ? playerImageIsoXRight1 : playerImageIsoXRight2;
//                     break;
//                 case DIAGONALE_DOWN:
//                     currentSprite = (SpriteNum == 1) ? playerImageDiagonaleDown1 : playerImageDiagonaleDown2;
//                     break;
//                 case ISO_X_LEFT:
//                     currentSprite = (SpriteNum == 1) ? playerImageIsoXLeft1 : playerImageIsoXLeft2;
//                     break;
//                 case LEFT:
//                     currentSprite = (SpriteNum == 1) ? playerImageLeft1 : playerImageLeft2;
//                     break;
//                 case RIGHT:
//                     currentSprite = (SpriteNum == 1) ? playerImageRight1 : playerImageRight2;
//                     break;
//             }
//         } else {
//             switch (direction) {
//                 case DIAGONALE_UP:
//                     currentSprite = playerImageDiagonaleUp;
//                     break;
//                 case DIAGONALE_DOWN:
//                     currentSprite = playerImageDiagonaleDown;
//                     break;
//                 case ISO_Y_UP:
//                     currentSprite = playerImageIsoYUp;
//                     break;
//                 case ISO_Y_DOWN:
//                     currentSprite = playerImageIsoYDown;
//                     break;
//                 case ISO_X_LEFT:
//                     currentSprite = playerImageIsoXLeft;
//                     break;
//                 case ISO_X_RIGHT:
//                     currentSprite = playerImageIsoXRight;
//                     break;
//                 case RIGHT:
//                     currentSprite = playerImageRight;
//                     break;
//                 case LEFT:
//                     currentSprite = playerImageLeft;
//                     break;
//             }
//         }

//         if (currentSprite != null) {
//             g2d.drawImage(currentSprite, spriteX, spriteY, 2*gp.tileSizeWidth, 4*gp.tileSizeHeight, null);
//         }
//     }

//     public boolean contains(int mouseX, int mouseY) {
//         return mouseX >= spriteX &&
//                mouseX <= spriteX + currentSprite.getWidth() &&
//                mouseY >= spriteY &&
//                mouseY <= spriteY + currentSprite.getHeight();
//     }

//     //! For opening and closing the bubble of the player in In Player.java
//     // public boolean contains(int mouseX, int mouseY) {
//     //     // Use actual drawn size with smaller, precise hitbox
//     //     int drawnWidth = 2 * gp.tileSizeWidth;   // 192 pixels
//     //     int drawnHeight = 4 * gp.tileSizeHeight; // 192 pixels
        
//     //     // Smaller hitbox - only character body (40% width, 50% height)
//     //     int hitboxWidth = (int)(drawnWidth * 0.4);
//     //     int hitboxHeight = (int)(drawnHeight * 0.5);
        
//     //     // Center horizontally, position at bottom
//     //     int hitboxX = spriteX + (drawnWidth - hitboxWidth) / 2;
//     //     int hitboxY = spriteY + drawnHeight - hitboxHeight;
        
//     //     return mouseX >= hitboxX &&
//     //         mouseX <= hitboxX + hitboxWidth &&
//     //         mouseY >= hitboxY &&
//     //         mouseY <= hitboxY + hitboxHeight;
//     // }

//     // Message class
//     public static class Message {
//         public String text;
//         public int y;
//         public int adjustedY;

//         public Message(String text, int y) {
//             this.text = text;
//             this.y = y;
//         }
//     }
// }

package Entity;

import main.GamePanel;
import mouse.MyMouseAdapter;
import network.NetworkManager;
import pathfinding.PathFinder;
import pathfinding.PathFinder.Node;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {

    private final GamePanel gp;
    public MyMouseAdapter myMouseAdapter;
    private NetworkManager networkManager;

    public BufferedImage currentSprite;

    public int spriteX;
    public int spriteY;

    // PATHFINDING - Habbo Hotel Style
    private PathFinder pathFinder;
    private List<Node> currentPath;
    private int pathIndex = 0;
    private long lastStepTime = 0;
    private static final long STEP_DURATION = 250_000_000; // 250ms per tile (Habbo-style speed)

    // Position tracking
    public int xCurrent, yCurrent;
    public boolean in_movement;

    // INFORMATION ABOUT THE PLAYER
    public int credits;
    public String name;
    public Gender gender;
    public List<Message> messages = new ArrayList<>();
    
    // ✨ NEW: TYPING INDICATOR (Habbo Hotel style!)
    public boolean isTyping = false;
    private long lastTypingTime = 0;
    private static final long TYPING_TIMEOUT = 3000; // 3 seconds
    
    // Network tracking
    private int prevMapX = -1;
    private int prevMapY = -1;
    private Direction prevDirection = null;
    private boolean prevInMovement = false;

    public Player(GamePanel gp, MyMouseAdapter myMouseAdapter, String name, Gender gender) {
        this.gp = gp;
        this.name = name;
        this.gender = gender;

        // Initialize pathfinder
        pathFinder = new PathFinder(gp.maxWorldCol, gp.maxWorldRow);

        // Start at (0,0)
        xCurrent = 0;
        yCurrent = 0;

        spriteX = conversion_from_mapXY_to_spriteX(xCurrent, yCurrent);
        spriteY = conversion_from_mapXY_to_spriteY(xCurrent, yCurrent);

        this.in_movement = false;
        direction = Direction.DIAGONALE_DOWN;
        this.myMouseAdapter = myMouseAdapter;

        loadPlayerImage();
        currentSprite = playerImageDiagonaleDown;

        credits = 56;
    }
    
    public void setNetworkManager(NetworkManager nm) {
        this.networkManager = nm;
    }

    // Load player sprites
    private void loadPlayerImage() {
        try {
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

    // Coordinate conversions
    int conversion_from_mapXY_to_tilecenterX(int mapX, int mapY){
        return (mapX - mapY)*(gp.tileSizeWidth / 2) + gp.tile_manager.xOffset + (gp.tileSizeWidth / 2);
    }
    
    int conversion_from_mapXY_to_tilecenterY(int mapX, int mapY){
        return (mapX + mapY)*(gp.tileSizeHeight/2)  + gp.tile_manager.yOffset + (gp.tileSizeHeight/ 2);
    }

    int conversion_from_mapXY_to_spriteX(int mapX, int mapY){
        return conversion_from_mapXY_to_tilecenterX(mapX, mapY) - gp.tileSizeWidth;
    }
    
    int conversion_from_mapXY_to_spriteY(int mapX, int mapY){
        return conversion_from_mapXY_to_tilecenterY(mapX, mapY) - (3*gp.tileSizeHeight);
    }

    public int getCredits(){
        return credits;
    }

    /**
     * Start moving to a destination (Habbo-style pathfinding)
     */
    public void moveTo(int targetCol, int targetRow) {
        // Find path using A*
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
     * Update player position - Habbo Hotel style (tile by tile)
     */
    public void update() {
        if (in_movement && currentPath != null && pathIndex < currentPath.size()) {
            long currentTime = System.nanoTime();
            
            // Time to move to next tile?
            if (currentTime - lastStepTime >= STEP_DURATION) {
                // Move to next tile in path
                Node nextNode = currentPath.get(pathIndex);
                
                // Determine direction for this step
                determineDirection(xCurrent, yCurrent, nextNode.col, nextNode.row);
                
                // Move to the tile
                xCurrent = nextNode.col;
                yCurrent = nextNode.row;
                
                pathIndex++;
                lastStepTime = currentTime;
                
                // Animate sprite
                SpriteCounter++;
                if (SpriteCounter > 10) {
                    SpriteNum = (SpriteNum == 1) ? 2 : 1;
                    SpriteCounter = 0;
                }
                
                // Reached destination?
                if (pathIndex >= currentPath.size()) {
                    in_movement = false;
                    currentPath = null;
                    pathIndex = 0;
                }
            }
        } else {
            in_movement = false;
        }

        // Update sprite position on screen
        spriteX = conversion_from_mapXY_to_spriteX(xCurrent, yCurrent);
        spriteY = conversion_from_mapXY_to_spriteY(xCurrent, yCurrent);

        // ✨ NEW: Update typing status (auto-hide after 3 seconds)
        updateTypingStatus();

        // Send network update
        sendNetworkUpdate();
    }

    /**
     * Determine direction based on movement from (x1,y1) to (x2,y2)
     */
    private void determineDirection(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        
        // Diagonal movements
        if (dx == -1 && dy == -1) {
            direction = Direction.DIAGONALE_UP;
        } else if (dx == 1 && dy == 1) {
            direction = Direction.DIAGONALE_DOWN;
        }
        // Straight movements
        else if (dx == -1 && dy == 0) {
            direction = Direction.ISO_X_LEFT;
        } else if (dx == 1 && dy == 0) {
            direction = Direction.ISO_X_RIGHT;
        } else if (dx == 0 && dy == -1) {
            direction = Direction.ISO_Y_UP;
        } else if (dx == 0 && dy == 1) {
            direction = Direction.ISO_Y_DOWN;
        }
        // Mixed diagonal movements
        else if (dx == -1 && dy == 1) {
            direction = Direction.LEFT;
        } else if (dx == 1 && dy == -1) {
            direction = Direction.RIGHT;
        }
    }
    
    private void sendNetworkUpdate() {
        if (networkManager != null && networkManager.isConnected()) {
            if (xCurrent != prevMapX || yCurrent != prevMapY || 
                direction != prevDirection || in_movement != prevInMovement) {
                
                networkManager.sendMoveMessage(xCurrent, yCurrent, direction.toString(), in_movement);
                
                prevMapX = xCurrent;
                prevMapY = yCurrent;
                prevDirection = direction;
                prevInMovement = in_movement;
            }
        }
    }

    // ✨ NEW: Set typing status
    public void setTyping(boolean typing) {
        this.isTyping = typing;
        if (typing) {
            lastTypingTime = System.currentTimeMillis();
        }
    }

    // ✨ NEW: Check if typing timeout expired
    private void updateTypingStatus() {
        if (isTyping && System.currentTimeMillis() - lastTypingTime > TYPING_TIMEOUT) {
            isTyping = false;
        }
    }

    // ✨ NEW: Draw typing indicator bubble (Habbo Hotel style!)
    private void drawTypingBubble(Graphics2D g2d) {
        if (!isTyping) return;
        
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

    /**
     * Draw player sprite
     */
    public void draw_player(Graphics2D g2d){
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

        if (currentSprite != null) {
            g2d.drawImage(currentSprite, spriteX, spriteY, 2*gp.tileSizeWidth, 4*gp.tileSizeHeight, null);
        }

        // ✨ NEW: Draw typing bubble if typing
        drawTypingBubble(g2d);
    }

    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= spriteX &&
               mouseX <= spriteX + currentSprite.getWidth() &&
               mouseY >= spriteY &&
               mouseY <= spriteY + currentSprite.getHeight();
    }

    // Message class
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