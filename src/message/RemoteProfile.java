package message;

import Entity.RemotePlayer;
import main.GamePanel;

import javax.swing.*;
import java.awt.*;

public class RemoteProfile extends JPanel {
    
    private GamePanel gp;
    private RemotePlayer remotePlayer;
    private boolean isVisible;
    
    private static final int PROFILE_WIDTH = 250;
    private static final int PROFILE_HEIGHT = 150;
    
    public RemoteProfile(GamePanel gp) {
        this.gp = gp;
        this.isVisible = false;
        setLayout(null);
        setOpaque(false);
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
    
    public void draw(Graphics2D g2d) {
        if (!isVisible || remotePlayer == null) {
            return;
        }
        
        // Position the profile near the remote player's sprite
        int profileX = remotePlayer.spriteX + (2 * gp.tileSizeWidth) + 20;
        int profileY = remotePlayer.spriteY;
        
        // Ensure profile stays within screen bounds
        if (profileX + PROFILE_WIDTH > gp.screenWidth) {
            profileX = remotePlayer.spriteX - PROFILE_WIDTH - 20;
        }
        if (profileY + PROFILE_HEIGHT > gp.screenHeight) {
            profileY = gp.screenHeight - PROFILE_HEIGHT - 20;
        }
        if (profileY < 0) {
            profileY = 20;
        }
        
        // Draw background panel
        g2d.setColor(new Color(255, 255, 255, 240));
        g2d.fillRoundRect(profileX, profileY, PROFILE_WIDTH, PROFILE_HEIGHT, 20, 20);
        
        // Draw border
        g2d.setColor(new Color(0, 102, 204));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(profileX, profileY, PROFILE_WIDTH, PROFILE_HEIGHT, 20, 20);
        
        // Draw header background
        g2d.setColor(new Color(0, 102, 204));
        g2d.fillRoundRect(profileX, profileY, PROFILE_WIDTH, 40, 20, 20);
        g2d.fillRect(profileX, profileY + 20, PROFILE_WIDTH, 20);
        
        // Draw "Player Profile" title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Player Profile", profileX + 15, profileY + 27);
        
        // Draw player information
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Username
        g2d.drawString("Username:", profileX + 20, profileY + 65);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString(remotePlayer.name, profileX + 110, profileY + 65);
        
        // Gender
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Gender:", profileX + 20, profileY + 95);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        String genderDisplay = remotePlayer.gender.toString().charAt(0) + 
                               remotePlayer.gender.toString().substring(1).toLowerCase();
        g2d.drawString(genderDisplay, profileX + 110, profileY + 95);
        
        // Draw close hint
        g2d.setFont(new Font("Arial", Font.ITALIC, 11));
        g2d.setColor(new Color(100, 100, 100));
        g2d.drawString("Click again to close", profileX + 60, profileY + 130);
    }
}