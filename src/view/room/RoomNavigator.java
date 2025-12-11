package view.room;

import main.GamePanel;
import model.room.Room;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import controller.room.RoomController;

/**
 * UI for navigating between rooms (like Habbo Hotel room navigator)
 */
public class RoomNavigator {
    
    private GamePanel gp;
    private RoomController roomController;
    private boolean visible;
    
    // Window dimensions
    private int windowX;
    private int windowY;
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 500;
    private static final int HEADER_HEIGHT = 40;
    private static final int ROOM_ITEM_HEIGHT = 60;
    
    // ═══════════════════════════════════════════════════════════
    // ✨ NEW - Dragging state
    // ═══════════════════════════════════════════════════════════
    private boolean isDragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    
    // UI state
    private int scrollOffset = 0;
    private Room hoveredRoom;
    private boolean hoverCloseButton;
    private boolean hoverCreateButton;
    
    // Current tab
    private Tab currentTab;
    
    public enum Tab {
        PUBLIC_ROOMS,
        MY_ROOMS
    }
    
    // Colors
    private static final Color WINDOW_BG = new Color(255, 255, 255, 240);
    private static final Color HEADER_BG = new Color(0, 102, 204);
    private static final Color ROOM_ITEM_BG = new Color(240, 240, 240);
    private static final Color ROOM_ITEM_HOVER = new Color(200, 220, 255);
    private static final Color CURRENT_ROOM = new Color(144, 238, 144);
    
    public RoomNavigator(GamePanel gp, RoomController roomController) {
        this.gp = gp;
        this.roomController = roomController;
        this.visible = false;
        this.currentTab = Tab.PUBLIC_ROOMS;
        
        // Center window
        this.windowX = (gp.screenWidth - WINDOW_WIDTH) / 2;
        this.windowY = (gp.screenHeight - WINDOW_HEIGHT) / 2;
    }
    
    // ═══════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════
    
    public void toggle() {
        visible = !visible;
        if (visible) {
            scrollOffset = 0;
            hoveredRoom = null;
            isDragging = false;
        }
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
        int closeX = windowX + WINDOW_WIDTH - 30;
        int closeY = windowY + 10;
        hoverCloseButton = (mouseX >= closeX && mouseX <= closeX + 20 &&
                           mouseY >= closeY && mouseY <= closeY + 20);
        
        // Check create button hover
        int createButtonY = windowY + WINDOW_HEIGHT - 50;
        hoverCreateButton = (mouseX >= windowX + 20 && 
                            mouseX <= windowX + 120 &&
                            mouseY >= createButtonY && 
                            mouseY <= createButtonY + 35);
        
        // Check room item hover
        hoveredRoom = getRoomAtPosition(mouseX, mouseY);
    }
    
