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
    private BufferedReader in; 
    private ClientContext context;
    
    public ClientHandler(Socket socket, GameServerGroup clientGroup) {
        this.socket = socket;
        
        try {
            in = new BufferedReader(
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

     // ═══════════════════════════════════════════════════════════
    // THIS WAS MISSING - The thread's main loop!
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received from " + context.clientAddr + ":" + 
                                   context.port + " - " + message);
                processMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + context.clientAddr + ":" + context.port);
        } finally {
            cleanup();
        }
    }
    
    private void processMessage(String message) {
        int spaceIndex = message.indexOf(' ');
        String commandName = (spaceIndex == -1) ? message : message.substring(0, spaceIndex);
        
        GameCommand command = registry.getCommand(commandName);
        command.execute(message, context);
    }

      // ═══════════════════════════════════════════════════════════
    // ALSO MISSING - Cleanup when client disconnects
    // ═══════════════════════════════════════════════════════════
    
    private void cleanup() {
        try {
            if (context.playerName != null) {
                context.clientGroup.broadcastToRoom(
                    context.currentRoomId, 
                    context.clientAddr, 
                    context.port,
                    "playerLeft " + context.playerName
                );
                context.clientGroup.removeClient(context.clientAddr, context.port);
                System.out.println(context.playerName + " disconnected from room: " + 
                                   context.currentRoomId);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}