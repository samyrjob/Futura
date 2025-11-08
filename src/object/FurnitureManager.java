package object;

import main.GamePanel;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class FurnitureManager {
    
    private GamePanel gp;
    private List<Furniture> placedFurniture;
    
    public FurnitureManager(GamePanel gp) {
        this.gp = gp;
        this.placedFurniture = new ArrayList<>();
    }
    
    public void addFurniture(Furniture furniture) {
        // Check if furniture overlaps with existing furniture
        removeFurnitureAt(furniture.mapX, furniture.mapY, furniture.tileWidth, furniture.tileHeight);
        placedFurniture.add(furniture);
    }
    
    public void removeFurnitureAt(int mapX, int mapY, int width, int height) {
        // Remove any furniture that overlaps with this area
        placedFurniture.removeIf(f -> {
            // Check if furniture footprints overlap
            boolean overlapX = mapX < f.mapX + f.tileWidth && mapX + width > f.mapX;
            boolean overlapY = mapY < f.mapY + f.tileHeight && mapY + height > f.mapY;
            return overlapX && overlapY;
        });
    }
    
public void draw(Graphics2D g2d) {
    //! debug prints for the array placedFurniture
    // System.out.println("=== DRAW CALL ===");
    // System.out.println("Placed furniture count: " + placedFurniture.size());
    
    placedFurniture.sort((f1, f2) -> {
        int pos1 = f1.mapX + f1.mapY;
        int pos2 = f2.mapX + f2.mapY;
        return Integer.compare(pos1, pos2);
    });

    for (Furniture furniture : placedFurniture) {
        if (furniture.placed && furniture.image != null) {
            
            // ✨ STEP 1: Calculate the CENTER of the tile
            int tileCenterX = (furniture.mapX - furniture.mapY) * (gp.tileSizeWidth / 2) 
                            + gp.tile_manager.xOffset 
                            + (gp.tileSizeWidth / 2);
            
            int tileCenterY = (furniture.mapX + furniture.mapY) * (gp.tileSizeHeight / 2) 
                            + gp.tile_manager.yOffset 
                            + (gp.tileSizeHeight / 2);
            
            // ✨ STEP 2: Use ORIGINAL image size (no scaling!)
            int imageWidth = furniture.image.getWidth();
            int imageHeight = furniture.image.getHeight();
            
            // ✨ STEP 3: Center furniture horizontally on the tile
            int drawX = tileCenterX - (imageWidth / 2);
            
            // ✨ STEP 4: Position furniture vertically (Habbo style - sits on tile)
            int drawY = tileCenterY - imageHeight + (gp.tileSizeHeight / 2);
            
            //! print lines for furniture objects
            // System.out.println("Drawing " + furniture.name + ":");
            // System.out.println("  Original size: " + imageWidth + "x" + imageHeight);
            // System.out.println("  Tile center: (" + tileCenterX + ", " + tileCenterY + ")");
            // System.out.println("  Draw position: (" + drawX + ", " + drawY + ")");
            
            // ✨ STEP 5: Draw at original size (NO width/height parameters = no scaling!)
            g2d.drawImage(furniture.image, drawX, drawY, null);
        }
    }
}
    
    private int conversion_from_mapXY_to_isoX(int mapX, int mapY) {
        return (mapX - mapY) * (gp.tileSizeWidth / 2) + gp.tile_manager.xOffset;
    }
    
    private int conversion_from_mapXY_to_isoY(int mapX, int mapY) {
        return (mapX + mapY) * (gp.tileSizeHeight / 2) + gp.tile_manager.yOffset;
    }
    
    public List<Furniture> getPlacedFurniture() {
        return placedFurniture;
    }
}