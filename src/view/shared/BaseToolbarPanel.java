package view.shared;

import main.GamePanel;
import java.awt.*;

/**
 * Abstract base class for all toolbar panels (Friends, Inventory, Rooms).
 * Handles common window behavior: visibility, dragging, close button, rendering.
 * 
 * This follows the MVC pattern - Panel is the View coordinator.
 * 
 * USAGE:
 * 1. Extend this class
 * 2. Implement abstract methods (createLayout, createRenderer, etc.)
 * 3. Override hooks as needed (onOpen, onClose, onItemClick)
 * 
 * The base class handles:
 * - Window visibility toggle
 * - Window dragging (by header)
 * - Close button hover/click
 * - Mouse input routing
 * - Basic rendering pipeline
 */
public abstract class BaseToolbarPanel {
    
    // ═══════════════════════════════════════════════════════════
    // CORE COMPONENTS
    // ═══════════════════════════════════════════════════════════
    
    protected final GamePanel gp;
    protected BasePanelLayout layout;
    protected PanelRenderer renderer;
    
    // ═══════════════════════════════════════════════════════════
    // WINDOW STATE
    // ═══════════════════════════════════════════════════════════
    
    protected boolean visible;
    protected boolean isDragging;
    protected int dragOffsetX;
    protected int dragOffsetY;
    protected boolean hoverCloseButton;
    
    // ═══════════════════════════════════════════════════════════
    // SCROLL STATE (for panels with scrollable lists)
    // ═══════════════════════════════════════════════════════════
    
    protected int scrollOffset;
    protected static final int SCROLL_SPEED = 25;
    
    // ═══════════════════════════════════════════════════════════
    // ABSTRACT METHODS (Subclasses MUST implement)
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Create and return the layout instance for this panel.
     * Called once during construction.
     */
    protected abstract BasePanelLayout createLayout();
    
    /**
     * Create and return the renderer instance for this panel.
     * Called once during construction.
     */
    protected abstract PanelRenderer createRenderer();
    
    /**
     * Handle a click within the panel content area.
     * Called after close button and header drag checks.
     * 
     * @param mouseX Mouse X coordinate
     * @param mouseY Mouse Y coordinate
     * @return true if click was handled
     */
    protected abstract boolean handleContentClick(int mouseX, int mouseY);
    
    /**
     * Handle mouse movement within the panel.
     * Used for hover effects on items.
     * 
     * @param mouseX Mouse X coordinate
     * @param mouseY Mouse Y coordinate
     */
    protected abstract void handleContentMouseMove(int mouseX, int mouseY);
    
    /**
     * Get the total number of items (for scroll calculations).
     * Return 0 if panel doesn't have a scrollable list.
     */
    protected abstract int getItemCount();
    
    // ═══════════════════════════════════════════════════════════
    // LIFECYCLE HOOKS (Optional overrides)
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Called when panel is opened (becomes visible).
     * Override to reset state, refresh data, etc.
     */
    protected void onOpen() {
        scrollOffset = 0;
    }
    
    /**
     * Called when panel is closed (becomes hidden).
     * Override to cleanup, save state, etc.
     */
    protected void onClose() {
        // Default: do nothing
    }
    
    /**
     * Called every frame for panels that need updates.
     * Override for animations, auto-refresh, etc.
     */
    public void update() {
        // Default: do nothing
    }
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public BaseToolbarPanel(GamePanel gp) {
        this.gp = gp;
        this.visible = false;
        this.isDragging = false;
        this.scrollOffset = 0;
        
        // Create layout and renderer (subclass implementations)
        this.layout = createLayout();
        this.renderer = createRenderer();
        
        // Center on screen
        layout.centerOnScreen(gp.screenWidth, gp.screenHeight);
    }
    
    // ═══════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Toggle panel visibility
     */
    public void toggle() {
        if (visible) {
            close();
        } else {
            open();
        }
    }
    
    /**
     * Open the panel
     */
    public void open() {
        if (!visible) {
            visible = true;
            isDragging = false;
            layout.centerOnScreen(gp.screenWidth, gp.screenHeight);
            onOpen();
        }
    }
    
