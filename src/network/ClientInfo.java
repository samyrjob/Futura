package network;

import java.io.PrintWriter;

public class ClientInfo {
    
    public PrintWriter out;
    public String address;
    public int port;
    public String playerName;
    public String gender;
    public int mapX;
    public int mapY;
    public String direction;
    
    public ClientInfo(PrintWriter out, String address, int port, String playerName, 
                     String gender, int mapX, int mapY, String direction) {
        this.out = out;
        this.address = address;
        this.port = port;
        this.playerName = playerName;
        this.gender = gender;
        this.mapX = mapX;
        this.mapY = mapY;
        this.direction = direction;
    }
    
    public boolean matches(String addr, int p) {
        return address.equals(addr) && port == p;
    }
    
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    
    public void updatePosition(int x, int y, String dir) {
        this.mapX = x;
        this.mapY = y;
        this.direction = dir;
    }
}
