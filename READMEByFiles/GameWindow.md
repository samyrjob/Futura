# 🪟 GameWindow.java - Main Game Window

> **The main window container that holds the game and chat interface**

---

## 🎯 **PURPOSE**

`GameWindow` is the **main window frame** that contains:
- The game panel (where the virtual world is rendered)
- Chat input system (message field + buttons)
- Window controls (close, minimize, etc.)

**Think of it as**: The outer shell/container that holds everything together.

---

## 📊 **STRUCTURE**

```
GameWindow
├── JFrame (window)
│   ├── GamePanel (CENTER) ← The game itself
│   └── Chat Panel (SOUTH) ← Input controls
│       ├── 📦 Inventory Button
│       ├── 💬 Message Field
│       └── ✉️ Send Button
```

---

## 🔧 **RESPONSIBILITIES**

### **1. Window Management**
- Creates the main JFrame window
- Sets window title: "Futura - Virtual World"
- Sets window size (fits to screen if needed)
- Centers window on screen
- Handles window closing events

### **2. GamePanel Integration**
- Creates the `GamePanel` (the actual game)
- Adds it to the center of the window
- Starts the game thread
- Connects to multiplayer server

### **3. Chat UI Creation**
- Creates inventory button (📦)
- Creates message input field
- Creates send button (✉️)
- Positions them at the bottom of the window

### **4. User Input Handling**
- Handles typing in the message field
- Shows typing indicator when user types
- Sends messages when user presses Enter or clicks Send
- Manages placeholder text ("type here to write a message")

### **5. Event Management**
- Handles window close (cleanup and exit)
- Handles button clicks (inventory, send)
- Handles keyboard input (Enter key)

---

## 🎨 **UI LAYOUT**

```
┌─────────────────────────────────────┐
│  Futura - Virtual World       [X]   │ ← Window title bar
├─────────────────────────────────────┤
│                                     │
│                                     │
│         GAME PANEL                  │
│      (Isometric game world)         │
│                                     │
│                                     │
│                                     │
├─────────────────────────────────────┤
│ [📦 INVENTORY] [Message...] [SEND] │ ← Chat panel
└─────────────────────────────────────┘
```

---

## 🚀 **INITIALIZATION FLOW**

```
1. GameWindow constructor called with (username, gender)
   ↓
2. Create JFrame window
   ↓
3. Create GamePanel (the game)
   ↓
4. initializeWindow()
   - Set window properties
   - Add GamePanel to center
   - Set window size
   ↓
5. initializeChatUI()
   - Create inventory button
   - Create message field
   - Create send button
   - Add to bottom of window
   ↓
6. initializeEventHandlers()
   - Setup window close handler
   ↓
7. Show window
   ↓
8. Start game thread
   ↓
9. Connect to multiplayer server
```

---

## 💬 **CHAT SYSTEM**

### **Message Field**
```java
// Placeholder text when empty
"type here to write a message"

// When user types:
- Placeholder disappears
- Text turns black
- Typing indicator appears (... bubble in game)

// When user stops typing:
- After 3 seconds, typing indicator disappears
```

### **Sending Messages**
```java
// User can send by:
1. Pressing Enter key
2. Clicking Send button

// What happens:
1. Message is added to player's chat bubble
2. Message is sent to server (if connected)
3. Message field is cleared
4. Typing indicator is hidden
```

---

## 🎨 **STYLING**

### **Window**
- Title: "Futura - Virtual World"
- Resizable: No (fixed size)
- Close operation: Custom (with cleanup)

### **Chat Panel**
- Background: Dark gray (40, 40, 40)
- Height: 60px
- Padding: 10px top/bottom

### **Inventory Button**
- Text: "📦 INVENTORY"
- Color: Gold (255, 193, 7)
- Hover: Lighter gold (255, 213, 79)
- Size: 130×35px

### **Message Field**
- Background: White
- Border: Black, 2px, rounded
- Size: 300×35px
- Placeholder: Gray italic text

### **Send Button**
- Text: "SEND"
- Color: White
- Hover: Light gray (230, 230, 230)
- Size: 80×35px

---

## 🔄 **INTERACTION FLOW**

### **Inventory Button**
```
User clicks 📦 INVENTORY
    ↓
gamePanel.inventoryWindow.toggle()
    ↓
Inventory window opens/closes
```

### **Message Field**
```
User focuses on field
    ↓
Placeholder disappears
    ↓
User types
    ↓
Typing indicator appears in game (... bubble)
    ↓
User presses Enter or clicks Send
    ↓
sendChatMessage() called
    ↓
Message bubble appears above player
    ↓
Message sent to server (if online)
```

### **Window Close**
```
User clicks [X]
    ↓
handleWindowClose() called
    ↓
gamePanel.cleanup()
    ↓
Disconnect from server
    ↓
window.dispose()
    ↓
System.exit(0)
```

---

## 📝 **KEY METHODS**

### **Constructor**
```java
public GameWindow(String username, String gender)
```
- Creates the window
- Initializes GamePanel with user info
- Sets up UI components
- Starts the game

