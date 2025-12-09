package friend;

import Entity.RemotePlayer;
import main.GamePanel;

import java.awt.*;

/**
 * Popup that appears above a player when they send you a friend request
 * Shows Accept (green check) and Reject (red X) buttons
 * Habbo Hotel style!
 */
public class FriendRequestPopup {
    
    private GamePanel gp;
    private FriendRequest request;
    private RemotePlayer senderPlayer;
    private boolean visible;
    
    // Popup dimensions
    private static final int POPUP_WIDTH = 220;
    private static final int POPUP_HEIGHT = 90;
    private static final int BUTTON_SIZE = 35;
    
    // Colors
    private static final Color BG_COLOR = new Color(255, 255, 255, 245);
    private static final Color HEADER_BG = new Color(156, 39, 176);  // Purple
    private static final Color ACCEPT_COLOR = new Color(76, 175, 80);
    private static final Color ACCEPT_HOVER = new Color(102, 187, 106);
    private static final Color REJECT_COLOR = new Color(244, 67, 54);
    private static final Color REJECT_HOVER = new Color(239, 83, 80);
    
    // Button positions (calculated during draw)
    private int acceptX, acceptY;
    private int rejectX, rejectY;
    private int popupX, popupY;
    
    // Hover states
    private boolean acceptHovered = false;
    private boolean rejectHovered = false;
    
    // Animation
    private float alpha = 0f;
    private long showTime;
    private static final long AUTO_HIDE_DELAY = 30000; // 30 seconds
    
    public FriendRequestPopup(GamePanel gp) {
        this.gp = gp;
        this.visible = false;
    }
    
    // ═══════════════════════════════════════════════════════════
    // SHOW/HIDE
    // ═══════════════════════════════════════════════════════════
    
    public void show(FriendRequest request, RemotePlayer sender) {
        this.request = request;
        this.senderPlayer = sender;
        this.visible = true;
        this.alpha = 0f;
        this.showTime = System.currentTimeMillis();
        System.out.println("[POPUP] Showing friend request from: " + request.getFromUsername());
    }
    
