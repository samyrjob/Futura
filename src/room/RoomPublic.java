package room;

import java.util.HashMap;
import java.util.Map;

import main.GamePanel;

public class RoomPublic {

      private GamePanel gp;
      private Map<String, Room> publicRooms;
         
      // Default lobby room
      private static final String LOBBY_ID = "lobby";


      public RoomPublic(GamePanel gp) {
        this.gp = gp;
        this.publicRooms = new HashMap<>();
    
        
        // Load saved public rooms
        loadPubicRooms();
        
    }




       /**
     * Create the default lobby room
     */
    private void createLobby() {
        Room lobby = new Room("Lobby", "System", gp.maxWorldCol, gp.maxWorldRow);
        lobby.setRoomType(Room.RoomType.PUBLIC);
        lobby.setMaxPlayers(50);
        
        // Load lobby tile layout from existing map
        int[][] lobbyTiles = loadDefaultTileLayout();
        lobby.setTileMap(lobbyTiles);
        
        publicRooms.put(LOBBY_ID, lobby);
        System.out.println("Lobby created");
    }


        /**
     * Load default tile layout from current TileManager
     */
    private int[][] loadDefaultTileLayout() {
        // Copy current tile layout
        int[][] layout = new int[gp.maxWorldCol][gp.maxWorldRow];
        
        if (gp.tile_manager != null && gp.tile_manager.mapTileNum != null) {
            for (int col = 0; col < gp.maxWorldCol; col++) {
                for (int row = 0; row < gp.maxWorldRow; row++) {
                    layout[col][row] = gp.tile_manager.mapTileNum[col][row];
                }
            }
        } else {
            // Default: all grass
            for (int col = 0; col < gp.maxWorldCol; col++) {
                for (int row = 0; row < gp.maxWorldRow; row++) {
                    layout[col][row] = 1;
                }
            }
        }
        
        return layout;
    }
    
}


