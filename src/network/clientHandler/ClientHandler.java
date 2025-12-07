// Simplified ClientHandler

package network.clientHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import network.GameServerGroup;

public class ClientHandler extends Thread {
    
    private static final CommandRegistry registry = new CommandRegistry();
    
    private Socket socket;
    private ClientContext context;
    
    public ClientHandler(Socket socket, GameServerGroup clientGroup) {
        this.socket = socket;
        
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            this.context = new ClientContext(
                out, clientGroup,
                socket.getInetAddress().getHostAddress(),
                socket.getPort(), socket
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void processMessage(String message) {
        int spaceIndex = message.indexOf(' ');
        String commandName = (spaceIndex == -1) ? message : message.substring(0, spaceIndex);
        
        GameCommand command = registry.getCommand(commandName);
        command.execute(message, context);
    }
}