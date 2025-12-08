package admin;

import java.io.PrintWriter;
import java.net.Socket;

import network.GameServerGroup;

public class AdminContext {
    
    public final PrintWriter out;
    public final GameServerGroup clientGroup;
    public final Socket socket;
    public final String clientIP;
    public boolean authenticated;
    
    public AdminContext(PrintWriter out, GameServerGroup clientGroup, Socket socket) {
        this.out = out;
        this.clientGroup = clientGroup;
        this.socket = socket;
        this.clientIP = socket.getInetAddress().getHostAddress();
        this.authenticated = false;
    }
    
    public void send(String message) {
        out.println(message);
    }
}