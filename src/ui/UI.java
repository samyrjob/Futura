// package ui;

// import java.awt.Font;
// import java.awt.Color;
// import java.awt.Graphics2D;
// import java.awt.image.BufferedImage;
// import java.awt.BasicStroke;
// import java.awt.RenderingHints;

// import main.GamePanel;
// import object.*;

// /**
//  * UI - Displays HUD elements and music player WITH VOLUME CONTROL
//  */
// public class UI {

//     GamePanel gp;
//     Font theFont;
//     Font musicFont;
//     BufferedImage credImage;
    
//     // ═══════════════════════════════════════════════════════════
//     // MUSIC PLAYER UI
//     // ═══════════════════════════════════════════════════════════
    
//     // Music player dimensions
//     private static final int PLAYER_HEIGHT = 50;
//     private static final int PLAYER_PADDING = 10;
//     private static final int BUTTON_SIZE = 30;
    
//     // Music player colors
//     private static final Color PLAYER_BG = new Color(30, 30, 30, 230);
//     private static final Color BUTTON_BG = new Color(60, 60, 60);
//     private static final Color BUTTON_HOVER = new Color(80, 80, 80);
//     private static final Color BUTTON_ACTIVE = new Color(0, 200, 100);
//     private static final Color TEXT_COLOR = new Color(255, 255, 255);
//     private static final Color SONG_TEXT = new Color(0, 200, 255);
    
//     // Button positions
//     private int playButtonX, playButtonY;
//     private int stopButtonX, stopButtonY;
//     private int volumeUpX, volumeUpY;      // ✨ NEW
//     private int volumeDownX, volumeDownY;  // ✨ NEW
    
//     // Hover states
//     private boolean playButtonHovered = false;
//     private boolean stopButtonHovered = false;
//     private boolean volumeUpHovered = false;   // ✨ NEW
//     private boolean volumeDownHovered = false; // ✨ NEW
    
//     // Volume state
//     private float currentVolume = 1.0f;  // ✨ NEW - 0.0 to 1.0

//     public UI(GamePanel gp) {
//         this.gp = gp;
//         this.theFont = new Font("Arial", Font.PLAIN, 40);
//         this.musicFont = new Font("Arial", Font.BOLD, 14);
        
//         OBJ_cred obj_cred = new OBJ_cred();
//         credImage = obj_cred.image;
//     }

//     public void draw(Graphics2D g2d) {
//         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
//         drawMusicPlayer(g2d);
//         drawCredits(g2d);
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // MUSIC PLAYER RENDERING
//     // ═══════════════════════════════════════════════════════════
    
//     private void drawMusicPlayer(Graphics2D g2d) {
//         int screenWidth = gp.screenWidth;
        
//         // Background bar
//         g2d.setColor(PLAYER_BG);
//         g2d.fillRoundRect(0, 0, screenWidth, PLAYER_HEIGHT, 0, 0);
        
//         // Border
//         g2d.setColor(new Color(100, 100, 100));
//         g2d.setStroke(new BasicStroke(2));
//         g2d.drawLine(0, PLAYER_HEIGHT - 1, screenWidth, PLAYER_HEIGHT - 1);
        
//         // Calculate button positions
//         int buttonY = (PLAYER_HEIGHT - BUTTON_SIZE) / 2;
//         playButtonX = PLAYER_PADDING;
//         playButtonY = buttonY;
//         stopButtonX = playButtonX + BUTTON_SIZE + 10;
//         stopButtonY = buttonY;
        
//         // Draw Play/Pause button
//         drawPlayButton(g2d);
        
//         // Draw Stop button
//         drawStopButton(g2d);
        
//         // Draw song info
//         drawSongInfo(g2d);
        
//         // ✨ NEW - Draw volume controls on the right
//         drawVolumeControls(g2d);
//     }
    
//     private void drawPlayButton(Graphics2D g2d) {
//         if (gp.sound != null && gp.sound.isPlaying()) {
//             g2d.setColor(BUTTON_ACTIVE);
//         } else if (playButtonHovered) {
//             g2d.setColor(BUTTON_HOVER);
//         } else {
//             g2d.setColor(BUTTON_BG);
//         }
//         g2d.fillRoundRect(playButtonX, playButtonY, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
//         g2d.setColor(new Color(150, 150, 150));
//         g2d.drawRoundRect(playButtonX, playButtonY, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
//         g2d.setColor(Color.WHITE);
//         if (gp.sound != null && gp.sound.isPlaying()) {
//             // Pause icon
//             int barWidth = 3;
//             int barHeight = 12;
//             int centerX = playButtonX + BUTTON_SIZE / 2;
//             int centerY = playButtonY + BUTTON_SIZE / 2;
            
