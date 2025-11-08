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
        // Draw furniture in order (back to front based on mapY + mapX for proper layering)
        placedFurniture.sort((f1, f2) -> {
            int pos1 = f1.mapX + f1.mapY;
            int pos2 = f2.mapX + f2.mapY;
            return Integer.compare(pos1, pos2);
        });
        
        for (Furniture furniture : placedFurniture) {
            if (furniture.placed && furniture.image != null) {
                // Calculate isometric position
                int isoX = conversion_from_mapXY_to_isoX(furniture.mapX, furniture.mapY);
                int isoY = conversion_from_mapXY_to_isoY(furniture.mapX, furniture.mapY);
                
                // Draw furniture at calculated position
                int drawWidth = furniture.tileWidth * gp.tileSizeWidth;
                int drawHeight = furniture.image.getHeight() * (drawWidth / furniture.image.getWidth());
                
                g2d.drawImage(furniture.image, isoX, isoY - drawHeight + gp.tileSizeHeight, 
                             drawWidth, drawHeight, null);
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