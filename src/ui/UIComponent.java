package ui;

import java.awt.Graphics2D;

/**
 * Interface for all UI components (HUD elements, panels, etc.)
 * Allows UI.java to manage components uniformly
 */
public interface UIComponent {
    
    /** Draw the component */
    void draw(Graphics2D g2d);
    
    /** Update hover states */
    void handleMouseMove(int mouseX, int mouseY);
    
    /** Handle click - return true if click was consumed */
    boolean handleClick(int mouseX, int mouseY);
    
    /** Check if point is inside this component */
    boolean containsPoint(int x, int y);
    
    /** Get component height (for layout calculations) */
    int getHeight();
}