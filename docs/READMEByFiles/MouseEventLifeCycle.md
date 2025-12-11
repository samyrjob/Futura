Great question! Here's a clear breakdown:

## Mouse Event Lifecycle  (from GamePanel.java)  [`../src/main/GamePanel.java`](../src/main/GamePanel.java)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          MOUSE BUTTON LIFECYCLE                              │
│                                                                              │
│   User pushes button down     User releases button     Combined action      │
│          ↓                           ↓                       ↓              │
│   ┌──────────────┐           ┌──────────────┐         ┌──────────────┐      │
│   │ mousePressed │ ────────→ │mouseReleased │ ──────→ │ mouseClicked │      │
│   └──────────────┘           └──────────────┘         └──────────────┘      │
│                                                                              │
│   Fires IMMEDIATELY          Fires when finger        Fires ONLY if         │
│   when button goes down      lifts off button         press + release       │
│                                                       happened on SAME spot │
└─────────────────────────────────────────────────────────────────────────────┘
```

## All Mouse Methods Explained

| Method | When It Fires | Use Case |
|--------|---------------|----------|
| `mousePressed` | Button pushed DOWN | Start dragging, immediate response |
| `mouseReleased` | Button released UP | Stop dragging, drop item |
| `mouseClicked` | Press + Release on SAME spot | Select item, toggle, open menu |
| `mouseMoved` | Mouse moves (NO button held) | Hover effects, tooltips |
| `mouseDragged` | Mouse moves WITH button held | Drag window, draw lines |
| `mouseEntered` | Cursor enters component | Highlight component |
| `mouseExited` | Cursor leaves component | Remove highlight |
| `mouseWheelMoved` | Scroll wheel rotates | Scroll lists, zoom |

## Key Differences

### `mousePressed` vs `mouseClicked`

```java
// mousePressed - INSTANT (fires immediately when button goes down)
void mousePressed(MouseEvent e) {
    // Good for: Starting drag, immediate feedback
    startDragging();
}

// mouseClicked - DELAYED (waits for full press+release cycle)
void mouseClicked(MouseEvent e) {
    // Good for: Selections, toggles, confirmed actions
    selectItem();
}
```

### `mouseMoved` vs `mouseDragged`

```java
// mouseMoved - NO button held
void mouseMoved(MouseEvent e) {
    // Good for: Hover effects, cursor changes
    updateHoverState(e.getX(), e.getY());
}

// mouseDragged - Button IS held down
void mouseDragged(MouseEvent e) {
    // Good for: Moving windows, drawing, camera pan
    updateWindowPosition(e.getX(), e.getY());
}
```

## Visual Timeline

```
TIME ──────────────────────────────────────────────────────────────────→

User action:     [PUSH]═══════════════════════════════[RELEASE]
                    │                                      │
                    ↓                                      ↓
mousePressed ──→ FIRES                                     
                    │                                      
                    │    (mouse moves while held)          
                    │         ↓  ↓  ↓  ↓  ↓                
mouseDragged ──→         FIRES FIRES FIRES FIRES           
                                                           │
mouseReleased ──→                                       FIRES
                                                           │
mouseClicked ──→                                        FIRES
                                                    (only if same spot)
```

## When `mouseClicked` Does NOT Fire

```
┌─────────────────────────────────────────────────────────────────┐
│  mouseClicked will NOT fire if:                                  │
│                                                                  │
│  1. User presses at point A, drags, releases at point B         │
│     → Only mousePressed + mouseDragged + mouseReleased          │
│                                                                  │
│  2. User presses, moves mouse significantly, releases           │
│     → No mouseClicked (considered a drag, not a click)          │
└─────────────────────────────────────────────────────────────────┘
```

## Your Game's Pattern

```java
// ═══════════════════════════════════════════════════════════════
// YOUR CURRENT USAGE
// ═══════════════════════════════════════════════════════════════

handleMousePressed(e) {
    // 1. Start window dragging (RoomNavigator, FriendsPanel)
    // 2. Handle UI button clicks that need instant feedback
}

handleMouseReleased(e) {
    // 1. Stop window dragging
    // 2. Reset drag state
}

handleMouseClicked(e) {
    // 1. Tile selection (player movement)  ← YOUR ISSUE WAS HERE
    // 2. Player profile toggle
    // 3. Music controls
}

handleMouseMoved(e) {
    // 1. Update hover states (buttons, room items)
    // 2. Placement preview for furniture
}

handleMouseDragged(e) {
    // 1. Move windows around (drag by header)
    // 2. Pan camera (drag the world)
}
```

## Why Your Bug Happened

```
User clicks tile while RoomNavigator is open:

1. mousePressed  → Checks RoomNavigator, not on it → PASSES ✅
2. mouseReleased → Does cleanup → PASSES ✅  
3. mouseClicked  → if (roomNavigator.isVisible()) return; → BLOCKED ❌
                   ↓
                   handleTileClick() NEVER CALLED!
```

## Best Practice for UI Panels

```java
// ✅ CORRECT PATTERN - Check if click is ON the panel
if (panel.isVisible() && panel.containsPoint(mouseX, mouseY)) {
    panel.handleClick(mouseX, mouseY);
    return;
}
// Clicks OUTSIDE the panel pass through to game world

// ❌ WRONG PATTERN - Blocks ALL clicks
if (panel.isVisible()) {
    return;  // Nothing else can be clicked!
}
```