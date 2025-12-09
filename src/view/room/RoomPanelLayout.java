package view.room;

import view.shared.BasePanelLayout;
import model.room.Room;
import java.awt.*;

/**
 * RoomPanelLayout - Layout constants for Room Navigator panel
 * 
 * Part of MVC Architecture:
 * - Extends BasePanelLayout for shared constants
 * - Defines room-specific dimensions, colors, and positions
 * - Used by RoomPanelRenderer for drawing
 */
public class RoomPanelLayout extends BasePanelLayout {
    
    // ═══════════════════════════════════════════════════════════
    // WINDOW DIMENSIONS
    // ═══════════════════════════════════════════════════════════
    
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 500;
    
    // ═══════════════════════════════════════════════════════════
    // ROOM-SPECIFIC DIMENSIONS
    // ═══════════════════════════════════════════════════════════
    
    public static final int TAB_HEIGHT = 30;
    public static final int TAB_WIDTH = 100;
    public static final int TAB_Y_OFFSET = 10;  // Below header
    
    public static final int ROOM_ITEM_HEIGHT = 60;
    public static final int ROOM_ITEM_PADDING = 5;
    
    public static final int CREATE_BUTTON_WIDTH = 120;
    public static final int CREATE_BUTTON_HEIGHT = 35;
    public static final int CREATE_BUTTON_MARGIN = 20;
    
    public static final int SEARCH_BAR_HEIGHT = 30;
    
    // ═══════════════════════════════════════════════════════════
    // COLORS - Blue theme for Rooms (vs Purple for Friends)
    // ═══════════════════════════════════════════════════════════
    
    public static final Color HEADER_COLOR = new Color(0, 102, 204);      // Blue
    public static final Color HEADER_LIGHT = new Color(30, 144, 255);     // Dodger Blue
    
    public static final Color TAB_ACTIVE = new Color(0, 102, 204);
    public static final Color TAB_INACTIVE = new Color(200, 200, 200);
    
    public static final Color ROOM_ITEM_BG = new Color(240, 240, 240);
    public static final Color ROOM_ITEM_HOVER = new Color(200, 220, 255);
    public static final Color ROOM_CURRENT = new Color(144, 238, 144);    // Light green
    public static final Color ROOM_LOCKED = new Color(255, 200, 200);     // Light red
    public static final Color ROOM_PRIVATE = new Color(255, 240, 200);    // Light yellow
    
    public static final Color CREATE_BUTTON_COLOR = new Color(76, 175, 80);
    public static final Color CREATE_BUTTON_HOVER = new Color(56, 142, 60);
    
    public static final Color FAVORITE_STAR = new Color(255, 193, 7);     // Amber
    
    // ═══════════════════════════════════════════════════════════
    // TAB DEFINITIONS
    // ═══════════════════════════════════════════════════════════
    
    public enum Tab {
        PUBLIC_ROOMS("Public Rooms", "🌐"),
        MY_ROOMS("My Rooms", "🏠"),
        FAVORITES("Favorites", "⭐");
        
        public final String label;
        public final String icon;
        
        Tab(String label, String icon) {
            this.label = label;
            this.icon = icon;
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR (matches BasePanelLayout)
    // ═══════════════════════════════════════════════════════════
    
    public RoomPanelLayout(int windowX, int windowY) {
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
        return HEADER_COLOR;
    }
    
    @Override
    public String getTitle() {
        return "Room Navigator";
    }
    
    @Override
    public String getIcon() {
        return "🏠";
    }
    
    // ═══════════════════════════════════════════════════════════
    // TAB BOUNDS
    // ═══════════════════════════════════════════════════════════
    
    public Rectangle getTabBounds(Tab tab) {
        int tabY = windowY + HEADER_HEIGHT + TAB_Y_OFFSET;
        int tabX;
        
        switch (tab) {
            case PUBLIC_ROOMS:
                tabX = windowX + 15;
                break;
            case MY_ROOMS:
                tabX = windowX + 15 + TAB_WIDTH + 10;
                break;
            case FAVORITES:
                tabX = windowX + 15 + (TAB_WIDTH + 10) * 2;
                break;
            default:
                tabX = windowX + 15;
        }
        
        return new Rectangle(tabX, tabY, TAB_WIDTH, TAB_HEIGHT);
    }
    
    // ═══════════════════════════════════════════════════════════
    // ROOM LIST BOUNDS
    // ═══════════════════════════════════════════════════════════
    
    public Rectangle getRoomListBounds() {
        int listX = windowX + 10;
        int listY = windowY + HEADER_HEIGHT + TAB_Y_OFFSET + TAB_HEIGHT + 15;
        int listWidth = WINDOW_WIDTH - 20;
        int listHeight = WINDOW_HEIGHT - HEADER_HEIGHT - TAB_HEIGHT - TAB_Y_OFFSET - 
                        CREATE_BUTTON_HEIGHT - CREATE_BUTTON_MARGIN * 2 - 20;
        
        return new Rectangle(listX, listY, listWidth, listHeight);
    }
    
    public Rectangle getRoomItemBounds(int index, int scrollOffset) {
        Rectangle listBounds = getRoomListBounds();
        
        int itemX = listBounds.x;
        int itemY = listBounds.y + (index * ROOM_ITEM_HEIGHT) - scrollOffset;
        int itemWidth = listBounds.width;
        int itemHeight = ROOM_ITEM_HEIGHT - ROOM_ITEM_PADDING;
        
        return new Rectangle(itemX, itemY, itemWidth, itemHeight);
    }
    
    // ═══════════════════════════════════════════════════════════
    // CREATE BUTTON BOUNDS
    // ═══════════════════════════════════════════════════════════
    
    public Rectangle getCreateButtonBounds() {
        int buttonX = windowX + CREATE_BUTTON_MARGIN;
        int buttonY = windowY + WINDOW_HEIGHT - CREATE_BUTTON_HEIGHT - CREATE_BUTTON_MARGIN;
        
        return new Rectangle(buttonX, buttonY, CREATE_BUTTON_WIDTH, CREATE_BUTTON_HEIGHT);
    }
    
    // ═══════════════════════════════════════════════════════════
    // SCROLLBAR CALCULATIONS
    // ═══════════════════════════════════════════════════════════
    
    public int getRoomListMaxScrollOffset(int itemCount) {
        Rectangle listBounds = getRoomListBounds();
        int totalHeight = itemCount * ROOM_ITEM_HEIGHT;
        int visibleHeight = listBounds.height;
        
        return Math.max(0, totalHeight - visibleHeight);
    }
    
    public int getRoomIndexAtY(int mouseY, int scrollOffset) {
        Rectangle listBounds = getRoomListBounds();
        
        if (mouseY < listBounds.y || mouseY > listBounds.y + listBounds.height) {
            return -1;
        }
        
        return (mouseY - listBounds.y + scrollOffset) / ROOM_ITEM_HEIGHT;
    }
    
    // ═══════════════════════════════════════════════════════════
    // ROOM TYPE COLORS
    // ═══════════════════════════════════════════════════════════
    
    public Color getRoomTypeColor(Room.RoomType type) {
        switch (type) {
            case PUBLIC:
                return ROOM_ITEM_BG;
            case PRIVATE:
                return ROOM_PRIVATE;
            case LOCKED:
                return ROOM_LOCKED;
            default:
                return ROOM_ITEM_BG;
        }
    }
    
    public String getRoomTypeIcon(Room.RoomType type) {
        switch (type) {
            case PUBLIC:
                return "🌐";
            case PRIVATE:
                return "🔒";
            case LOCKED:
                return "🔑";
            default:
                return "";
        }
    }
}