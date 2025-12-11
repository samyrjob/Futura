package view.room;

import view.shared.PanelRenderer;
import view.shared.BasePanelLayout;
import model.room.Room;

import java.awt.*;
import java.util.List;

/**
 * RoomPanelRenderer - Handles all rendering for Room Navigator
 * 
 * Part of MVC Architecture:
 * - Implements PanelRenderer interface
 * - Uses RoomPanelLayout for positioning
 * - Draws tabs, room list, buttons
 * - Stateless (receives data each frame)
 */
public class RoomPanelRenderer implements PanelRenderer {
    
    // State passed in before each render
    private RoomPanelLayout.Tab currentTab;
    private List<Room> rooms;
    private Room currentRoom;
    private Room hoveredRoom;
    private int scrollOffset;
    private boolean hoverCreateButton;
    private RoomPanelLayout.Tab hoveredTab;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public RoomPanelRenderer() {
        this.currentTab = RoomPanelLayout.Tab.PUBLIC_ROOMS;
    }
    
    // ═══════════════════════════════════════════════════════════
    // STATE SETTERS (called before render)
    // ═══════════════════════════════════════════════════════════
    
    public void setCurrentTab(RoomPanelLayout.Tab tab) {
        this.currentTab = tab;
    }
    
    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
    
    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }
    
    public void setHoveredRoom(Room room) {
        this.hoveredRoom = room;
    }
    
    public void setScrollOffset(int offset) {
        this.scrollOffset = offset;
    }
    
    public void setHoverCreateButton(boolean hover) {
        this.hoverCreateButton = hover;
    }
    
    public void setHoveredTab(RoomPanelLayout.Tab tab) {
        this.hoveredTab = tab;
    }
    
    // ═══════════════════════════════════════════════════════════
    // MAIN RENDER METHOD (implements PanelRenderer)
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public void render(Graphics2D g2d, BasePanelLayout baseLayout, boolean hoverCloseButton) {
        // Cast to our specific layout type
        RoomPanelLayout layout = (RoomPanelLayout) baseLayout;
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw components using default methods from interface
        drawWindowBackground(g2d, layout);
        drawHeader(g2d, layout);
        drawCloseButton(g2d, layout, hoverCloseButton);
        
        // Draw room-specific components
        drawTabs(g2d, layout);
        drawRoomList(g2d, layout);
        drawCreateButton(g2d, layout);
        drawScrollbar(g2d, layout);
    }
    
    // ═══════════════════════════════════════════════════════════
    // TAB RENDERING
    // ═══════════════════════════════════════════════════════════
    
    private void drawTabs(Graphics2D g2d, RoomPanelLayout layout) {
        for (RoomPanelLayout.Tab tab : RoomPanelLayout.Tab.values()) {
            drawTab(g2d, layout, tab);
        }
    }
    
    private void drawTab(Graphics2D g2d, RoomPanelLayout layout, RoomPanelLayout.Tab tab) {
        Rectangle bounds = layout.getTabBounds(tab);
        boolean isActive = (tab == currentTab);
        boolean isHovered = (tab == hoveredTab);
        
        // Background
        if (isActive) {
            g2d.setColor(RoomPanelLayout.TAB_ACTIVE);
        } else if (isHovered) {
            g2d.setColor(RoomPanelLayout.TAB_ACTIVE.brighter());
        } else {
            g2d.setColor(RoomPanelLayout.TAB_INACTIVE);
        }
        g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
        
        // Border
        if (isActive) {
            g2d.setColor(RoomPanelLayout.TAB_ACTIVE.darker());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
        }
        
        // Text
        g2d.setColor(isActive ? Color.WHITE : Color.DARK_GRAY);
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();
        
        String text = tab.label;
        int textX = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
        int textY = bounds.y + ((bounds.height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }
    
    // ═══════════════════════════════════════════════════════════
    // ROOM LIST RENDERING
    // ═══════════════════════════════════════════════════════════
    
    private void drawRoomList(Graphics2D g2d, RoomPanelLayout layout) {
        Rectangle listBounds = layout.getRoomListBounds();
        
        // Set clip region
        Shape oldClip = g2d.getClip();
        g2d.setClip(listBounds.x, listBounds.y, listBounds.width, listBounds.height);
        
        if (rooms == null || rooms.isEmpty()) {
            drawEmptyState(g2d, layout, getEmptyEmoji(), getEmptyMessage(), getEmptyHint());
        } else {
            for (int i = 0; i < rooms.size(); i++) {
                Room room = rooms.get(i);
                Rectangle itemBounds = layout.getRoomItemBounds(i, scrollOffset);
                
                // Skip if outside visible area
                if (itemBounds.y + itemBounds.height < listBounds.y || 
                    itemBounds.y > listBounds.y + listBounds.height) {
                    continue;
                }
                
                drawRoomItem(g2d, layout, room, itemBounds);
            }
        }
        
        // Restore clip
        g2d.setClip(oldClip);
    }
    
    private void drawRoomItem(Graphics2D g2d, RoomPanelLayout layout, Room room, Rectangle bounds) {
        boolean isHovered = (room == hoveredRoom);
        boolean isCurrent = (currentRoom != null && room.getRoomId().equals(currentRoom.getRoomId()));
        
        // Background
        Color bgColor;
        if (isCurrent) {
            bgColor = RoomPanelLayout.ROOM_CURRENT;
        } else if (isHovered) {
            bgColor = RoomPanelLayout.ROOM_ITEM_HOVER;
        } else {
            bgColor = layout.getRoomTypeColor(room.getRoomType());
        }
        
        g2d.setColor(bgColor);
        g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
        
        // Border
        g2d.setColor(new Color(180, 180, 180));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
        
        // Room icon based on type
        String typeIcon = layout.getRoomTypeIcon(room.getRoomType());
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        g2d.drawString(typeIcon, bounds.x + 12, bounds.y + 30);
        
        // Room name
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(room.getRoomName(), bounds.x + 45, bounds.y + 22);
        
        // Owner
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.setColor(Color.GRAY);
        g2d.drawString("by " + room.getOwnerUsername(), bounds.x + 45, bounds.y + 40);
        
        // Current room indicator
        if (isCurrent) {
            g2d.setColor(new Color(0, 128, 0));
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString("● CURRENT", bounds.x + bounds.width - 75, bounds.y + 22);
        }
        
        // Player count (if available)
        if (room.getCurrentPlayerCount() > 0) {
            g2d.setColor(new Color(100, 100, 100));
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString("👥 " + room.getCurrentPlayerCount(), 
                          bounds.x + bounds.width - 50, bounds.y + 40);
        }
        
        // Room type label
        g2d.setColor(new Color(100, 100, 100));
        g2d.setFont(new Font("Arial", Font.ITALIC, 9));
        g2d.drawString(room.getRoomType().name(), bounds.x + bounds.width - 75, bounds.y + 52);
    }
    
    private String getEmptyEmoji() {
        switch (currentTab) {
            case MY_ROOMS:
                return "🏠";
            case FAVORITES:
                return "⭐";
            case PUBLIC_ROOMS:
            default:
                return "🌐";
        }
    }
    
    private String getEmptyMessage() {
        switch (currentTab) {
            case MY_ROOMS:
                return "You haven't created any rooms yet";
            case FAVORITES:
                return "No favorite rooms yet";
            case PUBLIC_ROOMS:
            default:
                return "No public rooms available";
        }
    }
    
    private String getEmptyHint() {
        switch (currentTab) {
            case MY_ROOMS:
                return "Click 'Create Room' to get started!";
            case FAVORITES:
                return "Star rooms to add them here";
            case PUBLIC_ROOMS:
            default:
                return "Create a room or wait for others";
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // CREATE BUTTON RENDERING
    // ═══════════════════════════════════════════════════════════
    
    private void drawCreateButton(Graphics2D g2d, RoomPanelLayout layout) {
        Rectangle bounds = layout.getCreateButtonBounds();
        
        // Background
        g2d.setColor(hoverCreateButton ? 
                    RoomPanelLayout.CREATE_BUTTON_HOVER : 
                    RoomPanelLayout.CREATE_BUTTON_COLOR);
        g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
        
        // Border on hover
        if (hoverCreateButton) {
            g2d.setColor(RoomPanelLayout.CREATE_BUTTON_COLOR.darker());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
        }
        
        // Text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "+ Create Room";
        int textX = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
        int textY = bounds.y + ((bounds.height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }
    
    // ═══════════════════════════════════════════════════════════
    // SCROLLBAR RENDERING
    // ═══════════════════════════════════════════════════════════
    
    private void drawScrollbar(Graphics2D g2d, RoomPanelLayout layout) {
        if (rooms == null || rooms.isEmpty()) return;
        
        Rectangle listBounds = layout.getRoomListBounds();
        int totalHeight = rooms.size() * RoomPanelLayout.ROOM_ITEM_HEIGHT;
        
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
        
        int maxScroll = layout.getRoomListMaxScrollOffset(rooms.size());
        float scrollRatio = maxScroll > 0 ? (float) scrollOffset / maxScroll : 0;
        int thumbY = trackY + (int)((trackHeight - thumbHeight) * scrollRatio);
        
        g2d.setColor(new Color(150, 150, 150));
        g2d.fillRoundRect(trackX, thumbY, 6, thumbHeight, 3, 3);
    }
}