package message;

import object.Furniture;
import java.awt.*;

/**
 * Handles all rendering for InventoryWindow
 * Separated from logic for cleaner code organization
 */
public class InventoryWindowUI {
    
    private final InventoryLayout layout;
    private final FurnitureList furnitureList;
    private final boolean hoverCloseButton;
    
    public InventoryWindowUI(InventoryLayout layout, FurnitureList furnitureList, boolean hoverCloseButton) {
        this.layout = layout;
        this.furnitureList = furnitureList;
        this.hoverCloseButton = hoverCloseButton;
    }
    
    // ═══════════════════════════════════════════════════════════
    // MAIN DRAW METHOD
    // ═══════════════════════════════════════════════════════════
    
    public void draw(Graphics2D g2d) {
        drawWindow(g2d);
        drawHeader(g2d);
        drawCloseButton(g2d);
        drawDivider(g2d);
        drawFurnitureList(g2d);
        
        if (furnitureList.hasSelection()) {
            drawPreviewPanel(g2d);
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // WINDOW COMPONENTS
    // ═══════════════════════════════════════════════════════════
    
    private void drawWindow(Graphics2D g2d) {
        int x = layout.getWindowX();
        int y = layout.getWindowY();
        
        // Background
        g2d.setColor(InventoryLayout.WINDOW_BG);
        g2d.fillRoundRect(x, y, InventoryLayout.WINDOW_WIDTH, InventoryLayout.WINDOW_HEIGHT, 
                         InventoryLayout.WINDOW_BORDER_RADIUS, InventoryLayout.WINDOW_BORDER_RADIUS);
        
        // Border
        g2d.setColor(InventoryLayout.BORDER);
        g2d.setStroke(new BasicStroke(InventoryLayout.WINDOW_BORDER_WIDTH));
        g2d.drawRoundRect(x, y, InventoryLayout.WINDOW_WIDTH, InventoryLayout.WINDOW_HEIGHT, 
                         InventoryLayout.WINDOW_BORDER_RADIUS, InventoryLayout.WINDOW_BORDER_RADIUS);
    }
    
    private void drawHeader(Graphics2D g2d) {
        int x = layout.getWindowX();
        int y = layout.getWindowY();
        
        g2d.setColor(InventoryLayout.HEADER_BG);
        g2d.fillRoundRect(x, y, InventoryLayout.WINDOW_WIDTH, InventoryLayout.HEADER_HEIGHT, 
                         InventoryLayout.WINDOW_BORDER_RADIUS, InventoryLayout.WINDOW_BORDER_RADIUS);
        g2d.fillRect(x, y + 20, InventoryLayout.WINDOW_WIDTH, 20);
        
        g2d.setColor(InventoryLayout.HEADER_TEXT);
        g2d.setFont(InventoryLayout.HEADER_FONT);
        g2d.drawString("Inventory", x + 15, y + 27);
    }
    
    private void drawCloseButton(Graphics2D g2d) {
        Rectangle bounds = layout.closeButtonBounds;
        
        g2d.setColor(hoverCloseButton ? InventoryLayout.CLOSE_BUTTON_HOVER : InventoryLayout.CLOSE_BUTTON_NORMAL);
        g2d.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
        
        g2d.setColor(InventoryLayout.HEADER_BG);
        g2d.setFont(InventoryLayout.BUTTON_FONT);
        g2d.drawString("X", bounds.x + 6, bounds.y + 14);
    }
    
    private void drawDivider(Graphics2D g2d) {
        int x = layout.getWindowX();
        int y = layout.getWindowY();
        
        g2d.setColor(InventoryLayout.DIVIDER);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(
            x + InventoryLayout.LIST_WIDTH, 
            y + InventoryLayout.HEADER_HEIGHT,
            x + InventoryLayout.LIST_WIDTH, 
            y + InventoryLayout.WINDOW_HEIGHT
        );
    }
    
    // ═══════════════════════════════════════════════════════════
    // FURNITURE LIST
    // ═══════════════════════════════════════════════════════════
    
    private void drawFurnitureList(Graphics2D g2d) {
        Rectangle bounds = layout.listBounds;
        g2d.setClip(bounds);
        
        for (int i = 0; i < furnitureList.size(); i++) {
            drawFurnitureItem(g2d, furnitureList.getFurnitureAt(i), i);
        }
        
        g2d.setClip(null);
    }
    
    private void drawFurnitureItem(Graphics2D g2d, Furniture furniture, int index) {
        Rectangle bounds = layout.listBounds;
        int itemY = bounds.y + (index * InventoryLayout.ITEM_HEIGHT);
        
        // Skip if not visible
        if (itemY + InventoryLayout.ITEM_HEIGHT < bounds.y || 
            itemY > bounds.y + bounds.height) {
            return;
        }
        
        // Highlight if selected
        if (furnitureList.isSelected(furniture)) {
            g2d.setColor(InventoryLayout.SELECTION_HIGHLIGHT);
            g2d.fillRoundRect(bounds.x, itemY, bounds.width, 
                            InventoryLayout.ITEM_HEIGHT - InventoryLayout.ITEM_MARGIN, 10, 10);
        }
        
        // Draw thumbnail
        if (furniture.image != null) {
            g2d.drawImage(furniture.image, 
                         bounds.x + InventoryLayout.THUMBNAIL_MARGIN, 
                         itemY + InventoryLayout.THUMBNAIL_MARGIN, 
                         InventoryLayout.THUMBNAIL_SIZE, 
                         InventoryLayout.THUMBNAIL_SIZE, 
                         null);
        }
        
        // Draw name
        g2d.setColor(Color.BLACK);
        g2d.setFont(InventoryLayout.ITEM_NAME_FONT);
        g2d.drawString(furniture.name, bounds.x + 50, itemY + 25);
        
        // Draw dimensions
        g2d.setFont(InventoryLayout.ITEM_SIZE_FONT);
        g2d.setColor(Color.GRAY);
        g2d.drawString(
            furniture.tileWidth + "x" + furniture.tileHeight + " tiles",
            bounds.x + 50, 
            itemY + 40
        );
    }
    
    // ═══════════════════════════════════════════════════════════
    // PREVIEW PANEL
    // ═══════════════════════════════════════════════════════════
    
    private void drawPreviewPanel(Graphics2D g2d) {
        Furniture selected = furnitureList.getSelected();
        if (selected == null) return;
        
        int previewX = layout.getPreviewX();
        int previewY = layout.getPreviewY();
        
        // Title
        g2d.setColor(Color.BLACK);
        g2d.setFont(InventoryLayout.PREVIEW_TITLE_FONT);
        g2d.drawString("Preview", previewX, previewY);
        
        // Preview image
        if (selected.image != null) {
            int imgX = previewX + (InventoryLayout.PREVIEW_PANEL_WIDTH - InventoryLayout.PREVIEW_IMAGE_SIZE) / 2 - 20;
            int imgY = previewY + InventoryLayout.PREVIEW_IMAGE_Y;
            g2d.drawImage(selected.image, imgX, imgY, 
                         InventoryLayout.PREVIEW_IMAGE_SIZE, 
                         InventoryLayout.PREVIEW_IMAGE_SIZE, null);
        }
        
        // Info
        g2d.setFont(InventoryLayout.INFO_FONT);
        g2d.drawString("Name: " + selected.name, previewX, previewY + InventoryLayout.PREVIEW_INFO_Y);
        g2d.drawString(
            "Size: " + selected.tileWidth + "x" + selected.tileHeight + " tiles",
            previewX, previewY + InventoryLayout.PREVIEW_INFO_Y + 20
        );
        
        // Place button
        drawPlaceButton(g2d);
    }
    
    private void drawPlaceButton(Graphics2D g2d) {
        Rectangle bounds = layout.placeButtonBounds;
        if (bounds == null) return;
        
        g2d.setColor(InventoryLayout.PLACE_BUTTON);
        g2d.fillRoundRect(
            bounds.x, bounds.y,
            bounds.width, bounds.height,
            InventoryLayout.PLACE_BUTTON_RADIUS, 
            InventoryLayout.PLACE_BUTTON_RADIUS
        );
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(InventoryLayout.BUTTON_FONT);
        
        FontMetrics fm = g2d.getFontMetrics();
        String text = "Place";
        int textX = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
        int textY = bounds.y + ((bounds.height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }
}