// Command interface
package network.clientHandler;
@FunctionalInterface
public interface GameCommand {
    void execute(String message, ClientContext context);
}