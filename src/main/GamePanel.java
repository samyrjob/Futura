package main;

import Entity.Player;
import Entity.RemotePlayer;
import Entity.Entity.Direction;
import Entity.Entity.Gender;
import message.ChatBox;

import message.Profile;
import message.RemoteProfile;
import message.InventoryWindow;
import mouse.HandleMouseHover;
import mouse.MyMouseAdapter;
import network.NetworkManager;
import tile.TileManager;
import object.FurnitureManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
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
    RemoteProfile remoteProfile;
    Boolean displayProfile = false;

    //! pause Sound
    Sound sound = new Sound();
    Sound se = new Sound();

    UI ui = new UI(this);

    ChatBox chatbox;
    
    // ✨ NEW: Furniture and Inventory system
    public FurnitureManager furnitureManager;
    public InventoryWindow inventoryWindow;

    private Timer messageTimer;
    
    // MULTIPLAYER COMPONENTS
    public NetworkManager networkManager;
    private Map<String, RemotePlayer> remotePlayers;
    private boolean multiplayerEnabled = false;

    // for dragging the room
    private boolean isDragging = false;
    private int dragStartX = 0;
    private int dragStartY = 0;
    private int originalXOffset;
    private int originalYOffset;
    


    private Point calculateTileFromMouse(int mouseX, int mouseY) {
        // Adjust for tile offset
        int adjustedX = mouseX - tile_manager.xOffset;
        int adjustedY = mouseY - tile_manager.yOffset;
        
   
        
        float isoX = (float) adjustedX / (tileSizeWidth / 2);
        float isoY = (float) adjustedY / (tileSizeHeight / 2);
        
        int mapX = (int) Math.floor((isoX + isoY) / 2);
        int mapY = (int) Math.floor((isoY - isoX) / 2);
        
        // Debug output
        System.out.println("Mouse: (" + mouseX + ", " + mouseY + ") → Tile: (" + mapX + ", " + mapY + ")");
        
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
        remoteProfile = new RemoteProfile(this);
        
        // ✨ NEW: Initialize furniture system
        furnitureManager = new FurnitureManager(this);
        inventoryWindow = new InventoryWindow(this);

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
        
        messageTimer = new Timer(100, e -> {
            // Move local player messages
            for (Entity.Player.Message msg : player.messages) {
                msg.y -= 1;
            }
            player.messages.removeIf(m -> m.y < -100);
            
            // Move remote players' messages
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

        // Mouse listener for drag functionality and inventory
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ✨ NEW: Check if clicking inventory window first
                if (inventoryWindow.isVisible()) {
                    inventoryWindow.handleClick(e.getX(), e.getY());
                    repaint();
                    return;
                }
                
                // ✨ NEW: Handle placement mode
                if (inventoryWindow.isPlacementMode()) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        // Left click = confirm placement
                        inventoryWindow.confirmPlacement();
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        // Right click = cancel placement
                        inventoryWindow.cancelPlacement();
                    }
                    repaint();
                    return;
                }
                
                // Right-click or middle-click to drag
                if (e.getButton() == MouseEvent.BUTTON1) {
                    isDragging = true;
                    dragStartX = e.getX();
                    dragStartY = e.getY();
                    originalXOffset = tile_manager.xOffset;
                    originalYOffset = tile_manager.yOffset;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    isDragging = false;
                    setCursor(Cursor.getDefaultCursor());
                }
                
                // ✨ NEW: Handle inventory window dragging
                inventoryWindow.handleRelease();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                // Only handle left-click for interactions when NOT dragging
                if (e.getButton() != MouseEvent.BUTTON1) return;
                
                int mouseX = e.getX();
                int mouseY = e.getY();
                
                // Check if clicked on LOCAL player (show own profile)
                if (player.contains(mouseX, mouseY)) {
                    displayProfile = !displayProfile;
                    remoteProfile.hideProfile();
                    return;
                }
                
                // Check if clicked on any REMOTE player
                boolean clickedOnRemotePlayer = false;
                synchronized (remotePlayers) {
                    for (RemotePlayer remotePlayer : remotePlayers.values()) {
                        if (remotePlayer.contains(mouseX, mouseY)) {
                            clickedOnRemotePlayer = true;
                            
                            Direction newDirection = player.calculateDirectionToTarget(
                                remotePlayer.xCurrent, 
                                remotePlayer.yCurrent
                            );
                            
                            player.faceDirection(newDirection);
                            
                            if (networkManager != null && networkManager.isConnected()) {
                                networkManager.sendMoveMessage(
                                    player.movement.xCurrent, 
                                    player.movement.yCurrent, 
                                    player.direction.toString(), 
                                    false
                                );
                            }
                            
                            remoteProfile.toggleProfile(remotePlayer);
                            displayProfile = false;
                            
                            System.out.println("Clicked on remote player: " + remotePlayer.name);
                            return;
                        }
                    }
                }
                
                // If clicked on empty tile (not on any sprite)
                if (!clickedOnRemotePlayer) {
                    Point tilePoint = calculateTileFromMouse(mouseX, mouseY);
                    mouseOverTileX = tilePoint.x;
                    mouseOverTileY = tilePoint.y;
                    
                    if (mouseOverTileX >= 0 && mouseOverTileY >= 0 && 
                        mouseOverTileX < maxWorldCol && mouseOverTileY < maxWorldRow) {
                        
                        if (mouseOverTileX == previousTileX && mouseOverTileY == previousTileY) {
                            System.out.println("Clicked on the same tile, ignoring...");
                        } else {
                            hoveredTileX = mouseOverTileX;
                            hoveredTileY = mouseOverTileY;
                            System.out.println("Moving to tile: " + hoveredTileX + ", " + hoveredTileY);
                            
                            player.moveTo(hoveredTileX, hoveredTileY);
                            
                            previousTileX = hoveredTileX;
                            previousTileY = hoveredTileY;
                            
                            displayProfile = false;
                            remoteProfile.hideProfile();
                        }
                    }
                }
            }
        });

        // Keep existing mouse_adapter for hover functionality
        this.addMouseMotionListener(mouse_adapter);

        // Mouse motion listener for dragging
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // ✨ NEW: Handle inventory window dragging
                if (inventoryWindow.isVisible()) {
                    inventoryWindow.handleDrag(e.getX(), e.getY());
                    repaint();
                    return;
                }
                
                int deltaX = e.getX() - dragStartX;
                int deltaY = e.getY() - dragStartY;
                
                if (!isDragging && (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5)) {
                    isDragging = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
                
                if (isDragging) {
                    tile_manager.xOffset = originalXOffset + deltaX;
                    tile_manager.yOffset = originalYOffset + deltaY;
                    
                    // player.spriteX = player.conversion_from_mapXY_to_spriteX(player.movement.xCurrent, player.movement.yCurrent);
                    // player.spriteY = player.conversion_from_mapXY_to_spriteY(player.movement.xCurrent, player.movement.yCurrent);
                    
                    synchronized (remotePlayers) {
                        for (RemotePlayer remotePlayer : remotePlayers.values()) {
                            remotePlayer.updateSpritePosition();
                        }
                    }
                    
                    constrainMapOffsets();
                    repaint();
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                // ✨ NEW: Update placement preview
                if (inventoryWindow.isPlacementMode()) {
                    inventoryWindow.updatePlacementPreview(e.getX(), e.getY());
                    repaint();
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
        //! pause the song please
        // playSong(0);
        connectToServer();
    }
    
    private void connectToServer() {
        if (networkManager.connect()) {
            multiplayerEnabled = true;
            networkManager.sendJoinMessage(
                player.name,
                player.gender.toString(),
                player.movement.xCurrent,
                player.movement.yCurrent,
                player.direction.toString()
            );
            System.out.println("Connected to multiplayer server");
        } else {
            multiplayerEnabled = false;
            System.out.println("Running in single-player mode");
        }
    }
    
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
        RemotePlayer remotePlayer = remotePlayers.get(username);
        if (remotePlayer != null) {
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
        
        synchronized (remotePlayers) {
            for (RemotePlayer remotePlayer : remotePlayers.values()) {
                remotePlayer.update();
            }
        }
    }

    private void constrainMapOffsets() {
        int minXOffset = -worldWidth + screenWidth / 2;
        int maxXOffset = screenWidth / 2;
        int minYOffset = -worldHeight + screenHeight / 2;
        int maxYOffset = screenHeight / 2;
        
        tile_manager.xOffset = Math.max(minXOffset, Math.min(maxXOffset, tile_manager.xOffset));
        tile_manager.yOffset = Math.max(minYOffset, Math.min(maxYOffset, tile_manager.yOffset));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // PAINT TILE
        tile_manager.draw(g2d);

        // ✨ NEW: Draw placed furniture
        furnitureManager.draw(g2d);

        // TO DRAW THE POLYGON (edges on a tile)
        handleMouseHover.drawPolygon(mouseX, mouseY, g);

        // PAINT LOCAL PLAYER
        player.draw(g2d);
        
        // PAINT REMOTE PLAYERS
        synchronized (remotePlayers) {
            for (RemotePlayer remotePlayer : remotePlayers.values()) {
                remotePlayer.draw(g2d);
            }
        }
        
        // DRAW REMOTE PLAYERS' CHAT BUBBLES
        synchronized (remotePlayers) {
            for (RemotePlayer remotePlayer : remotePlayers.values()) {
                for (RemotePlayer.Message msg : remotePlayer.messages) {
                    if (msg.y < -50) continue;
                    
                    String displayText = msg.text;
                    if (displayText.length() > 200) {
                        displayText = displayText.substring(0, 200);
                    }
                    
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(displayText);
                    int bubbleWidth = textWidth + 50;
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

        if (remoteProfile.isVisible()) {
            remoteProfile.draw(g2d);
        }

        // DRAW CHAT MESSAGES
        for (Entity.Player.Message msg : player.messages) {
            if (msg.y < -50) continue;
            
            String displayText = msg.text;
            if (displayText.length() > 200) {
                displayText = displayText.substring(0, 200);
            }
            
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(displayText);
            int bubbleWidth = textWidth + 50;
            int bubbleHeight = 35;
            
            int bubbleX = player.spriteX + (2 * tileSizeWidth) - 30;
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

        // ✨ NEW: Draw placement preview
        inventoryWindow.drawPlacementPreview(g2d);
        
        // ✨ NEW: Draw inventory window (on top of everything)
        inventoryWindow.draw(g2d);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.dispose();
    }


    //! pause songs please
    // public void playSong(int i){
    //     sound.setFile(i);
    //     sound.play();
    //     sound.loop();
    // }

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