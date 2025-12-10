package view.inventory;

import view.shared.PanelRenderer;
import view.shared.BasePanelLayout;
import object.Furniture;

import java.awt.*;
import java.util.List;

/**
 * InventoryPanelRenderer - Handles all rendering for Inventory panel
 * 
 * Part of MVC Architecture:
 * - Implements PanelRenderer interface
 * - Uses InventoryPanelLayout for positioning
 * - Draws furniture list, preview panel, place button
 * - Stateless (receives data each frame)
 */
public class InventoryPanelRenderer implements PanelRenderer {
    
    // State passed in before each render
    private List<Furniture> furnitureItems;
    private Furniture selectedFurniture;
    private Furniture hoveredFurniture;
    private int scrollOffset;
    private boolean hoverPlaceButton;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public InventoryPanelRenderer() {
    }
    
    // ═══════════════════════════════════════════════════════════
    // STATE SETTERS (called before render)
    // ═══════════════════════════════════════════════════════════
    
    public void setFurnitureItems(List<Furniture> items) {
        this.furnitureItems = items;
    }
    
    public void setSelectedFurniture(Furniture furniture) {
        this.selectedFurniture = furniture;
    }
    
    public void setHoveredFurniture(Furniture furniture) {
        this.hoveredFurniture = furniture;
    }
    
    public void setScrollOffset(int offset) {
        this.scrollOffset = offset;
    }
    
    public void setHoverPlaceButton(boolean hover) {
        this.hoverPlaceButton = hover;
    }
    
