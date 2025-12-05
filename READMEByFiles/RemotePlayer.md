# 🌐 RemotePlayer System Documentation

> **Handles OTHER players in the multiplayer game - the avatars you see moving around controlled by other people!**

## 📂 File Location
[`../src/Entity/RemotePlayer.java`](../../src/Entity/RemotePlayer.java)

---

## 🎯 What is RemotePlayer?

**RemotePlayer** represents **other players** in your multiplayer game:
- **Player.java** = YOU (the local player you control)
- **RemotePlayer.java** = OTHER PEOPLE (players you see on your screen)

**Key Difference**: 
- `Player.java` **generates** movement and sends it to server
- `RemotePlayer.java` **receives** movement updates from server and displays them

Think of it like this:
```
YOUR SCREEN:
- 1 Player (you)           → Player.java
- 10 RemotePlayers (others) → RemotePlayer.java × 10
```

---

## 🔑 Key Components

### 1. **Basic Properties** ([Lines 10-25](../src/Entity/RemotePlayer.java#L10-L25))

```java
public String name;              // Other player's username
public Gender gender;            // Their character gender
public int xCurrent, yCurrent;   // Their position on map
public boolean in_movement;      // Are they walking?
public int spriteX, spriteY;     // Screen coordinates
```

**Purpose**: Store essential info about the remote player we're rendering

