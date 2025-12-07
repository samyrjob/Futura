package network.clientHandler.commands;

import network.clientHandler.ClientContext;
import network.clientHandler.GameCommand;

public class DetailsForCommand implements GameCommand {
    
    @Override
    public void execute(String message, ClientContext ctx) {
        // Just forward to client
        ctx.out.println(message);
    }
}
