package ui.hud;

import java.awt.*;
import main.GamePanel;
import ui.UIComponent;

/**
 * MusicPlayer - Self-contained music player HUD component
 * 
 * Handles:
 * - Play/Pause/Stop controls
 * - Volume up/down
 * - Song info display
 */
public class MusicPlayer implements UIComponent {
    
    private GamePanel gp;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════
    
    private static final int HEIGHT = 50;
    private static final int PADDING = 10;
    private static final int BUTTON_SIZE = 30;
    
    // Colors
    private static final Color BG_COLOR = new Color(30, 30, 30, 230);
    private static final Color BUTTON_BG = new Color(60, 60, 60);
    private static final Color BUTTON_HOVER = new Color(80, 80, 80);
    private static final Color BUTTON_ACTIVE = new Color(0, 200, 100);
    private static final Color TEXT_COLOR = new Color(255, 255, 255);
    private static final Color SONG_COLOR = new Color(0, 200, 255);
    
    // ═══════════════════════════════════════════════════════════
    // STATE
    // ═══════════════════════════════════════════════════════════
    
    // Button positions (calculated on draw)
    private Rectangle playButton = new Rectangle();
    private Rectangle stopButton = new Rectangle();
    private Rectangle volumeUpButton = new Rectangle();
    private Rectangle volumeDownButton = new Rectangle();
    
    // Hover states
    private boolean playHovered = false;
    private boolean stopHovered = false;
    private boolean volumeUpHovered = false;
    private boolean volumeDownHovered = false;
    
    // Volume
    private float volume = 1.0f;
    
    // Song info
    private String songTitle = "Unknown";
    private String artist = "Unknown";
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public MusicPlayer(GamePanel gp) {
        this.gp = gp;
    }
    
    // ═══════════════════════════════════════════════════════════
    // UIComponent INTERFACE
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public void draw(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawBackground(g2d);
        calculateButtonPositions();
        drawPlayButton(g2d);
        drawStopButton(g2d);
        drawSongInfo(g2d);
        drawVolumeControls(g2d);
    }
    
    @Override
    public void handleMouseMove(int mouseX, int mouseY) {
        playHovered = playButton.contains(mouseX, mouseY);
        stopHovered = stopButton.contains(mouseX, mouseY);
        volumeUpHovered = volumeUpButton.contains(mouseX, mouseY);
        volumeDownHovered = volumeDownButton.contains(mouseX, mouseY);
    }
    
