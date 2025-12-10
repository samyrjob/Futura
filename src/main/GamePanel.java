package main;


import controller.friend.FriendController;
import model.friend.FriendRequest;
import view.friend.FriendsPanel;
import view.friend.FriendRequestPopup;

import Entity.Player;
import Entity.RemotePlayer;
import Entity.Entity.Direction;
import Entity.Entity.Gender;
import ui.profile.Profile;
import ui.profile.RemoteProfile;
import ui.UI;
import ui.hud.TileHighlighter;
import view.inventory.InventoryWindow;
import view.room.RoomNavigator;
import network.NetworkManager;
import room.RoomManager;  // ✨ NEW IMPORT
import tile.TileManager;
import object.FurnitureManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

import service.kafka.KafkaService;

/**
 * GamePanel - Main game coordinator and rendering surface
 * 
 * Responsibilities:
 * - Initialize game systems
 * - Run game loop (60 FPS)
 * - Handle user input (mouse, keyboard)
 * - Coordinate rendering
 * - Manage multiplayer connections
 * 
 * This is NOT a God Object - it's the main controller that coordinates subsystems!
 */
public class GamePanel extends JPanel implements Runnable {

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════
    
    public static final int ORIGINAL_TILE_SIZE = 16;
    public static final int TILE_SCALE = 6;
    private static final int FPS = 60;
    
    public final int tileSizeWidth = ORIGINAL_TILE_SIZE * TILE_SCALE;
    public final int tileSizeHeight = ORIGINAL_TILE_SIZE * TILE_SCALE / 2;
    
    public final int maxScreenRow = 16;
    public final int maxScreenCol = 16;
    public final int screenWidth = maxScreenCol * tileSizeWidth;
    public final int screenHeight = maxScreenRow * tileSizeHeight;
    
    public final int maxWorldCol = 9;
    public final int maxWorldRow = 5;
    public final int worldWidth = maxWorldCol * tileSizeWidth;
    public final int worldHeight = maxWorldRow * tileSizeHeight;
    
    // ═══════════════════════════════════════════════════════════
    // CORE GAME SYSTEMS
    // ═══════════════════════════════════════════════════════════
    
    // Game loop
    private Thread gameThread;
    
    // Player
    public Player player;
    
    // Managers
    public TileManager tile_manager;
    public FurnitureManager furnitureManager;
    public NetworkManager networkManager;
    public RoomManager roomManager;  // ✨ NEW - Room system
    
    // UI Components
    private UI ui;
    private Profile profile;
    private RemoteProfile remoteProfile;
    public InventoryWindow inventoryWindow;
    public RoomNavigator roomNavigator;  // ✨ NEW - Room navigation UI
    
    // Input handlers
    private TileHighlighter handleMouseHover;
    
    // Audio (paused)
    public Sound sound;
    private Sound se;

    // friend request fields
    //-------------------------------
    public FriendController friendController;
    public FriendsPanel friendsPanel;
    public FriendRequestPopup friendRequestPopup;
// NOTE: No more kafkaConsumer field - it's handled inside FriendController!

    public KafkaService kafkaService;

    
    // ═══════════════════════════════════════════════════════════
    // MULTIPLAYER STATE
    // ═══════════════════════════════════════════════════════════
    
    private Map<String, RemotePlayer> remotePlayers;
    private boolean multiplayerEnabled = false;
    
    // ═══════════════════════════════════════════════════════════
    // MOUSE/CAMERA STATE
    // ═══════════════════════════════════════════════════════════
    
    // Mouse position
    public int mouseX = -1;
    public int mouseY = -1;
    
    // Tile selection
    private int previousTileX = -1;
    private int previousTileY = -1;
    public int hoveredTileX = -1;
    public int hoveredTileY = -1;
    public int mouseOverTileX;
    public int mouseOverTileY;
    
    // Camera dragging
    private boolean isDragging = false;
    private int dragStartX = 0;
    private int dragStartY = 0;
    private int originalXOffset;
    private int originalYOffset;
    
