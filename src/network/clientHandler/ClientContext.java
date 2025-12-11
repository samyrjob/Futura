// Context object holds everything commands might need
package network.clientHandler;
import java.io.PrintWriter;
import java.net.Socket;

import main.GameConstants;
import network.GameServerGroup;

public class ClientContext {
    public final PrintWriter out;
    public final GameServerGroup clientGroup;
    public final String clientAddr;
    public final int port;
    public final Socket socket;
    public String playerName;
    public String currentRoomId;
    
    public ClientContext(PrintWriter out, GameServerGroup clientGroup, 
                         String clientAddr, int port, Socket socket) {
        this.out = out;
        this.clientGroup = clientGroup;
        this.clientAddr = clientAddr;
        this.port = port;
        this.currentRoomId = GameConstants.LOBBY_ROOM_ID;
        this.socket = socket;
    }
}