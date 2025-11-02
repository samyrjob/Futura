// package Entity;

// import main.GamePanel;
// import mouse.MyMouseAdapter;
// import network.NetworkManager;
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

//     // for movement animation :
//     public long moveStartTime;
//     public int xInitial, yInitial;
//     public int xFinal, yFinal;
//     public int xCurrent, yCurrent; // To track current position
//     public boolean in_movement;


//     // INFORMATION ABOUT THE PLAYER :
//     public int credits;
//     public String name;
//     public Gender gender;
//     public List<Message> messages = new ArrayList<>();
    
//     // Previous position tracking for network updates
//     private int prevMapX = -1;
//     private int prevMapY = -1;
//     private Direction prevDirection = null;
//     private boolean prevInMovement = false;

//     // store a path list for smooth movement
//     List<Point> pathQueue = new ArrayList<>();



//     public Player(GamePanel gp, MyMouseAdapter myMouseAdapter, String name, Gender gender) {

//         this.gp = gp;
//         this.name = name;
//         this.gender = gender;

//         setInitialPosition( 0,0);

//         spriteX = conversion_from_mapXY_to_spriteX(xInitial,yInitial);
//         spriteY = conversion_from_mapXY_to_spriteY(xInitial,yInitial);

//         this.in_movement = false;

//         direction = Direction.DIAGONALE_DOWN;
//         this.myMouseAdapter = myMouseAdapter;

//         loadPlayerImage();
//         currentSprite = playerImageDiagonaleUp;

//         credits = 56;

//     }
    
//     public void setNetworkManager(NetworkManager nm) {
//         this.networkManager = nm;
//     }


//     public void setInitialPosition(int x, int y) {
//         this.xInitial = x;
//         this.yInitial = y;
//         this.xCurrent = x; // Set current to initial at first
//         this.yCurrent = y;
//     }

//     public void setFinalPosition(int x, int y) {
//         this.xFinal = x;
//         this.yFinal = y;
//     }

//     public int fromTileCenterXToSpriteX(int x) {

//         return x-gp.tileSizeWidth;
//     }

//     public int fromTileCenterYToSpriteY(int y) {
//         return y-(3*gp.tileSizeHeight);
//     }


//     public int getCredits(){
//         return credits;
//     }


//     // Load the player's image
//     private void loadPlayerImage() {
//         try {
//             // up
//             playerImageDiagonaleUp1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-back-mov-1.png"));
//             playerImageDiagonaleUp2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-back-mov-2.png"));
//             playerImageDiagonaleUp = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-back.png"));
//             // down
//             playerImageDiagonaleDown1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-front-mov-1.png"));
//             playerImageDiagonaleDown2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-front-mov-2.png"));
//             playerImageDiagonaleDown = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-front.png"));
//             //left
//             playerImageLeft1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-left-mov-1.png"));
//             playerImageLeft2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-left-mov-2.png"));
//             playerImageLeft = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-left.png"));
//             // right
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


//     // FIRST CONVERSION FROM MAPX AND MAPY TO TILECENTERX AND TILECENTERY

//     int conversion_from_mapXY_to_tilecenterX(int mapX, int mapY){
//         return (mapX - mapY)*(gp.tileSizeWidth / 2) + gp.tile_manager.xOffset + (gp.tileSizeWidth / 2);
//     }
//     int conversion_from_mapXY_to_tilecenterY(int mapX, int mapY){
//         return (mapX + mapY)*(gp.tileSizeHeight/2)  + gp.tile_manager.yOffset + (gp.tileSizeHeight/ 2);
//     }

//     // SECOND CONVERSION FROM MAPX AND MAPY AND SO ON TILECENTERX and TILECENTERY to SPRITEX AND SPRITEY

//     int conversion_from_mapXY_to_spriteX(int mapX, int mapY){
//         return conversion_from_mapXY_to_tilecenterX(mapX, mapY) - gp.tileSizeWidth;
//     }
//     int conversion_from_mapXY_to_spriteY(int mapX, int mapY){
//         return conversion_from_mapXY_to_tilecenterY(mapX, mapY) - (3*gp.tileSizeHeight);
//     }





//     public void update() {

//         if (in_movement) {
//             updateSmoothMove(xInitial, yInitial, xFinal, yFinal);
//         }

//         sendNetworkUpdate();

//     }
    
//     private void sendNetworkUpdate() {
//         if (networkManager != null && networkManager.isConnected()) {
//             // Check if anything changed
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





//     //* MOVEMENT TABLE SPEED :  */

//     //*totalDuration (nanoseconds)	Approx. per-tile time	Description
// //* */ 400_000_000.0	0.4 seconds	current speed (fast)
// //* 600_000_000.0	0.6 seconds	slightly slower
// //* */ 800_000_000.0	0.8 seconds	medium slow
// //* */ 1_000_000_000.0	1 second	slow and smooth
// //* */ 1_500_000_000.0	1.5 seconds	very slow (Habbo-like glide) */


// //!
// void updateSmoothMove(int x1, int y1, int x2, int y2) {
//     long elapsedTime = System.nanoTime() - moveStartTime;
//     double totalDuration = 800_000_000.0; // 0.8s per tile move
//     double progress = Math.min(1.0, elapsedTime / totalDuration);