    // ═══════════════════════════════════════════════════════════
    // MAIN RENDER METHOD (implements PanelRenderer)
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public void render(Graphics2D g2d, BasePanelLayout baseLayout, boolean hoverCloseButton) {
        // Cast to our specific layout type
        InventoryPanelLayout layout = (InventoryPanelLayout) baseLayout;
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw base components using default interface methods
        drawWindowBackground(g2d, layout);
        drawHeader(g2d, layout);
        drawCloseButton(g2d, layout, hoverCloseButton);
        
        // Draw inventory-specific components
        drawDivider(g2d, layout);
        drawFurnitureList(g2d, layout);
        
        // Draw preview panel if something is selected
        if (selectedFurniture != null) {
            drawPreviewPanel(g2d, layout);
        } else {
            drawEmptyPreview(g2d, layout);
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // DIVIDER
    // ═══════════════════════════════════════════════════════════
    
    private void drawDivider(Graphics2D g2d, InventoryPanelLayout layout) {
        g2d.setColor(InventoryPanelLayout.DIVIDER_LINE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(
            layout.getDividerX(),
            layout.getDividerStartY(),
            layout.getDividerX(),
            layout.getDividerEndY()
        );
    }
    
    // ═══════════════════════════════════════════════════════════
    // FURNITURE LIST RENDERING
    // ═══════════════════════════════════════════════════════════
    
    private void drawFurnitureList(Graphics2D g2d, InventoryPanelLayout layout) {
        Rectangle listBounds = layout.getListBounds();
        
        // Set clip region
        Shape oldClip = g2d.getClip();
        g2d.setClip(listBounds.x, listBounds.y, listBounds.width, listBounds.height);
        
        if (furnitureItems == null || furnitureItems.isEmpty()) {
            drawEmptyState(g2d, layout, "📦", "No furniture in inventory", "Furniture items will appear here");
        } else {
            for (int i = 0; i < furnitureItems.size(); i++) {
                Furniture furniture = furnitureItems.get(i);
                Rectangle itemBounds = layout.getFurnitureItemBounds(i, scrollOffset);
                
                // Skip if outside visible area
                if (itemBounds.y + itemBounds.height < listBounds.y || 
                    itemBounds.y > listBounds.y + listBounds.height) {
                    continue;
                }
                
                drawFurnitureItem(g2d, layout, furniture, itemBounds);
            }
        }
        
        // Restore clip
        g2d.setClip(oldClip);
        
        // Draw scrollbar if needed
        if (furnitureItems != null && !furnitureItems.isEmpty()) {
            drawListScrollbar(g2d, layout);
        }
    }
    
    private void drawFurnitureItem(Graphics2D g2d, InventoryPanelLayout layout, 
                                   Furniture furniture, Rectangle bounds) {
        boolean isSelected = (furniture == selectedFurniture);
        boolean isHovered = (furniture == hoveredFurniture);
        
        // Background
        Color bgColor;
        if (isSelected) {
            bgColor = InventoryPanelLayout.SELECTION_HIGHLIGHT;
        } else if (isHovered) {
            bgColor = new Color(230, 240, 255);
        } else {
            bgColor = BasePanelLayout.ITEM_BG;
        }
        
        g2d.setColor(bgColor);
        g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
        
        // Border on selection
        if (isSelected) {
            g2d.setColor(layout.getHeaderColor());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
        }
        
        // Thumbnail
        if (furniture.image != null) {
            g2d.drawImage(furniture.image,
                bounds.x + InventoryPanelLayout.THUMBNAIL_MARGIN,
                bounds.y + InventoryPanelLayout.THUMBNAIL_MARGIN,
                InventoryPanelLayout.THUMBNAIL_SIZE,
                InventoryPanelLayout.THUMBNAIL_SIZE,
                null);
        } else {
            // Placeholder
            g2d.setColor(new Color(200, 200, 200));
            g2d.fillRect(
                bounds.x + InventoryPanelLayout.THUMBNAIL_MARGIN,
                bounds.y + InventoryPanelLayout.THUMBNAIL_MARGIN,
                InventoryPanelLayout.THUMBNAIL_SIZE,
                InventoryPanelLayout.THUMBNAIL_SIZE
            );
        }
        
        // Name
        g2d.setColor(Color.BLACK);
        g2d.setFont(BasePanelLayout.ITEM_NAME_FONT);
        g2d.drawString(furniture.name, bounds.x + 55, bounds.y + 22);
        
        // Dimensions
        g2d.setFont(BasePanelLayout.ITEM_SUBTITLE_FONT);
        g2d.setColor(Color.GRAY);
        g2d.drawString(
            furniture.tileWidth + "x" + furniture.tileHeight + " tiles",
            bounds.x + 55,
            bounds.y + 38
        );
    }
    
    private void drawListScrollbar(Graphics2D g2d, InventoryPanelLayout layout) {
        if (furnitureItems == null || furnitureItems.isEmpty()) return;
        
        Rectangle listBounds = layout.getListBounds();
        int totalHeight = furnitureItems.size() * InventoryPanelLayout.FURNITURE_ITEM_HEIGHT;
        
        if (totalHeight <= listBounds.height) return;
        
        // Scrollbar track
        int trackX = listBounds.x + listBounds.width - 8;
        int trackY = listBounds.y;
        int trackHeight = listBounds.height;
        
        g2d.setColor(new Color(220, 220, 220));
        g2d.fillRoundRect(trackX, trackY, 6, trackHeight, 3, 3);
        
        // Scrollbar thumb
        float visibleRatio = (float) listBounds.height / totalHeight;
        int thumbHeight = Math.max(30, (int)(trackHeight * visibleRatio));
        
        int maxScroll = layout.getListMaxScrollOffset(furnitureItems.size());
        float scrollRatio = maxScroll > 0 ? (float) scrollOffset / maxScroll : 0;
        int thumbY = trackY + (int)((trackHeight - thumbHeight) * scrollRatio);
        
        g2d.setColor(new Color(150, 150, 150));
        g2d.fillRoundRect(trackX, thumbY, 6, thumbHeight, 3, 3);
    }
    
    // ═══════════════════════════════════════════════════════════
    // PREVIEW PANEL RENDERING
    // ═══════════════════════════════════════════════════════════
    
    private void drawPreviewPanel(Graphics2D g2d, InventoryPanelLayout layout) {
        int previewX = layout.getPreviewX();
        int previewY = layout.getPreviewY();
        
        // Title
        g2d.setColor(Color.BLACK);
        g2d.setFont(InventoryPanelLayout.PREVIEW_TITLE_FONT);
        g2d.drawString("Preview", previewX, previewY);
        
        // Preview image
        if (selectedFurniture.image != null) {
            int imgSize = InventoryPanelLayout.PREVIEW_IMAGE_SIZE;
            int imgX = previewX + (InventoryPanelLayout.PREVIEW_PANEL_WIDTH - imgSize) / 2 - 20;
            int imgY = previewY + InventoryPanelLayout.PREVIEW_IMAGE_Y;
            
            // Draw image with border
            g2d.setColor(new Color(240, 240, 240));
            g2d.fillRoundRect(imgX - 5, imgY - 5, imgSize + 10, imgSize + 10, 10, 10);
            g2d.drawImage(selectedFurniture.image, imgX, imgY, imgSize, imgSize, null);
        }
        
        // Info
        g2d.setFont(InventoryPanelLayout.INFO_FONT);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Name: " + selectedFurniture.name, 
                      previewX, previewY + InventoryPanelLayout.PREVIEW_INFO_Y);
        g2d.drawString("Size: " + selectedFurniture.tileWidth + "x" + selectedFurniture.tileHeight + " tiles",
                      previewX, previewY + InventoryPanelLayout.PREVIEW_INFO_Y + 20);
        
        // Place button
        drawPlaceButton(g2d, layout);
    }
    
    private void drawEmptyPreview(Graphics2D g2d, InventoryPanelLayout layout) {
        Rectangle previewBounds = layout.getPreviewBounds();
        int centerX = previewBounds.x + previewBounds.width / 2;
        int centerY = previewBounds.y + previewBounds.height / 2;
        
        // Icon
        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String icon = "👆";
        g2d.drawString(icon, centerX - fm.stringWidth(icon) / 2, centerY - 20);
        
        // Message
        g2d.setColor(new Color(150, 150, 150));
        g2d.setFont(BasePanelLayout.ITEM_NAME_FONT);
        fm = g2d.getFontMetrics();
        String message = "Select furniture to preview";
        g2d.drawString(message, centerX - fm.stringWidth(message) / 2, centerY + 25);
    }
    
    private void drawPlaceButton(Graphics2D g2d, InventoryPanelLayout layout) {
        Rectangle bounds = layout.getPlaceButtonBounds();
        
        // Background
        g2d.setColor(hoverPlaceButton ? 
                    InventoryPanelLayout.PLACE_BUTTON_HOVER : 
                    InventoryPanelLayout.PLACE_BUTTON_COLOR);
        g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height,
                         InventoryPanelLayout.PLACE_BUTTON_RADIUS,
                         InventoryPanelLayout.PLACE_BUTTON_RADIUS);
        
        // Hover border
        if (hoverPlaceButton) {
            g2d.setColor(InventoryPanelLayout.PLACE_BUTTON_COLOR.darker());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height,
                             InventoryPanelLayout.PLACE_BUTTON_RADIUS,
                             InventoryPanelLayout.PLACE_BUTTON_RADIUS);
        }
        
        // Text
        g2d.setColor(Color.WHITE);
        g2d.setFont(BasePanelLayout.BUTTON_FONT);
        FontMetrics fm = g2d.getFontMetrics();
        String text = "Place";
        int textX = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
        int textY = bounds.y + ((bounds.height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }
}