    public void hide() {
        this.visible = false;
        this.request = null;
        this.senderPlayer = null;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public FriendRequest getRequest() {
        return request;
    }
    
    // ═══════════════════════════════════════════════════════════
    // UPDATE (for auto-hide)
    // ═══════════════════════════════════════════════════════════
    
    public void update() {
        if (visible && System.currentTimeMillis() - showTime > AUTO_HIDE_DELAY) {
            System.out.println("[POPUP] Auto-hiding friend request popup (timeout)");
            hide();
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // INPUT HANDLING
    // ═══════════════════════════════════════════════════════════
    
    public void handleMouseMove(int mouseX, int mouseY) {
        if (!visible) return;
        
        acceptHovered = isInButton(mouseX, mouseY, acceptX, acceptY);
        rejectHovered = isInButton(mouseX, mouseY, rejectX, rejectY);
    }
    
    public boolean handleClick(int mouseX, int mouseY) {
        if (!visible) return false;
        
        if (isInButton(mouseX, mouseY, acceptX, acceptY)) {
            // Accept friend request
            if (gp.friendManager != null && request != null) {
                gp.friendManager.acceptRequest(request.getFromUsername());
            }
            hide();
            return true;
        }
        
        if (isInButton(mouseX, mouseY, rejectX, rejectY)) {
            // Reject friend request
            if (gp.friendManager != null && request != null) {
                gp.friendManager.rejectRequest(request.getFromUsername());
            }
            hide();
            return true;
        }
        
        return false;
    }
    
    private boolean isInButton(int mouseX, int mouseY, int buttonX, int buttonY) {
        return mouseX >= buttonX && mouseX <= buttonX + BUTTON_SIZE &&
               mouseY >= buttonY && mouseY <= buttonY + BUTTON_SIZE;
    }
    
    public boolean containsPoint(int mouseX, int mouseY) {
        if (!visible) return false;
        return mouseX >= popupX && mouseX <= popupX + POPUP_WIDTH &&
               mouseY >= popupY && mouseY <= popupY + POPUP_HEIGHT + 15;
    }
    
    // ═══════════════════════════════════════════════════════════
    // RENDERING
    // ═══════════════════════════════════════════════════════════
    
    public void draw(Graphics2D g2d) {
        if (!visible || request == null) return;
        
        // Fade in animation
        if (alpha < 1.0f) {
            alpha = Math.min(1.0f, alpha + 0.08f);
        }
        
        // Calculate position (above sender player or center if player not visible)
        if (senderPlayer != null) {
            popupX = senderPlayer.spriteX + gp.tileSizeWidth - POPUP_WIDTH / 2;
            popupY = senderPlayer.spriteY - POPUP_HEIGHT - 30;
        } else {
            // Center of screen if player not found
            popupX = (gp.screenWidth - POPUP_WIDTH) / 2;
            popupY = 150;
        }
        
        // Keep on screen
        popupX = Math.max(10, Math.min(popupX, gp.screenWidth - POPUP_WIDTH - 10));
        popupY = Math.max(60, popupY);
        
        // Calculate button positions
        int buttonsY = popupY + POPUP_HEIGHT - BUTTON_SIZE - 10;
        int centerX = popupX + POPUP_WIDTH / 2;
        acceptX = centerX - BUTTON_SIZE - 20;
        acceptY = buttonsY;
        rejectX = centerX + 20;
        rejectY = buttonsY;
        
        // Set transparency
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRoundRect(popupX + 4, popupY + 4, POPUP_WIDTH, POPUP_HEIGHT, 15, 15);
        
        // Draw background
        g2d.setColor(BG_COLOR);
        g2d.fillRoundRect(popupX, popupY, POPUP_WIDTH, POPUP_HEIGHT, 15, 15);
        
        // Draw header
        g2d.setColor(HEADER_BG);
        g2d.fillRoundRect(popupX, popupY, POPUP_WIDTH, 30, 15, 15);
        g2d.fillRect(popupX, popupY + 15, POPUP_WIDTH, 15);
        
        // Draw border
        g2d.setColor(HEADER_BG);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(popupX, popupY, POPUP_WIDTH, POPUP_HEIGHT, 15, 15);
        
        // Draw header text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String headerText = "💬 Friend Request";
        FontMetrics fm = g2d.getFontMetrics();
        int headerX = popupX + (POPUP_WIDTH - fm.stringWidth(headerText)) / 2;
        g2d.drawString(headerText, headerX, popupY + 20);
        
        // Draw sender name
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        fm = g2d.getFontMetrics();
        String nameText = request.getFromUsername();
        int nameX = popupX + (POPUP_WIDTH - fm.stringWidth(nameText)) / 2;
        g2d.drawString(nameText, nameX, popupY + 50);
        
        // Draw Accept button (green with checkmark)
        drawButton(g2d, acceptX, acceptY, acceptHovered ? ACCEPT_HOVER : ACCEPT_COLOR, true);
        
        // Draw Reject button (red with X)
        drawButton(g2d, rejectX, rejectY, rejectHovered ? REJECT_HOVER : REJECT_COLOR, false);
        
        // Draw pointer to player (if player is visible)
        if (senderPlayer != null) {
            int pointerCenterX = senderPlayer.spriteX + gp.tileSizeWidth;
            // Only draw pointer if it would be within the popup bounds
            if (pointerCenterX >= popupX && pointerCenterX <= popupX + POPUP_WIDTH) {
                int[] pointerX = {pointerCenterX - 10, pointerCenterX + 10, pointerCenterX};
                int[] pointerY = {popupY + POPUP_HEIGHT, popupY + POPUP_HEIGHT, popupY + POPUP_HEIGHT + 15};
                g2d.setColor(BG_COLOR);
                g2d.fillPolygon(pointerX, pointerY, 3);
                g2d.setColor(HEADER_BG);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(pointerX[0], pointerY[0], pointerX[2], pointerY[2]);
                g2d.drawLine(pointerX[1], pointerY[1], pointerX[2], pointerY[2]);
            }
        }
        
        // Restore composite
        g2d.setComposite(originalComposite);
    }
    
    private void drawButton(Graphics2D g2d, int x, int y, Color color, boolean isAccept) {
        // Button background
        g2d.setColor(color);
        g2d.fillRoundRect(x, y, BUTTON_SIZE, BUTTON_SIZE, 10, 10);
        
        // Button border
        g2d.setColor(color.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, BUTTON_SIZE, BUTTON_SIZE, 10, 10);
        
        // Icon
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        if (isAccept) {
            // Draw checkmark ✓
            int[] checkX = {x + 9, x + 15, x + 26};
            int[] checkY = {y + 18, y + 26, y + 12};
            g2d.drawPolyline(checkX, checkY, 3);
        } else {
            // Draw X
            int padding = 10;
            g2d.drawLine(x + padding, y + padding, x + BUTTON_SIZE - padding, y + BUTTON_SIZE - padding);
            g2d.drawLine(x + BUTTON_SIZE - padding, y + padding, x + padding, y + BUTTON_SIZE - padding);
        }
    }
}
