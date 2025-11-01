// package main;

// import Entity.Player;
// import Entity.Entity.Gender;
// import message.ChatBox;
// import Entity.Player.Message;
// import message.Profile;
// import mouse.HandleMouseHover;
// import mouse.MyMouseAdapter;
// import tile.TileManager;
// import java.awt.event.MouseEvent;
// import java.awt.event.MouseListener;
// import java.awt.event.MouseMotionAdapter;
// import java.util.ArrayList;
// import java.util.List;

// import javax.swing.*;
// import java.awt.*;
// import java.awt.event.MouseAdapter;

// public class GamePanel extends JPanel implements Runnable {






//     private int previousTileX = -1;
//     private int previousTileY = -1;

//     public int hoveredTileX =-1;
//     public int hoveredTileY=-1;


//     final int originalTileSize = 16;
//     final int size = 6;
//     public final int tileSizeWidth = originalTileSize * size; // 96
//     public final int tileSizeHeight = originalTileSize * size / 2; // 48

//     // full screen SETTINGS
//     public final int maxScreenRow = 16;
//     public final int maxScreenCol = 16;
//     public final int screenWidth = maxScreenCol * tileSizeWidth; // = 16*96 = 1537
//     public final int screenHeight = maxScreenRow * tileSizeHeight;  // = 8*96 = 768

//     ;    // WORLD SETTINGS :
//     public final int maxWorldCol = 9;
//     public final int maxWorldRow = 5;
//     public final int worldWidth = maxWorldCol * tileSizeWidth;  //  = 9*96 = 864
//     public final int worldHeight = maxWorldRow * tileSizeHeight; //  = 5 * 96 = 480
// //


//     // HANDLE HOUSE HOVER

//     HandleMouseHover handleMouseHover = new HandleMouseHover(this);

//     public int mouseOverTileX;
//     public int mouseOverTileY;

//     public int mouseX = -1;
//     public int mouseY = -1;

// //    public int coordinate_playerX_drawn = 768 - tileSizeWidth;
// //    public int coordinate_playerY_drawn = 384 - 3*tileSizeHeight;


// //    INITIAL tileCenterX and tileCenterY :
// //        int tileCenterX = 768;
// //        int tileCenterY = 384;





//     final int FPS = 60;

//     Thread gameThread;
//     KeyHandler key_handler = new KeyHandler();
//     public TileManager tile_manager = new TileManager(this);
//     MyMouseAdapter mouse_adapter = new MyMouseAdapter(this);
//     public Player player;
//     Profile profile;
//     Boolean displayProfile = false;


//     // the Song class 
//     Sound sound = new Sound();
//     Sound se = new Sound();

//     // UI COMPONENT
//     UI ui = new UI(this);

    
//     ChatBox chatbox;
 







//     private Point calculateTileFromMouse(int mouseX, int mouseY) {
//         int adjustedX = mouseX - tile_manager.xOffset;
//         int adjustedY = mouseY - tile_manager.yOffset;
//         int mapX = (adjustedX / (tileSizeWidth / 2) + adjustedY / (tileSizeHeight / 2)) / 2;
//         int mapY = (adjustedY / (tileSizeHeight / 2) - adjustedX / (tileSizeWidth / 2)) / 2;
//         return new Point(mapX, mapY);
//     }

//     public Point getCalculateTileFromMouse(int x, int y){
//         return calculateTileFromMouse(x, y);
//     }



    


//     public GamePanel(String username, String genderStr) {


//         this.setLayout(null);
//         // this.setLayout(FlowLayout);
//         Gender gender = genderStr.equalsIgnoreCase("female") ? Gender.FEMALE : Gender.MALE;
//         this.player = new Player(this, mouse_adapter, username, gender);

//         chatbox = new ChatBox(this, player);
//         profile = new Profile(this, player);


//         this.setPreferredSize(new Dimension(screenWidth, screenHeight));
//         this.setBackground(Color.BLACK);
//         this.setDoubleBuffered(true);
//         this.addKeyListener(key_handler);
//         this.setFocusable(true);

        
    
        
       
//         try {
//             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
        
   

      

      

       




//         // HOVER OF THE MOUSE ONTO A TILE
//         this.addMouseListener(mouse_adapter);
//         this.addMouseMotionListener(mouse_adapter);


//         // Mouse position tracking
//         addMouseListener(new MouseAdapter() {
//             @Override
//             public void mouseClicked(MouseEvent e) {
//                 int mouseX = e.getX();
//                 int mouseY = e.getY();

