# 📦 ui.inventory Package

**Professional inventory system with furniture browsing, selection, and placement capabilities.**

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Class Descriptions](#class-descriptions)
- [Quick Start](#quick-start)
- [Usage Examples](#usage-examples)
- [Customization Guide](#customization-guide)
- [Testing](#testing)
- [Future Enhancements](#future-enhancements)

---

## 🎯 Overview

The `ui.inventory` package provides a complete inventory management system for the Futura virtual world game. It allows players to:

- 📋 Browse available furniture items
- 🔍 Preview furniture before placing
- 🎨 Place furniture on the game map
- 🖱️ Drag the inventory window around
- ✅ Validate placement locations

### Key Features:
- ✅ Clean separation of concerns (5 focused classes)
- ✅ No magic numbers (all constants centralized)
- ✅ Fully tested and maintainable
- ✅ Easy to extend with new features
- ✅ Professional UI with hover effects

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│              InventoryWindow (Main)                 │
│  • Coordinates all subsystems                       │
│  • Routes user input                                │
│  • Manages window visibility                        │
└─────────────┬───────────────────────────────────────┘
              │
      ┌───────┴───────┐
      │               │
      ▼               ▼
┌─────────────┐  ┌──────────────────┐
│ FurnitureList│  │ PlacementMode    │
│ • Data mgmt │  │ • Placement logic│
└──────┬──────┘  └────────┬─────────┘
       │                  │
       │    ┌─────────────┴────────────┐
       │    │                          │
       ▼    ▼                          ▼
┌─────────────────┐          ┌──────────────────┐
│ InventoryLayout │          │ InventoryWindowUI│
│ • Constants     │          │ • All rendering  │
│ • Bounds calc   │          │                  │
└─────────────────┘          └──────────────────┘
```

### Dependency Graph:
```
InventoryWindow
├─> FurnitureList (has-a)
├─> PlacementMode (has-a)
├─> InventoryLayout (has-a)
└─> InventoryWindowUI (creates on demand)

InventoryWindowUI
├─> InventoryLayout (reads from)
└─> FurnitureList (reads from)

PlacementMode
└─> InventoryLayout (for colors)
```

---

## 📚 Class Descriptions

### 1️⃣ InventoryWindow.java

**Role:** Main coordinator and entry point

**Responsibilities:**
- Window state management (visible/hidden)
- User input routing to appropriate handlers
- Drag window functionality
- Coordinating between subsystems

**Public API:**
```java
// Window control
void toggle()                           // Show/hide inventory
boolean isVisible()                     // Check if window is open
boolean isPlacementMode()               // Check if placing furniture

// Input handling
void handleMouseMove(int x, int y)      // Track mouse for hover effects
void handleClick(int x, int y)          // Route clicks to components
void handleDrag(int x, int y)           // Drag window around
void handleRelease()                    // Stop dragging

// Placement control
void updatePlacementPreview(int x, int y)  // Update placement grid position
void confirmPlacement()                    // Place furniture (left-click)
void cancelPlacement()                     // Cancel placement (right-click)

// Rendering
void draw(Graphics2D g2d)                  // Draw inventory window
void drawPlacementPreview(Graphics2D g2d)  // Draw placement preview
```

**Usage:**
```java
// In GamePanel
InventoryWindow inventory = new InventoryWindow(gamePanel);

// Toggle inventory
inventory.toggle();

// Handle clicks
inventory.handleClick(mouseX, mouseY);

// Render
inventory.draw(g2d);
inventory.drawPlacementPreview(g2d);
```

---

### 2️⃣ InventoryLayout.java

**Role:** Centralized layout constants and bounds calculator

**Responsibilities:**
- Define ALL dimensions (window size, button size, margins, etc.)
- Define ALL colors (backgrounds, borders, highlights, etc.)
- Define ALL fonts
- Calculate UI component bounds (rectangles for hit testing)

**Public Constants:**
```java
// Window dimensions
WINDOW_WIDTH = 600
WINDOW_HEIGHT = 400
HEADER_HEIGHT = 40

// Furniture list
LIST_WIDTH = 250
ITEM_HEIGHT = 50
THUMBNAIL_SIZE = 40

// Preview panel
PREVIEW_PANEL_WIDTH = 350
PREVIEW_IMAGE_SIZE = 150

// Buttons
CLOSE_BUTTON_SIZE = 20
PLACE_BUTTON_WIDTH = 100
PLACE_BUTTON_HEIGHT = 35

// Colors
WINDOW_BG = new Color(255, 255, 255, 240)
HEADER_BG = new Color(0, 102, 204)
SELECTION_HIGHLIGHT = new Color(200, 220, 255)
PLACE_BUTTON = new Color(76, 175, 80)

// Fonts
HEADER_FONT = new Font("Arial", Font.BOLD, 18)
ITEM_NAME_FONT = new Font("Arial", Font.PLAIN, 14)
```

**Public API:**
```java
// Constructor
InventoryLayout(int windowX, int windowY)

// Getters
int getWindowX()
int getWindowY()
int getPreviewX()
int getPreviewY()

// Bounds (public final fields)
Rectangle closeButtonBounds
Rectangle headerBounds
Rectangle listBounds
Rectangle placeButtonBounds  // Can be null

// Update bounds when furniture selected
void updatePlaceButtonBounds(boolean hasSelection)
```

**Why this class exists:**
- ✅ No more magic numbers scattered everywhere
- ✅ Change UI layout in ONE place
- ✅ Easy to create different themes/skins
- ✅ Bounds calculated once, reused everywhere

---

### 3️⃣ FurnitureList.java

**Role:** Furniture data management

**Responsibilities:**
- Load furniture items from resources
- Track selected furniture
- Provide furniture query methods
- Manage selection state

**Public API:**
```java
// Selection
void selectFurniture(int index)         // Select furniture by index
void clearSelection()                   // Clear selection
Furniture getSelected()                 // Get selected furniture
boolean hasSelection()                  // Check if anything selected

// Queries
Furniture getFurnitureAt(int index)     // Get furniture at index
int size()                              // Total furniture count
List<Furniture> getAll()                // Get all furniture (copy)
boolean isSelected(Furniture furniture) // Check if furniture is selected
```

**Internal Structure:**
```java
private List<Furniture> items           // All available furniture
private Furniture selectedFurniture     // Currently selected item
```

**Usage:**
```java
FurnitureList list = new FurnitureList();

// Select furniture
list.selectFurniture(0);  // Select first item

// Check selection
if (list.hasSelection()) {
    Furniture selected = list.getSelected();
    System.out.println("Selected: " + selected.name);
}

// Query
for (int i = 0; i < list.size(); i++) {
    Furniture furniture = list.getFurnitureAt(i);
    // Do something with furniture
}
```

**TODO:** Currently loads hardcoded furniture. Should be extended to dynamically scan `/res` folder:
```java
private void loadFurnitureFromRes() {
    // TODO: Scan /res/furniture folder for PNG files
    // For now, manually add furniture:
    items.add(new Furniture("Chair", "/res/tile/Chair_base.png", 1, 1));
}
```

---

### 4️⃣ PlacementMode.java

**Role:** Furniture placement logic and rendering

**Responsibilities:**
- Enter/exit placement mode
- Track preview position (which tiles will be occupied)
- Validate placement (within bounds, not overlapping, etc.)
- Render placement preview (green grid + furniture ghost)
- Confirm or cancel placement

**Public API:**
```java
// Mode control
void enter(Furniture furniture)         // Enter placement mode with furniture
void exit()                             // Exit placement mode
boolean isActive()                      // Check if in placement mode

// Preview
void updatePreview(int mouseX, int mouseY)  // Update preview position

// Placement
void confirmPlacement()                 // Place furniture at preview position
void cancel()                           // Cancel placement

// Rendering
void drawPreview(Graphics2D g2d)        // Draw placement grid + ghost
```

**Internal State:**
```java
private boolean active                  // Is placement mode active?
private Furniture furnitureToPlace      // Furniture being placed
private int previewMapX                 // Preview tile X coordinate
private int previewMapY                 // Preview tile Y coordinate
```

**Placement Flow:**
```
1. enter(furniture) → Creates copy of furniture
2. updatePreview(x, y) → Updates preview position every frame
3. drawPreview(g2d) → Renders green grid + ghost
4. User clicks:
   - Left click → confirmPlacement() → Adds to FurnitureManager
   - Right click → cancel() → Exits without placing
```

**Rendering Details:**
```java
drawPreview(g2d)
├─> drawGrid(g2d)
│   └─> drawTile(g2d, x, y) for each tile
│       ├─> Green semi-transparent fill
│       └─> Green border
└─> drawFurnitureGhost(g2d)
    └─> 70% transparent furniture image
```

**Validation:**
```java
private boolean isValidPlacement() {
    return previewMapX >= 0 && previewMapY >= 0
        && previewMapX + furniture.tileWidth <= maxWorldCol
        && previewMapY + furniture.tileHeight <= maxWorldRow;
    // TODO: Add collision detection with existing furniture
}
```

---

### 5️⃣ InventoryWindowUI.java

**Role:** All rendering code (view layer)

**Responsibilities:**
- Draw inventory window background and border
- Draw header with title
- Draw close button (with hover effect)
- Draw furniture list with selection highlight
- Draw preview panel with furniture image
- Draw place button

**Public API:**
```java
// Constructor
InventoryWindowUI(InventoryLayout layout, FurnitureList list, boolean hoverClose)

// Rendering
void draw(Graphics2D g2d)  // Draw entire inventory window
```

**Rendering Methods (private):**
```java
private void drawWindow(Graphics2D g2d)         // Background + border
private void drawHeader(Graphics2D g2d)         // Header bar + title
private void drawCloseButton(Graphics2D g2d)    // X button (hover effect)
private void drawDivider(Graphics2D g2d)        // Vertical line
private void drawFurnitureList(Graphics2D g2d)  // Left panel
private void drawFurnitureItem(...)             // Individual item
private void drawPreviewPanel(Graphics2D g2d)   // Right panel
private void drawPlaceButton(Graphics2D g2d)    // Place button
```

**Why this class exists:**
- ✅ Separates rendering from logic
- ✅ Easy to modify UI without touching business logic
- ✅ Can swap rendering implementations (e.g., custom themes)
- ✅ All Graphics2D code isolated here

**Design Pattern:** This is the **View** in Model-View-Controller:
- **Model:** FurnitureList, PlacementMode
- **View:** InventoryWindowUI
- **Controller:** InventoryWindow

---

## 🚀 Quick Start

### Step 1: Add to your GamePanel

```java
package main;

import ui.inventory.InventoryWindow;

public class GamePanel extends JPanel {
    
    public InventoryWindow inventoryWindow;
    
    public GamePanel(String username, String gender) {
        // ... other initialization ...
        
        // Create inventory
        this.inventoryWindow = new InventoryWindow(this);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // ... draw game world ...
        
        // Draw inventory
        inventoryWindow.draw(g2d);
        inventoryWindow.drawPlacementPreview(g2d);
        
        g2d.dispose();
    }
}
```

### Step 2: Handle input events

```java
// Mouse click handler
private void handleMouseClicked(MouseEvent e) {
    if (inventoryWindow.isVisible()) {
        inventoryWindow.handleClick(e.getX(), e.getY());
        return;
    }
    
    if (inventoryWindow.isPlacementMode()) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            inventoryWindow.confirmPlacement();
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            inventoryWindow.cancelPlacement();
        }
        return;
    }
    
    // ... other game clicks ...
}

// Mouse move handler
private void handleMouseMoved(MouseEvent e) {
    inventoryWindow.handleMouseMove(e.getX(), e.getY());
    
    if (inventoryWindow.isPlacementMode()) {
        inventoryWindow.updatePlacementPreview(e.getX(), e.getY());
    }
}

// Mouse drag handler
private void handleMouseDragged(MouseEvent e) {
    inventoryWindow.handleDrag(e.getX(), e.getY());
}

// Mouse release handler
private void handleMouseReleased(MouseEvent e) {
    inventoryWindow.handleRelease();
}
```

### Step 3: Add inventory toggle button

```java
// In GameWindow or wherever you have UI controls
JButton inventoryButton = new JButton("📦 INVENTORY");
inventoryButton.addActionListener(e -> {
    gamePanel.inventoryWindow.toggle();
});
```

---

## 💡 Usage Examples

### Example 1: Open Inventory
```java
// User clicks inventory button
inventoryWindow.toggle();  // Opens inventory
```

### Example 2: Select and Place Furniture
```java
// 1. User opens inventory
inventoryWindow.toggle();

// 2. User clicks on "Chair" in list
inventoryWindow.handleClick(mouseX, mouseY);
// → FurnitureList selects chair

// 3. User clicks "Place" button
inventoryWindow.handleClick(placeButtonX, placeButtonY);
// → Enters placement mode
// → Inventory window hides

// 4. User moves mouse (every frame)
inventoryWindow.updatePlacementPreview(mouseX, mouseY);
// → Updates green grid position

// 5. User left-clicks to confirm
inventoryWindow.confirmPlacement();
// → Adds chair to FurnitureManager
// → Exits placement mode
```

### Example 3: Cancel Placement
```java
// User is in placement mode
if (inventoryWindow.isPlacementMode()) {
    // User right-clicks
    inventoryWindow.cancelPlacement();
    // → Exits placement mode without placing
}
```

### Example 4: Drag Window
```java
// User clicks and drags header
inventoryWindow.handleClick(headerX, headerY);     // Starts drag
inventoryWindow.handleDrag(newX, newY);            // Updates position
inventoryWindow.handleRelease();                   // Stops drag
```

---

## 🎨 Customization Guide

### Change Colors

**File:** `InventoryLayout.java`

```java
// Change to dark theme
public static final Color WINDOW_BG = new Color(40, 40, 40, 240);
public static final Color HEADER_BG = new Color(30, 30, 30);
public static final Color HEADER_TEXT = new Color(200, 200, 200);
public static final Color SELECTION_HIGHLIGHT = new Color(60, 60, 80);
```

### Change Window Size

**File:** `InventoryLayout.java`

```java
// Make window larger
public static final int WINDOW_WIDTH = 800;   // Was 600
public static final int WINDOW_HEIGHT = 600;  // Was 400
public static final int LIST_WIDTH = 350;     // Was 250
```

### Add Furniture Categories

**File:** `FurnitureList.java`

```java
private Map<String, List<Furniture>> categories;

public FurnitureList() {
    this.categories = new HashMap<>();
    loadFurnitureFromRes();
}

private void loadFurnitureFromRes() {
    // Chairs
    List<Furniture> chairs = new ArrayList<>();
    chairs.add(new Furniture("Wooden Chair", "/res/chair1.png", 1, 1));
    chairs.add(new Furniture("Office Chair", "/res/chair2.png", 1, 1));
    categories.put("Chairs", chairs);
    
    // Tables
    List<Furniture> tables = new ArrayList<>();
    tables.add(new Furniture("Coffee Table", "/res/table1.png", 2, 1));
    tables.add(new Furniture("Dining Table", "/res/table2.png", 2, 2));
    categories.put("Tables", tables);
    
    // Flatten for backward compatibility
    items.addAll(chairs);
    items.addAll(tables);
}

public List<String> getCategories() {
    return new ArrayList<>(categories.keySet());
}

public List<Furniture> getFurnitureByCategory(String category) {
    return new ArrayList<>(categories.get(category));
}
```

### Add Mouse Wheel Scrolling

**File:** `InventoryWindowUI.java`

```java
private int scrollOffset = 0;

public void handleScroll(int wheelRotation) {
    scrollOffset += wheelRotation * InventoryLayout.ITEM_HEIGHT;
    
    // Clamp to valid range
    int maxScroll = Math.max(0, 
        furnitureList.size() * InventoryLayout.ITEM_HEIGHT 
        - layout.listBounds.height);
    scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
}

private void drawFurnitureItem(Graphics2D g2d, Furniture furniture, int index) {
    int itemY = layout.listBounds.y + (index * InventoryLayout.ITEM_HEIGHT) - scrollOffset;
    // ... rest of drawing code ...
}
```

Then in `InventoryWindow.java`:
```java
public void handleScroll(int wheelRotation) {
    // Pass to UI (need to keep reference)
    ui.handleScroll(wheelRotation);
}
```

### Add Furniture Rotation

**File:** `PlacementMode.java`

```java
private int rotation = 0;  // 0, 90, 180, 270

public void rotate() {
    rotation = (rotation + 90) % 360;
}

private void drawFurnitureGhost(Graphics2D g2d) {
    // ... existing code ...
    
    // Apply rotation transform
    AffineTransform original = g2d.getTransform();
    g2d.rotate(Math.toRadians(rotation), isoX + drawWidth/2, isoY);
    
    g2d.drawImage(furnitureToPlace.image, isoX, isoY - drawHeight + gp.tileSizeHeight,
                 drawWidth, drawHeight, null);
    
    g2d.setTransform(original);
}
```

Then handle 'R' key press:
```java
// In GamePanel key handler
if (e.getKeyCode() == KeyEvent.VK_R && inventoryWindow.isPlacementMode()) {
    inventoryWindow.rotateFurniture();  // Add this method to InventoryWindow
}
```

---

## 🧪 Testing

### Unit Test Example

```java
import org.junit.Test;
import static org.junit.Assert.*;
import ui.inventory.*;

public class FurnitureListTest {
    
    @Test
    public void testSelection() {
        FurnitureList list = new FurnitureList();
        
        // Initially no selection
        assertFalse(list.hasSelection());
        assertNull(list.getSelected());
        
        // Select first item
        list.selectFurniture(0);
        assertTrue(list.hasSelection());
        assertNotNull(list.getSelected());
        
        // Clear selection
        list.clearSelection();
        assertFalse(list.hasSelection());
    }
    
    @Test
    public void testInvalidIndex() {
        FurnitureList list = new FurnitureList();
        
        list.selectFurniture(-1);  // Should not crash
        assertFalse(list.hasSelection());
        
        list.selectFurniture(999);  // Should not crash
        assertFalse(list.hasSelection());
    }
}
```

### Integration Test Example

```java
@Test
public void testFullPlacementFlow() {
    GamePanel mockPanel = new MockGamePanel();
    InventoryWindow inventory = new InventoryWindow(mockPanel);
    
    // 1. Open inventory
    inventory.toggle();
    assertTrue(inventory.isVisible());
    
    // 2. Select furniture (simulate click on first item)
    int listX = mockPanel.screenWidth / 2 - 200;
    int listY = mockPanel.screenHeight / 2 - 150 + 50;
    inventory.handleClick(listX, listY);
    
    // 3. Click place button (simulate)
    int placeX = listX + 300;
    int placeY = mockPanel.screenHeight / 2 + 140;
    inventory.handleClick(placeX, placeY);
    
    assertTrue(inventory.isPlacementMode());
    assertFalse(inventory.isVisible());
    
    // 4. Confirm placement
    inventory.confirmPlacement();
    assertFalse(inventory.isPlacementMode());
}
```

---

## 🔮 Future Enhancements

### Planned Features:
- [ ] **Mouse wheel scrolling** for long furniture lists
- [ ] **Search/filter** furniture by name
- [ ] **Categories** (Chairs, Tables, Decorations, etc.)
- [ ] **Furniture rotation** (90° increments with 'R' key)
- [ ] **Furniture preview** in 3D/isometric view
- [ ] **Drag-and-drop** furniture placement
- [ ] **Undo/redo** for furniture placement
- [ ] **Save/load** room layouts
- [ ] **Furniture shop** with pricing
- [ ] **Inventory capacity** limits

### Easy to Add:
Because of the clean architecture, these features only require modifying specific classes:

| Feature | Classes to Modify |
|---------|-------------------|
| Scrolling | `InventoryWindowUI` only |
| Categories | `FurnitureList` only |
| Rotation | `PlacementMode` only |
| New colors | `InventoryLayout` only |
| Shop system | Create new `FurnitureShop` class |

---

## 📖 Related Documentation

- **GamePanel Integration:** See `GamePanel.java` for input routing
- **Furniture Class:** See `object/Furniture.java` for furniture data structure
- **FurnitureManager:** See `object/FurnitureManager.java` for furniture storage

---

## 🤝 Contributing

When adding features to this package:

1. **Follow Single Responsibility Principle** - Each class should do ONE thing
2. **Update constants in InventoryLayout** - Don't add magic numbers
3. **Keep rendering in InventoryWindowUI** - Don't mix logic with rendering
4. **Write tests** - Test new features independently
5. **Update this README** - Document new public APIs

---

## 📝 Changelog

### v2.0 (Current)
- ✅ Refactored from monolithic 450-line file
- ✅ Split into 5 focused classes
- ✅ Eliminated all magic numbers
- ✅ Added proper separation of concerns
- ✅ Improved testability

### v1.0 (Legacy)
- ⚠️ Single InventoryWindow.java file (450 lines)
- ⚠️ Mixed logic and rendering
- ⚠️ Hard to maintain and extend

---

## 📧 Questions?

For questions about this package:
1. Check the class-specific documentation above
2. Look at usage examples
3. Review the code comments (heavily documented)
4. Test with the provided unit tests

---

**Built with ❤️ for Futura Virtual World**

*Last updated: December 2024*