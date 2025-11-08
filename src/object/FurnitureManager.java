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
    System.out.println("=== DRAW CALL ===");
    System.out.println("Placed furniture count: " + placedFurniture.size());
    
    placedFurniture.sort((f1, f2) -> {
        int pos1 = f1.mapX + f1.mapY;
        int pos2 = f2.mapX + f2.mapY;
        return Integer.compare(pos1, pos2);
    });

    for (Furniture furniture : placedFurniture) {
        System.out.println("Processing furniture: " + furniture);
        System.out.println(" - placed: " + furniture.placed);
        System.out.println(" - image: " + furniture.image);
        System.out.println(" - mapX: " + furniture.mapX + ", mapY: " + furniture.mapY);
        
        if (furniture.placed && furniture.image != null) {
            int isoX = conversion_from_mapXY_to_isoX(furniture.mapX, furniture.mapY);
            int isoY = conversion_from_mapXY_to_isoY(furniture.mapX, furniture.mapY);
            System.out.println(" - Drawing at iso coordinates: " + isoX + ", " + isoY);
            
            // Your actual drawing code here
            g2d.drawImage(furniture.image, isoX, isoY, null);
        } else {
            System.out.println(" - SKIPPING: placed=" + furniture.placed + ", image=" + furniture.image);
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