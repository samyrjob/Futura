package object;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class SuperObject {

    public int x,y;
    public BufferedImage image;

    void draw(Graphics2D g2d, int x, int y){
        g2d.drawImage(image, x, y, null);
    }
    
}