    // ═══════════════════════════════════════════════════════════
    // ✨ NEW - Handle mouse press (start dragging)
    // ═══════════════════════════════════════════════════════════
    public void handleMousePressed(int mouseX, int mouseY) {
        if (!visible) return;
        
        // Check if clicking on header (to start drag)
        if (isOnHeader(mouseX, mouseY) && !hoverCloseButton) {
            isDragging = true;
            dragOffsetX = mouseX - windowX;
            dragOffsetY = mouseY - windowY;
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // ✨ NEW - Handle mouse drag
    // ═══════════════════════════════════════════════════════════
    public void handleMouseDragged(int mouseX, int mouseY) {
        if (!visible || !isDragging) return;
        
        // Update window position
        windowX = mouseX - dragOffsetX;
        windowY = mouseY - dragOffsetY;
        
        // Constrain to screen bounds
        windowX = Math.max(0, Math.min(windowX, gp.screenWidth - WINDOW_WIDTH));
        windowY = Math.max(0, Math.min(windowY, gp.screenHeight - WINDOW_HEIGHT));
    }
    
    // ═══════════════════════════════════════════════════════════
    // ✨ NEW - Handle mouse release (stop dragging)
    // ═══════════════════════════════════════════════════════════
    public void handleMouseReleased() {
        isDragging = false;
    }
    
    // ═══════════════════════════════════════════════════════════
    // ✨ NEW - Check if mouse is on header
    // ═══════════════════════════════════════════════════════════
    private boolean isOnHeader(int mouseX, int mouseY) {
        return mouseX >= windowX && mouseX <= windowX + WINDOW_WIDTH &&
               mouseY >= windowY && mouseY <= windowY + HEADER_HEIGHT;
    }
    
    // ═══════════════════════════════════════════════════════════
    // ✨ NEW - Check if currently dragging
    // ═══════════════════════════════════════════════════════════
    public boolean isDragging() {
        return isDragging;
    }
    
    public void handleClick(int mouseX, int mouseY) {
        if (!visible) return;
        
        // ✨ Don't process clicks while dragging
        if (isDragging) return;
        
        // Close button
        if (hoverCloseButton) {
            visible = false;
            return;
        }
        
        // Create room button
        if (hoverCreateButton) {
            createNewRoom();
            return;
        }
        
        // Tab buttons
        if (handleTabClick(mouseX, mouseY)) {
            return;
        }
        
        // Room selection
        Room clickedRoom = getRoomAtPosition(mouseX, mouseY);
        if (clickedRoom != null) {
            enterRoom(clickedRoom);
        }
    }
    
    private boolean handleTabClick(int mouseX, int mouseY) {
        int tabY = windowY + HEADER_HEIGHT + 10;
        int tabHeight = 30;
        
        // Public rooms tab
        if (mouseX >= windowX + 20 && mouseX <= windowX + 120 &&
            mouseY >= tabY && mouseY <= tabY + tabHeight) {
            currentTab = Tab.PUBLIC_ROOMS;
            scrollOffset = 0;
            return true;
        }
        
        // My rooms tab
        if (mouseX >= windowX + 130 && mouseX <= windowX + 230 &&
            mouseY >= tabY && mouseY <= tabY + tabHeight) {
            currentTab = Tab.MY_ROOMS;
            scrollOffset = 0;
            return true;
        }
        
        return false;
    }
    
    private Room getRoomAtPosition(int mouseX, int mouseY) {
        List<Room> rooms = getRoomsForCurrentTab();
        
        int listY = windowY + HEADER_HEIGHT + 50;
        int listHeight = WINDOW_HEIGHT - HEADER_HEIGHT - 100;
        
        if (mouseX < windowX + 10 || mouseX > windowX + WINDOW_WIDTH - 10 ||
            mouseY < listY || mouseY > listY + listHeight) {
            return null;
        }
        
        int index = (mouseY - listY + scrollOffset) / ROOM_ITEM_HEIGHT;
        
        if (index >= 0 && index < rooms.size()) {
            return rooms.get(index);
        }
        
        return null;
    }
    
    // ═══════════════════════════════════════════════════════════
    // ACTIONS
    // ═══════════════════════════════════════════════════════════
    
    private void createNewRoom() {
        String roomName = promptForRoomName();
        if (roomName != null && !roomName.trim().isEmpty()) {
            Room newRoom = roomController.createRoom(roomName, gp.player.name);
            currentTab = Tab.MY_ROOMS;
            System.out.println("Created room: " + newRoom.getRoomName());
        }
    }
    
    private String promptForRoomName() {
        return javax.swing.JOptionPane.showInputDialog(
            null,
            "Enter room name:",
            "Create Room",
            javax.swing.JOptionPane.PLAIN_MESSAGE
        );
    }
    
    private void enterRoom(Room room) {
        boolean success = roomController.enterRoom(room.getRoomId(), gp.player.name);
        if (success) {
            visible = false;
            System.out.println("Entering room: " + room.getRoomName());
        } else {
            System.out.println("Failed to enter room");
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // RENDERING
    // ═══════════════════════════════════════════════════════════
    
    public void draw(Graphics2D g2d) {
        if (!visible) return;
        
        drawWindow(g2d);
        drawHeader(g2d);
        drawTabs(g2d);
        drawRoomList(g2d);
        drawCreateButton(g2d);
        drawCloseButton(g2d);
    }
    
    private void drawWindow(Graphics2D g2d) {
        g2d.setColor(WINDOW_BG);
        g2d.fillRoundRect(windowX, windowY, WINDOW_WIDTH, WINDOW_HEIGHT, 20, 20);
        
        g2d.setColor(HEADER_BG);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(windowX, windowY, WINDOW_WIDTH, WINDOW_HEIGHT, 20, 20);
    }
    
    private void drawHeader(Graphics2D g2d) {
        g2d.setColor(HEADER_BG);
        g2d.fillRoundRect(windowX, windowY, WINDOW_WIDTH, HEADER_HEIGHT, 20, 20);
        g2d.fillRect(windowX, windowY + 20, WINDOW_WIDTH, 20);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Room Navigator", windowX + 15, windowY + 27);
    }
    
    private void drawTabs(Graphics2D g2d) {
        int tabY = windowY + HEADER_HEIGHT + 10;
        int tabHeight = 30;
        
        drawTab(g2d, "Public Rooms", windowX + 20, tabY, 100, tabHeight, 
               currentTab == Tab.PUBLIC_ROOMS);
        
        drawTab(g2d, "My Rooms", windowX + 130, tabY, 100, tabHeight, 
               currentTab == Tab.MY_ROOMS);
    }
    
    private void drawTab(Graphics2D g2d, String text, int x, int y, int width, int height, boolean active) {
        g2d.setColor(active ? HEADER_BG : new Color(200, 200, 200));
        g2d.fillRoundRect(x, y, width, height, 10, 10);
        
        g2d.setColor(active ? Color.WHITE : Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }
    
    private void drawRoomList(Graphics2D g2d) {
        List<Room> rooms = getRoomsForCurrentTab();
        
        int listX = windowX + 10;
        int listY = windowY + HEADER_HEIGHT + 50;
        int listWidth = WINDOW_WIDTH - 20;
        int listHeight = WINDOW_HEIGHT - HEADER_HEIGHT - 100;
        
        g2d.setClip(listX, listY, listWidth, listHeight);
        
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            int itemY = listY + (i * ROOM_ITEM_HEIGHT) - scrollOffset;
            
            if (itemY + ROOM_ITEM_HEIGHT < listY || itemY > listY + listHeight) {
                continue;
            }
            
            drawRoomItem(g2d, room, listX, itemY, listWidth);
        }
        
        g2d.setClip(null);
        
        if (rooms.isEmpty()) {
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Arial", Font.ITALIC, 14));
            String message = currentTab == Tab.MY_ROOMS ? 
                "You haven't created any rooms yet" : "No public rooms available";
            FontMetrics fm = g2d.getFontMetrics();
            int textX = windowX + (WINDOW_WIDTH - fm.stringWidth(message)) / 2;
            int textY = windowY + WINDOW_HEIGHT / 2;
            g2d.drawString(message, textX, textY);
        }
    }
    
    private void drawRoomItem(Graphics2D g2d, Room room, int x, int y, int width) {
        boolean isHovered = (room == hoveredRoom);
        boolean isCurrent = room.getRoomId().equals(roomController.getCurrentRoomId());
        
        if (isCurrent) {
            g2d.setColor(CURRENT_ROOM);
        } else if (isHovered) {
            g2d.setColor(ROOM_ITEM_HOVER);
        } else {
            g2d.setColor(ROOM_ITEM_BG);
        }
        g2d.fillRoundRect(x, y, width, ROOM_ITEM_HEIGHT - 5, 10, 10);
        
        g2d.setColor(new Color(180, 180, 180));
        g2d.drawRoundRect(x, y, width, ROOM_ITEM_HEIGHT - 5, 10, 10);
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(room.getRoomName(), x + 15, y + 25);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.setColor(Color.GRAY);
        g2d.drawString("by " + room.getOwnerUsername(), x + 15, y + 42);
        
        if (isCurrent) {
            g2d.setColor(new Color(0, 128, 0));
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString("● CURRENT", x + width - 80, y + 25);
        }
        
        String typeText = room.getRoomType().name();
        g2d.setColor(new Color(100, 100, 100));
        g2d.setFont(new Font("Arial", Font.ITALIC, 9));
        g2d.drawString(typeText, x + width - 80, y + 42);
    }
    
    private void drawCreateButton(Graphics2D g2d) {
        int buttonX = windowX + 20;
        int buttonY = windowY + WINDOW_HEIGHT - 50;
        int buttonWidth = 100;
        int buttonHeight = 35;
        
        g2d.setColor(hoverCreateButton ? new Color(0, 153, 76) : new Color(76, 175, 80));
        g2d.fillRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 10, 10);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "Create Room";
        int textX = buttonX + (buttonWidth - fm.stringWidth(text)) / 2;
        int textY = buttonY + ((buttonHeight - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }
    
    private void drawCloseButton(Graphics2D g2d) {
        int closeX = windowX + WINDOW_WIDTH - 30;
        int closeY = windowY + 10;
        
        g2d.setColor(hoverCloseButton ? Color.RED : Color.WHITE);
        g2d.fillOval(closeX, closeY, 20, 20);
        
        g2d.setColor(HEADER_BG);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("X", closeX + 6, closeY + 14);
    }
    
    private List<Room> getRoomsForCurrentTab() {
        switch (currentTab) {
            case PUBLIC_ROOMS:
                return roomController.getPublicRooms();
            case MY_ROOMS:
                return roomController.getMyRooms(gp.player.name);
            default:
                return new ArrayList<>();
        }
    }


    public boolean containsPoint(int x, int y) {
    return x >= windowX && x <= windowX + WINDOW_WIDTH &&
           y >= windowY && y <= windowY + WINDOW_HEIGHT;
    }

    
}