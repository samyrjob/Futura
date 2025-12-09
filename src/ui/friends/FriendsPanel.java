package ui.friends;

import friend.Friend;
import friend.FriendManager;
import main.GamePanel;

import java.awt.*;
import java.util.List;

/**
 * Friends list panel (like Room Navigator)
 * Opens when clicking the bubble/friends icon
 */
public class FriendsPanel {
    
    private GamePanel gp;
    private FriendManager friendManager;
    private boolean visible;
    
    // Window position and dimensions
    private int windowX;
    private int windowY;
    private static final int WINDOW_WIDTH = 320;
    private static final int WINDOW_HEIGHT = 420;
    private static final int HEADER_HEIGHT = 45;
    private static final int FRIEND_ITEM_HEIGHT = 55;
    
    // Dragging state
    private boolean isDragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    
    // UI state
    private int scrollOffset = 0;
    private Friend hoveredFriend;
    private boolean hoverCloseButton;
    private int selectedFriendIndex = -1;
    
    // Colors
    private static final Color WINDOW_BG = new Color(255, 255, 255, 250);
    private static final Color HEADER_BG = new Color(156, 39, 176);  // Purple (matches friends button)
    private static final Color FRIEND_ITEM_BG = new Color(248, 248, 248);
    private static final Color FRIEND_ITEM_HOVER = new Color(237, 231, 246);
    private static final Color FRIEND_ITEM_SELECTED = new Color(225, 190, 231);
    private static final Color ONLINE_COLOR = new Color(76, 175, 80);
    private static final Color OFFLINE_COLOR = new Color(158, 158, 158);
    private static final Color DIVIDER_COLOR = new Color(224, 224, 224);
    
    public FriendsPanel(GamePanel gp, FriendManager friendManager) {
        this.gp = gp;
        this.friendManager = friendManager;
        this.visible = false;
        
        // Center window
        this.windowX = (gp.screenWidth - WINDOW_WIDTH) / 2;
        this.windowY = (gp.screenHeight - WINDOW_HEIGHT) / 2;
    }
    
    public void setFriendManager(FriendManager fm) {
        this.friendManager = fm;
    }
    
    // ═══════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════
    
    public void toggle() {
        visible = !visible;
        if (visible) {
            scrollOffset = 0;
            hoveredFriend = null;
            isDragging = false;
            selectedFriendIndex = -1;
            // Center on open
            windowX = (gp.screenWidth - WINDOW_WIDTH) / 2;
            windowY = (gp.screenHeight - WINDOW_HEIGHT) / 2;
        }
    }
    
    public void show() {
        visible = true;
        scrollOffset = 0;
        hoveredFriend = null;
    }
    
