**RoomController** = The main brain that coordinates everything. It uses all the other classes we discussed.

---

**Visual:**

```
┌─────────────────────────────────────────────────────────────────┐
│                       RoomController                            │
│                      (The Coordinator)                          │
│                                                                 │
│   ┌─────────────┐  ┌─────────────────┐  ┌──────────────────┐   │
│   │ RoomCache   │  │ ListenerManager │  │ FavoritesManager │   │
│   │             │  │                 │  │                  │   │
│   │ Stores      │  │ Notifies UI     │  │ Tracks ⭐ rooms  │   │
│   │ room data   │  │ components      │  │                  │   │
│   └─────────────┘  └─────────────────┘  └──────────────────┘   │
│                                                                 │
│   ┌─────────────┐  ┌─────────────────┐  ┌──────────────────┐   │
│   │ RoomApi     │  │ WebSocketClient │  │ GamePanel        │   │
│   │ Client      │  │                 │  │                  │   │
│   │ HTTP calls  │  │ Live updates    │  │ The game itself  │   │
│   └─────────────┘  └─────────────────┘  └──────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

**Each section explained:**

**1. Fields (Dependencies):**

```java
private GamePanel gp;                    // The game window
private RoomApiClient apiClient;         // HTTP calls to Spring Boot
private RoomCache cache;                 // Local room storage
private RoomListenerManager listenerManager;   // Observer pattern
private RoomFavoritesManager favoritesManager; // Favorites
private RoomWebSocketClient webSocketClient;   // Live updates

private Room currentRoom;      // Room player is in NOW
private String currentRoomId;  // ID of current room
```

---

**2. Initialization:**

```java
public void initialize(String username) {
    apiClient.setCurrentUsername(username);  // Set who we are
    cache.refresh();                          // Load rooms from server
    
    Room lobby = cache.get("lobby");
    if (lobby != null) {
        enterRoom("lobby", username);         // Auto-join lobby
    }
}
```

```
Player logs in as "samsamsam"
         │
         ▼
┌─────────────────────┐
│ initialize()        │
│                     │
│ 1. Set username     │
│ 2. Fetch all rooms  │
│ 3. Enter lobby      │
└─────────────────────┘
```

---

**3. Room Access (Simple getters):**

```java
public Room getCurrentRoom() { return currentRoom; }
public Room getRoom(String roomId) { return cache.get(roomId); }
public List<Room> getPublicRooms() { return cache.getPublicRooms(); }
public List<Room> getMyRooms(String username) { return cache.getByOwner(username); }
public List<Room> getFavoriteRooms() { return cache.getByIds(favoritesManager.getAll()); }
```

Just delegates to `RoomCache` and `RoomFavoritesManager`.

---

**4. Enter/Leave Room (Core logic):**

```java
public boolean enterRoom(String roomId, String username) {
    Room room = cache.get(roomId);           // 1. Get room
    
    if (room == null) return false;          // 2. Validate exists
    if (!room.canEnter(username)) return false; // 3. Validate access
    
    if (currentRoomId != null) {
        leaveCurrentRoom();                  // 4. Leave old room first
    }
    
    boolean success = apiClient.enterRoom(roomId); // 5. Tell server
    
    if (success) {
        currentRoom = room;
        currentRoomId = roomId;
        updateGameForRoom();                 // 6. Update game visuals
        listenerManager.notifyRoomEntered(room); // 7. Notify UI
    }
    
    return success;
}
```

```
Player clicks "Enter Beach Room"
              │
              ▼
┌──────────────────────────────────────────────────────────┐
│                     enterRoom()                          │
│                                                          │
│  1. cache.get("beach")         → Get room data           │
│  2. room.canEnter("sam")       → Check permission        │
│  3. leaveCurrentRoom()         → Leave lobby             │
│  4. apiClient.enterRoom()      → Tell Spring Boot        │
│  5. updateGameForRoom()        → Load map, spawn player  │
│  6. listenerManager.notify()   → Update UI panels        │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

---

**5. Update Game State:**