//                 Point tilePoint = calculateTileFromMouse(mouseX, mouseY);

//                 mouseOverTileX = tilePoint.x;
//                 mouseOverTileY = tilePoint.y;

//                 if (!(e.getX() >= player.spriteX && e.getX() <= player.spriteX + player.currentSprite.getWidth() && e.getY() >= player.spriteY && e.getY() <= player.spriteY + player.currentSprite
//                 .getHeight())){

//                     //                 Update hovered tile if within bounds
//                                     if (mouseOverTileX >= 0 && mouseOverTileY >= 0 && mouseOverTileX < maxWorldCol && mouseOverTileY < maxWorldRow) {
//                                         if (mouseOverTileX == previousTileX && mouseOverTileY == previousTileY) {
//                                             // Do nothing: the player continues its current movement
//                                             System.out.println("Clicked on the same tile, ignoring...");
//                                         } else {
//                                             hoveredTileX = mouseOverTileX;
//                                             hoveredTileY = mouseOverTileY;
//                                             System.out.println("mouseOverTileX = " + hoveredTileX + " mouseOverTileY = " + hoveredTileY);
//                                             player.setInitialPosition(player.xCurrent, player.yCurrent);
//                                             player.setFinalPosition(hoveredTileX, hoveredTileY);
//                                             player.in_movement = true;
//                                             player.moveStartTime = System.nanoTime();
                    
//                                             previousTileX = hoveredTileX;
//                                             previousTileY = hoveredTileY;
                    
                    
//                                         }
//                                     }
//                 }
//             }
//         });

        
//          // Mouse Drag Listener for scrolling
//         addMouseListener(new MouseAdapter() {
//             @Override
//             public void mousePressed(java.awt.event.MouseEvent e) {
//                 chatbox.lastMouseY = e.getY(); // Store initial mouse position
//                 chatbox.draggingMessage = chatbox.isClickOnMessage(e.getX(), e.getY());
//             }
//         });

//         addMouseMotionListener(new MouseMotionAdapter() {
          
//             @Override
//             public void mouseDragged(java.awt.event.MouseEvent e) {
//                 if (!chatbox.draggingMessage) return; // Only scroll if a message was clicked


//                 int deltaY = e.getY() - chatbox.lastMouseY;
//                 chatbox.lastMouseY = e.getY(); // Update last mouse position

//                 chatbox.scrollY += deltaY;

//                 int maxScrollY = 800;
//                 int minScrollY = -100;

//                 Message lastMessage = player.messages.get(player.messages.size() - 1);

//                 // Apply the new scrollY within the bounds
//                 if (chatbox.scrollY < 0){
//                     chatbox.scrollY = Math.max(chatbox.scrollY, minScrollY);
//                 } else if (chatbox.scrollY > 0){
//                     chatbox.scrollY = Math.min(chatbox.scrollY,  maxScrollY);
//                 }

//                 repaint(); // Update UI
//             }

//         });



//         // here the fact to display the ui when we click on the sprite otherwie close it with a button to close ! 
//         addMouseListener(new MouseAdapter() {
//             public void mouseClicked(java.awt.event.MouseEvent e){
//                 if (player.contains(e.getX(), e.getY())) {
//                     displayProfile = !displayProfile;
// }
//             }
//         });


//     }


  



//     public void startGameThread() {
//         gameThread = new Thread(this);
//         gameThread.start();
//         System.out.println("Game thread started");
//     }

//     public void setupGame(){
//         chatbox.setChatBox();
//         playSong(0);
//     }


//     @Override
//     public void run() {
//         double drawInterval = 1_000_000_000 / FPS;
//         long lastime = System.nanoTime();
//         long currentTime;
//         double delta = 0;
//         long timer = 0;
//         int drawCount = 0;

//         while (gameThread != null) {
//             currentTime = System.nanoTime();
//             delta += (currentTime - lastime) / drawInterval;
//             timer += (currentTime - lastime);
//             lastime = currentTime;
//             if (delta >= 1) {

//                 //! to print the direction and coordinates of the sprite
// //                 System.out.println("xinitial = " + player.xInitial + " yinitial = " + player.yInitial + "    xfinal = " + player.xFinal + "    yfinal = " + player.yFinal);
// //                 System.out.println("xmap current = " + player.xCurrent  + " ymap current " + player.yCurrent);
// // //                System.out.println("mouseX = " + mouseX + " mouseY = " + mouseY);
// //                 System.out.println(player.direction);
// //                 System.out.println("spriteX = " + player.spriteX + " spriteY = " + player.spriteY);