    public void hide() {
        visible = false;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    // ═══════════════════════════════════════════════════════════
    // INPUT HANDLING
    // ═══════════════════════════════════════════════════════════
    
    public void handleMouseMove(int mouseX, int mouseY) {
        if (!visible) return;
        
        // Check close button hover
        int closeX = windowX + WINDOW_WIDTH - 35;
        int closeY = windowY + 10;
        hoverCloseButton = (mouseX >= closeX && mouseX <= closeX + 25 &&
                           mouseY >= closeY && mouseY <= closeY + 25);
        
        // Check friend item hover
        hoveredFriend = getFriendAtPosition(mouseX, mouseY);
    }
    
    public void handleMousePressed(int mouseX, int mouseY) {
        if (!visible) return;
        
        // Check if clicking on header (to start drag)
        if (isOnHeader(mouseX, mouseY) && !hoverCloseButton) {
            isDragging = true;
            dragOffsetX = mouseX - windowX;
            dragOffsetY = mouseY - windowY;
        }
    }
    
    public void handleMouseDragged(int mouseX, int mouseY) {
        if (!visible || !isDragging) return;
        
        windowX = mouseX - dragOffsetX;
        windowY = mouseY - dragOffsetY;
        
        // Constrain to screen
        windowX = Math.max(0, Math.min(windowX, gp.screenWidth - WINDOW_WIDTH));
        windowY = Math.max(0, Math.min(windowY, gp.screenHeight - WINDOW_HEIGHT));
    }
    
    public void handleMouseReleased() {
        isDragging = false;
    }
    
    public boolean isDragging() {
        return isDragging;
    }
    
    public boolean handleClick(int mouseX, int mouseY) {
        if (!visible) return false;
        if (isDragging) return false;
        
        // Close button
        if (hoverCloseButton) {
            visible = false;
            return true;
        }
        
        // Check if click is in window
        if (!containsPoint(mouseX, mouseY)) {
            return false;
        }
        
        // Friend selection
        Friend clickedFriend = getFriendAtPosition(mouseX, mouseY);
        if (clickedFriend != null) {
            int index = friendManager.getFriends().indexOf(clickedFriend);
            if (selectedFriendIndex == index) {
                // Double-click or click on selected - could open chat/options
                System.out.println("[FRIENDS PANEL] Selected friend: " + clickedFriend.getUsername());
            }
            selectedFriendIndex = index;
            return true;
        }
        
        return true; // Consumed click but didn't do anything
    }
    
    public void handleScroll(int scrollAmount) {
        if (!visible || friendManager == null) return;
        
        int contentHeight = friendManager.getFriendCount() * FRIEND_ITEM_HEIGHT;
        int viewHeight = WINDOW_HEIGHT - HEADER_HEIGHT - 20;
        int maxScroll = Math.max(0, contentHeight - viewHeight);
        
        scrollOffset = Math.max(0, Math.min(scrollOffset + scrollAmount * 25, maxScroll));
    }
    
    public boolean containsPoint(int mouseX, int mouseY) {
        return mouseX >= windowX && mouseX <= windowX + WINDOW_WIDTH &&
               mouseY >= windowY && mouseY <= windowY + WINDOW_HEIGHT;
    }
    
    private boolean isOnHeader(int mouseX, int mouseY) {
        return mouseX >= windowX && mouseX <= windowX + WINDOW_WIDTH &&
               mouseY >= windowY && mouseY <= windowY + HEADER_HEIGHT;
    }
    
    private Friend getFriendAtPosition(int mouseX, int mouseY) {
        if (friendManager == null) return null;
        
        List<Friend> friends = friendManager.getFriends();
        
        int listX = windowX + 10;
        int listY = windowY + HEADER_HEIGHT + 10;
        int listWidth = WINDOW_WIDTH - 20;
        int listHeight = WINDOW_HEIGHT - HEADER_HEIGHT - 20;
        
        if (mouseX < listX || mouseX > listX + listWidth ||
            mouseY < listY || mouseY > listY + listHeight) {
            return null;
        }
        
        int index = (mouseY - listY + scrollOffset) / FRIEND_ITEM_HEIGHT;
        
        if (index >= 0 && index < friends.size()) {
            return friends.get(index);
        }
        
        return null;
    }
    
    // ═══════════════════════════════════════════════════════════
    // RENDERING
    // ═══════════════════════════════════════════════════════════
    
    public void draw(Graphics2D g2d) {
        if (!visible) return;
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawWindow(g2d);
        drawHeader(g2d);
        drawFriendsList(g2d);
        drawCloseButton(g2d);
    }
    
    private void drawWindow(Graphics2D g2d) {
        // Shadow
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fillRoundRect(windowX + 5, windowY + 5, WINDOW_WIDTH, WINDOW_HEIGHT, 20, 20);
        
        // Background
        g2d.setColor(WINDOW_BG);
        g2d.fillRoundRect(windowX, windowY, WINDOW_WIDTH, WINDOW_HEIGHT, 20, 20);
        
        // Border
        g2d.setColor(HEADER_BG);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(windowX, windowY, WINDOW_WIDTH, WINDOW_HEIGHT, 20, 20);
    }
    
    private void drawHeader(Graphics2D g2d) {
        // Header background
        g2d.setColor(HEADER_BG);
        g2d.fillRoundRect(windowX, windowY, WINDOW_WIDTH, HEADER_HEIGHT, 20, 20);
        g2d.fillRect(windowX, windowY + 25, WINDOW_WIDTH, 20);
        
        // Icon
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        g2d.setColor(Color.WHITE);
        g2d.drawString("💬", windowX + 15, windowY + 30);
        
        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Friends", windowX + 50, windowY + 30);
        
        // Count badge
        int friendCount = friendManager != null ? friendManager.getFriendCount() : 0;
        String countText = String.valueOf(friendCount);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int badgeWidth = Math.max(22, fm.stringWidth(countText) + 12);
        int badgeX = windowX + 125;
        int badgeY = windowY + 15;
        
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRoundRect(badgeX, badgeY, badgeWidth, 20, 10, 10);
        g2d.setColor(HEADER_BG);
        g2d.drawString(countText, badgeX + (badgeWidth - fm.stringWidth(countText)) / 2, badgeY + 15);
    }
    
    private void drawFriendsList(Graphics2D g2d) {
        if (friendManager == null) return;
        
        List<Friend> friends = friendManager.getFriends();
        
        int listX = windowX + 10;
        int listY = windowY + HEADER_HEIGHT + 10;
        int listWidth = WINDOW_WIDTH - 20;
        int listHeight = WINDOW_HEIGHT - HEADER_HEIGHT - 20;
        
        // Clip to list area
        Shape oldClip = g2d.getClip();
        g2d.setClip(listX, listY, listWidth, listHeight);
        
        if (friends.isEmpty()) {
            g2d.setClip(oldClip);
            drawEmptyState(g2d, listX, listY, listWidth, listHeight);
            return;
        }
        
        for (int i = 0; i < friends.size(); i++) {
            Friend friend = friends.get(i);
            int itemY = listY + (i * FRIEND_ITEM_HEIGHT) - scrollOffset;
            
            // Skip if outside visible area
            if (itemY + FRIEND_ITEM_HEIGHT < listY || itemY > listY + listHeight) {
                continue;
            }
            
            boolean isHovered = (friend == hoveredFriend);
            boolean isSelected = (i == selectedFriendIndex);
            
            drawFriendItem(g2d, friend, listX, itemY, listWidth, isHovered, isSelected);
        }
        
        g2d.setClip(oldClip);
        
        // Draw scroll indicator if needed
        int contentHeight = friends.size() * FRIEND_ITEM_HEIGHT;
        if (contentHeight > listHeight) {
            drawScrollIndicator(g2d, listX + listWidth - 8, listY, 6, listHeight, contentHeight);
        }
    }
    
    private void drawEmptyState(Graphics2D g2d, int x, int y, int width, int height) {
        // Empty state illustration
        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        String emoji = "👥";
        FontMetrics fm = g2d.getFontMetrics();
        int emojiX = x + (width - fm.stringWidth(emoji)) / 2;
        g2d.drawString(emoji, emojiX, y + height / 2 - 20);
        
        // Text
        g2d.setColor(new Color(120, 120, 120));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        String message = "No friends yet";
        fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(message)) / 2;
        g2d.drawString(message, textX, y + height / 2 + 25);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String hint = "Click on a player to send a request!";
        fm = g2d.getFontMetrics();
        int hintX = x + (width - fm.stringWidth(hint)) / 2;
        g2d.drawString(hint, hintX, y + height / 2 + 50);
    }
    
