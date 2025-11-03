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
    public final int tileSizeWidth = originalTileSize * size;
    public final int tileSizeHeight = originalTileSize * size / 2;

    public final int maxScreenRow = 16;
    public final int maxScreenCol = 16;
    public final int screenWidth = maxScreenCol * tileSizeWidth;
    public final int screenHeight = maxScreenRow * tileSizeHeight;

    public final int maxWorldCol = 9;
    public final int maxWorldRow = 5;
    public final int worldWidth = maxWorldCol * tileSizeWidth;
    public final int worldHeight = maxWorldRow * tileSizeHeight;

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

    Sound sound = new Sound();
    Sound se = new Sound();

    UI ui = new UI(this);

    ChatBox chatbox;

    private Timer messageTimer;
    
    // MULTIPLAYER COMPONENTS
    public NetworkManager networkManager;  // Made public so Main.java can access it
    private Map<String, RemotePlayer> remotePlayers;
    private boolean multiplayerEnabled = false;
    
    // Chat message scrolling

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
        
        //! Start message animation timer (moves messages up)
      


        // messageTimer = new Timer(50, e -> {  // Update every 50ms (smooth!)
        //     for (Entity.Player.Message msg : player.messages) {
        //         //! to change the speed of the bubble
        //         msg.y -= 1; // Move up 2 pixels per frame (smooth)
        //     }
        //     // Remove messages that went off screen
        //     player.messages.removeIf(m -> m.y < -100);
        //     repaint();
        // });
        messageTimer = new Timer(100, e -> {
            // Move local player messages
            for (Entity.Player.Message msg : player.messages) {
                msg.y -= 1;
            }
            player.messages.removeIf(m -> m.y < -100);
            
            // ✨ NEW: Move remote players' messages too!
            synchronized (remotePlayers) {
                for (RemotePlayer rp : remotePlayers.values()) {
                    for (RemotePlayer.Message msg : rp.messages) {
                        msg.y -= 1;
                    }
                    rp.messages.removeIf(m -> m.y < -100);
                }
            }
            
            repaint();
        });
        messageTimer.start();

        // HOVER OF THE MOUSE ONTO A TILE
        this.addMouseListener(mouse_adapter);
        this.addMouseMotionListener(mouse_adapter);

        //! Mouse click for movement
       
        // Mouse position tracking - FIXED VERSION
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();

                Point tilePoint = calculateTileFromMouse(mouseX, mouseY);

                mouseOverTileX = tilePoint.x;
                mouseOverTileY = tilePoint.y;

                // FIXED: Use smaller, precise hitbox instead of full image size
                int drawnWidth = 2 * tileSizeWidth;   // 192 pixels
                int drawnHeight = 4 * tileSizeHeight; // 192 pixels
                
                // Make hitbox smaller (only character body)
                int hitboxWidth = (int)(drawnWidth * 0.4);   // 40% width
                int hitboxHeight = (int)(drawnHeight * 0.5); // 50% height
                
                // Center horizontally, position at bottom
                int hitboxX = player.spriteX + (drawnWidth - hitboxWidth) / 2;
                int hitboxY = player.spriteY + drawnHeight - hitboxHeight;
                
                // Check if click is NOT on sprite
                boolean clickedOnSprite = (mouseX >= hitboxX && 
                                          mouseX <= hitboxX + hitboxWidth &&
                                          mouseY >= hitboxY && 
                                          mouseY <= hitboxY + hitboxHeight);

                if (!clickedOnSprite) {
                    // Click was on a tile, not on sprite - MOVE!
                    if (mouseOverTileX >= 0 && mouseOverTileY >= 0 && 
                        mouseOverTileX < maxWorldCol && mouseOverTileY < maxWorldRow) {
                        
                        if (mouseOverTileX == previousTileX && mouseOverTileY == previousTileY) {
                            System.out.println("Clicked on the same tile, ignoring...");
                        } else {
                            hoveredTileX = mouseOverTileX;
                            hoveredTileY = mouseOverTileY;
                            System.out.println("Moving to tile: " + hoveredTileX + ", " + hoveredTileY);
                            
                            // Use pathfinding to move (if you have it)
                            player.moveTo(hoveredTileX, hoveredTileY);
                    
                            previousTileX = hoveredTileX;
                            previousTileY = hoveredTileY;
                        }
                    }
                }
            }
        });

        // Profile display on sprite click
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
        playSong(0);
        
        // Connect to multiplayer server
        connectToServer();
    }
    
    private void connectToServer() {
        if (networkManager.connect()) {
            multiplayerEnabled = true;
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
    
    //! some changes
    // public synchronized void addRemotePlayerChat(String username, String text) {
    //     // Add chat message from remote player (will float up on screen)
    //     player.messages.add(new Entity.Player.Message(username + ": " + text, getHeight() - 95));
    //     repaint();
    // }

    public synchronized void addRemotePlayerChat(String username, String text) {
        // Find the remote player and add message to THEIR list
        RemotePlayer remotePlayer = remotePlayers.get(username);
        if (remotePlayer != null) {
            // Start bubble at THEIR sprite head (not bottom of screen!)
            int bubbleStartY = remotePlayer.spriteY + 50;
            remotePlayer.messages.add(new RemotePlayer.Message(username + ": " + text, bubbleStartY));
        }
        repaint();
    }
    
    public void sendChatToNetwork(String text) {
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.sendChatMessage(text);
        }
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
        // ✨ NEW: DRAW REMOTE PLAYERS' CHAT BUBBLES
        //! DRAW REMOTE PLAYERS' CHAT BUBBLES (ONE LINE ONLY!)
        synchronized (remotePlayers) {
            for (RemotePlayer remotePlayer : remotePlayers.values()) {
                for (RemotePlayer.Message msg : remotePlayer.messages) {
                    if (msg.y < -50) continue;
                    
                    String displayText = msg.text;
                    
                    // ✨ LIMIT: Max 200 characters
                    if (displayText.length() > 200) {
                        displayText = displayText.substring(0, 200);
                    }
                    
                    //! bubble width to change after
                    // int bubbleWidth = Math.max(displayText.length() * 10 + 40, 900);
                    // REPLACE THIS (for both local and remote players):
                    // int bubbleWidth = Math.min(displayText.length() * 8 + 30, 420);

                    // WITH THIS:
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(displayText);
                    int bubbleWidth = textWidth + 50; // 40px for padding, max 450px
                    int bubbleHeight = 35;
                    
                    int bubbleX = remotePlayer.spriteX + (2 * tileSizeWidth) - 30;
                    int bubbleY = msg.y;
                    
                    g2d.setColor(Color.WHITE);
                    g2d.fillRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 20, 20);
                    
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 20, 20);
                    
                    g2d.setFont(new Font("Arial", Font.BOLD, 14));
                    
                    int colonIndex = displayText.indexOf(":");
                    if (colonIndex > 0) {
                        String namePart = displayText.substring(0, colonIndex + 1);
                        String messagePart = displayText.substring(colonIndex + 1);
                        
                        g2d.setColor(new Color(0, 102, 204));
                        g2d.drawString(namePart, bubbleX + 15, bubbleY + 22);
                        
                        int nameWidth = g2d.getFontMetrics().stringWidth(namePart);
                        
                        g2d.setColor(Color.BLACK);
                        g2d.drawString(messagePart, bubbleX + 15 + nameWidth, bubbleY + 22);
                    } else {
                        g2d.setColor(Color.BLACK);
                        g2d.drawString(displayText, bubbleX + 15, bubbleY + 22);
                    }
                    
                    int[] xPoints = {bubbleX, bubbleX - 10, bubbleX};
                    int[] yPoints = {bubbleY + 10, bubbleY + 17, bubbleY + 24};
                    g2d.setColor(Color.WHITE);
                    g2d.fillPolygon(xPoints, yPoints, 3);
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawPolyline(xPoints, yPoints, 3);
                }
            }
        }

        ui.draw(g2d);

        if (displayProfile){
            profile.draw(g2d);
        }

        //! DRAW CHAT MESSAGES (floating up from bottom)
       
       // DRAW CHAT MESSAGES (Habbo Hotel style - ONE LINE ONLY!)
        for (Entity.Player.Message msg : player.messages) {
            if (msg.y < -50) continue;
            
            String displayText = msg.text;
            
            // ✨ LIMIT: Max 200 characters (like Habbo!)
            if (displayText.length() > 200) {
                displayText = displayText.substring(0, 200);
            }
            
            //! Calculate bubble size
            // int bubbleWidth = Math.min(displayText.length() * 8 + 30, 420);
            // REPLACE THIS (for both local and remote players):
            // int bubbleWidth = Math.min(displayText.length() * 8 + 30, 420);

            // WITH THIS:
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(displayText);
            int bubbleWidth = textWidth + 50; // 40px for padding, max 450px
            int bubbleHeight = 35;
            
            // Position bubble
            int bubbleX = player.spriteX + (2 * tileSizeWidth) - 30;
            int bubbleY = msg.y;
            
            // Draw white bubble
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 20, 20);
            
            // Draw border
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 20, 20);
            
            // Draw text with styled name (ONE LINE!)
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            
            int colonIndex = displayText.indexOf(":");
            if (colonIndex > 0) {
                String namePart = displayText.substring(0, colonIndex + 1);
                String messagePart = displayText.substring(colonIndex + 1);
                
                // Draw name in blue
                g2d.setColor(new Color(0, 102, 204));
                g2d.drawString(namePart, bubbleX + 15, bubbleY + 22);
                
                int nameWidth = g2d.getFontMetrics().stringWidth(namePart);
                
                // Draw message in black
                g2d.setColor(Color.BLACK);
                g2d.drawString(messagePart, bubbleX + 15 + nameWidth, bubbleY + 22);
            } else {
                g2d.setColor(Color.BLACK);
                g2d.drawString(displayText, bubbleX + 15, bubbleY + 22);
            }
            
            // Draw pointer triangle
            int[] xPoints = {bubbleX, bubbleX - 10, bubbleX};
            int[] yPoints = {bubbleY + 10, bubbleY + 17, bubbleY + 24};
            g2d.setColor(Color.WHITE);
            g2d.fillPolygon(xPoints, yPoints, 3);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawPolyline(xPoints, yPoints, 3);
        }



        // Rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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