**Comparison to Player.java**:
- ✅ Has: name, gender, position, sprites
- ❌ Missing: PathFinder (no pathfinding needed!)
- ❌ Missing: NetworkManager (doesn't send, only receives!)
- ❌ Missing: Mouse input handling (you don't control them!)

---

### 2. **Message System** ([Lines 27-28](../src/Entity/RemotePlayer.java#L27-L28), [237-244](../src/Entity/RemotePlayer.java#L237-L244))

```java
public java.util.List<Message> messages = new java.util.ArrayList<>();
```

**Purpose**: Store chat messages that appear above this player's head (speech bubbles)

**How it works**:
1. Server broadcasts: `"playerChat JohnDoe Hello everyone!"`
2. Your game finds the RemotePlayer named "JohnDoe"
3. Creates a Message and adds to their `messages` list
4. GamePanel draws speech bubble above their sprite

**Related**: See [`Player.Message`](../src/Entity/Player.java#L433-L440) for the same system on local player

---

### 3. **Sprite Loading** ([Lines 52-91](../src/Entity/RemotePlayer.java#L52-L91))

```java
private void loadPlayerImage() {
    // Load ALL 24 sprite images (same as local player)
    playerImageDiagonaleUp1 = ImageIO.read(...);
    playerImageLeft2 = ImageIO.read(...);
    // ... etc for all 8 directions × 3 states
}
```

**Purpose**: Load all sprite images for the 8 directions

**Sprite Structure**:
- **8 directions** (↑↗→↘↓↙←↖)
- **3 states per direction**: Standing, Walking Frame 1, Walking Frame 2
- **Total**: 24 sprite images per player!

**Why needed**: When you see another player walking northeast, your game needs to show the correct sprite!

**Method**: [`loadPlayerImage()`](../src/Entity/RemotePlayer.java#L52-L91)

---

### 4. **Position Updates from Server** ([Lines 93-105](../src/Entity/RemotePlayer.java#L93-L105))

```java
public void updatePosition(int mapX, int mapY, String directionStr, boolean inMovement) {
    this.xCurrent = mapX;
    this.yCurrent = mapY;
    this.in_movement = inMovement;
    
    try {
        this.direction = Direction.valueOf(directionStr);
    } catch (IllegalArgumentException e) {
        // Keep current direction if invalid
    }
    
    updateSpritePosition();
}
```

**Purpose**: Update this player's position when server sends movement data

**Flow**:
1. **Server sends**: `"playerMoved JohnDoe 5 3 DIAGONALE_DOWN true"`
2. **NetworkManager** parses it
3. **GamePanel** finds RemotePlayer "JohnDoe"
4. **Calls** [`updatePosition(5, 3, "DIAGONALE_DOWN", true)`](../src/Entity/RemotePlayer.java#L93)
5. **Result**: JohnDoe now appears at (5,3) walking diagonally down!

**This is THE key method** - it's how remote players move on your screen! 🎯

---

### 5. **Coordinate Conversion** ([Lines 107-119](../src/Entity/RemotePlayer.java#L107-L119))

```java
public void updateSpritePosition() {
    spriteX = tileCenterX - gp.tileSizeWidth;
    spriteY = tileCenterY - (3 * gp.tileSizeHeight);
}

private int conversion_from_mapXY_to_tilecenterX(int mapX, int mapY)
private int conversion_from_mapXY_to_tilecenterY(int mapX, int mapY)
```

**Purpose**: Convert map coordinates (5,3) to screen pixels (400px, 200px)

**Why needed**: 
- Server sends grid position: "Player at row 5, column 3"
- Your screen needs pixel position: "Draw at X=400px, Y=200px"
- Isometric math required! 📐

**Same as Player.java**: See [`Player coordinate conversion`](../src/Entity/Player.java#L114-L128)

**Methods**:
- [`updateSpritePosition()`](../src/Entity/RemotePlayer.java#L107-L112) - Main update method
- [`conversion_from_mapXY_to_tilecenterX()`](../src/Entity/RemotePlayer.java#L114-L116) - Calculate X pixel
- [`conversion_from_mapXY_to_tilecenterY()`](../src/Entity/RemotePlayer.java#L118-L120) - Calculate Y pixel

---

### 6. **Animation System** ([Lines 122-135](../src/Entity/RemotePlayer.java#L122-L135))

```java
public void update() {
    if (in_movement) {
        SpriteCounter++;
        if (SpriteCounter > 10) {
            SpriteNum = (SpriteNum == 1) ? 2 : 1;  // Toggle animation frame
            SpriteCounter = 0;
        }
    }
    updateCurrentSprite();
    updateSpritePosition();  // ✨ Update position (handles map dragging)
}
```

**Purpose**: Animate walking sprites (make legs move!)

**How it works**:
1. Called **60 times per second** (every frame)
2. If player is moving: increment counter
3. Every 10 frames (~0.16 seconds): switch between frame 1 and frame 2
4. Result: Legs alternate → walking animation! 🚶

**Flow**:
```
Frame 1  →  Frame 60  →  Frame 120
sprite-left-mov-1 → sprite-left-mov-2 → sprite-left-mov-1 → ...
```

**Method**: [`update()`](../src/Entity/RemotePlayer.java#L122-L135)

---

### 7. **Sprite Selection Logic** ([Lines 137-188](../src/Entity/RemotePlayer.java#L137-L188))

```java
private void updateCurrentSprite() {
    if (in_movement) {
        // Pick walking animation frame based on direction
        switch (direction) {
            case DIAGONALE_UP:
                currentSprite = (SpriteNum == 1) ? playerImageDiagonaleUp1 : playerImageDiagonaleUp2;
                break;
            // ... 7 more directions
        }
    } else {
        // Pick standing sprite based on direction
        switch (direction) {
            case DIAGONALE_DOWN:
                currentSprite = playerImageDiagonaleDown;
                break;
            // ... 7 more directions
        }
    }
}
```

**Purpose**: Choose the correct sprite image based on:
- **Direction** (which way facing?)
- **Movement state** (walking or standing?)
- **Animation frame** (frame 1 or 2?)

**Decision Tree**:
```
Is player moving?
├─ YES → Use walking sprite
│   └─ Which direction? → Pick direction sprite
│       └─ Which frame? → Pick frame 1 or 2
└─ NO → Use standing sprite
    └─ Which direction? → Pick direction sprite
```

**Method**: [`updateCurrentSprite()`](../src/Entity/RemotePlayer.java#L137-L188)

---

### 8. **Click Detection** ([Lines 190-204](../src/Entity/RemotePlayer.java#L190-L204))

```java
public boolean contains(int mouseX, int mouseY) {
    int hitboxWidth = (int)(drawnWidth * 0.4);   // 40% of sprite width
    int hitboxHeight = (int)(drawnHeight * 0.5); // 50% of sprite height
    
    int hitboxX = spriteX + (drawnWidth - hitboxWidth) / 2;  // Center horizontally
    int hitboxY = spriteY + drawnHeight - hitboxHeight;      // Bottom of sprite
    
    return (mouseX >= hitboxX && mouseX <= hitboxX + hitboxWidth &&
            mouseY >= hitboxY && mouseY <= hitboxY + hitboxHeight);
}
```

**Purpose**: Detect if mouse cursor is over this player (for clicking)

**Why smaller hitbox**: 
- Full sprite = 96×192px (2 tiles × 4 tiles)
- But character body is smaller!
- Hitbox = 40% width, 50% height (just the body, not empty space)

**Use Cases**:
- Click player to view profile
- Click player to initiate trade
- Click player to send friend request
- Right-click for context menu

**Same logic as**: [`Player.contains()`](../src/Entity/Player.java#L321-L337)

**Visual**:
```
┌─────────────────┐  Full sprite (96×192)
│                 │
│    ┌─────┐      │  ← Hitbox (40%×50%)
│    │ 🧍  │      │     Only body area
│    └─────┘      │
│                 │
└─────────────────┘
```

---

### 9. **Rendering** ([Lines 206-218](../src/Entity/RemotePlayer.java#L206-L218))

```java
public void draw(Graphics2D g2d) {
    if (currentSprite != null) {
        // Draw sprite
        g2d.drawImage(currentSprite, spriteX, spriteY, 
                      2 * gp.tileSizeWidth, 4 * gp.tileSizeHeight, null);
        
        // Draw player name above sprite
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int nameWidth = fm.stringWidth(name);
        g2d.drawString(name, spriteX + gp.tileSizeWidth - (nameWidth / 2), spriteY - 5);
    }
}
```

**Purpose**: Draw the remote player on screen

**What it draws**:
1. **Sprite image** (the character)
2. **Player name** (white text above head)

**Name positioning**:
- Centered horizontally above sprite
- 5 pixels above sprite top edge
- Uses `FontMetrics` to measure text width for perfect centering

**Called by**: GamePanel's main draw loop (60 times per second)

**Visual**:
```
     JohnDoe          ← Name (white text, centered)
      
       🧍             ← Sprite (96×192px)
```

---

## 🎮 How It All Works Together

### **Multiplayer Flow - Other Player Moves**

```
OTHER PLAYER'S COMPUTER:
1. They click tile (5,3)
2. Their Player.java pathfinds
3. Their NetworkManager sends: "move 5 3 DIAGONALE_DOWN true"

GAME SERVER:
4. Receives movement
5. Broadcasts to ALL other players: "playerMoved JohnDoe 5 3 DIAGONALE_DOWN true"

YOUR COMPUTER:
6. Your NetworkManager receives message
7. Your GamePanel finds RemotePlayer "JohnDoe"
8. Calls JohnDoe.updatePosition(5, 3, "DIAGONALE_DOWN", true)
9. JohnDoe's update() animates walking
10. JohnDoe's draw() renders on your screen
11. You see JohnDoe walking! 🎉
```

---

### **Constructor Flow** ([Lines 30-50](../src/Entity/RemotePlayer.java#L30-L50))

When a new player joins the game:

```java
// Server: "playerJoined JohnDoe male 0 0 DIAGONALE_DOWN"
RemotePlayer john = new RemotePlayer(gp, "JohnDoe", Gender.MALE, 0, 0, "DIAGONALE_DOWN");
```

**What happens**:
1. Store name, gender, initial position
2. Parse direction from string
3. Load all 24 sprite images
4. Calculate initial screen position
5. Set initial sprite (standing, facing down)
6. Ready to render! ✅

---

## 🔧 Key Methods Reference

| Method | Lines | Purpose |
|--------|-------|---------|
| [`RemotePlayer()`](../src/Entity/RemotePlayer.java#L30-L50) | 30-50 | Constructor - create remote player |
| [`loadPlayerImage()`](../src/Entity/RemotePlayer.java#L52-L91) | 52-91 | Load all sprite images |
| [`updatePosition()`](../src/Entity/RemotePlayer.java#L93-L105) | 93-105 | **KEY** - Update from server |
| [`updateSpritePosition()`](../src/Entity/RemotePlayer.java#L107-L112) | 107-112 | Convert map→screen coords |
| [`update()`](../src/Entity/RemotePlayer.java#L122-L135) | 122-135 | Animate sprite (60 FPS) |
| [`updateCurrentSprite()`](../src/Entity/RemotePlayer.java#L137-L188) | 137-188 | Pick correct sprite image |
| [`contains()`](../src/Entity/RemotePlayer.java#L190-L204) | 190-204 | Mouse click detection |
| [`draw()`](../src/Entity/RemotePlayer.java#L206-L218) | 206-218 | Render player on screen |

---

## 🆚 Player.java vs RemotePlayer.java

### **What RemotePlayer HAS** ✅
- Sprite rendering
- Position tracking
- Animation system
- Direction handling
- Coordinate conversion
- Click detection
- Message system (speech bubbles)

### **What RemotePlayer DOESN'T HAVE** ❌
- ❌ **PathFinder** - No pathfinding (server already calculated movement)
- ❌ **NetworkManager** - Doesn't send data, only receives
- ❌ **Mouse input** - You don't control other players!
- ❌ **Keyboard input** - Same reason
- ❌ **Credits/inventory** - Client doesn't know other players' items
- ❌ **Typing indicator** - (Could be added in future!)

### **Comparison Table**

| Feature | Player.java | RemotePlayer.java |
|---------|-------------|-------------------|
| Movement | Pathfinding (generates) | Direct updates (receives) |
| Networking | Sends to server | Receives from server |
| Input | Mouse + keyboard | None |
| Pathfinding | ✅ A* algorithm | ❌ Not needed |
| Sprite animation | ✅ Yes | ✅ Yes |
| Name display | ❌ No (you know your name!) | ✅ Yes (shows who they are) |
| Speech bubbles | ✅ Yes | ✅ Yes |
| Click detection | ✅ Yes | ✅ Yes |

---

## 🌐 Networking Integration

### **Server Messages → RemotePlayer Methods**

| Server Message | Method Called | Effect |
|----------------|---------------|--------|
| `playerJoined JohnDoe ...` | [`new RemotePlayer()`](../src/Entity/RemotePlayer.java#L30) | Create new player |
| `playerMoved JohnDoe 5 3 ...` | [`updatePosition()`](../src/Entity/RemotePlayer.java#L93) | Move player |
| `playerLeft JohnDoe` | Remove from list | Player disappears |
| `playerChat JohnDoe Hello!` | Add to `messages` | Show speech bubble |

### **How GamePanel Manages RemotePlayers** 

```java
// In GamePanel.java
Map<String, RemotePlayer> otherPlayers = new HashMap<>();

// When player joins
public void addOtherPlayer(String username, String gender, int x, int y, String dir) {
    RemotePlayer rp = new RemotePlayer(this, username, Gender.valueOf(gender), x, y, dir);
    otherPlayers.put(username, rp);
}

// When player moves
public void updateOtherPlayer(String username, int x, int y, String dir, boolean moving) {
    RemotePlayer rp = otherPlayers.get(username);
    if (rp != null) {
        rp.updatePosition(x, y, dir, moving);
    }
}

// Every frame - update and draw all
for (RemotePlayer rp : otherPlayers.values()) {
    rp.update();    // Animate
    rp.draw(g2d);   // Render
}
```

---

## 🐛 Common Issues & Solutions

### **Remote player appears at wrong position**
- **Cause**: Coordinate conversion incorrect or tile offset wrong
- **Fix**: Check [`updateSpritePosition()`](../src/Entity/RemotePlayer.java#L107-L112)
- **Debug**: Print `xCurrent, yCurrent` and `spriteX, spriteY`

### **Remote player doesn't animate**
- **Cause**: [`update()`](../src/Entity/RemotePlayer.java#L122) not being called every frame
- **Fix**: Verify GamePanel's update loop includes remote players

### **Remote player faces wrong direction**
- **Cause**: Direction string parsing failed
- **Fix**: Check server sends valid direction names (e.g., "DIAGONALE_DOWN" not "diagonale_down")
- **Related**: [`updatePosition()`](../src/Entity/RemotePlayer.java#L93-L105) has try-catch for this

### **Can't click on remote player**
- **Cause**: Hitbox calculation wrong or z-ordering issue
- **Fix**: Debug [`contains()`](../src/Entity/RemotePlayer.java#L190-L204) with print statements

### **Remote player name doesn't appear**
- **Cause**: Font not rendering or name is null
- **Fix**: Check [`draw()`](../src/Entity/RemotePlayer.java#L206-L218) and verify name set in constructor

### **Remote player teleports instead of smooth movement**
- **Expected behavior!** RemotePlayer doesn't do smooth interpolation
- Server sends discrete position updates
- For smooth movement, you'd need to interpolate between updates (advanced feature)

---

## 💡 For Junior Developers

### **Key Concepts**

**1. Client-Server Architecture**
```
YOUR CLIENT:
- 1 Player (you control)
- N RemotePlayers (you observe)

OTHER CLIENTS:
- 1 Player (they control - YOU are a RemotePlayer to them!)
- N RemotePlayers (they observe)

SERVER:
- Tracks everyone
- Broadcasts updates to all
```

**2. Separation of Concerns**
- **Player.java** = Input → Logic → Send
- **RemotePlayer.java** = Receive → Render
- **NetworkManager** = Communication
- **GamePanel** = Coordination

**3. State Synchronization**
```
Other player's state on their computer:
x=5, y=3, direction=DOWN, moving=true

Server broadcasts this

Your RemotePlayer receives and mirrors:
x=5, y=3, direction=DOWN, moving=true

Result: You see them at same position!
```

**4. Why No Pathfinding in RemotePlayer?**
```
BAD (inefficient):
1. Other player clicks tile
2. Server sends destination
3. Your RemotePlayer pathfinds (duplicate work!)

GOOD (efficient):
1. Other player pathfinds on their computer
2. Server sends each movement step
3. Your RemotePlayer just displays it
```

---

## 🚀 Potential Enhancements

### **Features You Could Add**

1. **Smooth Movement Interpolation**
   - Current: Teleports between tiles
   - Better: Smooth sliding between positions
   - Add: Lerp between `lastPosition` and `currentPosition`

2. **Typing Indicator** (like Player.java has)
   - Add `isTyping` boolean
   - Server broadcasts typing status
   - Draw "..." bubble above head

3. **Health Bars** (if you add combat)
   ```java
   public int health = 100;
   public int maxHealth = 100;
   // Draw bar above name
   ```

4. **Player Status Icons**
   - Away/AFK indicator
   - Trading indicator
   - Party/group indicator

5. **Outfit Customization**
   - Load different sprites based on outfit ID
   - Server sends: `"outfit 12"` → Load outfit_12 sprites

6. **Emotes/Actions**
   - Wave animation
   - Dance animation
   - Sit animation

---

## 📚 Related Classes

- [`Player.java`](../src/Entity/Player.java) - Local player (you)
- [`Entity.java`](../src/Entity/Entity.java) - Base class for both
- [`NetworkManager.java`](../src/network/NetworkManager.java) - Receives updates
- [`GamePanel.java`](../src/main/GamePanel.java) - Manages all RemotePlayers

---

## 🔄 Lifecycle Diagram

```
CREATION (Player joins server):
Server: "playerJoined JohnDoe male 0 0 DIAGONALE_DOWN"
  ↓
NetworkManager receives
  ↓
GamePanel.addOtherPlayer()
  ↓
new RemotePlayer("JohnDoe", ...)
  ↓
loadPlayerImage()
  ↓
Ready to render ✅

UPDATES (Every frame):
GamePanel.update()
  ↓
remotePlayer.update()
  ↓
- Animate sprite
- Update position
  ↓
GamePanel.draw()
  ↓
remotePlayer.draw(g2d)
  ↓
Rendered on screen 🎨

SERVER UPDATE (Player moves):
Server: "playerMoved JohnDoe 5 3 DIAGONALE_DOWN true"
  ↓
NetworkManager receives
  ↓
GamePanel.updateOtherPlayer()
  ↓
remotePlayer.updatePosition(5, 3, ...)
  ↓
Position updated ✅

DESTRUCTION (Player leaves):
Server: "playerLeft JohnDoe"
  ↓
NetworkManager receives
  ↓
GamePanel.removeOtherPlayer("JohnDoe")
  ↓
RemotePlayer removed from map
  ↓
No longer rendered ❌
```

---

## 🤝 Contributing

When modifying `RemotePlayer.java`:

**✅ DO**:
- Keep [`updatePosition()`](../src/Entity/RemotePlayer.java#L93) fast (called frequently!)
- Handle invalid direction strings gracefully
- Test with multiple players on screen
- Ensure sprites load correctly

**❌ DON'T**:
- Add heavy calculations in [`update()`](../src/Entity/RemotePlayer.java#L122) (60 FPS!)
- Assume server data is always valid
- Break backwards compatibility with server protocol

---

## 🎯 Quick Reference Card

```markdown
PURPOSE:           Render other players on your screen
CREATED WHEN:      Server broadcasts "playerJoined"
UPDATED WHEN:      Server broadcasts "playerMoved"
DESTROYED WHEN:    Server broadcasts "playerLeft"
KEY METHOD:        updatePosition() - receives server updates
RENDER METHOD:     draw() - draws sprite + name
FRAME RATE:        update() called 60 times/second
```

---

**Questions?** Check the inline code comments in [`RemotePlayer.java`](../src/Entity/RemotePlayer.java) or compare with [`Player.java`](../src/Entity/Player.java)!

---

Made with 💙 for multiplayer game devs!