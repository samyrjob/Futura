package ui.profile;

import Entity.RemotePlayer;
import main.GamePanel;

import java.awt.*;

/**
 * RemoteProfile - Shows profile panel when clicking on another player
 * Now includes "Send Friend Request" button!
 */
public class RemoteProfile {
    
    private GamePanel gp;
    private RemotePlayer remotePlayer;
    private boolean isVisible;
    
    private static final int PROFILE_WIDTH = 250;
    private static final int PROFILE_HEIGHT = 185;  // Increased for friend button
    
    // Friend button state
    private boolean friendButtonHovered = false;
    private int friendButtonX, friendButtonY;
    private static final int BUTTON_WIDTH = 180;
    private static final int BUTTON_HEIGHT = 32;
    
    // Colors
    private static final Color HEADER_BG = new Color(0, 102, 204);
    private static final Color FRIEND_BUTTON_COLOR = new Color(156, 39, 176);  // Purple
    private static final Color FRIEND_BUTTON_HOVER = new Color(186, 85, 211);
    private static final Color ALREADY_FRIENDS_COLOR = new Color(76, 175, 80);  // Green
    
    public RemoteProfile(GamePanel gp) {
        this.gp = gp;
        this.isVisible = false;
    }
    
    public void showProfile(RemotePlayer player) {
        this.remotePlayer = player;
        this.isVisible = true;
    }
    
    public void hideProfile() {
        this.isVisible = false;
        this.remotePlayer = null;
    }
    
