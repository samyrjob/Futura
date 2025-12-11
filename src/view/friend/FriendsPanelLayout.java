package view.friend;

import view.shared.BasePanelLayout;
import java.awt.*;

/**
 * Layout configuration for the Friends Panel.
 * Extends BasePanelLayout with friends-specific dimensions and colors.
 * 
 * Part of MVC architecture - this is the View layer (layout).
 */
public class FriendsPanelLayout extends BasePanelLayout {
    
    // ═══════════════════════════════════════════════════════════
    // FRIENDS-SPECIFIC DIMENSIONS
    // ═══════════════════════════════════════════════════════════
    
    public static final int WINDOW_WIDTH = 320;
    public static final int WINDOW_HEIGHT = 420;
    
    // Online status indicator
    public static final int STATUS_INDICATOR_SIZE = 14;
    public static final int STATUS_INDICATOR_MARGIN = 15;
    
    // Friend item layout
    public static final int FRIEND_NAME_X_OFFSET = 40;
    public static final int FRIEND_NAME_Y_OFFSET = 22;
    public static final int FRIEND_STATUS_Y_OFFSET = 40;
    public static final int FRIEND_GENDER_X_OFFSET = 35;
    public static final int FRIEND_GENDER_Y_OFFSET = 32;
    
    // Badge position (in header)
    public static final int BADGE_X_OFFSET = 125;
    public static final int BADGE_Y_OFFSET = 15;
    
    // ═══════════════════════════════════════════════════════════
    // FRIENDS-SPECIFIC COLORS
    // ═══════════════════════════════════════════════════════════
    
    /** Purple theme for friends panel */
    public static final Color HEADER_COLOR = new Color(156, 39, 176);
    
    /** Green for online status */
    public static final Color ONLINE_COLOR = new Color(76, 175, 80);
    
    /** Gray for offline status */
    public static final Color OFFLINE_COLOR = new Color(158, 158, 158);
    
    /** Pink for female gender icon */
    public static final Color FEMALE_COLOR = new Color(233, 30, 99);
    
    /** Blue for male gender icon */
    public static final Color MALE_COLOR = new Color(33, 150, 243);
    
    /** Glow effect for online indicator */
    public static final Color ONLINE_GLOW = new Color(76, 175, 80, 50);
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public FriendsPanelLayout(int windowX, int windowY) {
        super(windowX, windowY);
    }
    
    // ═══════════════════════════════════════════════════════════
    // ABSTRACT METHOD IMPLEMENTATIONS
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public int getWindowWidth() {
        return WINDOW_WIDTH;
    }
    
    @Override
    public int getWindowHeight() {
        return WINDOW_HEIGHT;
    }
    
    @Override
    public Color getHeaderColor() {
        return HEADER_COLOR;
    }
    
    @Override
    public String getTitle() {
        return "Friends";
    }
    
    @Override
    public String getIcon() {
        return "💬";
    }
    
    // ═══════════════════════════════════════════════════════════
    // FRIENDS-SPECIFIC BOUNDS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Get the position for the status indicator circle
     */
    public Point getStatusIndicatorPosition(int itemY) {
        int x = getContentBounds().x + STATUS_INDICATOR_MARGIN;
        int y = itemY + (ITEM_HEIGHT - ITEM_MARGIN) / 2 - STATUS_INDICATOR_SIZE / 2;
        return new Point(x, y);
    }
    
    /**
     * Get the color for a gender icon
     */
    public static Color getGenderColor(String gender) {
        if ("FEMALE".equalsIgnoreCase(gender)) {
            return FEMALE_COLOR;
        }
        return MALE_COLOR;
    }
    
    /**
     * Get the icon for a gender
     */
    public static String getGenderIcon(String gender) {
        if ("FEMALE".equalsIgnoreCase(gender)) {
            return "♀";
        }
        return "♂";
    }
}