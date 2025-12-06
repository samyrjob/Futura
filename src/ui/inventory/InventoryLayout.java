package ui.inventory;

import java.awt.*;

/**
 * Centralized layout constants and bounds calculation for InventoryWindow
 * Single source of truth for all UI dimensions and positions
 */
public class InventoryLayout {
    
    // ═══════════════════════════════════════════════════════════
    // WINDOW DIMENSIONS
    // ═══════════════════════════════════════════════════════════
    
    public static final int WINDOW_WIDTH = 600;
    public static final int WINDOW_HEIGHT = 400;
    public static final int HEADER_HEIGHT = 40;
    public static final int WINDOW_BORDER_RADIUS = 20;
    public static final int WINDOW_BORDER_WIDTH = 3;
    
    // ═══════════════════════════════════════════════════════════
    // FURNITURE LIST PANEL
    // ═══════════════════════════════════════════════════════════
    
    public static final int LIST_WIDTH = 250;
    public static final int LIST_PADDING = 10;
    public static final int ITEM_HEIGHT = 50;
    public static final int ITEM_MARGIN = 5;
    public static final int THUMBNAIL_SIZE = 40;
    public static final int THUMBNAIL_MARGIN = 5;
    
    // ═══════════════════════════════════════════════════════════
    // PREVIEW PANEL
    // ═══════════════════════════════════════════════════════════
    
    public static final int PREVIEW_PANEL_X_OFFSET = LIST_WIDTH + 20;
    public static final int PREVIEW_PANEL_WIDTH = 350;
    public static final int PREVIEW_IMAGE_SIZE = 150;
    public static final int PREVIEW_TITLE_Y = 20;
    public static final int PREVIEW_IMAGE_Y = 20;
    public static final int PREVIEW_INFO_Y = 190;
    
    // ═══════════════════════════════════════════════════════════
    // BUTTONS
    // ═══════════════════════════════════════════════════════════
    
    public static final int CLOSE_BUTTON_SIZE = 20;
    public static final int CLOSE_BUTTON_MARGIN = 10;
    
    public static final int PLACE_BUTTON_WIDTH = 100;
    public static final int PLACE_BUTTON_HEIGHT = 35;
    public static final int PLACE_BUTTON_MARGIN_BOTTOM = 60;
    public static final int PLACE_BUTTON_RADIUS = 10;
    
    // ═══════════════════════════════════════════════════════════
    // COLORS
    // ═══════════════════════════════════════════════════════════
    
    public static final Color WINDOW_BG = new Color(255, 255, 255, 240);
    public static final Color HEADER_BG = new Color(0, 102, 204);
    public static final Color HEADER_TEXT = Color.WHITE;
    public static final Color BORDER = new Color(0, 102, 204);
    public static final Color DIVIDER = new Color(200, 200, 200);
    public static final Color SELECTION_HIGHLIGHT = new Color(200, 220, 255);
    public static final Color PLACE_BUTTON = new Color(76, 175, 80);
    public static final Color PLACEMENT_PREVIEW = new Color(0, 255, 0, 100);
    public static final Color PLACEMENT_BORDER = new Color(0, 200, 0);
    public static final Color CLOSE_BUTTON_HOVER = Color.RED;
    public static final Color CLOSE_BUTTON_NORMAL = Color.WHITE;
    
    // ═══════════════════════════════════════════════════════════
    // FONTS
    // ═══════════════════════════════════════════════════════════
    
    public static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 18);
    public static final Font PREVIEW_TITLE_FONT = new Font("Arial", Font.BOLD, 16);
    public static final Font ITEM_NAME_FONT = new Font("Arial", Font.PLAIN, 14);
    public static final Font ITEM_SIZE_FONT = new Font("Arial", Font.ITALIC, 11);
    public static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 14);
    public static final Font INFO_FONT = new Font("Arial", Font.PLAIN, 14);
    
    // ═══════════════════════════════════════════════════════════
    // BOUNDS CALCULATOR
    // ═══════════════════════════════════════════════════════════
    
    private final int windowX;
    private final int windowY;
    
    // Cached bounds
    public final Rectangle closeButtonBounds;
    public final Rectangle headerBounds;
    public final Rectangle listBounds;
    public Rectangle placeButtonBounds; // Can be null if no selection
    
    public InventoryLayout(int windowX, int windowY) {
        this.windowX = windowX;
        this.windowY = windowY;
        
        // Calculate all bounds once
        this.closeButtonBounds = calculateCloseButtonBounds();
        this.headerBounds = calculateHeaderBounds();
        this.listBounds = calculateListBounds();
    }
    
    public void updatePlaceButtonBounds(boolean hasSelection) {
        if (hasSelection) {
            this.placeButtonBounds = calculatePlaceButtonBounds();
        } else {
            this.placeButtonBounds = null;
        }
    }
    
    private Rectangle calculateCloseButtonBounds() {
        return new Rectangle(
            windowX + WINDOW_WIDTH - CLOSE_BUTTON_SIZE - CLOSE_BUTTON_MARGIN,
            windowY + CLOSE_BUTTON_MARGIN,
            CLOSE_BUTTON_SIZE,
            CLOSE_BUTTON_SIZE
        );
    }
    
    private Rectangle calculateHeaderBounds() {
        return new Rectangle(
            windowX,
            windowY,
            WINDOW_WIDTH,
            HEADER_HEIGHT
        );
    }
    
    private Rectangle calculateListBounds() {
        return new Rectangle(
            windowX + LIST_PADDING,
            windowY + HEADER_HEIGHT + LIST_PADDING,
            LIST_WIDTH - 2 * LIST_PADDING,
            WINDOW_HEIGHT - HEADER_HEIGHT - 2 * LIST_PADDING
        );
    }
    
    private Rectangle calculatePlaceButtonBounds() {
        return new Rectangle(
            windowX + PREVIEW_PANEL_X_OFFSET,
            windowY + WINDOW_HEIGHT - PLACE_BUTTON_MARGIN_BOTTOM,
            PLACE_BUTTON_WIDTH,
            PLACE_BUTTON_HEIGHT
        );
    }
    
    public int getWindowX() {
        return windowX;
    }
    
    public int getWindowY() {
        return windowY;
    }
    
    public int getPreviewX() {
        return windowX + PREVIEW_PANEL_X_OFFSET;
    }
    
    public int getPreviewY() {
        return windowY + HEADER_HEIGHT + PREVIEW_TITLE_Y;
    }
}
