package view.shared;

import java.awt.*;

/**
 * Interface defining the rendering contract for all toolbar panels.
 * Each panel's renderer must implement these methods.
 * 
 * This follows the MVC pattern - Renderer is part of the View layer.
 * Separates rendering logic from business logic and input handling.
 */
public interface PanelRenderer {
    
    // ═══════════════════════════════════════════════════════════
    // CORE RENDERING METHODS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Draw the complete panel (called by BaseToolbarPanel.draw())
     * This is the main entry point for rendering.
     * 
     * @param g2d Graphics context
     * @param layout Panel layout for dimensions and positions
     * @param hoverCloseButton Whether close button is being hovered
     */
    void render(Graphics2D g2d, BasePanelLayout layout, boolean hoverCloseButton);
    
    // ═══════════════════════════════════════════════════════════
    // COMPONENT RENDERING (Optional overrides)
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Draw the window background and border
     * Default implementation provided.
     */
    default void drawWindowBackground(Graphics2D g2d, BasePanelLayout layout) {
        int x = layout.getWindowX();
        int y = layout.getWindowY();
        int width = layout.getWindowWidth();
        int height = layout.getWindowHeight();
        
        // Shadow
        g2d.setColor(BasePanelLayout.SHADOW_COLOR);
        g2d.fillRoundRect(
            x + BasePanelLayout.SHADOW_OFFSET, 
            y + BasePanelLayout.SHADOW_OFFSET, 
            width, height,
            BasePanelLayout.BORDER_RADIUS, 
            BasePanelLayout.BORDER_RADIUS
        );
        
        // Background
        g2d.setColor(BasePanelLayout.WINDOW_BG);
        g2d.fillRoundRect(x, y, width, height,
            BasePanelLayout.BORDER_RADIUS, 
            BasePanelLayout.BORDER_RADIUS
        );
        
        // Border
        g2d.setColor(layout.getHeaderColor());
        g2d.setStroke(new BasicStroke(BasePanelLayout.BORDER_WIDTH));
        g2d.drawRoundRect(x, y, width, height,
            BasePanelLayout.BORDER_RADIUS, 
            BasePanelLayout.BORDER_RADIUS
        );
    }
    
    /**
     * Draw the header bar with title and icon
     * Default implementation provided.
     */
    default void drawHeader(Graphics2D g2d, BasePanelLayout layout) {
        int x = layout.getWindowX();
        int y = layout.getWindowY();
        int width = layout.getWindowWidth();
        
        // Header background (rounded top, flat bottom)
        g2d.setColor(layout.getHeaderColor());
        g2d.fillRoundRect(x, y, width, BasePanelLayout.HEADER_HEIGHT,
            BasePanelLayout.BORDER_RADIUS, 
            BasePanelLayout.BORDER_RADIUS
        );
        g2d.fillRect(x, y + 25, width, 20); // Fill bottom corners
        
        // Icon
        g2d.setFont(BasePanelLayout.ICON_FONT);
        g2d.setColor(Color.WHITE);
        g2d.drawString(layout.getIcon(), x + 15, y + 30);
        
        // Title
        g2d.setFont(BasePanelLayout.HEADER_FONT);
        g2d.drawString(layout.getTitle(), x + 50, y + 30);
    }
    
    /**
     * Draw the close button
     * Default implementation provided.
     */
    default void drawCloseButton(Graphics2D g2d, BasePanelLayout layout, boolean hovered) {
        Rectangle bounds = layout.getCloseButtonBounds();
        
        // Button circle
        g2d.setColor(hovered ? BasePanelLayout.CLOSE_BUTTON_HOVER : BasePanelLayout.CLOSE_BUTTON_NORMAL);
        g2d.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
        
        // X icon
        g2d.setColor(hovered ? Color.WHITE : layout.getHeaderColor());
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int padding = 7;
        g2d.drawLine(
            bounds.x + padding, bounds.y + padding,
            bounds.x + bounds.width - padding, bounds.y + bounds.height - padding
        );
        g2d.drawLine(
            bounds.x + bounds.width - padding, bounds.y + padding,
            bounds.x + padding, bounds.y + bounds.height - padding
        );
    }
    
