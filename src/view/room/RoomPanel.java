package view.room;

import view.shared.BaseToolbarPanel;
import view.shared.BasePanelLayout;
import view.shared.PanelRenderer;
import model.room.Room;
import controller.room.*;
import main.GamePanel;

import java.awt.*;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * RoomPanel - Main coordinator for Room Navigator UI
 * 
 * Part of MVC Architecture:
 * - Extends BaseToolbarPanel for shared window behavior
 * - Coordinates between RoomController and RoomPanelRenderer
 * - Handles user input and delegates to appropriate handlers
 * 
 * Features:
 * - Tab navigation (Public Rooms, My Rooms, Favorites)
 * - Room list with scroll
 * - Create room button
 * - Enter room on click
 */
public class RoomPanel extends BaseToolbarPanel implements RoomListenerManager.RoomChangeListener {
    
    private RoomController controller;
    private RoomPanelLayout roomLayout;  // Typed reference for room-specific methods
    private RoomPanelRenderer roomRenderer;  // Typed reference for state setters
    
    // UI State
    private RoomPanelLayout.Tab currentTab;
    private Room hoveredRoom;
    private RoomPanelLayout.Tab hoveredTab;
    private boolean hoverCreateButton;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public RoomPanel(GamePanel gp, RoomController controller) {
        super(gp);  // This calls createLayout() and createRenderer()
        this.controller = controller;
        this.currentTab = RoomPanelLayout.Tab.PUBLIC_ROOMS;
        
        // Store typed references (layout and renderer were created by super())
        this.roomLayout = (RoomPanelLayout) layout;
        this.roomRenderer = (RoomPanelRenderer) renderer;
        
        // Register for controller events
        controller.addListener(this);
        
        System.out.println("[ROOM PANEL] Initialized");
    }
    
    // ═══════════════════════════════════════════════════════════
    // ABSTRACT METHOD IMPLEMENTATIONS (called by super constructor)
    // ═══════════════════════════════════════════════════════════
    
    @Override
    protected BasePanelLayout createLayout() {
        // Create with initial position (0,0) - will be centered by base class
        return new RoomPanelLayout(0, 0);
    }
    
    @Override
    protected PanelRenderer createRenderer() {
        return new RoomPanelRenderer();
    }
    
    @Override
    protected int getItemCount() {
        return getRoomsForCurrentTab().size();
    }
    
    // ═══════════════════════════════════════════════════════════
    // TOGGLE / VISIBILITY
    // ═══════════════════════════════════════════════════════════
    
    @Override
    protected void onOpen() {
        super.onOpen();
        hoveredRoom = null;
        hoveredTab = null;
    }
    
    // ═══════════════════════════════════════════════════════════
    // DRAWING
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public void draw(Graphics2D g2d) {
        if (!visible) return;
        
        // Update renderer state before drawing
        roomRenderer.setCurrentTab(currentTab);
        roomRenderer.setRooms(getRoomsForCurrentTab());
        roomRenderer.setCurrentRoom(controller.getCurrentRoom());
        roomRenderer.setHoveredRoom(hoveredRoom);
        roomRenderer.setScrollOffset(scrollOffset);
        roomRenderer.setHoverCreateButton(hoverCreateButton);
        roomRenderer.setHoveredTab(hoveredTab);
        
        // Call base class draw (which calls renderer.render())
        super.draw(g2d);
    }
    
    // ═══════════════════════════════════════════════════════════
    // INPUT HANDLING - MOUSE MOVE
    // ═══════════════════════════════════════════════════════════
    
