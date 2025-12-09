package view.friend;

import controller.friend.FriendController;
import main.GamePanel;
import model.friend.Friend;
import view.shared.BasePanelLayout;
import view.shared.BaseToolbarPanel;
import view.shared.PanelRenderer;

import java.awt.*;
import java.util.List;

/**
 * Friends Panel - displays friend list and handles friend interactions.
 * Extends BaseToolbarPanel for consistent window behavior.
 * 
 * Part of MVC architecture - this is the View layer (coordinator).
 * 
 * Responsibilities:
 * - Coordinate between layout, renderer, and controller
 * - Handle user input specific to friends
 * - Manage selection and hover states
 */
public class FriendsPanel extends BaseToolbarPanel {
    
    // ═══════════════════════════════════════════════════════════
    // COMPONENTS
    // ═══════════════════════════════════════════════════════════
    
    private final FriendController controller;
    private final FriendsPanelRenderer friendsRenderer;
    
    // ═══════════════════════════════════════════════════════════
    // UI STATE
    // ═══════════════════════════════════════════════════════════
    
    private Friend hoveredFriend;
    private int selectedFriendIndex;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public FriendsPanel(GamePanel gp, FriendController controller) {
        super(gp);
        this.controller = controller;
        this.friendsRenderer = (FriendsPanelRenderer) renderer;
        this.hoveredFriend = null;
        this.selectedFriendIndex = -1;
        
        // Setup controller listeners for UI updates
        setupControllerListeners();
    }
    
    // ═══════════════════════════════════════════════════════════
    // ABSTRACT METHOD IMPLEMENTATIONS
    // ═══════════════════════════════════════════════════════════
    
    @Override
    protected BasePanelLayout createLayout() {
        return new FriendsPanelLayout(0, 0); // Position set by centerOnScreen
    }
    
    @Override
    protected PanelRenderer createRenderer() {
        return new FriendsPanelRenderer(controller);
    }
    
    @Override
    protected int getItemCount() {
        return controller.getFriendCount();
    }
    
    @Override
    protected boolean handleContentClick(int mouseX, int mouseY) {
        // Check if clicking on a friend item
        Friend clickedFriend = getFriendAtPosition(mouseX, mouseY);
        
        if (clickedFriend != null) {
            int index = controller.getFriends().indexOf(clickedFriend);
            
            if (selectedFriendIndex == index) {
                // Double-click or click on selected - could open chat/options
                onFriendDoubleClick(clickedFriend);
            } else {
                selectedFriendIndex = index;
                onFriendSelected(clickedFriend);
            }
            return true;
        }
        
        return true; // Consume click even if not on item
    }
    
    @Override
    protected void handleContentMouseMove(int mouseX, int mouseY) {
        hoveredFriend = getFriendAtPosition(mouseX, mouseY);
    }
    
    // ═══════════════════════════════════════════════════════════
    // LIFECYCLE HOOKS
    // ═══════════════════════════════════════════════════════════
    
    @Override
    protected void onOpen() {
        super.onOpen();
        hoveredFriend = null;
        selectedFriendIndex = -1;
        System.out.println("[FRIENDS PANEL] Opened");
    }
    
    @Override
    protected void onClose() {
        super.onClose();
        System.out.println("[FRIENDS PANEL] Closed");
    }
    
    // ═══════════════════════════════════════════════════════════
    // RENDERING
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public void draw(Graphics2D g2d) {
        if (!visible) return;
        
        // Update renderer state before drawing
        friendsRenderer.setScrollOffset(scrollOffset);
        friendsRenderer.setHoveredFriend(hoveredFriend);
        friendsRenderer.setSelectedIndex(selectedFriendIndex);
        
        // Call parent draw (which calls renderer.render())
        super.draw(g2d);
    }
    
    // ═══════════════════════════════════════════════════════════
    // FRIEND INTERACTION HANDLERS
    // ═══════════════════════════════════════════════════════════
    
    private void onFriendSelected(Friend friend) {
        System.out.println("[FRIENDS PANEL] Selected: " + friend.getUsername());
        // Could show options: View Profile, Send Message, Remove Friend
    }
    
    private void onFriendDoubleClick(Friend friend) {
        System.out.println("[FRIENDS PANEL] Double-clicked: " + friend.getUsername());
        // Could open private chat or teleport to friend
    }
    
    // ═══════════════════════════════════════════════════════════
    // CONTROLLER EVENT LISTENERS
    // ═══════════════════════════════════════════════════════════
    
    private void setupControllerListeners() {
        controller.setOnFriendAdded(friend -> {
            System.out.println("[FRIENDS PANEL] Friend added: " + friend.getUsername());
            gp.showFriendNotification("You are now friends with " + friend.getUsername() + "!", true);
            refresh();
        });
        
        controller.setOnFriendRemoved(friend -> {
            System.out.println("[FRIENDS PANEL] Friend removed: " + friend.getUsername());
            refresh();
        });
        
        controller.setOnRequestReceived(request -> {
            System.out.println("[FRIENDS PANEL] Request received from: " + request.getFromUsername());
            // Show popup (handled by GamePanel)
            gp.showFriendRequestPopup(request);
        });
        
        controller.setOnRequestAccepted(request -> {
            System.out.println("[FRIENDS PANEL] Request accepted from: " + request.getFromUsername());
        });
        
        controller.setOnRequestRejected(request -> {
            System.out.println("[FRIENDS PANEL] Request rejected from: " + request.getFromUsername());
        });
        
        controller.setOnListChanged(() -> {
            refresh();
        });
    }
    
    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Get the friend at a given mouse position
     */
    private Friend getFriendAtPosition(int mouseX, int mouseY) {
        Rectangle content = layout.getContentBounds();
        
        // Check if within content area
        if (mouseX < content.x || mouseX > content.x + content.width ||
            mouseY < content.y || mouseY > content.y + content.height) {
            return null;
        }
        
        int index = layout.getItemIndexAtY(mouseY, scrollOffset);
        List<Friend> friends = controller.getFriends();
        
        if (index >= 0 && index < friends.size()) {
            return friends.get(index);
        }
        
        return null;
    }
    
    /**
     * Refresh the panel (recalculate scroll bounds, update display)
     */
    @Override
    public void refresh() {
        super.refresh();
        System.out.println("[FRIENDS PANEL] Refreshed - showing " + controller.getFriendCount() + " friends");
    }
    
    // ═══════════════════════════════════════════════════════════
    // PUBLIC API (for GamePanel integration)
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Get the controller (for external access if needed)
     */
    public FriendController getController() {
        return controller;
    }
    
    /**
     * Send a friend request (convenience method)
     */
    public boolean sendFriendRequest(String targetUsername, String targetGender) {
        return controller.sendFriendRequest(targetUsername, targetGender);
    }
    
    /**
     * Accept a friend request (convenience method)
     */
    public void acceptRequest(String fromUsername) {
        controller.acceptRequest(fromUsername);
    }
    
    /**
     * Reject a friend request (convenience method)
     */
    public void rejectRequest(String fromUsername) {
        controller.rejectRequest(fromUsername);
    }
    
    /**
     * Check if someone is a friend (convenience method)
     */
    public boolean isFriend(String username) {
        return controller.isFriend(username);
    }
    
    /**
     * Update a friend's online status
     */
    public void updateFriendStatus(String username, boolean online, String room) {
        controller.updateFriendStatus(username, online, room);
    }
    
    /**
     * Shutdown (save data, close connections)
     */
    public void shutdown() {
        controller.shutdown();
    }
}