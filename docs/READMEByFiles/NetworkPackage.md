# 🌐 network Package

**TCP-based multiplayer networking system for Futura virtual world.**

Enables real-time synchronization of player positions, chat messages, and game state across multiple clients using a centralized server architecture.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Class Descriptions](#class-descriptions)
- [Protocol Specification](#protocol-specification)
- [Quick Start](#quick-start)
- [Usage Examples](#usage-examples)
- [Troubleshooting](#troubleshooting)
- [Security Considerations](#security-considerations)

---

## 🎯 Overview

The `network` package implements a **client-server multiplayer system** that allows multiple players to:

- 🎮 See each other in real-time
- 💬 Send chat messages
- 🚶 Synchronize player movements
- 👋 Handle player joins/leaves gracefully

### Key Features:
- ✅ Real-time player synchronization
- ✅ Broadcast messaging (chat, movement)
- ✅ Automatic player discovery on join
- ✅ Thread-safe client management
- ✅ Graceful disconnect handling
- ✅ Text-based protocol (human-readable for debugging)

### Network Topology:
```
        ┌─────────────┐
        │ GameServer  │  (Port 5555)
        │  (Central)  │
        └──────┬──────┘
               │
    ┌──────────┼──────────┐
    │          │          │
┌───▼───┐  ┌──▼────┐  ┌──▼────┐
│Client1│  │Client2│  │Client3│
│ (Joe) │  │(Alice)│  │ (Bob) │
└───────┘  └───────┘  └───────┘
```

---

## 🏗️ Architecture

### Server Side:
```
GameServer (Main)
    │
    ├─> GameServerGroup (Client registry)
    │       └─> List<ClientInfo> (Connected players)
    │
    └─> ClientHandler (per client thread)
            └─> Processes messages from one client
```

### Client Side:
```
GamePanel
    │
    └─> NetworkManager
            │
            ├─> Socket (connection to server)
            └─> ServerMessageWatcher (thread)
                    └─> Processes messages from server
```

### Data Flow:

**Player Movement:**
```
Player moves
    ↓
NetworkManager.sendMoveMessage()
    ↓
[TCP] → GameServer
    ↓
ClientHandler.handleMove()
    ↓
GameServerGroup.broadcast()
    ↓
[TCP] → All other clients
    ↓
ServerMessageWatcher.handlePlayerMoved()
    ↓
GamePanel.updateRemotePlayer()
    ↓
Remote player sprite updates
```

---

## 📚 Class Descriptions

### SERVER SIDE

---

#### 1️⃣ **GameServer.java**

**Role:** Main server application (entry point)

**Responsibilities:**
- Listen for incoming connections on port 5555
- Accept new client connections
- Spawn `ClientHandler` thread for each client
- Manage server lifecycle (start/shutdown)

**Key Methods:**
```java
public GameServer()               // Start server, listen for connections
public void shutdown()            // Gracefully stop server
public static void main(String[]) // Entry point: java network.GameServer
```

**How it works:**
```java
1. Create ServerSocket on port 5555
2. Loop forever:
   - Accept incoming connection
   - Create ClientHandler for the connection
   - Start ClientHandler thread
3. Each client runs independently in their own thread
```

**Usage:**
```bash
# Start the server (Terminal 1)
java -cp bin network.GameServer

# Output:
# Game Server starting on port 5555
# Server started successfully. Waiting for players...
# New connection from: 127.0.0.1:54321
# Player joined: Joe (Total players: 1)
```

**Thread Safety:** Main thread only accepts connections, actual client handling is delegated to threads.

---

#### 2️⃣ **ClientHandler.java**

**Role:** Per-client thread handling one player's connection

**Responsibilities:**
- Read messages from ONE specific client
- Parse and process incoming messages
- Broadcast player actions to other clients
- Handle disconnect/cleanup

**Key Methods:**
```java
@Override
public void run()                      // Main loop: read messages from client
private void processMessage(String)    // Route message to appropriate handler
private void handleJoin(String)        // Player joins game
private void handleMove(String)        // Player moves
private void handleChat(String)        // Player sends chat
private void handleWantDetails(String) // Player requests other players' info
private void handleDetailsFor(String)  // Forward player details
private void handleBye()               // Player disconnects
private void cleanup()                 // Clean up resources
```

**Message Routing:**
```java
processMessage("join Joe MALE 4 2 SOUTH")
    ↓
handleJoin()
    ↓
1. Extract: username="Joe", gender="MALE", position=(4,2), direction="SOUTH"
2. Create ClientInfo
3. Add to GameServerGroup
4. Broadcast "playerJoined Joe MALE 4 2 SOUTH" to all OTHER clients
5. Request details from existing players
```

**Thread Lifecycle:**
```
Client connects
    ↓
ClientHandler thread starts
    ↓
Read messages in loop
    ↓
Client disconnects (socket closes)
    ↓
Loop exits
    ↓
cleanup() called
    ↓
Thread terminates
```

**Error Handling:**
- IOException → Client disconnected → cleanup()
- Malformed message → Log and ignore (resilient)

---

#### 3️⃣ **GameServerGroup.java**

**Role:** Thread-safe registry of all connected clients

**Responsibilities:**
- Maintain list of connected players (`List<ClientInfo>`)
- Add/remove clients
- Broadcast messages to all clients (or all except sender)
- Send message to specific client

**Key Methods:**
```java
public synchronized void addClient(ClientInfo)           // Add new player
public synchronized void removeClient(String, int)       // Remove player
public synchronized ClientInfo getClient(String, int)    // Find player by addr:port
public synchronized void broadcast(String, int, String)  // Send to all except sender
public synchronized void sendTo(String, int, String)     // Send to specific client
public synchronized void broadcastToAll(String)          // Send to everyone
```

**Why synchronized?**
Multiple `ClientHandler` threads access this simultaneously:
```
Thread 1 (Player A): addClient()
Thread 2 (Player B): broadcast()    } ← Can happen at same time!
Thread 3 (Player C): removeClient()

synchronized ensures thread safety
```

**Data Structure:**
```java
List<ClientInfo> clients = [
    ClientInfo(Joe,   127.0.0.1:54321, ...),
    ClientInfo(Alice, 127.0.0.1:54322, ...),
    ClientInfo(Bob,   127.0.0.1:54323, ...)
]
```

**Broadcast Example:**
```java
// Player Joe moves
broadcast("127.0.0.1", 54321, "playerMoved Joe 5 3 EAST true")

// Sends to:
// - Alice (127.0.0.1:54322) ✅
// - Bob   (127.0.0.1:54323) ✅
// - Joe   (127.0.0.1:54321) ❌ (sender, excluded)
```

---

#### 4️⃣ **ClientInfo.java**

**Role:** Data holder representing one connected player

**Responsibilities:**
- Store player information (name, gender, position, direction)
- Store connection details (address, port, output stream)
- Send messages to this specific player
- Update player position

**Fields:**
```java
public PrintWriter out;       // Output stream to send messages to this client
public String address;        // Client IP address (e.g., "127.0.0.1")
public int port;              // Client port (e.g., 54321)
public String playerName;     // Player username (e.g., "Joe")
public String gender;         // "MALE" or "FEMALE"
public int mapX;              // Current tile X coordinate
public int mapY;              // Current tile Y coordinate
public String direction;      // "NORTH", "SOUTH", "EAST", "WEST"
```

**Key Methods:**
```java
public boolean matches(String addr, int p)     // Check if this is the client at addr:port
public void sendMessage(String message)        // Send message to this client
public void updatePosition(int x, int y, String dir)  // Update position
```

**Why public fields?**
This is a simple **Data Transfer Object (DTO)** - used only within the server package for passing player data around. No complex logic needed.

**Usage:**
```java
ClientInfo client = new ClientInfo(
    out,              // PrintWriter to send messages
    "127.0.0.1",      // Client address
    54321,            // Client port
    "Joe",            // Player name
    "MALE",           // Gender
    4, 2,             // Position (4, 2)
    "SOUTH"           // Facing south
);

// Send message to this client
client.sendMessage("playerJoined Alice FEMALE 3 3 NORTH");

// Update position
client.updatePosition(5, 2, "EAST");
```

---

### CLIENT SIDE

---

#### 5️⃣ **NetworkManager.java**

**Role:** Client-side networking interface (used by GamePanel)

**Responsibilities:**
- Connect to server
- Send messages to server (join, move, chat)
- Manage connection lifecycle
- Spawn message watcher thread

**Configuration:**
```java
private static final String SERVER_HOST = "localhost";
private static final int SERVER_PORT = 5555;
```

**Key Methods:**
```java
// Connection
public boolean connect()                          // Connect to server
public void disconnect()                          // Disconnect from server
public boolean isConnected()                      // Check connection status

// Outgoing messages
public void sendJoinMessage(...)                  // Tell server we joined
public void sendMoveMessage(...)                  // Tell server we moved
public void sendChatMessage(String text)          // Send chat message
public void sendDetailsRequest(...)               // Request another player's info
public void sendDetailsResponse(...)              // Send our info to requester
public void sendByeMessage()                      // Tell server we're leaving
```

**Connection Flow:**
```java
NetworkManager nm = new NetworkManager(gamePanel);

1. nm.connect()
    ↓
2. Create Socket to localhost:5555
    ↓
3. Create input/output streams
    ↓
4. Start ServerMessageWatcher thread
    ↓
5. Return true (connected)

// Now send messages:
nm.sendJoinMessage("Joe", "MALE", 4, 2, "SOUTH");
nm.sendChatMessage("Hello world!");
```

**Integration with GamePanel:**
```java
// GamePanel.java
public class GamePanel extends JPanel {
    public NetworkManager networkManager;
    
    public GamePanel(String username, String gender) {
        // Initialize
        this.networkManager = new NetworkManager(this);
        
        // Connect
        if (networkManager.connect()) {
            // Send join message
            networkManager.sendJoinMessage(
                player.name, 
                player.gender.toString(),
                player.xCurrent, 
                player.yCurrent,
                player.direction.toString()
            );
        }
    }
    
    // When player moves
    public void onPlayerMove() {
        networkManager.sendMoveMessage(
            player.xCurrent, 
            player.yCurrent,
            player.direction.toString(),
            true
        );
    }
}
```

**Error Handling:**
```java
public boolean connect() {
    try {
        socket = new Socket(SERVER_HOST, SERVER_PORT);
        // ... setup streams ...
        return true;
    } catch (IOException e) {
        System.err.println("Could not connect to server");
        return false;  // ← Graceful failure
    }
}
```

If connection fails, game runs in **single-player mode**.

---

#### 6️⃣ **ServerMessageWatcher.java**

**Role:** Background thread watching for incoming messages from server

**Responsibilities:**
- Continuously read messages from server
- Parse and process incoming messages
- Update GamePanel with received information (player joins, moves, chat)
- Run in background (separate thread)

**Key Methods:**
```java
@Override
public void run()                          // Main loop: read from server
private void processMessage(String)        // Route message to handler
private void handlePlayerJoined(String)    // Another player joined
private void handlePlayerMoved(String)     // Another player moved
private void handlePlayerChat(String)      // Another player sent chat
private void handlePlayerLeft(String)      // Another player left
private void handleWantDetails(String)     // Server wants our details
private void handleDetailsFor(String)      // Received another player's details
public void stopWatching()                 // Stop thread gracefully
```

**Thread Lifecycle:**
```
NetworkManager.connect()
    ↓
new ServerMessageWatcher(...)
    ↓
watcher.start()
    ↓
┌──────────────────────────┐
│ Background Thread Loop:  │
│ 1. Read line from server │
│ 2. Process message       │
│ 3. Update GamePanel      │
│ 4. Repeat                │
└──────────────────────────┘
    ↓
Server disconnects OR stopWatching() called
    ↓
Loop exits
    ↓
Thread terminates
```

**Message Processing Example:**
```java
// Received from server:
"playerJoined Alice FEMALE 3 3 NORTH"
    ↓
handlePlayerJoined()
    ↓
1. Parse: username="Alice", gender="FEMALE", pos=(3,3), dir="NORTH"
2. Convert gender string to enum
3. Call: gamePanel.addRemotePlayer(Alice, FEMALE, 3, 3, NORTH)
    ↓
GamePanel creates RemotePlayer
    ↓
Alice appears in Joe's game world!
```

**Why a separate thread?**
```
Main Thread (GamePanel):
├─ Update game state
├─ Render graphics (60 FPS)
└─ Handle user input

Network Thread (ServerMessageWatcher):
└─ Wait for server messages (blocking read)

If we did networking on main thread:
→ Game would freeze while waiting for messages ❌

With separate thread:
→ Game runs smoothly while messages arrive in background ✅
```

**Synchronization:**
GamePanel methods called by this thread must be thread-safe:
```java
// In GamePanel
public synchronized void addRemotePlayer(...) {
    remotePlayers.put(username, remotePlayer);
}

public synchronized void updateRemotePlayer(...) {
    RemotePlayer rp = remotePlayers.get(username);
    rp.updatePosition(...);
}
```

---

## 📡 Protocol Specification

### Message Format:
All messages are **text-based**, one message per line, space-separated tokens.

### Client → Server Messages:

| Message | Format | Example | Description |
|---------|--------|---------|-------------|
| **join** | `join <username> <gender> <mapX> <mapY> <direction>` | `join Joe MALE 4 2 SOUTH` | Player joins game |
| **move** | `move <mapX> <mapY> <direction> <inMovement>` | `move 5 3 EAST true` | Player moves |
| **chat** | `chat <text...>` | `chat Hello everyone!` | Player sends chat |
| **wantDetails** | `wantDetails <requesterAddr> <requesterPort>` | `wantDetails 127.0.0.1 54321` | Request player details |
| **detailsFor** | `detailsFor <targetAddr> <targetPort> <username> <gender> <mapX> <mapY> <direction>` | `detailsFor 127.0.0.1 54321 Joe MALE 4 2 SOUTH` | Send player details |
| **bye** | `bye` | `bye` | Player disconnects |

### Server → Client Messages:

| Message | Format | Example | Description |
|---------|--------|---------|-------------|
| **playerJoined** | `playerJoined <username> <gender> <mapX> <mapY> <direction>` | `playerJoined Alice FEMALE 3 3 NORTH` | Another player joined |
| **playerMoved** | `playerMoved <username> <mapX> <mapY> <direction> <inMovement>` | `playerMoved Alice 4 3 EAST true` | Another player moved |
| **playerChat** | `playerChat <username> <text...>` | `playerChat Alice Hello!` | Another player sent chat |
| **playerLeft** | `playerLeft <username>` | `playerLeft Alice` | Another player left |
| **wantDetails** | `wantDetails <requesterAddr> <requesterPort>` | `wantDetails 127.0.0.1 54322` | Server requests your details |
| **detailsFor** | `detailsFor <username> <gender> <mapX> <mapY> <direction>` | `detailsFor Bob MALE 2 2 WEST` | Details of another player |

---

## 🚀 Quick Start

### Step 1: Start Server

```bash
# Terminal 1 - Start server
cd /path/to/Futura
javac -d bin src/network/*.java
java -cp bin network.GameServer

# Output:
# Game Server starting on port 5555
# Server started successfully. Waiting for players...
```

### Step 2: Start First Client

```bash
# Terminal 2 - Player 1 (Joe)
java -cp bin main.Main "futura://open?user=Joe&gender=male"

# Server output:
# New connection from: 127.0.0.1:54321
# Received: join Joe MALE 4 2 SOUTH
# Player joined: Joe (Total players: 1)
```

### Step 3: Start Second Client

```bash
# Terminal 3 - Player 2 (Alice)
java -cp bin main.Main "futura://open?user=Alice&gender=female"

# Server output:
# New connection from: 127.0.0.1:54322
# Received: join Alice FEMALE 3 3 NORTH
# Player joined: Alice (Total players: 2)

# Joe's client output:
# Received from server: playerJoined Alice FEMALE 3 3 NORTH
# Added remote player: Alice

# Alice's client output:
# Received from server: wantDetails 127.0.0.1 54322
# Received from server: detailsFor Joe MALE 4 2 SOUTH
# Added remote player: Joe
```

### Step 4: Test Multiplayer

**In Joe's window:**
1. Click somewhere to move
2. Alice sees Joe moving in her window ✅

**In Alice's window:**
1. Type a chat message and press Enter
2. Joe sees Alice's message bubble ✅

---

## 💡 Usage Examples

### Example 1: Send Chat Message

```java
// In GamePanel or GameWindow
public void sendChat(String message) {
    // Add to local player's messages
    player.addMessage(message);
    
    // Send to network
    networkManager.sendChatMessage(message);
}

// What happens:
// 1. "chat Hello world!" → Server
// 2. Server → "playerChat Joe Hello world!" → All other clients
// 3. Other clients show Joe's chat bubble
```

### Example 2: Handle Player Movement

```java
// In Player.java
public void moveTo(int targetX, int targetY) {
    // Update local position
    movement.setTarget(targetX, targetY);
    
    // Notify network
    if (networkManager != null && networkManager.isConnected()) {
        networkManager.sendMoveMessage(
            targetX, 
            targetY, 
            direction.toString(), 
            true  // inMovement = true
        );
    }
}

// What happens:
// 1. "move 5 3 EAST true" → Server
// 2. Server → "playerMoved Joe 5 3 EAST true" → All other clients
// 3. Other clients update Joe's RemotePlayer position
```

### Example 3: Clean Disconnect

```java
// In GamePanel cleanup
public void cleanup() {
    if (networkManager != null && networkManager.isConnected()) {
        // Tell server we're leaving
        networkManager.sendByeMessage();
        
        // Close connection
        networkManager.disconnect();
    }
}

// What happens:
// 1. "bye" → Server
// 2. Server removes Joe from GameServerGroup
// 3. Server → "playerLeft Joe" → All other clients
// 4. Other clients remove Joe's RemotePlayer
```

---

## 🐛 Troubleshooting

### Problem: "Could not connect to server"

**Cause:** Server not running or wrong host/port

**Solution:**
```bash
# 1. Check if server is running
netstat -an | grep 5555

# 2. Start server
java -cp bin network.GameServer

# 3. Check SERVER_HOST in NetworkManager.java
private static final String SERVER_HOST = "localhost";  // Change if needed
```

---

### Problem: Players not seeing each other

**Cause:** Messages not being broadcast

**Debug:**
```bash
# Server terminal - should show:
Received: join Joe MALE 4 2 SOUTH
Player joined: Joe (Total players: 1)
Received: join Alice FEMALE 3 3 NORTH
Player joined: Alice (Total players: 2)

# Client terminal - should show:
Received from server: playerJoined Alice FEMALE 3 3 NORTH
Added remote player: Alice
```

**Check:**
1. Is `gamePanel.addRemotePlayer()` being called?
2. Are RemotePlayer objects being added to the map?
3. Is `RemotePlayer.draw()` being called in render loop?

---

### Problem: Chat not working

**Cause:** Chat messages not being sent/received

**Debug:**
```java
// Add logging in NetworkManager
public void sendChatMessage(String text) {
    System.out.println("SENDING: chat " + text);  // ← Add this
    out.println("chat " + text);
}

// Add logging in ServerMessageWatcher
private void handlePlayerChat(String message) {
    System.out.println("RECEIVED CHAT: " + message);  // ← Add this
    // ... rest of code ...
}
```

---

### Problem: Player positions out of sync

**Cause:** Move messages not being sent on every position change

**Solution:**
```java
// In Player.update()
if (movement.hasTarget() && movement.isMoving()) {
    // ... move player ...
    
    // Send move update EVERY frame
    if (networkManager != null && networkManager.isConnected()) {
        networkManager.sendMoveMessage(
            movement.xCurrent,
            movement.yCurrent,
            direction.toString(),
            true
        );
    }
}
```

**Note:** This sends A LOT of messages. Consider throttling (send every N frames or when position changes significantly).

---

## 🔒 Security Considerations

### ⚠️ Current Implementation:

This is a **prototype/learning implementation**. It has NO security features:

- ❌ No authentication (anyone can connect)
- ❌ No encryption (all data sent in plain text)
- ❌ No input validation (malicious clients can crash server)
- ❌ No rate limiting (spam attacks possible)
- ❌ No authorization (players can impersonate others)

### 🛡️ For Production Use, Add:

1. **Authentication:**
```java
// Server validates username/password before accepting join
public void handleJoin(String message) {
    String username = st.nextToken();
    String password = st.nextToken();  // ← Add this
    
    if (!authenticate(username, password)) {
        out.println("error Authentication failed");
        socket.close();
        return;
    }
    // ... rest of code ...
}
```

2. **Input Validation:**
```java
// Validate all input
private void handleMove(String message) {
    StringTokenizer st = new StringTokenizer(message);
    st.nextToken(); // skip "move"
    
    try {
        int mapX = Integer.parseInt(st.nextToken());
        int mapY = Integer.parseInt(st.nextToken());
        
        // VALIDATE: Check bounds
        if (mapX < 0 || mapX >= MAX_MAP_WIDTH || 
            mapY < 0 || mapY >= MAX_MAP_HEIGHT) {
            System.err.println("Invalid position from " + playerName);
            return;
        }
        
        // VALIDATE: Check distance (prevent teleporting)
        ClientInfo client = clientGroup.getClient(clientAddr, port);
        int distance = Math.abs(mapX - client.mapX) + Math.abs(mapY - client.mapY);
        if (distance > MAX_MOVE_DISTANCE) {
            System.err.println("Suspicious movement from " + playerName);
            return;
        }
        
        // ... rest of code ...
    } catch (NumberFormatException e) {
        System.err.println("Malformed move message from " + playerName);
        return;
    }
}
```

3. **Rate Limiting:**
```java
// Limit messages per second per client
private long lastMessageTime = 0;
private static final long MIN_MESSAGE_INTERVAL = 16; // ~60 FPS

private void processMessage(String message) {
    long now = System.currentTimeMillis();
    if (now - lastMessageTime < MIN_MESSAGE_INTERVAL) {
        // Too fast, ignore or warn
        return;
    }
    lastMessageTime = now;
    
    // ... process message ...
}
```

4. **Use TLS/SSL for encryption** (beyond scope of this README)

---

## 📊 Performance Considerations

### Message Frequency:

**Current:**
- Movement messages sent every frame (60/second per player)
- With 10 players = 600 messages/second

**Optimization:**
```java
// Only send if position actually changed
if (mapX != lastSentX || mapY != lastSentY) {
    networkManager.sendMoveMessage(mapX, mapY, direction, true);
    lastSentX = mapX;
    lastSentY = mapY;
}
```

### Bandwidth:

Average message sizes:
- join: ~50 bytes
- move: ~30 bytes
- chat: 30 + message length bytes

10 players, 60 FPS movement:
- 600 moves/sec × 30 bytes = 18 KB/sec = 144 Kbps

**Optimization:** Use binary protocol instead of text (save 50% bandwidth)

---

## 🔮 Future Enhancements

### Planned Features:
- [ ] **Rooms/Lobbies** - Multiple game rooms
- [ ] **Persistence** - Save player data to database
- [ ] **Reconnection** - Resume session after disconnect
- [ ] **Binary Protocol** - More efficient than text
- [ ] **UDP for movement** - Faster but less reliable (acceptable for movement)
- [ ] **Server authoritative** - Server validates all moves (prevent cheating)
- [ ] **Spectator mode** - Watch without playing
- [ ] **Admin commands** - Kick, ban, etc.

---

## 📖 Related Documentation

- **GamePanel Integration:** See `GamePanel.java` for network initialization
- **RemotePlayer:** See `Entity/RemotePlayer.java` for remote player rendering
- **Player:** See `Entity/Player.java` for local player network integration

---

## 🎓 Learning Resources

This implementation demonstrates:
- ✅ TCP socket programming
- ✅ Multi-threaded server (one thread per client)
- ✅ Producer-consumer pattern (message queues)
- ✅ Text-based protocol design
- ✅ Real-time game state synchronization

**For deeper learning:**
- Java Networking Tutorial: https://docs.oracle.com/javase/tutorial/networking/
- Game Networking Fundamentals: [Gaffer on Games](https://gafferongames.com/)
- Protocol Design Best Practices: [RFC 1149](https://www.ietf.org/rfc/rfc1149.txt) (joke, but illustrative)

---

## 🤝 Contributing

When extending this package:

1. **Add new message types** by:
   - Adding handler in `ClientHandler.processMessage()`
   - Adding handler in `ServerMessageWatcher.processMessage()`
   - Updating protocol specification in this README

2. **Test thoroughly:**
   - Single player (fallback when server unavailable)
   - Two players (basic multiplayer)
   - 5+ players (stress test)

3. **Document protocol changes** in this README

---

## 📝 Changelog

### v1.0 (Current)
- ✅ Basic multiplayer (join, move, chat, leave)
- ✅ Player discovery on join
- ✅ Broadcast messaging
- ✅ Thread-safe client management

---

**Built with ☕ for Futura Virtual World**

*Last updated: December 2024*