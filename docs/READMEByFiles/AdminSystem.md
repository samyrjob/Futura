# Futura Admin System

**(EXCEPT FOR KICK AND MOVE PLAYER ACTIONS WHICH ARE DOONE THROUGH SHARED FILE SYSTEM admin_actions.dat)**

## Overview

The Admin System provides remote server management capabilities for the Futura multiplayer game. It uses a **server-to-server architecture** where an Admin Server runs alongside the Game Server, sharing access to the same game state while maintaining separation of concerns.

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                     GAME SERVER PROCESS                          │
│                                                                  │
│  ┌────────────────────┐         ┌─────────────────────────────┐  │
│  │    GameServer      │         │     GameServerGroup         │  │
│  │    (Port 5555)     │────────▶│   (Shared State)            │  │
│  │                    │         │                             │  │
│  │  • Player traffic  │         │  • Connected clients list   │  │
│  │  • Game commands   │         │  • Room assignments         │  │
│  │  • Chat messages   │         │  • Player positions         │  │
│  └────────────────────┘         │  • Broadcast methods        │  │
│                                 └─────────────────────────────┘  │
│                                           ▲                      │
│  ┌────────────────────┐                   │                      │
│  │    AdminServer     │───────────────────┘                      │
│  │    (Port 5001)     │    Direct memory access                  │
│  │                    │    (same JVM process)                    │
│  │  • Admin traffic   │                                          │
│  │  • Management cmds │                                          │
│  │  • Authenticated   │                                          │
│  └────────────────────┘                                          │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
         ▲                                        ▲
         │ TCP :5555                              │ TCP :5001
         │                                        │
┌────────┴────────┐                    ┌──────────┴──────────┐
│   Game Clients  │                    │    Admin Client     │
│   (Players)     │                    │    (You)            │
└─────────────────┘                    └─────────────────────┘
```

---

## How Server-to-Server Communication Works

### The Key Insight

The AdminServer and GameServer run in the **same Java process**, which means they share the same memory space. This allows the AdminServer to directly access and modify the `GameServerGroup` instance that holds all connected players.

### Flow Example: Kicking a Player

```
1. Admin Client sends: "KICK alice"
         │
         ▼
2. AdminServer receives command
         │
         ▼
3. AdminHandler processes via KickPlayerCommand
         │
         ▼
4. KickPlayerCommand accesses shared GameServerGroup
         │
         ▼
5. GameServerGroup.getClientByName("alice") returns ClientInfo
         │
         ▼
6. ClientInfo.sendMessage("KICKED You have been kicked")
         │
         ▼
7. GameServerGroup.broadcastToRoom(..., "playerLeft alice")
         │
         ▼
8. GameServerGroup.removeClient(alice)
         │
         ▼
9. Admin Client receives: "SUCCESS Kicked player: alice"
```

### Why This Works

```java
// In GameServer.main()
GameServerGroup clientGroup = new GameServerGroup();  // Created once

// Game server uses it
ClientHandler clientHandler = new ClientHandler(socket, clientGroup);

