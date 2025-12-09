package view.friend;

import controller.friend.FriendController;
import Entity.RemotePlayer;
import main.GamePanel;
import model.friend.FriendRequest;

import java.awt.*;

/**
 * Popup that appears above a player when they send you a friend request.
 * Shows Accept (green check) and Reject (red X) buttons.
 * Habbo Hotel style!
 * 
 * Part of MVC architecture - this is the View layer.
 * Note: This is a special popup, not a toolbar panel.
 */
public class FriendRequestPopup {
    
    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════
    
    private static final int POPUP_WIDTH = 220;
    private static final int POPUP_HEIGHT = 90;
    private static final int BUTTON_SIZE = 35;
    private static final long AUTO_HIDE_DELAY = 30000; // 30 seconds
    
    // Colors
    private static final Color BG_COLOR = new Color(255, 255, 255, 245);
    private static final Color HEADER_BG = new Color(156, 39, 176);  // Purple
    private static final Color ACCEPT_COLOR = new Color(76, 175, 80);
    private static final Color ACCEPT_HOVER = new Color(102, 187, 106);
    private static final Color REJECT_COLOR = new Color(244, 67, 54);
    private static final Color REJECT_HOVER = new Color(239, 83, 80);
    
    // ═══════════════════════════════════════════════════════════
    // DEPENDENCIES
    // ═══════════════════════════════════════════════════════════
    
    private final GamePanel gp;
    private final FriendController controller;
    
    // ═══════════════════════════════════════════════════════════
    // STATE
    // ═══════════════════════════════════════════════════════════
    
    private FriendRequest request;
    private RemotePlayer senderPlayer;
    private boolean visible;
    
    // Button positions (calculated during draw)
    private int acceptX, acceptY;
    private int rejectX, rejectY;
    private int popupX, popupY;
    
    // Hover states
    private boolean acceptHovered;
    private boolean rejectHovered;
    
    // Animation
    private float alpha;
    private long showTime;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public FriendRequestPopup(GamePanel gp, FriendController controller) {
        this.gp = gp;
        this.controller = controller;
        this.visible = false;
        this.alpha = 0f;
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
    // UPDATE
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
            if (controller != null && request != null) {
                controller.acceptRequest(request.getFromUsername());
            }
            hide();
            return true;
        }
        
        if (isInButton(mouseX, mouseY, rejectX, rejectY)) {
            // Reject friend request
            if (controller != null && request != null) {
                controller.rejectRequest(request.getFromUsername());
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
        
        calculatePosition();
        calculateButtonPositions();
        
        // Set transparency
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        drawShadow(g2d);
        drawBackground(g2d);
        drawHeader(g2d);
        drawBorder(g2d);
        drawSenderName(g2d);
        drawAcceptButton(g2d);
        drawRejectButton(g2d);
        drawPointer(g2d);
        
        g2d.setComposite(originalComposite);
    }
    
    private void calculatePosition() {
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
    }
    
    private void calculateButtonPositions() {
        int buttonsY = popupY + POPUP_HEIGHT - BUTTON_SIZE - 10;
        int centerX = popupX + POPUP_WIDTH / 2;
        acceptX = centerX - BUTTON_SIZE - 20;
        acceptY = buttonsY;
        rejectX = centerX + 20;
        rejectY = buttonsY;
    }
    
    private void drawShadow(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRoundRect(popupX + 4, popupY + 4, POPUP_WIDTH, POPUP_HEIGHT, 15, 15);
    }
    
    private void drawBackground(Graphics2D g2d) {
        g2d.setColor(BG_COLOR);
        g2d.fillRoundRect(popupX, popupY, POPUP_WIDTH, POPUP_HEIGHT, 15, 15);
    }
    
    private void drawHeader(Graphics2D g2d) {
        g2d.setColor(HEADER_BG);
        g2d.fillRoundRect(popupX, popupY, POPUP_WIDTH, 30, 15, 15);
        g2d.fillRect(popupX, popupY + 15, POPUP_WIDTH, 15);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String headerText = "💬 Friend Request";
        FontMetrics fm = g2d.getFontMetrics();
        int headerX = popupX + (POPUP_WIDTH - fm.stringWidth(headerText)) / 2;
        g2d.drawString(headerText, headerX, popupY + 20);
    }
    
    private void drawBorder(Graphics2D g2d) {
        g2d.setColor(HEADER_BG);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(popupX, popupY, POPUP_WIDTH, POPUP_HEIGHT, 15, 15);
    }
    
    private void drawSenderName(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        String nameText = request.getFromUsername();
        int nameX = popupX + (POPUP_WIDTH - fm.stringWidth(nameText)) / 2;
        g2d.drawString(nameText, nameX, popupY + 50);
    }
    
    private void drawAcceptButton(Graphics2D g2d) {
        // Background
        g2d.setColor(acceptHovered ? ACCEPT_HOVER : ACCEPT_COLOR);
        g2d.fillRoundRect(acceptX, acceptY, BUTTON_SIZE, BUTTON_SIZE, 10, 10);
        
        // Border
        g2d.setColor(ACCEPT_COLOR.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(acceptX, acceptY, BUTTON_SIZE, BUTTON_SIZE, 10, 10);
        
        // Checkmark
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int[] checkX = {acceptX + 9, acceptX + 15, acceptX + 26};
        int[] checkY = {acceptY + 18, acceptY + 26, acceptY + 12};
        g2d.drawPolyline(checkX, checkY, 3);
    }
    
    private void drawRejectButton(Graphics2D g2d) {
        // Background
        g2d.setColor(rejectHovered ? REJECT_HOVER : REJECT_COLOR);
        g2d.fillRoundRect(rejectX, rejectY, BUTTON_SIZE, BUTTON_SIZE, 10, 10);
        
        // Border
        g2d.setColor(REJECT_COLOR.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(rejectX, rejectY, BUTTON_SIZE, BUTTON_SIZE, 10, 10);
        
        // X icon
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int padding = 10;
        g2d.drawLine(rejectX + padding, rejectY + padding, 
                    rejectX + BUTTON_SIZE - padding, rejectY + BUTTON_SIZE - padding);
        g2d.drawLine(rejectX + BUTTON_SIZE - padding, rejectY + padding, 
                    rejectX + padding, rejectY + BUTTON_SIZE - padding);
    }
    
    private void drawPointer(Graphics2D g2d) {
        if (senderPlayer == null) return;
        
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
}