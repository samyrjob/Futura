package mouse;


import main.GamePanel;

import java.awt.*;

public class HandleMouseHover {


    GamePanel gp;


    /// TO DRAW THE POLYGON (edges on a tile)


    public HandleMouseHover(GamePanel gp){
        this.gp = gp;
    }

    public void drawPolygon (int mouseX, int mouseY, Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Point tilePoint = gp.getCalculateTileFromMouse(mouseX, mouseY);
//        System.out.println("mouseX  = " + mouseX + " mouseY = " + mouseY);
        int mapX = tilePoint.x;
        int mapY = tilePoint.y;
        //        // Do not draw if isoX or isoY is negative

        if (mapX >= 0 && mapY >= 0) {

            // Ensure the coordinates are within grid boundaries
            if (mapX < gp.maxWorldCol && mapY < gp.maxWorldRow) {

                int startX = ((mapX - mapY) * (gp.tileSizeWidth / 2)) + gp.tile_manager.xOffset + (gp.tileSizeWidth / 2);
                int startY = (mapX + mapY) * (gp.tileSizeHeight / 2) + gp.tile_manager.yOffset;

                // Draw the tile
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawPolygon(
                        new int[]{
                                startX,
                                startX + (gp.tileSizeWidth / 2),
                                startX,
                                startX - (gp.tileSizeWidth / 2)
                        },
                        new int[]{
                                startY,
                                startY + (gp.tileSizeHeight / 2),
                                startY + gp.tileSizeHeight,
                                startY + (gp.tileSizeHeight / 2)
                        },
                        4
                );


                // Check if this tile is the hovered one
                if (mapX == gp.hoveredTileX && mapY == gp.hoveredTileY) {
                    int tileCenterX = (mapX - mapY) * (gp.tileSizeWidth / 2) + gp.tile_manager.xOffset + (gp.tileSizeWidth / 2);
                    int tileCenterY = (mapX + mapY) * (gp.tileSizeHeight / 2) + gp.tile_manager.yOffset + (gp.tileSizeHeight / 2);

                    // Draw the player image and tile
//                    g2d.drawImage(tile_manager.tile[1].image, tileCenterX, tileCenterY, tileSizeWidth, tileSizeHeight, null);
//
//                    gp.coordinate_playerX_drawn = tileCenterX - gp.tileSizeWidth;
//                    gp.coordinate_playerY_drawn = tileCenterY - 3 * gp.tileSizeHeight;

//                    player.draw_player(g2d, tileCenterX - tileSizeWidth, tileCenterY - 3 * tileSizeHeight);
//                    System.out.println("tilecenterX and tilecenterY : " + tileCenterX + " ,  " + tileCenterY);
                }
            }
        }





    }
}