package Entity;

import main.GamePanel;
import mouse.MyMouseAdapter;
import network.NetworkManager;
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

    // for movement animation :
    public long moveStartTime;
    public int xInitial, yInitial;
    public int xFinal, yFinal;
    public int xCurrent, yCurrent; // To track current position
    public boolean in_movement;


    // INFORMATION ABOUT THE PLAYER :
    public int credits;
    public String name;
    public Gender gender;
    public List<Message> messages = new ArrayList<>();
    
    // Previous position tracking for network updates
    private int prevMapX = -1;
    private int prevMapY = -1;
    private Direction prevDirection = null;
    private boolean prevInMovement = false;

    // store a path list for smooth movement
    List<Point> pathQueue = new ArrayList<>();



    public Player(GamePanel gp, MyMouseAdapter myMouseAdapter, String name, Gender gender) {

        this.gp = gp;
        this.name = name;
        this.gender = gender;

        setInitialPosition( 0,0);

        spriteX = conversion_from_mapXY_to_spriteX(xInitial,yInitial);
        spriteY = conversion_from_mapXY_to_spriteY(xInitial,yInitial);

        this.in_movement = false;

        direction = Direction.DIAGONALE_DOWN;
        this.myMouseAdapter = myMouseAdapter;

        loadPlayerImage();
        currentSprite = playerImageDiagonaleUp;

        credits = 56;

    }
    
    public void setNetworkManager(NetworkManager nm) {
        this.networkManager = nm;
    }


    public void setInitialPosition(int x, int y) {
        this.xInitial = x;
        this.yInitial = y;
        this.xCurrent = x; // Set current to initial at first
        this.yCurrent = y;
    }

    public void setFinalPosition(int x, int y) {
        this.xFinal = x;
        this.yFinal = y;
    }

    public int fromTileCenterXToSpriteX(int x) {

        return x-gp.tileSizeWidth;
    }

    public int fromTileCenterYToSpriteY(int y) {
        return y-(3*gp.tileSizeHeight);
    }


    public int getCredits(){
        return credits;
    }


    // Load the player's image
    private void loadPlayerImage() {
        try {
            // up
            playerImageDiagonaleUp1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-back-mov-1.png"));
            playerImageDiagonaleUp2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-back-mov-2.png"));
            playerImageDiagonaleUp = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-back.png"));
            // down
            playerImageDiagonaleDown1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-front-mov-1.png"));
            playerImageDiagonaleDown2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-front-mov-2.png"));
            playerImageDiagonaleDown = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-front.png"));
            //left
            playerImageLeft1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-left-mov-1.png"));
            playerImageLeft2 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-left-mov-2.png"));
            playerImageLeft = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-left.png"));
            // right
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


    // FIRST CONVERSION FROM MAPX AND MAPY TO TILECENTERX AND TILECENTERY

    int conversion_from_mapXY_to_tilecenterX(int mapX, int mapY){
        return (mapX - mapY)*(gp.tileSizeWidth / 2) + gp.tile_manager.xOffset + (gp.tileSizeWidth / 2);
    }
    int conversion_from_mapXY_to_tilecenterY(int mapX, int mapY){
        return (mapX + mapY)*(gp.tileSizeHeight/2)  + gp.tile_manager.yOffset + (gp.tileSizeHeight/ 2);
    }

    // SECOND CONVERSION FROM MAPX AND MAPY AND SO ON TILECENTERX and TILECENTERY to SPRITEX AND SPRITEY

    int conversion_from_mapXY_to_spriteX(int mapX, int mapY){
        return conversion_from_mapXY_to_tilecenterX(mapX, mapY) - gp.tileSizeWidth;
    }
    int conversion_from_mapXY_to_spriteY(int mapX, int mapY){
        return conversion_from_mapXY_to_tilecenterY(mapX, mapY) - (3*gp.tileSizeHeight);
    }

    // private void updateCurrentPosition_diagonaleUp(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
    //     // Calculate the time per step
    //     double timePerStep = totalDuration / steps;

    //     // Determine the current step based on elapsed time
    //     int currentStep = (int) (elapsedTime / timePerStep);

    //     // Ensure we don't exceed the total number of steps
    //     currentStep = Math.min(currentStep, steps);

    //     // Update xCurrent and yCurrent based on the current step
    //     xCurrent = x1 - currentStep;
    //     yCurrent = y1 - currentStep;

    // }

    // private void updateCurrentPosition_diagonaleDown(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
    //     // Calculate the time per step
    //     double timePerStep = totalDuration / steps;

    //     // Determine the current step based on elapsed time
    //     int currentStep = (int) (elapsedTime / timePerStep);

    //     // Ensure we don't exceed the total number of steps
    //     currentStep = Math.min(currentStep, steps);

    //     // Update xCurrent and yCurrent based on the current step
    //     xCurrent = x1 + currentStep;
    //     yCurrent = y1 + currentStep;

    // }


    // private void updateCurrentPosition_isoXLeft(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
    //     // Calculate the time per step
    //     double timePerStep = totalDuration / steps;

    //     // Determine the current step based on elapsed time
    //     int currentStep = (int) (elapsedTime / timePerStep);

    //     // Ensure we don't exceed the total number of steps
    //     currentStep = Math.min(currentStep, steps);

    //     // Update xCurrent and yCurrent based on the current step
    //     xCurrent = x1 - currentStep;

    // }

    // private void updateCurrentPosition_isoXRight(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
    //     // Calculate the time per step
    //     double timePerStep = totalDuration / steps;

    //     // Determine the current step based on elapsed time
    //     int currentStep = (int) (elapsedTime / timePerStep);

    //     // Ensure we don't exceed the total number of steps
    //     currentStep = Math.min(currentStep, steps);

    //     // Update xCurrent and yCurrent based on the current step

    //     xCurrent = x1 + currentStep;
    // }

    // private void updateCurrentPosition_isoYUp(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
    //     // Calculate the time per step
    //     double timePerStep = totalDuration / steps;

    //     // Determine the current step based on elapsed time
    //     int currentStep = (int) (elapsedTime / timePerStep);

    //     // Ensure we don't exceed the total number of steps
    //     currentStep = Math.min(currentStep, steps);

    //     // Update xCurrent and yCurrent based on the current step
    //     yCurrent = y1 - currentStep;
    // }

    // private void updateCurrentPosition_isoYDown(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
    //     // Calculate the time per step
    //     double timePerStep = totalDuration / steps;

    //     // Determine the current step based on elapsed time
    //     int currentStep = (int) (elapsedTime / timePerStep);

    //     // Ensure we don't exceed the total number of steps
    //     currentStep = Math.min(currentStep, steps);

    //     // Update xCurrent and yCurrent based on the current step
    //     yCurrent = y1 + currentStep;
    // }

    // private void updateCurrentPosition_right(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
    //     // Calculate the time per step
    //     double timePerStep = totalDuration / steps;

    //     // Determine the current step based on elapsed time
    //     int currentStep = (int) (elapsedTime / timePerStep);

    //     // Ensure we don't exceed the total number of steps
    //     currentStep = Math.min(currentStep, steps);

    //     // Update xCurrent and yCurrent based on the current step
    //     xCurrent = x1 + currentStep;
    //     yCurrent = y1 + currentStep;
    // }

    // private void updateCurrentPosition_left(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
    //     // Calculate the time per step
    //     double timePerStep = totalDuration / steps;

    //     // Determine the current step based on elapsed time
    //     int currentStep = (int) (elapsedTime / timePerStep);

    //     // Ensure we don't exceed the total number of steps
    //     currentStep = Math.min(currentStep, steps);

    //     // Update xCurrent and yCurrent based on the current step
    //     xCurrent = x1 - currentStep;
    //     yCurrent = y1 + currentStep;
    // }




    public void update() {

        if (in_movement) {
            updateSmoothMove(xInitial, yInitial, xFinal, yFinal);
        }

        // if (in_movement) {
        //     SpriteCounter++;
        //     if (SpriteCounter > 10) {
        //         if (SpriteNum == 1) {
        //             SpriteNum = 2;
        //         } else if (SpriteNum == 2) {
        //             SpriteNum = 1;
        //         }
        //         SpriteCounter = 0;
        //     }
        // }

        // int tileCenterX = conversion_from_mapXY_to_tilecenterX(xCurrent,yCurrent);
        // int tileCenterY = conversion_from_mapXY_to_tilecenterY(xCurrent, yCurrent);

        // spriteX = fromTileCenterXToSpriteX(tileCenterX);
        // spriteY = fromTileCenterYToSpriteY(tileCenterY);

        
        // Send network update if position changed
        sendNetworkUpdate();

    }
    
    private void sendNetworkUpdate() {
        if (networkManager != null && networkManager.isConnected()) {
            // Check if anything changed
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


//!
    // void update1(int x1, int y1, int x2, int y2) {
    //     long elapsedTime = System.nanoTime() - moveStartTime;
    //     double totalDuration = 1_500_000_000.0; // 1.5 seconds in nanoseconds
    //     int totalSteps = Math.abs(x1 - x2) + Math.abs(y1 - y2);

    //     boolean movementFinished = false;

    //     if (elapsedTime >= totalDuration) {
    //         in_movement = false;
    //         xCurrent = xFinal;
    //         yCurrent = yFinal;
    //         spriteX = conversion_from_mapXY_to_spriteX(xCurrent, yCurrent);
    //         spriteY = conversion_from_mapXY_to_spriteY(xCurrent, yCurrent);
    //         movementFinished = true;
    //     } else {
    //         switch (direction) {
    //             case DIAGONALE_UP:
    //                 updateCurrentPosition_diagonaleUp(x1, y1, x2, y2, totalSteps, elapsedTime, totalDuration);
    //                 break;
    //             case DIAGONALE_DOWN:
    //                 updateCurrentPosition_diagonaleDown(x1, y1, x2, y2, totalSteps, elapsedTime, totalDuration);
    //                 break;
    //             case LEFT:
    //                 updateCurrentPosition_left(x1, y1, x2, y2, totalSteps, elapsedTime, totalDuration);
    //                 break;
    //             case RIGHT:
    //                 updateCurrentPosition_right(x1, y1, x2, y2, totalSteps, elapsedTime, totalDuration);
    //                 break;
    //             case ISO_X_LEFT:
    //                 updateCurrentPosition_isoXLeft(x1, y1, x2, y2, totalSteps, elapsedTime, totalDuration);
    //                 break;
    //             case ISO_X_RIGHT:
    //                 updateCurrentPosition_isoXRight(x1, y1, x2, y2, totalSteps, elapsedTime, totalDuration);
    //                 break;
    //             case ISO_Y_UP:
    //                 updateCurrentPosition_isoYUp(x1, y1, x2, y2, totalSteps, elapsedTime, totalDuration);
    //                 break;
    //             case ISO_Y_DOWN:
    //                 updateCurrentPosition_isoYDown(x1, y1, x2, y2, totalSteps, elapsedTime, totalDuration);
    //                 break;
    //         }

    //         spriteX = conversion_from_mapXY_to_spriteX(xCurrent, yCurrent);
    //         spriteY = conversion_from_mapXY_to_spriteY(xCurrent, yCurrent);
    //     }

    //     if (movementFinished) {
    //         xInitial = xCurrent;
    //         yInitial = yCurrent;
    //     }
    // }
//!
void updateSmoothMove(int x1, int y1, int x2, int y2) {
    long elapsedTime = System.nanoTime() - moveStartTime;
    double totalDuration = 400_000_000.0; // 0.4s per tile move
    double progress = Math.min(1.0, elapsedTime / totalDuration);

    int dx = xFinal - xInitial;
    int dy = yFinal - yInitial;

    if (dx > 0 && dy > 0) direction = Direction.DIAGONALE_DOWN;
    else if (dx < 0 && dy < 0) direction = Direction.DIAGONALE_UP;
    else if (dx > 0 && dy < 0) direction = Direction.ISO_X_RIGHT;
    else if (dx < 0 && dy > 0) direction = Direction.ISO_X_LEFT;
    else if (dx > 0) direction = Direction.RIGHT;
    else if (dx < 0) direction = Direction.LEFT;
    else if (dy > 0) direction = Direction.ISO_Y_DOWN;
    else if (dy < 0) direction = Direction.ISO_Y_UP;


    // Easing (optional, makes motion more natural)
    progress = progress * progress * (3 - 2 * progress); // smoothstep easing

     double dxPixels = x2 - x1;
    double dyPixels = y2 - y1;

    double interpolatedX = x1 + dxPixels * progress;
    double interpolatedY = y1 + dyPixels * progress;

    xCurrent = (int) interpolatedX;
    yCurrent = (int) interpolatedY;

    spriteX = conversion_from_mapXY_to_spriteX(xCurrent, yCurrent);
    spriteY = conversion_from_mapXY_to_spriteY(xCurrent, yCurrent);

    if (progress >= 1.0) {
        in_movement = false;
        xInitial = xFinal;
        yInitial = yFinal;
    }
}


//!
    // void update_position_player(int x1, int y1, int x2, int y2){

    //     if (x1 == x2){
    //         use_isoY_method(x1 , x2 , y1, y2);
    //     }
    //     else if (y1 == y2){
    //         use_isoX_method(x1 , x2 , y1, y2);
    //     }
    //     else if (Math.abs(x1 - x2) == Math.abs(y1 - y2)){
    //         use_of_diagonale(x1, x2, y1, y2);
    //     }
    //     else if (Math.abs(x1 - x2) > Math.abs(y1 - y2)) {
    //         if (xInitial < xFinal && yInitial < yFinal){
    //             use_right_left_method(xInitial, Math.abs(xFinal - xInitial - yFinal + yInitial), yInitial,  yFinal );
    //         }
    //         if (xInitial < xFinal && yInitial > yFinal){
    //             use_right_left_method(xInitial, Math.abs(xFinal - xInitial + yFinal - yInitial), yInitial,  yFinal );
    //         }
    //         if (xInitial > xFinal && yInitial > yFinal){
    //             use_right_left_method(xInitial, Math.abs(-xFinal + xInitial + yFinal - yInitial), yInitial,  yFinal );
    //         }
    //         if (xInitial > xFinal && yInitial < yFinal){
    //             use_right_left_method(xInitial, Math.abs(-xFinal + xInitial - yFinal + yInitial), yInitial,  yFinal );
    //         }
    //     } else if (Math.abs(x1 - x2) < Math.abs(y1 - y2)) {
    //         if (xInitial < xFinal && yInitial < yFinal){
    //             use_right_left_method(xInitial, Math.abs(yFinal - yInitial - xFinal + xInitial), yInitial,  yFinal );
    //         }
    //         if (xInitial < xFinal && yInitial > yFinal){
    //             use_right_left_method(xInitial, Math.abs(-yFinal + yInitial - xFinal + xInitial), yInitial,  yFinal );
    //         }
    //         if (xInitial > xFinal && yInitial > yFinal){
    //             use_right_left_method(xInitial, Math.abs(-yFinal + yInitial + xFinal - xInitial), yInitial,  yFinal );
    //         }
    //         if (xInitial > xFinal && yInitial < yFinal){
    //             use_right_left_method(xInitial, Math.abs(-yFinal - yInitial + xInitial), yInitial,  yFinal );
    //         }
    //     }

    // }
    // //!

    // void use_of_diagonale(int x1, int x2, int y1, int y2) {
    //     if ((x1 > x2 && y1 > y2)){
    //             direction = Direction.DIAGONALE_UP;
    //             update1(x1 , y1 , xFinal, yFinal);


    //         }
    //     else if (x1 < x2 && y1 < y2) {
    //         direction = Direction.DIAGONALE_DOWN;
    //         update1(x1 , y1 , xFinal, yFinal);

    //     }
    //     else if (x1 <= x2 && y1 > y2) {
    //         direction = Direction.DIAGONALE_UP;
    //         update1(x1 , y1 , xFinal, yFinal);

    //     }
    // }


    // void use_isoX_method(int x1, int x2, int y1, int y2){
    //     if (x1 > x2) {
    //         direction = Direction.ISO_X_LEFT;
    //         update1(x1 , y1 , xFinal, yFinal);
    //     }
    //     if (x1 < x2) {
    //         direction = Direction.ISO_X_RIGHT;
    //         update1(x1 , y1 , xFinal, yFinal);
    //     }

    // }

    // void use_isoY_method(int x1, int x2, int y1, int y2){
    //     if (y1 > y2) {
    //         direction = Direction.ISO_Y_UP;
    //         update1(x1 , y1 , xFinal, yFinal);
    //     }
    //     if (y1 < y2) {
    //         direction = Direction.ISO_Y_DOWN;
    //         update1(x1 , y1 , xFinal, yFinal);
    //     }

    // }

    // void use_right_left_method(int x1, int x2, int y1, int y2){
    //     if (x1 >= x2) {
    //         direction = Direction.LEFT;
    //         update1(x1 , y1 , xFinal, yFinal);
    //     }
    //     else {
    //         direction = Direction.RIGHT;
    //         update1(x1 , y1 , xFinal, yFinal);
    //     }
    // }







    public void draw_player(Graphics2D g2d){


        if (in_movement) {
            // Handle movement logic and assign the correct moving sprite
            switch (direction) {
                case DIAGONALE_UP:
                    // Assign the movement-related diagonaleup sprite
                if (SpriteNum == 1) {
                    currentSprite = playerImageDiagonaleUp1;        // Replace with your moving sprite
                }
                if (SpriteNum == 2) {
                    currentSprite = playerImageDiagonaleUp2;         // Replace with your moving sprite
                }
                break;
                case ISO_Y_UP:
                    if (SpriteNum == 1) {
                        currentSprite = playerImageIsoYUp1;        // Replace with your moving sprite
                    }
                    if (SpriteNum == 2) {
                        currentSprite = playerImageIsoYUp2;         // Replace with your moving sprite
                    }
                    break;
                case ISO_Y_DOWN:
                    if (SpriteNum == 1) {
                        currentSprite = playerImageIsoYDown1;        // Replace with your moving sprite
                    }
                    if (SpriteNum == 2) {
                        currentSprite = playerImageIsoYDown2;         // Replace with your moving sprite
                    }
                    break;
                case ISO_X_RIGHT:
                    if (SpriteNum == 1) {
                        currentSprite = playerImageIsoXRight1;        // Replace with your moving sprite
                    }
                    if (SpriteNum == 2) {
                        currentSprite = playerImageIsoXRight2;         // Replace with your moving sprite
                    }
                    break;
                case DIAGONALE_DOWN:
                    // Assign the movement-related diagonaleup sprite
                    if (SpriteNum == 1) {
                        currentSprite = playerImageDiagonaleDown1;        // Replace with your moving sprite
                    }
                    if (SpriteNum == 2) {
                        currentSprite = playerImageDiagonaleDown2;         // Replace with your moving sprite
                    }
                    break;
                case ISO_X_LEFT:
                    if (SpriteNum == 1) {
                        currentSprite = playerImageIsoXLeft1;        // Replace with your moving sprite
                    }
                    if (SpriteNum == 2) {
                        currentSprite = playerImageIsoXLeft2;         // Replace with your moving sprite
                    }
                    break;
                case LEFT:
                    if (SpriteNum == 1) {
                        currentSprite = playerImageLeft1;        // Replace with your moving sprite
                    }
                    if (SpriteNum == 2) {
                        currentSprite = playerImageLeft2;         // Replace with your moving sprite
                    }
                    break;

                case RIGHT:
                    if (SpriteNum == 1) {
                        currentSprite = playerImageRight1;        // Replace with your moving sprite
                    }
                    if (SpriteNum == 2) {
                        currentSprite = playerImageRight2;         // Replace with your moving sprite
                    }
                    break;

                // Add other cases for diagonaledown, isoXLeft, isoXRight, etc.
            }

        } else {
            // Assign the static sprite based on the last direction
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
            g2d.drawImage(currentSprite, spriteX, spriteY, 2*gp.tileSizeWidth, (4)*gp.tileSizeHeight, null);
        }

    }







    
    // Message class to store text and position
    public static class Message {
        public String text;
        public int y;
        public int adjustedY;

        public Message(String text, int y) {
            this.text = text;
            this.y = y;
        }
    }



// For opening and closing the bubble of the player in In Player.java
public boolean contains(int mouseX, int mouseY) {
    return mouseX >= spriteX &&
           mouseX <= spriteX + currentSprite.getWidth() &&
           mouseY >= spriteY &&
           mouseY <= spriteY + currentSprite.getHeight();
}


}