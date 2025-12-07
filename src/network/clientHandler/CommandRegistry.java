package network;

import java.util.HashMap;
import java.util.Map;

// Command registry
public class CommandRegistry {
    
    private final Map<String, GameCommand> commands = new HashMap<>();
    private final GameCommand unknownCommand = (msg, ctx) -> {
        System.out.println("Unknown command: " + msg);
        ctx.out.println("error Unknown command");
    };
    
    public CommandRegistry() {
        // Register all commands
        register("join", new JoinCommand());
        register("move", new MoveCommand());
        register("chat", new ChatCommand());
        register("changeRoom", new ChangeRoomCommand());
        register("leaveRoom", new LeaveRoomCommand());
        register("wantDetails", new WantDetailsCommand());
        register("detailsFor", new DetailsForCommand());
        register("bye", new ByeCommand());
    }
    
    public void register(String name, GameCommand command) {
        commands.put(name, command);
    }
    
    public GameCommand getCommand(String name) {
        return commands.getOrDefault(name, unknownCommand);
    }
}