//             g2d.fillRect(centerX - 6, centerY - barHeight / 2, barWidth, barHeight);
//             g2d.fillRect(centerX + 3, centerY - barHeight / 2, barWidth, barHeight);
//         } else {
//             // Play icon
//             int[] xPoints = {
//                 playButtonX + BUTTON_SIZE / 2 - 4,
//                 playButtonX + BUTTON_SIZE / 2 - 4,
//                 playButtonX + BUTTON_SIZE / 2 + 6
//             };
//             int[] yPoints = {
//                 playButtonY + BUTTON_SIZE / 2 - 6,
//                 playButtonY + BUTTON_SIZE / 2 + 6,
//                 playButtonY + BUTTON_SIZE / 2
//             };
//             g2d.fillPolygon(xPoints, yPoints, 3);
//         }
//     }
    
//     private void drawStopButton(Graphics2D g2d) {
//         if (stopButtonHovered) {
//             g2d.setColor(BUTTON_HOVER);
//         } else {
//             g2d.setColor(BUTTON_BG);
//         }
//         g2d.fillRoundRect(stopButtonX, stopButtonY, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
//         g2d.setColor(new Color(150, 150, 150));
//         g2d.drawRoundRect(stopButtonX, stopButtonY, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
//         g2d.setColor(Color.WHITE);
//         int squareSize = 12;
//         g2d.fillRect(
//             stopButtonX + (BUTTON_SIZE - squareSize) / 2,
//             stopButtonY + (BUTTON_SIZE - squareSize) / 2,
//             squareSize,
//             squareSize
//         );
//     }
    
//     private void drawSongInfo(Graphics2D g2d) {
//         g2d.setFont(musicFont);
        
//         String songTitle = "♫ Becky G - Arranca ft. Omega";
//         g2d.setColor(SONG_TEXT);
//         g2d.drawString(songTitle, stopButtonX + BUTTON_SIZE + 20, PLAYER_HEIGHT / 2 + 5);
        
//         String status = "";
//         if (gp.sound != null && gp.sound.isPlaying()) {
//             status = "Now Playing";
//             g2d.setColor(BUTTON_ACTIVE);
//         } else {
//             status = "Paused";
//             g2d.setColor(new Color(200, 200, 200));
//         }
        
//         int statusX = stopButtonX + BUTTON_SIZE + 20 + g2d.getFontMetrics().stringWidth(songTitle) + 15;
//         g2d.setFont(new Font("Arial", Font.ITALIC, 11));
//         g2d.drawString(status, statusX, PLAYER_HEIGHT / 2 + 5);
//     }
    
//     // ✨ NEW - Volume controls with + and - buttons
//     private void drawVolumeControls(Graphics2D g2d) {
//         int rightPadding = 20;
//         int volumeWidth = 150;
//         int volumeX = gp.screenWidth - volumeWidth - rightPadding;
        
//         // Volume down button (-)
//         volumeDownX = volumeX;
//         volumeDownY = (PLAYER_HEIGHT - BUTTON_SIZE) / 2;
        
//         if (volumeDownHovered) {
//             g2d.setColor(BUTTON_HOVER);
//         } else {
//             g2d.setColor(BUTTON_BG);
//         }
//         g2d.fillRoundRect(volumeDownX, volumeDownY, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
//         g2d.setColor(new Color(150, 150, 150));
//         g2d.drawRoundRect(volumeDownX, volumeDownY, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
//         // Draw minus sign
//         g2d.setColor(Color.WHITE);
//         g2d.setStroke(new BasicStroke(3));
//         g2d.drawLine(
//             volumeDownX + 8, 
//             volumeDownY + BUTTON_SIZE / 2,
//             volumeDownX + BUTTON_SIZE - 8,
//             volumeDownY + BUTTON_SIZE / 2
//         );
        
//         // Volume percentage display
//         int volumePercent = (int) (currentVolume * 100);
//         String volumeText = volumePercent + "%";
        
//         g2d.setFont(new Font("Arial", Font.BOLD, 14));
//         int textWidth = g2d.getFontMetrics().stringWidth(volumeText);
//         int textX = volumeDownX + BUTTON_SIZE + 15 + (30 - textWidth) / 2;
        
