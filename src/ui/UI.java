package ui;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.BasicStroke;
import java.awt.RenderingHints;

import main.GamePanel;
import object.*;

/**
 * UI - Displays HUD elements and music player
 */
public class UI {

    GamePanel gp;
    Font theFont;
    Font musicFont;
    BufferedImage credImage;
    
    // ═══════════════════════════════════════════════════════════
    // MUSIC PLAYER UI
    // ═══════════════════════════════════════════════════════════
    
    // Music player dimensions
    private static final int PLAYER_HEIGHT = 50;
    private static final int PLAYER_PADDING = 10;
    private static final int BUTTON_SIZE = 30;
    
    // Music player colors
    private static final Color PLAYER_BG = new Color(30, 30, 30, 230);  // Dark semi-transparent
    private static final Color BUTTON_BG = new Color(60, 60, 60);
    private static final Color BUTTON_HOVER = new Color(80, 80, 80);
    private static final Color BUTTON_ACTIVE = new Color(0, 200, 100);  // Green when playing
    private static final Color TEXT_COLOR = new Color(255, 255, 255);
    private static final Color SONG_TEXT = new Color(0, 200, 255);  // Cyan
    
    // Button positions (will be calculated in draw)
    private int playButtonX, playButtonY;
    private int stopButtonX, stopButtonY;
    
    // Hover states
    private boolean playButtonHovered = false;
    private boolean stopButtonHovered = false;

    public UI(GamePanel gp) {
        this.gp = gp;
        this.theFont = new Font("Arial", Font.PLAIN, 40);
        this.musicFont = new Font("Arial", Font.BOLD, 14);
        
        OBJ_cred obj_cred = new OBJ_cred();
        credImage = obj_cred.image;
    }

    public void draw(Graphics2D g2d) {
        // Enable anti-aliasing for smooth rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw music player at top
        drawMusicPlayer(g2d);
        
        // Draw credits (original functionality)
        drawCredits(g2d);
    }
    
    // ═══════════════════════════════════════════════════════════
    // MUSIC PLAYER RENDERING
    // ═══════════════════════════════════════════════════════════
    
    private void drawMusicPlayer(Graphics2D g2d) {
        int screenWidth = gp.screenWidth;
        
        // Background bar
        g2d.setColor(PLAYER_BG);
        g2d.fillRoundRect(0, 0, screenWidth, PLAYER_HEIGHT, 0, 0);
        
        // Border
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(0, PLAYER_HEIGHT - 1, screenWidth, PLAYER_HEIGHT - 1);
        
        // Calculate button positions
        int buttonY = (PLAYER_HEIGHT - BUTTON_SIZE) / 2;
        playButtonX = PLAYER_PADDING;
        playButtonY = buttonY;
        stopButtonX = playButtonX + BUTTON_SIZE + 10;
        stopButtonY = buttonY;
        
        // Draw Play/Pause button
        drawPlayButton(g2d);
        
        // Draw Stop button
        drawStopButton(g2d);
        
        // Draw song info
        drawSongInfo(g2d);
        
        // Draw volume indicator (optional)
        drawVolumeIndicator(g2d);
    }
    
