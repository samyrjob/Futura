package view.friend;

import controller.friend.FriendController;
import model.friend.Friend;
import view.shared.BasePanelLayout;
import view.shared.PanelRenderer;

import java.awt.*;
import java.util.List;

/**
 * Renderer for the Friends Panel.
 * Handles all drawing operations for the friends list.
 * 
 * Part of MVC architecture - this is the View layer (rendering).
 * Single Responsibility: Only handles drawing, no business logic.
 */
public class FriendsPanelRenderer implements PanelRenderer {
    
    // ═══════════════════════════════════════════════════════════
    // DEPENDENCIES
    // ═══════════════════════════════════════════════════════════
    
    private final FriendController controller;
    
    // ═══════════════════════════════════════════════════════════
    // RENDER STATE (set by panel before each render)
    // ═══════════════════════════════════════════════════════════
    
    private int scrollOffset;
    private Friend hoveredFriend;
    private int selectedIndex;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public FriendsPanelRenderer(FriendController controller) {
        this.controller = controller;
        this.scrollOffset = 0;
        this.hoveredFriend = null;
        this.selectedIndex = -1;
    }
    
    // ═══════════════════════════════════════════════════════════
    // STATE SETTERS (called by panel before render)
    // ═══════════════════════════════════════════════════════════
    
    public void setScrollOffset(int offset) {
        this.scrollOffset = offset;
    }
    
    public void setHoveredFriend(Friend friend) {
        this.hoveredFriend = friend;
    }
    
    public void setSelectedIndex(int index) {
        this.selectedIndex = index;
    }
    
    // ═══════════════════════════════════════════════════════════
    // MAIN RENDER METHOD
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public void render(Graphics2D g2d, BasePanelLayout baseLayout, boolean hoverCloseButton) {
        FriendsPanelLayout layout = (FriendsPanelLayout) baseLayout;
        
        // Draw window components (using default implementations)
        drawWindowBackground(g2d, layout);
        drawHeader(g2d, layout);
        drawCloseButton(g2d, layout, hoverCloseButton);
        
        // Draw friends-specific content
        drawFriendCount(g2d, layout);
        drawFriendsList(g2d, layout);
    }
    
    // ═══════════════════════════════════════════════════════════
    // FRIENDS-SPECIFIC RENDERING
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Draw the friend count badge in header
     */
    private void drawFriendCount(Graphics2D g2d, FriendsPanelLayout layout) {
        int count = controller.getFriendCount();
        int badgeX = layout.getWindowX() + FriendsPanelLayout.BADGE_X_OFFSET;
        int badgeY = layout.getWindowY() + FriendsPanelLayout.BADGE_Y_OFFSET;
        
        drawBadge(g2d, badgeX, badgeY, count, layout.getHeaderColor());
    }
    
    /**
     * Draw the friends list
     */
    private void drawFriendsList(Graphics2D g2d, FriendsPanelLayout layout) {
        List<Friend> friends = controller.getFriends();
        Rectangle content = layout.getContentBounds();
        
        // Set clip for scrolling
        Shape oldClip = g2d.getClip();
        g2d.setClip(content);
        
        if (friends.isEmpty()) {
            g2d.setClip(oldClip);
            drawEmptyState(g2d, layout, "👥", "No friends yet", "Click on a player to send a request!");
            return;
        }
        
        // Draw each friend item
        for (int i = 0; i < friends.size(); i++) {
            Friend friend = friends.get(i);
            int itemY = layout.getItemY(i, scrollOffset);
            
            // Skip if outside visible area
            if (!layout.isItemVisible(itemY)) {
                continue;
            }
            
            boolean isHovered = (friend == hoveredFriend);
            boolean isSelected = (i == selectedIndex);
            
            drawFriendItem(g2d, layout, friend, content.x, itemY, content.width, isHovered, isSelected);
        }
        
        g2d.setClip(oldClip);
        
        // Draw scrollbar if needed
        if (friends.size() * BasePanelLayout.ITEM_HEIGHT > content.height) {
            drawScrollbar(g2d, layout, scrollOffset, friends.size());
        }
    }
    
    /**
     * Draw a single friend item
     */
    private void drawFriendItem(Graphics2D g2d, FriendsPanelLayout layout, 
                                Friend friend, int x, int y, int width,
                                boolean isHovered, boolean isSelected) {
        // Background
        drawItemBackground(g2d, x, y, width, isHovered, isSelected, layout.getHeaderColor());
        
        // Online status indicator
        drawStatusIndicator(g2d, layout, friend, y);
        
        // Username
        g2d.setColor(Color.BLACK);
        g2d.setFont(BasePanelLayout.ITEM_NAME_FONT);
        g2d.drawString(friend.getUsername(), x + FriendsPanelLayout.FRIEND_NAME_X_OFFSET, 
                      y + FriendsPanelLayout.FRIEND_NAME_Y_OFFSET);
        
        // Status text
        drawStatusText(g2d, layout, friend, x, y);
        
        // Gender icon
        drawGenderIcon(g2d, layout, friend, x, y, width);
    }
    
    /**
     * Draw the online/offline status indicator
     */
    private void drawStatusIndicator(Graphics2D g2d, FriendsPanelLayout layout, Friend friend, int itemY) {
        Point pos = layout.getStatusIndicatorPosition(itemY);
        int size = FriendsPanelLayout.STATUS_INDICATOR_SIZE;
        
        // Glow effect for online
        if (friend.isOnline()) {
            g2d.setColor(FriendsPanelLayout.ONLINE_GLOW);
            g2d.fillOval(pos.x - 3, pos.y - 3, size + 6, size + 6);
        }
        
        // Main indicator
        g2d.setColor(friend.isOnline() ? FriendsPanelLayout.ONLINE_COLOR : FriendsPanelLayout.OFFLINE_COLOR);
        g2d.fillOval(pos.x, pos.y, size, size);
    }
    
    /**
     * Draw the status text (Online/Offline/In room)
     */
    private void drawStatusText(Graphics2D g2d, FriendsPanelLayout layout, Friend friend, int x, int y) {
        g2d.setFont(BasePanelLayout.ITEM_SUBTITLE_FONT);
        
        String status;
        if (friend.isOnline()) {
            String room = friend.getCurrentRoom();
            status = (room != null && !room.isEmpty()) ? "In: " + truncate(room, 20) : "Online";
            g2d.setColor(FriendsPanelLayout.ONLINE_COLOR.darker());
        } else {
            status = "Offline";
            g2d.setColor(new Color(100, 100, 100));
        }
        
        g2d.drawString(status, x + FriendsPanelLayout.FRIEND_NAME_X_OFFSET, 
                      y + FriendsPanelLayout.FRIEND_STATUS_Y_OFFSET);
    }
    
    /**
     * Draw the gender icon
     */
    private void drawGenderIcon(Graphics2D g2d, FriendsPanelLayout layout, 
                               Friend friend, int x, int y, int width) {
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        g2d.setColor(FriendsPanelLayout.getGenderColor(friend.getGender()));
        
        String icon = FriendsPanelLayout.getGenderIcon(friend.getGender());
        g2d.drawString(icon, x + width - FriendsPanelLayout.FRIEND_GENDER_X_OFFSET, 
                      y + FriendsPanelLayout.FRIEND_GENDER_Y_OFFSET);
    }
    
    // ═══════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}