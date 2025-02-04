package Entity;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Entity {



    public int entityMapX;
    public int entityMapY;
    public int speed;
    public boolean diagonaleUpTrue;

    public BufferedImage playerImageDiagonaleUp1, playerImageDiagonaleUp2, playerImageDiagonaleUp, playerImageDiagonaleDown1, playerImageDiagonaleDown2,playerImageDiagonaleDown,playerImageLeft1, playerImageLeft2, playerImageLeft, playerImageRight1, playerImageRight2, playerImageRight, playerImageIsoXLeft, playerImageIsoXRight, playerImageIsoYUp, playerImageIsoYDown,  playerImageIsoXLeft1, playerImageIsoXLeft2, playerImageIsoYUp1, playerImageIsoYUp2, playerImageIsoXRight1, playerImageIsoXRight2, playerImageIsoYDown1, playerImageIsoYDown2;
    public Direction direction;

    public enum Direction {
        DIAGONALE_UP,
        DIAGONALE_DOWN,
        LEFT,
        RIGHT,
        ISO_Y_UP,
        ISO_Y_DOWN,
        ISO_X_RIGHT,
        ISO_X_LEFT,
    }

    public enum Gender {
        MALE, FEMALE;
    }

    public int SpriteCounter = 0;
    public int SpriteNum = 1;

    public Rectangle solidArea;
    public boolean collisionOn = false;
}
