package ui.hud;


import main.GamePanel;

import java.awt.*;

public class TileHighlighter {


    GamePanel gp;


    /// TO DRAW THE POLYGON (edges on a tile)


    public TileHighlighter(GamePanel gp){
        this.gp = gp;
    }



public void drawPolygon(int mouseX, int mouseY, Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    Point tilePoint = gp.getCalculateTileFromMouse(mouseX, mouseY);
    int mapX = tilePoint.x;
    int mapY = tilePoint.y;

    if (mapX >= 0 && mapY >= 0 && mapX < gp.maxWorldCol && mapY < gp.maxWorldRow) {
        // Calculate tile center position
        int startX = (mapX - mapY) * (gp.tileSizeWidth / 2) + gp.tile_manager.xOffset + (gp.tileSizeWidth / 2);
        int startY = (mapX + mapY) * (gp.tileSizeHeight / 2) + gp.tile_manager.yOffset;

        // Draw the diamond outline
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawPolygon(
            new int[]{
                startX,                           // Top
                startX + (gp.tileSizeWidth / 2),  // Right
                startX,                           // Bottom
                startX - (gp.tileSizeWidth / 2)   // Left
            },
            new int[]{
                startY,                           // Top
                startY + (gp.tileSizeHeight / 2), // Right
                startY + gp.tileSizeHeight,       // Bottom
                startY + (gp.tileSizeHeight / 2)  // Left
            },
            4
        );
    }
}
}