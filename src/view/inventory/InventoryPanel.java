package view.inventory;

import view.shared.BaseToolbarPanel;
import view.shared.BasePanelLayout;
import view.shared.PanelRenderer;
import ui.inventory.FurnitureList;
import ui.inventory.PlacementMode;
import object.Furniture;
import main.GamePanel;

import java.awt.*;
import java.util.List;

/**
 * InventoryPanel - Main coordinator for Inventory UI
 * 
 * Part of MVC Architecture:
 * - Extends BaseToolbarPanel for shared window behavior
 * - Coordinates between FurnitureList (data) and InventoryPanelRenderer (view)
 * - Handles user input and delegates to appropriate handlers
 * 
 * Features:
 * - Scrollable furniture list (left panel)
 * - Preview panel with details (right panel)
 * - Place button to enter placement mode
 * - Integration with PlacementMode for furniture placement
 */
public class InventoryPanel extends BaseToolbarPanel {
    
    private InventoryPanelLayout inventoryLayout;  // Typed reference
    private InventoryPanelRenderer inventoryRenderer;  // Typed reference
    
    // Data management
    private final FurnitureList furnitureList;
    private final PlacementMode placementMode;
    
    // UI State
    private Furniture hoveredFurniture;
    private boolean hoverPlaceButton;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public InventoryPanel(GamePanel gp) {
        super(gp);  // This calls createLayout() and createRenderer()
        
        // Initialize data management
        this.furnitureList = new FurnitureList();
        this.placementMode = new PlacementMode(gp);
        
        // Store typed references (layout and renderer were created by super())
        this.inventoryLayout = (InventoryPanelLayout) layout;
        this.inventoryRenderer = (InventoryPanelRenderer) renderer;
        
        System.out.println("[INVENTORY PANEL] Initialized with " + furnitureList.size() + " items");
    }
    
    // ═══════════════════════════════════════════════════════════
    // ABSTRACT METHOD IMPLEMENTATIONS
    // ═══════════════════════════════════════════════════════════
    
    @Override
    protected BasePanelLayout createLayout() {
        return new InventoryPanelLayout(0, 0);
    }
    
    @Override
    protected PanelRenderer createRenderer() {
        return new InventoryPanelRenderer();
    }
    
    @Override
    protected int getItemCount() {
        return furnitureList.size();
    }
    
    // ═══════════════════════════════════════════════════════════
    // LIFECYCLE HOOKS
    // ═══════════════════════════════════════════════════════════
    
    @Override
    protected void onOpen() {
        super.onOpen();
        hoveredFurniture = null;
        hoverPlaceButton = false;
    }
    
    @Override
    protected void onClose() {
        super.onClose();
        // Don't exit placement mode here - it should persist after closing window
    }
    
    // ═══════════════════════════════════════════════════════════
    // DRAWING
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public void draw(Graphics2D g2d) {
        if (!visible) return;
        
        // Update renderer state before drawing
        inventoryRenderer.setFurnitureItems(furnitureList.getAll());
        inventoryRenderer.setSelectedFurniture(furnitureList.getSelected());
        inventoryRenderer.setHoveredFurniture(hoveredFurniture);
        inventoryRenderer.setScrollOffset(scrollOffset);
        inventoryRenderer.setHoverPlaceButton(hoverPlaceButton);
        
        // Call base class draw (which calls renderer.render())
        super.draw(g2d);
    }
    
    /**
     * Draw placement preview (called separately from main draw)
     * This is drawn on the game world, not on the inventory window
     */
    public void drawPlacementPreview(Graphics2D g2d) {
        placementMode.drawPreview(g2d);
    }
    
    // ═══════════════════════════════════════════════════════════
    // INPUT HANDLING - MOUSE MOVE
    // ═══════════════════════════════════════════════════════════
    