//                 System.out.println("height is :   " + this.getHeight());
//                 update();
//                 repaint();
//                 delta--;
//                 drawCount++;
//             }
//             if (timer >= 1_000_000_000) {
// //                System.out.println("FPS : " + drawCount);
//                 drawCount = 0;
//                 timer = 0;
//             }

//         }
//     }
// //
// //
//     public void update() {
//         player.update();


//     }


// // Drawing logic
//     @Override
//     public void paintComponent(Graphics g) {
//         super.paintComponent(g);
//         Graphics2D g2d = (Graphics2D) g;
//         // PAINT TILE
//         tile_manager.draw(g2d);


//         /// TO DRAW THE POLYGON (edges on a tile)
//         handleMouseHover.drawPolygon(mouseX, mouseY, g);


//         //  PAINT PLAYER
//         player.draw_player(g2d);
// //        System.out.println("mouseOverTileX = " + mouseOverTileX + " mouseOverTileY = " + mouseOverTileY);
// //

//         ui.draw(g2d);

    

//         if (displayProfile){
//             profile.draw(g2d);

//         }
//         chatbox.draw(g2d);


//         // setHenderingHint is to hide the bad visual of pixels, to be smooth graphically and visually more beautiful
//         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        

        
//         // g2d.setColor(Color.GRAY);
//         // g2d.drawRect(10 + 75, getHeight() - 40, 300, 30);
//         // g2d.setColor(Color.WHITE);
//         // g2d.fillRect(10 + 75, getHeight() - 40, 300, 30);
//         // g2d.fillRoundRect(320 + 75, getHeight() - 40, 80, 30, 35, 35);



        

//       for (Message msg : player.messages) {
//           int adjustedY = msg.y + chatbox.scrollY; // Adjust messages with scrolling
//           if (msg.y < -400) continue;

//           g2d.setColor(Color.WHITE);
//           g2d.fillRoundRect(10, adjustedY, msg.text.length() * 7 + 20, 30, 15, 15);
//           g2d.setColor(Color.BLACK);
//           g2d.setFont(new Font("Arial", Font.PLAIN, 14)); // Adjust size as needed
//           g2d.drawString(msg.text, 20, adjustedY + 20);
//           msg.adjustedY = adjustedY;
//       }






//         g2d.dispose();


//     }


//     public void playSong(int i){
//         sound.setFile(i);
//         sound.play();
//         sound.loop();

//     }

//     public void stopSong (){
//         sound.stop();
        
//     }

//     public void playSE(int i){
//         se.setFile(i);
//         se.play();
//     }


    




// }




//! second draft

package main;

import Entity.Player;
import Entity.RemotePlayer;
import Entity.Entity.Gender;
import message.ChatBox;
import Entity.Player.Message;
import message.Profile;
import mouse.HandleMouseHover;
import mouse.MyMouseAdapter;
import network.NetworkManager;
import tile.TileManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

public class GamePanel extends JPanel implements Runnable {


    private int previousTileX = -1;
    private int previousTileY = -1;

    public int hoveredTileX =-1;
    public int hoveredTileY=-1;


    final int originalTileSize = 16;
    final int size = 6;
    public final int tileSizeWidth = originalTileSize * size; // 96
    public final int tileSizeHeight = originalTileSize * size / 2; // 48

    // full screen SETTINGS
    public final int maxScreenRow = 16;
    public final int maxScreenCol = 16;
    public final int screenWidth = maxScreenCol * tileSizeWidth; // = 16*96 = 1537
    public final int screenHeight = maxScreenRow * tileSizeHeight;  // = 8*96 = 768

    ;    // WORLD SETTINGS :
    public final int maxWorldCol = 9;
    public final int maxWorldRow = 5;
    public final int worldWidth = maxWorldCol * tileSizeWidth;  //  = 9*96 = 864
    public final int worldHeight = maxWorldRow * tileSizeHeight; //  = 5 * 96 = 480

    // HANDLE HOUSE HOVER
    HandleMouseHover handleMouseHover = new HandleMouseHover(this);

    public int mouseOverTileX;
    public int mouseOverTileY;

    public int mouseX = -1;
    public int mouseY = -1;

