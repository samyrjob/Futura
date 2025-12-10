package view.shared;

import java.awt.*;

/**
 * Abstract base class for panel layout constants and bounds calculation.
 * Each toolbar panel (Friends, Inventory, Rooms) extends this with its own dimensions/colors.
 * 
 * This follows the MVC pattern - Layout is part of the View layer.
 * Single source of truth for UI dimensions, colors, and fonts.
 */
public abstract class BasePanelLayout {
    
    // ═══════════════════════════════════════════════════════════
    // SHARED CONSTANTS (Same for ALL panels)
    // ═══════════════════════════════════════════════════════════
    
    // Header
    public static final int HEADER_HEIGHT = 45;
    public static final int CLOSE_BUTTON_SIZE = 25;
    public static final int CLOSE_BUTTON_MARGIN = 10;
    
    // Window styling
    public static final int BORDER_RADIUS = 20;
    public static final int BORDER_WIDTH = 3;
    public static final int SHADOW_OFFSET = 5;
    
    // Content area
    public static final int CONTENT_PADDING = 10;
    public static final int ITEM_HEIGHT = 55;
    public static final int ITEM_MARGIN = 5;
    public static final int ITEM_BORDER_RADIUS = 12;
    
    // Scrollbar
    public static final int SCROLLBAR_WIDTH = 8;
    public static final int SCROLLBAR_MIN_THUMB = 30;
    
    // ═══════════════════════════════════════════════════════════
    // SHARED COLORS
    // ═══════════════════════════════════════════════════════════
    