// Admin server uses the SAME instance
AdminServer adminServer = new AdminServer(clientGroup);  // Same reference!
```

Both servers reference the **same object in memory**, so changes made by AdminServer are immediately visible to GameServer and vice versa.

---

## Package Structure

```
src/
├── network/
│   ├── GameServer.java           # Main server entry point
│   ├── GameServerGroup.java      # Shared state (clients, rooms)
│   ├── ClientInfo.java           # Individual client data
│   ├── NetworkManager.java       # Client-side networking
│   ├── ServerMessageWatcher.java # Client-side message handler
│   └── clientHandler/
│       ├── ClientHandler.java    # Handles player connections
│       ├── ClientContext.java    # Player session context
│       ├── GameCommand.java      # Command interface
│       ├── CommandRegistry.java  # Maps commands to handlers
│       └── commands/
│           ├── JoinCommand.java
│           ├── MoveCommand.java
│           ├── ChatCommand.java
│           └── ...
│
├── admin/
│   ├── AdminMain.java            # Admin client entry point
│   ├── AdminNetworkClient.java   # Admin client networking
│   ├── AdminServer.java          # Admin server (runs with GameServer)
│   ├── AdminHandler.java         # Handles admin connections
│   ├── AdminContext.java         # Admin session context
│   ├── AdminCommand.java         # Admin command interface
│   ├── AdminCommandRegistry.java # Maps admin commands
│   ├── commands/
│   │   ├── ListPlayersCommand.java
│   │   ├── ListRoomsCommand.java
│   │   ├── RoomInfoCommand.java
│   │   ├── KickPlayerCommand.java
│   │   ├── MovePlayerCommand.java
│   │   ├── ClearRoomCommand.java
│   │   └── BroadcastCommand.java
│   └── ui/
│       ├── AdminFrame.java       # Main window
│       ├── LoginPanel.java       # Authentication UI
│       ├── DashboardPanel.java   # Main dashboard
│       ├── PlayersPanel.java     # Player management
│       ├── RoomsPanel.java       # Room management
│       ├── BroadcastPanel.java   # Message broadcasting
│       └── ConsolePanel.java     # Raw command console
```

---

## Setup & Configuration

### 1. Server Configuration

In `AdminServer.java`:

```java
private static final int ADMIN_PORT = 5001;              // Admin listening port
private static final String ADMIN_SECRET = "your-key";   // Authentication key
```

### 2. Starting the Servers

The GameServer automatically starts the AdminServer:

```java
// In GameServer.main()
public static void main(String[] args) {
    GameServerGroup clientGroup = new GameServerGroup();
    
    // Start Admin Server (runs in separate thread)
    AdminServer adminServer = new AdminServer(clientGroup);
    adminServer.start();
    
    // Start Game Server (main thread)
    // ... accept player connections
}
```

### 3. Running

```bash
# Step 1: Start the server (includes both Game + Admin servers)
java network.GameServer

# Step 2: Start game clients (players)
java main.Main

# Step 3: Start admin client (separate application)
java admin.AdminMain
```

---

## Authentication Flow

```
Admin Client                          Admin Server
     │                                     │
     │──── TCP Connect ───────────────────▶│
     │                                     │
     │◀─── "ADMIN_SERVER_READY" ──────────│
     │◀─── "AUTH_REQUIRED" ────────────────│
     │                                     │
     │──── "AUTH your-secret-key" ────────▶│
     │                                     │
     │                              ┌──────┴──────┐
     │                              │ Key matches? │
     │                              └──────┬──────┘
     │                                     │
     │◀─── "AUTH_SUCCESS" ─────────────────│  (if yes)
     │◀─── "AUTH_FAILED" ──────────────────│  (if no)
     │                                     │
     │──── Commands... ───────────────────▶│  (only if authenticated)