    final int FPS = 60;

    Thread gameThread;
    KeyHandler key_handler = new KeyHandler();
    public TileManager tile_manager = new TileManager(this);
    MyMouseAdapter mouse_adapter = new MyMouseAdapter(this);
    public Player player;
    Profile profile;
    Boolean displayProfile = false;

    // the Song class 
    Sound sound = new Sound();
    Sound se = new Sound();

    // UI COMPONENT
    UI ui = new UI(this);

    ChatBox chatbox;
    
    // MULTIPLAYER COMPONENTS
    private NetworkManager networkManager;
    private Map<String, RemotePlayer> remotePlayers;
    private boolean multiplayerEnabled = false;


    private Point calculateTileFromMouse(int mouseX, int mouseY) {
        int adjustedX = mouseX - tile_manager.xOffset;
        int adjustedY = mouseY - tile_manager.yOffset;
        int mapX = (adjustedX / (tileSizeWidth / 2) + adjustedY / (tileSizeHeight / 2)) / 2;
        int mapY = (adjustedY / (tileSizeHeight / 2) - adjustedX / (tileSizeWidth / 2)) / 2;
        return new Point(mapX, mapY);
    }

    public Point getCalculateTileFromMouse(int x, int y){
        return calculateTileFromMouse(x, y);
    }


    public GamePanel(String username, String genderStr) {
        this.setLayout(null);
        Gender gender = genderStr.equalsIgnoreCase("female") ? Gender.FEMALE : Gender.MALE;
        this.player = new Player(this, mouse_adapter, username, gender);

        chatbox = new ChatBox(this, player);
        profile = new Profile(this, player);

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(key_handler);
        this.setFocusable(true);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Initialize multiplayer components
        remotePlayers = new HashMap<>();
        networkManager = new NetworkManager(this);
        player.setNetworkManager(networkManager);

        // HOVER OF THE MOUSE ONTO A TILE
        this.addMouseListener(mouse_adapter);
        this.addMouseMotionListener(mouse_adapter);

        // Mouse position tracking
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();

                Point tilePoint = calculateTileFromMouse(mouseX, mouseY);

                mouseOverTileX = tilePoint.x;
                mouseOverTileY = tilePoint.y;

                if (!(e.getX() >= player.spriteX && e.getX() <= player.spriteX + player.currentSprite.getWidth() && e.getY() >= player.spriteY && e.getY() <= player.spriteY + player.currentSprite
                .getHeight())){

                    //                 Update hovered tile if within bounds
                                    if (mouseOverTileX >= 0 && mouseOverTileY >= 0 && mouseOverTileX < maxWorldCol && mouseOverTileY < maxWorldRow) {
                                        if (mouseOverTileX == previousTileX && mouseOverTileY == previousTileY) {
                                            // Do nothing: the player continues its current movement
                                            System.out.println("Clicked on the same tile, ignoring...");
                                        } else {
                                            hoveredTileX = mouseOverTileX;
                                            hoveredTileY = mouseOverTileY;
                                            System.out.println("mouseOverTileX = " + hoveredTileX + " mouseOverTileY = " + hoveredTileY);
                                            player.setInitialPosition(player.xCurrent, player.yCurrent);
                                            player.setFinalPosition(hoveredTileX, hoveredTileY);
                                            player.in_movement = true;
                                            player.moveStartTime = System.nanoTime();
                    
                                            previousTileX = hoveredTileX;
                                            previousTileY = hoveredTileY;
                    
                    
                                        }
                                    }
                }
            }
        });

        
         // Mouse Drag Listener for scrolling
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                chatbox.lastMouseY = e.getY(); // Store initial mouse position
                chatbox.draggingMessage = chatbox.isClickOnMessage(e.getX(), e.getY());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
          
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (!chatbox.draggingMessage) return; // Only scroll if a message was clicked

                int deltaY = e.getY() - chatbox.lastMouseY;
                chatbox.lastMouseY = e.getY(); // Update last mouse position

                chatbox.scrollY += deltaY;

                int maxScrollY = 800;
                int minScrollY = -100;

                Message lastMessage = player.messages.get(player.messages.size() - 1);

                // Apply the new scrollY within the bounds
                if (chatbox.scrollY < 0){
                    chatbox.scrollY = Math.max(chatbox.scrollY, minScrollY);
                } else if (chatbox.scrollY > 0){
                    chatbox.scrollY = Math.min(chatbox.scrollY,  maxScrollY);
                }

                repaint(); // Update UI
            }

        });

        // here the fact to display the ui when we click on the sprite otherwie close it with a button to close ! 
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e){
                if (player.contains(e.getX(), e.getY())) {
                    displayProfile = !displayProfile;
                }
            }
        });
    }


    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
        System.out.println("Game thread started");
    }

    public void setupGame(){
        chatbox.setChatBox();
        playSong(0);
        
        // Connect to multiplayer server
        connectToServer();
    }
    
    private void connectToServer() {
        if (networkManager.connect()) {
            multiplayerEnabled = true;
            // Send join message
            networkManager.sendJoinMessage(
                player.name,
                player.gender.toString(),
                player.xCurrent,
                player.yCurrent,
                player.direction.toString()
            );
            System.out.println("Connected to multiplayer server");
        } else {
            multiplayerEnabled = false;
            System.out.println("Running in single-player mode");
        }
    }
    
    // Methods for managing remote players
    public synchronized void addRemotePlayer(String username, Gender gender, int mapX, int mapY, String direction) {
        if (!remotePlayers.containsKey(username)) {
            RemotePlayer remotePlayer = new RemotePlayer(this, username, gender, mapX, mapY, direction);
            remotePlayers.put(username, remotePlayer);
            System.out.println("Added remote player: " + username);
        }
    }
    
    public synchronized void updateRemotePlayer(String username, int mapX, int mapY, String direction, boolean inMovement) {
        RemotePlayer remotePlayer = remotePlayers.get(username);
        if (remotePlayer != null) {
            remotePlayer.updatePosition(mapX, mapY, direction, inMovement);
        }
    }
    
    public synchronized void removeRemotePlayer(String username) {
        remotePlayers.remove(username);
        System.out.println("Removed remote player: " + username);
    }
    
    public synchronized void addRemotePlayerChat(String username, String text) {
        // For now, just add to local player's messages
        // You could enhance this to show who sent the message
        player.messages.add(new Message(username + ": " + text, getHeight() - 95));
    }


    @Override
    public void run() {
        double drawInterval = 1_000_000_000 / FPS;
        long lastime = System.nanoTime();
        long currentTime;
        double delta = 0;
        long timer = 0;
        int drawCount = 0;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastime) / drawInterval;
            timer += (currentTime - lastime);
            lastime = currentTime;
            if (delta >= 1) {

                System.out.println("height is :   " + this.getHeight());
                update();
                repaint();
                delta--;
                drawCount++;
            }
            if (timer >= 1_000_000_000) {
                drawCount = 0;
                timer = 0;
            }

        }
    }

    public void update() {
        player.update();
        
        // Update all remote players
        synchronized (remotePlayers) {
            for (RemotePlayer remotePlayer : remotePlayers.values()) {
                remotePlayer.update();
            }
        }
    }