    private void drawFriendItem(Graphics2D g2d, Friend friend, int x, int y, int width, 
                                boolean isHovered, boolean isSelected) {
        // Background
        if (isSelected) {
            g2d.setColor(FRIEND_ITEM_SELECTED);
        } else if (isHovered) {
            g2d.setColor(FRIEND_ITEM_HOVER);
        } else {
            g2d.setColor(FRIEND_ITEM_BG);
        }
        g2d.fillRoundRect(x, y, width, FRIEND_ITEM_HEIGHT - 5, 12, 12);
        
        // Border on hover/select
        if (isHovered || isSelected) {
            g2d.setColor(HEADER_BG.brighter());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, width, FRIEND_ITEM_HEIGHT - 5, 12, 12);
        }
        
        // Online status indicator (circle)
        int indicatorSize = 14;
        int indicatorX = x + 15;
        int indicatorY = y + (FRIEND_ITEM_HEIGHT - 5) / 2 - indicatorSize / 2;
        
        g2d.setColor(friend.isOnline() ? ONLINE_COLOR : OFFLINE_COLOR);
        g2d.fillOval(indicatorX, indicatorY, indicatorSize, indicatorSize);
        
        // Glow effect for online
        if (friend.isOnline()) {
            g2d.setColor(new Color(76, 175, 80, 50));
            g2d.fillOval(indicatorX - 3, indicatorY - 3, indicatorSize + 6, indicatorSize + 6);
        }
        