    /**
     * Close the panel
     */
    public void close() {
        if (visible) {
            visible = false;
            isDragging = false;
            onClose();
        }
    }
    
    /**
     * Check if panel is currently visible
     */
    public boolean isVisible() {
        return visible;
    }
    
    /**
     * Check if panel is currently being dragged
     */
    public boolean isDragging() {
        return isDragging;
    }
    
    /**
     * Check if a point is within this panel's bounds
     */
    public boolean containsPoint(int mouseX, int mouseY) {
        return visible && layout.containsPoint(mouseX, mouseY);
    }
    
    /**
     * Refresh the panel (re-fetch data, update display)
     * Subclasses should override to implement refresh logic.
     */
    public void refresh() {
        // Recalculate scroll bounds
        int maxScroll = layout.getMaxScrollOffset(getItemCount());
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // INPUT HANDLING
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Handle mouse movement
     */
    public void handleMouseMove(int mouseX, int mouseY) {
        if (!visible) return;
        
        // Check close button hover
        hoverCloseButton = layout.getCloseButtonBounds().contains(mouseX, mouseY);
        
        // Delegate to subclass for content hover effects
        if (layout.containsPoint(mouseX, mouseY)) {
            handleContentMouseMove(mouseX, mouseY);
        }
    }
    
    /**
     * Handle mouse press (start drag or delegate to content)
     */
    public void handleMousePressed(int mouseX, int mouseY) {
        if (!visible) return;
        
        // Check if starting a drag on the header
        if (layout.isOnHeader(mouseX, mouseY)) {
            isDragging = true;
            dragOffsetX = mouseX - layout.getWindowX();
            dragOffsetY = mouseY - layout.getWindowY();
        }
    }
    
    /**
     * Handle mouse drag
     */
    public void handleMouseDragged(int mouseX, int mouseY) {
        if (!visible || !isDragging) return;
        
        // Update window position
        int newX = mouseX - dragOffsetX;
        int newY = mouseY - dragOffsetY;
        layout.setPosition(newX, newY);
        layout.constrainToScreen(gp.screenWidth, gp.screenHeight);
    }
    
    /**
     * Handle mouse release (stop dragging)
     */
    public void handleMouseReleased() {
        isDragging = false;
    }
    
    /**
     * Handle mouse click
     * @return true if click was handled
     */
    public boolean handleClick(int mouseX, int mouseY) {
        if (!visible) return false;
        if (isDragging) return false;
        
        // Close button
        if (hoverCloseButton) {
            close();
            return true;
        }
        
        // Check if click is in window
        if (!layout.containsPoint(mouseX, mouseY)) {
            return false;
        }
        
        // Don't process clicks on header (that's for dragging)
        if (layout.isOnHeader(mouseX, mouseY)) {
            return true; // Consume but don't handle
        }
        
        // Delegate to subclass for content clicks
        return handleContentClick(mouseX, mouseY);
    }
    
    /**
     * Handle mouse scroll
     */
    public void handleScroll(int scrollAmount) {
        if (!visible) return;
        
        int maxScroll = layout.getMaxScrollOffset(getItemCount());
        scrollOffset = Math.max(0, Math.min(scrollOffset + scrollAmount * SCROLL_SPEED, maxScroll));
    }
    
    // ═══════════════════════════════════════════════════════════
    // RENDERING
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Draw the panel
     */
    public void draw(Graphics2D g2d) {
        if (!visible) return;
        
        // Enable antialiasing for smooth rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Delegate to renderer
        renderer.render(g2d, layout, hoverCloseButton);
    }
    
    // ═══════════════════════════════════════════════════════════
    // ACCESSORS (for subclasses and renderers)
    // ═══════════════════════════════════════════════════════════
    
    public BasePanelLayout getLayout() {
        return layout;
    }
    
    public int getScrollOffset() {
        return scrollOffset;
    }
    
    public GamePanel getGamePanel() {
        return gp;
    }
}