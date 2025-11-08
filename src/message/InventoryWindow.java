package message;

import main.GamePanel;
import object.Furniture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InventoryWindow {
    
    private GamePanel gp;
    private boolean visible;
    
    // Window dimensions and position
    private int windowX = 200;
    private int windowY = 150;
    private int windowWidth = 600;
    private int windowHeight = 400;
    
    // Sections
    private int listWidth = 250;
    private int previewWidth = 350;
    private int headerHeight = 40;
    private int itemHeight = 50;
    
    // Inventory items
    private List<Furniture> furnitureList;
    private Furniture selectedFurniture;
    private int scrollOffset = 0;
    
    // Placement mode
    private boolean placementMode = false;
    private Furniture furnitureToPlace;
    private int previewMapX = 0;
    private int previewMapY = 0;
    
    // Dragging window
    private boolean draggingWindow = false;
    private int dragOffsetX, dragOffsetY;
    
    public InventoryWindow(GamePanel gp) {
        this.gp = gp;
        this.visible = false;
        this.furnitureList = new ArrayList<>();
        
        loadFurnitureFromRes();
    }
    
    private void loadFurnitureFromRes() {
        // Load all PNG files from /res folder as furniture
        // For demo purposes, I'll add some example furniture
        // You can expand this to scan the actual res folder
        
        furnitureList.add(new Furniture("Chair", "/res/tile/Chair_base.png", 1, 1));
        // Add more furniture items by scanning /res folder
        // This is where you'd dynamically load PNG files
    }
    
    public void toggle() {
        visible = !visible;
        if (!visible) {
            placementMode = false;
            furnitureToPlace = null;
        }
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public boolean isPlacementMode() {
        return placementMode;
    }
    
    public void handleClick(int mouseX, int mouseY) {
        if (!visible) return;
        
    
        // Check if clicking close button FIRST
        int closeX = windowX + windowWidth - 30;
        int closeY = windowY + 10;
        int closeW = 20;
        int closeH = 20;

        if (mouseX >= closeX && mouseX <= closeX + closeW &&
            mouseY >= closeY && mouseY <= closeY + closeH) {
            visible = false;
            placementMode = false;
            return;
        }

        // Now check header for dragging
        if (mouseX >= windowX && mouseX <= windowX + windowWidth &&
            mouseY >= windowY && mouseY <= windowY + headerHeight) {
            draggingWindow = true;
            dragOffsetX = mouseX - windowX;
            dragOffsetY = mouseY - windowY;
            return;
        }


        
        // Check if clicking furniture list
        if (mouseX >= windowX + 10 && mouseX <= windowX + listWidth) {
            int listY = windowY + headerHeight + 10;
            int index = (mouseY - listY + scrollOffset) / itemHeight;
            
            if (index >= 0 && index < furnitureList.size()) {
                selectedFurniture = furnitureList.get(index);
            }
        }
        
        // Check if clicking "Place" button
        if (selectedFurniture != null) {
            int placeButtonX = windowX + listWidth + 20;
            int placeButtonY = windowY + windowHeight - 60;
            int placeButtonWidth = 100;
            int placeButtonHeight = 35;
            
            if (mouseX >= placeButtonX && mouseX <= placeButtonX + placeButtonWidth &&
                mouseY >= placeButtonY && mouseY <= placeButtonY + placeButtonHeight) {
                enterPlacementMode();
            }
        }
    }
    
    public void handleDrag(int mouseX, int mouseY) {
        if (draggingWindow) {
            windowX = mouseX - dragOffsetX;
            windowY = mouseY - dragOffsetY;
        }
    }
    
    public void handleRelease() {
        draggingWindow = false;
    }
    
    private void enterPlacementMode() {
        placementMode = true;
        furnitureToPlace = selectedFurniture.copy();
        visible = false; // Hide inventory during placement
    }
    
    public void updatePlacementPreview(int mouseX, int mouseY) {
        if (!placementMode) return;
        
        // Convert mouse position to tile coordinates
        Point tilePoint = gp.getCalculateTileFromMouse(mouseX, mouseY);
        previewMapX = tilePoint.x;
        previewMapY = tilePoint.y;
    }
    
    public void confirmPlacement() {
        if (!placementMode || furnitureToPlace == null) return;
        
        // Check if placement is within bounds
        if (previewMapX >= 0 && previewMapY >= 0 &&
            previewMapX + furnitureToPlace.tileWidth <= gp.maxWorldCol &&
            previewMapY + furnitureToPlace.tileHeight <= gp.maxWorldRow) {
            
            furnitureToPlace.place(previewMapX, previewMapY);
            gp.furnitureManager.addFurniture(furnitureToPlace);
        }
        
        placementMode = false;
        furnitureToPlace = null;
    }
    
    public void cancelPlacement() {
        placementMode = false;
        furnitureToPlace = null;
    }
    
    public void draw(Graphics2D g2d) {
        if (!visible) return;
        
        // Draw window background
        g2d.setColor(new Color(255, 255, 255, 240));
        g2d.fillRoundRect(windowX, windowY, windowWidth, windowHeight, 20, 20);
        
        // Draw border
        g2d.setColor(new Color(0, 102, 204));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(windowX, windowY, windowWidth, windowHeight, 20, 20);
        
        // Draw header
        g2d.setColor(new Color(0, 102, 204));
        g2d.fillRoundRect(windowX, windowY, windowWidth, headerHeight, 20, 20);
        g2d.fillRect(windowX, windowY + 20, windowWidth, 20);
        
        // Draw title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Inventory", windowX + 15, windowY + 27);
        
        // Draw close button
        g2d.setColor(Color.WHITE);
        g2d.fillOval(windowX + windowWidth - 30, windowY + 10, 20, 20);
        g2d.setColor(new Color(0, 102, 204));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("X", windowX + windowWidth - 24, windowY + 24);
        
        // Draw divider
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(windowX + listWidth, windowY + headerHeight, 
                     windowX + listWidth, windowY + windowHeight);
        
        // Draw furniture list
        drawFurnitureList(g2d);
        
        // Draw preview panel
        if (selectedFurniture != null) {
            drawPreviewPanel(g2d);
        }
    }
    
    private void drawFurnitureList(Graphics2D g2d) {
        int listX = windowX + 10;
        int listY = windowY + headerHeight + 10;
        
        g2d.setClip(listX, listY, listWidth - 20, windowHeight - headerHeight - 20);
        
        for (int i = 0; i < furnitureList.size(); i++) {
            Furniture furniture = furnitureList.get(i);
            int itemY = listY + (i * itemHeight) - scrollOffset;
            
            // Skip if not visible
            if (itemY + itemHeight < listY || itemY > listY + windowHeight - headerHeight) {
                continue;
            }
            
            // Highlight selected
            if (furniture == selectedFurniture) {
                g2d.setColor(new Color(200, 220, 255));
                g2d.fillRoundRect(listX, itemY, listWidth - 20, itemHeight - 5, 10, 10);
            }
            
            // Draw thumbnail
            if (furniture.image != null) {
                int thumbSize = 40;
                g2d.drawImage(furniture.image, listX + 5, itemY + 5, thumbSize, thumbSize, null);
            }
            
            // Draw name
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            g2d.drawString(furniture.name, listX + 50, itemY + 25);
            
            // Draw dimensions
            g2d.setFont(new Font("Arial", Font.ITALIC, 11));
            g2d.setColor(Color.GRAY);
            g2d.drawString(furniture.tileWidth + "x" + furniture.tileHeight + " tiles", 
                          listX + 50, itemY + 40);
        }
        
        g2d.setClip(null);
    }
    
    private void drawPreviewPanel(Graphics2D g2d) {
        int previewX = windowX + listWidth + 20;
        int previewY = windowY + headerHeight + 20;
        
        // Draw preview title
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Preview", previewX, previewY);
        
        // Draw preview image
        if (selectedFurniture.image != null) {
            int previewImgSize = 150;
            int imgX = previewX + (previewWidth - previewImgSize) / 2 - 20;
            int imgY = previewY + 20;
            
            g2d.drawImage(selectedFurniture.image, imgX, imgY, 
                         previewImgSize, previewImgSize, null);
        }
        
        // Draw info
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(Color.BLACK);
        g2d.drawString("Name: " + selectedFurniture.name, previewX, previewY + 190);
        g2d.drawString("Size: " + selectedFurniture.tileWidth + "x" + 
                      selectedFurniture.tileHeight + " tiles", previewX, previewY + 210);
        
        // Draw "Place" button
        int buttonX = previewX;
        int buttonY = windowY + windowHeight - 60;
        int buttonWidth = 100;
        int buttonHeight = 35;
        
        g2d.setColor(new Color(76, 175, 80));
        g2d.fillRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 10, 10);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        String buttonText = "Place";
        int textX = buttonX + (buttonWidth - fm.stringWidth(buttonText)) / 2;
        int textY = buttonY + ((buttonHeight - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(buttonText, textX, textY);
    }
    
    public void drawPlacementPreview(Graphics2D g2d) {
        if (!placementMode || furnitureToPlace == null) return;
        
        // Draw tile grid preview
        for (int x = 0; x < furnitureToPlace.tileWidth; x++) {
            for (int y = 0; y < furnitureToPlace.tileHeight; y++) {
                int tileX = previewMapX + x;
                int tileY = previewMapY + y;
                
                if (tileX >= 0 && tileX < gp.maxWorldCol && tileY >= 0 && tileY < gp.maxWorldRow) {
                    int isoX = (tileX - tileY) * (gp.tileSizeWidth / 2) + gp.tile_manager.xOffset;
                    int isoY = (tileX + tileY) * (gp.tileSizeHeight / 2) + gp.tile_manager.yOffset;
                    
                    // Draw green semi-transparent tile
                    g2d.setColor(new Color(0, 255, 0, 100));
                    g2d.fillPolygon(
                        new int[]{
                            isoX + gp.tileSizeWidth / 2,
                            isoX + gp.tileSizeWidth,
                            isoX + gp.tileSizeWidth / 2,
                            isoX
                        },
                        new int[]{
                            isoY,
                            isoY + gp.tileSizeHeight / 2,
                            isoY + gp.tileSizeHeight,
                            isoY + gp.tileSizeHeight / 2
                        },
                        4
                    );
                    
                    // Draw border
                    g2d.setColor(new Color(0, 200, 0));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawPolygon(
                        new int[]{
                            isoX + gp.tileSizeWidth / 2,
                            isoX + gp.tileSizeWidth,
                            isoX + gp.tileSizeWidth / 2,
                            isoX
                        },
                        new int[]{
                            isoY,
                            isoY + gp.tileSizeHeight / 2,
                            isoY + gp.tileSizeHeight,
                            isoY + gp.tileSizeHeight / 2
                        },
                        4
                    );
                }
            }
        }
        
        // Draw furniture preview at position
        if (furnitureToPlace.image != null) {
            int isoX = (previewMapX - previewMapY) * (gp.tileSizeWidth / 2) + gp.tile_manager.xOffset;
            int isoY = (previewMapX + previewMapY) * (gp.tileSizeHeight / 2) + gp.tile_manager.yOffset;
            
            int drawWidth = furnitureToPlace.tileWidth * gp.tileSizeWidth;
            int drawHeight = furnitureToPlace.image.getHeight() * (drawWidth / furnitureToPlace.image.getWidth());
            
            // Draw semi-transparent preview
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g2d.drawImage(furnitureToPlace.image, isoX, isoY - drawHeight + gp.tileSizeHeight,
                         drawWidth, drawHeight, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }
}