### **initializeWindow()**
```java
private void initializeWindow()
```
- Configures JFrame properties
- Sets window size and position
- Adds GamePanel to center

### **initializeChatUI()**
```java
private void initializeChatUI()
```
- Creates chat panel at bottom
- Creates inventory button, message field, send button
- Adds them to the window

### **createStyledButton()**
```java
private JButton createStyledButton(String text, int width, int height, Color bg)
```
- Helper method to create buttons with consistent styling
- Used for both inventory and send buttons

### **sendChatMessage()**
```java
private void sendChatMessage()
```
- Gets text from message field
- Adds chat bubble to player
- Sends message to server
- Clears the field

### **handleWindowClose()**
```java
private void handleWindowClose()
```
- Cleans up resources
- Disconnects from server
- Exits the game

---

## 🎮 **USAGE EXAMPLE**

```java
// In Main.java:
public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        new GameWindow("JoeRogan", "male");
    });
}

// This creates:
// 1. Window with title "Futura - Virtual World"
// 2. GamePanel with player "JoeRogan" (male)
// 3. Chat UI at bottom
// 4. Starts game and connects to server
```

---

## 🔗 **DEPENDENCIES**

### **External Classes**
- `GamePanel` - The actual game (isometric world)
- `Entity.Player` - The player character
- `NetworkManager` - Handles multiplayer (accessed via GamePanel)
- `InventoryWindow` - Inventory UI (accessed via GamePanel)

### **Java Swing**
- `JFrame` - Main window
- `JPanel` - Containers
- `JTextField` - Message input
- `JButton` - Inventory and send buttons
- `BorderLayout` - Layout manager

---

## ⚙️ **CONSTANTS**

```java
WINDOW_TITLE = "Futura - Virtual World"
CHAT_PANEL_BG = Color(40, 40, 40)         // Dark gray
CHAT_PANEL_HEIGHT = 60                    // Pixels
INVENTORY_BG = Color(255, 193, 7)         // Gold
INVENTORY_BG_HOVER = Color(255, 213, 79)  // Light gold
SEND_BG = Color.WHITE                     // White
SEND_BG_HOVER = Color(230, 230, 230)      // Light gray
PLACEHOLDER = "type here to write a message"
```

---

## 🎯 **DESIGN PATTERN**

**Pattern**: **Composite/Container Pattern**

```
GameWindow (Container)
├── Has-a JFrame (window shell)
├── Has-a GamePanel (game content)
└── Has-a Chat UI (user controls)
```

**Benefits**:
- ✅ Separates window management from game logic
- ✅ UI components are modular
- ✅ Easy to add/remove UI elements
- ✅ Clean separation of concerns

---

## 🛠️ **CUSTOMIZATION**

### **Change Window Title**
```java
private static final String WINDOW_TITLE = "Your Game Name";
```

### **Change Chat Panel Color**
```java
private static final Color CHAT_PANEL_BG = new Color(50, 50, 50);
```

### **Change Button Colors**
```java
private static final Color INVENTORY_BG = new Color(100, 150, 200);
```

### **Change Placeholder Text**
```java
private static final String PLACEHOLDER = "Type your message...";
```

---

## 🐛 **TROUBLESHOOTING**

### **Window doesn't appear**
- Check if `window.setVisible(true)` is called
- Ensure `SwingUtilities.invokeLater()` is used in Main.java

### **Chat doesn't work**
- Check if GamePanel is initialized
- Verify player is created properly
- Check network connection (if multiplayer)

### **Inventory button doesn't work**
- Ensure `gamePanel.inventoryWindow` is initialized
- Check if InventoryWindow exists in GamePanel

---

## 📊 **COMPONENT HIERARCHY**

```
GameWindow
│
├── JFrame window
│   │
│   ├── GamePanel (CENTER)
│   │   ├── Player
│   │   ├── RemotePlayers
│   │   ├── TileManager
│   │   ├── FurnitureManager
│   │   ├── NetworkManager
│   │   └── InventoryWindow
│   │
│   └── chatInputPanel (SOUTH)
│       └── innerPanel
│           ├── inventoryButton
│           ├── messageField
│           └── sendButton
```

---

## ✅ **SUMMARY**

**GameWindow** is the **main window container** that:

1. **Creates** the window frame
2. **Holds** the GamePanel (the actual game)
3. **Provides** chat UI (inventory, message field, send button)
4. **Manages** window events (close, etc.)
5. **Coordinates** between UI and game logic

**Key Concept**: It's the **outer shell** that brings everything together!

---

## 🔗 **RELATED FILES**

- `Main.java` - Entry point, creates GameWindow
- `GamePanel.java` - The actual game (inside GameWindow)
- `Player.java` - Player character (used by chat system)
- `NetworkManager.java` - Multiplayer (accessed via GamePanel)
- `InventoryWindow.java` - Inventory UI (toggled by button)

---

**That's GameWindow.java!** 🪟🎮

The container that holds your virtual world! ✨