package ui.inventory;

import main.GamePanel;
import java.awt.*;

/**
 * Main coordinator for the inventory window system
 * Delegates to specialized classes for clean separation of concerns
 * 
 * Responsibilities:
 * - Window state management (visible/hidden)
 * - User input routing
 * - Coordinating between UI, data, and placement subsystems
 */
public class InventoryWindow {
    
    // ═══════════════════════════════════════════════════════════
    // CORE COMPONENTS
    // ═══════════════════════════════════════════════════════════
    
    private final GamePanel gp;
    private final FurnitureList furnitureList;
    private final PlacementMode placementMode;
    
    private InventoryLayout layout;
    private boolean visible;
    
    // ═══════════════════════════════════════════════════════════
    // WINDOW DRAGGING STATE
    // ═══════════════════════════════════════════════════════════
    
    private boolean draggingWindow;
    private int dragOffsetX;
    private int dragOffsetY;
    private boolean hoverCloseButton;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public InventoryWindow(GamePanel gp) {
        this.gp = gp;
        this.furnitureList = new FurnitureList();
        this.placementMode = new PlacementMode(gp);
        this.visible = false;
        
        // Center window on screen
        int windowX = (gp.screenWidth - InventoryLayout.WINDOW_WIDTH) / 2;
        int windowY = (gp.screenHeight - InventoryLayout.WINDOW_HEIGHT) / 2;
        this.layout = new InventoryLayout(windowX, windowY);
    }
    
    // ═══════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════
    
    public void toggle() {
        visible = !visible;
        if (!visible) {
            placementMode.exit();
        }
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public boolean isPlacementMode() {
        return placementMode.isActive();
    }
    
    // ═══════════════════════════════════════════════════════════
    // INPUT HANDLING
    // ═══════════════════════════════════════════════════════════
    
    public void handleMouseMove(int mouseX, int mouseY) {
        if (!visible) return;
        hoverCloseButton = layout.closeButtonBounds.contains(mouseX, mouseY);
    }
    
    public void handleClick(int mouseX, int mouseY) {
        if (!visible) return;
        
        if (handleCloseButtonClick(mouseX, mouseY)) return;
        if (handleHeaderClick(mouseX, mouseY)) return;
        if (handleListClick(mouseX, mouseY)) return;
        if (handlePlaceButtonClick(mouseX, mouseY)) return;
    }
    
    private boolean handleCloseButtonClick(int mouseX, int mouseY) {
        if (layout.closeButtonBounds.contains(mouseX, mouseY)) {
            visible = false;
            placementMode.exit();
            return true;
        }
        return false;
    }
    
    private boolean handleHeaderClick(int mouseX, int mouseY) {
        if (layout.headerBounds.contains(mouseX, mouseY) && 
            !layout.closeButtonBounds.contains(mouseX, mouseY)) {
            draggingWindow = true;
            dragOffsetX = mouseX - layout.getWindowX();
            dragOffsetY = mouseY - layout.getWindowY();
            return true;
        }
        return false;
    }
    
    private boolean handleListClick(int mouseX, int mouseY) {
        if (layout.listBounds.contains(mouseX, mouseY)) {
            int index = (mouseY - layout.listBounds.y) / InventoryLayout.ITEM_HEIGHT;
            if (index >= 0 && index < furnitureList.size()) {
                furnitureList.selectFurniture(index);
                layout.updatePlaceButtonBounds(true);
                return true;
            }
        }
        return false;
    }
    
    private boolean handlePlaceButtonClick(int mouseX, int mouseY) {
        if (furnitureList.hasSelection() && 
            layout.placeButtonBounds != null && 
            layout.placeButtonBounds.contains(mouseX, mouseY)) {
            enterPlacementMode();
            return true;
        }
        return false;
    }
    
    public void handleDrag(int mouseX, int mouseY) {
        if (draggingWindow) {
            int newX = mouseX - dragOffsetX;
            int newY = mouseY - dragOffsetY;
            layout = new InventoryLayout(newX, newY);
            layout.updatePlaceButtonBounds(furnitureList.hasSelection());
        }
    }
    
    public void handleRelease() {
        draggingWindow = false;
    }
    
    // ═══════════════════════════════════════════════════════════
    // PLACEMENT MODE
    // ═══════════════════════════════════════════════════════════
    
    private void enterPlacementMode() {
        placementMode.enter(furnitureList.getSelected());
        visible = false;
    }
    
    public void updatePlacementPreview(int mouseX, int mouseY) {
        placementMode.updatePreview(mouseX, mouseY);
    }
    
    public void confirmPlacement() {
        placementMode.confirmPlacement();
    }
    
    public void cancelPlacement() {
        placementMode.cancel();
    }
    
    // ═══════════════════════════════════════════════════════════
    // RENDERING
    // ═══════════════════════════════════════════════════════════
    
    public void draw(Graphics2D g2d) {
        if (!visible) return;
        
        InventoryWindowUI ui = new InventoryWindowUI(layout, furnitureList, hoverCloseButton);
        ui.draw(g2d);
    }
    
    public void drawPlacementPreview(Graphics2D g2d) {
        placementMode.drawPreview(g2d);
    }
}