// Drawing logic
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // PAINT TILE
        tile_manager.draw(g2d);

        /// TO DRAW THE POLYGON (edges on a tile)
        handleMouseHover.drawPolygon(mouseX, mouseY, g);

        //  PAINT LOCAL PLAYER
        player.draw_player(g2d);
        
        // PAINT REMOTE PLAYERS
        synchronized (remotePlayers) {
            for (RemotePlayer remotePlayer : remotePlayers.values()) {
                remotePlayer.draw(g2d);
            }
        }

        ui.draw(g2d);

        if (displayProfile){
            profile.draw(g2d);
        }
        
        chatbox.draw(g2d);

        // setHenderingHint is to hide the bad visual of pixels, to be smooth graphically and visually more beautiful
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Message msg : player.messages) {
            int adjustedY = msg.y + chatbox.scrollY; // Adjust messages with scrolling
            if (msg.y < -400) continue;

            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(10, adjustedY, msg.text.length() * 7 + 20, 30, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 14)); // Adjust size as needed
            g2d.drawString(msg.text, 20, adjustedY + 20);
            msg.adjustedY = adjustedY;
        }

        g2d.dispose();
    }


    public void playSong(int i){
        sound.setFile(i);
        sound.play();
        sound.loop();
    }

    public void stopSong (){
        sound.stop();
    }

    public void playSE(int i){
        se.setFile(i);
        se.play();
    }
    
    public void cleanup() {
        if (networkManager != null && multiplayerEnabled) {
            networkManager.sendByeMessage();
            networkManager.disconnect();
        }
    }
}