    public void toggleProfile(RemotePlayer player) {
        if (isVisible && remotePlayer == player) {
            hideProfile();
        } else {
            showProfile(player);
        }
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public RemotePlayer getRemotePlayer() {
        return remotePlayer;
    }
    
    // ═══════════════════════════════════════════════════════════
    // MOUSE HANDLING
    // ═══════════════════════════════════════════════════════════
    
    public void handleMouseMove(int mouseX, int mouseY) {
        if (!isVisible || remotePlayer == null) {
            friendButtonHovered = false;
            return;
        }
        
        friendButtonHovered = (mouseX >= friendButtonX && mouseX <= friendButtonX + BUTTON_WIDTH &&
                              mouseY >= friendButtonY && mouseY <= friendButtonY + BUTTON_HEIGHT);
    }
    
    public boolean handleClick(int mouseX, int mouseY) {
        if (!isVisible || remotePlayer == null) return false;
        
        // Check friend button click
        if (mouseX >= friendButtonX && mouseX <= friendButtonX + BUTTON_WIDTH &&
            mouseY >= friendButtonY && mouseY <= friendButtonY + BUTTON_HEIGHT) {
            
            sendFriendRequest();
            return true;
        }
        
        return false;
    }
    
    private void sendFriendRequest() {
        if (gp.friendManager == null || remotePlayer == null) return;
        
        // Check if already friends
        if (gp.friendManager.isFriend(remotePlayer.name)) {
            System.out.println("[REMOTE PROFILE] Already friends with: " + remotePlayer.name);
            showNotification("Already friends with " + remotePlayer.name, false);
            return;
        }
        
        // Send request
        boolean sent = gp.friendManager.sendFriendRequest(remotePlayer.name, remotePlayer.gender.toString());
        
        if (sent) {
            showNotification("Friend request sent to " + remotePlayer.name + "!", true);
            hideProfile();  // Close profile after sending
        } else {
            showNotification("Could not send request", false);
        }
    }
    
    private void showNotification(String message, boolean success) {
        if (gp.player != null) {
            int bubbleY = gp.player.spriteY + 50;
            String prefix = success ? "[FRIEND] ✓ " : "[FRIEND] ";
            gp.player.messages.add(new Entity.Player.Message(prefix + message, bubbleY));
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // RENDERING
    // ═══════════════════════════════════════════════════════════
    
    public void draw(Graphics2D g2d) {
        if (!isVisible || remotePlayer == null) {
            return;
        }
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Calculate position near the remote player's sprite
        int profileX = remotePlayer.spriteX + (2 * gp.tileSizeWidth) + 20;
        int profileY = remotePlayer.spriteY;
        
        // Ensure profile stays within screen bounds
        if (profileX + PROFILE_WIDTH > gp.screenWidth) {
            profileX = remotePlayer.spriteX - PROFILE_WIDTH - 20;
        }
        if (profileY + PROFILE_HEIGHT > gp.screenHeight) {
            profileY = gp.screenHeight - PROFILE_HEIGHT - 20;
        }
        if (profileY < 60) {
            profileY = 60;
        }
        
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fillRoundRect(profileX + 4, profileY + 4, PROFILE_WIDTH, PROFILE_HEIGHT, 20, 20);
        
        // Draw background panel
        g2d.setColor(new Color(255, 255, 255, 245));
        g2d.fillRoundRect(profileX, profileY, PROFILE_WIDTH, PROFILE_HEIGHT, 20, 20);
        
        // Draw border
        g2d.setColor(HEADER_BG);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(profileX, profileY, PROFILE_WIDTH, PROFILE_HEIGHT, 20, 20);
        
        // Draw header background
        g2d.setColor(HEADER_BG);
        g2d.fillRoundRect(profileX, profileY, PROFILE_WIDTH, 40, 20, 20);
        g2d.fillRect(profileX, profileY + 20, PROFILE_WIDTH, 20);
        
        // Draw "Player Profile" title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Player Profile", profileX + 15, profileY + 27);
        
        // Draw player information
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Username label
        g2d.drawString("Username:", profileX + 20, profileY + 65);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(new Color(50, 50, 50));
        g2d.drawString(remotePlayer.name, profileX + 110, profileY + 65);
        
        // Gender label
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Gender:", profileX + 20, profileY + 92);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        String genderDisplay = remotePlayer.gender.toString().charAt(0) + 
                               remotePlayer.gender.toString().substring(1).toLowerCase();
        
        // Gender with colored icon
        Color genderColor = remotePlayer.gender.toString().equalsIgnoreCase("FEMALE") ? 
            new Color(233, 30, 99) : new Color(33, 150, 243);
        String genderIcon = remotePlayer.gender.toString().equalsIgnoreCase("FEMALE") ? " ♀" : " ♂";
        g2d.setColor(new Color(50, 50, 50));
        g2d.drawString(genderDisplay, profileX + 110, profileY + 92);
        g2d.setColor(genderColor);
        g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        g2d.drawString(genderIcon, profileX + 110 + g2d.getFontMetrics(new Font("Arial", Font.PLAIN, 14)).stringWidth(genderDisplay), profileY + 93);
        
        // Friend Request Button
        friendButtonX = profileX + (PROFILE_WIDTH - BUTTON_WIDTH) / 2;
        friendButtonY = profileY + 115;
        
        // Check friendship status
        boolean alreadyFriends = gp.friendManager != null && gp.friendManager.isFriend(remotePlayer.name);
        
        if (alreadyFriends) {
            // Show "Already Friends" label (green)
            g2d.setColor(ALREADY_FRIENDS_COLOR);
            g2d.fillRoundRect(friendButtonX, friendButtonY, BUTTON_WIDTH, BUTTON_HEIGHT, 10, 10);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            FontMetrics fm = g2d.getFontMetrics();
            String text = "✓ Already Friends";
            int textX = friendButtonX + (BUTTON_WIDTH - fm.stringWidth(text)) / 2;
            g2d.drawString(text, textX, friendButtonY + 21);
        } else {
            // Show "Send Friend Request" button (purple)
            g2d.setColor(friendButtonHovered ? FRIEND_BUTTON_HOVER : FRIEND_BUTTON_COLOR);
            g2d.fillRoundRect(friendButtonX, friendButtonY, BUTTON_WIDTH, BUTTON_HEIGHT, 10, 10);
            
            // Button border on hover
            if (friendButtonHovered) {
                g2d.setColor(FRIEND_BUTTON_COLOR.darker());
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(friendButtonX, friendButtonY, BUTTON_WIDTH, BUTTON_HEIGHT, 10, 10);
            }
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            FontMetrics fm = g2d.getFontMetrics();
            String text = "💬 Send Friend Request";
            int textX = friendButtonX + (BUTTON_WIDTH - fm.stringWidth(text)) / 2;
            g2d.drawString(text, textX, friendButtonY + 21);
        }
        
        // Draw close hint
        g2d.setFont(new Font("Arial", Font.ITALIC, 10));
        g2d.setColor(new Color(120, 120, 120));
        String hint = "Click player again to close";
        FontMetrics fm = g2d.getFontMetrics();
        int hintX = profileX + (PROFILE_WIDTH - fm.stringWidth(hint)) / 2;
        g2d.drawString(hint, hintX, profileY + PROFILE_HEIGHT - 10);
    }
}
