package network.clientHandler.commands;

import java.util.StringTokenizer;

import network.clientHandler.ClientContext;
import network.clientHandler.GameCommand;

public class DetailsForCommand implements GameCommand {
    
    @Override
    public void execute(String message, ClientContext ctx) {
        // Format FROM client: detailsFor <targetAddr> <targetPort> <username> <gender> <mapX> <mapY> <direction>
        // Format TO target:   detailsFor <username> <gender> <mapX> <mapY> <direction>
        
        StringTokenizer st = new StringTokenizer(message);
        st.nextToken(); // skip "detailsFor"
        
        String targetAddr = st.nextToken();
        int targetPort = Integer.parseInt(st.nextToken());
        
        // Get remaining tokens (username, gender, mapX, mapY, direction)
        String username = st.nextToken();
        String gender = st.nextToken();
        String mapX = st.nextToken();
        String mapY = st.nextToken();
        String direction = st.nextToken();
        
        // Route to target (WITHOUT addr/port)
        String forwardMessage = "detailsFor " + username + " " + gender + " " + 
                                mapX + " " + mapY + " " + direction;
        
        ctx.clientGroup.sendTo(targetAddr, targetPort, forwardMessage);
        
        System.out.println("Routed details to " + targetAddr + ":" + targetPort + " - " + forwardMessage);
    }
}