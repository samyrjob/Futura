package admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import network.GameServerGroup;

public class AdminHandler extends Thread {
    
    private Socket socket;
    private BufferedReader in;
    private AdminContext context;
    private AdminCommandRegistry registry;
    private String secretKey;
    
    public AdminHandler(Socket socket, GameServerGroup clientGroup, String secretKey) {
        this.socket = socket;
        this.secretKey = secretKey;
        this.registry = new AdminCommandRegistry();
        
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            this.context = new AdminContext(out, clientGroup, socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        try {
            context.send("ADMIN_SERVER_READY");
            context.send("AUTH_REQUIRED");
            
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("[ADMIN] Received: " + message);
                
                // Handle authentication first
                if (!context.authenticated) {
                    if (message.startsWith("AUTH ")) {
                        String providedKey = message.substring(5);
                        if (providedKey.equals(secretKey)) {
                            context.authenticated = true;
                            context.send("AUTH_SUCCESS");
                            System.out.println("[ADMIN] Client authenticated");
                        } else {
                            context.send("AUTH_FAILED");
                            System.out.println("[ADMIN] Authentication failed");
                        }
                    } else {
                        context.send("ERROR Not authenticated");
                    }
                    continue;
                }
                
                // Process admin commands
                processCommand(message);
            }
        } catch (IOException e) {
            System.out.println("[ADMIN] Client disconnected");
        } finally {
            cleanup();
        }
    }
    
    private void processCommand(String message) {
        int spaceIndex = message.indexOf(' ');
        String commandName = (spaceIndex == -1) ? message : message.substring(0, spaceIndex);
        
        AdminCommand command = registry.getCommand(commandName);
        command.execute(message, context);
    }
    
    private void cleanup() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}