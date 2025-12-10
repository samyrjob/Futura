package ui.hud;

import java.awt.*;
import java.awt.image.BufferedImage;
import main.GamePanel;
import object.OBJ_cred;
import ui.UIComponent;

/**
 * CreditsDisplay - Shows player's credit count
 */
public class CreditsDisplay implements UIComponent {
    
    private GamePanel gp;
    private BufferedImage credImage;
    private int yOffset;
    
    private static final int ICON_SIZE = GamePanel.ORIGINAL_TILE_SIZE * 2;
    
    public CreditsDisplay(GamePanel gp, int yOffset) {
        this.gp = gp;
        this.yOffset = yOffset;
        this.credImage = new OBJ_cred().image;
    }
    
    @Override
    public void draw(Graphics2D g2d) {
        int x = GamePanel.ORIGINAL_TILE_SIZE;
        int y = yOffset;
        
        // Draw icon
        g2d.drawImage(credImage, x, y, ICON_SIZE, ICON_SIZE, null);
        
        // Draw count
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 40));
        g2d.drawString(String.valueOf(gp.player.getCredits()), x + 34, y + 31);
    }
    
    @Override
    public void handleMouseMove(int mouseX, int mouseY) {
        // No hover effects
    }
    
    @Override
    public boolean handleClick(int mouseX, int mouseY) {
        return false;  // Not clickable
    }
    
    @Override
    public boolean containsPoint(int x, int y) {
        return false;  // Not interactive
    }
    
    @Override
    public int getHeight() {
        return ICON_SIZE;
    }
}