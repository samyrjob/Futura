package Entity;

import main.GamePanel;
import mouse.MyMouseAdapter;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Player extends Entity {

    private final GamePanel gp;
    public MyMouseAdapter myMouseAdapter;

    private BufferedImage currentSprite;

    public int spriteX;
    public int spriteY;

    // for movement animation :
    public long moveStartTime;
    public int xInitial, yInitial;
    public int xFinal, yFinal;
    public int xCurrent, yCurrent; // To track current position
    public boolean in_movement;


    public Player(GamePanel gp, MyMouseAdapter myMouseAdapter) {

        this.gp = gp;

        setInitialPosition( 0,0);

        spriteX = conversion_from_mapXY_to_spriteX(xInitial,yInitial);
        spriteY = conversion_from_mapXY_to_spriteY(xInitial,yInitial);

        this.in_movement = false;

        direction = Direction.DIAGONALE_DOWN;
        this.myMouseAdapter = myMouseAdapter;

//        speed = 4; // Movement speed (adjustable)
        loadPlayerImage();
        currentSprite = playerImageDiagonaleUp;

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



    // Load the player's image
    private void loadPlayerImage() {
        try {
            // up
            playerImageDiagonaleUp1 = ImageIO.read(getClass().getResourceAsStream("/res/player/sprite-back-mov-1.png"));
//            playerImageDiagonaleUp1 = ImageIO.read(getClass().getResourceAsStream("/player/chair.png"));
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

    private void updateCurrentPosition_diagonaleUp(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
        // Calculate the time per step
        double timePerStep = totalDuration / steps;

        // Determine the current step based on elapsed time
        int currentStep = (int) (elapsedTime / timePerStep);

        // Ensure we don't exceed the total number of steps
        currentStep = Math.min(currentStep, steps);

        // Update xCurrent and yCurrent based on the current step
        xCurrent = x1 - currentStep;
        yCurrent = y1 - currentStep;

    }

    private void updateCurrentPosition_diagonaleDown(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
        // Calculate the time per step
        double timePerStep = totalDuration / steps;

        // Determine the current step based on elapsed time
        int currentStep = (int) (elapsedTime / timePerStep);

        // Ensure we don't exceed the total number of steps
        currentStep = Math.min(currentStep, steps);

        // Update xCurrent and yCurrent based on the current step
        xCurrent = x1 + currentStep;
        yCurrent = y1 + currentStep;

    }


    private void updateCurrentPosition_isoXLeft(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
        // Calculate the time per step
        double timePerStep = totalDuration / steps;

        // Determine the current step based on elapsed time
        int currentStep = (int) (elapsedTime / timePerStep);

        // Ensure we don't exceed the total number of steps
        currentStep = Math.min(currentStep, steps);

        // Update xCurrent and yCurrent based on the current step
        xCurrent = x1 - currentStep;

    }

    private void updateCurrentPosition_isoXRight(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
        // Calculate the time per step
        double timePerStep = totalDuration / steps;

        // Determine the current step based on elapsed time
        int currentStep = (int) (elapsedTime / timePerStep);

        // Ensure we don't exceed the total number of steps
        currentStep = Math.min(currentStep, steps);

        // Update xCurrent and yCurrent based on the current step

        xCurrent = x1 + currentStep;
    }

    private void updateCurrentPosition_isoYUp(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
        // Calculate the time per step
        double timePerStep = totalDuration / steps;

        // Determine the current step based on elapsed time
        int currentStep = (int) (elapsedTime / timePerStep);

        // Ensure we don't exceed the total number of steps
        currentStep = Math.min(currentStep, steps);

        // Update xCurrent and yCurrent based on the current step
        yCurrent = y1 - currentStep;
    }

    private void updateCurrentPosition_isoYDown(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
        // Calculate the time per step
        double timePerStep = totalDuration / steps;

        // Determine the current step based on elapsed time
        int currentStep = (int) (elapsedTime / timePerStep);

        // Ensure we don't exceed the total number of steps
        currentStep = Math.min(currentStep, steps);

        // Update xCurrent and yCurrent based on the current step
        yCurrent = y1 + currentStep;
    }

    private void updateCurrentPosition_right(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
        // Calculate the time per step
        double timePerStep = totalDuration / steps;

        // Determine the current step based on elapsed time
        int currentStep = (int) (elapsedTime / timePerStep);

        // Ensure we don't exceed the total number of steps
        currentStep = Math.min(currentStep, steps);

        // Update xCurrent and yCurrent based on the current step
        yCurrent = y1 - currentStep;
        xCurrent = x1 + currentStep;

    }
    private void updateCurrentPosition_left(int x1, int y1, int x2, int y2, int steps, long elapsedTime, double totalDuration) {
        // Calculate the time per step
        double timePerStep = totalDuration / steps;

        // Determine the current step based on elapsed time
        int currentStep = (int) (elapsedTime / timePerStep);

        // Ensure we don't exceed the total number of steps
        currentStep = Math.min(currentStep, steps);

        // Update xCurrent and yCurrent based on the current step
        yCurrent = y1 + currentStep;
        xCurrent = x1 - currentStep;

    }


    private double assign_totalDuration_value(){

        int dx = Math.abs(xInitial - xFinal);
        int dy = Math.abs(yInitial - yFinal);

        if (dx >= dy){
            double totalDuration = 500_000_000.0 * dx;
            return totalDuration; // Duration for the movement (0.5 seconds)

        } else {
            double totalDuration = 500_000_000.0 * dy;
            return totalDuration;
        }
    }



    public void update1(int x1, int y1, int x2, int y2) {


        if (in_movement) {

//            // if collision is false, player can move


                // Calculate the progress of the movement
                long elapsedTime = System.nanoTime() - moveStartTime;
                double totalDuration = assign_totalDuration_value();


                double progress = Math.min(elapsedTime / totalDuration, 1.0); // Normalize progress from 0 to 1


                int playerWorldX_initial = (x1 - y1) * (gp.tileSizeWidth / 2) + gp.tile_manager.xOffset + (gp.tileSizeWidth / 2) -96;
                int playerWorldY_initial = (x1 + y1 )* (gp.tileSizeHeight / 2) + gp.tile_manager.yOffset + (gp.tileSizeHeight / 2) - 144;
                int playerWorldX_target = (x2 - y2) * (gp.tileSizeWidth / 2) + gp.tile_manager.xOffset + (gp.tileSizeWidth / 2) -96;
                int playerWorldY_target = (x2 + y2 )* (gp.tileSizeHeight / 2) + gp.tile_manager.yOffset + (gp.tileSizeHeight / 2) - 144;


                // Interpolate the player's position
                spriteX = (int) (playerWorldX_initial + (playerWorldX_target - playerWorldX_initial) * progress) ;
                spriteY = (int) (playerWorldY_initial + (playerWorldY_target - playerWorldY_initial) * progress) ;
                System.out.println("progress :  " +  progress);

//    // FORMULA TILECENTERX AND TILECENTERY
//            int tileCenterX = (mapX - mapY) * (gp.tileSizeWidth / 2) + gp.tile_manager.xOffset + (gp.tileSizeWidth / 2);
//            int tileCenterY = (mapX + mapY) * (gp.tileSizeHeight / 2) + gp.tile_manager.yOffset + (gp.tileSizeHeight / 2);

            // Generalize xCurrent and yCurrent updates
////            int steps = (x1 == x2) ? Math.abs(y1 - y2) : Math.abs(x1 - x2);
            int steps = Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)); // Number of steps (tiles) OR WE CAN WRITE LIKE THIS

            // Number of steps (tiles) to traverse
            switch (direction) {
                case DIAGONALE_UP:
                    updateCurrentPosition_diagonaleUp(x1, y1, x2, y2, steps, elapsedTime, totalDuration);
                    break;
                case DIAGONALE_DOWN:
                    updateCurrentPosition_diagonaleDown(x1, y1, x2, y2, steps, elapsedTime, totalDuration);
                    break;
                case ISO_X_LEFT:
                    updateCurrentPosition_isoXLeft(x1, y1, x2, y2, steps, elapsedTime, totalDuration);
                    break;
                case ISO_X_RIGHT:
                    updateCurrentPosition_isoXRight(x1, y1, x2, y2, steps, elapsedTime, totalDuration);
                    break;
                case ISO_Y_UP:
                    updateCurrentPosition_isoYUp(x1, y1, x2, y2, steps, elapsedTime, totalDuration);
                    break;
                case ISO_Y_DOWN:
                    updateCurrentPosition_isoYDown(x1, y1, x2, y2, steps, elapsedTime, totalDuration);
                    break;
                case RIGHT:
                    updateCurrentPosition_right(x1, y1, x2, y2, steps, elapsedTime, totalDuration);
                    break;
                case LEFT:
                    updateCurrentPosition_left(x1, y1, x2, y2, steps, elapsedTime, totalDuration);
                    break;

            }


                // Stop the movement when the target position is reached
                if ( progress == 1.0 ) {
//                    if (xCurrent == xFinal && yCurrent == yFinal)
                        in_movement = false;  //                     Stop the movement
                        setInitialPosition(x2, y2);
                }


        // Handle sprite animation
                SpriteCounter++;
                if (SpriteCounter > 12) {
                    SpriteNum = (SpriteNum == 1) ? 2 : 1;
                    SpriteCounter = 0;
                }
        }
    }



//
//    public void update() {
//
//
//            if (in_movement) {
//
//                if (((xInitial - xFinal) >= 1 && (yInitial - yFinal) >= 1) || ((xFinal - xInitial) >= 1 && (yFinal - yInitial) >= 1) ){
////                    int alpha = Math.abs(xInitial - xFinal);
////                    int lambda = Math.abs(yInitial - yFinal);
////                    if (alpha < lambda){
////                        System.out.println("debug here please");
////                        use_of_diagonale(xInitial, yInitial, xInitial - alpha, yInitial - alpha);
////                    }
//                    use_of_diagonale(xInitial, xFinal, yInitial, yFinal);
//                }
////
//
//                else if (xInitial != xFinal && yInitial == yFinal) {
//                    use_isoX_method(xInitial, xFinal, yInitial, yFinal);
//                }
//                else if (xInitial == xFinal && yInitial != yFinal) {
//                    use_isoY_method(xInitial, xFinal, yInitial, yFinal);
//                }
//                else{
//                    use_right_left_method(xInitial, xFinal, yInitial, yFinal);
//                }
//
////
////                else {
////                    other_method(xInitial, xFinal, yInitial, yFinal);
////
////                }
//            }
////
////            if (keep_movement){
////
////            }
//
//
//    }
////

    public void update() {
        if (in_movement) {

            int dx = Math.abs(xCurrent - xFinal);
            int dy = Math.abs(yCurrent - yFinal);
            int steps = Math.max(dx, dy);  // Use the greater of x or y distance for steps
            // Handle diagonal movement first
            if (dx == dy && (xInitial > xFinal) && (yInitial > yFinal)) {
                // Handle diagonal movement up
                use_of_diagonale(xInitial, xFinal, yInitial, yFinal);
            }
            else if (dx == dy && (xInitial < xFinal) && (yInitial < yFinal)) {
                // Handle diagonal movement down
                use_of_diagonale(xInitial, xFinal, yInitial, yFinal);
            }
            // Then handle axis-aligned movement (horizontal or vertical)
            else if (dx > dy && yCurrent == yFinal) {
                // Horizontal movement (isoXLeft or isoXRight)
                use_isoX_method(xInitial, xFinal, yInitial, yFinal);
            } else if (dy > dx && xCurrent == xFinal){
                // Vertical movement (isoYUp or isoYDown)
                use_isoY_method(xInitial, xFinal, yInitial, yFinal);
            }

            // to move left :
            else if (dx == dy && (xInitial > xFinal) && (yInitial < yFinal)){
                use_right_left_method(xInitial, xFinal, yInitial, yFinal);
            }
            // to move right :
            else if (dx == dy && (xInitial < xFinal) && (yInitial > yFinal)){
                use_right_left_method(xInitial, xFinal, yInitial, yFinal);
            }
            else {
                map_function();
            }

        }
    }

//
public void map_function(){

        if (in_movement){

            if (xInitial > xFinal && yInitial > yFinal){
                use_of_diagonale(xInitial, Math.abs(yFinal - yInitial + xInitial ), yInitial,  yFinal );
            }
            if (xInitial < xFinal && yInitial < yFinal){
                use_of_diagonale(xInitial, Math.abs(yFinal - yInitial + xInitial), yInitial,  yFinal );
            }
            if (xInitial < xFinal && yInitial > yFinal){
                use_right_left_method(xInitial, Math.abs(-yFinal - yInitial - xInitial), yInitial,  yFinal );
            }
            if (xInitial > xFinal && yInitial < yFinal){
                use_right_left_method(xInitial, Math.abs(-yFinal - yInitial + xInitial), yInitial,  yFinal );
            }
        }

}

    void use_of_diagonale(int x1, int x2, int y1, int y2) {
        if ((x1 > x2 && y1 > y2)){
                direction = Direction.DIAGONALE_UP;
                update1(x1 , y1 , xFinal, yFinal);


            }
        else if (x1 < x2 && y1 < y2) {
            direction = Direction.DIAGONALE_DOWN;
            update1(x1 , y1 , xFinal, yFinal);

        }
        else if (x1 <= x2 && y1 > y2) {
            direction = Direction.DIAGONALE_UP;
            update1(x1 , y1 , xFinal, yFinal);

        }
    }


    void use_isoX_method(int x1, int x2, int y1, int y2){
        if (x1 > x2) {
            direction = Direction.ISO_X_LEFT;
            update1(x1 , y1 , xFinal, yFinal);
        }
        if (x1 < x2) {
            direction = Direction.ISO_X_RIGHT;
            update1(x1 , y1 , xFinal, yFinal);
        }

    }

    void use_isoY_method(int x1, int x2, int y1, int y2){
        if (y1 > y2) {
            direction = Direction.ISO_Y_UP;
            update1(x1 , y1 , xFinal, yFinal);
        }
        if (y1 < y2) {
            direction = Direction.ISO_Y_DOWN;
            update1(x1 , y1 , xFinal, yFinal);
        }

    }

    void use_right_left_method(int x1, int x2, int y1, int y2){
        if (x1 >= x2) {
            direction = Direction.LEFT;
            update1(x1 , y1 , xFinal, yFinal);
        }
        else {
            direction = Direction.RIGHT;
            update1(x1 , y1 , xFinal, yFinal);
        }
    }







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

}