//     int dx = xFinal - xInitial;
//     int dy = yFinal - yInitial;

//     if (dx > 0 && dy > 0) direction = Direction.DIAGONALE_DOWN;
//     else if (dx < 0 && dy < 0) direction = Direction.DIAGONALE_UP;
//     else if (dx > 0 && dy < 0) direction = Direction.ISO_X_RIGHT;
//     else if (dx < 0 && dy > 0) direction = Direction.ISO_X_LEFT;
//     else if (dx > 0) direction = Direction.RIGHT;
//     else if (dx < 0) direction = Direction.LEFT;
//     else if (dy > 0) direction = Direction.ISO_Y_DOWN;
//     else if (dy < 0) direction = Direction.ISO_Y_UP;


//     // Easing (optional, makes motion more natural)
//     progress = progress * progress * (3 - 2 * progress); // smoothstep easing

//      double dxPixels = x2 - x1;
//     double dyPixels = y2 - y1;

//     double interpolatedX = x1 + dxPixels * progress;
//     double interpolatedY = y1 + dyPixels * progress;

//     xCurrent = (int) interpolatedX;
//     yCurrent = (int) interpolatedY;

//     spriteX = conversion_from_mapXY_to_spriteX(xCurrent, yCurrent);
//     spriteY = conversion_from_mapXY_to_spriteY(xCurrent, yCurrent);

//     if (progress >= 1.0) {
//         in_movement = false;
//         xInitial = xFinal;
//         yInitial = yFinal;
//     }
// }








//     public void draw_player(Graphics2D g2d){


//         if (in_movement) {
//             // Handle movement logic and assign the correct moving sprite
//             switch (direction) {
//                 case DIAGONALE_UP:
//                     // Assign the movement-related diagonaleup sprite
//                 if (SpriteNum == 1) {
//                     currentSprite = playerImageDiagonaleUp1;        // Replace with your moving sprite
//                 }
//                 if (SpriteNum == 2) {
//                     currentSprite = playerImageDiagonaleUp2;         // Replace with your moving sprite
//                 }
//                 break;
//                 case ISO_Y_UP:
//                     if (SpriteNum == 1) {
//                         currentSprite = playerImageIsoYUp1;        // Replace with your moving sprite
//                     }
//                     if (SpriteNum == 2) {
//                         currentSprite = playerImageIsoYUp2;         // Replace with your moving sprite
//                     }
//                     break;
//                 case ISO_Y_DOWN:
//                     if (SpriteNum == 1) {
//                         currentSprite = playerImageIsoYDown1;        // Replace with your moving sprite
//                     }
//                     if (SpriteNum == 2) {
//                         currentSprite = playerImageIsoYDown2;         // Replace with your moving sprite
//                     }
//                     break;
//                 case ISO_X_RIGHT:
//                     if (SpriteNum == 1) {
//                         currentSprite = playerImageIsoXRight1;        // Replace with your moving sprite
//                     }
//                     if (SpriteNum == 2) {
//                         currentSprite = playerImageIsoXRight2;         // Replace with your moving sprite
//                     }
//                     break;
//                 case DIAGONALE_DOWN:
//                     // Assign the movement-related diagonaleup sprite
//                     if (SpriteNum == 1) {
//                         currentSprite = playerImageDiagonaleDown1;        // Replace with your moving sprite
//                     }
//                     if (SpriteNum == 2) {
//                         currentSprite = playerImageDiagonaleDown2;         // Replace with your moving sprite
//                     }
//                     break;
//                 case ISO_X_LEFT:
//                     if (SpriteNum == 1) {
//                         currentSprite = playerImageIsoXLeft1;        // Replace with your moving sprite
//                     }
//                     if (SpriteNum == 2) {
//                         currentSprite = playerImageIsoXLeft2;         // Replace with your moving sprite
//                     }
//                     break;
//                 case LEFT:
//                     if (SpriteNum == 1) {
//                         currentSprite = playerImageLeft1;        // Replace with your moving sprite
//                     }
//                     if (SpriteNum == 2) {
//                         currentSprite = playerImageLeft2;         // Replace with your moving sprite
//                     }
//                     break;

//                 case RIGHT:
//                     if (SpriteNum == 1) {
//                         currentSprite = playerImageRight1;        // Replace with your moving sprite
//                     }
//                     if (SpriteNum == 2) {
//                         currentSprite = playerImageRight2;         // Replace with your moving sprite
//                     }
//                     break;

//                 // Add other cases for diagonaledown, isoXLeft, isoXRight, etc.
//             }

//         } else {
//             // Assign the static sprite based on the last direction
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
//             g2d.drawImage(currentSprite, spriteX, spriteY, 2*gp.tileSizeWidth, (4)*gp.tileSizeHeight, null);
//         }

//     }







    
//     // Message class to store text and position
//     public static class Message {
//         public String text;
//         public int y;
//         public int adjustedY;

//         public Message(String text, int y) {
//             this.text = text;
//             this.y = y;
//         }
//     }



// // For opening and closing the bubble of the player in In Player.java
// public boolean contains(int mouseX, int mouseY) {
//     return mouseX >= spriteX &&
//            mouseX <= spriteX + currentSprite.getWidth() &&
//            mouseY >= spriteY &&
//            mouseY <= spriteY + currentSprite.getHeight();
// }


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