    private void drawPlayButton(Graphics2D g2d) {
        // Button background
        if (gp.sound != null && gp.sound.isPlaying()) {
            g2d.setColor(BUTTON_ACTIVE);  // Green when playing
        } else if (playButtonHovered) {
            g2d.setColor(BUTTON_HOVER);
        } else {
            g2d.setColor(BUTTON_BG);
        }
        g2d.fillRoundRect(playButtonX, playButtonY, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
        // Button border
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawRoundRect(playButtonX, playButtonY, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
        // Play/Pause icon
        g2d.setColor(Color.WHITE);
        if (gp.sound != null && gp.sound.isPlaying()) {
            // Draw PAUSE icon (two vertical bars)
            int barWidth = 3;
            int barHeight = 12;
            int centerX = playButtonX + BUTTON_SIZE / 2;
            int centerY = playButtonY + BUTTON_SIZE / 2;
            
            g2d.fillRect(centerX - 6, centerY - barHeight / 2, barWidth, barHeight);
            g2d.fillRect(centerX + 3, centerY - barHeight / 2, barWidth, barHeight);
        } else {
            // Draw PLAY icon (triangle)
            int[] xPoints = {
                playButtonX + BUTTON_SIZE / 2 - 4,
                playButtonX + BUTTON_SIZE / 2 - 4,
                playButtonX + BUTTON_SIZE / 2 + 6
            };
            int[] yPoints = {
                playButtonY + BUTTON_SIZE / 2 - 6,
                playButtonY + BUTTON_SIZE / 2 + 6,
                playButtonY + BUTTON_SIZE / 2
            };
            g2d.fillPolygon(xPoints, yPoints, 3);
        }
    }
    
    private void drawStopButton(Graphics2D g2d) {
        // Button background
        if (stopButtonHovered) {
            g2d.setColor(BUTTON_HOVER);
        } else {
            g2d.setColor(BUTTON_BG);
        }
        g2d.fillRoundRect(stopButtonX, stopButtonY, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
        // Button border
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawRoundRect(stopButtonX, stopButtonY, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
        // Stop icon (square)
        g2d.setColor(Color.WHITE);
        int squareSize = 12;
        g2d.fillRect(
            stopButtonX + (BUTTON_SIZE - squareSize) / 2,
            stopButtonY + (BUTTON_SIZE - squareSize) / 2,
            squareSize,
            squareSize
        );
    }
    
    private void drawSongInfo(Graphics2D g2d) {
        g2d.setFont(musicFont);
        
        // Song title
        String songTitle = "♫ Becky G - Arranca ft. Omega";
        g2d.setColor(SONG_TEXT);
        g2d.drawString(songTitle, stopButtonX + BUTTON_SIZE + 20, PLAYER_HEIGHT / 2 + 5);
        
        // Status text
        String status = "";
        if (gp.sound != null && gp.sound.isPlaying()) {
            status = "Now Playing";
            g2d.setColor(BUTTON_ACTIVE);
        } else {
            status = "Paused";
            g2d.setColor(new Color(200, 200, 200));
        }
        
        int statusX = stopButtonX + BUTTON_SIZE + 20 + g2d.getFontMetrics().stringWidth(songTitle) + 15;
        g2d.setFont(new Font("Arial", Font.ITALIC, 11));
        g2d.drawString(status, statusX, PLAYER_HEIGHT / 2 + 5);
    }
    
    private void drawVolumeIndicator(Graphics2D g2d) {
        // Draw speaker icon on the right
        int speakerX = gp.screenWidth - 80;
        int speakerY = PLAYER_HEIGHT / 2 - 8;
        
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("🔊", speakerX, speakerY + 13);
        
        // Volume level text
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("100%", speakerX + 25, speakerY + 11);
    }
    
    // ═══════════════════════════════════════════════════════════
    // CREDITS DISPLAY (Original functionality)
    // ═══════════════════════════════════════════════════════════
    
    private void drawCredits(Graphics2D g2d) {
        // Draw below music player
        int credY = PLAYER_HEIGHT + 20;
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(theFont);
        g2d.drawImage(credImage, 
            GamePanel.ORIGINAL_TILE_SIZE, 
            credY, 
            GamePanel.ORIGINAL_TILE_SIZE * 2, 
            GamePanel.ORIGINAL_TILE_SIZE * 2, 
            null
        );
        
        String nbCredits = gp.player.getCredits() + "";
        g2d.drawString(nbCredits, 50, credY + 31);
    }
    
    // ═══════════════════════════════════════════════════════════
    // MOUSE INTERACTION
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Check if mouse is hovering over play button
     */
    public void updatePlayButtonHover(int mouseX, int mouseY) {
        playButtonHovered = (mouseX >= playButtonX && mouseX <= playButtonX + BUTTON_SIZE &&
                            mouseY >= playButtonY && mouseY <= playButtonY + BUTTON_SIZE);
    }
    
    /**
     * Check if mouse is hovering over stop button
     */
    public void updateStopButtonHover(int mouseX, int mouseY) {
        stopButtonHovered = (mouseX >= stopButtonX && mouseX <= stopButtonX + BUTTON_SIZE &&
                            mouseY >= stopButtonY && mouseY <= stopButtonY + BUTTON_SIZE);
    }
    
    /**
     * Handle click on play button
     */
    public boolean isPlayButtonClicked(int mouseX, int mouseY) {
        return (mouseX >= playButtonX && mouseX <= playButtonX + BUTTON_SIZE &&
                mouseY >= playButtonY && mouseY <= playButtonY + BUTTON_SIZE);
    }
    
    /**
     * Handle click on stop button
     */
    public boolean isStopButtonClicked(int mouseX, int mouseY) {
        return (mouseX >= stopButtonX && mouseX <= stopButtonX + BUTTON_SIZE &&
                mouseY >= stopButtonY && mouseY <= stopButtonY + BUTTON_SIZE);
    }
    
    /**
     * Get the height of the music player for layout calculations
     */
    public static int getMusicPlayerHeight() {
        return PLAYER_HEIGHT;
    }
}