//         // Color based on volume level
//         if (volumePercent > 70) {
//             g2d.setColor(BUTTON_ACTIVE);  // Green
//         } else if (volumePercent > 30) {
//             g2d.setColor(new Color(255, 165, 0));  // Orange
//         } else {
//             g2d.setColor(new Color(255, 100, 100));  // Red
//         }
        
//         g2d.drawString(volumeText, textX, PLAYER_HEIGHT / 2 + 5);
        
//         // Volume up button (+)
//         volumeUpX = volumeDownX + BUTTON_SIZE + 60;
//         volumeUpY = (PLAYER_HEIGHT - BUTTON_SIZE) / 2;
        
//         if (volumeUpHovered) {
//             g2d.setColor(BUTTON_HOVER);
//         } else {
//             g2d.setColor(BUTTON_BG);
//         }
//         g2d.fillRoundRect(volumeUpX, volumeUpY, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
//         g2d.setColor(new Color(150, 150, 150));
//         g2d.drawRoundRect(volumeUpX, volumeUpY, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
//         // Draw plus sign
//         g2d.setColor(Color.WHITE);
//         g2d.setStroke(new BasicStroke(3));
//         // Horizontal line
//         g2d.drawLine(
//             volumeUpX + 8,
//             volumeUpY + BUTTON_SIZE / 2,
//             volumeUpX + BUTTON_SIZE - 8,
//             volumeUpY + BUTTON_SIZE / 2
//         );
//         // Vertical line
//         g2d.drawLine(
//             volumeUpX + BUTTON_SIZE / 2,
//             volumeUpY + 8,
//             volumeUpX + BUTTON_SIZE / 2,
//             volumeUpY + BUTTON_SIZE - 8
//         );
        
//         // Speaker icon
//         int speakerX = volumeUpX + BUTTON_SIZE + 10;
//         int speakerY = PLAYER_HEIGHT / 2 - 8;
//         g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        
//         // Different speaker icon based on volume
//         if (volumePercent == 0) {
//             g2d.setColor(new Color(150, 150, 150));
//             g2d.drawString("🔇", speakerX, speakerY + 13);  // Muted
//         } else if (volumePercent < 50) {
//             g2d.setColor(TEXT_COLOR);
//             g2d.drawString("🔉", speakerX, speakerY + 13);  // Low
//         } else {
//             g2d.setColor(TEXT_COLOR);
//             g2d.drawString("🔊", speakerX, speakerY + 13);  // High
//         }
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // CREDITS DISPLAY
//     // ═══════════════════════════════════════════════════════════
    
//     private void drawCredits(Graphics2D g2d) {
//         int credY = PLAYER_HEIGHT + 20;
        
//         g2d.setColor(Color.WHITE);
//         g2d.setFont(theFont);
//         g2d.drawImage(credImage, 
//             GamePanel.ORIGINAL_TILE_SIZE, 
//             credY, 
//             GamePanel.ORIGINAL_TILE_SIZE * 2, 
//             GamePanel.ORIGINAL_TILE_SIZE * 2, 
//             null
//         );
        
//         String nbCredits = gp.player.getCredits() + "";
//         g2d.drawString(nbCredits, 50, credY + 31);
//     }
    
//     // ═══════════════════════════════════════════════════════════
//     // MOUSE INTERACTION
//     // ═══════════════════════════════════════════════════════════
    
//     public void updatePlayButtonHover(int mouseX, int mouseY) {
//         playButtonHovered = (mouseX >= playButtonX && mouseX <= playButtonX + BUTTON_SIZE &&
//                             mouseY >= playButtonY && mouseY <= playButtonY + BUTTON_SIZE);
//     }
    
//     public void updateStopButtonHover(int mouseX, int mouseY) {
//         stopButtonHovered = (mouseX >= stopButtonX && mouseX <= stopButtonX + BUTTON_SIZE &&
//                             mouseY >= stopButtonY && mouseY <= stopButtonY + BUTTON_SIZE);
//     }
    
//     // ✨ NEW - Volume button hover
//     public void updateVolumeUpHover(int mouseX, int mouseY) {
//         volumeUpHovered = (mouseX >= volumeUpX && mouseX <= volumeUpX + BUTTON_SIZE &&
//                           mouseY >= volumeUpY && mouseY <= volumeUpY + BUTTON_SIZE);
//     }
    