```

---

## Available Admin Commands

| Command | Description | Example |
|---------|-------------|---------|
| `PING` | Test connection | `PING` → `PONG` |
| `HELP` | List commands | `HELP` |
| `LIST_PLAYERS` | Get all connected players | `LIST_PLAYERS` |
| `LIST_ROOMS` | Get all active rooms | `LIST_ROOMS` |
| `ROOM_INFO <roomId>` | Get room details | `ROOM_INFO lobby` |
| `KICK <username>` | Disconnect a player | `KICK alice` |
| `MOVE_PLAYER <user> <room>` | Force player to room | `MOVE_PLAYER alice lobby` |
| `CLEAR_ROOM <roomId>` | Empty a room | `CLEAR_ROOM party-room` |
| `BROADCAST <message>` | Message all players | `BROADCAST Server restarting!` |

---

## Protocol Reference

### Request/Response Formats

**LIST_PLAYERS**
```
→ LIST_PLAYERS
← PLAYERS_START
← COUNT 3
← PLAYER alice lobby 4 2
← PLAYER bob lobby 5 3
← PLAYER charlie party 2 1
← PLAYERS_END
```

**LIST_ROOMS**
```
→ LIST_ROOMS
← ROOMS_START
← COUNT 2
← ROOM lobby 2
← ROOM party 1
← ROOMS_END
```

**ROOM_INFO**
```
→ ROOM_INFO lobby
← ROOM_INFO_START
← ROOM_ID lobby
← STATUS ACTIVE
← PLAYER_COUNT 2
← PLAYER_DETAIL alice FEMALE 4 2 SOUTH
← PLAYER_DETAIL bob MALE 5 3 EAST
← ROOM_INFO_END
```

**KICK**
```
→ KICK alice
← SUCCESS Kicked player: alice
```

**BROADCAST**
```
→ BROADCAST Hello everyone!
← SUCCESS Broadcast sent
```

---

## Client-Side Handling

Game clients must handle admin-initiated messages. Add these to `ServerMessageWatcher.java`:

```java
private void processMessage(String message) {
    // ... existing handlers ...
    
    // Admin-initiated events
    else if (message.startsWith("forceRoomChange")) {
        handleForceRoomChange(message);
    } else if (message.startsWith("adminMessage")) {
        handleAdminMessage(message);
    } else if (message.startsWith("KICKED")) {
        handleKicked(message);
    }
}
```

---

## Security Considerations

### Current Implementation (Basic)

- Secret key authentication
- Single shared key for all admins

### Recommended Improvements

1. **IP Whitelist**
   ```java
   private static final List<String> ALLOWED_IPS = Arrays.asList(
       "127.0.0.1",
       "192.168.1.100"
   );
   
   // In AdminServer.run()
   if (!ALLOWED_IPS.contains(clientIP)) {
       socket.close();
       continue;
   }
   ```

2. **Per-User Authentication**
   ```java
   private static final Map<String, String> ADMIN_USERS = Map.of(
       "admin", "password123",
       "moderator", "modpass456"
   );
   ```

3. **Rate Limiting**
   - Limit login attempts
   - Cooldown after failed auth

4. **Audit Logging**
   ```java
   private void logAction(String admin, String command) {
       logger.info("[AUDIT] " + admin + " executed: " + command);
   }
   ```

5. **TLS/SSL Encryption**
   - Use `SSLServerSocket` for encrypted connections

---

## Extending the System

### Adding a New Command

1. **Create command class:**
   ```java
   package admin.commands;
   
   import admin.AdminCommand;
   import admin.AdminContext;
   
   public class MyNewCommand implements AdminCommand {
       @Override
       public void execute(String message, AdminContext context) {
           // Parse message
           // Do something with context.clientGroup
           // Send response via context.send()
       }
   }
   ```

2. **Register in AdminCommandRegistry:**
   ```java
   public AdminCommandRegistry() {
       // ... existing commands ...
       register("MY_COMMAND", new MyNewCommand());
   }
   ```

3. **Update Admin Client UI** (if needed)

---

## Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| "Connection refused" | Server not running | Start GameServer first |
| "Auth failed" | Wrong secret key | Check ADMIN_SECRET matches |
| "Port in use" | Another process on 5001 | Change ADMIN_PORT |
| Commands not working | Not authenticated | Check auth flow completed |
| Players not appearing | Wrong room filter | Check room ID spelling |

---

## Example Session

```
$ java admin.AdminMain

[Connect to localhost:5001]
[Enter secret key: your-secret-key-123]
[Click Connect]

> PING
PONG

> LIST_PLAYERS
PLAYERS_START
COUNT 2
PLAYER alice lobby 4 2
PLAYER bob lobby 5 3
PLAYERS_END

> BROADCAST Welcome to the server!
SUCCESS Broadcast sent

> KICK bob
SUCCESS Kicked player: bob

> LIST_PLAYERS
PLAYERS_START
COUNT 1
PLAYER alice lobby 4 2
PLAYERS_END
```

---

## License

Part of the Futura game project.

---

## Contact

For questions or issues, contact the development team.