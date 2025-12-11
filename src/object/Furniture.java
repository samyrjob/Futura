package object;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Furniture {
    
    public String name;
    public BufferedImage image;
    public int tileWidth;  // Width in tiles
    public int tileHeight; // Height in tiles
    public int mapX;       // Position on isometric grid
    public int mapY;
    public boolean placed; // Is it placed on the map?
    
    public Furniture(String name, String imagePath, int tileWidth, int tileHeight) {
        this.name = name;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.placed = false;
        
        try {
            image = ImageIO.read(getClass().getResourceAsStream(imagePath));
        } catch (IOException e) {
            System.err.println("Failed to load furniture: " + imagePath);
            e.printStackTrace();
        }
    }
    
    public void place(int mapX, int mapY) {
        this.mapX = mapX;
        this.mapY = mapY;
        this.placed = true;
    }
    
    public void remove() {
        this.placed = false;
    }
    
    public boolean occupiesTile(int tileX, int tileY) {
        if (!placed) return false;
        
        // Check if the given tile is within this furniture's footprint
        return tileX >= mapX && tileX < mapX + tileWidth &&
               tileY >= mapY && tileY < mapY + tileHeight;
    }
    
    public Furniture copy() {
        // Create a copy for placing (so we keep the original in inventory)
        Furniture copy = new Furniture(name, "", tileWidth, tileHeight);
        copy.image = this.image;
        return copy;
    }
}