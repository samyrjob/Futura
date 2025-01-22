package main;

import Entity.Player;
import mouse.HandleMouseHover;
import mouse.MyMouseAdapter;
import tile.TileManager;
import java.awt.event.MouseEvent;


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
//


    // HANDLE HOUSE HOVER

    HandleMouseHover handleMouseHover = new HandleMouseHover(this);

    public int mouseOverTileX;
    public int mouseOverTileY;

    public int mouseX = -1;
    public int mouseY = -1;

//    public int coordinate_playerX_drawn = 768 - tileSizeWidth;
//    public int coordinate_playerY_drawn = 384 - 3*tileSizeHeight;


//    INITIAL tileCenterX and tileCenterY :
//        int tileCenterX = 768;
//        int tileCenterY = 384;





    final int FPS = 60;

    Thread gameThread;
    KeyHandler key_handler = new KeyHandler();
    public TileManager tile_manager = new TileManager(this);
    MyMouseAdapter mouse_adapter = new MyMouseAdapter(this);
    public Player player = new Player(this, mouse_adapter);




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






    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(key_handler);
        this.setFocusable(true);





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
        });
    }


    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
        System.out.println("Game thread started");
    }


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
                System.out.println("xinitial = " + player.xInitial + " yinitial = " + player.yInitial + "    xfinal = " + player.xFinal + "    yfinal = " + player.yFinal);
                System.out.println("xmap current = " + player.xCurrent  + " ymap current " + player.yCurrent);
//                System.out.println("mouseX = " + mouseX + " mouseY = " + mouseY);
                System.out.println(player.direction);
                System.out.println("spriteX = " + player.spriteX + " spriteY = " + player.spriteY);
                update();
                repaint();
                delta--;
                drawCount++;
//                System.out.println("xinitial = " + player.xInitial + " yinitial = " + player.yInitial + "    xfinal = " + player.xFinal + "    yfinal = " + player.yFinal);
////                System.out.println("mouseovertileX = " + mouseOverTileX + " mouseovertileY = " + mouseOverTileY);
//                System.out.println("coordinateplayerXdrawn = " + coordinate_playerX_drawn + "    coordinateplayerYdrawn = " + coordinate_playerY_drawn);
//                System.out.println("spriteX = " + player.spriteX + " spriteY = " + player.spriteY);
            }
            if (timer >= 1_000_000_000) {
//                System.out.println("FPS : " + drawCount);
                drawCount = 0;
                timer = 0;
            }

        }
    }
//
//
    public void update() {
//        player.update1(player.xInitial, player.yInitial, player.xFinal, player.yFinal);
        player.update();


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


        //  PAINT PLAYER
        player.draw_player(g2d);
//        System.out.println("mouseOverTileX = " + mouseOverTileX + " mouseOverTileY = " + mouseOverTileY);
//


            g2d.dispose();


    }




}