    @Override
    public boolean handleClick(int mouseX, int mouseY) {
        if (playButton.contains(mouseX, mouseY)) {
            togglePlayPause();
            return true;
        }
        
        if (stopButton.contains(mouseX, mouseY)) {
            stop();
            return true;
        }
        
        if (volumeUpButton.contains(mouseX, mouseY)) {
            increaseVolume();
            return true;
        }
        
        if (volumeDownButton.contains(mouseX, mouseY)) {
            decreaseVolume();
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean containsPoint(int x, int y) {
        return y >= 0 && y <= HEIGHT;
    }
    
    @Override
    public int getHeight() {
        return HEIGHT;
    }
    
    // ═══════════════════════════════════════════════════════════
    // MUSIC CONTROL API
    // ═══════════════════════════════════════════════════════════
    
    public void togglePlayPause() {
        if (gp.sound != null) {
            gp.sound.togglePlayPause();
        }
    }
    
    public void stop() {
        if (gp.sound != null) {
            gp.sound.stop();
        }
    }
    
    public void increaseVolume() {
        volume = Math.min(1.0f, volume + 0.1f);
        applyVolume();
        System.out.println("[MUSIC] Volume: " + getVolumePercent() + "%");
    }
    
    public void decreaseVolume() {
        volume = Math.max(0.0f, volume - 0.1f);
        applyVolume();
        System.out.println("[MUSIC] Volume: " + getVolumePercent() + "%");
    }
    
    public void setVolume(float vol) {
        volume = Math.max(0.0f, Math.min(1.0f, vol));
        applyVolume();
    }
    
    public int getVolumePercent() {
        return (int) (volume * 100);
    }
    
    public void setSongInfo(String title, String artist) {
        this.songTitle = title;
        this.artist = artist;
    }
    
    private void applyVolume() {
        if (gp.sound != null) {
            gp.sound.setVolume(volume);
        }
    }
    
    private boolean isPlaying() {
        return gp.sound != null && gp.sound.isPlaying();
    }
    
    // ═══════════════════════════════════════════════════════════
    // PRIVATE RENDERING METHODS
    // ═══════════════════════════════════════════════════════════
    
    private void drawBackground(Graphics2D g2d) {
        g2d.setColor(BG_COLOR);
        g2d.fillRect(0, 0, gp.screenWidth, HEIGHT);
        
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(0, HEIGHT - 1, gp.screenWidth, HEIGHT - 1);
    }
    
    private void calculateButtonPositions() {
        int buttonY = (HEIGHT - BUTTON_SIZE) / 2;
        
        playButton.setBounds(PADDING, buttonY, BUTTON_SIZE, BUTTON_SIZE);
        stopButton.setBounds(PADDING + BUTTON_SIZE + 10, buttonY, BUTTON_SIZE, BUTTON_SIZE);
        
        int volumeX = gp.screenWidth - 150;
        volumeDownButton.setBounds(volumeX, buttonY, BUTTON_SIZE, BUTTON_SIZE);
        volumeUpButton.setBounds(volumeX + BUTTON_SIZE + 60, buttonY, BUTTON_SIZE, BUTTON_SIZE);
    }
    
    private void drawPlayButton(Graphics2D g2d) {
        // Background
        if (isPlaying()) {
            g2d.setColor(BUTTON_ACTIVE);
        } else if (playHovered) {
            g2d.setColor(BUTTON_HOVER);
        } else {
            g2d.setColor(BUTTON_BG);
        }
        g2d.fillRoundRect(playButton.x, playButton.y, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
        // Border
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawRoundRect(playButton.x, playButton.y, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
        // Icon
        g2d.setColor(Color.WHITE);
        int cx = playButton.x + BUTTON_SIZE / 2;
        int cy = playButton.y + BUTTON_SIZE / 2;
        
        if (isPlaying()) {
            // Pause icon (two bars)
            g2d.fillRect(cx - 6, cy - 6, 4, 12);
            g2d.fillRect(cx + 2, cy - 6, 4, 12);
        } else {
            // Play icon (triangle)
            int[] xPoints = {cx - 4, cx - 4, cx + 6};
            int[] yPoints = {cy - 6, cy + 6, cy};
            g2d.fillPolygon(xPoints, yPoints, 3);
        }
    }
    
    private void drawStopButton(Graphics2D g2d) {
        // Background
        g2d.setColor(stopHovered ? BUTTON_HOVER : BUTTON_BG);
        g2d.fillRoundRect(stopButton.x, stopButton.y, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
        // Border
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawRoundRect(stopButton.x, stopButton.y, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
        // Stop icon (square)
        g2d.setColor(Color.WHITE);
        g2d.fillRect(stopButton.x + 9, stopButton.y + 9, 12, 12);
    }
    
    private void drawSongInfo(Graphics2D g2d) {
        int textX = stopButton.x + BUTTON_SIZE + 20;
        
        // Song title
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(SONG_COLOR);
        g2d.drawString("♫ " + artist + " - " + songTitle, textX, HEIGHT / 2 + 5);
        
        // Status
        String status = isPlaying() ? "Now Playing" : "Paused";
        g2d.setColor(isPlaying() ? BUTTON_ACTIVE : new Color(200, 200, 200));
        g2d.setFont(new Font("Arial", Font.ITALIC, 11));
        
        int statusX = textX + g2d.getFontMetrics(new Font("Arial", Font.BOLD, 14))
                              .stringWidth("♫ " + artist + " - " + songTitle) + 15;
        g2d.drawString(status, statusX, HEIGHT / 2 + 5);
    }
    
    private void drawVolumeControls(Graphics2D g2d) {
        // Volume Down (-)
        g2d.setColor(volumeDownHovered ? BUTTON_HOVER : BUTTON_BG);
        g2d.fillRoundRect(volumeDownButton.x, volumeDownButton.y, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawRoundRect(volumeDownButton.x, volumeDownButton.y, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        int cy = volumeDownButton.y + BUTTON_SIZE / 2;
        g2d.drawLine(volumeDownButton.x + 8, cy, volumeDownButton.x + BUTTON_SIZE - 8, cy);
        
        // Volume percentage
        int percent = getVolumePercent();
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        
        if (percent > 70) g2d.setColor(BUTTON_ACTIVE);
        else if (percent > 30) g2d.setColor(new Color(255, 165, 0));
        else g2d.setColor(new Color(255, 100, 100));
        
        String volText = percent + "%";
        int textX = volumeDownButton.x + BUTTON_SIZE + 15;
        g2d.drawString(volText, textX, HEIGHT / 2 + 5);
        
        // Volume Up (+)
        g2d.setColor(volumeUpHovered ? BUTTON_HOVER : BUTTON_BG);
        g2d.fillRoundRect(volumeUpButton.x, volumeUpButton.y, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawRoundRect(volumeUpButton.x, volumeUpButton.y, BUTTON_SIZE, BUTTON_SIZE, 5, 5);
        
        g2d.setColor(Color.WHITE);
        int cx = volumeUpButton.x + BUTTON_SIZE / 2;
        cy = volumeUpButton.y + BUTTON_SIZE / 2;
        g2d.drawLine(volumeUpButton.x + 8, cy, volumeUpButton.x + BUTTON_SIZE - 8, cy);
        g2d.drawLine(cx, volumeUpButton.y + 8, cx, volumeUpButton.y + BUTTON_SIZE - 8);
        
        // Speaker icon
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        String speaker = percent == 0 ? "🔇" : percent < 50 ? "🔉" : "🔊";
        g2d.setColor(percent == 0 ? new Color(150, 150, 150) : TEXT_COLOR);
        g2d.drawString(speaker, volumeUpButton.x + BUTTON_SIZE + 10, HEIGHT / 2 + 6);
    }
}