# 🎮 Player System Documentation

> **Complete guide to the Player character system in our Habbo Hotel-style multiplayer game**

## 📂 File Location
[`src/Entity/Player.java`](src/Entity/Player.java)

---

## 🔑 Key Components

### 1. **Basic Properties** ([Lines 19-38](src/Entity/Player.java#L19-L38))

```java
public String name;           // Player's username
public Gender gender;          // Male/Female (affects sprite)
public int credits;            // In-game currency
public int xCurrent, yCurrent; // Position on map (grid coordinates)
public boolean in_movement;    // Are we moving right now?
```

**Purpose**: Core player data that defines who the player is and their current state.

---

### 2. **Sprite System** ([Lines 85-112](src/Entity/Player.java#L85-L112))

The player has **multiple sprites** for different directions and animation states:

- **8 Directions**: Front, Back, Left, Right, and 4 diagonals
- **3 States per direction**: Standing, Walking Frame 1, Walking Frame 2

**Loading**: See [`loadPlayerImage()`](src/Entity/Player.java#L85-L112)

**Example sprites**:
- `sprite-front.png` - Standing still facing camera
- `sprite-front-mov-1.png` - Walking animation frame 1
- `sprite-front-mov-2.png` - Walking animation frame 2

---

### 3. **Pathfinding System** ([Lines 25-29](src/Entity/Player.java#L25-L29), [157-167](src/Entity/Player.java#L157-L167))

```java
private PathFinder pathFinder;        // The brain that finds paths
private List<Node> currentPath;       // The route we're following
private int pathIndex = 0;            // Which step we're on
```

**How it works**:
1. You **click** somewhere on the map
2. **PathFinder** uses **A* algorithm** to find the shortest path
3. Player walks **tile by tile** along that path
4. Each step takes **250ms** ([`STEP_DURATION`](src/Entity/Player.java#L28))

**Example**:
```
You're at (0,0), you click (5,5)
PathFinder creates: (0,0) → (1,1) → (2,2) → (3,3) → (4,4) → (5,5)
Player walks one tile every 250ms 
```

**Key Methods**:
- [`moveTo(int targetCol, int targetRow)`](src/Entity/Player.java#L157-L167) - Initiates pathfinding to destination
- [`update()`](src/Entity/Player.java#L169-L206) - Executes movement along the path
- [`determineDirection()`](src/Entity/Player.java#L208-L234) - Calculates which way to face

---

### 4. **Coordinate Conversion** ([Lines 114-128](src/Entity/Player.java#L114-L128))

**The Challenge**: Converting between grid coordinates and screen pixels in isometric view

```java
// Convert map position (row, col) to screen position (pixels)
public int conversion_from_mapXY_to_spriteX(int mapX, int mapY)
public int conversion_from_mapXY_to_spriteY(int mapX, int mapY)
```

**Why it's needed**: 
- Map uses grid coordinates (like chess: row 5, column 3)
- Screen uses pixel coordinates (X=400px, Y=200px)
- Isometric view is tilted 45° so requires special math!

**Helper Methods**:
- [`conversion_from_mapXY_to_tilecenterX()`](src/Entity/Player.java#L114-L116) - Get tile center X
- [`conversion_from_mapXY_to_tilecenterY()`](src/Entity/Player.java#L118-L120) - Get tile center Y
- [`conversion_from_mapXY_to_spriteX()`](src/Entity/Player.java#L122-L124) - Get sprite draw X
- [`conversion_from_mapXY_to_spriteY()`](src/Entity/Player.java#L126-L128) - Get sprite draw Y

---

### 5. **Game Loop Update** ([Lines 169-206](src/Entity/Player.java#L169-L206))

Called **60 times per second** (every frame):

```java
public void update() {
    // 1. Check if time to move to next tile (250ms intervals)
    // 2. Update position (xCurrent, yCurrent)
    // 3. Animate sprite (switch between frames)
    // 4. Send network update to other players
    // 5. Update typing status
}
```

**Flow**:
1. Has 250ms passed since last step? → Move to next tile
2. Update sprite position on screen
3. Animate walking (frame 1 ↔ frame 2)
4. Check if typing bubble should disappear
5. Send position to server if changed

---

### 6. **Direction System** ([Lines 208-234](src/Entity/Player.java#L208-L234))

**8 Possible Directions**:

```
      ISO_Y_UP (⬆️)
         
ISO_X_LEFT (⬅️)  🧍  ISO_X_RIGHT (➡️)
         
      ISO_Y_DOWN (⬇️)

Plus 4 diagonals:
DIAGONALE_UP (↖️)
DIAGONALE_DOWN (↘️)  
LEFT (↙️)
RIGHT (↗️)
```

**Methods**:
- [`determineDirection()`](src/Entity/Player.java#L208-L234) - Calculate direction from movement vector
- [`calculateDirectionToTarget()`](src/Entity/Player.java#L355-L404) - Face toward a target position
- [`faceDirection()`](src/Entity/Player.java#L408-L431) - Change facing direction without moving

---

### 7. **Typing Indicator** ([Lines 32-35](src/Entity/Player.java#L32-L35), [255-274](src/Entity/Player.java#L255-L274))

The **"..." bubble** that appears when typing! 💬

```java
public boolean isTyping = false;                    // Currently typing?
private long lastTypingTime = 0;                    // When did typing start?
private static final long TYPING_TIMEOUT = 3000;    // Auto-hide after 3s
```

**How it works**:
1. Player starts typing → [`setTyping(true)`](src/Entity/Player.java#L248-L252) is called
2. Bubble appears above head ([`drawTypingBubble()`](src/Entity/Player.java#L255-L274))
3. After 3 seconds of no typing → bubble disappears automatically
4. Visual: White speech bubble with "..." text

**Just like Habbo Hotel!** 🏨

---

### 8. **Network Synchronization** ([Lines 236-252](src/Entity/Player.java#L236-L252))

```java
private void sendNetworkUpdate() {
    // Only send if something actually changed!
    if (xCurrent != prevMapX || yCurrent != prevMapY || 
        direction != prevDirection || in_movement != prevInMovement) {
        
        networkManager.sendMoveMessage(...);
    }
}
```

**Purpose**: Keep other players in sync with your movements

**Flow**:
1. Detect if position/direction changed
2. Send update to server via [`NetworkManager`](src/network/NetworkManager.java)
3. Server broadcasts to all other players
4. They see you move on their screens!

**Optimization**: Only sends updates when state changes (not every frame!)

---

### 9. **Rendering** ([Lines 281-318](src/Entity/Player.java#L281-L318))

```java
public void draw_player(Graphics2D g2d) {
    // 1. Pick correct sprite based on direction and movement state
    // 2. Draw sprite at screen position
    // 3. Draw typing bubble if typing
}
```

**Called every frame** to paint the character on screen!

**Sprite Selection Logic**:
- If `in_movement == true` → Use walking animation frames
- If `in_movement == false` → Use standing sprite
- Direction determines which sprite set to use

---

## 🎮 How It All Works Together

### **Example: Player Clicks a Tile**

1. **Click detected** → [`moveTo(5, 3)`](src/Entity/Player.java#L157-L167) called
2. **Pathfinding** → `PathFinder` calculates: (0,0)→(1,1)→(2,2)→(3,3)→(4,4)→(5,3)
3. **Movement starts** → `in_movement = true`, `currentPath` stored
4. **Every frame** → [`update()`](src/Entity/Player.java#L169-L206) checks if 250ms passed
5. **Every 250ms** → Move to next tile, animate sprite
6. **Direction** → [`determineDirection()`](src/Entity/Player.java#L208-L234) calculates facing
7. **Rendering** → [`draw_player()`](src/Entity/Player.java#L281-L318) shows animation
8. **Networking** → [`sendNetworkUpdate()`](src/Entity/Player.java#L236-L252) broadcasts position
9. **Other players** → Receive update and see you walking!

---

## 🔧 Key Methods Reference

| Method | Lines | Purpose |
|--------|-------|---------|
| [`Player()`](src/Entity/Player.java#L59-L82) | 59-82 | Constructor - initializes player |
| [`loadPlayerImage()`](src/Entity/Player.java#L85-L112) | 85-112 | Load all sprite images |
| [`moveTo()`](src/Entity/Player.java#L157-L167) | 157-167 | Start pathfinding to destination |
| [`update()`](src/Entity/Player.java#L169-L206) | 169-206 | Game loop - handle movement |
| [`determineDirection()`](src/Entity/Player.java#L208-L234) | 208-234 | Calculate direction from movement |
| [`sendNetworkUpdate()`](src/Entity/Player.java#L236-L252) | 236-252 | Sync with other players |
| [`setTyping()`](src/Entity/Player.java#L248-L252) | 248-252 | Show/hide typing bubble |
| [`drawTypingBubble()`](src/Entity/Player.java#L255-L274) | 255-274 | Render typing indicator |
| [`draw_player()`](src/Entity/Player.java#L281-L318) | 281-318 | Render player sprite |
| [`contains()`](src/Entity/Player.java#L321-L337) | 321-337 | Check if mouse is over player |
| [`calculateDirectionToTarget()`](src/Entity/Player.java#L355-L404) | 355-404 | Face toward target |
| [`faceDirection()`](src/Entity/Player.java#L408-L431) | 408-431 | Change facing direction |

---

## 🐛 Common Issues & Solutions

### **Player teleports instead of walking**
- **Cause**: `STEP_DURATION` might be too small or `update()` not called
- **Fix**: Check game loop timing and verify `STEP_DURATION = 250ms`

### **Player faces wrong direction**
- **Cause**: [`determineDirection()`](src/Entity/Player.java#L208-L234) math incorrect
- **Fix**: Debug the deltaX/deltaY calculations

### **Other players don't see movement**
- **Cause**: [`networkManager`](src/Entity/Player.java#L16) is null or not connected
- **Fix**: Ensure network connection established before gameplay

### **Sprite doesn't animate**
- **Cause**: `SpriteNum` not toggling or images not loaded
- **Fix**: Check [`loadPlayerImage()`](src/Entity/Player.java#L85-L112) for errors

---

## 📚 Related Classes

- [`PathFinder.java`](src/pathfinding/PathFinder.java) - A* pathfinding algorithm
- [`NetworkManager.java`](src/network/NetworkManager.java) - Server communication
- [`GamePanel.java`](src/main/GamePanel.java) - Game loop and rendering
- [`Entity.java`](src/Entity/Entity.java) - Base entity class

---

## 💡 For Junior Developers

**Key Concepts Demonstrated**:

1. **Game Loop Pattern** - `update()` + `draw()` called every frame
2. **State Management** - `in_movement`, `direction` track player state  
3. **Event-Driven** - Methods triggered by user actions (clicks, typing)
4. **Networking** - Client-server synchronization
5. **Animation** - Frame-based sprite animation
6. **Pathfinding** - A* algorithm for smart movement
7. **Coordinate Systems** - Grid vs screen coordinates

**This is well-structured game code!** Each concern (movement, rendering, networking) is separated into clear methods. 👍

---

## 🤝 Contributing

When modifying `Player.java`:
1. Keep network updates efficient (only send when changed)
2. Maintain 250ms step duration for consistent feel
3. Test all 8 directions for sprite correctness
4. Verify multiplayer sync after changes

---

## 📝 License

Part of the Habbo Hotel-style multiplayer game project.

---

**Questions?** Check the inline code comments in [`Player.java`](src/Entity/Player.java) or reach out to the dev team!