    // UI state
    private Boolean displayProfile = false;
    
    // Message animation
    private Timer messageTimer;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR & INITIALIZATION
    // ═══════════════════════════════════════════════════════════
    
    public GamePanel(String username, String genderStr) {
        initializePanel();
        initializePlayer(username, genderStr);
        initializeManagers();
        initializeFriendSystem();  // ✨ MOVE HERE - before UI and game loop
        initializeUI();
        initializeInput();
        initializeMultiplayer();
        initializeMessageTimer();
        initializeMusic();  // ✨ ADD THIS
    }
    
    private void initializePanel() {
        this.setLayout(null);
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initializePlayer(String username, String genderStr) {
        Gender gender = genderStr.equalsIgnoreCase("female") ? Gender.FEMALE : Gender.MALE;
        this.player = new Player(this,  username, gender);
    }
    
    private void initializeManagers() {



        this.tile_manager = new TileManager(this);
        this.furnitureManager = new FurnitureManager(this);
        this.handleMouseHover = new TileHighlighter(this);
        
        // ✨ NEW - Initialize room system AFTER other managers
        this.roomManager = new RoomManager(this);
    }
    
    private void initializeUI() {
        this.ui = new UI(this);
        this.profile = new Profile(this, player);
        this.remoteProfile = new RemoteProfile(this);
        this.inventoryWindow = new InventoryWindow(this);
        
        // ✨ NEW - Initialize room navigator AFTER roomManager exists
        this.roomNavigator = new RoomNavigator(this, roomManager);
    }
    
    private void initializeInput() {

        
        // Complex mouse listeners
        addMouseListener(new GameMouseListener());
        addMouseMotionListener(new GameMouseMotionListener());
          
    // ✨ ADD THIS - Keyboard listener
    addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent e) {
            if (ui == null) return;
            
            switch (e.getKeyCode()) {
                case java.awt.event.KeyEvent.VK_UP:
                    ui.increaseVolume();  // Arrow UP = volume up
                    repaint();
                    break;
                case java.awt.event.KeyEvent.VK_DOWN:
                    ui.decreaseVolume();  // Arrow DOWN = volume down
                    repaint();
                    break;
            }
        }
    });
    }
    
    private void initializeMultiplayer() {
        this.remotePlayers = new HashMap<>();
        this.networkManager = new NetworkManager(this);
        this.player.setNetworkManager(networkManager);
        
        // Friend system is now initialized in initializeFriendSystem()
        // Kafka is handled internally by FriendController!
    }


        /**
     * Initialize the Friend system (MVC pattern)
     * Call this AFTER player is created
     */
    private void initializeFriendSystem() {
        try {
            // Initialize Kafka service FIRST
            this.kafkaService = new KafkaService(player.name);
            
            // Create controller (handles business logic)
            this.friendController = new FriendController(
                this, 
                player.name, 
                player.gender.toString()
            );
            
            // Connect Kafka to controller and start
            this.kafkaService.setFriendController(friendController);
            this.kafkaService.start();
            
            // Create view components
            this.friendsPanel = new FriendsPanel(this, friendController);
            this.friendRequestPopup = new FriendRequestPopup(this, friendController);
            
            System.out.println("[GAME] Friend system initialized (MVC)");
        } catch (Exception e) {
            System.err.println("[GAME] Failed to initialize friend system: " + e.getMessage());
            e.printStackTrace();
        }
    }


        
    private void initializeMessageTimer() {
        messageTimer = new Timer(100, e -> updateMessages());
        messageTimer.start();
    }


    // ✨ ADD THIS METHOD
    private void initializeMusic() {
        // Load the song
        sound = new Sound();
        // sound.setFile("src\\res\\sound\\Move For Me - DJ XOXO _ New Summer Dance Hit 2025.wav");  // Update path to your file
        sound.setFile("src\\res\\sound\\becky_g_arranca.wav");  // Update path to your file

        // Start playing on loop
        sound.loop();
    }
    
    // ═══════════════════════════════════════════════════════════
    // GAME LOOP
    // ═══════════════════════════════════════════════════════════
    
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
        System.out.println("Game thread started");
    }
    
    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        long lastTime = System.nanoTime();
        double delta = 0;
        
        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }
    
    // The update method stays mostly the same
    public void update() {
        player.update();
        
        synchronized (remotePlayers) {
            for (RemotePlayer remotePlayer : remotePlayers.values()) {
                remotePlayer.update();
            }
        }

        // ✨ Update friend request popup (same as before)
        if (friendRequestPopup != null) {
            friendRequestPopup.update();
        }
        
        // ✨ NEW - Update friends panel (for any animations)
        if (friendsPanel != null) {
            friendsPanel.update();
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // RENDERING
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Draw world
        drawWorld(g2d);
        
        // Draw entities
        drawEntities(g2d);
        
        // Draw UI
        drawUI(g2d);
        
        // Cleanup
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.dispose();
    }
    
    private void drawWorld(Graphics2D g2d) {
        tile_manager.draw(g2d);
        furnitureManager.draw(g2d);
        handleMouseHover.drawPolygon(mouseX, mouseY, g2d);
    }
    
    private void drawEntities(Graphics2D g2d) {
        // Local player
        player.draw(g2d);
        
        // Remote players
        synchronized (remotePlayers) {
            for (RemotePlayer rp : remotePlayers.values()) {
                rp.draw(g2d);
            }
        }
        
        // Chat bubbles
        drawChatBubbles(g2d);
    }
    
    private void drawUI(Graphics2D g2d) {
    ui.draw(g2d);
    
    if (displayProfile) {
        profile.draw(g2d);
    }
    
    if (remoteProfile.isVisible()) {
        remoteProfile.draw(g2d);
    }
    
    inventoryWindow.drawPlacementPreview(g2d);
    inventoryWindow.draw(g2d);
    
    // ✨ NEW - Draw room navigator (always last so it's on top)
    roomNavigator.draw(g2d);

    // ✨ FIX - Add null checks for friend system (initialized later in setupGame)
    if (friendsPanel != null) {
        friendsPanel.draw(g2d);
    }
    
    if (friendRequestPopup != null) {
        friendRequestPopup.draw(g2d);
    }
}
    
    private void drawChatBubbles(Graphics2D g2d) {
        // Local player messages
        for (Entity.Player.Message msg : player.messages) {
            drawChatBubble(g2d, msg.text, player.spriteX, msg.y);
        }
        
        // Remote player messages
        synchronized (remotePlayers) {
            for (RemotePlayer rp : remotePlayers.values()) {
                for (RemotePlayer.Message msg : rp.messages) {
                    drawChatBubble(g2d, msg.text, rp.spriteX, msg.y);
                }
            }
        }
    }
    
    private void drawChatBubble(Graphics2D g2d, String text, int spriteX, int y) {
        if (y < -50) return;
        
        String displayText = text.length() > 200 ? text.substring(0, 200) : text;
        
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(displayText);
        int bubbleWidth = textWidth + 50;
        int bubbleHeight = 35;
        int bubbleX = spriteX + (2 * tileSizeWidth) - 30;
        int bubbleY = y;
        
        // Draw bubble
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 20, 20);
        
        // Draw text
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
        
        // Draw pointer
        int[] xPoints = {bubbleX, bubbleX - 10, bubbleX};
        int[] yPoints = {bubbleY + 10, bubbleY + 17, bubbleY + 24};
        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(xPoints, yPoints, 3);
        g2d.setColor(Color.BLACK);
        g2d.drawPolyline(xPoints, yPoints, 3);
    }
    
    // ═══════════════════════════════════════════════════════════
    // MULTIPLAYER CALLBACKS
    // ═══════════════════════════════════════════════════════════
    
    public void setupGame() {
        
        // Connect to multiplayer server
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

    public synchronized void removeAllRemotePlayers() {
        remotePlayers.clear();
        System.out.println("Cleared all remote players");
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
    
    // ═══════════════════════════════════════════════════════════
    // COORDINATE CONVERSION
    // ═══════════════════════════════════════════════════════════
    
    private Point calculateTileFromMouse(int mouseX, int mouseY) {
        int adjustedX = mouseX - tile_manager.xOffset;
        int adjustedY = mouseY - tile_manager.yOffset;
        
        float isoX = (float) adjustedX / (tileSizeWidth / 2);
        float isoY = (float) adjustedY / (tileSizeHeight / 2);
        
        int mapX = (int) Math.floor((isoX + isoY) / 2);
        int mapY = (int) Math.floor((isoY - isoX) / 2);

        //! calculate mouseX and mouseY and tile mapX mapY
        
        // System.out.println("Mouse: (" + mouseX + ", " + mouseY + ") → Tile: (" + mapX + ", " + mapY + ")");
        
        return new Point(mapX, mapY);
    }
    
    public Point getCalculateTileFromMouse(int x, int y) {
        return calculateTileFromMouse(x, y);
    }
    
    // ═══════════════════════════════════════════════════════════
    // CAMERA/VIEW MANAGEMENT
    // ═══════════════════════════════════════════════════════════
    
    private void constrainMapOffsets() {
        int minXOffset = -worldWidth + screenWidth / 2;
        int maxXOffset = screenWidth / 2;
        int minYOffset = -worldHeight + screenHeight / 2;
        int maxYOffset = screenHeight / 2;
        
        tile_manager.xOffset = Math.max(minXOffset, Math.min(maxXOffset, tile_manager.xOffset));
        tile_manager.yOffset = Math.max(minYOffset, Math.min(maxYOffset, tile_manager.yOffset));
    }
    
    // ═══════════════════════════════════════════════════════════
    // MESSAGE ANIMATION
    // ═══════════════════════════════════════════════════════════
    
    private void updateMessages() {
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
    }
    
    // ═══════════════════════════════════════════════════════════
    // CLEANUP
    // ═══════════════════════════════════════════════════════════
    
    public void cleanup() {
        System.out.println("[GAME] Cleaning up...");

        // Shutdown friend system (controller handles Kafka internally)
        if (friendsPanel != null) {
            friendsPanel.shutdown();
        }

        if (networkManager != null && multiplayerEnabled) {
            networkManager.sendByeMessage();
            networkManager.disconnect();
        }

          if (kafkaService != null) {
            kafkaService.shutdown();
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // AUDIO (PAUSED)
    // ═══════════════════════════════════════════════════════════
    
    public void stopSong() {
        if (sound != null) sound.stop();
    }
    
    public void playSE(int i) {
        if (se != null) {
            se.setFile(i);
            se.play();
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // INNER CLASSES - MOUSE LISTENERS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Handles mouse clicks (tile selection, player interaction)
     */
    private class GameMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            handleMousePressed(e);
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            handleMouseReleased(e);
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            handleMouseClicked(e);
        }
    }
    
    /**
     * Handles mouse dragging (camera movement) and movement
     */
    private class GameMouseMotionListener extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            handleMouseDragged(e);
        }
        
        @Override
        public void mouseMoved(MouseEvent e) {
            handleMouseMoved(e);
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // MOUSE EVENT HANDLERS
    // ═══════════════════════════════════════════════════════════
    
    private void handleMousePressed(MouseEvent e) {

        mouseX = e.getX();
        mouseY = e.getY();

        if (friendRequestPopup != null && friendRequestPopup.isVisible()) {
                // ✨ NEW - Check friend request popup FIRST (highest priority)
            if (friendRequestPopup.isVisible()) {
                if (friendRequestPopup.handleClick(mouseX, mouseY)) {
                    repaint();
                    return;
                }
            }
    // ...
        }

        if (friendsPanel != null && friendsPanel.isVisible()) {
        // ...
            // ✨ NEW - Check friends panel
            if (friendsPanel.isVisible()) {
                friendsPanel.handleMousePressed(mouseX, mouseY);
                if (friendsPanel.isDragging()) {
                    return;
                }
                if (friendsPanel.handleClick(mouseX, mouseY)) {
                    repaint();
                    return;
                }
            }
        }
        
        // ✨ NEW - Check remote profile friend button
        if (remoteProfile.isVisible()) {
            if (remoteProfile.handleClick(mouseX, mouseY)) {
                repaint();
                return;
            }
        }

            // ✨ NEW - Handle room navigator dragging
        if (roomNavigator.isVisible()) {
            roomNavigator.handleMousePressed(e.getX(), e.getY());
            if (roomNavigator.isDragging()) {
                return;  // Don't process other clicks while starting drag
            }
            roomNavigator.handleClick(e.getX(), e.getY());
            repaint();
            return;
        }
    
           // ✨ ADD THIS - Update music player hover states
        if (ui != null) {
            ui.updatePlayButtonHover(mouseX, mouseY);
            ui.updateStopButtonHover(mouseX, mouseY);
        }
        // ✨ NEW - Check room navigator FIRST (highest priority)
        if (roomNavigator.isVisible()) {
            roomNavigator.handleClick(e.getX(), e.getY());
            repaint();
            return;
        }
        
        // Inventory window
        if (inventoryWindow.isVisible()) {
            inventoryWindow.handleClick(e.getX(), e.getY());
            repaint();
            return;
        }
        
        // Placement mode
        if (inventoryWindow.isPlacementMode()) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                inventoryWindow.confirmPlacement();
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                inventoryWindow.cancelPlacement();
            }
            repaint();
            return;
        }
        
        // Start dragging
        if (e.getButton() == MouseEvent.BUTTON1) {
            isDragging = true;
            dragStartX = e.getX();
            dragStartY = e.getY();
            originalXOffset = tile_manager.xOffset;
            originalYOffset = tile_manager.yOffset;
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }
    }
    
    private void handleMouseReleased(MouseEvent e) {

           // ✨ NEW - Friends panel release
        if (friendsPanel != null && friendsPanel.isVisible()) {
            friendsPanel.handleMouseReleased();
        }

          // ✨ NEW - Stop room navigator dragging
        if (roomNavigator.isVisible()) {
            roomNavigator.handleMouseReleased();
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
            isDragging = false;
            setCursor(Cursor.getDefaultCursor());
        }
        
        inventoryWindow.handleRelease();
    }
    
    private void handleMouseClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) return;
        
        int mouseX = e.getX();
        int mouseY = e.getY();

        // ✨ Music player clicks (at the very beginning)
    if (ui != null) {
        if (ui.isPlayButtonClicked(mouseX, mouseY)) {
            if (sound != null) {
                sound.togglePlayPause();
            }
            repaint();
            return;
        }
        
        if (ui.isStopButtonClicked(mouseX, mouseY)) {
            if (sound != null) {
                sound.stop();
            }
            repaint();
            return;
        }
        
        // ✨ ADD THESE - Volume button clicks
        if (ui.isVolumeUpClicked(mouseX, mouseY)) {
            ui.increaseVolume();  // +10%
            repaint();
            return;
        }
        
        if (ui.isVolumeDownClicked(mouseX, mouseY)) {
            ui.decreaseVolume();  // -10%
            repaint();
            return;
        }
    }
        
        
        // ✨ NEW - Check room navigator first
        if (roomNavigator.isVisible()) {
            // Already handled in handleMousePressed
            return;
        }
        
        // Check local player
        if (player.contains(mouseX, mouseY)) {
            displayProfile = !displayProfile;
            remoteProfile.hideProfile();
            return;
        }
        
        // Check remote players
        if (checkRemotePlayerClick(mouseX, mouseY)) {
            return;
        }
        
        // Handle tile click
        handleTileClick(mouseX, mouseY);
    }
    
    private boolean checkRemotePlayerClick(int mouseX, int mouseY) {
        synchronized (remotePlayers) {
            for (RemotePlayer remotePlayer : remotePlayers.values()) {
                if (remotePlayer.contains(mouseX, mouseY)) {
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
                    return true;
                }
            }
        }
        return false;
    }
    
    private void handleTileClick(int mouseX, int mouseY) {
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
    
    private void handleMouseDragged(MouseEvent e) {


                // ✨ NEW - Friends panel dragging
        if (friendsPanel.isVisible() && friendsPanel.isDragging()) {
            friendsPanel.handleMouseDragged(e.getX(), e.getY());
            repaint();
            return;
        }

        // ✨ NEW - Room navigator dragging
        if (roomNavigator.isVisible() && roomNavigator.isDragging()) {
            roomNavigator.handleMouseDragged(e.getX(), e.getY());
            repaint();
            return;
        }

        // Inventory dragging
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
            
            synchronized (remotePlayers) {
                for (RemotePlayer remotePlayer : remotePlayers.values()) {
                    remotePlayer.updateSpritePosition();
                }
            }
            
            constrainMapOffsets();
            repaint();
        }
    }
    
    private void handleMouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();

            // ✨ NEW - Update friend popup hover
        if (friendRequestPopup != null && friendRequestPopup.isVisible()) {
            friendRequestPopup.handleMouseMove(mouseX, mouseY);
        }
        
        // ✨ NEW - Update friends panel hover
        if (friendsPanel != null && friendsPanel.isVisible()) {
            friendsPanel.handleMouseMove(mouseX, mouseY);
        }
        
        // ✨ NEW - Update remote profile hover (for friend button)
        if (remoteProfile.isVisible()) {
            remoteProfile.handleMouseMove(mouseX, mouseY);
        }

        // ✨ UPDATE THIS - Add volume button hovers
        if (ui != null) {
            ui.updatePlayButtonHover(mouseX, mouseY);
            ui.updateStopButtonHover(mouseX, mouseY);
            ui.updateVolumeUpHover(mouseX, mouseY);    // ✨ ADD THIS
            ui.updateVolumeDownHover(mouseX, mouseY);  // ✨ ADD THIS
        }
        
        // ✨ NEW - Update room navigator hover state
        if (roomNavigator.isVisible()) {
            roomNavigator.handleMouseMove(mouseX, mouseY);
        }
        
        if (inventoryWindow.isPlacementMode()) {
            inventoryWindow.updatePlacementPreview(e.getX(), e.getY());
            repaint();
        }
    }


    private void handleMouseWheelMoved(MouseWheelEvent e) {
    // ✨ NEW - Friends panel scrolling
    if (friendsPanel != null && friendsPanel.isVisible() && friendsPanel.containsPoint(e.getX(), e.getY())) {
        friendsPanel.handleScroll(e.getWheelRotation());
        repaint();
        return;
    }
    
    // ... rest of existing scroll handling ...
}


        // ─────────────────────────────────────────────────────────────────────────────
    // 5. ADD THESE NEW PUBLIC METHODS (for friend notifications)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Called by FriendManager when a friend request is received
     */
    public void showFriendRequestPopup(FriendRequest request) {
        // Find the sender's RemotePlayer (if they're in the same room)
        RemotePlayer sender = null;
        synchronized (remotePlayers) {
            sender = remotePlayers.get(request.getFromUsername());
        }
        
        friendRequestPopup.show(request, sender);
        repaint();
    }

    /**
     * Called by FriendManager to show friend notifications
     */
    public void showFriendNotification(String message, boolean success) {
        // Show as chat bubble above player
        int bubbleY = player.spriteY + 50;
        String prefix = success ? "[FRIEND] ✓ " : "[FRIEND] ";
        player.messages.add(new Entity.Player.Message(prefix + message, bubbleY));
        repaint();
    }

}