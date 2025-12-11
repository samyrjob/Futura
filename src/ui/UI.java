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