    public static final Color WINDOW_BG = new Color(255, 255, 255, 245);
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 40);
    public static final Color CLOSE_BUTTON_NORMAL = new Color(255, 255, 255, 180);
    public static final Color CLOSE_BUTTON_HOVER = new Color(255, 82, 82);
    public static final Color DIVIDER_COLOR = new Color(224, 224, 224);
    public static final Color ITEM_BG = new Color(248, 248, 248);
    public static final Color ITEM_HOVER = new Color(237, 231, 246);
    public static final Color ITEM_SELECTED = new Color(225, 190, 231);
    public static final Color SCROLLBAR_TRACK = new Color(230, 230, 230);
    
    // ═══════════════════════════════════════════════════════════
    // SHARED FONTS
    // ═══════════════════════════════════════════════════════════
    
    public static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 18);
    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 16);
    public static final Font ITEM_NAME_FONT = new Font("Arial", Font.BOLD, 14);
    public static final Font ITEM_SUBTITLE_FONT = new Font("Arial", Font.PLAIN, 11);
    public static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 14);
    public static final Font BADGE_FONT = new Font("Arial", Font.BOLD, 12);
    public static final Font EMPTY_STATE_FONT = new Font("Arial", Font.ITALIC, 14);
    public static final Font ICON_FONT = new Font("Segoe UI Emoji", Font.PLAIN, 20);
    
    // ═══════════════════════════════════════════════════════════
    // INSTANCE STATE (Position tracking)
    // ═══════════════════════════════════════════════════════════
    
    protected int windowX;
    protected int windowY;
    
    // ═══════════════════════════════════════════════════════════
    // ABSTRACT METHODS (Each panel MUST implement)
    // ═══════════════════════════════════════════════════════════
    
    /** Window width in pixels */
    public abstract int getWindowWidth();
    
    /** Window height in pixels */
    public abstract int getWindowHeight();
    
    /** Header background color (theme color for this panel) */
    public abstract Color getHeaderColor();
    
    /** Panel title displayed in header */
    public abstract String getTitle();
    
    /** Icon/emoji displayed in header */
    public abstract String getIcon();
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public BasePanelLayout(int windowX, int windowY) {
        this.windowX = windowX;
        this.windowY = windowY;
    }
    
    // ═══════════════════════════════════════════════════════════
    // POSITION MANAGEMENT
    // ═══════════════════════════════════════════════════════════
    
    public int getWindowX() {
        return windowX;
    }
    
    public int getWindowY() {
        return windowY;
    }
    
    public void setPosition(int x, int y) {
        this.windowX = x;
        this.windowY = y;
    }
    
    /**
     * Center the window on screen
     */
    public void centerOnScreen(int screenWidth, int screenHeight) {
        this.windowX = (screenWidth - getWindowWidth()) / 2;
        this.windowY = (screenHeight - getWindowHeight()) / 2;
    }
    
    /**
     * Constrain window position to screen bounds
     */
    public void constrainToScreen(int screenWidth, int screenHeight) {
        windowX = Math.max(0, Math.min(windowX, screenWidth - getWindowWidth()));
        windowY = Math.max(0, Math.min(windowY, screenHeight - getWindowHeight()));
    }
    
    // ═══════════════════════════════════════════════════════════
    // BOUNDS CALCULATIONS (Used for hit detection)
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Get the close button bounds for hit detection
     */
    public Rectangle getCloseButtonBounds() {
        return new Rectangle(
            windowX + getWindowWidth() - CLOSE_BUTTON_SIZE - CLOSE_BUTTON_MARGIN,
            windowY + CLOSE_BUTTON_MARGIN,
            CLOSE_BUTTON_SIZE,
            CLOSE_BUTTON_SIZE
        );
    }
    
    /**
     * Get the header bounds for drag detection
     */
    public Rectangle getHeaderBounds() {
        return new Rectangle(
            windowX,
            windowY,
            getWindowWidth(),
            HEADER_HEIGHT
        );
    }
    
    /**
     * Get the content area bounds (below header)
     */
    public Rectangle getContentBounds() {
        return new Rectangle(
            windowX + CONTENT_PADDING,
            windowY + HEADER_HEIGHT + CONTENT_PADDING,
            getWindowWidth() - (2 * CONTENT_PADDING),
            getWindowHeight() - HEADER_HEIGHT - (2 * CONTENT_PADDING)
        );
    }
    
    /**
     * Get the full window bounds
     */
    public Rectangle getWindowBounds() {
        return new Rectangle(windowX, windowY, getWindowWidth(), getWindowHeight());
    }
    
    /**
     * Check if a point is within the window
     */
    public boolean containsPoint(int x, int y) {
        return getWindowBounds().contains(x, y);
    }
    
    /**
     * Check if a point is on the header (for dragging)
     */
    public boolean isOnHeader(int x, int y) {
        return getHeaderBounds().contains(x, y) && !getCloseButtonBounds().contains(x, y);
    }
    
    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS FOR ITEM LISTS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Calculate which item index is at the given Y position
     * @param mouseY Mouse Y coordinate
     * @param scrollOffset Current scroll offset
     * @return Item index, or -1 if not on an item
     */
    public int getItemIndexAtY(int mouseY, int scrollOffset) {
        Rectangle content = getContentBounds();
        if (mouseY < content.y || mouseY > content.y + content.height) {
            return -1;
        }
        return (mouseY - content.y + scrollOffset) / ITEM_HEIGHT;
    }
    
    /**
     * Calculate the Y position for drawing an item
     * @param index Item index
     * @param scrollOffset Current scroll offset
     * @return Y coordinate for the item
     */
    public int getItemY(int index, int scrollOffset) {
        Rectangle content = getContentBounds();
        return content.y + (index * ITEM_HEIGHT) - scrollOffset;
    }
    
    /**
     * Calculate maximum scroll offset for a list
     * @param itemCount Number of items in the list
     * @return Maximum scroll offset
     */
    public int getMaxScrollOffset(int itemCount) {
        int contentHeight = itemCount * ITEM_HEIGHT;
        int viewHeight = getContentBounds().height;
        return Math.max(0, contentHeight - viewHeight);
    }
    
    /**
     * Check if an item is visible (within the content bounds)
     * @param itemY Y position of the item
     * @return true if visible
     */
    public boolean isItemVisible(int itemY) {
        Rectangle content = getContentBounds();
        return itemY + ITEM_HEIGHT >= content.y && itemY <= content.y + content.height;
    }
}