    /**
     * Draw a scrollbar for scrollable content
     * Default implementation provided.
     */
    default void drawScrollbar(Graphics2D g2d, BasePanelLayout layout, 
                               int scrollOffset, int totalItems) {
        Rectangle content = layout.getContentBounds();
        int contentHeight = totalItems * BasePanelLayout.ITEM_HEIGHT;
        
        // Only draw if content exceeds view
        if (contentHeight <= content.height) return;
        
        int scrollbarX = content.x + content.width - BasePanelLayout.SCROLLBAR_WIDTH;
        int scrollbarY = content.y;
        int scrollbarHeight = content.height;
        
        // Track
        g2d.setColor(BasePanelLayout.SCROLLBAR_TRACK);
        g2d.fillRoundRect(scrollbarX, scrollbarY, 
            BasePanelLayout.SCROLLBAR_WIDTH, scrollbarHeight, 3, 3);
        
        // Thumb
        float visibleRatio = (float) content.height / contentHeight;
        int thumbHeight = Math.max(
            BasePanelLayout.SCROLLBAR_MIN_THUMB, 
            (int) (scrollbarHeight * visibleRatio)
        );
        int maxScroll = layout.getMaxScrollOffset(totalItems);
        float scrollRatio = maxScroll > 0 ? (float) scrollOffset / maxScroll : 0;
        int thumbY = scrollbarY + (int) ((scrollbarHeight - thumbHeight) * scrollRatio);
        
        g2d.setColor(layout.getHeaderColor().brighter());
        g2d.fillRoundRect(scrollbarX, thumbY, 
            BasePanelLayout.SCROLLBAR_WIDTH, thumbHeight, 3, 3);
    }
    
    /**
     * Draw empty state when list has no items
     * Default implementation provided.
     */
    default void drawEmptyState(Graphics2D g2d, BasePanelLayout layout, 
                                String emoji, String message, String hint) {
        Rectangle content = layout.getContentBounds();
        int centerX = content.x + content.width / 2;
        int centerY = content.y + content.height / 2;
        
        // Emoji
        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        FontMetrics fm = g2d.getFontMetrics();
        int emojiX = centerX - fm.stringWidth(emoji) / 2;
        g2d.drawString(emoji, emojiX, centerY - 20);
        
        // Message
        g2d.setColor(new Color(120, 120, 120));
        g2d.setFont(BasePanelLayout.ITEM_NAME_FONT);
        fm = g2d.getFontMetrics();
        int messageX = centerX - fm.stringWidth(message) / 2;
        g2d.drawString(message, messageX, centerY + 25);
        
        // Hint
        if (hint != null && !hint.isEmpty()) {
            g2d.setFont(BasePanelLayout.ITEM_SUBTITLE_FONT);
            fm = g2d.getFontMetrics();
            int hintX = centerX - fm.stringWidth(hint) / 2;
            g2d.drawString(hint, hintX, centerY + 50);
        }
    }
    
    /**
     * Draw a standard list item with highlight states
     * Utility method for consistent item rendering.
     */
    default void drawItemBackground(Graphics2D g2d, int x, int y, int width,
                                   boolean hovered, boolean selected, Color accentColor) {
        Color bgColor;
        if (selected) {
            bgColor = BasePanelLayout.ITEM_SELECTED;
        } else if (hovered) {
            bgColor = BasePanelLayout.ITEM_HOVER;
        } else {
            bgColor = BasePanelLayout.ITEM_BG;
        }
        
        g2d.setColor(bgColor);
        g2d.fillRoundRect(x, y, width, 
            BasePanelLayout.ITEM_HEIGHT - BasePanelLayout.ITEM_MARGIN,
            BasePanelLayout.ITEM_BORDER_RADIUS, 
            BasePanelLayout.ITEM_BORDER_RADIUS
        );
        
        // Border on hover/select
        if (hovered || selected) {
            g2d.setColor(accentColor.brighter());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, width,
                BasePanelLayout.ITEM_HEIGHT - BasePanelLayout.ITEM_MARGIN,
                BasePanelLayout.ITEM_BORDER_RADIUS,
                BasePanelLayout.ITEM_BORDER_RADIUS
            );
        }
    }
    
    /**
     * Draw a badge (count indicator)
     * Utility method for showing counts in headers.
     */
    default void drawBadge(Graphics2D g2d, int x, int y, int count, Color accentColor) {
        String countText = String.valueOf(count);
        g2d.setFont(BasePanelLayout.BADGE_FONT);
        FontMetrics fm = g2d.getFontMetrics();
        int badgeWidth = Math.max(22, fm.stringWidth(countText) + 12);
        
        // Badge background
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRoundRect(x, y, badgeWidth, 20, 10, 10);
        
        // Badge text
        g2d.setColor(accentColor);
        g2d.drawString(countText, x + (badgeWidth - fm.stringWidth(countText)) / 2, y + 15);
    }
    
    /**
     * Draw a standard action button
     * Utility method for consistent button rendering.
     */
    default void drawButton(Graphics2D g2d, int x, int y, int width, int height,
                           String text, Color bgColor, boolean hovered) {
        // Background
        g2d.setColor(hovered ? bgColor.darker() : bgColor);
        g2d.fillRoundRect(x, y, width, height, 10, 10);
        
        // Text
        g2d.setColor(Color.WHITE);
        g2d.setFont(BasePanelLayout.BUTTON_FONT);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }
}