        // Username
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(friend.getUsername(), x + 40, y + 22);
        
        // Status text
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.setColor(new Color(100, 100, 100));
        String status;
        if (friend.isOnline()) {
            status = friend.getCurrentRoom() != null ? "In: " + truncate(friend.getCurrentRoom(), 20) : "Online";
            g2d.setColor(ONLINE_COLOR.darker());
        } else {
            status = "Offline";
        }
        g2d.drawString(status, x + 40, y + 40);
        
        // Gender icon
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        String genderIcon = friend.getGender().equalsIgnoreCase("FEMALE") ? "♀" : "♂";
        Color genderColor = friend.getGender().equalsIgnoreCase("FEMALE") ? 
            new Color(233, 30, 99) : new Color(33, 150, 243);
        g2d.setColor(genderColor);
        g2d.drawString(genderIcon, x + width - 35, y + 32);
    }
    
    private void drawScrollIndicator(Graphics2D g2d, int x, int y, int width, int height, int contentHeight) {
        // Track
        g2d.setColor(new Color(230, 230, 230));
        g2d.fillRoundRect(x, y, width, height, 3, 3);
        
        // Thumb
        float visibleRatio = (float) height / contentHeight;
        int thumbHeight = Math.max(30, (int) (height * visibleRatio));
        int maxScroll = contentHeight - height;
        float scrollRatio = maxScroll > 0 ? (float) scrollOffset / maxScroll : 0;
        int thumbY = y + (int) ((height - thumbHeight) * scrollRatio);
        
        g2d.setColor(HEADER_BG.brighter());
        g2d.fillRoundRect(x, thumbY, width, thumbHeight, 3, 3);
    }
    
    private void drawCloseButton(Graphics2D g2d) {
        int closeX = windowX + WINDOW_WIDTH - 35;
        int closeY = windowY + 10;
        int size = 25;
        
        // Button background
        g2d.setColor(hoverCloseButton ? new Color(255, 82, 82) : new Color(255, 255, 255, 180));
        g2d.fillOval(closeX, closeY, size, size);
        
        // X icon
        g2d.setColor(hoverCloseButton ? Color.WHITE : HEADER_BG);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int padding = 7;
        g2d.drawLine(closeX + padding, closeY + padding, closeX + size - padding, closeY + size - padding);
        g2d.drawLine(closeX + size - padding, closeY + padding, closeX + padding, closeY + size - padding);
    }
    
    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

        /**
     * Refresh the friends list display
     */
    public void refresh() {
        // Recalculate scroll bounds
        int totalHeight = friendManager.getFriendCount() * FRIEND_ITEM_HEIGHT;
        int visibleHeight = WINDOW_HEIGHT - HEADER_HEIGHT - 20;
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        
        // Reset scroll if needed
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }
        
        System.out.println("[FRIENDS PANEL] Refreshed - showing " + friendManager.getFriendCount() + " friends");
    }
}
