package admin;

import java.util.HashMap;
import java.util.Map;

import admin.commands.*;

public class AdminCommandRegistry {
    
    private final Map<String, AdminCommand> commands = new HashMap<>();
    private final AdminCommand unknownCommand = (msg, ctx) -> {
        ctx.send("ERROR Unknown command: " + msg);
    };
    
    public AdminCommandRegistry() {
        register("LIST_PLAYERS", new ListPlayersCommand());
        register("LIST_ROOMS", new ListRoomsCommand());
        register("ROOM_INFO", new RoomInfoCommand());
        register("CLEAR_ROOM", new ClearRoomCommand());
        register("MOVE_PLAYER", new MovePlayerCommand());
        register("KICK", new KickPlayerCommand());
        register("BROADCAST", new BroadcastCommand());
        register("ROOM_INFO", new RoomInfoCommand());
        register("PING", (msg, ctx) -> ctx.send("PONG"));
        register("HELP", (msg, ctx) -> {
        ctx.send("COMMANDS: LIST_PLAYERS, LIST_ROOMS, ROOM_INFO <roomId>, " +
                "CLEAR_ROOM <roomId>, MOVE_PLAYER <user> <room>, " +
                "KICK <user>, BROADCAST <msg>, PING, HELP");
    });
    }
    
    public void register(String name, AdminCommand command) {
        commands.put(name, command);
    }
    
    public AdminCommand getCommand(String name) {
        return commands.getOrDefault(name, unknownCommand);
    }
}