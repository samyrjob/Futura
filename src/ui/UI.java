package ui;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import main.GamePanel;
import object.*;


public class UI {

    GamePanel gp;
    Font theFont;
    BufferedImage credImage;

    

    public UI(GamePanel gp){

        this.gp = gp;
        theFont = new Font("Arial", Font.PLAIN, 40);
        OBJ_cred obj_cred = new OBJ_cred();
        credImage = obj_cred.image;
   



    }

    public void draw(Graphics2D g2d){
        g2d.setColor(Color.WHITE);
        g2d.setFont(theFont);
        g2d.drawImage(credImage, GamePanel.ORIGINAL_TILE_SIZE, GamePanel.ORIGINAL_TILE_SIZE, GamePanel.ORIGINAL_TILE_SIZE *2 , GamePanel.ORIGINAL_TILE_SIZE*2, null);
        String nbCredits = gp.player.getCredits() + "";
        g2d.drawString(nbCredits, 50,47);
    }





}
