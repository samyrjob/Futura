package view.inventory;

import view.shared.BasePanelLayout;
import java.awt.*;

/**
 * InventoryPanelLayout - Layout constants for Inventory panel
 * 
 * Part of MVC Architecture:
 * - Extends BasePanelLayout for shared constants
 * - Defines inventory-specific dimensions (two-panel layout)
 * - Used by InventoryPanelRenderer for drawing
 */
public class InventoryPanelLayout extends BasePanelLayout {
    
    // ═══════════════════════════════════════════════════════════
    // WINDOW DIMENSIONS
    // ═══════════════════════════════════════════════════════════
    
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 400;
    
    // ═══════════════════════════════════════════════════════════
    // FURNITURE LIST PANEL (Left side)
    // ═══════════════════════════════════════════════════════════
    
    public static final int LIST_WIDTH = 250;
    public static final int LIST_PADDING = 10;
    public static final int FURNITURE_ITEM_HEIGHT = 50;
    public static final int FURNITURE_ITEM_MARGIN = 5;
    public static final int THUMBNAIL_SIZE = 40;
    public static final int THUMBNAIL_MARGIN = 5;
    
    // ═══════════════════════════════════════════════════════════
    // PREVIEW PANEL (Right side)
    // ═══════════════════════════════════════════════════════════
    
    public static final int PREVIEW_PANEL_X_OFFSET = LIST_WIDTH + 20;
    public static final int PREVIEW_PANEL_WIDTH = 350;
    public static final int PREVIEW_IMAGE_SIZE = 150;
    public static final int PREVIEW_TITLE_Y = 20;
    public static final int PREVIEW_IMAGE_Y = 20;
    public static final int PREVIEW_INFO_Y = 190;
    
    // ═══════════════════════════════════════════════════════════
    // PLACE BUTTON
    // ═══════════════════════════════════════════════════════════
    
    public static final int PLACE_BUTTON_WIDTH = 100;
    public static final int PLACE_BUTTON_HEIGHT = 35;
    public static final int PLACE_BUTTON_MARGIN_BOTTOM = 60;
    public static final int PLACE_BUTTON_RADIUS = 10;
    
    // ═══════════════════════════════════════════════════════════
    // COLORS - Blue theme (same as Rooms)
    // ═══════════════════════════════════════════════════════════
    
    public static final Color HEADER_COLOR_INVENTORY = new Color(0, 102, 204);
    public static final Color SELECTION_HIGHLIGHT = new Color(200, 220, 255);
    public static final Color PLACE_BUTTON_COLOR = new Color(76, 175, 80);
    public static final Color PLACE_BUTTON_HOVER = new Color(56, 142, 60);
    public static final Color PLACEMENT_PREVIEW = new Color(0, 255, 0, 100);
    public static final Color PLACEMENT_BORDER = new Color(0, 200, 0);
    public static final Color DIVIDER_LINE = new Color(200, 200, 200);
    
    // ═══════════════════════════════════════════════════════════
    // FONTS
    // ═══════════════════════════════════════════════════════════
    
    public static final Font PREVIEW_TITLE_FONT = new Font("Arial", Font.BOLD, 16);
    public static final Font INFO_FONT = new Font("Arial", Font.PLAIN, 14);
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public InventoryPanelLayout(int windowX, int windowY) {
        super(windowX, windowY);
    }
    
    // ═══════════════════════════════════════════════════════════
    // ABSTRACT METHOD IMPLEMENTATIONS
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public int getWindowWidth() {
        return WINDOW_WIDTH;
    }
    
    @Override
    public int getWindowHeight() {
        return WINDOW_HEIGHT;
    }
    
    @Override
    public Color getHeaderColor() {
        return HEADER_COLOR_INVENTORY;
    }
    
    @Override
    public String getTitle() {
        return "Inventory";
    }
    
    @Override
    public String getIcon() {
        return "📦";
    }
    
    // ═══════════════════════════════════════════════════════════
    // FURNITURE LIST BOUNDS
    // ═══════════════════════════════════════════════════════════
    
    public Rectangle getListBounds() {
        return new Rectangle(
            windowX + LIST_PADDING,
            windowY + HEADER_HEIGHT + LIST_PADDING,
            LIST_WIDTH - 2 * LIST_PADDING,
            WINDOW_HEIGHT - HEADER_HEIGHT - 2 * LIST_PADDING
        );
    }
    
    public Rectangle getFurnitureItemBounds(int index, int scrollOffset) {
        Rectangle listBounds = getListBounds();
        
        int itemX = listBounds.x;
        int itemY = listBounds.y + (index * FURNITURE_ITEM_HEIGHT) - scrollOffset;
        int itemWidth = listBounds.width;
        int itemHeight = FURNITURE_ITEM_HEIGHT - FURNITURE_ITEM_MARGIN;
        
        return new Rectangle(itemX, itemY, itemWidth, itemHeight);
    }
    
    public int getFurnitureIndexAtY(int mouseY, int scrollOffset) {
        Rectangle listBounds = getListBounds();
        
        if (mouseY < listBounds.y || mouseY > listBounds.y + listBounds.height) {
            return -1;
        }
        
        return (mouseY - listBounds.y + scrollOffset) / FURNITURE_ITEM_HEIGHT;
    }
    
    public int getListMaxScrollOffset(int itemCount) {
        Rectangle listBounds = getListBounds();
        int totalHeight = itemCount * FURNITURE_ITEM_HEIGHT;
        int visibleHeight = listBounds.height;
        
        return Math.max(0, totalHeight - visibleHeight);
    }
    
    // ═══════════════════════════════════════════════════════════
    // PREVIEW PANEL BOUNDS
    // ═══════════════════════════════════════════════════════════
    
    public int getPreviewX() {
        return windowX + PREVIEW_PANEL_X_OFFSET;
    }
    
    public int getPreviewY() {
        return windowY + HEADER_HEIGHT + PREVIEW_TITLE_Y;
    }
    
    public Rectangle getPreviewBounds() {
        return new Rectangle(
            getPreviewX(),
            getPreviewY(),
            PREVIEW_PANEL_WIDTH,
            WINDOW_HEIGHT - HEADER_HEIGHT - 20
        );
    }
    
    // ═══════════════════════════════════════════════════════════
    // PLACE BUTTON BOUNDS
    // ═══════════════════════════════════════════════════════════
    
    public Rectangle getPlaceButtonBounds() {
        return new Rectangle(
            windowX + PREVIEW_PANEL_X_OFFSET,
            windowY + WINDOW_HEIGHT - PLACE_BUTTON_MARGIN_BOTTOM,
            PLACE_BUTTON_WIDTH,
            PLACE_BUTTON_HEIGHT
        );
    }
    
    // ═══════════════════════════════════════════════════════════
    // DIVIDER LINE
    // ═══════════════════════════════════════════════════════════
    
    public int getDividerX() {
        return windowX + LIST_WIDTH;
    }
    
    public int getDividerStartY() {
        return windowY + HEADER_HEIGHT;
    }
    
    public int getDividerEndY() {
        return windowY + WINDOW_HEIGHT;
    }
}