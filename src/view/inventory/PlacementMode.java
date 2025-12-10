package view.inventory;

import main.GamePanel;
import object.Furniture;
import java.awt.*;

/**
 * Handles furniture placement mode logic and rendering
 * Manages preview grid, validation, and placement confirmation
 */
public class PlacementMode {
    
    private final GamePanel gp;
    private boolean active;
    private Furniture furnitureToPlace;
    private int previewMapX;
    private int previewMapY;
    
    public PlacementMode(GamePanel gp) {
        this.gp = gp;
        this.active = false;
    }
    
    public void enter(Furniture furniture) {
        this.active = true;
        this.furnitureToPlace = furniture.copy();
    }
    
    public void exit() {
        this.active = false;
        this.furnitureToPlace = null;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void updatePreview(int mouseX, int mouseY) {
        if (!active) return;
        
        Point tilePoint = gp.getCalculateTileFromMouse(mouseX, mouseY);
        previewMapX = tilePoint.x;
        previewMapY = tilePoint.y;
    }
    
    public void confirmPlacement() {
        if (!active || furnitureToPlace == null) return;
        
        if (isValidPlacement()) {
            furnitureToPlace.place(previewMapX, previewMapY);
            gp.furnitureManager.addFurniture(furnitureToPlace);
        }
        
        exit();
    }
    
    public void cancel() {
        exit();
    }
    
    private boolean isValidPlacement() {
        return previewMapX >= 0 && previewMapY >= 0
            && previewMapX + furnitureToPlace.tileWidth <= gp.maxWorldCol
            && previewMapY + furnitureToPlace.tileHeight <= gp.maxWorldRow;
    }
    
    private boolean isValidTile(int tileX, int tileY) {
        return tileX >= 0 && tileX < gp.maxWorldCol 
            && tileY >= 0 && tileY < gp.maxWorldRow;
    }
    
    // ═══════════════════════════════════════════════════════════
    // RENDERING
    // ═══════════════════════════════════════════════════════════
    
    public void drawPreview(Graphics2D g2d) {
        if (!active || furnitureToPlace == null) return;
        
        drawGrid(g2d);
        drawFurnitureGhost(g2d);
    }
    
    private void drawGrid(Graphics2D g2d) {
        for (int x = 0; x < furnitureToPlace.tileWidth; x++) {
            for (int y = 0; y < furnitureToPlace.tileHeight; y++) {
                drawTile(g2d, previewMapX + x, previewMapY + y);
            }
        }
    }
    
    private void drawTile(Graphics2D g2d, int tileX, int tileY) {
        if (!isValidTile(tileX, tileY)) return;
        
        int isoX = (tileX - tileY) * (gp.tileSizeWidth / 2) + gp.tile_manager.xOffset;
        int isoY = (tileX + tileY) * (gp.tileSizeHeight / 2) + gp.tile_manager.yOffset;
        
        int[] xPoints = {
            isoX + gp.tileSizeWidth / 2,
            isoX + gp.tileSizeWidth,
            isoX + gp.tileSizeWidth / 2,
            isoX
        };
        int[] yPoints = {
            isoY,
            isoY + gp.tileSizeHeight / 2,
            isoY + gp.tileSizeHeight,
            isoY + gp.tileSizeHeight / 2
        };
        
        // Fill
        g2d.setColor(InventoryLayout.PLACEMENT_PREVIEW);
        g2d.fillPolygon(xPoints, yPoints, 4);
        
        // Border
        g2d.setColor(InventoryLayout.PLACEMENT_BORDER);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolygon(xPoints, yPoints, 4);
    }
    
    private void drawFurnitureGhost(Graphics2D g2d) {
        if (furnitureToPlace.image == null) return;
        
        int isoX = (previewMapX - previewMapY) * (gp.tileSizeWidth / 2) + gp.tile_manager.xOffset;
        int isoY = (previewMapX + previewMapY) * (gp.tileSizeHeight / 2) + gp.tile_manager.yOffset;
        
        int drawWidth = furnitureToPlace.tileWidth * gp.tileSizeWidth;
        int drawHeight = furnitureToPlace.image.getHeight() * (drawWidth / furnitureToPlace.image.getWidth());
        
        // Draw semi-transparent preview
        Composite original = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g2d.drawImage(
            furnitureToPlace.image,
            isoX, isoY - drawHeight + gp.tileSizeHeight,
            drawWidth, drawHeight,
            null
        );
        g2d.setComposite(original);
    }
}