```java
private void updateGameForRoom() {
    // 1. Load room's tile map
    if (currentRoom.getTileMap() != null) {
        gp.tile_manager.setMapTileNum(currentRoom.getTileMap());
    } else {
        gp.tile_manager.loadMap("/res/maps/map01.txt");
    }
    
    // 2. Spawn player at corner (Habbo style!)
    gp.player.setPosition(0, 0);
    
    // 3. Clear other players (they're in old room)
    gp.removeAllRemotePlayers();
    
    // 4. Tell game server we changed rooms
    gp.networkManager.sendRoomChange(currentRoomId);
    
    // 5. Refresh screen
    gp.repaint();
}
```

```
┌─────────────────────────────────────────┐
│          updateGameForRoom()            │
│                                         │
│  Before:              After:            │
│  ┌─────────┐         ┌─────────┐        │
│  │ LOBBY   │         │ BEACH   │        │
│  │         │   →     │         │        │
│  │   😀    │         │ 😀      │        │
│  │  center │         │ corner  │        │
│  └─────────┘         └─────────┘        │
│                                         │
└─────────────────────────────────────────┘
```

---

**6. Create/Delete Room:**

```java
public Room createRoom(String roomName, String ownerUsername) {
    Room newRoom = apiClient.createRoom(roomName);  // Tell server
    
    if (newRoom != null) {
        cache.put(newRoom);                    // Add to local cache
        listenerManager.notifyRoomCreated(newRoom); // Update UI
    }
    
    return newRoom;
}

public boolean deleteRoom(String roomId, String username) {
    Room room = cache.get(roomId);
    
    if (room == null || !room.isOwner(username)) {
        return false;  // Only owner can delete!
    }
    
    boolean success = apiClient.deleteRoom(roomId);  // Tell server
    
    if (success) {
        cache.remove(roomId);           // Remove from cache
        favoritesManager.remove(roomId); // Remove from favorites
        listenerManager.notifyRoomDeleted(room); // Update UI
    }
    
    return success;
}
```

---

**7. Shutdown (Cleanup):**

```java
public void shutdown() {
    stopLiveUpdates();       // Disconnect WebSocket
    leaveCurrentRoom();      // Leave room properly
    listenerManager.clear(); // Remove all listeners
    cache.clear();           // Clear room data
    favoritesManager.clear(); // Clear favorites
}
```

Called when player logs out or closes game.

---

**Full flow example:**

```
Player: "I want to enter Beach Room"
                    │
                    ▼
┌─────────────────────────────────────────────────────────────┐
│                    RoomController                           │
│                                                             │
│  enterRoom("beach", "samsamsam")                            │
│         │                                                   │
│         ├──► cache.get("beach")                             │
│         │         └──► Returns Room object                  │
│         │                                                   │
│         ├──► room.canEnter("samsamsam")                     │
│         │         └──► Returns true (public room)           │
│         │                                                   │
│         ├──► leaveCurrentRoom()                             │
│         │         └──► apiClient.leaveRoom("lobby")         │
│         │         └──► listenerManager.notifyRoomLeft()     │
│         │                                                   │
│         ├──► apiClient.enterRoom("beach")                   │
│         │         └──► HTTP POST to Spring Boot             │
│         │                                                   │
│         ├──► updateGameForRoom()                            │
│         │         └──► Load beach map tiles                 │
│         │         └──► Spawn player at (0,0)                │
│         │         └──► Clear remote players                 │
│         │         └──► networkManager.sendRoomChange()      │
│         │                                                   │
│         └──► listenerManager.notifyRoomEntered()            │
│                   └──► RoomNavigator updates UI             │
│                   └──► ChatPanel shows "Welcome!"           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

**Summary:**

| Role | Description |
|------|-------------|
| **Coordinator** | Brings all components together |
| **Delegates** | Uses Cache, Listeners, Favorites, API |
| **Single entry point** | UI only talks to RoomController |
| **Business logic** | Validates, orchestrates, updates game |

**This is the "C" in MVC** (Model-View-Controller)!