//     public void updateVolumeDownHover(int mouseX, int mouseY) {
//         volumeDownHovered = (mouseX >= volumeDownX && mouseX <= volumeDownX + BUTTON_SIZE &&
//                             mouseY >= volumeDownY && mouseY <= volumeDownY + BUTTON_SIZE);
//     }
    
//     public boolean isPlayButtonClicked(int mouseX, int mouseY) {
//         return (mouseX >= playButtonX && mouseX <= playButtonX + BUTTON_SIZE &&
//                 mouseY >= playButtonY && mouseY <= playButtonY + BUTTON_SIZE);
//     }
    
//     public boolean isStopButtonClicked(int mouseX, int mouseY) {
//         return (mouseX >= stopButtonX && mouseX <= stopButtonX + BUTTON_SIZE &&
//                 mouseY >= stopButtonY && mouseY <= stopButtonY + BUTTON_SIZE);
//     }
    
//     // ✨ NEW - Volume button clicks
//     public boolean isVolumeUpClicked(int mouseX, int mouseY) {
//         return (mouseX >= volumeUpX && mouseX <= volumeUpX + BUTTON_SIZE &&
//                 mouseY >= volumeUpY && mouseY <= volumeUpY + BUTTON_SIZE);
//     }
    
//     public boolean isVolumeDownClicked(int mouseX, int mouseY) {
//         return (mouseX >= volumeDownX && mouseX <= volumeDownX + BUTTON_SIZE &&
//                 mouseY >= volumeDownY && mouseY <= volumeDownY + BUTTON_SIZE);
//     }
    
//     // ✨ NEW - Volume control methods
//     public void increaseVolume() {
//         currentVolume = Math.min(1.0f, currentVolume + 0.1f);  // +10%
//         if (gp.sound != null) {
//             gp.sound.setVolume(currentVolume);
//         }
//         System.out.println("Volume increased to: " + (int)(currentVolume * 100) + "%");
//     }
    
//     public void decreaseVolume() {
//         currentVolume = Math.max(0.0f, currentVolume - 0.1f);  // -10%
//         if (gp.sound != null) {
//             gp.sound.setVolume(currentVolume);
//         }
//         System.out.println("Volume decreased to: " + (int)(currentVolume * 100) + "%");
//     }
    
//     public static int getMusicPlayerHeight() {
//         return PLAYER_HEIGHT;
//     }
// }

package ui;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import main.GamePanel;
import ui.hud.MusicPlayer;
import ui.hud.CreditsDisplay;

/**
 * UI - Main coordinator for all HUD components
 * 
 * This class doesn't DO much - it just manages and delegates to components.
 * Each component handles its own rendering and input.
 */
public class UI {
    
    private GamePanel gp;
    private List<UIComponent> components;
    
    // Direct references for special access
    private MusicPlayer musicPlayer;
    
    public UI(GamePanel gp) {
        this.gp = gp;
        this.components = new ArrayList<>();
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        // Music player at top
        musicPlayer = new MusicPlayer(gp);
        musicPlayer.setSongInfo("Arranca ft. Omega", "Becky G");
        components.add(musicPlayer);
        
        // Credits below music player
        components.add(new CreditsDisplay(gp, musicPlayer.getHeight() + 20));
        
        System.out.println("[UI] Initialized " + components.size() + " components");
    }
    
    // ═══════════════════════════════════════════════════════════
    // MAIN METHODS (delegate to components)
    // ═══════════════════════════════════════════════════════════
    
    public void draw(Graphics2D g2d) {
        for (UIComponent component : components) {
            component.draw(g2d);
        }
    }
    
    public void handleMouseMove(int mouseX, int mouseY) {
        for (UIComponent component : components) {
            component.handleMouseMove(mouseX, mouseY);
        }
    }
    
    public boolean handleClick(int mouseX, int mouseY) {
        for (UIComponent component : components) {
            if (component.handleClick(mouseX, mouseY)) {
                return true;  // Click was consumed
            }
        }
        return false;
    }
    
    // ═══════════════════════════════════════════════════════════
    // COMPONENT ACCESS (for special cases)
    // ═══════════════════════════════════════════════════════════
    
    public MusicPlayer getMusicPlayer() {
        return musicPlayer;
    }
    
    public static int getMusicPlayerHeight() {
        return 50;  // Could make this dynamic
    }
}