    @Override
    protected void handleContentMouseMove(int mouseX, int mouseY) {
        // Check tabs
        hoveredTab = null;
        for (RoomPanelLayout.Tab tab : RoomPanelLayout.Tab.values()) {
            Rectangle tabBounds = roomLayout.getTabBounds(tab);
            if (tabBounds.contains(mouseX, mouseY)) {
                hoveredTab = tab;
                break;
            }
        }
        
        // Check create button
        Rectangle createBounds = roomLayout.getCreateButtonBounds();
        hoverCreateButton = createBounds.contains(mouseX, mouseY);
        
        // Check room items
        hoveredRoom = null;
        Rectangle listBounds = roomLayout.getRoomListBounds();
        if (listBounds.contains(mouseX, mouseY)) {
            int index = roomLayout.getRoomIndexAtY(mouseY, scrollOffset);
            List<Room> rooms = getRoomsForCurrentTab();
            
            if (index >= 0 && index < rooms.size()) {
                hoveredRoom = rooms.get(index);
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // INPUT HANDLING - CLICKS
    // ═══════════════════════════════════════════════════════════
    
    @Override
    protected boolean handleContentClick(int mouseX, int mouseY) {
        // Check tab clicks
        for (RoomPanelLayout.Tab tab : RoomPanelLayout.Tab.values()) {
            Rectangle tabBounds = roomLayout.getTabBounds(tab);
            if (tabBounds.contains(mouseX, mouseY)) {
                currentTab = tab;
                scrollOffset = 0;
                hoveredRoom = null;
                System.out.println("[ROOM PANEL] Switched to tab: " + tab);
                return true;
            }
        }
        
        // Check create button click
        Rectangle createBounds = roomLayout.getCreateButtonBounds();
        if (createBounds.contains(mouseX, mouseY)) {
            createNewRoom();
            return true;
        }
        
        // Check room item click
        Rectangle listBounds = roomLayout.getRoomListBounds();
        if (listBounds.contains(mouseX, mouseY)) {
            int index = roomLayout.getRoomIndexAtY(mouseY, scrollOffset);
            List<Room> rooms = getRoomsForCurrentTab();
            
            if (index >= 0 && index < rooms.size()) {
                Room clickedRoom = rooms.get(index);
                enterRoom(clickedRoom);
                return true;
            }
        }
        
        return false;
    }
    
    // ═══════════════════════════════════════════════════════════
    // ACTIONS
    // ═══════════════════════════════════════════════════════════
    
    private void createNewRoom() {
        String roomName = JOptionPane.showInputDialog(
            null,
            "Enter room name:",
            "Create Room",
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (roomName != null && !roomName.trim().isEmpty()) {
            Room newRoom = controller.createRoom(roomName, gp.player.name);
            if (newRoom != null) {
                currentTab = RoomPanelLayout.Tab.MY_ROOMS;
                scrollOffset = 0;
                System.out.println("[ROOM PANEL] Created room: " + newRoom.getRoomName());
            }
        }
    }
    
    private void enterRoom(Room room) {
        // Check if room is locked
        if (room.getRoomType() == Room.RoomType.LOCKED && !room.isOwner(gp.player.name)) {
            String password = JOptionPane.showInputDialog(
                null,
                "Enter room password:",
                "Room Locked",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (password != null) {
                boolean success = controller.enterRoomWithPassword(
                    room.getRoomId(), 
                    gp.player.name, 
                    password
                );
                
                if (success) {
                    visible = false;
                } else {
                    JOptionPane.showMessageDialog(
                        null,
                        "Wrong password!",
                        "Access Denied",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        } else {
            // Normal room entry
            boolean success = controller.enterRoom(room.getRoomId(), gp.player.name);
            if (success) {
                visible = false;
                System.out.println("[ROOM PANEL] Entering room: " + room.getRoomName());
            } else {
                JOptionPane.showMessageDialog(
                    null,
                    "Cannot enter this room",
                    "Access Denied",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // ROOM LIST HELPERS
    // ═══════════════════════════════════════════════════════════
    
    private List<Room> getRoomsForCurrentTab() {
        switch (currentTab) {
            case PUBLIC_ROOMS:
                return controller.getPublicRooms();
            case MY_ROOMS:
                return controller.getMyRooms(gp.player.name);
            case FAVORITES:
                return controller.getFavoriteRooms();
            default:
                return controller.getPublicRooms();
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // SCROLL HANDLING
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public void handleScroll(int rotation) {
        int scrollAmount = rotation * 30;
        int maxScroll = roomLayout.getRoomListMaxScrollOffset(getItemCount());
        
        scrollOffset = Math.max(0, Math.min(scrollOffset + scrollAmount, maxScroll));
    }
    
    // ═══════════════════════════════════════════════════════════
    // CONTROLLER LISTENER CALLBACKS
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public void onRoomEntered(Room room) {
        // Close panel when entering a room
        visible = false;
    }
    
    @Override
    public void onRoomLeft(Room room) {
        // Could show notification
    }
    
    @Override
    public void onRoomCreated(Room room) {
        // Switch to My Rooms tab to show new room
        currentTab = RoomPanelLayout.Tab.MY_ROOMS;
        scrollOffset = 0;
    }
    
    @Override
    public void onRoomDeleted(Room room) {
        // Refresh the list
        hoveredRoom = null;
    }
    
    @Override
    public void onRoomListChanged() {
        // Refresh display
        hoveredRoom = null;
    }
    
    // ═══════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public void refresh() {
        super.refresh();
        hoveredRoom = null;
    }
    
    public void setTab(RoomPanelLayout.Tab tab) {
        this.currentTab = tab;
        this.scrollOffset = 0;
    }
    
    public RoomPanelLayout.Tab getCurrentTab() {
        return currentTab;
    }
    
    // ═══════════════════════════════════════════════════════════
    // CLEANUP
    // ═══════════════════════════════════════════════════════════
    
    public void shutdown() {
        controller.removeListener(this);
        System.out.println("[ROOM PANEL] Shutdown");
    }
}