    @Override
    protected void handleContentMouseMove(int mouseX, int mouseY) {
        // Check place button hover
        if (furnitureList.hasSelection()) {
            Rectangle placeButtonBounds = inventoryLayout.getPlaceButtonBounds();
            hoverPlaceButton = placeButtonBounds.contains(mouseX, mouseY);
        } else {
            hoverPlaceButton = false;
        }
        
        // Check furniture list hover
        hoveredFurniture = null;
        Rectangle listBounds = inventoryLayout.getListBounds();
        if (listBounds.contains(mouseX, mouseY)) {
            int index = inventoryLayout.getFurnitureIndexAtY(mouseY, scrollOffset);
            List<Furniture> items = furnitureList.getAll();
            
            if (index >= 0 && index < items.size()) {
                hoveredFurniture = items.get(index);
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // INPUT HANDLING - CLICKS
    // ═══════════════════════════════════════════════════════════
    
    @Override
    protected boolean handleContentClick(int mouseX, int mouseY) {
        // Check furniture list click
        Rectangle listBounds = inventoryLayout.getListBounds();
        if (listBounds.contains(mouseX, mouseY)) {
            int index = inventoryLayout.getFurnitureIndexAtY(mouseY, scrollOffset);
            if (index >= 0 && index < furnitureList.size()) {
                furnitureList.selectFurniture(index);
                System.out.println("[INVENTORY] Selected: " + furnitureList.getSelected().name);
                return true;
            }
        }
        
        // Check place button click
        if (furnitureList.hasSelection()) {
            Rectangle placeButtonBounds = inventoryLayout.getPlaceButtonBounds();
            if (placeButtonBounds.contains(mouseX, mouseY)) {
                enterPlacementMode();
                return true;
            }
        }
        
        return false;
    }
    
    // ═══════════════════════════════════════════════════════════
    // SCROLL HANDLING
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public void handleScroll(int rotation) {
        int scrollAmount = rotation * 30;
        int maxScroll = inventoryLayout.getListMaxScrollOffset(getItemCount());
        
        scrollOffset = Math.max(0, Math.min(scrollOffset + scrollAmount, maxScroll));
    }
    
    // ═══════════════════════════════════════════════════════════
    // PLACEMENT MODE
    // ═══════════════════════════════════════════════════════════
    
    private void enterPlacementMode() {
        Furniture selected = furnitureList.getSelected();
        if (selected != null) {
            placementMode.enter(selected);
            visible = false;  // Close inventory window
            System.out.println("[INVENTORY] Entering placement mode for: " + selected.name);
        }
    }
    
    /**
     * Update placement preview position (call from game loop)
     */
    public void updatePlacementPreview(int mouseX, int mouseY) {
        placementMode.updatePreview(mouseX, mouseY);
    }
    
    /**
     * Confirm furniture placement (call on click during placement mode)
     */
    public void confirmPlacement() {
        placementMode.confirmPlacement();
    }
    
    /**
     * Cancel placement mode (call on right-click or ESC)
     */
    public void cancelPlacement() {
        placementMode.cancel();
    }
    
    /**
     * Check if currently in placement mode
     */
    public boolean isPlacementMode() {
        return placementMode.isActive();
    }
    
    // ═══════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public void refresh() {
        super.refresh();
        hoveredFurniture = null;
    }
    
    /**
     * Get the furniture list (for external access if needed)
     */
    public FurnitureList getFurnitureList() {
        return furnitureList;
    }
    
    /**
     * Check if any furniture is selected
     */
    public boolean hasSelection() {
        return furnitureList.hasSelection();
    }
    
    /**
     * Get selected furniture
     */
    public Furniture getSelectedFurniture() {
        return furnitureList.getSelected();
    }
    
    /**
     * Clear the current selection
     */
    public void clearSelection() {
        furnitureList.clearSelection();
    }
    
    // ═══════════════════════════════════════════════════════════
    // CLEANUP
    // ═══════════════════════════════════════════════════════════
    
    public void shutdown() {
        placementMode.cancel();
        System.out.println("[INVENTORY PANEL] Shutdown");
    }
}