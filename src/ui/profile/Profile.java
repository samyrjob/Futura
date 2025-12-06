package ui.profile;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import Entity.Player;
import main.GamePanel;

public class Profile {
    Player player;
    GamePanel gp;


    public Profile(GamePanel gp, Player player){
        this.gp = gp;
        this.player = player;
    }


    public void draw(Graphics2D g2d){

        g2d.setColor(Color.WHITE);
        // Create a BasicStroke to set the line thickness to 5 pixels
        BasicStroke thickStroke = new BasicStroke(5);

        // Set the stroke for the Graphics2D object
        g2d.setStroke(thickStroke);
        // Draw a rectangle with the gray thick line around it
        g2d.drawRoundRect(player.spriteX - 20, player.spriteY - 20, 90, 70, 35, 35); // x, y, width, height
        g2d.setStroke(new BasicStroke());
        g2d.setColor(Color.BLACK);
        g2d.fillRoundRect(player.spriteX - 20, player.spriteY - 20 , 90, 70, 35, 35);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString(player.name, player.spriteX, player.spriteY + 10);
        g2d.drawLine(player.spriteX -20 , player.spriteY + 15 , player.spriteX -20 + 90, player.spriteY  + 15 );
        g2d.drawString(player.gender.toString(), player.spriteX, player.spriteY + 35);
       
        // set stroke to do now 
        
    }

    
}
