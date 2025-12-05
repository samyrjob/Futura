package tile;
import main.GamePanel;
import javax.imageio.ImageIO;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class TileManager {

    GamePanel gp;
    public Tile [] tile;
    public int[][] mapTileNum;
    public int xOffset = 576; // 576
    public int yOffset = 144; // 144

    public TileManager(GamePanel gp){
        this.gp = gp;
        tile = new Tile[10];
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];
        getTileImage();
        loadMap("/res/maps/map01.txt");

    }



    public void loadMap(String filepath){


        try{
            int col =0;
            int row =0;
            InputStream map_txt_stream = getClass().getResourceAsStream(filepath);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(map_txt_stream));


            while (col < gp.maxWorldCol && row < gp.maxWorldRow){
                String line = bufferedReader.readLine();

                while (col < gp.maxWorldCol) {
                    String[] array_numinchar = line.split(" ");
                    int num = Integer.parseInt(array_numinchar[col]);
                    mapTileNum[col][row] = num;
                    col++;
                }
                if (col == gp.maxWorldCol){
                    col = 0;
                    row++;
                }

            }
            bufferedReader.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }



    }


    public void getTileImage() {
        try {
            tile[0] = new Tile();
            tile[0].image = ImageIO.read(getClass().getResourceAsStream("/res/tile/wooden_floor_tile.png"));

            tile[1] = new Tile();
            tile[1].image = ImageIO.read(getClass().getResourceAsStream("/res/tile/grass00.png"));
            

        } catch (IOException ea) {
            ea.printStackTrace();
        }
    }




    public void draw(Graphics2D g2) {
        int tileWidth = gp.tileSizeWidth ; // Horizontal width of the tile
        int tileHeight = gp.tileSizeHeight; // Vertical height of the tile
//
//         Offsets for centering the map
//        xOffset = (gp.screenWidth / 2) - (gp.maxWorldCol * gp.tileSizeWidth / 4) + (tileWidth/4);
//        yOffset = (gp.screenHeight / 2) - (gp.maxWorldRow * gp.tileSizeHeight / 1);
//
//        int xOffset = (gp.screenWidth - gp.worldWidth);
//        int yOffset = (gp.screenHeight - gp.worldHeight)/2 ;



        for (int row = 0; row < gp.maxWorldRow; row++) {
            for (int col = 0; col < gp.maxWorldCol; col++) {
                int tileNum = mapTileNum[col][row];

                // Calculate isometric coordinates
                int isoX = xOffset + (col - row) * tileWidth / 2;
                int isoY = yOffset + (col + row) * tileHeight / 2;
//                System.out.println("isoX = " + isoX + "   isoY = " + isoY);
                g2.drawImage(tile[tileNum].image, isoX, isoY, tileWidth, tileHeight, null);
            }
        }
    }


}


